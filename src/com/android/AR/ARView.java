package com.android.AR;

import java.io.InputStream;
import java.util.Random;
import java.util.Enumeration;
import java.util.Vector;
import java.util.LinkedList;
import java.util.Iterator;

import com.android.R;

import android.R.color;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.PorterDuff;
//some other changes here haha
public class ARView extends SurfaceView implements SurfaceHolder.Callback{//ARSurfaceView is also a SurfaceView. It has a thread called 
	//ARThread, which is used to refresh the display of the AR Elements.
	private Context AR_Context;
	private ARThread AR_thread;
	private SurfaceHolder AR_holder;

	private SurfaceHolder holder = null;
	Paint mPaint = new Paint();	
	public float screenWidth=10;
	public float screenHeight=5;

	//these two parameters need to be updated for different models
	private float xAngleWidth = 50f;
	private float yAngleWidth = 40f;
	volatile LinkedList<BF> flyingList = new LinkedList<BF>();
	volatile LinkedList<BF> catchedList = new LinkedList<BF>();

	public final float threshold_Close2BF=15;
	public final int visible_range=100;//the range that the butterflys are visible

	public volatile boolean ifThreadRun=false;
	public SensorData sensor;

	//ARThread
	class ARThread extends Thread {
		private SurfaceHolder _holder=null;
		private Context _context;
		public ARThread(SurfaceHolder surfaceHolder, Context context)
		{			_holder = surfaceHolder;
		_context = context;		}

		public void setRunning(boolean b) 
		{			ifThreadRun = b;		}

		/* callback invoked when the surface dimensions change. */
		// public void setSurfaceSize(int width, int height) {
		// // synchronized to make sure these all change atomically

