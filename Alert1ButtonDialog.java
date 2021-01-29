package com.modelsw.loadingassetfiles;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Gerard on 6/28/2015.
 */
public class Alert1ButtonDialog extends Activity implements OnClickListener {
    private static final String TAG = "Alert1ButtonDialog";
    private TextView textName;
    private TextView textTitle;
    private Button b1;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog1);
        textTitle = (TextView) findViewById(R.id.identify_title);
        textName = (TextView) findViewById(R.id.identify_text);
        findViewById(R.id.button1).setOnClickListener(this);
        b1=(Button)findViewById(R.id.button1);

        switch (Main.alertRequest) {

            case 5: // meta data info box
                //Log.d(TAG, "metadata:" + Main.metaData);
                setResult(0); // cancel in case they exit out
                textTitle.setText("Meta Data:");
                String msg = Main.metaData;
                textName.setText(msg);
                LinearLayout ll = new LinearLayout(this);
                ll.layout(ll.getTop(), ll.getLeft(), ll.getRight(), 50);
                b1.setText("OK");
                break;
            case 6: // main database upgrade complete
                //Log.d(TAG, "main upgrade complete);
                setResult(0); // cancel in case they exit out
                textTitle.setText("Database Upgrade:");
                msg = "Upgrade is complete.";
                textName.setText(msg);
                ll = new LinearLayout(this);
                ll.layout(ll.getTop(), ll.getLeft(), ll.getRight(), 50);
                b1.setText("OK");
                break;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1: {
                setResult(0); // 0 = File and Definition // Yes  // OK
                finish();
                break;
            }
        } // switch
    } // on click

}
