package mymusic.com.mymusic.PlayMusic;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import mymusic.com.mymusic.CollectMusicSQ.collectMusicBean;
import mymusic.com.mymusic.Constant.MusicModel;
import mymusic.com.mymusic.HomeMusic.HomeMainActivity;
import mymusic.com.mymusic.HomeMusic.MusicBean;
import mymusic.com.mymusic.CollectMusicSQ.DatabaseHelper;
import mymusic.com.mymusic.HomeMusic.MusicGrideAdapter;
import mymusic.com.mymusic.MusicList.MusicListMainActivity;
import mymusic.com.mymusic.R;

/**
 *
 * 音乐播放界面，notification设置
 * */
public class PlayMusicActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener{
    public static final int Handler_TYPE = 1;

    private static final String PERV="com.HomeMainActivity_PERV";   //用于通知栏控制音乐的广播
    private static final String PURSE="HomeMainActivity_PUSER";
    private static final String NEXT="HomeMainActivity_NEXT";
    private static final String SET_HOME_MESS="HomeMainActivity_MESS";

    private DatabaseHelper mDataBaseHelper;  //保存收藏音乐
    private SQLiteDatabase sqLiteDatabase;
    private final static String DATABASE_TABLE_NAME="collectMusic";

    private NotificationManager managerMP;    //通知栏
    private RemoteViews remoteView;
    private NotificationCompat.Builder notifyMP3Builder;

    public static int mCurrentPostion;  //当前播放的音乐在列表中的位置
    public static int mCurrentProgress;  //进度条
    public static ArrayList<MusicBean> musicList = new ArrayList<>();  //当前播放列表
    public static MusicService.MusicBinder musicBinder;

    private ImageButton musicStar, musicStop, musicPerv, musicNext, musicModel;//上一曲，下一曲，暂停，播放模式
    private ImageButton showList, IntentHome;  //跳转主界面，显示音乐列表

    private TextView totalTime, currentTime, currentMusicTitle, currentMusicSinger;
    private ViewPager musicViewPager; //显示专辑
    private MyPagerAdapter myPagerAdapter; //Viewpager适配器
    private SeekBar musicSeek;  //进度条
    private CheckBox myLike;   //收藏图标

    private MediaPlayer mMediaPlayer;
    private Boolean isFlag = true;
    private Intent intent;

    public static ArrayList<MusicBean> mLikeList;//收藏音乐列表；
    private SimpleDateFormat mMusicData = new SimpleDateFormat("mm:ss");


