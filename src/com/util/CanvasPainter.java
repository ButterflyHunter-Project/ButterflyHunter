package com.util;

import java.util.Iterator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Location;
import android.util.Log;

import com.ar.ARView;
import com.ar.BF;

public class CanvasPainter {//this class includes several methods that controls the Canvas(show information, animation, etc...)

	ARView ar;
	public CanvasPainter(ARView ar){
		this.ar=ar;
	}

	public void onDraw(Canvas c) //calculate the parameters
	//to display the AR element (rotation, etc.) Orientation  -the angle between the north and the y axis of the phone. location  -the 
	// the location of the phone. x_axis  -the rotation angle of the phone x axis
	{					
		drawBFs(c);
		animateCatchedBF(c);

	}

	public void animateCatchedBF(Canvas c){
		//Draw animation if one butterfly is catched
		int delta_x=100;
		if(ar.getCatchedList()!=null&&ar.getThread().getAnimationStart()<ar.getCatchedList().size()){
			boolean flag=true;
			for(int i=ar.getThread().getAnimationStart();i<ar.getCatchedList().size();i++){
				BF element =ar.getCatchedList().get(i);
				if(element.ini_rotation==0||Math.pow(element.x+element.width()/2, 2)+Math.pow(ar.getScreenHeight()-element.y-element.height(),2)<50||element.x<-50||element.y>ar.getScreenHeight()+50)//stop animation
				{
					element.ini_rotation=0;
					continue;
				}
				if(flag)ar.getThread().setAnimationStart(i);flag=false;
				c.drawBitmap(element.resized_picture[0], element.x,element.y, null);
				if((int)element.ini_distance==Integer.MAX_VALUE)
				{
					element.y-=delta_x;
				}
				else
				{
					element.x-=delta_x;
					element.y+=element.ini_distance*delta_x;      
				}

			}
		}
	}

	public void drawBFs(Canvas c){
		float x_axis=ar.getSensor().getPhoneData().x_axis;float y_axis=ar.getSensor().getPhoneData().y_axis;float z_axis=ar.getSensor().getPhoneData().z_axis;
		float device_orientation=ar.getSensor().getPhoneData().device_orientation;
		Location location=ar.getSensor().getPhoneData().location;
		Iterator<BF> e = ar.getFlyingList().iterator();
		if (ar.getFlyingList().size() == 0)
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
					picture_view_changed=true;				
				float tem_orientation=location.bearingTo(element.location)-device_orientation-90;
				if (tem_orientation>180) tem_orientation=tem_orientation-360;
				if (tem_orientation<-180) tem_orientation=tem_orientation+360;
				element.orientation_2_element=tem_orientation;
				x=(int) Calculator.calPositionX(element.orientation_2_element,x_axis,y_axis,z_axis,ar.getScreenWidth(),ar.getScreenHeight(),ar.getXAngle(),ar.getYAngle(),element.resized_picture[BF.nextFrameNo()].getWidth(),element.resized_picture[BF.nextFrameNo()].getHeight());
				y= (int) Calculator.calPositionY(element.orientation_2_element,x_axis,y_axis,z_axis,element.vertical_offset,ar.getScreenWidth(),ar.getScreenHeight(),ar.getXAngle(),ar.getYAngle(),element.resized_picture[BF.nextFrameNo()].getWidth(),element.resized_picture[BF.nextFrameNo()].getHeight());
				if(Calculator.isPicturePositionChanged(x-element.x, y-element.y))
				{picture_position_changed=true;}
				if (Calculator.isPictureOutOfRange(x,y))
					element.visible=false;
				if (!element.visible)
					continue;
				if (element.resized_picture != null)
				{
					if(picture_position_changed==true||picture_view_changed==true)
					{		
						element.resized_picture[BF.nextFrameNo()] = Calculator.transformImage(element.picture[BF.nextFrameNo()],element.width(BF.nextFrameNo()), element.height(BF.nextFrameNo()),rotation);
						c.drawBitmap(element.resized_picture[BF.nextFrameNo()], x,y, null);
						showInfoTab(x, y, c,element);
					}
					else
					{	
						c.drawBitmap(element.resized_picture[BF.nextFrameNo()], element.x,element.y, null);
						showInfoTab(element.x, element.y, c,  element);
					}
					element.picture_changed=false;element.rotation=rotation;element.x=x;element.y=y;element.distance=distance;
				}
			} catch (Exception x1) {
				Log.e("ArLayout", x1.getMessage());
			}
		}
	}
	public void showToast(Canvas c){//shows the infoTab (distance, type, size, etc...) of the butterfly
		Paint p=new Paint();
		int delta=5;int text_height=40;int text_size=30;
		int rect_width=480;int rect_height=text_height+delta;
		int left=(int)ar.getScreenWidth()/2-rect_width/2;
		int top=(int)ar.getScreenHeight()-rect_height-delta;
		int right=left+rect_width;
		int bottom=top+rect_height;
		RectF rect=new RectF(left,top,right,bottom);
		p.setColor(Color.BLACK);
		p.setAlpha(100);
		c.drawRoundRect(rect,6,7,p);
		p.setColor(Color.WHITE);
		p.setTextSize(text_size);
		c.drawText("Congrats! You catched a butterfly. ",left+delta,top+text_size+delta,p);
	}
	public void showNumber(Canvas c){//show number of catched bfs on left bottom corner
		int rect_height=60;
		int rect_width=90;
		int delta=5;
		int left=delta;
		int top=(int) (ar.getScreenHeight()-rect_height-delta);
		int right=left+rect_width;
		int  bottom=top+rect_height;
		int text_size=42;
		RectF numberBF=new RectF(left,top,right,bottom);
		Paint p=new Paint();
		p.setColor(Color.BLACK);
		p.setAlpha(160);
		c.drawRoundRect(numberBF, 10, 10,p);
		p.setColor(Color.WHITE);
		p.setTextSize(text_size);
		c.drawText(String.valueOf(ar.getNumberOfBF()),left+delta,top+text_size+delta,p);
	}


	public void showInfoTab(int x, int y, Canvas c, BF element){//shows the infoTab (distance, type, size, etc...) of the butterfly

		if(element.isChecked){
			Paint p=new Paint();
			int delta=5;int text_height=40;int text_size=30;
			int rect_width=220;int rect_height=text_height+delta;
			int left=x+element.getMaxWidth()+delta;
			int top=y-rect_height;
			int right=left+rect_width;
			int bottom=top+rect_height;
			RectF rect=new RectF(left,top,right,bottom);
			p.setColor(Color.BLACK);
			p.setAlpha(160);
			//p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR)); 
			c.drawRoundRect(rect,10,10,p);
			p.setColor(Color.WHITE);
			p.setTextSize(text_size);
			c.drawText("Distance: "+String.valueOf(Math.abs((int)element.distance-15))+"m",left+delta,top+text_size+delta,p);
			//c.drawText("Type:         "+String.valueOf(element.type),left+delta,top+delta+text_size+text_height,p);
			//c.drawText("Value:        "+String.valueOf(element.value),left+delta,top+delta+text_size+2*text_height,p);
		}


	}
}
