package com.android.AR;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.android.AR.R;
public class MenuActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainmenu);

        // Handle yes button
        Button yesButton = (Button) findViewById(R.id.btn_enter);
        yesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startActivity(new Intent(MenuActivity.this,CameraActivity.class));
            }
        });
	}
}
