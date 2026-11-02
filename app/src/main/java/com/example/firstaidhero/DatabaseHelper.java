package com.example.firstaidhero;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "firstaid.db";
    private static final int DATABASE_VERSION = 1;
    private static String DB_PATH = "";
    private final Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        copyDatabase();
    }

    private boolean checkDatabase() {
        File dbFile = new File(DB_PATH + DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDatabase() {
        if (!checkDatabase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
                Log.d("DatabaseHelper", "Database copied successfully.");
            } catch (IOException e) {
                Log.e("DatabaseHelper", "Error copying database: " + e.getMessage());
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
        File dir = new File(DB_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        OutputStream mOutput = new FileOutputStream(DB_PATH + DATABASE_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }
    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(DB_PATH + DATABASE_NAME, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public Cursor getAllEmergencies() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM emergencies ORDER BY case_id ASC", null);
    }

    public Cursor searchEmergencies(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM emergencies WHERE title LIKE ? OR symptoms LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"});
    }

    public Cursor getEmergencyById(int caseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM emergencies WHERE case_id = ?",
                new String[]{String.valueOf(caseId)});
    }
    public Cursor getStepsByCaseId(int caseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM steps WHERE case_id = ? ORDER BY step_number ASC",
                new String[]{String.valueOf(caseId)});
    }
    public Cursor getStepsForCase(int caseId) {
        return getStepsByCaseId(caseId);
    }
    public Cursor getAllCustomContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM custom_contacts ORDER BY contact_id DESC", null);
    }

    public boolean addCustomContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("contact_name", name);
        values.put("phone_number", phone);
        long result = db.insert("custom_contacts", null, values);
        return result != -1;
    }
    public boolean deleteCustomContact(int contactId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("custom_contacts", "contact_id = ?", new String[]{String.valueOf(contactId)});
        return result > 0;
    }
    public boolean addTreatedCase(String description, String date, String photoPath, String procedures) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Cursor cursor = db.rawQuery("PRAGMA table_info(treated_cases)", null);
        boolean hasUpperDesc = false;
        boolean hasUpperDate = false;
        boolean hasUpperProc = false;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String colName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                if (colName.equalsIgnoreCase("Description")) {
                    hasUpperDesc = Character.isUpperCase(colName.charAt(0));
                }
                if (colName.equalsIgnoreCase("Date")) {
                    hasUpperDate = Character.isUpperCase(colName.charAt(0));
                }
                if (colName.equalsIgnoreCase("Procedures")) {
                    hasUpperProc = Character.isUpperCase(colName.charAt(0));
                }
            }
            cursor.close();
        }

        values.put(hasUpperDesc ? "Description" : "description", description);
        values.put(hasUpperDate ? "Date" : "date", date);
        values.put("photo", photoPath);
        values.put(hasUpperProc ? "Procedures" : "procedures", procedures);

        long result = -1;
        try {
            result = db.insert("treated_cases", null, values);
            if (result == -1) {
                values.remove("photo");
                values.put("photo_path", photoPath);
                result = db.insert("treated_cases", null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result != -1;
    }

    public Cursor getAllTreatedCases() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM treated_cases ORDER BY rowid DESC", null);
    }

    public Cursor searchTreatedCases(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM treated_cases WHERE date LIKE ? OR description LIKE ? ORDER BY rowid DESC",
                new String[]{"%" + query + "%", "%" + query + "%"});
    }

    public int getCaseCountByDanger(String dangerLevel) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM emergencies WHERE danger_level LIKE ?",
                new String[]{"%" + dangerLevel + "%"});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }
    public int getTotalTreatedCasesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM treated_cases", null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }
}