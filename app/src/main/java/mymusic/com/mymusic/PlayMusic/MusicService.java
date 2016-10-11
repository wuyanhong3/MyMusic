package mymusic.com.mymusic.PlayMusic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import mymusic.com.mymusic.Constant.MusicModel;
import mymusic.com.mymusic.HomeMusic.MusicBean;

/**
 *
 * 音乐服务，音乐控制方法
 * */
public class MusicService extends Service {
    public static int Current_PLAY_MODEL= MusicModel.playMusicModel.ALL_LOOPING_MODEL;     //默认音乐播放模式全部循环
    private List<MusicBean> serviceList= PlayMusicActivity.musicList;           //当前音乐列表
    private  String serviceCurrentFile;                      //当前音乐路径
    private  int serviceCurrentPostion;                      //当前音乐列表中的位置；
    private MediaPlayer serviceMediaPlayer=new MediaPlayer();
    private MusicBinder musicBinder=new MusicBinder();
    private MusicBean musicBean;

    public MusicService() {}

   interface IMusicService{
       boolean isPlay();
       void setCurrentPostion(int postion);
       int getCurrentPostion();
       int IMusicTotaleTime();
       int IMusicCurrentTime();//
       void startOrPause();//暂停
       void PreviousMusic(Context context);//上一曲
       void NextMusic(Context context);//下一曲
       void StopMusic();//停止
       void  playCurrentMusic();//播放当前资源；
       void setMusicModel(Context context);//设置循环模式
       MusicBean CurrentMusic();//返回当前播放的音乐对象

   }
    interface NotifyUpdate{
        void Update(int position);     //切换音乐时更新界面信息
    }
    public class MusicBinder extends Binder implements IMusicService{
        private NotifyUpdate notifyUpdate;
        public void setNotifyUpdate(NotifyUpdate notifyUpdate){
            this.notifyUpdate=notifyUpdate;
        }

        public MediaPlayer getMediaPlayer(){
            return serviceMediaPlayer;
        }
        @Override
        public boolean isPlay() {
           return serviceMediaPlayer.isPlaying();
        }

        @Override
        public void setCurrentPostion(int postion) {
              serviceCurrentPostion=postion;
        }

        @Override
        public int getCurrentPostion() {
            return serviceCurrentPostion;
        }


        @Override
        public int IMusicTotaleTime() {
            return serviceMediaPlayer.getDuration();
        }

        @Override
        public int IMusicCurrentTime() {
            return serviceMediaPlayer.getCurrentPosition();
        }
        @Override
        public void startOrPause() {
               if(isPlay()){
                   serviceMediaPlayer.pause();
               }else{
                   serviceMediaPlayer.start();
               }
        }
        @Override
        public void PreviousMusic(Context context) {
            serviceList=PlayMusicActivity.musicList;
            if(--serviceCurrentPostion<0){
                Toast.makeText(context,"已是第一首",Toast.LENGTH_SHORT).show();
                serviceCurrentPostion=0;
                playCurrentMusic();
                Log.v("ppppp","previousMusic>>>>>>>>已是第一首"+serviceCurrentPostion);
            }else{
                playCurrentMusic();
            }
            Log.v("ppppp","previousMusic>>>>>>>>"+serviceCurrentPostion);
        }

        @Override
        public void NextMusic(Context context) {
            serviceList=PlayMusicActivity.musicList;
            if(++serviceCurrentPostion==serviceList.size()){
                  Toast.makeText(context,"已是最后一首",Toast.LENGTH_SHORT).show();
                serviceCurrentPostion=serviceList.size()-1;
                Log.v("ppppp","NextMusic>>>>>>>>已是最后一首"+serviceCurrentPostion);
                playCurrentMusic();
            }else{
                playCurrentMusic();
            }
            Log.v("ppppp","NextMusic>>>>>>>>"+serviceCurrentPostion);
        }
        @Override
        public void StopMusic() {
            serviceMediaPlayer.reset();
                intntMusic();
                   serviceMediaPlayer.seekTo(0);
        }

