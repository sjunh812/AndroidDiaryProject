package org.techtown.diary.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Objects;

public class NoteDatebase {
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

    private Context context;
    private DatabaseHelper helper;
    private SQLiteDatabase db;

    // 생성자
    public NoteDatebase(Context context) {
        this.context = context;
    }

    public void dbInit(String dbName) {
        helper = new DatabaseHelper(context, dbName, null, DB_VERSION);      // 현재 db version = 1
        db = helper.getWritableDatabase();                                          // DatabaseHelper 를 이용해 db 초기화
        db.execSQL(dropNoteTableSQL);
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
