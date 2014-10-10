package com.ar;
import android.app.ActionBar;

import com.butterflyHunter.R;
import com.util.Calculator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Point;

/*
 * CameraActivity displays the camera view of the phone.
 * */
public class CameraActivity extends Activity {
	public CameraView cv;
	private FrameLayout rl;
	public static volatile Context ctx;
	public ARView ar=null;//
	private TextView tv;
	private View menu_layer;
	private int screenHeight;
	private int screenWidth;
	private Point touch_point=new Point();
	WakeLock mWakeLock;
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ctx = this.getApplicationContext();
 
		
		//final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		//PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, TAG); wl.acquire(); // ... wl.release(); 
		//this.mWakeLock = pm.newWakeLock(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON , "");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//this code block gets the screen width and height
		WindowManager w = getWindowManager();
		Display d = w.getDefaultDisplay();
		Point tem_point=new Point();
		d.getSize(tem_point);
		screenWidth = tem_point.x;
		screenHeight = tem_point.y;
		
		cv = new CameraView(ctx);//The camera is set up now!
		rl = new FrameLayout(getApplicationContext());
		
		ar = new ARView(ctx,cv,screenWidth,screenHeight);
		ar.setBackgroundColor(Color.TRANSPARENT);
		ar.getHolder().setFormat(PixelFormat.RGBA_8888);
	
		rl.addView(ar, screenWidth, screenHeight);
		rl.addView(cv, screenWidth, screenHeight);
        rl.addView(LayoutInflater.from(this).inflate(R.layout.camera_menu, null));
		setContentView(rl);
		
		
		Button resetButton= (Button) findViewById(R.id.reset_button);
		resetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				ar.reset();
				toast("Re-generating butterflies");
			}
		});
		
		resetButton.setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				ar.setNumberOfBF(0);
				ar.reset();
				toast("Re-generating butterflies");
				return false;
			}
		});
//		
		 

		
		ar.setOnTouchListener(new OnTouchListener()
		{
			public boolean onTouch(View view, MotionEvent event) 
			{
				touch_point.x=(int)event.getX();
				touch_point.y=(int)event.getY();
				return false;
			}

		});	

		ar.setOnClickListener(new OnClickListener()//once clicked, the isChecked is set true for the butterfly. In the ARView, the information tab for this butterfly will be displayed
		{
			public void onClick(View v) {
				for(int i=ar.getFlyingList().size()-1;i>=0;i--)
				{
					if(Calculator.pointInRect(touch_point.x, touch_point.y,ar.getFlyingList().get(i).x,ar.getFlyingList().get(i).y, 
							ar.getFlyingList().get(i).width(),  ar.getFlyingList().get(i).height()))
					{						
						ar.getFlyingList().get(i).isChecked=!ar.getFlyingList().get(i).isChecked;
					}
				}
			}

		});	

		ar.setOnLongClickListener (new OnLongClickListener()//once longClicked, the isLongClicked will be set true for the butterfly. It will be removed in the ARView.
		{
			public boolean onLongClick(View view) 
			{
				for(int i=ar.getFlyingList().size()-1;i>=0;i--)
				{
					if(Calculator.pointInRect(touch_point.x, touch_point.y,ar.getFlyingList().get(i).x,ar.getFlyingList().get(i).y, 
							ar.getFlyingList().get(i).width(),  ar.getFlyingList().get(i).height()))
					{							
						ar.getFlyingList().get(i).isLongClicked=true;ar.setIfUpdate(true);
					}
				}
				return false;
			}


		});	
	}
	protected void onPause(){
		super.onPause();
		ar.AR_thread.setRunning(false);
	    //ar.AR_thread.interrupt();
	}
	protected void onResume(){
		super.onResume();
		ar.AR_thread.setRunning(true);
	}
	

	@SuppressLint("NewApi")
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		ar.AR_thread.setRunning(false);
		ar.AR_thread.interrupt();
		return;
	}
	
	public void toast(String text){
		int duration=Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(ctx, text, duration);
		toast.show();
	}
	
//		@Override
//		public boolean onCreateOptionsMenu(Menu menu) {
//		    // Inflate the menu items for use in the action bar
//		    MenuInflater inflater = getMenuInflater();
//		    inflater.inflate(R.layout.menu_layer, menu);
//		    return super.onCreateOptionsMenu(menu);
//		}
}