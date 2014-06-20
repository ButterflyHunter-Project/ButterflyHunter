package com.android.AR;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class SensorData implements LocationListener,SensorEventListener{
	public SensorManager sensorMan;
	public LocationManager locMan;
	public Location curLocation;
	public PhoneData phonedata;
	public ARView arview;
	int filterOrder=2;
	float [][]hist_gravity=new float[filterOrder][3];
	float [][]hist_geomagnetic=new float[filterOrder][3];
	float [][]hist_orientation=new float[filterOrder][3];
	float []gravity=new float[3];
	float []geomagnetic=new float[3];
	float []orientation=new float[3];
	float []new_orientation=new float[3];
	float [] output=new float[3];
	float [] coefficient=new float[5];
	float x_axis,y_axis,z_axis;

	static final float ALPHA = 0.1f;
	float[] Rot=new float[9];float[] I=new float[9];
	protected float[] lowPass( float[] input, float[] output ) {
		if ( output==null ) return input;     
		for ( int i=0; i<input.length; i++ ) {
			output[i] = ALPHA*input[i]+(1-ALPHA)*output[i];
		}
		return output;
	}
	
	protected float[] highOrderLowPass( float[][] history_data ) {   
		float[] out=new float[3];
		for ( int i=0; i<3; i++ ) {
			for(int j=0;j<filterOrder;++j){out[i]=out[i]+coefficient[j]*history_data[j][i];}
		}
		return out;
	}
	private void updateHistoryData(float[] newData,float[][]histData){
	
		for(int i=filterOrder-1;i>0;--i){
			histData[i]=histData[i-1];
		}
		histData[0]=newData;
	}
	public void onSensorChanged(SensorEvent evt) {
		//		
		if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			gravity = lowPass(evt.values, gravity);
			
			//updateHistoryData(evt.values,hist_gravity);
			//gravity=highOrderLowPass(hist_gravity);
			
		} 
		
		if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			geomagnetic = lowPass(evt.values, geomagnetic);
			//updateHistoryData(evt.values,hist_geomagnetic);
			//geomagnetic=highOrderLowPass(hist_geomagnetic);

		}
		SensorManager.getRotationMatrix(Rot, I, gravity, geomagnetic);
		SensorManager.getOrientation(Rot, new_orientation);
		orientation=lowPass(new_orientation,orientation);
		x_axis=Calculator.calRotationX(gravity)	;
		y_axis=Calculator.calRatationY(gravity)	;
		z_axis=Calculator.calRotationZ(gravity)	;
		phonedata.setPhoneData((float)(orientation[0]*180/Math.PI),curLocation,x_axis,y_axis,z_axis);
		arview.ifThreadRun=true;
	}
	
	
	public void onLocationChanged(Location location) {
		curLocation.setLatitude(location.getLatitude());
		curLocation.setLongitude(location.getLongitude());
		curLocation.setAltitude(location.getAltitude());
		phonedata.setPhoneData((float)(orientation[0]*180/Math.PI),curLocation,x_axis,y_axis,z_axis);
		arview.ifThreadRun=true;
		BF.deviceLocation=curLocation;
		arview.updateBFLists();
		if (arview.numOfBFWithinRange(arview.visible_range)<2)
			arview.generateBF();
	}

	SensorData(Context AR_Context, ARView ar){
		this.arview=ar;
		coefficient[0]=0.1f;coefficient[1]=0.9f;coefficient[2]=0.1f;coefficient[3]=0.1f;coefficient[4]=0.2f;
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
		locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 2,this);
		curLocation=locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(curLocation==null)
		{
			Location temp_location = new Location("GPS");
			temp_location.setLatitude(54.527474);
			temp_location.setLongitude(-114.530674);
			temp_location.setAltitude(671);
			curLocation = temp_location;
		}
		phonedata=new PhoneData(1,curLocation,0,0,0);
			}

	
	
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		if (provider.compareTo(LocationManager.GPS_PROVIDER) == 0)
		{		}
	}
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onAccuracyChanged(Sensor arg0, int arg1) {}
	
	
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
}
