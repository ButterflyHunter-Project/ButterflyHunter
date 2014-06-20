package com.android.AR;
//I just add some test text here
import android.app.ActionBar;

import com.android.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.graphics.Point;
public class CameraActivity extends Activity {//AR is an activity. AR includes two Surfaceview objects:CameraView and ARView. Basically an activity means a screen
	public CameraView cv;
	private FrameLayout rl;
	public static volatile Context ctx;
	public ARView ar;//
	private TextView tv;
	private View menu_layer;
	volatile Location curLocation = null;
	private int screenHeight;
	private int screenWidth;
	public static String information = "Welcome to the world of Butterflys!";
	/** Called when the activity is first created. */
    WakeLock mWakeLock;
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ctx = this.getApplicationContext();
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG); wl.acquire(); // ... wl.release(); 
		//this.mWakeLock = pm.newWakeLock(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON , "");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		cv = new CameraView(ctx);//The camera is set up now!

		ar = new ARView(ctx);
		tv = new TextView(this);
		tv.setText(information);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		WindowManager w = getWindowManager();

		//this code block gets the screen width and height
		Display d = w.getDefaultDisplay();
		Point tem_point=new Point();
		d.getSize(tem_point);
		screenWidth = tem_point.x;
		screenHeight = tem_point.y;
		ar.screenWidth = screenWidth;
		ar.screenHeight = screenHeight;

		ar.setBackgroundColor(Color.TRANSPARENT);
		ar.getHolder().setFormat(PixelFormat.RGBA_8888);
		cv.screenWidth = screenWidth;
		cv.screenHeight = screenHeight;
		rl = new FrameLayout(getApplicationContext());
		rl.addView(ar, screenWidth, screenHeight);//把各层view加到frameLayout上
		rl.addView(cv, screenWidth, screenHeight);
		rl.addView(tv);
		//menu_layer = LayoutInflater.from(this).inflate(R.layout.menu_layer, null);
		//rl.addView(menu_layer);
		
		setContentView(rl);
		ar.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View view, MotionEvent event) 
			{
				float npx = event.getX();  //get the coordinate of the touch area
				float npy = event.getY();
				if(event.getAction()==MotionEvent.ACTION_DOWN)
					for(int i=ar.flyingList.size()-1;i>=0;i--)
					{
						if(Calculator.pointInRect(npx, npy,ar.flyingList.get(i).x,ar.flyingList.get(i).y, 
								ar.flyingList.get(i).width(),  ar.flyingList.get(i).height()))
						{
							
							ar.flyingList.get(i).isChecked=!ar.flyingList.get(i).isChecked;
							//break;
						}
					}
				return false;
			}

		});	
	}
	protected void onPause(){
		super.onPause();
		//finish();
	}
	protected void onResume(){
		super.onResume();
	}
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//	    // Inflate the menu items for use in the action bar
//	    MenuInflater inflater = getMenuInflater();
//	    inflater.inflate(R.layout.menu_layer, menu);
//	    return super.onCreateOptionsMenu(menu);
//	}
}