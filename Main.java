package com.modelsw.loadingassetfiles;
/*
 * 1.A_V1 -- Start with Birding Via Mic and off line remove files and rename BirdingViaMic to LoadingAssetFiles
 * 2.A_V2 -- Remove CSV Filter in NW and OW -- Causes different .abb
 * 3.A_V3 -- Put CSV Filter back in NW
 *
 */
// CHECK build.gradle for the 4 items that need to be updated: 1)versionCode 2)versionName 3)versionName(in string) 4) DatabaseVersion (in Main)
// Where is android studio building my .apk file?
//  	 YourApplication\app\build\outputs\apk name: app-release.apk
// or -- YourApplication\app\release\app.aab
//  https://play.google.com/console/developers/5224623645443335130/app-list
// the following is how to load asset packs:
// https://codelabs.developers.google.com/codelabs/native-gamepad#0
/* -- got to be one of these to get asset packs to work
https://developer.android.com/guide/app-bundle/asset-delivery
https://developer.android.com/guide/app-bundle/asset-delivery/build-native-java <-- done
https://developer.android.com/guide/playcore/asset-delivery
https://developer.android.com/guide/playcore/asset-delivery/integrate-java <-- I will come back
https://developer.android.com/reference/com/google/android/play/core/assetpacks/AssetPackLocation#assetspath <-- I'm here
https://developer.android.com/guide/playcore#java-kotlin
https://developer.android.com/guide/playcore#import-library
https://codelabs.developers.google.com/codelabs/native-gamepad#3
https://developer.android.com/guide/playcore/play-feature-delivery
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import android.media.AudioManager;
import android.os.Environment;


public class Main extends AppCompatActivity implements OnClickListener {
	private static final String TAG = "Main";
	public static String adjustViewOption; // AdjustView: clear, move, save, exclude, cancel, edit -- used in playSong, VisualizerView
	public static int alertRequest = 0; // 2=delete files; 3=delete species; 4=delete web; 5=meta data info box; 6=database upgrade complete;
	public static String assetPackName;  //
	public static short[] audioData;  // the entire song -- -32767 +32767 (16 bit)
	public static int audioDataLength;  // the usable file length without overflows
	public static int audioDataSeek;  // in playSong and decodeFile
	public static int audioSource = -1; // stored in SongList -- 0=default, 1=mic, 5=camcorder, 6 voice recognition, -1=unknown
	public static int bitmapWidth; // set from VisualizerView used in AdjustView
	public static int bitmapHeight; // set from VisualizerView used in AdjustView
	public static int buttonHeight;  // fails to set in Main -- set in PlaySong used in VisualizerView
	public static Boolean[] ck;  // used in SongList -- song selected
	public static String codeNameFile;
	public static String commonName; // stored in species table CodeName
	public static String customPathLocation = null;
	public static String databaseName; // loadingassetfiles/Define/BirdSongs.db
	public static int databaseVersion = 83; // increment if change database -- calls SongData -- bad name databases is 92 (aka 9.2)
	public static SQLiteDatabase db;
	public static String definepath = null; // loadingassetfiles/Define
	public static File definePathDir; // file format
	public static String displayName = ""; // used in PlaySong identify to hold identification
	public static int duration; // song length in milliseconds
	public static String environment = null;   // /storage/sdcard0
	public static int existingInx;
	public static String existingLink;
	public static String existingName;
	public static int existingRef;
	public static String existingRedList;
	public static String existingRegion;
	public static int existingSeg;
	public static String existingSpec;
	public static String existingSpecName;
	public static String existingSubRegion;
	public static String existingWebName;
	public static Boolean fileRenamed = false; // used in restart to refresh the songlist
	public static Boolean fileReshowExisting = false; // used in restart to refresh the songlist
	public static int filterStartAtLoc = 0; // set in AdjustView -- used in PlaySong
	public static int filterStopAtLoc = 0; // set in AdjustView -- used in PlaySong
	public static int highFreqCutoff = 0;  // user entered from adjust view
	public static float hzPerStep = 11025f / 512f;  // 21.53 hz per step -- in the file as hz; 0-511 everywhere else.
	public static int[] inx;  // holds existingInx from songList
	public static boolean isAutoLocation = true; // ShowLocation use GPS vs manual
	public static boolean isCheckPermissions = false; // set true if you need to check permissions (Android 6.0+)
	public static boolean isDebug = false; // save extra files
	public static boolean isDecodeBackground = true; // manual=false / background=true
	public static boolean isEdit = false; // set true when edit button on play is tapped
	public static boolean isEnhanceQuality = true; // build s/n kernel and apply to normalized audio
	public static boolean isExternalMic = false; // set true when plugged in else false if internal mic
	public static boolean isFilterExists = false; // has a manual filter been set in AdjustView
	public static Boolean isIdentify = true;  // has PlaySong identify button been pushed
	public static boolean isLoadDefinition = false;  // (set in options) any checked in the list -- define if not mic -- identify if mic
	public static boolean isNewStartStop = false; // set in AdjustView for song segment
	public static boolean isOptionAutoFilter = true;  // find mean in Voiced overruled if manual filter
	public static boolean isPlaying = false; // is the song currently playing
	public static boolean isSampleRate = false; // option used for recording false=22050, true=44100
	public static boolean isSavePcmData = false; // save audioData output from DecodeFileJava
	public static boolean isShowDetail = false; // show the fft on the visualizerView screen
	public static boolean isShowDefinition = false;  // Show the definition (frequency, distance, energy, quality) on the VisualizerView screen
	public static boolean isSortByName = true; // songList sort by CommonName vs sort by species
	public static boolean isStartRecordScreen = false; // start the app with the record screen showing
	public static boolean isStartRecording = false; // start the app with the record screen showing and start recording
	public static boolean isShowWeb = false; // option if true attempt show xeno-canto or Wikipedia
	public static boolean isStereo = false; // vs mono
	public static boolean isUseAudioRecorder = false; // option true = wav / false media = m4a
	public static boolean isUseLocation = false; // option - limit list in identification
	public static boolean isUseSmoothing = false; // option - use smoothing
	public static boolean isViewDistance = true; // option - display distance in VisualizerView
	public static boolean isViewEnergy = true; // option - display energy in VisualizerView
	public static boolean isViewFrequency = true; // option - display frequency in VisualizerView
	public static boolean isViewQuality = true; // option - display quality in VisualizerView
	public static boolean isWebLink = false; // in WebList if true show web page else Wikipedia or xeno-canto
	public static int latitude = 40; // location can be auto or manual
	//public static float lengthEachRecord = 5.0f; // number of records * lengthEachRecord = size required for dimension of bitmap
	public static int listOffset = 0;
	public static List<String> listPermissionsNeeded;
	public static int longitude = -100; // location can be auto or manual
	public static int lowFreqCutoff = 0;  // user entered from adjust view
	public static int manualLat;
	public static int manualLng;
	public static float maxPower;  // max of fft power calc.
	public static int maxPowerJ = 0; // frequency where power is max (don't know if it is lo or hi for harmonics and percent peak)
	public static int maxPowerRec = 0; // record at which the max power occurred
	public static String metaData = null;
	public static int myRequest = 0;
	public static int myResult = 0;
	public static int myUpgrade = 0;
	public static String newLink;
	public static String newName;
	public static String newRedList;
	public static String newRegion;
	public static String newSubRegion;
	public static int newRef;
	public static String newSpec;
	public static String newSpecName;
	public static int newSpecRef;
	public static String newWebName;
	public static boolean optionsRead = false; // set true when read and in memory
	public static int path = 1;  // internal songs are default
	public static int permCntr = 6;
	public static String[] permissions = new String[]{
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.RECORD_AUDIO,
			Manifest.permission.INTERNET,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION };
	public static int phoneLat;
	public static int phoneLng;
	private Button playButton;
	public String qry = "";
	private Button recordButton;
	public static String recordedName;
	public static int[] ref;  // reference (to replace defineName)
	private Cursor rs; // I think of Cursor as Record Set
	private Cursor rsd; // record set Detail
	public static int sampleRate = 22050;
	public static int sampleRateOption = 0;
	public static int[] seg;
	public static int[] selectedSong;
	public static String sharedDefine;
	public static int shortCntr;
	public static Boolean showPlayFromList = false;
	public static Boolean showPlayFromRecord = false;
	public static Boolean showWebFromIdentify = false;
	private Button songButton;
	public static int songCounter = 0;  // count of songs selected (checked)
	public static String songpath = null;   // environment + /loadingassetfiles/Songs/ or environment + /iBird_Lite/ or custom
	public static String[] songs; // names from the file (to string)
	public static String[] songsCombined;  // the listing (fileName newLine Spec Inx Seg)
	public static SongData songdata = null;
	public static int songsDbLen; // count of songs in the SongList
	public static File[] songFile; // names from the file (file format)
	public static File songPathDir;
	public static int songStartAtLoc = 0;
	public static int songStopAtLoc = 0;
	public static int sourceMic = 0;
	public static int[] speciesRef;
	public static int specOffset = 0;
	public static Boolean specRenamed = false;
	public static int stereoFlag = 0; // used in sampleRateOption 0=mono / 1=stereo
	//public static int stopAt = 0;
	int targetSdkVersion;
	public static int thisSong = 0;  // current song
	//public static int totalCntr = 0;  // I have to keep this for mediaPlayer and visualizerView
	Toolbar toolbar;
	public static int userRefStart = 40000;
	private int versionNum = 0;
	private String refUpgradeFile = "";
	private Button webButton;
	public static int webOffset = 0;
	public static Boolean webRenamed = false;
	public static boolean wikipedia = true;  // true show identified bird - false bring up a different web site
	public static boolean xenocanto;
	Bundle savedInstanceState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// action bar toolbar
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setLogo(R.drawable.treble_clef_linen);
		toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.teal));
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "Navigation Icon tapped");
			}
		});

		findViewById(R.id.song_button).setOnClickListener(this);
		songButton = (Button) findViewById(R.id.song_button);
		findViewById(R.id.select_songpath_button).setOnClickListener(this);
		findViewById(R.id.help_button).setOnClickListener(this);
	}

	public void init() {
		Log.d(TAG, "**** App is through checking permissions. ****");
		environment = Environment.getExternalStorageDirectory().getAbsolutePath();
		songPathDir = getExternalFilesDir("Song"); // File
		definePathDir = getExternalFilesDir("Define"); // File
		definepath = definePathDir.toString() + "/"; // String
		databaseName = definepath + "BirdSongs.db";
		Log.d(TAG, "onCreate environment:" + environment + " songPathDir:" + songPathDir +
				" definePathDir:" + definePathDir + " databaseName:" + databaseName);
		// test in loadAssets and only load if missing
		Log.d(TAG, "make the Define directory");
		new File(definePathDir.toString()).mkdirs();
		Log.d(TAG, "go load the database");
		loadAssets("Define");

		Log.d(TAG, "make the Song directory");
		new File(songPathDir.toString()).mkdirs(); // doesn't do any harm if dir exists -- adds if missing
		Log.d(TAG, "go load the song files");
		loadAssets("Song");

		// I really have fixed the database leaking problem -- this is the ONLY NEW SongData in the WHOLE application
		songdata = new SongData(this, Main.databaseName, null, Main.databaseVersion);
		db = songdata.getWritableDatabase();
		// database loaded
		readTheSongPath();
		// I have disabled all but path = 1 here in main.
		songpath = Main.songPathDir.toString() + "/";
		Log.d(TAG, "onCreate definepath:" + definepath + " songpath:" + songpath);

		readTheOptions();
		commonName = "CommonName: ";

		// ****************************
		checkVersion();
		// ****************************
		readTheLocationFile();
		runPatch();

		if (isStartRecordScreen == true) {
			recordButton.performClick();
		}

	}

	private boolean checkPermissions() {
		try {
			final PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			targetSdkVersion = info.applicationInfo.targetSdkVersion;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "error name not found:" + e);
			return true; // problems with package -- don't get lost in permissions
		}
		// For Android < Android M, self permissions are always granted.
		Log.d(TAG, "targetSdkVersion:" + targetSdkVersion);
		Log.d(TAG, "Build.VERSION.SDK_INT:" + Build.VERSION.SDK_INT
				+ " Build.VERSION_CODES.M:" + Build.VERSION_CODES.M);
		if (targetSdkVersion < Build.VERSION_CODES.M) {
			return true;
		}
		listPermissionsNeeded = new ArrayList<String>();
		for (String p:permissions) {
			int result = ContextCompat.checkSelfPermission(this, p);
			Log.d(TAG, "result:" + result + " permission:" + p);
			if (result != PackageManager.PERMISSION_GRANTED && result != 0) {
				listPermissionsNeeded.add(p);
				Log.d(TAG, "missing permission:" + p + " result:" + result);
			}
		}
		if (listPermissionsNeeded.isEmpty()) { // either none added or failed to add
			return true;
		} else {
			return false; // list is not empty go get permissions
		}
	}

	public void onClick(View v) {
		int resId;
		switch (v.getId()) {
			case R.id.song_button: {
				buttonHeight = songButton.getHeight();
				Log.d(TAG, "onClick List buttonHeight:" + buttonHeight);
				wikipedia = true;
				xenocanto = false;
				isWebLink = false;
				existingRef = 0;
				Intent sl = new Intent(this, SongList.class);
				startActivity(sl);
				break;
			}
			case R.id.select_songpath_button: {
				Log.d(TAG, "onClick SongPath");
				Intent sl = new Intent(this, SelectSongPath.class);
				startActivity(sl);
				break;
			}
			case R.id.help_button: {
				Log.d(TAG, "onClick Help");
				Intent h = new Intent(this, HelpActivity.class);
				startActivity(h);
				break;
			}

		} // switch
	} // onClick

	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		//db.close();

	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG, "onResume check songpath or songdata");
		if (songpath == null || songdata == null) {
			Log.d(TAG, "** onResume songpath or songdata is null");
			init();
		}

		Log.d(TAG, "onResume newStartStop:" + isNewStartStop);
		if (isNewStartStop == true) {
			isNewStartStop = false;
			//Intent nss = new Intent(this, PlaySong.class);
			//startActivity(nss);
			playButton.performClick();
		}

		// keep this next to last -- if nothing else runs and return from play song then go back to the list where you were
		// I had to move it above web -- because web quit working -- web has to be last.
		// fileReshowExisting flag also used on deleted files
		Log.d(TAG, "onResume fileReshowExisting:" + fileReshowExisting);
		if (fileReshowExisting == true) {
			//fileReshowExisting = false;  will be cleared to false later in SongList
			Intent rn = new Intent(this, SongList.class);
			startActivity(rn);
		}

	}

	// fix what I can in local database
	void runPatch() {
	}

	public void readTheOptions() {
		Log.d(TAG, "readTheOptions");
		optionsRead = true;
	}

	public void loadAssets(String folder) {
		AssetManager assetManager = getAssets();
		String[] inFile = null;
		try {
			inFile = assetManager.list(folder);
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		File outFile = null;
		InputStream in = null;
		OutputStream out = null;
		int inFileLen = 0;

		if (inFile == null) {
			Log.d(TAG, "loadAsset inFile is null -- returning");
			return;
		}
		inFileLen = inFile.length; // number of files
		if (inFileLen == 0) {
			Log.d(TAG, "loadAsset inFileLen == 0 -- returning" );
			return;
		}
		Log.d(TAG, "loadAsset inFileLen:" + inFileLen);
		for (int i = 0; i < inFileLen; i++) {
			tryNext:
			try {
				if (folder.equals("Define")) {
					in = assetManager.open("Define/" + inFile[i]); // in from assets
					outFile = new File(definePathDir + "/" + inFile[i]);  // out to definePathDir
					if (outFile.exists()) { // if the database or any file is there already don't load an empty one and trash the users files
						Log.d(TAG, "loadAssets outFile exists -- not loading:" + outFile);
						break tryNext;
					}
				}
				if (folder.equals("Song")) {
					in = assetManager.open("Song/" + inFile[i]);
					outFile = new File(songPathDir, inFile[i]);
					if (outFile.exists()) {
						Log.d(TAG, "loadAssets outFile exists -- not loading:" + outFile);
						break tryNext;
					}
				}
				Log.d(TAG, "loadAssets in:" + in);
				Log.d(TAG, "loadAssets outFile:" + outFile);
				out = new FileOutputStream(outFile);
				copyFile(in, out);

			} catch (IOException e) {
				Log.e(TAG, "Failed to copy asset file: " + inFile[i], e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						Log.e(TAG, "Failed in.close() error:" + e);
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						Log.e(TAG, "Failed out.close() error:" + e);
					}
				}
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	public void readTheSongPath() {
		// fix
		//qry = "Update SongPath set CustomPath = ''";
		//Main.db.execSQL(qry);
		// end fix
		Log.d(TAG, "readTheSongPath");
		qry = "SELECT Path, CustomPath FROM SongPath";
		rs = songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		//Main.path = rs.getInt(0);
		path = 1;  // always point to internal in main
		Main.customPathLocation = rs.getString(1);
		Log.d(TAG, " * * readTheSongPath path:" + path + " customPathLocation:" + customPathLocation);
	}

	public void readTheLocationFile() { // not file anymore; it is in database
		Log.d(TAG, "read the Location file");

		qry = "SELECT Value FROM Location WHERE Name = 'AutoLocation'";
		rs = songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		isAutoLocation = (rs.getInt(0) != 0);
		qry = "SELECT Value FROM Location WHERE Name = 'PhoneLat'";
		rs = songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		phoneLat = rs.getInt(0);
		qry = "SELECT Value FROM Location WHERE Name = 'PhoneLng'";
		rs = songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		phoneLng = rs.getInt(0);
		qry = "SELECT Value FROM Location WHERE Name = 'ManualLat'";
		rs = songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		manualLat = rs.getInt(0);
		qry = "SELECT Value FROM Location WHERE Name = 'ManualLng'";
		rs = songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		manualLng = rs.getInt(0);
		rs.close();

		if (Main.isAutoLocation == true) {
			Main.latitude = phoneLat;
			Main.longitude = phoneLng;
		} else {
			Main.latitude = manualLat;
			Main.longitude = manualLng;
		}
	}

	// currently NOT called
	private void rebuildNameList() {
		// this is a one time event -- change file name from existing to species common name plus existing numbers and extension
		Log.d(TAG, "rebuildNameList");
		songpath = songPathDir + "/";
		int pathLen = songpath.length();
		File dir = new File(songpath);
		Log.d(TAG, "onCreate: dir:" + dir);
		Main.songFile = dir.listFiles();
		if (Main.songFile == null) {
			Log.d(TAG, "rebuildNameList songFile is null -- closing");
			String msg = "invalid path:" + songpath;
			Log.d(TAG, msg);
			//finish();
		} else {
			int songsFileLen = Main.songFile.length;
			Main.songs = new String[songsFileLen];
			char q = 34; // double quote to avoid crash on single quote
			String nums = "0123456789";
			for (int i = 0; i < songsFileLen; i++) {
				songs[i] = songFile[i].toString().substring(pathLen);
				String oldName = songs[i];
				if (!oldName.contains("@_") || !oldName.contains("XC")) { // don't mess with unknowns and Xeno-Canto files
					qry = "SELECT FileName, CodeName.CommonName, CodeName.Ref, Inx, Seg " +
							" FROM SongList JOIN CodeName ON SongList.Ref = CodeName.Ref" +
							" WHERE path = " + path +
							" AND FileName = " + q + oldName + q;

					Cursor rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
					rs.moveToFirst();
					int cntr = rs.getCount();
					if (cntr == 1) { // don't solve the world -- work on the ones with possible success
						existingRef = rs.getInt(2);
						if (existingRef > 0) { // don't change the name or it will become "Unknown"
							existingInx = rs.getInt(3);
							existingSeg = rs.getInt(4);
							String thisInx = "";
							String nam = rs.getString(0); // existing file name
							String comnam = rs.getString(1); // // species common name
							thisInx = "";
							int lennam = nam.length() - 4; // remove the dot and extension
							String ext = nam.substring(lennam); // .m4a
							nam = nam.substring(0, lennam);  // fileName5V30
							lennam = nam.length(); //
							String ch = "";
							if (lennam > 0) {
								for (int j = 0; j < lennam; j++) { // look for a number
									ch = nam.substring(j, j + 1);
									//	Log.d(TAG, "ch:" + ch);
									if (nums.contains(ch)) { // i found a number
										thisInx = nam.substring(j);
										break;
									}
								}
							}
							newName = comnam + thisInx + ext;  //
							songFile[i].renameTo(new File(Main.songpath + newName));
							Log.d(TAG, "oldName:" + oldName + " newName:" + newName);
						} // don't do Unknowns
					} // cntr == 1
					rs.close();

					Main.db.beginTransaction();
					qry = "UPDATE SongList" +
							" SET FileName = " + q + newName + q +
							" WHERE FileName = " + q + oldName + q +
							" AND Path = " + path +
							" AND Ref = " + existingRef +
							" AND Inx = " + existingInx +
							" AND Seg = " + existingSeg;
					//Log.d(TAG, "Song Identity With @ Update SongList qry:" + qry);
					Main.db.execSQL(qry);
					Main.db.setTransactionSuccessful();
					Main.db.endTransaction();
				} // skip the @_
			} // next i
		} // song file null test

	} // rebuildNameList()


	void checkVersion() { // ONLY RUN THIS ONCE !!!
		// this is a one time event -- it will run if it finds RefUpgrade92.csv AND CodeName92.csv
		// it deletes those two files on completion
		// change Ref to new version In SongList, DefineTotals, DefineDetail
		// replace CodeName with the new data
		Log.d(TAG, "CheckVersion");
		// upgrade from version 6.1 to 9.2 -- WITHOUT Loosing the existing songlist or defines.
		// the table RefUpgrade contains the Species Reference number for the existing version 6.1 and the new version 9.2
		// It has to be the full file because names and species changed
		// i need a dialog to upgrade -- only ask if upgrade files exist -- yes, later
		// no files return -- else ask for upgrade
		// yes - do it now
		// later - return here the next startup
		// I have to load the files out of Assets Define into definepath before I get here
		// Always during transfer from Assets I delete the files first in Define then if they exist in Assets I transfer them
		// I retain an empty.csv file if they have been loaded so I don't transfer again. -- NO I don't think I empty a file out !!
		// they won't be in the next version -- just the new database which won't be loaded if exists -- true
		// And I delete before I transfer so no files
		qry = "SELECT Num from Version";
		rs = Main.songdata.getReadableDatabase().rawQuery(qry, null);
		rs.moveToFirst();
		versionNum = rs.getInt(0);  // this is the old version 61 the first time you check and 92 the next time you check
		rs.close();
		Log.d(TAG, "checkVersion versionNum " + versionNum);
        int upgradeCntr = 0;
		Scanner refup = null;
		try {
			// see if it is already loaded
			File dir = new File(definepath);
			File[] files = dir.listFiles();
            String refUpgradeFileName = "RefUpgrade" + versionNum + ".csv";
            String codeNameFileName =  "CodeName" + versionNum + ".csv";
            int checkVersionNum1 = 0;
            int checkVersionNum2 = 0;
            for (File inFile : files) {
                if (inFile.getName().equals(refUpgradeFileName)) {
                    boolean deleted = inFile.delete();
                    Log.d(TAG, "checkVersion version matches -- deleting " + refUpgradeFileName + " " + deleted);
                }
                if (inFile.getName().equals(codeNameFileName)) {
                    boolean deleted = inFile.delete();
                    Log.d(TAG, "checkVersion version matches -- deleting " + codeNameFileName + " " + deleted);
                }
                String nam1 = inFile.getName().substring(0, 10);
                if (nam1.equals("RefUpgrade")) {
                    int dot = inFile.getName().indexOf(".csv");
                    String refUpVer = inFile.getName().substring(10, dot);
                    checkVersionNum1 = Integer.parseInt(refUpVer);
                    if (checkVersionNum1 > versionNum) {
                        Log.d(TAG, "checkVersion RefUpgrade is new file version:" + checkVersionNum1);
                        upgradeCntr++;
						refUpgradeFile = inFile.getName();
                    }
                }
				String nam2 = inFile.getName().substring(0, 8);
                if (nam2.equals("CodeName")) {
                    int dot = inFile.getName().indexOf(".csv");
                    String codeVer = inFile.getName().substring(8, dot);
                    checkVersionNum2 = Integer.parseInt(codeVer);
					if (checkVersionNum2 > versionNum) {
						Log.d(TAG, "checkVersion CodeName is new file version:" + checkVersionNum2);
						upgradeCntr++;
						codeNameFile = inFile.getName();
					}
                }
            } // next
		} catch (Exception e) {
			// the files don't exist leave quitely.
			Log.d(TAG, "catch checkVersion -- refUpgradeFileXX does NOT Exist:" + e);
			return;
		}
	} // checkVersion


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult requestCode:" + Main.myRequest + " resultCode:" + Main.myUpgrade);
		init();
	}



}