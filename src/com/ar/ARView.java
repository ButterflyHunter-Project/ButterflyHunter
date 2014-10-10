package com.ar;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.LinkedList;
import java.util.Iterator;

import com.util.Calculator;
import com.util.CanvasPainter;
import com.util.RadarView;
import com.butterflyHunter.R;
import com.util.SensorData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.location.Location;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
ARView contains all the virtual butterflies and includes a thread for drawing the butterflies.
 * */
public class ARView extends SurfaceView implements SurfaceHolder.Callback{
	private Context AR_Context;
	public ARThread AR_thread;//the thread for updating the surfaceview (redraw the butterflies)
	private SurfaceHolder AR_holder;
	private volatile boolean ifUpdate=false;//when ifUpdate=true, the list of butterflies should be updates (e.g. a butterfly was catched)
	private volatile boolean ifReset=false;
	private float screenWidth=10;
	private float screenHeight=5;
	volatile int numberBF=0;
	volatile int pre_numberBF=0;

	//these two parameters need to be updated for different models
	private float xAngleWidth = 50f;//the anglewidth of the camera
	private float yAngleWidth = 40f;

	private volatile LinkedList<BF> flyingList = new LinkedList<BF>();//list that contains flying (alive) butterflies
	volatile ArrayList<BF> catchedList = new ArrayList<BF>();//list that contains catched butterfies

	private final float threshold_Close2BF=15;//when distance of the butterfly is smaller than this threshold, catch it
	private final int visible_range=100;//the range that the butterflys are visible
	private volatile boolean ifThreadRun=false;//when ifThreadRun is set to true, the ARThread runs
	private SensorData sensor;//data from the sensors
	private RadarView radar;
	private CanvasPainter info;

	public SensorData getSensor(){return this.sensor;}

	public List<BF> getFlyingList(){return flyingList;}

	public List<BF> getCatchedList(){return this.catchedList;}

	public float getScreenWidth(){return this.screenWidth;}

	public float getScreenHeight(){return this.screenHeight;}

	public void setIfUpdate(boolean b){this.ifUpdate=b;}

	public float getThreshold_Close2BF(){return threshold_Close2BF;}

	public ARThread getThread(){return this.AR_thread;}

	public void setThread(ARThread aR_thread) {AR_thread = aR_thread;}

	public void reset(){this.ifReset=true;}

	public int getNumberOfBF(){	return numberBF;}

	public void setNumberOfBF(int n){numberBF=n;}

	public float getXAngle(){return this.xAngleWidth;}

	public float getYAngle(){return this.yAngleWidth;}

	//ARThread for updating (redrawing) the canvas
	public class ARThread extends Thread {
		protected int animation_start=0;
		private SurfaceHolder _holder=null;
		private int toastDuration=0;

		public ARThread(SurfaceHolder surfaceHolder, Context context)
		{_holder = surfaceHolder;}

		public void setRunning(boolean b) 
		{ifThreadRun = b;}

		public int getAnimationStart(){return animation_start;}

		public void setAnimationStart(int start){this.animation_start=start;}

		@SuppressLint("WrongCall")
		@Override
		public void run() {
			Canvas c;
			while (ifThreadRun) {
				c=null;
				try {
					c = _holder.lockCanvas();
					BF.incTimer();//for butterfly animation
					synchronized (_holder) {
						if(c!=null)
						{
							c.drawColor(0, Mode.CLEAR);
							radar.drawRadar(c);//draw radar view
							info.showNumber(c);//show number of butterflies
							if(toastDuration>0)//show toasts
							{	
								info.showToast(c);
								toastDuration--;
							}
							if(ifReset){
								updateBFLists();
								removeAllBF();
								generateBF();
								ifReset=false;
							}
							else{
								if(ifUpdate){	
									if(updateBFLists()) {
										toastDuration=8;
									}
									if (numOfBFWithinRange(visible_range)<3)
										generateBF();
								}
							}
							info.onDraw(c);//show butterflies
						}
					}
				} 
				finally {
					if (c != null) {
						_holder.unlockCanvasAndPost(c);
					}	
				}				
				try {sleep(100);} catch (Exception e) {}
			}
		}
	}



