package com.modelsw.loadingassetfiles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class SongData extends SQLiteOpenHelper {
    private static final String TAG = "SongData";


	public SongData(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate table and (maybe) insert records");
	    // disable the following line if you upgrade database then re-enable
	//	if (db != null) { // i was getting null pointer
	//		Main.db.enableWriteAheadLogging();   // re-add trying to find SQLiteDiskIOException: disk I/O error (code 1802)
	//	}

	/*

		qry = "CREATE TABLE CodeName (Ref INTEGER PRIMARY KEY, Spec TEXT," + 
				" CommonName TEXT, Region TEXT, SubRegion TEXT, InArea INTEGER," + 
				" MinX INTEGER, MinY INTEGER, MaxX INTEGER, MaxY INTEGER)";


		qry = "CREATE TABLE SongList (Ref INTEGER, Inx INTEGER, Seg INTEGER," + 
				" Path INTEGER, FileName TEXT, Start INTEGER, Stop INTEGER," + 
				" Identified INTEGER, Defined INTEGER, AutoFilter INTEGER, Enhanced INTEGER," +
				" Smoothing INTEGER, SourceMic INTEGER, SampleRate INTEGER, AudioSource INTEGER," +
				" LowFreqCutoff INTEGER, HighFreqCutoff INTEGER," +
				" FilterStart INTEGER, FilterStop INTEGER," +
				" PRIMARY KEY (Ref, Inx, Seg, Path, FileName) )";
				

		qry = "CREATE TABLE DefineTotals (Ref INTEGER, Inx INTEGER," + 
				" Seg INTEGER, Phrase INTEGER, Silence INTEGER, Records INTEGER," +
				" FreqMean FLOAT, FreqStdDev FLOAT, VoicedMean FLOAT, VoicedStdDev FLOAT, EnergyMean FLOAT, EnergyStdDev FLOAT," +
				" DistMean FLOAT, DistStdDev FLOAT, QualityMean FLOAT, QualityStdDev FLOAT, SampMean FLOAT, SampStdDev FLOAT," +
				" CoefA FLOAT, Slope FLOAT, CoefC FLOAT, CorrCoef FLOAT," +
				" PRIMARY KEY (Ref, Inx, Seg, Phrase))";

		qry = "CREATE TABLE DefineDetail (Ref INTEGER, Inx INTEGER," + 
				" Seg INTEGER, Phrase INTEGER, Record INTEGER," +
				" Freq INTEGER, Voiced INTEGER, Energy INTEGER, Distance INTEGER," +
				" Quality INTEGER, Samp INTEGER," +
				" PRIMARY KEY (Ref, Inx, Seg, Phrase, Record))";


		qry = "CREATE TABLE TopFrequency (Ref INTEGER, Inx INTEGER," + 
				" Seg INTEGER, PhraseCntr INTEGER, InxCntr INTEGER," + 
				" Rank INTEGER, jFreq INTEGER, Power FLOAT," + 
				" PRIMARY KEY (Ref, Inx, Seg, PhraseCntr, InxCntr, Rank))";

		qry = "CREATE TABLE Identify (Ref INTEGER, Cntr INTEGER, Criteria TEXT)";
		
		qry = "CREATE TABLE FreqRamp (RFreq INTEGER, Rpwr FLOAT)";

		qry = "CREATE TABLE FreqMic (MFreq INTEGER, Mpwr FLOAT)";		

		Log.d(TAG, "qry: " + qry  );
		db.execSQL(qry);

		qry = "CREATE TABLE VoicedWav (Freq INTEGER, Gain FLOAT, PRIMARY KEY (Freq))";

		qry = "CREATE TABLE VoicedM4a (Freq INTEGER, Gain FLOAT, PRIMARY KEY (Freq))";


*/

	//	Log.d(TAG, "qry: " + qry  );
	//	db.execSQL(qry);

	}

	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade");
		// I will only get here if databaseVersion is incremented

//		ContentValues val = new ContentValues();
//		val.put("Name", "SavePcmData");
//		val.put("Value", 0);
//		db.insert("Options", null, val);


//		qry = "DROP TABLE IF EXISTS SongList";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS DefineTotals";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS DefineDetail";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS DefineFilter";
//		db.execSQL(qry);
//		qry = "CREATE TABLE Identify (DefineName TEXT, Cntr INTEGER, Criteria TEXT)";		
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS SongPath";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS TopFrequency";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS FreqRamp";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS FreqMic";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS VoicedM4a";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS VoicedWav";
//		db.execSQL(qry);
//		qry = "DROP TABLE IF EXISTS Filter";
//		db.execSQL(qry);
//		qry = "CREATE TABLE Filter (XcName TEXT, FilterType TEXT, FilterVal INTEGER, PRIMARY KEY (XcName,FilterType))";
//		db.execSQL(qry);

		//onCreate(db); // now that you have deleted a table -- call the above function to re-create it.

	}

}
