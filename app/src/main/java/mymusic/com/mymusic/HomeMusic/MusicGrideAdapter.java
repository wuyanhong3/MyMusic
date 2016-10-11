package mymusic.com.mymusic.HomeMusic;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.BaseAdapter;
import android.widget.GridView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mymusic.com.mymusic.R;

/**
 * Created by Administrator on 2016/9/8.
 *
 * 动态添加内容
 *
 */
public class MusicGrideAdapter extends BaseAdapter{
    Uri albumArtUri = Uri
            .parse("content://media/external/audio/albumart");
    private LayoutInflater layoutInflater;
    private ArrayList<MusicBean> musicList=new ArrayList<>();
    private Context context;
    public MusicGrideAdapter(Context context){
        this.context=context;
        this.layoutInflater=LayoutInflater.from(context);
    }
    public void setMusicList(ArrayList<MusicBean> musicList){
        this.musicList=musicList;
        notifyDataSetChanged();
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
         MusicBean  musicBean= (MusicBean) getItem(position);
                     holderView.musicName.setText(musicBean.getTitle());
       // Bitmap icon=BitmapUtil.getArtwork(context,musicBean.getId(),musicBean.getAlbumid(),false,true);
         Bitmap    icon=decodeSampledBitmapFromResource(musicBean.getImage(),convertView.getResources(),3,110,110);
                    holderView.musicPhoto.setImageBitmap(icon == null ? BitmapFactory.decodeResource(convertView.getResources(),R.mipmap.music1):icon);
                    holderView.musicSinger.setText(musicBean.getArtist());
        return convertView;
    }
    /**
     * 设置载入图片大小，防止图片占用内存过大；
     * */

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(String name, Resources res, int resId,
                                                         int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        BitmapFactory.decodeFile(name,options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeFile(name,options);
    }
    class HolderView{
        TextView musicName,musicSinger;
        ImageView musicPhoto;
    }


}
