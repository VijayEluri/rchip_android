package com.nosideracing.msremote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MS_database extends SQLiteOpenHelper {

    MS_database(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String DATABASE_NAME = "msdb.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME_SL = "ms_show_list";

    private SQLiteDatabase db;

    public void deleteOneSL(int id) {
	Log.i(MS_constants.LOG_TAG, "Deleting all row " + id + " from " + TABLE_NAME_SL);
	db = this.getWritableDatabase();
	db.execSQL("Delete from " + TABLE_NAME_SL + " where id = " + id);
	db.close();
    }

    public void deleteAllSL() {
	Log.i(MS_constants.LOG_TAG, "Deleting all rows from " + TABLE_NAME_SL);
	db = this.getWritableDatabase();
	db.execSQL("Delete from " + TABLE_NAME_SL);
	db.close();
    }

    public String[] getShows() {
	
	int index = 0;
	db = this.getReadableDatabase();
	Cursor C = db.query(TABLE_NAME_SL, new String[] { "id", "ShowName", "EpisodeNumber",
		"EpisodeName", "Location" }, null, null, null, null,"ShowName,EpisodeNUmber");
	Log.v(MS_constants.LOG_TAG, "Got " + C.getCount() + " Rows from table " + TABLE_NAME_SL);
	String[] retval = new String[C.getCount()];
	if (C.moveToFirst()) {
	    do {
		retval[index] = C.getString(0) + "|" + C.getString(1) + "|" + C.getString(2) + "|"
			+ C.getString(3) + "|" + C.getString(4);
		index++;
	    } while (C.moveToNext());
	}
	if (C != null && !C.isClosed()) {
	    C.close();
	}
	db.close();
	return retval;
    }

    public void insertShow(String ShowName, String EpsName, String EpsNumber, String LOC) {
	db = this.getWritableDatabase();
	ContentValues values = new ContentValues();
	values.put("ShowName", ShowName.replace("_"," "));
	values.put("EpisodeNumber", EpsNumber);
	values.put("EpisodeName", EpsName);
	values.put("Location", LOC);
	long rows = db.insert(TABLE_NAME_SL, null, values);
	if (rows < 1) {
	    Log.e(MS_constants.LOG_TAG, "Couldn't insert into database");
	} else {
	    Log.v(MS_constants.LOG_TAG, "Inserted " + rows + " into table " + TABLE_NAME_SL);
	}
    }

    /* (non-Javadoc)
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    public void onCreate(SQLiteDatabase db) {
	db.execSQL("CREATE TABLE `"+TABLE_NAME_SL+"` ( `ID` INTEGER PRIMARY KEY AUTOINCREMENT,"
		+ "  `ShowName` VARCHAR( 64 ) NOT NULL , `EpisodeNumber` VARCHAR( 10 ) NOT NULL ,  "
		+ "`EpisodeName` VARCHAR( 128 ) NOT NULL , `Location` VARCHAR( 1024 ) NOT NULL ,  "
		+ "`Updated` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP);");
		Log.d(MS_constants.LOG_TAG,"MS_database: Got to On Create");
	        
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	Log.d(MS_constants.LOG_TAG,"MS_database: Got to On Upgrade");
	// put new create tables, or changes here
    }
}