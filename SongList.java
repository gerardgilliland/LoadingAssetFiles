package com.modelsw.loadingassetfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SongList extends AppCompatActivity {
	private static final String TAG = "SongList";
	private Boolean foundExisting = false;
	private Boolean foundRenamed = false;
    private ListView list;
	private String qry = "";
	private Cursor rs;  // I see cursor as RecordSet (rs)
	private Cursor rsCk; 
	private String songPath = null;
	private int filtLow;
	private int filtHi;
	private int filtBeg;
	private int filtEnd;
	private int filtStrt;
	private int filtStop;
	private int songsDbLen;  // count of songs in Database
	private int songsFileLen; // count of songs in file
	private int songsLen;  // the max of the above plus some space
	Toolbar toolbar;
	private byte[] metaBuffer;
	char q = 34;

    @Override
    public void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate songpath:" + Main.songpath + " songdata:" + Main.songdata);
        setContentView(R.layout.songlist_header );
        // action bar toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setLogo(R.drawable.treble_clef_linen);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.teal));
		toolbar.showOverflowMenu();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "Navigation Icon tapped");
                finish();
            }
        });
		if (Main.songdata == null || Main.songpath == null) { // knocked out of memory re-init the database
			Main.fileReshowExisting = true;
			finish();
			return;
		}
		songPath = Main.songpath;
		buildList();
		Main.db = Main.songdata.getWritableDatabase();
		qry = "SELECT FileName from SongList WHERE FileName = '@_RampSource.wav'";
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		rs.close();
        list = (ListView) findViewById(R.id.list);  // list is in song_list.xml
        list.setFastScrollEnabled(true);
       	Main.songStartAtLoc = 0;
       	Main.songStopAtLoc = 0;   	
       	Main.isNewStartStop = false;
       	Main.showPlayFromList = false;
		Log.d(TAG, "onCreate path:" + Main.path );
		Button delete=(Button)findViewById(R.id.delete_button);
		delete.setOnClickListener(listener);
		if (Main.fileRenamed == true || Main.fileReshowExisting == true) {
			list.setSelection(Main.listOffset-1);
		}
		Main.fileRenamed = false;
		Main.fileReshowExisting = false;
    } // onCreate


	void buildList() {
    	char q = 34;
		InputStream is;
		Log.d(TAG, "buildList:" + songPath );		// crash if songPath is null		
        int pathLen = songPath.length();
        File dir = new File(songPath);
        if (dir.exists() == false) {
        	String msg = "Your song path:" + songPath + " is invalid";
        	Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            Log.d(TAG, msg);
			finish();
        	return;
        } 
       	Log.d(TAG, "buildList: dir:" + dir);
    	Main.songFile = dir.listFiles();
    	if (Main.songFile == null) {
    		Log.d(TAG, "onCreate songFile is null -- closing" );        
        	String msg = "invalid path:" + songPath;
        	Log.d(TAG, msg);
     	   	finish();
			return;
    	}
		Log.d(TAG, "buildList songFile is not null -- length:" + Main.songFile.length );
		String z = "zzzzzzzz";
		ContentValues val = new ContentValues();
		qry = "SELECT FileName FROM SongList" +
			" WHERE FileName = '" + z + "'"; 
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		if (rs.getCount() == 0) {
			Main.db.beginTransaction();
			val.put("Ref", 39997);
			val.put("Inx", 1);
			val.put("Seg", 0);
			val.put("Path", Main.path);				
			val.put("FileName", z);
			val.put("Start", 0);
			val.put("Stop", 0);
			val.put("Identified", 0);  
			val.put("Defined", 0);
   			val.put("AutoFilter", 0);
   			val.put("Enhanced", 0);
   			val.put("Smoothing", 0);
   			val.put("SourceMic", 0);
            val.put("SampleRate", 0);
            val.put("AudioSource", -1);
			val.put("Stereo", 0);
   			val.put("LowFreqCutoff", 0);
   			val.put("HighFreqCutoff", 0);
   			val.put("FilterStart", 0);
   			val.put("FilterStop", 0);
   			Main.db.insert("SongList", null, val);
   			Main.db.setTransactionSuccessful();
   			Main.db.endTransaction();
   			val.clear();
			rs.close();
		}
		qry = "SELECT FileName FROM SongList" +
				" WHERE Path = " + Main.path +
				" GROUP BY FileName" + 
    			" ORDER BY FileName";
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		songsDbLen = rs.getCount();  // count (indexed 0 to < count
		if (songsDbLen == 0) {
			String msg = "No songs are available. Please load or record some. See Help.";
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			Log.d(TAG, msg);
			finish();
			return;
		}
		rs.moveToFirst();
		songsFileLen = Main.songFile.length;
		Log.d(TAG, "buildList songsDbLen:" + songsDbLen + " songsFilesLen:" + songsFileLen );
		songsLen = Math.max(songsDbLen, songsFileLen);  // for dim of arrays
		songsLen += 20;  // save more than enough room for additions.		
    	Main.songs = new String[songsLen]; 
    	Log.d(TAG, "START Update database isFilterExists:" + Main.isFilterExists );
		//int kcc = 0;
    	for (int i=0; i<songsFileLen; i++) {
    		Main.songs[i] = Main.songFile[i].toString().substring(pathLen);
			//Log.d(TAG, "buildList songs[" + i + "] " + Main.songs[i]) ; // + " kcc:" + kcc);
    	}
    	for (int i=songsFileLen; i<songsLen; i++) {    		
    		Main.songs[i] = z;
    	}
    	Arrays.sort(Main.songs);
		int ifile = 0;
		int ref = 0;
		String atSign = null;
		int sourceMic = 0;
		int maxInx = 0;
		// remember: there are duplicate names in the database file=xxx seg=0; file=xxx seg=1
		if (songsDbLen == 0) {
			Log.d(TAG, "EMPTY Database" );
			for (ifile = 0; ifile < songsFileLen; ifile++) {
				Log.d(TAG, "Adding '" + Main.songs[ifile] + "' to the SongList ref:" + ref);
				atSign = Main.songs[ifile].substring(0,1);  
				filtLow = 0;
				filtHi = 0;
				filtBeg = 0;
				filtEnd = 0;
				if (atSign.equals("@")) { // song starts with "@_date ...
					int lenExist = Main.songs[ifile].length();
					ref = 0;
				} else {
					ref = tryForSpec(Main.songs[ifile]);  // the file name				
					if (Main.isFilterExists = true) {
						checkForFilter(Main.songs[ifile]);
					}
				}
				if (ref == 0) {
					maxInx = 0;
				} else {
					qry = "SELECT MAX(Inx) AS MaxInx FROM SongList" +
						" WHERE Ref = " + ref;
					rsCk = Main.songdata.getReadableDatabase().rawQuery(qry, null);
					rsCk.moveToFirst();
					maxInx = rsCk.getInt(0)+1;  // increment the last known inx
					rsCk.close();
				}
				// this code when added changes time to list 174 songs from 10 ms to over 3 sec -- two errors for every file
				// so only run this when adding a file
				sourceMic = 0; // adding a file so it has not been recorded here
				Main.audioSource = -1;
				try {
					MediaExtractor extractor = new MediaExtractor();
					extractor.setDataSource(Main.songpath + Main.songs[ifile]);
					MediaFormat format = extractor.getTrackFormat(0);
					int chancnt = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
					extractor.release();
					Main.stereoFlag = 0;
					if (chancnt == 2) {
						Main.stereoFlag = 1; // stereo
					}
				} catch (IOException e) {
					Log.e(TAG, "error:" + e + " " +  Main.songs[ifile]);
				}
				Main.db.beginTransaction();
				val = new ContentValues();
				val.put("Ref", ref);
				val.put("Inx", maxInx);
				val.put("Seg", 0);
				val.put("Path", Main.path);				
				val.put("FileName", Main.songs[ifile]);
				val.put("Start", 0);
				val.put("Stop", 0);
				val.put("Identified", 0);  
				val.put("Defined", 0);
           	   	val.put("AutoFilter", 0);
				val.put("Enhanced", 0);
	   			val.put("Smoothing", 0);
				val.put("SourceMic", sourceMic); // pre-recorded
                val.put("SampleRate", 4);  // 0=22050, 1=44100 2=24000, 3=48000, 4=unknown
                val.put("AudioSource", Main.audioSource); // really unknown 0=default, 1=mic, 5=camcorder, 6 voice recognition
				val.put("Stereo", Main.stereoFlag); // 0 = mono, 1 = stereo
                val.put("LowFreqCutoff", filtLow);
				val.put("HighFreqCutoff", filtHi);
				val.put("FilterStart", filtBeg);
				val.put("FilterStop", filtEnd);
				Main.db.insert("SongList", null, val);
				Main.db.setTransactionSuccessful();
				Main.db.endTransaction();
				val.clear();
			} // next ifile
		} else { // not empty database
			rs.moveToFirst();
			int rsLen = rs.getCount();
			while (!rs.isAfterLast()) {
				String nam = rs.getString(0);
				int result = nam.compareTo(Main.songs[ifile]); // -1 if file < dbName, 0 if dbName = file, +1 if file > dbName
				//Log.d(TAG, "  " + Main.songs[ifile] + "<-file " + result + " db->" + nam );
				if (result < 0) { // file < dbName -- so the dbName needs to be deleted
					// this is deleting from the song list as always to keep the song files and database in sync
					// when this code is executed the file HAS ALREADY BEEN deleted. -- this deletes the record in the database.
					// when Delete option within SongList is executed the file and record are both deleted and this code is not run.
					Log.d(TAG, "Deleting " + nam + " from SongList");
					Main.db.beginTransaction();
						qry = "DELETE FROM SongList" +
								" WHERE Path = " + Main.path +
								" AND FileName = " + q + nam + q;
						Main.db.execSQL(qry);
						Main.db.setTransactionSuccessful();
						Main.db.endTransaction();
					rs.moveToNext();
				}
				if (result == 0) { // do nothing (except keep the files in sync)
					rs.moveToNext();
					ifile++;
				}
				if (result > 0) {  // file > dbName name -- so the song needs to be added
					//Log.d(TAG, "Adding '" + Main.songs[ifile] + "' to the SongList");
					atSign = Main.songs[ifile].substring(0,1);  
					filtLow = 0;
					filtHi = 0;
					filtBeg = 0;
					filtEnd = 0;
					filtStrt = 0;
					filtStop = 0;
					if (atSign.equals("@")) { // song starts with "@_date ...
						ref = 0;
					} else {
						ref = tryForSpec(Main.songs[ifile]);  // the file name
						if (Main.isFilterExists = true) {
							checkForFilter(Main.songs[ifile]);
						}
					}
					if (ref == 0) {
						maxInx = 0;
					} else {
						qry = "SELECT MAX(Inx) AS MaxInx FROM SongList" +
							" WHERE Ref = " + ref;
						rsCk = Main.songdata.getReadableDatabase().rawQuery(qry, null);
						rsCk.moveToFirst();
						maxInx = rsCk.getInt(0)+1;  // increment the last known inx
						rsCk.close();
					}
					// this code when added changes time to list 174 songs from 10 ms to over 3 sec -- two errors for every file
					// so only run this when adding a file
					sourceMic = 0; // adding a file so it has not been recorded
					Main.audioSource = -1;
					try {
						Main.stereoFlag = 0; // mono
						MediaExtractor extractor = new MediaExtractor();
						extractor.setDataSource(Main.songpath + Main.songs[ifile]);
						MediaFormat format = extractor.getTrackFormat(0);
						int chancnt = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
						extractor.release();
						if (chancnt == 2) {
							Main.stereoFlag = 1; // stereo
						}
					} catch (IOException e) {
						Log.e(TAG, "error:" + e + " " +  Main.songs[ifile]);
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "error:" + e + " " +  Main.songs[ifile]);
					}
	   	            try {        	
	   	            	Log.d(TAG, "db begin transaction -- adding file:" + Main.songs[ifile]);
	   	            	Main.db.beginTransaction();
   	            		val = new ContentValues();
	   	            	try {
	   	            		val.put("Ref", ref);
	   	            		val.put("Inx", maxInx);
	   	            		val.put("Seg", 0);
	   	            		val.put("Path", Main.path);
	   	            		val.put("FileName", Main.songs[ifile]);
	   	            		val.put("Start", 0);
	   	            		val.put("Stop", 0);
	   	            		val.put("Identified", 0);  
	   	            		val.put("Defined", 0);
	   	            	   	val.put("AutoFilter", 0);
	   	            		val.put("Enhanced", 0);
	   	        			val.put("Smoothing", 0);
	   	            		val.put("SourceMic", sourceMic); // pre-recorded
							val.put("SampleRate", Main.sampleRateOption);  // 0=22050, 1=44100
                            val.put("AudioSource", Main.audioSource); // 0=default, 1=mic, 5=camcorder, 6 voice recognition
							val.put("Stereo", Main.stereoFlag); // 0 = mono, 1 = stereo
	   	            		val.put("LowFreqCutoff", filtLow);
	   	            		val.put("HighFreqCutoff", filtHi);
	   	            		val.put("FilterStart", filtBeg);
	   	            		val.put("FilterStop", filtEnd);
	   	            		Main.db.insert("SongList", null, val);
	   	            		Main.db.setTransactionSuccessful();					
	   	            	} finally {
	   	            		Main.db.endTransaction();
	   	            		val.clear();
	   	            		//Log.d(TAG, "db end transaction");
	   	            	}
	   	            } catch( Exception e ) {
	   	            	Log.e(TAG, "Database Exception: " + e.toString() );     	    
	   	            }
					if (filtStrt > 0 || filtStop > 0) {
						Log.d(TAG, "Start from XC file:" + filtStrt + " Stop:" + filtStop);
						qry = "SELECT MAX(Seg) AS MaxSeg FROM SongList" +
								" WHERE Ref = " + ref + " AND Inx = " + maxInx;
						rsCk = Main.songdata.getReadableDatabase().rawQuery(qry, null);
						rsCk.moveToFirst();
						int maxSeg = rsCk.getInt(0)+1;  // increment the last known seg
						rsCk.close();
						try {
							//Log.d(TAG, "db begin transaction");
							Main.db.beginTransaction();
							val = new ContentValues();
							try {
								val.put("Ref", ref);
								val.put("Inx", maxInx);
								val.put("Seg", maxSeg);
								val.put("Path", Main.path);
								val.put("FileName", Main.songs[ifile]);
								val.put("Start", filtStrt);
								val.put("Stop", filtStop);
								val.put("Identified", 0);
								val.put("Defined", 0);
								val.put("AutoFilter", 0);
								val.put("Enhanced", 0);
								val.put("Smoothing", 0);
								val.put("SourceMic", sourceMic); //
								val.put("SampleRate", Main.sampleRateOption);  // 0=22050, 1=44100
								val.put("AudioSource", Main.audioSource); // 0=default, 1=mic, 5=camcorder, 6 voice recognition
								val.put("Stereo", Main.stereoFlag); // 0 = mono, 1 = stereo
								val.put("LowFreqCutoff", 0);
								val.put("HighFreqCutoff", 0);
								val.put("FilterStart", 0);
								val.put("FilterStop", 0);
								Main.db.insert("SongList", null, val);
								Main.db.setTransactionSuccessful();
							} finally {
								Main.db.endTransaction();
								val.clear();
								Log.d(TAG, "new record from filter start and stop");
							}
						} catch( Exception e ) {
							Log.e(TAG, "failed to add record from filter Start Stop: " + e.toString() );
						}

					}
					// don't increment database let the file catch up
					ifile++;
				}
			} // while
			
		} // else database has data 
    	Log.d(TAG, "END Update database");
		// at this point the database names match the file names
		// now build the list which includes the location and options
   	    qry = "SELECT Area from Region WHERE isSelected = 1";
   		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
   		rs.moveToFirst();
   		int cntr = rs.getCount();
   		String[] area = new String[cntr];
   		for (int i = 0; i<cntr; i++) {
   			area[i] = rs.getString(0);
			rs.moveToNext();
   		}
		qry = "SELECT Type from RedList WHERE isSelected = 1";
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		int cntrR = rs.getCount();
		String[] type = new String[cntrR];
		for (int i = 0; i<cntrR; i++) {
			type[i] = rs.getString(0);
			rs.moveToNext();
		}

		// build the list from the latest database
		qry = "PRAGMA case_sensitive_like = 1";
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);

		qry = "SELECT FileName, CodeName.Spec, CodeName.Ref, Inx, Seg, Identified, Defined, AutoFilter, Enhanced, Smoothing," +
                " SourceMic, SampleRate, AudioSource, Stereo," +
				" CodeName.Region, CodeName.SubRegion, CodeName.RedList," +
				" LowFreqCutoff, HighFreqCutoff, FilterStart, FilterStop" +
				" FROM SongList JOIN CodeName ON SongList.Ref = CodeName.Ref" + 
				" WHERE path = " + Main.path ;
		if (cntr == 0) {
			qry += " AND CodeName.Region = 'none'";
		} else {
			int oneTime = 0;
			for (int i = 0; i< cntr; i++) {
				if (oneTime == 0) {
					qry += " AND (CodeName.Region like '%" + area[i] + "%'";
					oneTime++;
				} else {
					qry += " OR CodeName.Region like '%" + area[i] + "%'";
				}
				if (area[i].equals("Worldwide")) {
					qry += " OR CodeName.SubRegion like '%introduced worldwide%'";
				}
			}
			qry += ")";
			if (cntrR == 0) {
				qry += " AND RedList = 'None'";
			} else {
				oneTime = 0;
				for (int i = 0; i < cntrR; i++) {
					if (oneTime == 0) {
						qry += " AND (RedList = '" + type[i] + "'";
						oneTime++;
					} else {
						qry += " OR RedList = '" + type[i] + "'";
					}
				}
				qry += ")";
			}
			if (Main.isUseLocation == true) {
				qry += " AND InArea = 1 ";
			}
			if (Main.isSortByName == true) {
				qry += " ORDER BY FileName";
			} else {
				qry += " ORDER BY CodeName.Ref";
			}
		}
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		songsDbLen = rs.getCount(); // no extra 20 now
		Main.songsDbLen = songsDbLen;
		songsLen = songsDbLen + 20;
		Main.songs = new String[songsLen];
		Main.songsCombined = new String[songsLen];
		Main.ref = new int[songsLen];
		Main.inx = new int[songsLen];
		Main.seg = new int[songsLen];
		Main.ck = new Boolean[songsLen];
		Main.selectedSong = new int[songsLen];
		rs.moveToFirst();
		for (int i=0; i<songsDbLen; i++) {
			Main.songs[i] = rs.getString(0);  // the file name
			Main.ref[i] = rs.getInt(2);
			Main.inx[i] = rs.getInt(3);
			Main.seg[i] = rs.getInt(4);
			int iref = Main.ref[i];
			int iden = rs.getInt(5); // identified (set if Identified) only from play song id button
			int idef = rs.getInt(6); // Defined
			int iaut = rs.getInt(7); // AutoFilter
			int ienh = rs.getInt(8); // Enhanced
			int ismo = rs.getInt(9); // Smoothing
			int imic = rs.getInt(10); // SourceMic  0=pre-recorded, 1=internal, 2=external
            int isrt = rs.getInt(11); // SampleRate 0=22050, 1=44100, 2=24000, 3=48000, 4=unknown
            int iaud = rs.getInt(12); // AudioSource 0,1,5, or 6, -1=unknown
			int iste = rs.getInt(13); // Stereo 0=mono, 1=stereo, -1=unknown
			int if1 = rs.getInt(17); //  lowFreqCutoff
			int if2 = rs.getInt(18); //  highFreqCutoff
			int if3 = rs.getInt(19); // filterBegin
			int if4 = rs.getInt(20); // filterEnd
			String def = " ";

            switch (imic) { // first microphone
                case 0:
					def += ".";  // pre-recorded
					iaud = -1;
					break;
				case 1: {
					def += "i"; // internal
					break;
				}
				case 2: {
					def += "x"; // external
					break;
				}
			}
			switch (iste) { // second mono/stereo
				case -1: {
					def += "."; // unknown
					break;
				}
				case 0:	{
                    def += "m";  // mono
                    break;
                }
				case 1: {
					def += "s"; // stereo
					break;
				}
            }
            switch(isrt) { // third sample rate
                case 0: {
                    def += "0";  // 22050
                    break;
                }
                case 1: {
                    def += "1"; // 44100
                    break;
                }
				case 2: {
					def += "2";  // 24000
					break;
				}
				case 3: {
					def += "3"; // 48000
					break;
				}
				case 4: {
					def += "."; // unknown
					break;
				}
            }
			if (imic == 0) {  // pre-recorded
				def += ".";
			} else {
				switch (iaud) { // fourth audio source
					case 0: {
						def += "d";  // the default was used
						break;
					}
					case 1: {
						def += "m";  // the microphone was used
						break;
					}
					case 5: {
						def += "c";  // the camcorder was used
						break;
					}
					case 6: {
						def += "v";  // the voice recognition was used
						break;
					}
				}
			}
			// PROCESS
			if (if1 > 0 || if2 > 0 || if3 > 0 || if4 > 0) { /// fourth . Filter  -- low freq, high freq, begin end noise
				def += "f"; // manual filter exists
			} else if (iaut == 1) {
				def += "a";
			} else {
				def += ".";  // not used
			}
			if (ienh == 0) { // fifth enhanced
				def += ".";  // not processed
			} else {
				def += "e";  // use digital filter
			}
			if (ismo == 0) { // sixth smoothing
				def += ".";  // not processed
			} else {
				def += "s";  // use smoothing
			}

			// RESULT
			if (iden == 0) {  // seventh identified set Identified to 0 to remove "i" from SongList
				def += "."; // not identified
			} else if (iden == 1) {
				def += "i"; // identified
			} else if (iden == 2) {
				def += "E"; // decoder Error
			} else if (iden > 2) {
				def += "?";  // rejected identification  the identifiedRef didn't match the definedRef
			}
			if (idef == 0) { // eighth defined analyzed and saved  -- set Defined to 0 to remove "d" from the SongList
				def += ".";  // not defined
			} else if (idef == 1) {
				def += "d"; // defined
			} else if (idef == 2) {
				def += "E"; // decoder Error
			}
			if (Main.fileRenamed == true) {
				if (foundRenamed == false) {
					if (Main.existingName == null || Main.songs == null) {
						Main.fileRenamed = false;
						finish();
						return;
					}
					if (Main.existingName.equals(Main.songs[i])) {
						Main.listOffset = i;
						foundRenamed = true;
					}
				}
			}

			if (Main.fileReshowExisting == true) {
				if (foundExisting == false) {
					if (Main.existingName == null || Main.songs == null) {
						Main.fileReshowExisting = false;
						finish();
						return;
					}
					if (Main.existingName.equals(Main.songs[i])) {
						Main.listOffset = i;
						foundExisting = true;
					}
				}
			}
			String specInxSeg = rs.getString(1) + "_" + rs.getInt(3) + "." + rs.getInt(4) + " " + rs.getString(16);  // Species name_1.0 RedList
			Main.songsCombined[i] = rs.getString(0) + def + "\n\t" + specInxSeg + "\n\t" + rs.getString(14) + " : " + rs.getString(15); // fileName newline specIncSeg
			Main.ck[i] = false;
			rs.moveToNext();
		}
		qry = "PRAGMA case_sensitive_like = 0";
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		rs.close();
		
    } // buildList
    
    int tryForSpec(String filname) {
    	int ref = 0;  // unknown
//    	Log.d(TAG, "try for spec filname:" + filname);    	
    	int fillen; // the length of the common name from the database
		fillen = filname.length(); // the length of the common name from the database
    	String fil3 = filname.substring(0,3);  // the name can be as short as 3 before blanks
    	qry = "SELECT Ref, CommonName FROM CodeName WHERE CommonName LIKE " + q + fil3 + "%" + q; // don't look at all of them
		rsCk = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		int cntr = rsCk.getCount();
    	if (cntr == 0) {
        	Log.d(TAG, "returning ref:" + ref + " Unknown");
        	rsCk.close();
    		return ref;  // Unknown
    	}
		rsCk.moveToFirst();
    	for (int i = 0; i < cntr; i++) {
    		String comname = rsCk.getString(1);  // common name from the database
    		int comlen = comname.length(); // the length of the common name from the database
    		if (comlen < fillen) { // the database name is shorter than the file name - continue ( file has .wav or .m4a)
	        	//Log.d(TAG, "filname:" + filname + " comname:" + comname);
    			String filcheck = filname.substring(0,comlen); // make them the same length
    			if (comname.equalsIgnoreCase(filcheck)) {  
    				ref = rsCk.getInt(0);
    	        	//Log.d(TAG, "returning ref:" + ref + " comname:" + comname);
    	        	rsCk.close();
    				return ref; // the reference (if you get lucky)
    			}
    		}
    		rsCk.moveToNext();
    	}
    	rsCk.close();
    	Log.d(TAG, "returning ref:" + ref + " Unknown");
		return ref; 
    }

	// Main.isFilterExist == true to get to here. -- filter is in the database
	public void checkForFilter(String filName) { // filter loaded at the time the file is loaded.
		Log.d(TAG, "checkForFilter filName:" + filName);
		// I used to use just XC123456 in XcName -- now I have the whole file name in the field XcName
		qry = "SELECT XcName, FilterType, FilterVal FROM Filter WHERE XcName = " + q + filName + q;
		Cursor rsF = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		int cntr = rsF.getCount();
		if (cntr == 0) {
			rsF.close();
			return;  // no filter
		}
		rsF.moveToFirst();
		Log.d(TAG, "checkForFilter cntr:" + cntr);
		for (int f = 0; f < cntr; f++) {
			String typ = rsF.getString(1);
			if (typ.equals("LowFreqCutoff")) {
				filtLow = rsF.getInt(2);
			} else if (typ.equals("HighFreqCutoff")) {
				filtHi = rsF.getInt(2);
			} else if (typ.equals("FilterStart")) {
				filtBeg = rsF.getInt(2);
			} else if (typ.equals("FilterStop")) {
				filtEnd = rsF.getInt(2);
			} else if (typ.equals("Start")) {
				filtStrt = rsF.getInt(2);
			} else if (typ.equals("Stop")) {
				filtStop = rsF.getInt(2);
			}
			rsF.moveToNext();
		}
		rsF.close();
		Main.db.beginTransaction();
		qry = "DELETE FROM Filter WHERE XcName = " + q + filName + q;
		Main.db.execSQL(qry);
		Main.db.setTransactionSuccessful();
		Main.db.endTransaction();
		return;
	}

    public OnClickListener listener = new OnClickListener() {  // for non-list items -- i.e. buttons -- see SongAdaptor for click on list
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.delete_button:
                    Log.d(TAG, "Delete clicked");
                    deleteSelectedFile();  // I will now delete files regardless of path
                    break;
            } // switch
        } // onclick
    }; // onClickListener (because it does NOT extend OnClickListener)


    private void deleteSelectedFile() {
		if (Main.songCounter > 0) {
			Main.alertRequest = 2; // delete selected files
			Intent a3b = new Intent(this, Alert3ButtonDialog.class);
			startActivityForResult(a3b, Main.alertRequest);  // request == 2 == delete selected files
		} else {
			Toast.makeText(this, "Nothing selected to delete.", Toast.LENGTH_LONG).show();
		}
    } // deleteSelectedFile


    private void deleteOk(int id) {
        char q = 34;
        Log.d(TAG, "deleteOk option:" + id);
        if (id == 2) { // 2 = no - cancel
            Toast.makeText(this, "Delete Canceled", Toast.LENGTH_LONG).show();
            return; // cancel
        }

        Cursor rsDel = null;
        for (int i = 0; i < Main.songsDbLen; i++) {
            if (Main.ck[i] == true) {
				Main.listOffset = i;
				Main.existingName = Main.songs[i];
                Main.existingRef = Main.ref[i];
                Main.existingInx = Main.inx[i];
                Main.existingSeg = Main.seg[i];
                qry = "SELECT count(*) FROM SongList" +
                        " WHERE FileName = " + q + Main.existingName + q +
						" AND Ref = " + Main.existingRef +
                        " AND Inx = " + Main.existingInx +
                        " AND Seg = " + Main.existingSeg;
                Log.d(TAG, "count qry:" + qry);
                rsDel = Main.songdata.getReadableDatabase().rawQuery(qry, null);
                rsDel.moveToFirst();
                int cntr = rsDel.getInt(0);
                Log.d(TAG, "deleteSelectedFile count potential files:" + cntr);
                rsDel.close();
                try {
                    Log.d(TAG, "db begin transaction");
                    Main.db.beginTransaction();
                    try {   // 0= file and definition 1 = definition only
						if (id == 0) {
							qry = "DELETE FROM SongList" +
									" WHERE FileName = " + q + Main.existingName + q +
									" AND Inx = " + Main.existingInx +
									" AND Seg = " + Main.existingSeg +
									" AND Path = " + Main.path;
							Main.db.execSQL(qry);
						}
						if (id == 0 || id == 1) {
                            Log.d(TAG, "delete definition with ref:" + Main.existingRef + " inx:" + Main.existingInx + " seg:" + Main.existingSeg);
                            qry = "DELETE FROM DefineTotals" +
                                    " WHERE Ref = " + Main.existingRef +
                                    " AND Inx = " + Main.existingInx +
                                    " AND Seg = " + Main.existingSeg;
                            Main.db.execSQL(qry);
                            qry = "DELETE FROM DefineDetail" +
                                    " WHERE Ref = " + Main.existingRef +
                                    " AND Inx = " + Main.existingInx +
                                    " AND Seg = " + Main.existingSeg;
                            Main.db.execSQL(qry);
						}
                        if (id == 1) {
							qry = "UPDATE SongList " +
									" SET Defined = 0" +
									", Identified = 0" +
									", AutoFilter = 0" +
									", Enhanced = 0" +
									", Smoothing = 0" +
									" WHERE FileName = " + q + Main.existingName + q +
									" AND Path = " + Main.path +
									" AND Ref = " + Main.existingRef +
									" AND Inx = " + Main.existingInx +
									" AND Seg = " + Main.existingSeg;
							Log.d(TAG, "qry:" + qry);
							Main.db.execSQL(qry);
                        }
                    } finally {
                        Main.db.setTransactionSuccessful();
                        Main.db.endTransaction();
                        Log.d(TAG, "db end transaction");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Database Exception: " + e.toString());
				}

                boolean deleted = false;
                if (id == 0) { // 0 = file and definition
					Log.d(TAG, "is it a real file? existingSeg:" + Main.existingSeg);
                    if (Main.existingSeg == 0) {
						try {
							Log.d(TAG, "deleteFile name:" + Main.songpath + Main.existingName);
							File file = new File(Main.songpath + Main.existingName);
							if (file.exists()) { // it will not exist if it was only in the list.
								deleted = file.delete();
								Log.d(TAG, "deleteFile was file deleted?" + deleted);
							}
						} catch (Exception e) {
							Log.d(TAG, "exception e:" + e);
						}
                    }
                }
            }
        } // next i
        Main.fileReshowExisting = true;
		Main.isNewStartStop = false; // finish was taking me to play
        //db.close();
		Log.d(TAG, "finish() back to main resume fileReshowExisting:" + Main.fileReshowExisting);
		finish(); // should resume and back here to songlist -- error "rsrc of package null"
	} // deleteOk

	void showMetaData() throws IOException {
		if (Main.existingName == null) {
			Toast.makeText(this, "Please select a song first.", Toast.LENGTH_LONG).show();
			return;
		}
		Main.metaData = "";
		String filePath = Main.songpath + Main.existingName;
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(filePath);

		if (filePath.indexOf(".mp3") > 0 || filePath.indexOf(".ogg") > 0 || filePath.indexOf(".wav") > 0) {
			// [4 bytes atom name][4 bytes len to next atom name][data]								[4 bytes atom name]
			// 		TIT2			 	0x24 				 	Cedar Waxwing (Bombycilla cedrorum)	TCON
			metaBuffer = new byte[1024];
			File selected = new File(Main.songpath + Main.existingName);
			Long startAt = 0L;
			readFile(selected, startAt);
			char[] hexArray = "0123456789ABCDEF".toCharArray();
			char[] hexChars = new char[metaBuffer.length * 2];
			StringBuilder metaHex = new StringBuilder();
			for ( int j = 0; j < metaBuffer.length; j++ ) {
				int v = metaBuffer[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
				metaHex.append(hexChars[j*2]);
				metaHex.append(hexChars[j * 2 + 1]);
				//Log.d(TAG, "mp3 hexChars at:" + j + " " + hexChars[j*2] + hexChars[j*2+1]);
			}
			Log.d(TAG, "read hexChars:" + metaHex);
			StringBuilder meta = new StringBuilder();
			for (int i = 0; i < metaHex.length(); i+=2) {
				String str = metaHex.substring(i, i+2);
				meta.append((char)Integer.parseInt(str, 16));
			}
			int i = meta.indexOf("TALB"); // Album
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i*2+8, i*2+16),16);
				Main.metaData += "ALBUM:" + meta.substring(i + 11, i + len + 10) + "\n";
			} else {
				Main.metaData += "ALBUM:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) + "\n"; // 13
			}
			i = meta.indexOf("TIT2"); // Key_Title
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i * 2 + 8, i * 2 + 16), 16); // the 16 on the end is radix so it knows it is hex
				//Log.d(TAG, "mp3 TIT2 at:" + i + " len:" + len);
				Main.metaData += "TITLE:" + meta.substring(i + 11, i + len + 10) + "\n";
				//Log.d(TAG, "mp3 Main.metaData:" + Main.metaData);
			} else {
				Main.metaData += "TITLE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + "\n";
			}
			i = meta.indexOf("TCON"); // Family
			Log.d(TAG, "mp3 TCON at:" + i);
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i*2+8, i*2+16),16);
				//Log.d(TAG, "mp3 TCON at:" + i + " len:" + len);
				Main.metaData += "FAMILY:" + meta.substring(i + 11, i + len + 10) + "\n";
				//Log.d(TAG, "mp3 Main.metaData:" + Main.metaData);
			}
			i = meta.indexOf("TPE1"); // Artist
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i*2+8, i*2+16),16);
				Main.metaData += "ARTIST:" + meta.substring(i + 11, i + len + 10) + "\n";
			}
			i = meta.indexOf("COMM"); // Comment
			Log.d(TAG, "mp3 COMM at:" + i);
			if (i > 0) {
				int len = meta.indexOf("TCOP"); // next one the comment length is way long
				Log.d(TAG, "mp3 TCOP at:" + len);
				Main.metaData += "COMMENT:" + meta.substring(i + 11, len) + "\n";
			}
			Main.metaData += "DURATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) + "\n"; // 9
			Main.metaData += "MIMETYPE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) + "\n"; //12

			//Main.metaData += "xKEY_TITLE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + "\n";
			//Main.metaData += "NUM_TRACKS:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS) + "\n"; // 10
			//Main.metaData += "xARTIST:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) + "\n"; // 13
			//Main.metaData += "xAUTHOR:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR) + "\n";  // 3
			//Main.metaData += "xFRAMERATE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE) + "\n"; // 25
			//Main.metaData += "TRACK_NUMBER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) + "\n"; // 0
			//Main.metaData += "xCOMPILATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION) + "\n"; // 15
			//Main.metaData += "xCOMPOSER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER) + "\n"; // 4
			//Main.metaData += "xDATE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE) + "\n"; // 5
			//Main.metaData += "DISK_NUMBER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER) + "\n"; // 14
			//Main.metaData += "HAS_AUDIO:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) + "\n"; // 16
			//Main.metaData += "HAS_VIDEO:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) + "\n"; // 17
			//Main.metaData += "xLOCATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION) + "\n"; // 23
			//Main.metaData += "VIDEO_HEIGHT:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) + "\n"; // 19
			//Main.metaData += "VIDEO_ROTATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) + "\n"; // 24
			//Main.metaData += "VIDEO_WIDTH:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) + "\n"; // 18
			//Main.metaData += "xWRITER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER) + "\n"; // 11
			//Main.metaData += "xYEAR:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) + "\n"; // 8
			Log.d(TAG, "mp3 metadata:" + Main.metaData);

		}
			// [4 bytes atom name][4 bytes len to next atom name][data]								[4 bytes atom name]
			// 		TIT2			 	0x24 				 	Cedar Waxwing (Bombycilla cedrorum)	TCON
		if (filePath.indexOf(".m4a") > 0) {
			// [4 bytes atom length] [4 bytes atom name] [4 bytes len] [4 bytes "data"] [4 len of data] [data] [contents of the atom, if any]
			// 3B,"©nam",33,"data",1,0,"American ...." (length 33-12)
			metaBuffer = new byte[1024];
			File selected = new File(Main.songpath + Main.existingName);
			Long startAt = selected.length()-1024;
			readFile(selected, startAt);
			char[] hexArray = "0123456789ABCDEF".toCharArray();
			char[] hexChars = new char[metaBuffer.length * 2];
			StringBuilder metaHex = new StringBuilder();
			for ( int j = 0; j < metaBuffer.length; j++ ) {
				int v = metaBuffer[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
				metaHex.append(hexChars[j*2]);
				metaHex.append(hexChars[j * 2 + 1]);
					//Log.d(TAG, "m4a hexChars at:" + j + " " + hexChars[j*2] + hexChars[j*2+1]);
			}
			//Log.d(TAG, "read hexChars:" + metaHex);
			StringBuilder meta = new StringBuilder();
			for (int i = 0; i < metaHex.length(); i+=2) {
				String str = metaHex.substring(i, i+2);
				meta.append((char)Integer.parseInt(str, 16));
			}
			Log.d(TAG, "m4a metaHex.length():" + metaHex.length());
			Log.d(TAG, "m4a meta.length():" + meta.length());
			int i = meta.indexOf("meta");
			Log.d(TAG, "m4a meta at:" + i);
			i = meta.indexOf("©alb");
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i * 2 + 10, i * 2 + 16),16);
				Log.d(TAG, "m4a ©alb at:" + i + " len:" + len);
				Main.metaData += "ALBUM:" + meta.substring(i + 12, i + len + 4) + "\n";
			} else {
				Main.metaData += "ALBUM:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) + "\n"; // 13
			}
			i = meta.indexOf("©nam");
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i * 2 + 10, i * 2 + 16),16);
				Log.d(TAG, "m4a ©nam at:" + i + " len:" + len);
				Main.metaData += "TITLE:" + meta.substring(i + 12, i + len + 4) + "\n";
			} else {
				String nam = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
				if (nam != "") {
					Main.metaData += "TITLE:" + nam + "\n";
				}
			}
			i = meta.indexOf("©gen");
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i * 2 + 10, i * 2 + 16), 16);
				Log.d(TAG, "m4a ©gen at:" + i + " len:" + len);
				Main.metaData += "FAMILY:" + meta.substring(i + 12, i + len + 4) + "\n";
			}
			i = meta.indexOf("©cmt");
			if (i > 0) {
				int len = Integer.parseInt(metaHex.substring(i * 2 + 10, i * 2 + 16),16);
				Log.d(TAG, "m4a ©cmt at:" + i + " len:" + len);
				Main.metaData += "COMMENT:" + meta.substring(i + 12, i + len + 4) + "\n";
			}
			Main.metaData += "DURATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) + "\n";
			Main.metaData += "MIMETYPE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) + "\n";

			//Main.metaData += "xKEY_TITLE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) + "\n";
			//Main.metaData += "NUM_TRACKS:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS) + "\n"; // 10
			//Main.metaData += "xALBUM:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) + "\n"; // 13
			//Main.metaData += "xAUTHOR:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR) + "\n";  // 3
			//Main.metaData += "xFRAMERATE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE) + "\n"; // 25
			//Main.metaData += "TRACK_NUMBER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) + "\n"; // 0
			//Main.metaData += "xCOMPILATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION) + "\n"; // 15
			//Main.metaData += "xCOMPOSER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER) + "\n"; // 4
			//Main.metaData += "xDATE:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE) + "\n"; // 5
			//Main.metaData += "DISK_NUMBER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER) + "\n"; // 14
			//Main.metaData += "HAS_AUDIO:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) + "\n"; // 16
			//Main.metaData += "HAS_VIDEO:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO) + "\n"; // 17
			//Main.metaData += "xLOCATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION) + "\n"; // 23
			//Main.metaData += "VIDEO_HEIGHT:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) + "\n"; // 19
			//Main.metaData += "VIDEO_ROTATION:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) + "\n"; // 24
			//Main.metaData += "VIDEO_WIDTH:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) + "\n"; // 18
			//Main.metaData += "xWRITER:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER) + "\n"; // 11
			//Main.metaData += "xYEAR:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) + "\n"; // 8
			//Main.metaData += "NUM_TRACKS:" + mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS) + "\n";
			Log.d(TAG, "m4a metadata:" + Main.metaData);

		}

		if (Main.metaData == "") {
			Main.metaData = "Meta Data NOT available";
		}
		Main.metaData += " \n \n \n";
		Main.alertRequest = 5; // meta data info box
		Intent mdib = new Intent(this, Alert1ButtonDialog.class);
		startActivityForResult(mdib, Main.alertRequest);  // request == 5 == show meta data info box
	}

	public void readFile(File file, Long startAt) throws IOException {
		metaBuffer = new byte[1024];
		InputStream ios = null;
		try {
			ios = new FileInputStream(file);
			ios.skip(startAt);
			if (ios.read(metaBuffer) == -1) {
				throw new IOException(
						"EOF reached while trying to read the whole file");
			}
		} finally {
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
			}
		}
	}

	void emailFile() {
		Log.d(TAG, "emailFile");
		if (Main.existingName == null) {
			Toast.makeText(this, "Please select a song first.", Toast.LENGTH_LONG).show();
			return;
		}
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "", null));
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""}); // recipients
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, Main.existingName);
		emailIntent.putExtra(Intent.EXTRA_TEXT, Main.songsCombined[Main.listOffset] + "\n");
		File file = new File(Main.songpath + Main.existingName);
		Uri uri = Uri.fromFile(file);
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		startActivityForResult(emailIntent, 1);
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
		if (list != null) {
			list = null;
		}
    }

} // SongList

