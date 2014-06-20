package com.android.butterflygui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.R;
import com.android.AR.CameraActivity;

public class MenuActivity extends MainActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        
        ListView menulist = (ListView) findViewById(R.id.ListView_Menu); //设定menu item
        String[] items = { getResources().getString(R.string.menu_item_play),
                getResources().getString(R.string.menu_item_scores),
                getResources().getString(R.string.menu_item_settings),
                getResources().getString(R.string.menu_item_help) };
        
        ArrayAdapter<String> adapt = new ArrayAdapter<String>(this, R.layout.menu_item, items);
        
     
       menulist.setAdapter(adapt); 
        
        //menulist.setSelection(0); //无法看到
       
   //  menulist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
   //     menulist.setItemChecked(1, true);////也不行
       
   //     menulist.setSelector(R.string.menu_item_scores);
        
        menulist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			//@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				TextView textView= (TextView) view;
				String stringText = textView.getText().toString();
				if(stringText.equalsIgnoreCase(getResources().getString(R.string.menu_item_play)))
				{
				//	startActivity(new Intent(MenuActivity.this, GameActivity.class));
					startActivity(new Intent(MenuActivity.this,CameraActivity.class));
				}
				
				else if(stringText.equalsIgnoreCase(getResources().getString(R.string.menu_item_help)))
				{
				//Todo
				}
				
				else if(stringText.equalsIgnoreCase(getResources().getString(R.string.menu_item_scores)))
				{
				//Todo
				}
				
				else if(stringText.equalsIgnoreCase(getResources().getString(R.string.menu_item_settings)))
				{
				//Todo
				}
			}
		});
   
    }

}