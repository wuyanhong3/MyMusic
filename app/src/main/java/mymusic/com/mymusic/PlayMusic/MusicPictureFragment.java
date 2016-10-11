package mymusic.com.mymusic.PlayMusic;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import mymusic.com.mymusic.R;


/**
 * A simple {@link Fragment} subclass.
 * 播放界面音乐专辑Fragment
 *
 */
public class MusicPictureFragment extends Fragment {
      private static final String URI="URI";
      private  ImageView musicPicture;
      private   View view;
    public MusicPictureFragment() {}
public static Fragment newInstance(String uri){
             Bundle bundle=new Bundle();
             bundle.putString(URI,uri);
    MusicPictureFragment music=new MusicPictureFragment();
                         music.setArguments(bundle);
    return music;
}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            Bundle bundle=getArguments();

            if(view==null||musicPicture==null)
             view =inflater.inflate(R.layout.fragment_music_picture, container, false);
               musicPicture= (ImageView) view.findViewById(R.id.fragment_music_picture);
        if(bundle!=null){
            String  uri=bundle.getString(URI);
            Glide.with(getContext()).load(uri).into(musicPicture).onLoadFailed(null,getResources().getDrawable(R.mipmap.music1));
           /* if(uri==null){
            Glide.with(getContext()).load(R.drawable.red).into(musicPicture);
        }else {
            Glide.with(getContext()).load(uri).into(musicPicture).onLoadFailed(null,getResources().getDrawable(R.drawable.red));}*/
    }


               musicPicture.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Toast.makeText(getContext(),"显示歌词",Toast.LENGTH_SHORT).show();
                   }
               });
        return view;
    }

}
