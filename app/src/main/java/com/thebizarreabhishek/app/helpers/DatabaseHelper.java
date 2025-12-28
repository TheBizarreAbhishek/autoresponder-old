package com.thebizarreabhishek.app.helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.thebizarreabhishek.app.models.Message;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "MADARA";
    private static final String DATABASE_NAME = "whatsappMessages.db";
    private static final int DATABASE_VERSION = 2; // Updated for platform column
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_REPLY = "reply";
    public static final String COLUMN_PLATFORM = "platform";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_MESSAGES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SENDER + " TEXT, " +
            COLUMN_MESSAGE + " TEXT, " +
            COLUMN_TIMESTAMP + " TEXT, " +
            COLUMN_REPLY + " TEXT, " +
            COLUMN_PLATFORM + " TEXT" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // ----------------------------------------------------------------------------------------------

    public void insertMessage(String sender, String message, String timestamp, String reply, String platform) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_REPLY, reply);
        values.put(COLUMN_PLATFORM, platform);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public void deleteOldMessages() {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            @SuppressLint("SimpleDateFormat")
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String whereClause = COLUMN_TIMESTAMP + " < ?";
            String[] whereArgs = { currentDate + " 00:00:00" };
            db.delete(TABLE_MESSAGES, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting old messages", e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public List<Message> getChatHistoryBySender(String sender) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_SENDER + " = ? " +
                    "ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 7";
            cursor = db.rawQuery(query, new String[] { sender });

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    String reply = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPLY));
                    String platform = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLATFORM));

                    Message msg = new Message(id, sender, message, timestamp, reply, platform);
                    messages.add(msg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getChatHistoryBySender: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return messages;
    }

    public List<Message> getAllMessagesBySender(String sender) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + COLUMN_SENDER + " = ? " +
                    "ORDER BY " + COLUMN_TIMESTAMP + " DESC";
            cursor = db.rawQuery(query, new String[] { sender });

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    String reply = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPLY));
                    String platform = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLATFORM));

                    Message msg = new Message(id, sender, message, timestamp, reply, platform);
                    messages.add(msg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllMessagesBySender: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return messages;
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_MESSAGES + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String sender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
                    String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                    String reply = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPLY));
                    String platform = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLATFORM));

                    Message msg = new Message(id, sender, message, timestamp, reply, platform);
                    messages.add(msg);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllMessages: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return messages;
    }

    public int getMessagesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = android.database.DatabaseUtils.queryNumEntries(db, TABLE_MESSAGES);
        db.close();
        return (int) count;
    }

    public int getTotalWordsCount() {
        int totalWords = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + COLUMN_REPLY + " FROM " + TABLE_MESSAGES, null);
            if (cursor.moveToFirst()) {
                do {
                    String reply = cursor.getString(0);
                    if (reply != null && !reply.isEmpty()) {
                        String[] words = reply.trim().split("\\s+");
                        totalWords += words.length;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getTotalWordsCount: ", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return totalWords;
    }

    public List<com.thebizarreabhishek.app.models.ContactSummary> getUniqueSenders() {
        List<com.thebizarreabhishek.app.models.ContactSummary> contacts = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            // Get unique senders with their latest message, timestamp, message count, and platform
            String query = "SELECT " + COLUMN_SENDER + ", " +
                    "(SELECT " + COLUMN_MESSAGE + " FROM " + TABLE_MESSAGES + " m2 WHERE m2." + COLUMN_SENDER + " = m1." + COLUMN_SENDER + " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1) as lastMessage, " +
                    "MAX(" + COLUMN_TIMESTAMP + ") as lastTimestamp, " +
                    "COUNT(*) as messageCount, " +
                    "(SELECT " + COLUMN_PLATFORM + " FROM " + TABLE_MESSAGES + " m3 WHERE m3." + COLUMN_SENDER + " = m1." + COLUMN_SENDER + " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1) as platform " +
                    "FROM " + TABLE_MESSAGES + " m1 " +
                    "GROUP BY " + COLUMN_SENDER + " " +
                    "ORDER BY lastTimestamp DESC";
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String senderName = cursor.getString(0);
                    String lastMessage = cursor.getString(1);
                    String lastTimestamp = cursor.getString(2);
                    int messageCount = cursor.getInt(3);
                    String platform = cursor.getString(4);

                    com.thebizarreabhishek.app.models.ContactSummary contact = 
                        new com.thebizarreabhishek.app.models.ContactSummary(senderName, lastMessage, lastTimestamp, messageCount, platform);
                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getUniqueSenders: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return contacts;
    }

    // ==================== SMART REPLIES ====================

    public static final String TABLE_SMART_REPLIES = "smart_replies";
    public static final String SR_COLUMN_ID = "_id";
    public static final String SR_COLUMN_TRIGGER = "trigger_text";
    public static final String SR_COLUMN_RESPONSE = "response_text";
    public static final String SR_COLUMN_ENABLED = "enabled";

    public void createSmartRepliesTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SMART_REPLIES + " (" +
                SR_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SR_COLUMN_TRIGGER + " TEXT, " +
                SR_COLUMN_RESPONSE + " TEXT, " +
                SR_COLUMN_ENABLED + " INTEGER DEFAULT 1" +
                ");";
        db.execSQL(createTable);
        db.close();
    }

    public void insertSmartReply(String trigger, String response) {
        createSmartRepliesTable();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SR_COLUMN_TRIGGER, trigger);
        values.put(SR_COLUMN_RESPONSE, response);
        values.put(SR_COLUMN_ENABLED, 1);
        db.insert(TABLE_SMART_REPLIES, null, values);
        db.close();
    }

    public List<com.thebizarreabhishek.app.models.SmartReply> getAllSmartReplies() {
        createSmartRepliesTable();
        List<com.thebizarreabhishek.app.models.SmartReply> replies = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + TABLE_SMART_REPLIES, null);

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(SR_COLUMN_ID));
                    String trigger = cursor.getString(cursor.getColumnIndexOrThrow(SR_COLUMN_TRIGGER));
                    String response = cursor.getString(cursor.getColumnIndexOrThrow(SR_COLUMN_RESPONSE));
                    boolean enabled = cursor.getInt(cursor.getColumnIndexOrThrow(SR_COLUMN_ENABLED)) == 1;

                    replies.add(new com.thebizarreabhishek.app.models.SmartReply(id, trigger, response, enabled));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllSmartReplies: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return replies;
    }

    public void updateSmartReply(int id, String trigger, String response, boolean enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SR_COLUMN_TRIGGER, trigger);
        values.put(SR_COLUMN_RESPONSE, response);
        values.put(SR_COLUMN_ENABLED, enabled ? 1 : 0);
        db.update(TABLE_SMART_REPLIES, values, SR_COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteSmartReply(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SMART_REPLIES, SR_COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public String findMatchingSmartReply(String incomingMessage) {
        createSmartRepliesTable();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String response = null;

        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + TABLE_SMART_REPLIES + " WHERE " + SR_COLUMN_ENABLED + " = 1", null);

            if (cursor.moveToFirst()) {
                String lowerMessage = incomingMessage.toLowerCase().trim();
                do {
                    String trigger = cursor.getString(cursor.getColumnIndexOrThrow(SR_COLUMN_TRIGGER));
                    if (trigger != null && lowerMessage.contains(trigger.toLowerCase().trim())) {
                        response = cursor.getString(cursor.getColumnIndexOrThrow(SR_COLUMN_RESPONSE));
                        break;
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "findMatchingSmartReply: ", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }
        return response;
    }
}

