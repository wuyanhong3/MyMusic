package mymusic.com.mymusic.MusicList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mymusic.com.mymusic.HomeMusic.BitmapUtil;
import mymusic.com.mymusic.HomeMusic.HomeMainActivity;
import mymusic.com.mymusic.HomeMusic.HomeMusicAdapter;
import mymusic.com.mymusic.HomeMusic.MusicBean;
import mymusic.com.mymusic.HomeMusic.MusicGrideAdapter;
import mymusic.com.mymusic.PlayMusic.PlayMusicActivity;
import mymusic.com.mymusic.R;

/**
 *
 * 全部音乐和收藏音乐列表界面
 * */
public class MusicListMainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
             private ViewPager musicViewpager;
             private   ListView totalMusic;
             private   ArrayList<MusicBean> totalMusicData = HomeMainActivity.totalMusicList;
             private   ArrayList<MusicBean> collectMusicData=HomeMainActivity.collectMusicList;
             private ImageButton searchMusicBT;
             private AutoCompleteTextView mSearchMusic;
             private ImageButton menu;
             private PopupWindow popupWindow;
             private View context;
             private LinearLayout linearLayout;
             private TextView totalMusicList,collectMusicList;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.musicl_list_lauout);
        getID();
        context= LayoutInflater.from(this).inflate(R.layout.pupupwindows_layout,null);
        linearLayout= (LinearLayout) context.findViewById(R.id.music_home_Lin);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent=new Intent(MusicListMainActivity.this,HomeMainActivity.class);
                startActivity(intent);
            }
        });

        popupWindow=new PopupWindow(context);
        setPopup(popupWindow,context);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupWindow.isShowing()){
                    popupWindow.dismiss();
                }else{
                    popupWindow.showAtLocation(v,0,0,0);//显示左侧菜单
                }
            }
        });
        setViewPager();
        totalMusic.setOnItemClickListener(this);
        searchMusicBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  if(mSearchMusic.getVisibility()==View.INVISIBLE){
                        mSearchMusic.setVisibility(View.VISIBLE);
                  }else{
                      mSearchMusic.setVisibility(View.INVISIBLE);
                  }
            }
        });

        musicViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               if(position==0){
                   totalMusicList.setSelected(true);
                   totalMusicList.setFocusable(true);
                   collectMusicList.setSelected(false);
                   collectMusicList.setFocusable(false);
               }else{
                   totalMusicList.setSelected(false);
                   totalMusicList.setFocusable(false);
                   collectMusicList.setSelected(true);
                   collectMusicList.setFocusable(true);
               }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        totalMusicList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicViewpager.setCurrentItem(0);
            }
        });
        collectMusicList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicViewpager.setCurrentItem(1);
            }
        });
    }
    /**
     * 初始化控件；
     * */
    public void getID(){
        musicViewpager= (ViewPager) findViewById(R.id.music_list_viewpager);
        totalMusicList= (TextView) findViewById(R.id.music_list_total);
        totalMusicList.setSelected(true);
        collectMusicList= (TextView) findViewById(R.id.music_list_collect);
        mSearchMusic = (AutoCompleteTextView) findViewById(R.id.found_music_edit);
        searchMusicBT = (ImageButton) findViewById(R.id.found_music_button);
        menu= (ImageButton) findViewById(R.id.music_menu);
         mSearchMusic.setVisibility(View.INVISIBLE);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,getAutoCompletData());
                              mSearchMusic.setAdapter(arrayAdapter);
    }
    public void setPopup(PopupWindow popupWindow, View context){
        //实例PopupWindow；
        popupWindow.setContentView(context);
        popupWindow.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setWidth(750);
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x06060));
    }

    private String[] getAutoCompletData(){
        String[] data=new String[totalMusicData.size()];
                 for(int i=0;i<data.length;i++){
                     data[i]= totalMusicData.get(i).getTitle();
                 }

        return data;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(popupWindow.isShowing()){
            popupWindow.dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent   intent = new Intent(this,PlayMusicActivity.class);
                 intent.putParcelableArrayListExtra("MUSIC_LIST", totalMusicData);
                 intent.putExtra("CURRENT_POSITION",position);
                 intent.putExtra("TYPE",true);
                                Log.v("searchMList",totalMusicData.size()+">>>>position:"+position);
                  startActivity(intent);
               HomeMainActivity.type=false;
    }

    private void setViewPager(){
         LayoutInflater layoutInflater=LayoutInflater.from(this);
         View  totalMusicView=layoutInflater.inflate(R.layout.all_music_layout,null);       //全部音乐
                 totalMusic = (ListView) totalMusicView.findViewById(R.id.all_music_list);
                        MyMusicAdapter myMusicAdapter =new MyMusicAdapter(this);
                                totalMusic.setAdapter(myMusicAdapter);
                                       myMusicAdapter.setMusicList(totalMusicData);

        View collectMusicView=layoutInflater.inflate(R.layout.collect_music_layout,null);        //收藏音乐
                final GridView collectMusic= (GridView) collectMusicView.findViewById(R.id.collect_music_list);
                          MusicGrideAdapter homeMusicAdapter =new MusicGrideAdapter(this);
                                   collectMusic.setAdapter(homeMusicAdapter);
                                             homeMusicAdapter.setMusicList(collectMusicData);
                                                  collectMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                      @Override
                                                      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                          Intent intent = new Intent(MusicListMainActivity.this,PlayMusicActivity.class);
                                                                  intent.putParcelableArrayListExtra("MUSIC_LIST", collectMusicData);
                                                                   intent.putExtra("CURRENT_POSITION",position);
                                                                   intent.putExtra("TYPE",true);
                                                                               Log.v("searchMList",collectMusicData.size()+">>>>position:"+position);
                                                                    startActivity(intent);
                                                                     HomeMainActivity.type=false;
                                                      }
                                                  });

                          List<View> list=new ArrayList<>();
                                     list.add(totalMusicView);
                                     list.add(collectMusicView);
                             MyViewPager myViewPager=new MyViewPager(list);
                                 musicViewpager.setAdapter(myViewPager);
    }
