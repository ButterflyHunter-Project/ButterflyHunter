package com.android.AR;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class Calculator {
	public static double upperThreshold=1.1;//
	public static double lowerThreshold=0.9;
	public static double xMoveThreshold=0;
	public static double yMoveThreshold=0;

	public static boolean pointInRect(float pointX, float pointY, int x, int y, float width, float height) {
		if(pointX < x || pointY < y || pointX > x + width || pointY > y + height) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean rectOverlap(float rect1X1, float rect1Y1, float rect1X2, float rect1Y2, float rect2X1, float rect2Y1, float rect2X2, float rect2Y2) {
		return rect1X1 < rect2X2 && rect1X2 > rect2X1 && rect1Y1 < rect2Y2 && rect1Y2 > rect2Y1;
	}
	
	
	public static float calRotationX(float gravity[])//This function calculates the rotation degree of the phone (x axis) with respect to the initial 
	//rotation (put the phone scree horizontal)
	{
		float x_axis=0;
		double d=Math.abs(gravity[0])/Math.sqrt(gravity[1]*gravity[1]+gravity[2]*gravity[2]);
		x_axis=(float)(Math.atan(d));
		x_axis=(float)x_axis*180/(float)Math.PI;
		if(gravity[0]>0&&gravity[2]>0)//手机镜头朝下前方
			x_axis=x_axis;
		else if(gravity[0]>0&&gravity[2]<=0)//手机镜头上前方
			x_axis=180-x_axis;
		else if(gravity[0]<=0&&gravity[2]>0)//手机镜头后下方
			x_axis=360-x_axis;
		else
			x_axis=180+x_axis;
		return x_axis;
	}

	public static float calRatationY(float gravity[])//计算手机y轴与水平方向的夹角
	{
		float y_axis=0;
		double d=Math.abs(gravity[1])/Math.sqrt((gravity[0]*gravity[0]+gravity[2]*gravity[2]));
		y_axis=(float)(Math.atan(d));
		y_axis=y_axis*180/(float)Math.PI;
		//	if(gravity[1]>0&&gravity[2]>0)//手机镜头朝左上方
		//	y_axis=360-y_axis;
		//	else if(gravity[1]>0&&gravity[2]<=0)//手机镜头右下方
		//		y_axis=180+y_axis;
		if(gravity[1]<=0&&gravity[2]>0)//
			y_axis=y_axis;
		if(gravity[1]>0)
			y_axis=-y_axis;
		//	else
		//		y_axis=180-y_axis;
		return y_axis;
	}

	public static float calRotationZ(float gravity[])//计算手机z轴与水平方向的夹角
	{
		float z_axis=0;
		double d=Math.abs(gravity[2])/Math.sqrt((gravity[0]*gravity[0]+gravity[1]*gravity[1]));
		z_axis=(float)(Math.atan(d));
		z_axis=z_axis*180/(float)Math.PI;
		if(gravity[2]>0)//
			z_axis=90-z_axis;
		else 
			z_axis=90+z_axis;
		return z_axis;
	}

	public static float calPositionX(float orientation,float x_axis,float y_axis,float z_axis,float screen_width,float screen_height,float focal_width,float focal_height,float picture_width,float picture_height)
	{//this function calculates the position of the ARE on the screen (x axis). x is the left low corner of the picture
		float x;
		x=(orientation*2/focal_width)*screen_width/2+screen_width/2;
		if(x>screen_width)
			return 10001;
		else if (x<-picture_width)
			return -10001;
		else return x;
	}

	public static float calPositionY(float orientation,float x_axis,float y_axis,float z_axis,float vertical_offset,float screen_width,float screen_height,float focal_width,float focal_height,float picture_width,float picture_height)
	{
		float y;
		y=((z_axis-vertical_offset)*2/focal_height)*screen_height/2+screen_height/2;
		if(y>screen_height)
			return 10001;
		else if (y<-picture_height)
			return -10001;
		else 
			return y;
	}
	public static boolean isPicturePositionChanged(double xDelta,double yDelta)
	{//tells if the position of the ARE on the screen changes
		if(Math.abs(xDelta)>xMoveThreshold||Math.abs(yDelta)>yMoveThreshold)
			return true;
		else 
			return false;
	}
	public static boolean isPictureSizeOrRotationChanged(double scale,float degrees)
	{//tells if the size or the rotation of the ARE changes
		if(scale>upperThreshold||scale<lowerThreshold||Math.abs(degrees)>6)
			return true;
		else
			return false;
	}
	public static boolean isPictureOutOfRange(float x,float y)
	{
		if(Math.abs(x)>10000||Math.abs(y)>10000)
			return true;
		else return false;
	}


	public static Bitmap transformImage(Bitmap mBitmap, double new_width,
			double new_height,float degrees) {
		int mBitmap_height = mBitmap.getHeight();
		int mBitmap_width = mBitmap.getWidth();
		float scaleWidth = 0;
		float scaleHeight = 0;
		Matrix matrix = new Matrix();
		scaleWidth = (float) ((float) new_width/mBitmap_width);
		scaleHeight = (float) ((float) new_height/mBitmap_height);
		matrix.postScale(scaleWidth, scaleHeight);
		matrix.postRotate(degrees);
		Bitmap changed_picture = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap_width,
				mBitmap_height, matrix, true);
		return (changed_picture);
	}

	public static Bitmap Zoomimage(Bitmap mBitmap, double new_width_scale,
			double new_height_scale) {
		int mBitmap_height = mBitmap.getHeight();
		int mBitmap_width = mBitmap.getWidth();
		float scaleWidth = 0;
		float scaleHeight = 0;
		Matrix matrix = new Matrix();
		scaleWidth = (float) ((float) new_width_scale);
		scaleHeight = (float) ((float) new_height_scale);
		matrix.postScale(scaleWidth, scaleHeight);

		Bitmap changed_picture = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap_width,
				mBitmap_height, matrix, true);
		return (changed_picture);
	}

}
