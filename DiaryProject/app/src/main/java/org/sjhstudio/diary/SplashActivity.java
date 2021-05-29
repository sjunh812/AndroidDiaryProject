package org.sjhstudio.diary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.sjhstudio.diary.helper.MyTheme;

public class SplashActivity extends AppCompatActivity {
    private Handler handler = new Handler();    // 0.5초 딜레이를 위한 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyTheme.applyTheme(this);   // 사용자가 설정한 테마적용 (폰트 및 다크모드)
        setContentView(R.layout.activity_splash);   // 인플레이션 (Xml -> Java)

        handler.postDelayed(()->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 500);    // 0.5초 뒤, 메인 액티비티로 전환
    }
}