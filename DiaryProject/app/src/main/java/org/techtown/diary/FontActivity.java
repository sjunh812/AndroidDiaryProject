package org.techtown.diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.techtown.diary.helper.MyApplication;
import org.techtown.diary.helper.MyTheme;

public class FontActivity extends AppCompatActivity {
    private static final String LOG = "FontActivity";
    private TextView exampleTextView;
    private RadioButton fontButton;
    private RadioButton fontButton2;
    private RadioButton fontButton3;
    private RadioButton fontButton4;
    private RadioButton fontButton5;

    private int curFontIndex = 0;
    private int selectedFontIndex = -1;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyTheme.applyTheme(this);
        setContentView(R.layout.activity_font);

        SharedPreferences pref = getSharedPreferences(MyTheme.SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        if(pref != null) {
            curFontIndex = pref.getInt(MyTheme.FONT_KEY, 0);
            Log.d(LOG, "from fontAcitvity fontIndex : " + curFontIndex);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("폰트 설정");
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fontButton = (RadioButton)findViewById(R.id.fontButton);
        fontButton2 = (RadioButton)findViewById(R.id.fontButton2);
        fontButton3 = (RadioButton)findViewById(R.id.fontButton3);
        fontButton4 = (RadioButton)findViewById(R.id.fontButton4);
        fontButton5 = (RadioButton)findViewById(R.id.fontButton5);

        exampleTextView = (TextView)findViewById(R.id.exampleTextView);

        RadioGroup fontGroup = (RadioGroup)findViewById(R.id.fontGroup);
        fontGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Typeface font = null;

                switch(checkedId) {
                    case R.id.fontButton:
                        font = Typeface.createFromAsset(getAssets(), "font1.ttf");
                        exampleTextView.setTypeface(font);
                        selectedFontIndex = 0;
                        break;
                    case R.id.fontButton2:
                        font = Typeface.createFromAsset(getAssets(), "font2.ttf");
                        exampleTextView.setTypeface(font);
                        selectedFontIndex = 1;
                        break;
                    case R.id.fontButton3:
                        font = Typeface.createFromAsset(getAssets(), "font3.ttf");
                        exampleTextView.setTypeface(font);
                        selectedFontIndex = 2;
                        break;
                    case R.id.fontButton4:
                        font = Typeface.createFromAsset(getAssets(), "font4.ttf");
                        exampleTextView.setTypeface(font);
                        selectedFontIndex = 3;
                        break;
                    case R.id.fontButton5:
                        font = Typeface.createFromAsset(getAssets(), "font5.ttf");
                        exampleTextView.setTypeface(font);
                        selectedFontIndex = 4;
                        break;
                }
            }
        });

        Button okButton = (Button)findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG, "번호 : " + selectedFontIndex);
                MyTheme.applyTheme(getApplicationContext(), selectedFontIndex);
                intent = getIntent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button cancelButton = (Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setExampleTextViewFont();
    }

    private void setExampleTextViewFont() {
        switch(curFontIndex) {
            case 0:
                fontButton.setChecked(true);
                break;
            case 1:
                fontButton2.setChecked(true);
                break;
            case 2:
                fontButton3.setChecked(true);
                break;
            case 3:
                fontButton4.setChecked(true);
                break;
            case 4:
                fontButton5.setChecked(true);
                break;
            default:
                Log.d(LOG, "ERROR : curFontInex Error");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}