		@Override
		public void run() {
			Canvas c;
			while (true) {
				c=null;
				try {
					c = _holder.lockCanvas();
					synchronized (_holder) {
						if(c!=null)
						{
							c.drawColor(0, Mode.CLEAR);
							onDraw(c);
							ifThreadRun=false;}
					}
				} 
				finally {
					if (c != null) {
						_holder.unlockCanvasAndPost(c);
					}	
				}				
				try {sleep(1);} catch (Exception e) {}
			}
		}
	}
	@Override
	protected void onDraw(Canvas c) //calculate the parameters
	//to display the AR element (rotation, etc.) Orientation  -the angle between the north and the y axis of the phone. location  -the 
	// the location of the phone. x_axis  -the rotation angle of the phone x axis
	{						
		float x_axis=sensor.phonedata.x_axis;float y_axis=sensor.phonedata.y_axis;float z_axis=sensor.phonedata.z_axis;
		float device_orientation=sensor.phonedata.device_orientation;
		Location location=sensor.phonedata.location;
		Iterator<BF> e = flyingList.iterator();
		if (flyingList.size() == 0)
			return;
		int x,y;
		float distance,rotation,scale;
		boolean picture_view_changed=false; boolean picture_position_changed=false;
		while (e.hasNext()) {
			try {
				BF element = e.next();
				element.visible=true;
				distance=element.getDistance();
				element.vertical_offset=(float)Math.atan((element.location.getAltitude()-location.getAltitude())/distance)*180/(float)Math.PI+90;
				scale=element.distance/distance;
				rotation=y_axis;
				if(Calculator.isPictureSizeOrRotationChanged(scale,y_axis))
				{
					picture_view_changed=true;
				}
				float tem_orientation=location.bearingTo(element.location)-device_orientation-90;
				if (tem_orientation>180) tem_orientation=tem_orientation-360;
				if (tem_orientation<-180) tem_orientation=tem_orientation+360;
				element.orientation_2_element=tem_orientation;
				x=(int) Calculator.calPositionX(element.orientation_2_element,x_axis,y_axis,z_axis,screenWidth,screenHeight,xAngleWidth,yAngleWidth,element.resized_picture.getWidth(),element.resized_picture.getHeight());
				y= (int) Calculator.calPositionY(element.orientation_2_element,x_axis,y_axis,z_axis,element.vertical_offset,screenWidth,screenHeight,xAngleWidth,yAngleWidth,element.resized_picture.getWidth(),element.resized_picture.getHeight());
				if(Calculator.isPicturePositionChanged(x-element.x, y-element.y))
				{picture_position_changed=true;}
				if (Calculator.isPictureOutOfRange(x,y))
					element.visible=false;
				if (!element.visible)
					continue;
				if (element.resized_picture != null)
				{
					if(picture_position_changed==true||picture_view_changed==true)
					{			c.drawBitmap(element.resized_picture, x,y, null);

					showInfoTab(x, y, c,element);
					element.resized_picture = Calculator.transformImage(element.picture,element.width(), element.height(),rotation);
					}
					else
					{	c.drawBitmap(element.resized_picture, element.x,element.y, null);

					showInfoTab(element.x, element.y, c,  element);
					}
					element.picture_changed=false;element.rotation=rotation;element.x=x;element.y=y;element.distance=distance;
				}
			} catch (Exception x1) {
				Log.e("ArLayout", x1.getMessage());
			}
		}
	}
	public void showInfoTab(int x, int y, Canvas c, BF element){
		if(element.isChecked){
			Paint p=new Paint();
			int delta=5;int text_height=40;int text_size=30;
			int rect_width=300;int rect_height=3*text_height+delta;
			int left=x+element.width()+delta;
			int top=y-rect_height;
			int right=left+rect_width;
			int bottom=top+rect_height;
			RectF rect=new RectF(left,top,right,bottom);
			p.setColor(Color.LTGRAY);
			c.drawRoundRect(rect,1,2,p);
			p.setColor(Color.MAGENTA);
			p.setTextSize(text_size);
			c.drawText("Distance   "+String.valueOf(element.distance),left+delta,top+text_size+delta,p);
			c.drawText("Type         "+String.valueOf(element.type),left+delta,top+delta+text_size+text_height,p);
			c.drawText("Value        "+String.valueOf(element.value),left+delta,top+delta+text_size+2*text_height,p);


		}
	}
	public ARView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		AR_Context = context;
		AR_holder = getHolder();
		AR_holder.addCallback(this);
		AR_thread=new ARThread(AR_holder,AR_Context);
		sensor=new SensorData(context,this);
		BF.deviceLocation=sensor.curLocation;
		Bitmap picture;
		BF element;
		InputStream is1 =getResources().openRawResource(R.drawable.images);
		picture = BitmapFactory.decodeStream(is1);
		Location loc1=new Location("GPS");
		loc1.setLatitude(sensor.curLocation.getLatitude()-0.0002);
		loc1.setLongitude(sensor.curLocation.getLongitude());
		loc1.setAltitude(sensor.curLocation.getAltitude());
		element=new BF(AR_Context, loc1,picture,1) ;
		element.picture_size = 1;
		element.visible = true;
		element.name = "first";
		this.addBF(element);
		loc1.setLatitude(sensor.curLocation.getLatitude());
		loc1.setLongitude(sensor.curLocation.getLongitude()-0.0003);
		element=new BF(AR_Context,loc1,picture,1) ;
		element.picture_size = 1;
		element.visible = true;
		element.name = "second";
		this.addBF(element);
		catchBF(element);
		//onDraw(canvas);
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {}
	
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		setWillNotDraw(false);
		AR_thread.setRunning(true);
		AR_thread.start();
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */

	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		try{
			AR_thread.setRunning(false);
			AR_thread.join();
		}
		catch(InterruptedException e){}
	}





	public int numOfBF(){return flyingList.size();}
	public int numOfBFWithinRange(int radious){
		int num=0;
		for (BF bf:flyingList){
			if (bf.distance<radious) num++;
		}
		return num;
	}
	public void updateBFLists(){//update both the flying and cathed lists
		for (BF bf:flyingList){
			if(bf.getDistance()<threshold_Close2BF)
				catchBF(bf);
		}
	}
	public void generateBF(){
		Random random=new Random();
		int num=random.nextInt(7)+3;//number of butterflys to be generated

		double lati,longi;
		Bitmap picture;
		BF element;
		InputStream is;
		Bitmap [] pictures=new Bitmap[4];
		is=getResources().openRawResource(R.drawable.butterfly1);
		pictures[0] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.butterfly2);
		pictures[1] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.butterfly3);
		pictures[2] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.butterfly4);
		pictures[3] = BitmapFactory.decodeStream(is);
		for (int i=0;i<num;++i){
			int BFNo=random.nextInt(4);
			picture=pictures[BFNo];
			lati=(2*random.nextDouble()-1)/2000;
			longi=(2*random.nextDouble()-1)/2000;
			if (Math.abs(lati)<0.00015) lati=0.0002;
			if (Math.abs(longi)<0.0002) longi=0.0002;
			Location loc1=new Location("GPS");
			loc1.setLatitude(sensor.curLocation.getLatitude()+lati);
			loc1.setLongitude(sensor.curLocation.getLongitude()+longi);
			loc1.setAltitude(sensor.curLocation.getAltitude());
			element=new BF(AR_Context, loc1,picture,BFNo) ;
			element.picture_size = 1;
			element.visible = true;
			this.addBF(element);
		}
	}
	public void catchBF(BF bf){
		flyingList.remove(bf);
		catchedList.add(bf);
	}

	public void addBF(BF element) {flyingList.add(element);}
	public void removeBF(BF element) {flyingList.remove(element);}
	public ARThread GetThread() {return (getAR_thread());}
	public void close() {this.getAR_thread().setRunning(false);}
	protected void onDestroy() {}
	public void setAR_thread(ARThread aR_thread) {AR_thread = aR_thread;}
	public ARThread getAR_thread() {return AR_thread;}

}