        @Override
        public void playCurrentMusic() {
            serviceList= PlayMusicActivity.musicList;
            musicBean=serviceList.get(serviceCurrentPostion);
            serviceCurrentFile=musicBean.getUri();
                         Log.v("pppp","playCurrentMusic>>>>"+serviceCurrentFile.toString());
            intntMusic();
            startOrPause();
            notifyUpdate.Update(serviceCurrentPostion);
            Log.v("pppp","playCurrentMusic>>>>notifyUpdate"+serviceCurrentPostion);
        }
        @Override
        public MusicBean CurrentMusic(){
            return musicBean;
        }
        @Override
          public void setMusicModel(Context context){
            switch (Current_PLAY_MODEL){           //形成循环才能切换四种模式；
                case MusicModel.playMusicModel.LOOPING_MODEL:
                    Current_PLAY_MODEL= MusicModel.playMusicModel.ALL_LOOPING_MODEL;
                    Toast.makeText(context,"列表循环",Toast.LENGTH_SHORT).show();
                    break;
                case MusicModel.playMusicModel.ALL_LOOPING_MODEL:
                    Current_PLAY_MODEL= MusicModel.playMusicModel.ORDERING_MODEL;
                    Toast.makeText(context,"顺序播放",Toast.LENGTH_SHORT).show();
                    break;
                case MusicModel.playMusicModel.ORDERING_MODEL:
                    Current_PLAY_MODEL= MusicModel.playMusicModel.RANDOM_MODEL;
                    Toast.makeText(context,"随机播放",Toast.LENGTH_SHORT).show();
                    break;
                case MusicModel.playMusicModel.RANDOM_MODEL:
                    Current_PLAY_MODEL= MusicModel.playMusicModel.LOOPING_MODEL;
                    Toast.makeText(context,"单曲循环",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("pppp","musicService>>>>>>>"+"onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serviceMediaPlayer != null){
            serviceMediaPlayer.release();
            serviceMediaPlayer = null;
        }


    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            serviceCurrentPostion= PlayMusicActivity.mCurrentPostion;
                                          Log.v("pppp","musicService>>>>>>>"+"onStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public IBinder onBind(Intent intent) {
                 Log.v("pppp","musicService>>>>>>>"+"onBind");
        return musicBinder;
    }
    /**
     * 初始化音乐；
     * */
    public void intntMusic(){
        if(serviceMediaPlayer==null)
            serviceMediaPlayer=new MediaPlayer();
        try {
            serviceMediaPlayer.reset();
                  serviceMediaPlayer.setDataSource("file://"+serviceCurrentFile);
                                           Log.v("pppp","intntMusic>>>>>>>>>"+serviceCurrentFile);
            serviceMediaPlayer.prepare();
            serviceMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    singleModel();
                    orderModel();
                    randomModel();
                    listModel();
                }
            });

    }catch(IOException e){
            e.printStackTrace();
        }

    }
    /**
     * 单曲循环；
     * */
    public void singleModel(){
        if(Current_PLAY_MODEL==MusicModel.playMusicModel.LOOPING_MODEL){
            if(serviceMediaPlayer!=null){
                serviceMediaPlayer.setLooping(true);}
                musicBinder.playCurrentMusic();
        }else{
            if(serviceMediaPlayer!=null){
                serviceMediaPlayer.setLooping(false);
            }
        }
    }
    /**
     * 列表循环；
     * */
    public void listModel(){
        if(Current_PLAY_MODEL==MusicModel.playMusicModel.ALL_LOOPING_MODEL){
            if(++serviceCurrentPostion==serviceList.size()){
                serviceCurrentPostion=0;
            }
            musicBinder.playCurrentMusic();
        }
    }
    /**
     * 随机播放；
     * */
    public void randomModel(){
        if(Current_PLAY_MODEL==MusicModel.playMusicModel.RANDOM_MODEL){
            serviceCurrentPostion=new Random().nextInt(serviceList.size());
            musicBinder.playCurrentMusic();
        }
    }
    /**
     * 顺序播放；
     * */
    public void orderModel(){
        if(Current_PLAY_MODEL==MusicModel.playMusicModel.ORDERING_MODEL){
            musicBinder.NextMusic(getBaseContext());
        }
    }

}