/**
 *
 * collect
 *
 * */
private class MyViewPager extends PagerAdapter {  //自定义Viewpager适配器；
    List<View> list = new ArrayList<>();

    public MyViewPager(List<View> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        View view = list.get(position);
        container.addView(view);
        return view;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position));
    }
}

    /**
     *
     * 自定义音乐列表适配器(全部音乐)；
     * */
}
class MyMusicAdapter extends BaseAdapter{
    private LayoutInflater layoutInflater;
    private ArrayList<MusicBean> musicList=new ArrayList<>();
    private Context context;
    public MyMusicAdapter(Context context){
        this.context=context;
        this.layoutInflater=LayoutInflater.from(context);
    }
    public void setMusicList(ArrayList<MusicBean> musicList){
        this.musicList=musicList;
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
            convertView=layoutInflater.inflate(R.layout.musicadpater_list_layout,null);
            holderView=new HolderView();
            holderView.musicName= (TextView) convertView.findViewById(R.id.music_name);
            holderView.musicPhoto= (ImageView) convertView.findViewById(R.id.music_photo);
            holderView.musicPath= (TextView) convertView.findViewById(R.id.music_path);
            convertView.setTag(holderView);
        }else{
            holderView= (HolderView) convertView.getTag();
        }
        MusicBean musicBean= (MusicBean) getItem(position);
         holderView.musicName.setText(musicBean.getTitle());
       // Bitmap icon = BitmapFactory.decodeFile(musicBean.getImage());
       // Bitmap icon= BitmapUtil.getArtwork(context,musicBean.getId(),musicBean.getAlbumid(),false,false);
           Bitmap icon=decodeSampledBitmapFromResource(musicBean.getImage(),convertView.getResources(),4,30,32);
             holderView.musicPhoto.setImageBitmap(icon == null ? BitmapFactory.decodeResource(convertView.getResources(),R.mipmap.music1):icon);
               //holderView.musicPhoto.setImageBitmap(icon);
                holderView.musicPath.setText(musicBean.getUri());
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
        // return BitmapFactory.decodeResource(res, resId, options);
        return BitmapFactory.decodeFile(name,options);
    }
    class HolderView{
        TextView musicName,musicPath;
        ImageView musicPhoto;
    }
}