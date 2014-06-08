package com.android.AR;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.android.AR.R;


//import com.android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.PorterDuff;
//some other changes here haha
public class ARView extends SurfaceView implements LocationListener,//ARSurfaceView is also a SurfaceView. It has a thread called 
//ARThread, which is used to refresh the display of the AR Elements.
SensorEventListener, SurfaceHolder.Callback{
	private Context AR_Context;
	private ARThread AR_thread;
	private SurfaceHolder AR_holder;
	public SensorManager sensorMan;
	public LocationManager locMan;
	private SurfaceHolder holder = null;


	public Location curLocation = null;
	public Location fixed_location=null;
	public float screenWidth=10;
	public float screenHeight=5;
	public PhoneData phonedata;
	//these two parameters need to be updated for different models
	private float xAngleWidth = 44f;
	private float yAngleWidth = 34f;
	volatile Vector<ARElements> areContainer = new Vector<ARElements>();
	float []gravity=new float[3];
	float []geomagnetic=new float[3];
	float []orientation=new float[3];
	float x_axis;
	float y_axis;
	float z_axis;
	private boolean gpsUsable = false;
	public volatile boolean ifThreadRun=false;
	//	private boolean locationChanged = false;
	//	private boolean BeforeFirestGetLocation = true;
	/*	public Integer[] mImageIds = { R.drawable.img1, R.drawable.img2,
			R.drawable.img3, R.drawable.img4, R.drawable.img5, R.drawable.img6,
			R.drawable.img7, R.drawable.img8, 
			R.drawable.img10, R.drawable.img11, R.drawable.img12,
			R.drawable.img13, R.drawable.img14, R.drawable.img15 };*/
	//	public android.hardware.Camera.Parameters  cameraAngleRange;
	//	public android.hardware.Camera camera;
	class PhoneData
	{
		public float device_orientation;
		public Location location;
		public float x_axis;
		public float y_axis;
		public float z_axis;
		PhoneData(float device_orientation,Location location,float x_axis,float y_axis,float z_axis)
		{
			this.device_orientation=device_orientation;
			this.location=location;
			this.x_axis=x_axis;
			this.y_axis=y_axis;
			this.z_axis=z_axis;
		}
		public void setPhoneData( float device_orientation,Location location,float x_axis,float y_axis,float z_axis)
		{
			this.device_orientation=device_orientation;
			this.location=location;
			this.x_axis=x_axis;
			this.y_axis=y_axis;
			this.z_axis=z_axis;
		}
	}
	//ARThread
	class ARThread extends Thread {
		private Paint p = new Paint();
		public String test_string = "";
		public String test_string2 = "";
		public String test_string3 = "";
		public String test_string4 = "";
		public String test_string5 = "";
		public Vector<String> messages = new Vector<String>(5);
		public ARThread(SurfaceHolder surfaceHolder, Context context)
		{
			AR_holder = surfaceHolder;
			AR_Context = context;
		}

		public void setRunning(boolean b) 
		{
			ifThreadRun = b;
		}

		/* callback invoked when the surface dimensions change. */
		// public void setSurfaceSize(int width, int height) {
		// // synchronized to make sure these all change atomically

		@SuppressLint("WrongCall")
		@Override
		public void run() {
			Canvas c = null;
			while (true) {
				c = AR_holder.lockCanvas();
				try {
					synchronized (AR_holder) {
						//c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
						p.setColor(Color.WHITE);
						for (int i = 0; i < messages.size(); i++) {
							c.drawText(messages.get(i), 150, 30 + i * 15, p);
						}
						onDraw(c,phonedata);
						ifThreadRun=false;
					}
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					// do this in a finally so that if an exception is thrown
					// during the above, we don't leave the Surface in an
					// inconsistent state
					if (c != null) {
						AR_holder.unlockCanvasAndPost(c);
					}	
				}		
			}		
		}

		public void onDraw(Canvas c, PhoneData phonedata)//calculate the parameters
		//to display the AR element (rotation, etc.) Orientation---the angle between the north and the y axis of the phone. location---the 
		// the location of the phone. x_axis---the rotation angle of the phone x axis
		{	float x_axis=phonedata.x_axis;float y_axis=phonedata.y_axis;float z_axis=phonedata.z_axis;
		float device_orientation=phonedata.device_orientation;
		Location location=phonedata.location;
		//Paint p = new Paint();
		p.setColor(Color.WHITE);
		//For debug use only. Display the sensor data.
		
		Enumeration<ARElements> e = areContainer.elements();
		if (areContainer.size() == 0)
			return;
		int x,y;
		float distance,rotation,scale;
		boolean picture_view_changed=false; boolean picture_position_changed=false;
		while (e.hasMoreElements()) {
			try {
				ARElements element = e.nextElement();
				element.visible=true;
				distance=element.distance(location);
				element.vertical_offset=(float)Math.atan((element.location.getAltitude()-location.getAltitude())/distance)*180/(float)Math.PI+90;
				scale=element.distance/distance;
				rotation=y_axis;
				if(Calculator.isPictureSizeOrRotationChanged(scale,y_axis))
				{
					picture_view_changed=true;
				}
				if(device_orientation>180)
					device_orientation=device_orientation-360;
				element.orientation_2_element=device_orientation-location.bearingTo(element.location)+90;
				x=(int) Calculator.calPositionX(element.orientation_2_element,x_axis,y_axis,z_axis,screenWidth,screenHeight,xAngleWidth,yAngleWidth,element.resized_picture.getWidth(),element.resized_picture.getHeight());
				y= (int) Calculator.calPositionY(element.orientation_2_element,x_axis,y_axis,z_axis,element.vertical_offset,screenWidth,screenHeight,xAngleWidth,yAngleWidth,element.resized_picture.getWidth(),element.resized_picture.getHeight());
				if(Calculator.isPicturePositionChanged(x-element.x, y-element.y))
				{picture_position_changed=true;}
				if (Calculator.isPictureOutOfRange(element.x,element.y))
					element.visible=false;
				if (!element.visible)
					continue;
				if (element.picture_size != -1&& element.visible==true) 
				{
					c.drawText(element.name, 0, element.name.length(),element.x, element.y, p);
				}	
				if (element.resized_picture != null)
				{
					if(picture_position_changed==true||picture_view_changed==true)
					{
						element.resized_picture = Calculator.transformImage(element.picture,element.width(), element.height(),rotation);
						c.drawBitmap(element.resized_picture, x,y, p);
					}
					element.picture_changed=false;element.rotation=rotation;element.x=x;element.y=y;element.distance=distance;
				}
			} catch (Exception x1) {
				Log.e("ArLayout", x1.getMessage());
			}
		}
		//c.drawText(String.valueOf(ifThreadRun), 10, 50, p);
	/*	c.drawText("Latitude--" + String.valueOf(curLocation.getLatitude()), 10, 100, p);
		c.drawText("Longitude--" + String.valueOf(curLocation.getLongitude()), 10, 150, p);
		c.drawText("Altitue--" + String.valueOf(curLocation.getAltitude()), 10, 200, p);
		c.drawText("Gravity0--" + String.valueOf(gravity[0]), 10, 250, p);
		c.drawText("Gravity1--" + String.valueOf(gravity[1]), 10, 300, p);
		c.drawText("Gravity2--" + String.valueOf(gravity[2]), 10, 350, p);
		c.drawText("Geomagnetic0--" + String.valueOf(geomagnetic[0]), 10, 400, p);
		c.drawText("Geomagnetic1--" + String.valueOf(geomagnetic[1]), 10, 450, p);
		c.drawText("Geomagnetic2--" + String.valueOf(geomagnetic[2]), 10, 500, p);
		c.drawText("Orientation0--" + String.valueOf(orientation[0]), 10, 550, p);
		c.drawText("Orientation1--" + String.valueOf(orientation[1]), 10,600, p);
		c.drawText("Orientation2--" + String.valueOf(orientation[2]), 10, 650, p);
		c.drawText("Distance--" + String.valueOf(curLocation.distanceTo(fixed_location)), 10,700, p);*/
		}
		public void addMessage(String msg)
		{
			if (messages.size() > 4) {
				messages.remove(4);
			}
			messages.add(0, msg);
		}
	}


	public ARView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		AR_Context = context;
		holder = getHolder();
		AR_holder=holder;
		holder.addCallback(this);
		setAR_thread(new ARThread(holder, AR_Context));
		sensorMan = (SensorManager) AR_Context//initialize the sensors
				.getSystemService(Context.SENSOR_SERVICE);
		sensorMan.registerListener(this,sensorMan.getDefaultSensor(Sensor.TYPE_ALL),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorMan.registerListener(this,sensorMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD ),
				SensorManager.SENSOR_DELAY_FASTEST);
		sensorMan.registerListener(this,sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		locMan = (LocationManager) AR_Context.getSystemService(Context.LOCATION_SERVICE);
		// set the miniTime and miniDistance here
		locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10,this);
		curLocation=locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(curLocation==null)
		{
			Location temp_location = new Location("GPS");
			temp_location.setLatitude(54.527474);
			temp_location.setLongitude(-114.530674);
			temp_location.setAltitude(671);
			curLocation = temp_location;
		}
		fixed_location=new Location(curLocation);
		phonedata=new PhoneData(1,curLocation,0,0,0);
		Bitmap picture;
		ARElements element;
		InputStream is1 =getResources().openRawResource(R.drawable.images);
		picture = BitmapFactory.decodeStream(is1);
		Location loc1=new Location("GPS");
		loc1.setLatitude(curLocation.getLatitude());
		loc1.setLongitude(curLocation.getLongitude()+0.01);
		loc1.setAltitude(curLocation.getAltitude());
		element=new ARElements(AR_Context, loc1,picture) ;
		element.picture_size = 1;
		element.visible = true;
		element.name = "first";
		this.addARView(element);
		InputStream is2 =getResources().openRawResource(R.drawable.img3);
		picture = BitmapFactory.decodeStream(is2);
		loc1.setLatitude(curLocation.getLatitude()-0.01);
		element=new ARElements(AR_Context,loc1,picture) ;
		element.picture_size = 1;
		element.visible = true;
		element.name = "second";
		this.addARView(element);
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {}
	public void surfaceCreated(SurfaceHolder holder) {
		// start the thread here so that we don't busy-wait in run()
		// waiting for the surface to be created
		getAR_thread().setRunning(true);
		getAR_thread().start();
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be touched. WARNING: after this method returns, the Surface/Canvas must
	 * never be touched again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
	}

	public void onLocationChanged(Location location) {//¸üÐÂ
		curLocation.setLatitude(location.getLatitude());
		curLocation.setLongitude(location.getLongitude());
		curLocation.setAltitude(location.getAltitude());
		//curLocation = location;
		phonedata.setPhoneData(orientation[0],curLocation,x_axis,y_axis,z_axis);
		ifThreadRun=true;
	}
	public void onSensorChanged(SensorEvent evt) {
		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			for(int i=0;i<3;i++)
				gravity[i]=evt.values[i];
		}
		if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			for(int i=0;i<3;i++)
				geomagnetic[i]=evt.values[i];
		}
		float[] R=new float[9];float[] I=new float[9];
		SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
		SensorManager.getOrientation(R, orientation);
		x_axis=Calculator.calRotationX(gravity)	;
		y_axis=Calculator.calRatationY(gravity)	;
		z_axis=Calculator.calRotationZ(gravity)	;
		phonedata.setPhoneData(orientation[0],curLocation,x_axis,y_axis,z_axis);
		ifThreadRun=true;
	}
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		if (provider.compareTo(LocationManager.GPS_PROVIDER) == 0)
		{
			gpsUsable = (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) ? false
					: true;
		}
	}
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
	public void addARView(ARElements element) {areContainer.add(element);}
	public void removeARView(ARElements element) {areContainer.remove(element);}
	public ARThread GetThread() {return (getAR_thread());}
	public void close() {this.getAR_thread().setRunning(false);}
	protected void onDestroy() {}
	public void setAR_thread(ARThread aR_thread) {AR_thread = aR_thread;}
	public ARThread getAR_thread() {return AR_thread;}
}
