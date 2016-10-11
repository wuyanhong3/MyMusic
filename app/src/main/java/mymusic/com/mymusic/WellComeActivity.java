package mymusic.com.mymusic;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import mymusic.com.mymusic.HomeMusic.HomeMainActivity;
import mymusic.com.mymusic.R;

/**
 * app欢迎界面
 *
 * */
public class WellComeActivity extends AppCompatActivity {
     private TextView wellCome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wellcome_layout);
        wellCome= (TextView) findViewById(R.id.wellcome_music);

        wellCome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(WellComeActivity.this,HomeMainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(5000);
                Intent intent=new Intent(WellComeActivity.this,HomeMainActivity.class);
                startActivity(intent);
                finish();
            }
        }).start();
    }
}
