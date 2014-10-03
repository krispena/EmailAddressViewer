package com.krispena.emailaddressviewer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class EmailProvider extends ContentProvider {


    public static final String AUTHORITY = "com.krispena.emailaddressviewer";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Email.EMAIL_TABLE_NAME);
    public static final String EMAIL_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.krispena.emails";
    private static final String DATABASE_NAME = "email.db";
    private static final int VERSION = 1;
    private static final int QUERY_ALL_EMAIL_CODE = 1;
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());

        matcher.addURI(AUTHORITY, Email.EMAIL_TABLE_NAME, QUERY_ALL_EMAIL_CODE);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int id = matcher.match(uri);
        if (id == QUERY_ALL_EMAIL_CODE) {
            if (sortOrder == null || sortOrder.trim().length() == 0)
                sortOrder = Email.EMAIL_ADDRESS + " ASC";
        }

        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        Cursor cursor = database.query(Email.EMAIL_TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        if (matcher.match(uri) == QUERY_ALL_EMAIL_CODE) {
            return EMAIL_CONTENT_TYPE;
        }
        throw new IllegalArgumentException("Unknown URI " + uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        // unrecognized uri
        if (matcher.match(uri) != QUERY_ALL_EMAIL_CODE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // something is wrong with contentValues
        if (contentValues == null
                || !contentValues.containsKey(Email.EMAIL_ADDRESS)
                || contentValues.getAsString(Email.EMAIL_ADDRESS).trim().length() == 0) {
            throw new IllegalArgumentException("Missing Column:" + Email.EMAIL_ADDRESS);
        }

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        long id = database.insert(Email.EMAIL_TABLE_NAME, null, contentValues);
        if (id > 0) {
            Uri insertUri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(insertUri, null);
            return insertUri;
        }

        throw new SQLException("Failed to insert email address into uri:" + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        if (matcher.match(uri) != QUERY_ALL_EMAIL_CODE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int count = database.delete(Email.EMAIL_TABLE_NAME, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArgs) {
        if (matcher.match(uri) != QUERY_ALL_EMAIL_CODE) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int count = database.update(Email.EMAIL_TABLE_NAME, contentValues, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {
        /* Array to use to preload database in onCreate */
        private static final String[] emails = new String[]{"alice@someDomain.com", "bob@someDomain.com",
                "larry@someDomain.com", "edward@someDomain.com", "matt@someDomain.com",
                "william@someDomain.com", "jean@someDomain.com", "john@someDomain.com",
                "jude@someDomain.com", "arnold@someDomain.com", "james@someDomain.com",
                "nancy@someDomain.com", "benedict@someDomain.com", "diane@someDomain.com",
                "clyde@someDomain.com"};

        private DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String createStatement = "CREATE TABLE " + Email.EMAIL_TABLE_NAME + " (" +
                    Email.EMAIL_ID + " integer  PRIMARY KEY AUTOINCREMENT DEFAULT NULL," +
                    Email.EMAIL_ADDRESS + " Varchar(100))";
            sqLiteDatabase.execSQL(createStatement);
            ContentValues values;
            for (String email : emails) {
                values = new ContentValues();
                values.put(Email.EMAIL_ADDRESS, email);
                sqLiteDatabase.insert(Email.EMAIL_TABLE_NAME, null, values);
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Email.EMAIL_TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }
}