	@SuppressLint("NewApi")
	public ARView(Context context, CameraView camera, int screenWidth, int screenHeight) {
		super(context);
		// TODO Auto-generated constructor stub
		AR_Context = context;
		AR_holder = getHolder();
		AR_holder.addCallback(this);
		AR_thread=new ARThread(AR_holder,AR_Context);

		sensor=new SensorData(context,this);
		BF.deviceLocation=sensor.getCurLocation();
		this.screenHeight=screenHeight;
		this.screenWidth=screenWidth;
		radar=new RadarView(sensor,this);
		info=new CanvasPainter(this);
		generateBF();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		setWillNotDraw(false);
		AR_thread.setRunning(true);
		AR_thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		try{
			AR_thread.setRunning(false);
			AR_thread.join();
			AR_thread.interrupt();
		}
		catch(InterruptedException e){}
	}

	public void removeAllBF(){if(flyingList!=null) flyingList.clear();}
	
	public void addBF(BF element) {flyingList.add(element);}
	
	public void removeBF(BF element) {flyingList.remove(element);}
	
	public void close() {this.getThread().setRunning(false);}
	
	protected void onDestroy() {}
	
	private int numOfBFWithinRange(int radious){//get the number of flying butterflies within a certain range
		int num=0;
		for (BF bf:flyingList){
			if (bf.distance<radious) num++;
		}
		return num;
	}

	public boolean updateBFLists(){//update both the flying and cathed lists
		BF bf;
		if (flyingList!=null)
			if (flyingList.size()!=0)
				for (int i=0;i<flyingList.size();++i){
					bf=flyingList.get(i);
					if(bf.isLongClicked)
						return catchBF(bf);
					else if(bf.getDistance()<threshold_Close2BF)
						return catchBF(bf);
				}
		return false;
	}

	public boolean catchBF(BF bf){
		if(flyingList!=null&&catchedList!=null)
		{	if(flyingList.contains(bf))
		{flyingList.remove(bf);
		pre_numberBF=numberBF;
		numberBF+=bf.number;
		//NOTE: use bf.ini_distance to represent the line slope for animation
		if(Math.abs(bf.x+bf.width()/2)>1)
			bf.ini_distance=(float) ((screenHeight-bf.y-bf.height()/2)/((float)bf.x+bf.width()/2));
		else bf.ini_distance=Integer.MAX_VALUE;
		bf.ini_rotation=1;//ini_rotation=1, continue animation; otherwise stop
		catchedList.add(bf);
		return true;}
		}
		return false;
	}

	public void generateBF(){
		Random random=new Random();
		int num=random.nextInt(7)+3;//number of butterflys to be generated
		double lati,longi;
		Bitmap[] frames=new Bitmap[3];
		BF element;
		InputStream is;
		Bitmap [] pictures=new Bitmap[12];
		is=getResources().openRawResource(R.drawable.blue0);
		pictures[0] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.blue1);
		pictures[1] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.blue2);
		pictures[2] = BitmapFactory.decodeStream(is);

		is=getResources().openRawResource(R.drawable.red0);
		pictures[3] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.red1);
		pictures[4] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.red2);
		pictures[5] = BitmapFactory.decodeStream(is);

		is=getResources().openRawResource(R.drawable.red0);
		pictures[6] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.red1);
		pictures[7] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.red2);
		pictures[8] = BitmapFactory.decodeStream(is);

		is=getResources().openRawResource(R.drawable.blue0);
		pictures[9] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.blue1);
		pictures[10] = BitmapFactory.decodeStream(is);
		is=getResources().openRawResource(R.drawable.blue2);
		pictures[11] = BitmapFactory.decodeStream(is);

		for (int i=0;i<num;++i){
			int BFNo=random.nextInt(4);
			for(int j=0;j<3;++j)
				frames[j]=pictures[BFNo*3+j];
			lati=(2*random.nextDouble()-1)/3000;
			longi=(2*random.nextDouble()-1)/2000;
			if (Math.abs(lati)<0.00015) lati=0.0002;
			if (Math.abs(longi)<0.0002) longi=0.0002;
			Location loc1=new Location("GPS");
			loc1.setLatitude(sensor.getCurLocation().getLatitude()+lati);
			loc1.setLongitude(sensor.getCurLocation().getLongitude()+longi);
			loc1.setAltitude(sensor.getCurLocation().getAltitude());
			element=new BF(AR_Context, loc1,frames,BFNo) ;
			element.picture_size = 1;
			element.visible = true;
			this.addBF(element);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}
}

