package mymusic.com.mymusic.CollectMusicSQ;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Administrator on 2016/9/5.
 * 保存收藏音乐信息的数据库；
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "collectMusic.db";       //数据库名
    private final static int DATABASE_VERSION = 1;                     //数据库版本
    private final static String DATABASE_TABLE_NAME="collectMusic";       //表名
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.v("pppp","DataBaseHelper 构造方法");
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String addTable=
                "CREATE TABLE IF NOT EXISTS "+
                        DATABASE_TABLE_NAME+"("+
                        collectMusicBean.MUSIC_TABLE_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        collectMusicBean.MUSIC_NAME+" varchar, "+
                        collectMusicBean.MUSIC_SINGER+" varchar, "+
                        collectMusicBean.MUSIC_PATH+" varchar, "+
                        collectMusicBean.MUSIC_ALBUM_ID+" long, "+
                        collectMusicBean.MUSIC_ID+" long, "+
                        collectMusicBean.MUSIC_IMAGE_PATH+" varchar,"+
                        collectMusicBean.MUSIC_COLLECT+" varchar"+")";
        db.execSQL(addTable);
                                Log.v("ppppp","DatabaseHelper>>>>>onCrate>>"+addTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {        //升级数据库时会执行；

    }
}
