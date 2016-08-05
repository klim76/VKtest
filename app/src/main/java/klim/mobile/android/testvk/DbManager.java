package klim.mobile.android.testvk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by e.klim on 03.08.2016.
 */
public class DbManager {
    private static final String DB_NAME = "vkdb";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "friends";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERID = "user_id";
    public static final String COLUMN_NAME = "full_name";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_PHOTO = "photo";


    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_USERID + " integer, "+
                    COLUMN_NAME + " text, " +
                    COLUMN_STATUS + " text, " +
                    COLUMN_PHOTO + " BLOB " +
                    ");";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DbManager(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDB.query(DB_TABLE, null, null, null, null, null, null);
    }

    public Cursor getData(int id) {
        return mDB.query(DB_TABLE, null, COLUMN_ID + " = " + id, null, null, null, null);
    }

    // добавить запись в DB_TABLE
    public void addRec(int id, String name, String status, byte[] photo) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERID, id);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_STATUS, status);
        cv.put(COLUMN_PHOTO, photo);
        mDB.insert(DB_TABLE, null, cv);
    }

    // удалить запись из DB_TABLE
    public void delRec() {
        mDB.delete(DB_TABLE, null, null);
    }

    //TODO: обновление записи
    public void update(ContentValues values) {
        mDB.update(DB_TABLE,values,null,null);
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
