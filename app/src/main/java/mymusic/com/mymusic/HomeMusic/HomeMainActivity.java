package mymusic.com.mymusic.HomeMusic;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import mymusic.com.mymusic.CollectMusicSQ.collectMusicBean;
import mymusic.com.mymusic.CollectMusicSQ.DatabaseHelper;
import mymusic.com.mymusic.MusicList.MusicListMainActivity;
import mymusic.com.mymusic.PlayMusic.MusicService;
import mymusic.com.mymusic.PlayMusic.PlayMusicActivity;
import mymusic.com.mymusic.R;

/**
 *
 * app主界面
 * */
public class HomeMainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String PERV="com.HomeMainActivity_PERV";
    private static final String PURSE="HomeMainActivity_PUSER";
    private static final String NEXT="HomeMainActivity_NEXT";
    private static final String SET_HOME_MESS="HomeMainActivity_MESS";
    private final static String DATABASE_TABLE_NAME="collectMusic";
    private static final String MUSIC_LIST="MUSIC_LIST";
    private static final String CURRENT_POSITION="CURRENT_POSITION";
    private static final String INTENT_TYPE="INTENT_TYPE";
    private static final String END_MUSIC="END_MUSIC";
    private static final Uri albumArtUri = Uri
            .parse("content://media/external/audio/albumart");
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;
    private SharedPreferences share;
    private int end_postion;  //上次退出时播放的音乐
    private  int list_type;   //上次退出时播放的音乐列表
    public static boolean type=true;
    private ImageView pImage;
    private TextView pTitle;
    private MusicBean musicBean ;
    private ImageButton popupMenu;
    private View context;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;
    private float x1, x2, y1, y2;
    private TextView musicName;
    private ImageView musicPicture, musicTopPicture;
    private HorizontalScrollView homeMusicList;
    public static ArrayList<MusicBean> totalMusicList =new ArrayList<>();//全部音乐
    public static ArrayList<MusicBean> collectMusicList=new ArrayList<>();//收藏的音乐
    private MusicGrideAdapter homeMusicAdapterCollect;
    private GridView totalMusicListGrid;
    private TextView searchMusic;
    private GridView collectMusicListGrid;
    private ImageButton prev,purse,next;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private BroadcastReceiver setHmess=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                    if(intent.getAction().equals(SET_HOME_MESS)){
                        setHomeMusicMess();
                    }
        }
    };
    private void registerReceiver(){
        IntentFilter setMess=new IntentFilter(SET_HOME_MESS);
        registerReceiver(setHmess,setMess);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);
           databaseHelper=new DatabaseHelper(this);
           sqLiteDatabase=databaseHelper.getReadableDatabase();
            setCollectMusic();


        inintView();
        setListener();
        new Thread(){            //开一线程来读取音乐库
            @Override
            public void run() {
                queryMedia();
                new Thread(){
                    @Override
                    public void run() {

                        handler.post(new Runnable() {             //更新音乐列表界面
                            @Override
                            public void run() {
                                searchMusic.setVisibility(View.GONE);
                                MusicGrideAdapter homeMusicAdapterTotal = new MusicGrideAdapter(HomeMainActivity.this);
                                ViewGroup.LayoutParams params=totalMusicListGrid.getLayoutParams();
                                params.width=DensityUtil.dip2px(HomeMainActivity.this,90)*totalMusicList.size();
                                totalMusicListGrid.setLayoutParams(params);
                                totalMusicListGrid.setNumColumns(totalMusicList.size());
                                totalMusicListGrid.setAdapter(homeMusicAdapterTotal);
                                homeMusicAdapterTotal.setMusicList(totalMusicList);
                                Log.v("pppp","musicHome>>>>>>>onCreate"+ totalMusicList.size()+"");
                                setFirstMusic();
                            }
                        });
                    }
                }.start();

            }

        }.start();


        homeMusicAdapterCollect =new MusicGrideAdapter(this);
        collectMusicListGrid.setAdapter(homeMusicAdapterCollect);
        homeMusicAdapterCollect.setMusicList(collectMusicList);



        context = LayoutInflater.from(this).inflate(R.layout.pupupwindows_layout, null);     //解析左侧菜单popupWindows
        linearLayout = (LinearLayout) context.findViewById(R.id.music_home_Lin_musicList);    //全部音乐按钮；
        pImage= (ImageView) context.findViewById(R.id.popup_image);
        pTitle= (TextView) context.findViewById(R.id.popup_music_title);
        linearLayout.setOnClickListener(this);
        popupWindow = new PopupWindow(context);
        setPopup(popupWindow, context);


        totalMusicListGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HomeMainActivity.this,PlayMusicActivity.class);
                      intent.putParcelableArrayListExtra(MUSIC_LIST, totalMusicList);
                        intent.putExtra(CURRENT_POSITION,position);
                         intent.putExtra(INTENT_TYPE,true);
                           startActivity(intent);
                               overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);//引用系统淡入淡出动画

                                      Log.v("searchMList", totalMusicList.size()+">>>>>position:"+position);
                                    type=false;
            }
        });


        collectMusicListGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HomeMainActivity.this,PlayMusicActivity.class);
                  intent.putParcelableArrayListExtra(MUSIC_LIST, collectMusicList);
                    intent.putExtra(CURRENT_POSITION,position);
                      intent.putExtra(INTENT_TYPE,true);
                        startActivity(intent);
                            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);//引用系统淡入淡出动画
                                      Log.v("searchMList", collectMusicList.size()+">>>>>position:"+position);
                                    type=false;
            }
        });


    }


    /**
     * 从数据库获取收藏的音乐数据；
     * */
    private ArrayList<MusicBean> setCollectMusic(){
        String[] colume=new String[]{
            collectMusicBean.MUSIC_ID,collectMusicBean.MUSIC_NAME,collectMusicBean.MUSIC_PATH,collectMusicBean.MUSIC_SINGER,collectMusicBean.MUSIC_ALBUM_ID,collectMusicBean.MUSIC_IMAGE_PATH,collectMusicBean.MUSIC_COLLECT};
        Cursor cursor=sqLiteDatabase.query(DATABASE_TABLE_NAME,colume,null,null,null,null,null,null);
        if(collectMusicList.size()>0){
            collectMusicList.clear();
        }
          while (cursor.moveToNext()){
              MusicBean musicBean=new MusicBean();
              int idIndex=cursor.getColumnIndex(collectMusicBean.MUSIC_ID);
              int nameIndex=cursor.getColumnIndex(collectMusicBean.MUSIC_NAME);
              int pathIndex=cursor.getColumnIndex(collectMusicBean.MUSIC_PATH);
              int singerIndex=cursor.getColumnIndex(collectMusicBean.MUSIC_SINGER);
              int imageIndex=cursor.getColumnIndex(collectMusicBean.MUSIC_IMAGE_PATH);
              int collectIndex=cursor.getColumnIndex(collectMusicBean.MUSIC_COLLECT);
              int albumIndex=cursor.getColumnIndex(collectMusicBean.MUSIC_ALBUM_ID);

              String name=cursor.getString(nameIndex);
              String path=cursor.getString(pathIndex);
              String singer=cursor.getString(singerIndex);
              String image=cursor.getString(imageIndex);
              String collect=cursor.getString(collectIndex);
              long album=cursor.getLong(albumIndex);
              long id=cursor.getLong(idIndex);
              musicBean.setTitle(name);
              musicBean.setUri(path);
              musicBean.setArtist(singer);
              musicBean.setImage(image);
              musicBean.setCollect(collect);
              musicBean.setAlbumid(album);
              musicBean.setId(id);
              Log.v("ppppv","collect>>>>>>>>>"+name+path+">>collect"+collect);
               collectMusicList.add(musicBean);
          }
        cursor.close();
        return collectMusicList;
    }

    /**
     * 设置上一次退出时播放的音乐；
     *
     * */
  private void setFirstMusic(){
      share=getSharedPreferences("endMusic",MODE_PRIVATE);
      end_postion=share.getInt("POSITION",0);
      // list_type=share.getInt("LIST_TYPE",0);
    /*  if(list_type==0){
          musicBean = collectMusicList.get(end_postion);
      }else{
          musicBean = totalMusicList.get(end_postion);
      }*/
      musicBean = totalMusicList.get(end_postion);
          musicName.setText(musicBean.getTitle());
          Bitmap icon=BitmapUtil.getArtwork(this,musicBean.getId(),musicBean.getAlbumid(),false,false);
       musicPicture.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(),R.drawable.red):icon);
       musicTopPicture.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(),R.drawable.red):icon);
       pImage.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(),R.drawable.red):icon);
          pTitle.setText("正在播放："+musicBean.getTitle());

  }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();//注册广播
        if(popupWindow.isShowing()){
            popupWindow.dismiss();
        }

               setHomeMusicMess(); //同步音乐信息
                 Log.v("pppp","musicHome>>>>>>>onResume");
    }

    /**
     * 初始化控件
     * */
    private void inintView() {
        popupMenu = (ImageButton) findViewById(R.id.music_honme_menu);
        musicName = (TextView) findViewById(R.id.home_music_title);
        musicPicture = (ImageView) findViewById(R.id.home_music_small_picture);
        musicTopPicture = (ImageView) findViewById(R.id.home_music_picture);
        homeMusicList= (HorizontalScrollView) findViewById(R.id.home_music_list_scrollview);
        totalMusicListGrid = (GridView) findViewById(R.id.home_music_list_grid);
        collectMusicListGrid= (GridView) findViewById(R.id.home_music_collectList);

        prev= (ImageButton) findViewById(R.id.home_last);
        purse= (ImageButton) findViewById(R.id.home_purse);
        next= (ImageButton) findViewById(R.id.home_next);

        searchMusic= (TextView) findViewById(R.id.home_music_list_search);

    }

    /**
     * 注册监听
     *
     * */
    private void setListener() {
        popupMenu.setOnClickListener(this);
        musicPicture.setOnClickListener(this);
        prev.setOnClickListener(this);
        purse.setOnClickListener(this);
        next.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        MusicService.MusicBinder musicBinder=PlayMusicActivity.getMusicBinder();
        switch (v.getId()) {
            case R.id.music_honme_menu:
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    popupWindow.showAtLocation(v, 0, 0, 0);//显示左侧菜单
                }
                break;


            case R.id.music_home_Lin_musicList:
                Intent intent = new Intent(this, MusicListMainActivity.class);
                  startActivity(intent);
                    break;


            case R.id.home_music_title:
            case R.id.home_music_small_picture:
                Intent intentM = new Intent(this, PlayMusicActivity.class);
                if(type){
                   /* ArrayList<MusicBean> list;
                    if(list_type==0){
                        list=collectMusicList;}else{
                        list=totalMusicList;
                    }*/
                    intentM.putParcelableArrayListExtra("MUSIC_LIST", totalMusicList);
                    intentM.putExtra("CURRENT_POSITION",end_postion);
                    intentM.putExtra("TYPE",type);
                         type=false;
                }else{
                    intentM.putExtra("TYPE",type);
                }
                      startActivity(intentM);
                          overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                         break;


            case R.id.home_last:
                if(musicBinder!=null){
                    musicBinder.PreviousMusic(this);
                    Toast.makeText(this,"上一曲",Toast.LENGTH_SHORT).show();
                }
                  /*Intent intentPerv=new Intent();
                    intentPerv.setAction(PERV);
                      sendBroadcast(intentPerv);*/
                               break;


            case R.id.home_purse:
                if(type){
                    Toast.makeText(this,"音乐君睡着了,点击音乐图片试试看",Toast.LENGTH_SHORT).show();
                       return;
                }
              /*  if(musicBinder!=null){
                    musicBinder.startOrPause();
                        if(musicBinder.isPlay()){
                            purse.setImageResource(R.mipmap.startnotify);
                        }else {
                            purse.setImageResource(R.mipmap.pausenotify);
                        }
                }*/
                Intent intentPurse=new Intent();
                   intentPurse.setAction(PURSE);
                     sendBroadcast(intentPurse);
                            break;

            case R.id.home_next:
                if(musicBinder!=null){
                    musicBinder.NextMusic(this);
                    Toast.makeText(this,"下一曲",Toast.LENGTH_SHORT).show();
                }
               /* Intent intentNext=new Intent();
                  intentNext.setAction(NEXT);
                    sendBroadcast(intentNext);*/
                           break;

        }
    }


    /**
     * 设置左边弹出菜单
     */
    public void setPopup(PopupWindow popupWindow, View context) {
        //实例PopupWindow；
        popupWindow.setContentView(context);
        popupWindow.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setWidth(750);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x06060));
    }

    /**
     * 与音乐播放界面信息同步
     * */
    public void setHomeMusicMess() {

        if (PlayMusicActivity.getCurrentMusicBean() == null) {
                    return;
        }else{
            musicBean = PlayMusicActivity.getCurrentMusicBean();
            if(PlayMusicActivity.getMusicBinder().isPlay()){
                purse.setImageResource(R.mipmap.startnotify);
            }else {
                purse.setImageResource(R.mipmap.pausenotify);
            }
            musicName.setText(musicBean.getTitle());

            Bitmap icon = BitmapFactory.decodeFile(musicBean.getImage());
            musicPicture.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(),R.mipmap.music1):icon);
            musicTopPicture.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(),R.drawable.red):icon);

            pImage.setImageBitmap(icon == null ? BitmapFactory.decodeResource(getResources(),R.drawable.red):icon);  //左侧弹出菜单音乐封面设置
            pTitle.setText("正在播放："+musicBean.getTitle());                                                       //左侧弹出菜单音乐名
            homeMusicAdapterCollect.setMusicList(collectMusicList);
        }
                                    Log.v("pppp","homeSetMess>>>>>>>."+musicBean.getTitle());
    }

    /**
     *
     * 读取手机媒体库，获取全部音乐信息；
     * */
    public  void queryMedia() {
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        /**
         * MediaStore.Audio.Media.EXTERNAL_CONTENT_URI 对应字段
         * 歌曲ID：MediaStore.Audio.Media._ID
         * 歌曲的名称：MediaStore.Audio.Media.TITLE
         * 歌曲的专辑名：MediaStore.Audio.Media.ALBUM
         * 歌曲的歌手名：MediaStore.Audio.Media.ARTIST
         * 歌曲文件的路径：MediaStore.Audio.Media.DATA
         * 歌曲的总播放时长：MediaStore.Audio.Media.DURATION
         * 歌曲文件的大小：MediaStore.Audio.Media.SIZE
         *
         */
        Log.v("music","音乐名  " + "艺术家" + "  " + "icon");
        while (cursor.moveToNext()) {
            // 如果不是音乐
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));//音乐id
            final String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));//音乐标题
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));//专辑
            long albumid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//专辑id
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));//文件大小
            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//文件路径

            if (isRepeat(title, artist)) continue;
            if(duration>60000&&isMusic!=0){         //过滤掉时长小于60秒的音乐
            MusicBean music = new MusicBean();
            music.setId(id);
            music.setTitle(title);
            music.setArtist(artist);
            music.setUri(url);
            music.setLength(duration);
            music.setAlbumid(albumid);
            music.setImage(getAlbumImage(albumid));
                Bitmap icon=BitmapUtil.getArtwork(this,id,albumid,false,true);
            searchMusic.post(new Runnable() {
                @Override
                public void run() {
                    searchMusic.setText("正在读取音乐库："+title);
                }
            });
            totalMusicList.add(music);}
        }
        if(totalMusicList.size()==0){
            searchMusic.post(new Runnable() {
                @Override
                public void run() {
                    searchMusic.setText("音乐库空空如也");
                }
            });
        }
        cursor.close();
    }


    /**
     * 根据音乐名称和艺术家来判断是否重复包含了
     *
     * @param title
     * @param artist
     * @return
     */
    private boolean isRepeat(String title, String artist) {
        for (MusicBean music : totalMusicList) {
            if (title.equals(music.getTitle()) && artist.equals(music.getArtist())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据歌曲id获取图片
     *
     * @param albumId
     * @return
     */
    private String getAlbumImage(long albumId) {
        String result = "";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    //"content://media/external/audio/albums/"
                 /*   Uri.parse("content://media/external/audio/albumart/" +
                            + albumId), new String[]{"album_art"}, null,
                    null, null);*/
                    Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + "/"
                            + albumId), new String[]{MediaStore.Audio.AlbumColumns.ALBUM_ART}, null,
                    null, null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); ) {
                result = cursor.getString(0);
                break;
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }

        return null == result ? null : result;
    }






}