    private BroadcastReceiver HomeButton=new BroadcastReceiver() {         //接收通知栏发来的广播切换音乐，更新界面；
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMess=new Intent();
                   intentMess.setAction(SET_HOME_MESS);
            switch (intent.getAction()){
                case PERV:
                    musicBinder.PreviousMusic(PlayMusicActivity.this);
                                                      Log.v("pppp","onReceive>>>>>>>>>>PERV");
                    break;

                case PURSE:
                    musicBinder.startOrPause();
                      setMusicMess();
                          sendBroadcast(intentMess);
                                                      Log.v("pppp","onReceive>>>>>>>>>>PUSER");
                    break;

                case NEXT:
                    musicBinder.NextMusic(PlayMusicActivity.this);
                                                             Log.v("pppp","onReceive>>>>>>>>>>NEXT");
                    break;
            }
        }
    };

    private Intent intentMusicService;  //音乐服务
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicBinder = (MusicService.MusicBinder) service;

           musicBinder.setNotifyUpdate(new MusicService.NotifyUpdate() {
                @Override
                public void Update(int position) {
                    if(remoteView!=null){
                        setMusicMess();
                        Intent intentMess=new Intent();
                        intentMess.setAction(SET_HOME_MESS);
                        sendBroadcast(intentMess);
                    }
                    Log.v("ppppu","update>>>>>position:"+position+">>>>>>mCurrentPostion"+musicBinder.getCurrentPostion());
                }
            });
            musicBinder.playCurrentMusic();
            setNotify();
            setMusicMess();
            new UpdateSeek().start();
            totalTime.setText(mMusicData.format(new Date(musicBinder.IMusicTotaleTime())));

                                                           Log.v("pppp", "musicPlayer>>>>>>>onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
                                                         Log.v("pppp", "musicPlayer>>>>>>>onServiceDisconnected");
            musicBinder = null;
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {    //更新进度条
            switch (msg.arg1) {
                case Handler_TYPE:
                    int currentPostion = msg.arg2;
                    musicSeek.setProgress(currentPostion);
                    currentTime.setText(mMusicData.format(new Date(currentPostion)));
                    break;
            }
        }
    };

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_main);
        mLikeList = HomeMainActivity.collectMusicList;//收藏音乐列表；
        mDataBaseHelper = new DatabaseHelper(this);
        sqLiteDatabase = mDataBaseHelper.getReadableDatabase();

        getID();
        setListener();
        myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        musicViewPager.setAdapter(myPagerAdapter);

        intent = getIntent();
        musicList = intent.getParcelableArrayListExtra("MUSIC_LIST");
        mCurrentPostion = intent.getIntExtra("CURRENT_POSITION", 0);
                                Log.v("pppp----musicList", "musicPlayer>>>>>>>onCreate" + mCurrentPostion + "" + "------" + musicList.get(mCurrentPostion).getTitle());
        myPagerAdapter.setDataList(musicList);

        intentMusicService= new Intent(this, MusicService.class);
        startService(intentMusicService);
        bindService(intentMusicService, serviceConnection, BIND_AUTO_CREATE);     //绑定服务




        myLike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {         //收藏音乐checkbox监听
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLikeList=HomeMainActivity.collectMusicList;
                MusicBean musicBean = getCurrentMusicBean();
                boolean tag=false;
                for(MusicBean musicBean1:mLikeList){          //判断当前音乐是否在收藏列表中
                    if(musicBean.getTitle().equals(musicBean1.getTitle())){
                        tag=true;
                        break;
                    }else{
                        tag=false;
                    }
                }
                if (isChecked) {
                    if(!tag){                  //checkbox未选中状态并且音乐不在收藏列表中则添加到数据库和收藏播放列表中
                    HomeMainActivity.collectMusicList.add(musicBean);

                    myLike.setSelected(true);
                    ContentValues contentValues=new ContentValues();
                                  contentValues.put(collectMusicBean.MUSIC_PATH,musicBean.getUri());
                                  contentValues.put(collectMusicBean.MUSIC_IMAGE_PATH,musicBean.getImage());
                                  contentValues.put(collectMusicBean.MUSIC_NAME,musicBean.getTitle());
                                  contentValues.put(collectMusicBean.MUSIC_SINGER,musicBean.getArtist());
                                  contentValues.put(collectMusicBean.MUSIC_ALBUM_ID,musicBean.getAlbumid());
                                  contentValues.put(collectMusicBean.MUSIC_ID,musicBean.getId());
                                  contentValues.put(collectMusicBean.MUSIC_COLLECT,"true");
                                        sqLiteDatabase.insert(DATABASE_TABLE_NAME,null,contentValues);

                                             Toast.makeText(PlayMusicActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                                                        Log.v("pppp",musicBean.getTitle()+">>>>>"+mLikeList.size()+"isChecked>>>"+isChecked);}

                } else {
                    if(tag){        //checkbox非选中状态并且音乐在收藏列表中则移除

                     for(MusicBean music:HomeMainActivity.collectMusicList){
                         if(music.getTitle().equals(musicBean.getTitle())){
                                   HomeMainActivity.collectMusicList.remove(music);
                                              Log.v("pppp","remove>>>>>>>>>"+music.getTitle());
                             break;
                         }
                     }

                     String whereClause = collectMusicBean.MUSIC_NAME+" = ? and "+collectMusicBean.MUSIC_PATH+" = ?";
                     String[] whereArg = new String[]{musicBean.getTitle(),musicBean.getUri()};
                        sqLiteDatabase.delete(DATABASE_TABLE_NAME,whereClause,whereArg);
                         myLike.setSelected(false);
                               Toast.makeText(PlayMusicActivity.this, "取消收藏", Toast.LENGTH_SHORT).show();
                                                 Log.v("pppp",musicBean.getTitle()+">>>>>"+mLikeList.size()+"isChecked>>>"+isChecked);}
                }
            }
        });
      //  musicViewPager.setOffscreenPageLimit(1);
        musicViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                   @Override
                   public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                   @Override
                   public void onPageSelected(int position) {
                       if(musicBinder.getCurrentPostion()!=position){
                           musicBinder.setCurrentPostion(position);
                           musicBinder.playCurrentMusic();
                           setMusicMess();
                          }
                                   Log.v("ppppps",">>>>>>>select"+position+">>>>>>id:"+musicBinder.getCurrentPostion());
                   }

                   @Override
                   public void onPageScrollStateChanged(int state) {}
                });

                                  Log.v("pppp", "musicPlayer>>>>>>>onCreate");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
       public static void playCurrent(int position){
           musicBinder.setCurrentPostion(position);
           musicBinder.playCurrentMusic();
       }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.v("pppp", "musicPlayer>>>>>>>onTouch");
        return true;
    }

    /**
     * 修改launchMode为singleInstance需重写onNewIntent(Intent intent)方法才能接收值；
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        intent = getIntent();
        if (intent.getBooleanExtra("TYPE",true)) {
            musicList = intent.getParcelableArrayListExtra("MUSIC_LIST");
            myPagerAdapter.setDataList(musicList);
            mCurrentPostion = intent.getIntExtra("CURRENT_POSITION", 0);
            Log.v("pppp----musicList", "musicPlayer>>>>>>>onNewIntent" + musicList.size() + "" + "------" + musicList.get(mCurrentPostion).getTitle());
            musicBinder.setCurrentPostion(mCurrentPostion);
            musicBinder.playCurrentMusic();
        }
    }


    public static MusicService.MusicBinder getMusicBinder() {
        return musicBinder;
    }
    /**
     * 初始化控件；
     */
    public void getID() {
        myLike = (CheckBox) findViewById(R.id.music_like);
        showList = (ImageButton) findViewById(R.id.music_show_list_button);
        musicPerv = (ImageButton) findViewById(R.id.music_last);
        musicNext = (ImageButton) findViewById(R.id.music_next);
        musicStar = (ImageButton) findViewById(R.id.music_start);
        musicStop = (ImageButton) findViewById(R.id.music_stop);
        musicViewPager= (ViewPager) findViewById(R.id.play_music_viewpager);

        totalTime = (TextView) findViewById(R.id.music_total_time);
        currentTime = (TextView) findViewById(R.id.music_current_time);
        musicSeek = (SeekBar) findViewById(R.id.music_seekbar);
        musicModel = (ImageButton) findViewById(R.id.music_model);
        currentMusicTitle = (TextView) findViewById(R.id.music_title);
        currentMusicSinger = (TextView) findViewById(R.id.music_singer);
        IntentHome = (ImageButton) findViewById(R.id.intent_home);
    }

    /**
     * 注册监听；
     */
    public void setListener() {
        showList.setOnClickListener(this);
        musicPerv.setOnClickListener(this);
        musicNext.setOnClickListener(this);
        musicStop.setOnClickListener(this);
        musicStar.setOnClickListener(this);
        musicModel.setOnClickListener(this);
        IntentHome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.music_show_list_button:
                Intent intent = new Intent(this, MusicListMainActivity.class);
                startActivity(intent);
                 overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                break;

            case R.id.music_start:
                if (musicBinder.isPlay()) {
                    musicStar.setImageResource(R.mipmap.startnotify);
                } else {
                    musicStar.setImageResource(R.mipmap.pausenotify);
                }
                musicBinder.startOrPause();
                setMusicMess();
                break;

            case R.id.music_stop:
                musicBinder.StopMusic();
                setMusicMess();
                break;

            case R.id.music_last:
                musicBinder.PreviousMusic(this);

                break;

            case R.id.music_next:
                musicBinder.NextMusic(this);

                break;

            case R.id.music_model:
                musicBinder.setMusicModel(this);
                switch (MusicService.Current_PLAY_MODEL) {
                    case MusicModel.playMusicModel.LOOPING_MODEL:    //单曲循环
                        musicModel.setImageResource(R.mipmap.loop);
                        break;
                    case MusicModel.playMusicModel.ALL_LOOPING_MODEL:   //全部循环
                        musicModel.setImageResource(R.mipmap.allloop);
                        break;
                    case MusicModel.playMusicModel.ORDERING_MODEL:      //顺序播放
                        musicModel.setImageResource(R.mipmap.order);
                        break;
                    case MusicModel.playMusicModel.RANDOM_MODEL:      //随机播放
                        musicModel.setImageResource(R.mipmap.random);
                        break;
                }
                break;

            case R.id.intent_home:
                Intent intentH = new Intent(this, HomeMainActivity.class);
                startActivity(intentH);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
         managerMP= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
             registerReceiver();
        if(musicBinder!=null){
            setMusicMess();
        }
                     Log.v("pppp", "musicPlayer>>>>>>>onResume");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!musicBinder.isPlay()){           //如果音乐停止时按返回键，播放界面会被销毁
           moveTaskToBack(false);
            onDestroy();
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {          //音乐处于播放状态，按返回键播放界面会处于onResume状态，
            moveTaskToBack(true);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    /**
     * 销毁音乐
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            isFlag = false;
        }
        this.stopService(intentMusicService);
        unbindService(serviceConnection);
        SharedPreferences music=getSharedPreferences("endMusic",MODE_PRIVATE);           //保存退出时播放的音乐；
        SharedPreferences.Editor editor=music.edit();
       /* if(musicList==mLikeList){
            editor.putInt("LIST_TYPE",0);  //退出时播放的列表为收藏列表
            Log.v("ppppd", "musicPlayer>>>000>>>>onDestroy"+mCurrentPostion);
        }else{
            editor.putInt("LIST_TYPE",1);
            Log.v("ppppd", "musicPlayer>>>1111>>>>onDestroy"+mCurrentPostion);
        }*/

        editor.putInt("POSITION",mCurrentPostion);
        editor.commit();
        managerMP.cancel(100);    //取消通知栏

    }

    /**
     * 设置播放界面音乐信息；
     */
    private void setMusicMess() {
        mLikeList=HomeMainActivity.collectMusicList;
         MusicBean musicBean = musicBinder.CurrentMusic();
        // boolean tag=mLikeList.contains(musicBean);
           boolean tag=false;
          for(MusicBean musicBean1:HomeMainActivity.collectMusicList){
              if(musicBean.getTitle().equals(musicBean1.getTitle())){
                 tag=true;
                  break;
              }else{
                   tag=false;
              }
          }
        if(tag){                           //判断是否为收藏音乐
             myLike.setSelected(tag);
             myLike.setChecked(tag);
                                             Log.v("pppp","Play>>>>setMusic>>>>"+tag+">>>>"+mLikeList.size()+musicBean+"");
        }else{
             myLike.setSelected(tag);
             myLike.setChecked(tag);
                                              Log.v("pppp","Play>>>>setMusic>>>>"+tag+">>>>"+mLikeList.size());
        }

        if (musicBinder.isPlay()) {
            musicStar.setImageResource(R.mipmap.startnotify);
        } else {
            musicStar.setImageResource(R.mipmap.pausenotify);
        }

        currentMusicTitle.setText(musicBean.getTitle());
        currentMusicSinger.setText(musicBean.getArtist());

        int item=musicBinder.getCurrentPostion();
        if( musicViewPager.getCurrentItem()!=item){
           musicViewPager.setCurrentItem(musicBinder.getCurrentPostion());

        }
                                                                Log.v("ppppt","Play>>>>setMusic>>>>"+">>>>"+musicBinder.getCurrentPostion());

        musicSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {         //进度条控制音乐进度
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentProgress = progress;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicBinder.getMediaPlayer().seekTo(mCurrentProgress);
            }
        });

        int duration = musicBinder.getMediaPlayer().getDuration();
        musicSeek.setMax(duration);
        totalTime.setText(mMusicData.format(new Date(duration)));
        setNotifyMess();
    }

    /**
     * 返回当前播放对象
     */
    public static MusicBean getCurrentMusicBean() {
        if (musicBinder != null) {
            return musicBinder.CurrentMusic();
        } else {
            return null;
        }
    }
    /**
     * 注册接收播放器通知栏发送的音乐控制消息；
     * */
    public void registerReceiver(){
        IntentFilter intentFilter1=new IntentFilter(PERV);
        IntentFilter intentFilter2=new IntentFilter(PURSE);
        IntentFilter intentFilter3=new IntentFilter(NEXT);

        registerReceiver(HomeButton,intentFilter2);
        registerReceiver(HomeButton,intentFilter1);
        registerReceiver(HomeButton,intentFilter3);
    }

    /**
     * 更新音乐进度条；
     */
    class UpdateSeek extends Thread {
        @Override
        public void run() {
            while (isFlag) {
                if (musicBinder.isPlay()) {
                    int currentPostion = musicBinder.IMusicCurrentTime();
                    Message message = Message.obtain();
                    message.arg1 = Handler_TYPE;
                    message.arg2 = currentPostion;
                    handler.sendMessage(message);
                    SystemClock.sleep(1000);
                }
            }
        }
    }


    /**
     * 音乐通知栏
     * */
   @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
   private void setNotify(){
       final MusicBean musicBean = musicBinder.CurrentMusic();
       notifyMP3Builder=new NotificationCompat.Builder(this);
       remoteView=new RemoteViews(getPackageName(),R.layout.music_notify_layout);
       notifyMP3Builder.setContent(remoteView);
       notifyMP3Builder.setSmallIcon(R.drawable.red);
       notifyMP3Builder.setTicker("正在播放:"+musicBean.getTitle());
       notifyMP3Builder.setOngoing(true);

       Intent intentPerv= new Intent(PERV);                                                         //上一曲
       PendingIntent pendingIntentPerv = PendingIntent.getBroadcast(this,100,intentPerv,PendingIntent.FLAG_UPDATE_CURRENT);
       remoteView.setOnClickPendingIntent(R.id.music_notify_last,pendingIntentPerv);

       Intent intentPuse= new Intent(PURSE);                                                         //暂停
       PendingIntent pendingIntentPuse = PendingIntent.getBroadcast(this,100,intentPuse,PendingIntent.FLAG_UPDATE_CURRENT);
       remoteView.setOnClickPendingIntent(R.id.music_notify_star,pendingIntentPuse);

       Intent intentNext= new Intent(NEXT);                                                         //下一曲
       PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this,100,intentNext,PendingIntent.FLAG_UPDATE_CURRENT);
       remoteView.setOnClickPendingIntent(R.id.music_notify_next,pendingIntentNext);

       Intent intentShow=new Intent(this,PlayMusicActivity.class);                               //点击通知栏音乐封面跳转到播放界面
               intentShow.putExtra("TYPE",false);
       PendingIntent pendingIntent=PendingIntent.getActivity(this,100,intentShow,PendingIntent.FLAG_UPDATE_CURRENT);
       remoteView.setOnClickPendingIntent(R.id.music_notify_picture,pendingIntent);
       overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                 setNotifyMess();
   }

    /**
     *
     * 刷新通知栏
     * */
