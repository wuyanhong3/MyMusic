package mymusic.com.mymusic.HomeMusic;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 音乐对象序列化
 */
public class MusicBean implements Parcelable {
    private String  collect="false";
    private String musicName;
    private String musicPath;
    private long id;
    private long albumid;
    private String title;
    private String artist;
    private String uri;
    private long length;
    private String image;
    public MusicBean(){}
    protected MusicBean(Parcel in) {
        musicName=in.readString();
        musicPath=in.readString();
        id=in.readLong();
        albumid=in.readLong();
        title=in.readString();
        artist=in.readString();
        image=in.readString();
        length=in.readLong();
        uri=in.readString();
        collect=in.readString();
    }
    public static final Creator<MusicBean> CREATOR = new Creator<MusicBean>() {
        @Override
        public MusicBean createFromParcel(Parcel in) {
            return new MusicBean(in);
        }
        @Override
        public MusicBean[] newArray(int size) {
            return new MusicBean[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(musicName);
        dest.writeString(musicPath);
        dest.writeLong(id);
        dest.writeLong(albumid);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(image);
        dest.writeLong(length);
        dest.writeString(uri);
        dest.writeString(collect);


    }
    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAlbumid() {
        return albumid;
    }

    public void setAlbumid(long albumid) {
        this.albumid = albumid;
    }

    public String getCollect() {
        return collect;
    }

    public void setCollect(String collect) {
        this.collect = collect;
    }

}
