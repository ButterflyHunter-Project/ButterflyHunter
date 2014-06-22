package com.android.butterflygui;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.os.Build;

import com.android.R;
import com.android.AR.CameraActivity;
public class FirstInterfaceActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_interface);
		//http://stackoverflow.com/questions/5279182/android-how-to-register-ontouchevent-for-entire-activity-main-content-view
		//ImageButton imageButton = (ImageButton) findViewById(R.id.ImageButton);
		View view = findViewById(R.id.FirstInterfaceView);
		//imageButton.setOnTouchListener(ViewOnTouchListener);
		view.setOnTouchListener(ViewOnTouchListener);
		//Toast.makeText(this, "Action down pressed", Toast.LENGTH_SHORT).show();
	}
	
	//http://blog.sina.com.cn/s/blog_6c1fe98c0101398t.html
	private OnTouchListener ViewOnTouchListener = new OnTouchListener() {
		 
		  //@Override
		  public boolean onTouch(View v, MotionEvent event) {
		   // TODO Auto-generated method stub
		   switch (event.getAction()) {
		  
		   case MotionEvent.ACTION_DOWN:
		   {
			   //不要忘记在androidManifest中注册activity
			startActivity(new Intent(FirstInterfaceActivity.this, CameraActivity.class));
			// Toast.makeText(FirstInterfaceActivity.this, "Action down pressed", Toast.LENGTH_SHORT).show();
		    break;
		   }
		   case MotionEvent.ACTION_MOVE:
		   {
			//startActivity(new Intent(FirstInterfaceActivity.this, MenuActivity.class));
		    break;
		   }
		   case MotionEvent.ACTION_UP:
		   {
			//startActivity(new Intent(FirstInterfaceActivity.this, MenuActivity.class));
		    break;
		   }
		  
		   default:
		   
		    break;
		   }
		   return true; // if return false, it will collapse
		  }

		};

	@Override
	protected void onPause()
	{
		super.onPause();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_interface, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_first_interface,
					container, false);
			return rootView;
		}
	}

}
