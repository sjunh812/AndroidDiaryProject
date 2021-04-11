package org.techtown.diary.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.R;
import org.techtown.diary.item.Note;

public class NoteViewHolder extends RecyclerView.ViewHolder {
    // 내용버튼 선택시 보일 뷰 UI
    private LinearLayout contentsLayout;
    private ImageView moodImageView;
    private ImageView weatherImageView;
    private ImageView existPictureImageView;
    private TextView contentsTextView;
    private TextView locationTextView;
    private TextView dateTextView;

    // 사진버튼 선택시 보일 뷰 UI
    private LinearLayout photoLayout;
    private ImageView moodImageView2;
    private ImageView weatherImageView2;
    private ImageView pictureImageView;
    private TextView contentsTextView2;
    private TextView locationTextView2;
    private TextView dateTextView2;

    private OnNoteItemClickListener listener;

    public NoteViewHolder(@NonNull View itemView, int type) {
        super(itemView);
        contentsLayout = (LinearLayout)itemView.findViewById(R.id.contentsLayout);
        moodImageView = (ImageView)itemView.findViewById(R.id.moodImageView);
        weatherImageView = (ImageView)itemView.findViewById(R.id.weatherImageView);
        existPictureImageView = (ImageView)itemView.findViewById(R.id.existPictureImageView);
        contentsTextView = (TextView)itemView.findViewById(R.id.contentsTextView);
        locationTextView = (TextView)itemView.findViewById(R.id.locationTextView);
        dateTextView = (TextView)itemView.findViewById(R.id.dateTextView);

        photoLayout = (LinearLayout)itemView.findViewById(R.id.photoLayout);
        moodImageView2 = (ImageView)itemView.findViewById(R.id.moodImageView2);
        weatherImageView2 = (ImageView)itemView.findViewById(R.id.weatherImageView2);
        pictureImageView = (ImageView)itemView.findViewById(R.id.pictureImageView);
        contentsTextView2 = (TextView)itemView.findViewById(R.id.contentsTextView2);
        locationTextView2 = (TextView)itemView.findViewById(R.id.locationTextView2);
        dateTextView2 = (TextView)itemView.findViewById(R.id.dateTextView2);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if(listener != null) {
                    listener.onitemClick(NoteViewHolder.this, v, position);
                }
            }
        });

        setLayoutType(type);        // 내용레이아웃, 사진레이아웃 중 선택
    }

    public void setItem(Note item) {
        // 기분 설정
        String mood = item.getMood();
        int moodIndex = Integer.parseInt(mood);
        setMoodImage(moodIndex);

        // 사진 설정
        String picturePath = item.getPicture();
        if(picturePath != null && !picturePath.equals("")) {
            existPictureImageView.setVisibility(View.VISIBLE);
            pictureImageView.setImageURI(Uri.parse("file://" + picturePath));
        } else {
            existPictureImageView.setVisibility(View.INVISIBLE);
            pictureImageView.setImageResource(R.drawable.noimagefound);
        }

        // 날씨 설정
        String weather = item.getWeather();
        int weatherIndex = Integer.parseInt(weather);
        setWeatherImage(weatherIndex);

        // 내용 설정
        String contents = item.getContents();
        contentsTextView.setText(contents);
        contentsTextView2.setText(contents);

        // 위치 설정
        String location = item.getAddress();
        locationTextView.setText(location);
        locationTextView2.setText(location);

        // 날짜 설정
        String date = item.getCreateDateStr();
        dateTextView.setText(date);
        dateTextView2.setText(date);
    }

    private void setMoodImage(int index) {
        switch(index) {
            case 0:     // 화남
                moodImageView.setImageResource(R.drawable.mood_angry_color);
                moodImageView2.setImageResource(R.drawable.mood_angry_color);
                break;
            case 1:     // 쿨
                moodImageView.setImageResource(R.drawable.mood_cool_color);
                moodImageView2.setImageResource(R.drawable.mood_cool_color);
                break;
            case 2:     // 슬픔
                moodImageView.setImageResource(R.drawable.mood_crying_color);
                moodImageView2.setImageResource(R.drawable.mood_crying_color);
                break;
            case 3:     // 아픔
                moodImageView.setImageResource(R.drawable.mood_ill_color);
                moodImageView2.setImageResource(R.drawable.mood_ill_color);
                break;
            case 4:     // 웃음
                moodImageView.setImageResource(R.drawable.mood_laugh_color);
                moodImageView2.setImageResource(R.drawable.mood_laugh_color);
                break;
            case 5:     // 보통
                moodImageView.setImageResource(R.drawable.mood_meh_color);
                moodImageView2.setImageResource(R.drawable.mood_meh_color);
                break;
            case 6:     // 나쁨
                moodImageView.setImageResource(R.drawable.mood_sad);
                moodImageView2.setImageResource(R.drawable.mood_sad);
                break;
            case 7:     // 좋음
                moodImageView.setImageResource(R.drawable.mood_smile_color);
                moodImageView2.setImageResource(R.drawable.mood_smile_color);
                break;
            case 8:     // 졸림
                moodImageView.setImageResource(R.drawable.mood_yawn_color);
                moodImageView2.setImageResource(R.drawable.mood_yawn_color);
                break;
            default:    // default(미소)
                moodImageView.setImageResource(R.drawable.mood_smile_color);
                moodImageView2.setImageResource(R.drawable.mood_smile_color);
                break;
        }
    }

    private void setWeatherImage(int index) {
        switch(index) {
            case 0:
                weatherImageView.setImageResource(R.drawable.weather_icon_1);
                weatherImageView2.setImageResource(R.drawable.weather_icon_1);
                break;
            case 1:
                weatherImageView.setImageResource(R.drawable.weather_icon_2);
                weatherImageView2.setImageResource(R.drawable.weather_icon_2);
                break;
            case 2:
                weatherImageView.setImageResource(R.drawable.weather_icon_3);
                weatherImageView2.setImageResource(R.drawable.weather_icon_3);
                break;
            case 3:
                weatherImageView.setImageResource(R.drawable.weather_icon_4);
                weatherImageView2.setImageResource(R.drawable.weather_icon_4);
                break;
            case 4:
                weatherImageView.setImageResource(R.drawable.weather_icon_5);
                weatherImageView2.setImageResource(R.drawable.weather_icon_5);
                break;
            case 5:
                weatherImageView.setImageResource(R.drawable.weather_icon_6);
                weatherImageView2.setImageResource(R.drawable.weather_icon_6);
                break;
            case 6:
                weatherImageView.setImageResource(R.drawable.weather_icon_7);
                weatherImageView2.setImageResource(R.drawable.weather_icon_7);
                break;
            default:
                weatherImageView.setImageResource(R.drawable.weather_icon_1);
                weatherImageView2.setImageResource(R.drawable.weather_icon_1);
                break;
        }
    }

    public void setLayoutType(int type) {
        if(type == 0) {
            contentsLayout.setVisibility(View.VISIBLE);
            photoLayout.setVisibility(View.GONE);
        } else if(type == 1) {
            contentsLayout.setVisibility(View.GONE);
            photoLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setOnItemClickListener(OnNoteItemClickListener listener) {
        this.listener = listener;
    }
}
