package mymusic.com.mymusic.HomeMusic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mymusic.com.mymusic.R;

/**
 *
 * 音乐列表GridView配置器(滑动动态加载专辑封面)
 */
public class HomeMusicAdapter extends BaseAdapter {
    //GridView中可见的第一张图片的下标
    private int mFirstVisibleItem;
    //GridView中可见的图片的数量
    private int mVisibleItemCount;
    //记录是否是第一次进入该界面
    private boolean isFirstEnterThisActivity = true;
    private LruCache<String, Bitmap> mMemoryCache;


    private LayoutInflater layoutInflater;
    private ArrayList<MusicBean> musicList=new ArrayList<>();
    private Context context;
    private GridView gridView;
    public HomeMusicAdapter(Context context,GridView gridView){
        this.gridView = gridView;
        gridView.setOnScrollListener(new ScrollListenerImpl());
        this.context=context;
        this.layoutInflater=LayoutInflater.from(context);
        int maxMemory = (int) Runtime.getRuntime().maxMemory();      // 获取应用程序最大可用内存
        int cacheSize = maxMemory / 14;                                // 设置图片缓存大小为程序最大可用内存的1/8
        mMemoryCache= new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
    }

    public void setMusicList(ArrayList<MusicBean> musicList){
        this.musicList=musicList;
             notifyDataSetChanged();
    }
    /**
     * 将一张图片存储到LruCache中。
     *
     * @param key
     *            LruCache的键，这里传入图片的URL地址。
     * @param bitmap
     *            LruCache的键，这里传入从网络上下载的Bitmap对象。
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }
    /**
     * 从LruCache中获取一张图片，如果不存在就返回null。
     *
     * @param key
     *            LruCache的键，这里传入图片的URL地址。
     * @return 对应传入键的Bitmap对象，或者null。
     */
    public Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }
    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return musicList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView holderView;
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.home_musiclist_layout,null);
            holderView=new HolderView();
            holderView.musicName= (TextView) convertView.findViewById(R.id.home_music_list_title);
            holderView.musicPhoto= (ImageView) convertView.findViewById(R.id.home_music_list_picture);
            holderView.musicSinger= (TextView) convertView.findViewById(R.id.home_music_list_singer);
            convertView.setTag(holderView);
        }else{
            holderView= (HolderView) convertView.getTag();
        }
        MusicBean musicBean= (MusicBean) getItem(position);
       // holderView.musicName.setText(musicBean.getTitle());
        holderView.musicPhoto.setTag(musicBean.getImage());
        holderView.musicName.setTag(musicBean.getTitle());
        holderView.musicSinger.setTag(musicBean.getArtist());
     //   Glide.with(context).load(musicBean.getImage()).into(holderView.musicPhoto).onLoadFailed(null,convertView.getResources().getDrawable(R.mipmap.music1));
        // Bitmap icon=decodeSampledBitmapFromResource(musicBean.getImage(),convertView.getResources(),3,100,100);
            // addBitmapToMemoryCache(musicBean.getTitle(),icon);
      //  holderView.musicPhoto.setImageBitmap(icon == null ? BitmapFactory.decodeResource(convertView.getResources(),R.mipmap.music1):getBitmapFromMemoryCache(musicBean.getTitle()));
      // holderView.musicSinger.setText(musicBean.getArtist());
        return convertView;
    }


    class HolderView{
        TextView musicName,musicSinger;
        ImageView musicPhoto;
    }
    /**
     * 为GridView的item加载图片
     * @param firstVisibleItem GridView中可见的第一张图片的下标
     * @param visibleItemCount GridView中可见的图片的数量
     */
    private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
        try {
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
               MusicBean musicBean=musicList.get(i);
                Bitmap bitmap = getBitmapFromMemoryCache(musicBean.getImage());
                if (bitmap == null) {
                    Log.v("pppp",">>>>loadBitmaps>>>"+musicBean.getImage());
                    bitmap=BitmapFactory.decodeFile(musicList.get(i).getImage());
                    ImageView imageView = (ImageView) gridView.findViewWithTag(musicBean.getImage());
                    TextView name= (TextView) gridView.findViewWithTag(musicBean.getTitle());
                    TextView singer= (TextView) gridView.findViewWithTag(musicBean.getArtist());
                    if(name!=null&&singer!=null){
                        name.setText(musicList.get(i).getTitle());
                        singer.setText(musicList.get(i).getArtist());
                    }
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                    //将从SDCard读取的图片添加到LruCache中
                    addBitmapToMemoryCache(musicBean.getImage(),bitmap);
                } else {
                  Log.v("pppp",">>>>>>>>>>从缓存中取出"+musicBean.getTitle());
                    //依据Tag找到对应的ImageView显示图片
                    ImageView imageView = (ImageView) gridView.findViewWithTag(musicBean.getImage());
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }else{
                        imageView.setImageResource(R.mipmap.music1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ScrollListenerImpl implements AbsListView.OnScrollListener {
        /**
         * 通过onScrollStateChanged获知:每次GridView停止滑动时加载图片
         * 但是存在一个特殊情况:
         * 当第一次入应用的时候,此时并没有滑动屏幕的操作即不会调用onScrollStateChanged,但应该加载图片.
         * 所以在此处做一个特殊的处理.
         * 即代码:
         * if (isFirstEnterThisActivity && visibleItemCount > 0) {
         *      loadBitmaps(firstVisibleItem, visibleItemCount);
         *      isFirstEnterThisActivity = false;
         *    }
         *
         * ------------------------------------------------------------
         *
         * 其余的都是正常情况.
         * 所以我们需要不断保存:firstVisibleItem和visibleItemCount
         * 从而便于中在onScrollStateChanged()判断当停止滑动时加载图片
         *
         */
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
            mFirstVisibleItem = firstVisibleItem;
            mVisibleItemCount = visibleItemCount;
            if (isFirstEnterThisActivity && visibleItemCount > 0) {
               Log.v("pppp","第一次进入该界面");
                loadBitmaps(firstVisibleItem, visibleItemCount);
                isFirstEnterThisActivity = false;
            }
        }

        /**
         *  GridView停止滑动时下载图片
         *  其余情况下取消所有正在下载或者等待下载的任务
         */
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE) {
                Log.v("pppp","---> GridView停止滑动  mFirstVisibleItem="+mFirstVisibleItem+",mVisibleItemCount="+mVisibleItemCount);
                loadBitmaps(mFirstVisibleItem, mVisibleItemCount);
            }
        }
    }
}
