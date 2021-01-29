package com.modelsw.loadingassetfiles;

import android.os.Bundle;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class HelpActivity extends AppCompatActivity implements OnClickListener{
	private static final String TAG = "HelpActivity";  
	public TextView mAdStatus;
    Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");
		setContentView(R.layout.help_text);

        // action bar toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setLogo(R.drawable.treble_clef_linen);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.teal));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Navigation Icon tapped");
                finish();
            }
        });

	}


	public void onClick(View v) {
    	switch (v.getId()) {
/*    	case R.id.closehelp_button:
    		finish();
	    	break; */
    	}
	}


}
