package org.techtown.diary.note;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.techtown.diary.MainActivity;
import org.techtown.diary.PhotoActivity;
import org.techtown.diary.R;
import org.techtown.diary.helper.OnNoteItemClickListener;
import org.techtown.diary.helper.OnNoteItemLongClickListener;
import org.techtown.diary.helper.OnNoteItemTouchListener;
import org.techtown.diary.helper.OnRequestListener;

public class NoteViewHolder extends RecyclerView.ViewHolder {
    // 내용버튼 선택시 보일 뷰 UI
    private LinearLayout contentsLayout;
    private ImageView moodImageView;
    private ImageView weatherImageView;
    private ImageView existPictureImageView;        // 일기에 이미지가 있는 경우 띄워줄 작은 이미지
    private TextView contentsTextView;
    private TextView locationTextView;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView weekTextView;

    // 사진버튼 선택시 보일 뷰 UI
    private LinearLayout photoLayout;
    private ImageView moodImageView2;
    private ImageView weatherImageView2;
    private ImageView pictureImageView;
    private TextView contentsTextView2;
    private TextView locationTextView2;
    private TextView dateTextView2;
    private TextView timeTextView2;
    private TextView weekTextView2;
    private LinearLayout showPhotoStateView;

    private OnNoteItemClickListener clickListener;
    private OnNoteItemTouchListener touchListener;
    private OnNoteItemLongClickListener longClickListener;
    private OnRequestListener requestListener;
    private Context context;

