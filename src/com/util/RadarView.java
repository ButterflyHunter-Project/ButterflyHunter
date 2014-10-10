package com.util;

import java.util.Iterator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import com.ar.ARView;
import com.ar.BF;

public class RadarView {//This class can draw a radar on the top left that shows the butterflies 

	private SensorData sensor;
	private ARView ar;
	private  int radius=100;//radius of the radar
	private final int margin=60;//the margin between the screen edge and the radar edge
	private int cx,cy;
	public RadarView(SensorData sensor,ARView ar){
		this.sensor=sensor;
		this.ar=ar;
		if(ar!=null){this.radius=(int) (ar.getScreenHeight()/7);}
		cx=radius/2+margin;
		cy=cx+30;
	};
	
	public void drawRadar(Canvas c){
		
		//Draw a circle
		Paint p=new Paint();
		p.setColor(Color.WHITE);
		p.setTextSize(30);
		c.drawText("N", cx-10, cy-radius-10, p);
		p.setColor(Color.RED);
		p.setAlpha(90);
		c.drawCircle(cx, cy, radius, p);
		
		
		drawCameraAngle(c, p);
		drawBF(c,p);
		
	}
	
	public void drawBF(Canvas c, Paint p){//draw butterflies on the radar as dots
		p.setColor(Color.WHITE);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(6);
		if(ar!=null&&ar.getFlyingList()!=null){
			Iterator<BF> iter=ar.getFlyingList().iterator();
			while(iter.hasNext()){
				BF bf=iter.next();
				drawPoint(bf.orientation_2_element+sensor.getPhoneData().device_orientation, bf.distance, c, p);
			}
		}
	}
	public void drawCameraAngle( Canvas c, Paint p){//draw a sector which represents the horizon of the phone camera
		float cAngle=sensor.getPhoneData().device_orientation;
		float lAngle=cAngle-ar.getXAngle()/2;
		float rAngle=cAngle+ar.getXAngle()/2;
		p.setStyle(Style.FILL);
		p.setColor(Color.GREEN);
		p.setAlpha(160);
		RectF oval=new RectF(cx-radius,cy-radius,cx+radius,cy+radius);
		c.drawArc(oval, lAngle, rAngle-lAngle,true, p);
	}
	
	public void drawPoint(float angle, float distance, Canvas c, Paint p){//Draw a point on the radar
		//angle: angle from east, counter-clockwise,positive, in degrees
		distance*=1.5;
		float dx=(float) (distance*Math.cos(angle*Math.PI/180));
		float dy=(float) (distance*Math.sin(angle*Math.PI/180));
		c.drawPoint(cx+dx, cy+dy, p);
		
	}
	
	public void drawLine(float angle, float distance, Canvas c, Paint p){//draw a line on the radar
		//angle: angle from east, counter-clockwise,positive, in degrees
		
		float dx=(float) (distance*Math.cos(angle*Math.PI/180));
		float dy=(float) (distance*Math.sin(angle*Math.PI/180));
		c.drawLine(cx, cy, cx+dx, cy+dy, p);
		
	}
}
