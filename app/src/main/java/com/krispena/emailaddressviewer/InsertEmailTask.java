package com.krispena.emailaddressviewer;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class InsertEmailTask extends AsyncTask<String, Void, Void> {

    private ContentResolver resolver;
    private Handler handler;

    /**
     * @param resolver ContentResolver to use for querying database
     * @param handler Handler to use as an alternative to passing Activity
     */
    public InsertEmailTask(ContentResolver resolver, Handler handler) {
        this.resolver = resolver;
        this.handler = handler;
    }

    @Override
    protected Void doInBackground(String... emails) {
        // only allow insertion of 1 email address at a time and force lowercase
        String email = emails[0].toLowerCase();
        String whereArgs = Email.EMAIL_ADDRESS + " LIKE ?";
        Cursor cursor = resolver.query(EmailProvider.CONTENT_URI, null, whereArgs, new String[]{email}, null);
        if (cursor.getCount() > 0) { // email address already exists, notify error
            Message message = handler.obtainMessage();
            message.what = EmailListFragment.MESSAGE_FAIL;
            handler.sendMessage(message);
        } else { // safe to insert
            // insert database entry
            ContentValues values = new ContentValues();
            values.put(Email.EMAIL_ADDRESS, email);
            resolver.insert(EmailProvider.CONTENT_URI, values);

            // notify handler of success
            Message message = handler.obtainMessage();
            message.what = EmailListFragment.MESSAGE_SUCCESS;
            handler.sendMessage(message);
        }
        return null;
    }
}
