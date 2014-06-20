package com.android.AR;
//
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.view.View;

public class BF extends View {//defines the AR elements (ARE) on the camera view
	public volatile Location location;//current location of the ARE
	public static Location deviceLocation;
	public volatile boolean visible = false;
	public String name = null;
	public Bitmap picture = null;
	public Bitmap resized_picture = null;
	public float size=10;//set the initial size of the ARE meaning when distance=size, the ARE will be displayed in default size
	public volatile int x=0;//the x axis of the picture on the screen
	public volatile int y=0;	
	public float ini_rotation=0;//the initial rotation of the ARE
    public float rotation=0;//the current rotation of the ARE
    public float ini_distance=1000;//the initial rotation of the ARE
    public float distance=1000;//the current rotation of the ARE
	public volatile float vertical_offset=0;
	public volatile float orientation_2_element=0;
	public boolean picture_view_changed = false;
	public boolean picture_position_changed = false;//
	public double picture_size = -1;//
	public boolean picture_changed = true;
	public BF(Context ctx) {
		super(ctx);
	}

	public BF(Context ctx,int x,int y,Bitmap picture) {
		super(ctx);
		this.x=x;
		this.y=y;
		this.picture=picture;
		updateDistance();
		
	}

	public BF(Context ctx,double location_latitude,double location_longitude,
			double altitude,Bitmap picture) {
		super(ctx);
		//this.location_latitude=location_latitude;
		//this.location_longitude=location_longitude;
		//this.altitude=altitude;
		this.picture=picture;
		this.resized_picture=this.picture;
		this.ini_rotation=0;
		Location temp_location = new Location("GPS");
		temp_location.setLatitude(location_latitude);
		temp_location.setLongitude(location_longitude);
		temp_location.setAltitude(altitude);
		this.location = temp_location;
		updateDistance();
		//actual_width = picture.getWidth();
		//actual_height = picture.getHeight();
		//window_width=actual_width;
		//window_height=actual_height;
	}
	public BF(Context ctx,Location location,Bitmap picture) {
		super(ctx);
		//this.location_latitude=location_latitude;
		//this.location_longitude=location_longitude;
		//this.altitude=altitude;
		this.picture=picture;
		this.resized_picture=this.picture;
		this.ini_rotation=0;
		Location temp_location = new Location("GPS");
		temp_location.setLatitude(location.getLatitude());
		temp_location.setLongitude(location.getLongitude());
		temp_location.setAltitude(location.getAltitude());
		this.location = temp_location;
		updateDistance();
		//actual_width = picture.getWidth();
		//actual_height = picture.getHeight();
		//window_width=actual_width;
		//window_height=actual_height;
	}
	public float getDistance()
	{
		float dis=0;
		dis=location.distanceTo(deviceLocation);
		return dis;
	}
	public void updateDistance()
	{
		float dis=getDistance();
		this.distance=dis;
	}
	
public float width()
{
	float width;
	width=picture.getWidth()*size/(this.distance+1);//+1 in case distance=0
	return width;
	}
 
public float height()
{
	float height;
	height=picture.getHeight()*size/(this.distance+1);
	return height;
	}

public float rotation(float cur_rotation)
{
	return (cur_rotation+ini_rotation);
	}
}
