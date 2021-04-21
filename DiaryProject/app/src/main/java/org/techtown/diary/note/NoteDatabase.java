package org.techtown.diary.note;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.techtown.diary.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class NoteDatabase {
    // 상수
    private static final String LOG = "NoteDatabase";
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "note.db";             // db 이름
    public static final String NOTE_TABLE = "Note";             // table 이름(일기목록을 위한 테이블)
    public static final String NOTE_INDEX = "Note_Index";       //

    // Column
    public static final String NOTE_ID = "_id";                 // id(기본키)
    public static final String NOTE_WEATHER = "weather";        // 날씨
    public static final String NOTE_ADDRESS = "address";        // 주소
    public static final String NOTE_LOCATION_X = "location_x";   //
    public static final String NOTE_LOCATION_Y = "location_y";   //
    public static final String NOTE_CONTENTS = "contents";      // 일기 내용
    public static final String NOTE_MOOD = "mood";              // 기분
    public static final String NOTE_PICTURE = "picture";        // 사진 경로
    public static final String NOTE_CREATE_DATE = "create_date";// 일기 생성일
    public static final String NOTE_MODIFY_DATE = "modify_date";// 일기 수정일

    // SQL
    private static final String dropNoteTableSQL = "DROP TABLE IF EXISTS " + NOTE_TABLE + ";";
    private static final String createNoteTableSQL = "CREATE TABLE IF NOT EXISTS " + NOTE_TABLE + " ("
            + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NOTE_WEATHER + " INTEGER DEFAULT -1, "
            + NOTE_ADDRESS + " TEXT DEFAULT '', "
            + NOTE_LOCATION_X + " TEXT DEFAULT '', "
            + NOTE_LOCATION_Y + " TEXT DEFAULT '', "
            + NOTE_CONTENTS + " TEXT DEFAULT '', "
            + NOTE_MOOD + " INTEGER DEFAULT -1, "
            + NOTE_PICTURE + " TEXT DEFAULT '', "
            + NOTE_CREATE_DATE + " TIMESTAMP DEFAULT (datetime('now','localtime')), "
            + NOTE_MODIFY_DATE + " TIMESTAMP DEFAULT (datetime('now','localtime'))"
            + ");";
    private static final String createNoteIndexCreateDateSQL = "CREATE UNIQUE INDEX IF NOT EXISTS " + NOTE_INDEX + " ON " +
            NOTE_TABLE + "(" + NOTE_CREATE_DATE + ");";
    private static final String insertNoteSQL = "INSERT INTO " + NOTE_TABLE + "("
            + NOTE_WEATHER + ", " + NOTE_ADDRESS + ", " + NOTE_LOCATION_X + ", " + NOTE_LOCATION_Y + ", " + NOTE_CONTENTS + ", " + NOTE_MOOD + ", " + NOTE_PICTURE
            + ") VALUES(?,?,?,?,?,?,?);";
    private static final String updateNoteSQL = "UPDATE " + NOTE_TABLE + " SET "
            + NOTE_CONTENTS + "=?, "
            + NOTE_MOOD + "=?, "
            + NOTE_LOCATION_X + "='" + "', "
            + NOTE_LOCATION_Y + "='" + "', "
            + NOTE_PICTURE + "=?, "
            + NOTE_MODIFY_DATE + "=" + "(datetime('now','localtime'))"
            + "WHERE " + NOTE_ID + "=?;";
    private static final String selectNoteSQL = "SELECT " + NOTE_ID + ", " + NOTE_WEATHER + ", " + NOTE_ADDRESS + ", " + NOTE_LOCATION_X + ", " + NOTE_LOCATION_Y + ", "
            + NOTE_CONTENTS + ", " + NOTE_MOOD + ", " + NOTE_PICTURE + ", " + NOTE_CREATE_DATE +
            " FROM " + NOTE_TABLE + " ORDER BY " + NOTE_CREATE_DATE + " DESC;";      // 일기 생성일 내림차순 = 최신 일기가 제일 위


    private Context context;
    private DatabaseHelper helper;
    private SQLiteDatabase db;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 생성자
    public NoteDatabase(Context context) {
        this.context = context;
    }

    public void dbInit(String dbName) {
        helper = new DatabaseHelper(context, dbName, null, DB_VERSION);      // 현재 db version = 1
        db = helper.getWritableDatabase();                                          // DatabaseHelper 를 이용해 db 초기화
        //db.execSQL(dropNoteTableSQL);
        Log.d(LOG, "db 오픈 성공");
    }

    public void createTable(String tableName) {
        if(db != null) {
            if(tableName.equals(NOTE_TABLE)) {
                db.execSQL(createNoteTableSQL);
                Log.d(LOG, "Note 테이블 생성 성공");
                db.execSQL(createNoteIndexCreateDateSQL);
                Log.d(LOG, "create_date 에 인덱스 생성 완료");
            }
        } else {
            Log.d(LOG, "db를 먼저 오픈해주세요");
        }
    }

    public void insert(String tableName, Object[] objs) {
        if(db != null) {
            if(tableName.equals(NOTE_TABLE)) {
                db.execSQL(insertNoteSQL, objs);
                Log.d(LOG, "Note 테이블에 데이터 삽입 성공");
            }
        } else {
            Log.d(LOG, "db를 먼저 오픈해주세요");
        }
    }

    public void delete(String tableName, int id) {
        if(db != null) {
            if(tableName.equals(NOTE_TABLE)) {
                String sql = "DELETE FROM " + NOTE_TABLE + " WHERE " + NOTE_ID + "=" + id + ";";
                db.execSQL(sql);
                Log.d(LOG, "Note 테이블 데이터 삭제성공 : " + id);
            }
        } else {
            Log.d(LOG, "db를 먼저 오픈해주세요");
        }
    }

    public void update(String tableName, Note item) {
        if(db != null) {
            if(tableName.equals(NOTE_TABLE)) {
                int id = item.get_id();
                String contents = item.getContents();
                int moodIndex = item.getMood();
                String path = item.getPicture();

                Object[] objs = { contents, moodIndex, path, id };

                db.execSQL(updateNoteSQL, objs);
                Log.d(LOG, "Note 테이블 데이터 수정성공 : " + id);
            }
        } else {
            Log.d(LOG, "db를 먼저 오픈해주세요");
        }
    }

    public ArrayList<Note> selectAll(String tableName) {
        ArrayList<Note> items = new ArrayList<>();

        if(db != null) {
            if(tableName.equals(NOTE_TABLE)) {
                Cursor cursor = db.rawQuery(selectNoteSQL, null);
                Note item = null;

                while(cursor.moveToNext()) {
                    int _id = cursor.getInt(0);
                    int _weather = cursor.getInt(1);
                    String _address = cursor.getString(2);
                    String _locationX = cursor.getString(3);
                    String _locationY = cursor.getString(4);
                    String _contents = cursor.getString(5);
                    int _mood = cursor.getInt(6);
                    String _picture = cursor.getString(7);
                    String _createDate = cursor.getString(8);
                    String createDateStr = null;
                    String time = null;
                    String dayOfWeek = null;

                    if(_createDate != null && _createDate.length() > 10) {
                        try {
                            Date date = timeFormat.parse(_createDate);
                            createDateStr = MainActivity.dateFormat.format(date);
                            time = MainActivity.timeFormat.format(date);
                            dayOfWeek = MainActivity.getDayOfWeek(date);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        createDateStr = "";
                    }

                    item = new Note(_id, _weather, _address, _locationX, _locationY, _contents, _mood, _picture, createDateStr, time, dayOfWeek);
                    items.add(item);
                }

                cursor.close();
            }
        } else {
            Log.d(LOG, "db를 먼저 오픈해주세요");
        }

        return items;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(dropNoteTableSQL);
            Log.d(LOG, "Note 테이블 삭제 완료");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
