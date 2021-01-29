package com.modelsw.loadingassetfiles;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Gerard on 6/28/2015.
 */
public class Alert3ButtonDialog extends Activity implements OnClickListener {
    private static final String TAG = "Alert3ButtonDialog";
    private TextView textName;
    private TextView textTitle;
    private Button b1;
    private Button b2;
    private Button b3;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog3);
        textTitle = (TextView) findViewById(R.id.identify_title);
        textName = (TextView) findViewById(R.id.identify_text);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        b1=(Button)findViewById(R.id.button1);
        b2=(Button)findViewById(R.id.button2);
        b3=(Button)findViewById(R.id.button3);

        switch (Main.alertRequest) {

            case 2: // delete file and or definitions
                setResult(2); // cancel in case they exit out
                textTitle.setText("Delete:");
                Main.listOffset = 0;
                String msg = "Song(s):\n";
                for (int i = 0; i < Main.songsDbLen; i++) {
                    if (Main.ck[i] == true) {
                        if (Main.listOffset == 0) {
                            Main.listOffset = i;
                        }
                        msg += Main.songs[i] + "\n";
                    }
                } // next i
                textName.setText((CharSequence) msg);
                b1.setText("File And Definition");
                b2.setText("Definition Only");
                b3.setText("No - Cancel");
                break;
            case 3: // delete species
                setResult(1); // cancel in case they exit out
                textTitle.setText("Delete:");
                msg = "Species:\n";
                msg += Main.existingSpec + "\n";
                textName.setText((CharSequence) msg);
                b1.setText("Yes");
                b2.setText("No - Cancel");
                b3.setVisibility(View.GONE);
                break;
            case 4: // delete web
                setResult(1); // cancel in case they exit out
                textTitle.setText("Delete:");
                msg = "Web:\n";
                msg += Main.existingWebName + "\n";
                textName.setText((CharSequence) msg);
                b1.setText("Yes");
                b2.setText("No - Cancel");
                b3.setVisibility(View.GONE);
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
            case R.id.button2: {
                setResult(1); // 1 = Definition Only // No - Cancel
                finish();
                break;
            }
            case R.id.button3: {
                setResult(2); // 2 = No - Cancel // hidden
                finish();
                break;
            }
        } // switch
    } // on click

}