    public NoteViewHolder(@NonNull View itemView, int type, Context context) {
        super(itemView);

        this.context = context;
        if(context instanceof OnRequestListener) {
            requestListener = (OnRequestListener)context;
        }

        contentsLayout = (LinearLayout)itemView.findViewById(R.id.contentsLayout);
        moodImageView = (ImageView)itemView.findViewById(R.id.moodImageView);
        weatherImageView = (ImageView)itemView.findViewById(R.id.weatherImageView);
        existPictureImageView = (ImageView)itemView.findViewById(R.id.existPictureImageView);
        contentsTextView = (TextView)itemView.findViewById(R.id.contentsTextView);
        locationTextView = (TextView)itemView.findViewById(R.id.locationTextView);
        dateTextView = (TextView)itemView.findViewById(R.id.dateTextView);
        timeTextView = (TextView)itemView.findViewById(R.id.timeTextView);
        weekTextView = (TextView)itemView.findViewById(R.id.weekTextView);

        photoLayout = (LinearLayout)itemView.findViewById(R.id.photoLayout);
        moodImageView2 = (ImageView)itemView.findViewById(R.id.moodImageView2);
        weatherImageView2 = (ImageView)itemView.findViewById(R.id.weatherImageView2);
        pictureImageView = (ImageView)itemView.findViewById(R.id.pictureImageView);
        contentsTextView2 = (TextView)itemView.findViewById(R.id.contentsTextView2);
        locationTextView2 = (TextView)itemView.findViewById(R.id.locationTextView2);
        dateTextView2 = (TextView)itemView.findViewById(R.id.dateTextView2);
        timeTextView2 = (TextView)itemView.findViewById(R.id.timeTextView2);
        weekTextView2 = (TextView)itemView.findViewById(R.id.weekTextView2);
        showPhotoStateView = (LinearLayout)itemView.findViewById(R.id.showPhotoStateView);

        contentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if(clickListener != null) {
                    clickListener.onItemClick(NoteViewHolder.this, v, position);
                }
            }
        });

        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                if(clickListener != null) {
                    clickListener.onItemClick(NoteViewHolder.this, v, position);
                }
            }
        });

        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int position = getAdapterPosition();
                if(touchListener != null) {
                    touchListener.onItemTouch(NoteViewHolder.this, v, position, event);
                }

                return true;
            }
        });

        contentsLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if(longClickListener != null) {
                    longClickListener.onLongClick(NoteViewHolder.this, v, position);
                    return true;
                }

                return false;
            }
        });

        photoLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                if(longClickListener != null) {
                    longClickListener.onLongClick(NoteViewHolder.this, v, position);
                    return true;
                }

                return false;
            }
        });

        setLayoutType(type);        // 내용레이아웃, 사진레이아웃 중 선택
    }

    public void setItem(Note item) {
        // 기분 설정
        int moodIndex = item.getMood();
        setMoodImage(moodIndex);

        // 사진 설정
        String picturePath = item.getPicture();

        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(picturePath != null && !picturePath.equals("")) {
                    Intent intent  = new Intent(context, PhotoActivity.class);
                    intent.putExtra("picturePath", picturePath);
                    context.startActivity(intent);
                }
            }
        });

        if(picturePath != null && !picturePath.equals("")) {
            existPictureImageView.setVisibility(View.VISIBLE);
            //pictureImageView.setImageURI(Uri.parse("file://" + picturePath));
            //Glide.with(context).load(Uri.parse("file://" + picturePath)).into(pictureImageView);
            Glide.with(context).load(Uri.parse("file://" + picturePath)).apply(RequestOptions.bitmapTransform(MainActivity.option)).into(pictureImageView);
            pictureImageView.setVisibility(View.VISIBLE);
            showPhotoStateView.setVisibility(View.GONE);
        } else {
            existPictureImageView.setVisibility(View.GONE);
            pictureImageView.setVisibility(View.GONE);
            showPhotoStateView.setVisibility(View.VISIBLE);
            //pictureImageView.setImageResource(R.drawable.no_image_64_color);
        }

        // 날씨 설정
        int weatherIndex = item.getWeather();
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

        // 시간 설정
        String time = item.getTime();
        timeTextView.setText(time);
        timeTextView2.setText(time);

        // 요일 설정
        String dayOfWeek = item.getDayOfWeek();
        weekTextView.setText(dayOfWeek);
        weekTextView2.setText(dayOfWeek);
    }

    private void setMoodImage(int index) {
        switch(index) {
            case 0:     // 화남
                moodImageView.setImageResource(R.drawable.mood_angry_color);
                moodImageView2.setImageResource(R.drawable.mood_angry_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_red));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_red));
                break;
            case 1:     // 쿨
                moodImageView.setImageResource(R.drawable.mood_cool_color);
                moodImageView2.setImageResource(R.drawable.mood_cool_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_blue));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_blue));
                break;
            case 2:     // 슬픔
                moodImageView.setImageResource(R.drawable.mood_crying_color);
                moodImageView2.setImageResource(R.drawable.mood_crying_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_skyblue));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_skyblue));
                break;
            case 3:     // 아픔
                moodImageView.setImageResource(R.drawable.mood_ill_color);
                moodImageView2.setImageResource(R.drawable.mood_ill_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_green));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_green));
                break;
            case 4:     // 웃음
                moodImageView.setImageResource(R.drawable.mood_laugh_color);
                moodImageView2.setImageResource(R.drawable.mood_laugh_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_yellow));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_yellow));
                break;
            case 5:     // 보통
                moodImageView.setImageResource(R.drawable.mood_meh_color);
                moodImageView2.setImageResource(R.drawable.mood_meh_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_gray));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_gray));
                break;
            case 6:     // 나쁨
                moodImageView.setImageResource(R.drawable.mood_sad);
                moodImageView2.setImageResource(R.drawable.mood_sad);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_black));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_black));
                break;
            case 7:     // 좋음
                moodImageView.setImageResource(R.drawable.mood_smile_color);
                moodImageView2.setImageResource(R.drawable.mood_smile_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_orange));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_orange));
                break;
            case 8:     // 졸림
                moodImageView.setImageResource(R.drawable.mood_yawn_color);
                moodImageView2.setImageResource(R.drawable.mood_yawn_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_pink));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_pink));
                break;
            default:    // default(미소)
                moodImageView.setImageResource(R.drawable.mood_smile_color);
                moodImageView2.setImageResource(R.drawable.mood_smile_color);
                //contentsLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_orange));
                //photoLayout.setBackground(context.getResources().getDrawable(R.drawable.border_view_orange));
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
        clickListener = listener;
    }

    public void setOnItemTouchListener(OnNoteItemTouchListener listener) {
        touchListener = listener;
    }

    public void setOnItemLongClickListener(OnNoteItemLongClickListener listener) {
        longClickListener = listener;
    }
}
