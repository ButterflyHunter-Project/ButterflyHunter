package com.ar;
import android.R.color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Location;
import android.view.View;
/*
 Class BF defines a butterfly object to be displayed on the ARView.
 */
public class BF extends View {
	public BFType type;//The type of the butterfly. e.g a blue butterfly
	public volatile Location location;//current location of the butterfly
	public volatile static Location deviceLocation;//location of the phone
	public volatile boolean visible = false;
	public String name = null;
	public Bitmap picture[] = new Bitmap[3];//3 pictures to play an animation
	public Bitmap[] resized_picture = new Bitmap[3];
	public int size=20;//set the initial size of the butterfly meaning when distance=size, the butterfly will be displayed in default size
	public volatile int x=0;//the x axis of the picture on the screen
	public volatile int y=0;	
	public float ini_rotation=0;//the initial rotation of the butterfly
	public float rotation=0;//the current rotation of the butterfly
	public float ini_distance=1000;//the initial distance of the butterfly to the phone
	public float distance=1000;//the current distance of the butterfly
	public volatile float vertical_offset=0;//the vertial offset of the butterfly on the phone screen
	public volatile float orientation_2_element=0;//the orientation of the phone to the butterfly
	public boolean picture_view_changed = false;
	public boolean picture_position_changed = false;//
	public double picture_size = -1;//
	public boolean picture_changed = true;
	public boolean isChecked=true;//if the user touches the BF, then an info tab will pop up and remains until the user touches again
	public boolean isLongClicked=false;//if the user long click the BF, remove it
	public int value=0;
	public int number=0;
	private static int timer=0;
	
	public static void incTimer()//the timer is used to animate the butterfly
	{timer++;
	 timer=timer>2?0:timer;
	}
	public enum BFType{Blue,Green,Red,Purple,Group}
	public BF(Context ctx) {
		super(ctx);
	}
	public BF(Context ctx,int x,int y,Bitmap[] picture, int picture_ID) {
		super(ctx);
		this.x=x;
		this.y=y;
		this.picture=picture;
		for (int i=0;i<3;++i)
		this.resized_picture[i]=this.picture[i];
		updateDistance();
		type=getType(picture_ID);
		this.value=picture_ID+2;
		this.number=1;
	}

	public BF(Context ctx,double location_latitude,double location_longitude,
			double altitude,Bitmap[] picture,int picture_ID) {
		super(ctx);
		this.picture=picture;
		for (int i=0;i<3;++i)
		this.resized_picture[i]=this.picture[i];
		this.ini_rotation=0;
		Location temp_location = new Location("GPS");
		temp_location.setLatitude(location_latitude);
		temp_location.setLongitude(location_longitude);
		temp_location.setAltitude(altitude);
		this.location = temp_location;
		updateDistance();
		type=getType(picture_ID);
		this.value=picture_ID+2;
		this.number=1;
	}
	public BF(Context ctx,Location location,Bitmap[] picture,int picture_ID) {
		super(ctx);
		this.picture=picture;
		for (int i=0;i<3;++i)
		this.resized_picture[i]=this.picture[i];
		this.ini_rotation=0;
		Location temp_location = new Location("GPS");
		temp_location.setLatitude(location.getLatitude());
		temp_location.setLongitude(location.getLongitude());
		temp_location.setAltitude(location.getAltitude());
		this.location = temp_location;
		updateDistance();
		type=getType(picture_ID);
		this.value=picture_ID+2;
		this.number=1;
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

	public int getMaxWidth()
	{
		int width=0;
		for(int i=0;i<3;i++)
		width=Math.max(width, picture[i].getWidth()*size/((int)(this.distance)+1));//+1 in case distance=0
		return width;
	}
	
	public int width(int frameNo)
	{
		int width;
		width=picture[frameNo].getWidth()*size/((int)(this.distance)+1);//+1 in case distance=0
		return width;
	}
	public int width()
	{
		int width;
		width=picture[0].getWidth()*size/((int)(this.distance)+1);//+1 in case distance=0
		return width;
	}
	public int height()
	{
		int height;
		height=picture[0].getHeight()*size/((int)this.distance+1);
		return height;
	}
	public int height(int frameNo)
	{
		int height;
		height=picture[frameNo].getHeight()*size/((int)this.distance+1);
		return height;
	}

	public float rotation(float cur_rotation)
	{
		return (cur_rotation+ini_rotation);
	}
	
	public BFType getType(int ID){
		BFType t=BFType.Blue;
		switch(ID){
		case 0: 
			t=BFType.Blue;
			break;
		case 1: 
			t=BFType.Red;
			break;
		case 2: 
			t=BFType.Red;
			break;
		case 3: 
			t=BFType.Group;
			break;
		default:break;
		}
		return t;
	}
	
	public static int nextFrameNo(){//get the next frame number of the animation
		return(timer);
	}
	
}
