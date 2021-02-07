package com.modelsw.loadingassetfiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Scanner;
//import java.lang.Object;
import java.io.InputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;


//private String assetPackName;
//private String assetFilterName;
//private File assetpath;
//private File assetPathDir;
//private File[] assetFile; // names from the file (file format)

public class SelectSongPath extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "SelectSongPath";
    private Intent ap;
    private Button loadNow;
    private RadioButton path1;
    private RadioButton path2;
    private RadioButton path4;
    private RadioButton path5;
    private RadioGroup pathGroup;
    private CharSequence path1Label;
    private CharSequence path2Label;
    private CharSequence path4Label;
    private CharSequence path5Label;
    Toolbar toolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_song_path);
        // action bar toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setLogo(R.drawable.treble_clef_linen);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.teal));
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Navigation Icon tapped");
                finish();
            }
        });


        pathGroup = (RadioGroup) findViewById(R.id.path_group);
        path1 = (RadioButton) findViewById(R.id.path_1);
        path1.setOnClickListener(this);
        path1Label = path1.getText();
        path2 = (RadioButton) findViewById(R.id.path_2);
        path2.setOnClickListener(this);
        path2Label = path2.getText();
        path4 = (RadioButton) findViewById(R.id.path_4);
        path4.setOnClickListener(this);
        path4Label = path4.getText();
        path5 = (RadioButton) findViewById(R.id.path_5);
        path5.setOnClickListener(this);
        path5Label = path5.getText();
        loadNow = (Button) findViewById(R.id.load_now_button);
        loadNow.setOnClickListener(this);
        Log.d(TAG, "path:" + Main.path + " customPathLocation:" + Main.customPathLocation);
        // move the data from assets to definepath and songpath
        String packageName = getPackageName(); // com.modelsw.loadingassetfiles
        switch (Main.path) {
            case 1:
                path1.setChecked(true);
                break;
            case 2:
                path2.setChecked(true);
                break;
            case 4:
                path4.setChecked(true);
                break;
            case 5:
                path5.setChecked(true);
                break;
        }
        if (path1.isChecked() == true) {
            loadNow.setEnabled(false);
        }
    }

    public void onClick(View v) {
        if (path1.isChecked() == false) {
            loadNow.setEnabled(true);
        }
        switch (v.getId()) {
            case R.id.path_1:
                Main.songpath = Main.songPathDir.toString() + "/";
                Main.sharedDefine = null;
                Main.path = 1;
                Log.d(TAG, "onClick path:" + Main.path + " songpath:" + Main.songpath);
                break;
            case R.id.path_2:
                Main.songpath = Main.environment + "/" + getResources().getString(R.string.path2_location) + "/";
                Main.sharedDefine = null;
                Main.path = 2;
                Log.d(TAG, "onClick path:" + Main.path + " songpath:" + Main.songpath);
                break;
            case R.id.path_4:
                //Main.songpath = Main.environment + "/" + getResources().getString(R.string.path4_location) + "/";
                //Main.sharedDefine = Main.environment + "/" + getResources().getString(R.string.path4_define) + "/";
                Main.path = 4;
                Main.songpath = "";
                Log.d(TAG, "path:" + Main.path);
                Main.assetPackName = "SongsNW";
                Main.assetPackLocation = "";
                Main.assetPackLoaded = false;
                Log.d(TAG, "before call assetPackName: SongsNW");
                loadap = new Intent(this, LoadAssetPack.class);
                startActivity(loadap);
                Log.d(TAG, "after call assetPackName Main.assetPackName: " + Main.assetPackName);
                if (Main.assetPackLocation != "" && Main.assetPackLoaded == true) {
                    Main.songpath = Main.environment + "/" + Main.assetPackLocation + "/";
                }
                break;
            case R.id.path_5:
                //Main.songpath = Main.environment + "/" + getResources().getString(R.string.path5_location) + "/";
                //Main.sharedDefine = Main.environment + "/" + getResources().getString(R.string.path5_define) + "/";
                Main.path = 5;
                Main.songpath = "";
                Log.d(TAG, "path:" + Main.path);
                Main.assetPackName = "SongsOW";
                Main.assetPackLocation = "";
                Main.assetPackLoaded = false;
                Log.d(TAG, "before call assetPackName: SongsOW");
                loadap = new Intent(this, LoadAssetPack.class);
                startActivity(loadap);
                Log.d(TAG, "after call assetPackName Main.assetPackName: " + Main.assetPackName);
                if (Main.assetPackLocation != "" && Main.assetPackLoaded == true) {
                    Main.songpath = Main.environment + "/" + Main.assetPackLocation + "/";
                }
                break;
            case R.id.load_now_button:
                if (path1.isChecked() == false) {
                    checkForNewFiles();
                } else {
                    String msg = "Please select an external path";
                    Log.d(TAG, msg);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


    protected void onPause() {
        super.onPause();
        //if (Main.isDebug == true) {
        // keep the external path
        //} else {
        Main.path = 1; // don't confuse the issue
        Main.songpath = Main.songPathDir.toString() + "/";
        //}
        Log.d(TAG, "onPause saveTheSongPath path:" + Main.path + " customPathLocation:" + Main.customPathLocation);
        Main.db.beginTransaction();
        String qry = "UPDATE SongPath SET Path = " + Main.path + ", CustomPath = '" + Main.customPathLocation + "'";
        Main.db.execSQL(qry);
        Main.db.setTransactionSuccessful();
        Main.db.endTransaction();
    }

    void checkForNewFiles() {  // moves them out of selected path to local/Songs/ directory.
        // and filter out of selected path Define to local/Define/ directory
        // note: this is only called if path > 1
        // after moving the files, path is re-set to 1
        // songpath is selected path which is > 1 -- could be null if invalid path from custom.
        if (Main.songpath == null || Main.songdata == null) {
            finish();
            return;
        }
        if (Main.sharedDefine != null) {
            int pathLen = Main.sharedDefine.length();
            Log.d(TAG, "* checkForNewFiles() sharedDefine:" + Main.sharedDefine);

            File dirDef = new File(Main.sharedDefine);
            Log.d(TAG, "onCreate: dirDef:" + dirDef);
            File[] defineFile = dirDef.listFiles();
            int defFileLen = 0;
            if (defineFile != null) {
                defFileLen = defineFile.length;
                Log.d(TAG, "* checkForNewDefineFiles() defFileLen:" + defFileLen);
                //String[] defines = new String[defFileLen]; // names from the folder
                for (int i = 0; i < defFileLen; i++) {
                    //defines[i] = defineFile[i].toString().substring(pathLen);
                    //String nam = defines[i];
                    String nam = defineFile[i].toString().substring(pathLen);
                    int extLoc = nam.length() - 4;
                    String ext = nam.substring(extLoc);
                    Log.d(TAG, " definefile:" + nam + " ext:" + ext);
                    if (ext.equalsIgnoreCase(".csv")) { // only transfers csv files
                        Boolean success = defineFile[i].renameTo(new File(Main.definepath + nam));
                        Log.d(TAG, " did i move file:" + nam + " ?:" + success);
                        if (nam.equals("filter.csv")) {
                            loadFilterData();  // it is loaded into database
                            //deleteFile(nam); // crash on has a file separater
                        }
                    }
                }
            }
        }
        // localPath is songPath = 1 from getExternalFilesDir("Song");
        String localPath = Main.songPathDir.toString() + "/";
        Log.d(TAG, "* checkForNewFiles() path:" + Main.path + " songpath:" + Main.songpath);
        int pathLen = Main.songpath.length();
        File dir = new File(Main.songpath);
        Log.d(TAG, "onCreate: dir:" + dir);
        Main.songFile = dir.listFiles();
        String nums = "0123456789";
        String ch = "";
        int cntr = 0;
        int songsFileLen = 0;
        if (Main.songFile == null) {
            Log.d(TAG, "* checkForNewFiles() songFile is null -- closing");
            String msg = "invalid path:" + Main.songpath;
            Log.d(TAG, msg);
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return;
        } else {
            songsFileLen = Main.songFile.length;
            Log.d(TAG, "* checkForNewFiles() songFileLength:" + songsFileLen);        // crash if songPath is null
            Main.songs = new String[songsFileLen];
            for (int i = 0; i < songsFileLen; i++) {
                Main.songs[i] = Main.songFile[i].toString().substring(pathLen);
                //  01234567890123456789012345678901234
                String nam = Main.songs[i];         // "16 White-crowned Sparrow Song 1.mp3"
                //Log.d(TAG, "* checkForNewFiles() songs[" + i + "] " + nam );
                Boolean isAlbumNumber = false;
                Boolean isStartsWithXC = false;
                int chLoc = nam.indexOf(' '); // 2
                if (chLoc > 0 && chLoc < 5) {
                    ch = nam.substring(0, chLoc);
                    //Log.d(TAG, "ch:" + ch);

                    if (nums.contains(ch.substring(0, 1)) == true && nums.contains(nam.substring(chLoc + 1, chLoc + 2)) == false) {
                        isAlbumNumber = true;
                        // find the first beyond the name -- its a number followed by a space followed by a letter
                        int songLoc = nam.indexOf(" Song"); // 24
                        int callLoc = nam.indexOf(" Call"); // -1
                        int drumLoc = nam.indexOf(" Drum"); // -1
                        int extLoc = nam.length() - 4;
                        int afterName = Math.max(chLoc, extLoc); // 31
                        if (afterName > chLoc) {
                            if (songLoc > chLoc) {
                                afterName = Math.min(songLoc, afterName); // 24
                            }
                            if (callLoc > chLoc) {
                                afterName = Math.min(callLoc, afterName);
                            }
                            if (drumLoc > chLoc) {
                                afterName = Math.min(drumLoc, afterName);
                            }
                            if (afterName == extLoc) { // just the name.ext
                                Main.newName = nam.substring(chLoc + 1, afterName) + ch + nam.substring(afterName);
                            } else {  // additional words between
                                Main.newName = nam.substring(chLoc + 1, afterName) + ch + nam.substring(afterName + 1);
                            }
                            Main.songFile[i].renameTo(new File(localPath + Main.newName));
                            cntr++;
                        }
                    }
                } else if (nam.substring(0, 2).equals("XC")) {    // XC179353-Northern Mockingbird 140414-002.mp3
                    chLoc = nam.indexOf("-");                    // 0123456789012345678901234567890123456789012
                    isStartsWithXC = true;
                    if (chLoc > 3) {
                        ch = nam.substring(0, chLoc);  // XC179353
                        int extLoc = nam.length() - 4;
                        if (extLoc > chLoc) {  // check for .mp3 file
                            // Northern Mockingbird 140414-002-XC179353.mp3
                            Main.newName = nam.substring(chLoc + 1, extLoc) + "-" + ch + nam.substring(extLoc);
                            Main.songFile[i].renameTo(new File(localPath + Main.newName));
                            cntr++;
                        }
                    }
                }
                if ((isAlbumNumber == false) && (isStartsWithXC == false)) { // for Birding Via Mic_XX external apps or full name Download
                    int extLoc = nam.length() - 4;
                    String ext = nam.substring(extLoc);
                    if (ext.equalsIgnoreCase(".mp3") || ext.equalsIgnoreCase(".m4a") ||
                            ext.equalsIgnoreCase(".wav") || ext.equalsIgnoreCase(".ogg")) {
                        Main.newName = nam; // it's a song file load it like it is.
                        Main.songFile[i].renameTo(new File(localPath + Main.newName));
                        cntr++;
                    }

                }
                Log.d(TAG, "* checkForNewFiles() newName:" + Main.newName);
            } // finished loading files
            // check the Define folder for filter.csv
        }
        Main.db.beginTransaction();
        // remove the non-internal songs from the songlist
        String qry = "DELETE FROM SongList WHERE Path = " + Main.path;
        Main.db.execSQL(qry);
        Main.path = 1;
        pathGroup.check(R.id.path_1);
        Main.sharedDefine = null;
        Main.songpath = Main.songPathDir.toString() + "/";
        qry = "UPDATE SongPath SET Path = " + Main.path;
        Main.db.execSQL(qry);
        Main.db.setTransactionSuccessful();
        Main.db.endTransaction();
        String msg = "Files in Download:" + songsFileLen + " Files Loaded:" + cntr;
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Log.d(TAG, "* return from checkForNewFiles() path:" + Main.path + " songpath:" + Main.songpath);
    }

    void loadFilterData() { // called only on transfer from external data if filter.csv file now exists
        Log.d(TAG, "loadFilterData()");
        Scanner filtr = null;
        File localFilter = null;
        try {
            localFilter = new File(Main.definePathDir.toString() + "/filter.csv");
            filtr = new Scanner(new BufferedReader(new FileReader(localFilter)));
            Log.d(TAG, "loadFilterData - clear existing filter -- load new data");
            Main.db.beginTransaction();
            String qry = "DELETE FROM Filter";  // clear out the table first it may have previous data
            Main.db.execSQL(qry);
            Main.db.setTransactionSuccessful();
            Main.db.endTransaction();
            // the filter file  has FileName XCxxxxxx.m4a, filterType, integerValue
            ContentValues val = new ContentValues();
            try {
                String line;
                String[] tokens;
                while ((line = filtr.nextLine()) != null) {
                    tokens = line.split(",");
                    if (tokens.length == 3) {
                        Main.db.beginTransaction();
                        val.put("XcName", tokens[0]);  // it was XC123456 now it is the Full Name XC123456.m4a
                        val.put("FilterType", tokens[1]);
                        val.put("FilterVal", Integer.parseInt(tokens[2]));
                        Log.d(TAG, " filter data:" + tokens[0] + "," + tokens[1] + "," + Integer.parseInt(tokens[2]));
                        Main.db.insert("Filter", null, val);
                        Main.db.setTransactionSuccessful();
                        Main.db.endTransaction();
                        val.clear();
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "internal error loading filter:" + e);
            } finally {
                Main.isFilterExists = true;
                filtr.close(); // does this close cause an exception ? -- file will be deleted on return -- no it is left there
                Log.d(TAG, "Close and Cleanup file filter");
            }
        } catch (Exception e) {
            // the file dosen't exist leave quitely.
            Log.d(TAG, "Exit checkVersion -- filter.csv does NOT Exist:" + e);
            Main.isFilterExists = false;
            return;
        }
        //boolean success = deleteFile(localFilter.toString()); // crash on contains file separator
        //Log.d(TAG, " delete filter.csv ?" + success);

    }


}
