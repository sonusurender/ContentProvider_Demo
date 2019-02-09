package com.contentprovider_demo;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class Custom_ContentProvider extends ContentProvider {
	static final String PROVIDER_NAME = "com.contentprovider_demo.Custom_ContentProvider";// Provider
																							// name
	static final String URL = "content://" + PROVIDER_NAME + "/androhub";// Provider
																			// url
	static final Uri CONTENT_URI = Uri.parse(URL);// Contnet Uri in URI format

	// All fields of database
	static final String id = "id";
	static final String name = "name";
	static final String email = "email";
	static final String number = "number";

	// Uri code
	static final int uriCode = 1;
	static final UriMatcher uriMatcher;
	private static HashMap<String, String> values;
	static {

		// Match the uri code to provider name
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "androhub", uriCode);
		uriMatcher.addURI(PROVIDER_NAME, "androhub/*", uriCode);
	}

	// detele uri method
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;// Count to tell how many rows deleted
		switch (uriMatcher.match(uri)) {
		case uriCode:
			count = db.delete(TABLE_NAME, selection, selectionArgs);
			break;
		default:
			count = 0;
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	// return type of content provider
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case uriCode:
			return "vnd.android.cursor.dir/androhub";

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	// Insert method
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = db.insert(TABLE_NAME, "", values);// Insert data into
														// database
		// If row id is greater than 0 then notify content provider
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to add a record into " + uri);
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		DatabaseHelper dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
		if (db != null) {
			return true;
		}
		return false;
	}

	// Read all data method
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_NAME);

		switch (uriMatcher.match(uri)) {
		case uriCode:
			qb.setProjectionMap(values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		if (sortOrder == null || sortOrder == "") {
			sortOrder = name;
		}
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	// Update data
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;// Count to tell number of rows updated
		switch (uriMatcher.match(uri)) {
		case uriCode:
			count = db.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		default:
			count = 0;
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	/** SqlLite Database **/
	private SQLiteDatabase db;
	static final String DATABASE_NAME = "ContentProvider_Database";// Database
																	// name
	static final String TABLE_NAME = "User_Details";// Table Name
	static final int DATABASE_VERSION = 1;// Database Version
	static final String CREATE_DB_TABLE = " CREATE TABLE " + TABLE_NAME
			+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + name
			+ " TEXT NOT NULL, " + email + " TEXT NOT NULL, " + number
			+ " TEXT NOT NULL" + " );"; // Create table query

	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_DB_TABLE);// Create table
		}

		// On database version upgrade create new table
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
}