private void setNotifyMess(){
     MusicBean musicBean = musicBinder.CurrentMusic();
    if(musicBinder.isPlay()){
        remoteView.setImageViewResource(R.id.music_notify_star,R.mipmap.startnotify);
        notifyMP3Builder.setOngoing(true);                        //处于音乐播放状态，滑动不会删除notification；
    }else {
        remoteView.setImageViewResource(R.id.music_notify_star,R.mipmap.pausenotify);
        notifyMP3Builder.setOngoing(false);
    }

    remoteView.setTextViewText(R.id.music_notify_name,musicBean.getTitle());
    remoteView.setTextViewText(R.id.music_notify_singer,musicBean.getArtist());

    Bitmap icon= MusicGrideAdapter.decodeSampledBitmapFromResource(musicBean.getImage(),getResources(),3,120,120);  //设置通知栏音乐图片

    if(icon!=null){
             remoteView.setImageViewBitmap(R.id.music_notify_picture,icon);
       }else{
        remoteView.setImageViewResource(R.id.music_notify_picture,R.mipmap.music1);
    }

    Notification notification = notifyMP3Builder.build();           //Notification必须在NotificationManager后实例；
    if(Build.VERSION.SDK_INT>16){                                   //android大于16可设置notification高度大于64dp；
        notification.bigContentView = remoteView;
    }
                  notification.contentView = remoteView;
                  managerMP.notify(100,notification);
}
    /**
     *
     * Viewpager适配器
     * */
    class MyPagerAdapter extends FragmentStatePagerAdapter {
        public ArrayList<MusicBean> List = new ArrayList<>();
        public void setDataList(ArrayList<MusicBean> List ){
               this.List=List;
            notifyDataSetChanged();
        }
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {

            return MusicPictureFragment.newInstance(this.List.get(position).getImage());
        }

        @Override
        public int getCount() {
            return this.List.size();
        }
    }











        @Override
        public void onStart() {
            super.onStart();

            // ATTENTION: This was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidS1tudio for more information.
            client.connect();
            Action viewAction = Action.newAction(
                    Action.TYPE_VIEW, // TODO: choose an action type.
                    "PlayMusic Page", // TODO: Define a title for the content shown.
                    // TODO: If you have web page content that matches this app activity's content,
                    // make sure this auto-generated web page URL is correct.
                    // Otherwise, set the URL to null.
                    Uri.parse("http://host/path"),
                    // TODO: Make sure this auto-generated app URL is correct.
                    Uri.parse("android-app://mymusic.com.mymusic/http/host/path")
            );
            AppIndex.AppIndexApi.start(client, viewAction);
        }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "PlayMusic Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://mymusic.com.mymusic/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
