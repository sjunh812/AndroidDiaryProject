package org.sjhstudio.diary.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.sjhstudio.diary.MainActivity;
import org.sjhstudio.diary.custom.CustomDatePickerDialog;
import org.sjhstudio.diary.custom.CustomDeleteDialog;
import org.sjhstudio.diary.custom.CustomDialog;
import org.sjhstudio.diary.R;
import org.sjhstudio.diary.helper.OnRequestListener;
import org.sjhstudio.diary.helper.OnTabItemSelectedListener;
import org.sjhstudio.diary.note.Note;
import org.sjhstudio.diary.note.NoteDatabaseCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteFragment extends Fragment {
    /* 상수 */
    private static final String LOG = "WriteFragment";  // 로그
    public static final int REQUEST_CAMERA = 21;        // 카메라 액티비티에 보내는 요청
    public static final int REQUEST_ALBUM = 22;         // 갤러리 액티비티에 보내는 요청

    /* UI */
    private TextView titleTextView;
    private ImageView weatherImageView;
    private ImageView weatherAddImageView;
    private LinearLayout weatherView;
    private TextView dateTextView;
    private ImageView dateTextImageView;
    private EditText locationTextView;
    private FrameLayout pictureContainer;
    private ImageView pictureImageView;
    private ImageView addPictureImageView;
    private EditText contentsEditText;
    private ImageButton saveButton;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private Button button9;
    private Button curButton = null;                    // 현재 선택된 감정표현 버튼
    private CustomDialog dialog;                        // 사진 추가시 띄워지는 커스텀 다이얼로그
    private CustomDeleteDialog deleteDialog;            // 사진 삭제시 띄워지는 커스텀 다이얼로그
    private CustomDeleteDialog deleteNoteDialog;        // 일기 삭제시 띄워지는 커스텀 다이얼로그
    private CustomDatePickerDialog pickerDialog;
    private ImageButton starButton;                     // 즐겨찾기 버튼
    private SwipeRefreshLayout swipeRefreshLayout;      // 새로고침 뷰

    /* Helper */
    private OnTabItemSelectedListener tabListener;      // 메인 액티비티에서 관리하는 하단 탭 선택 리스터
    private OnRequestListener requestListener;          // 메인 액티비티에서 현재 위치 정보를 가져오게 해주는 리스너
    private MoodButtonClickListener moodButtonListener; // 감정표현 버튼 눌림에 따른 버튼 스케일 효과를 위한 리스터
    private NoteDatabaseCallback callback;              // db 쿼리문 실행을 위한 콜백 인터페이스
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");     // 카메라 앱을 이용해 촬영시 저장되는 파일이름에 사용될 날짜포멧 ex)20210418

    /* Data */
    private Note updateItem = null;
    private Date calDate = null;
    private Context context;
    private int weatherIndex = -1;                       // 날씨 정보(0:맑음, 1:구름 조금, 2:구름 많음, 3:흐림, 4:비, 5:눈/비, 6:눈)
    private int moodIndex = -1;                         // 0~8 총 9개의 기분을 index 로 표현(-1은 사용자가 아무런 기분도 선택하지 않은 경우)
    private String address = "";                        // 위치 정보
    private String contents = "";                       // 일기 내용
    private String filePath = "";                       // cropper 로 수정까지한 최종 사진 경로
    private String recentFilePath = "";                 // 수정하기 시 기존에 사진이 있는 경우 해당 사진 경로
    private String dateText = null;                     // yyyy-MM-dd HH:mm (사용자가 직접 일기 날짜를 지정한 경우 이용됨)
    private Uri fileUri;                                // 카메라로 찍고 난 후 저장되는 파일의 Uri
    private Object[] objs;                              // db 에 데이터 삽입을 위해 필요한 Object[] 객체
    private int curYear;
    private int curMonth;
    private int curDay;
    private boolean deleteRecentFilePath = false;       // 수정하기 시 사용자가 기존 사진을 삭제한지 여부
    private Animation moodAnim;
    private int starIndex = 0;                          // 0 = 즐겨찾기 x, 1 = 즐겨찾기
    private Animation translateLeftAnim;
    private Animation translateRightAnim;
    private Animation translateRightTitleAnim;
    private boolean isWeatherViewOpen = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;

        // 메인 액티비티로 부터온 리스너들 초기화
        if(context instanceof OnTabItemSelectedListener) {
            tabListener = (OnTabItemSelectedListener)context;
        }
        if(context instanceof OnRequestListener) {
            requestListener = (OnRequestListener)context;
        }
        if(context instanceof NoteDatabaseCallback) {
            callback = (NoteDatabaseCallback)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        deleteFileCache();          // 남아있는 파일캐시 삭제

        // 리스너 해제
        if(context != null) {
            context = null;
            tabListener = null;
            requestListener = null;
            callback = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_write, container, false);

        initAnimationUI(rootView);
        initBasicUI(rootView);
        initWeatherViewUI(rootView);
        initMoodUI(rootView);

        starButton = (ImageButton)rootView.findViewById(R.id.starButton);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(starIndex == 0) {
                    starButton.setImageDrawable(getResources().getDrawable(R.drawable.star_icon_color));
                    starIndex = 1;
                } else {
                    starButton.setImageDrawable(getResources().getDrawable(R.drawable.star_icon));
                    starIndex = 0;
                }
            }
        });

        saveButton = (ImageButton)rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new SaveButtonClickListener());

        ImageButton deleteButton = (ImageButton)rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    if(updateItem == null) {                    // 일기 작성을 멈출 때
                        if(requestListener != null) {
                            requestListener.onRequest("showStopWriteDialog");
                        }
                    } else {                                    // 수정 중일때 삭제
                        setDeleteNoteDialog();
                    }
                }
            }
        });

        if(requestListener != null && updateItem == null) {
            if(calDate == null) {
                requestListener.onRequest("getCurrentLocation");            // 메인 액티비티로부터 현재 위치 정보 가져오기
            } else {
                requestListener.onRequest("getCurrentLocation", calDate);   // 메인 액티비티로부터 현재 위치 정보 가져오기 (단, 달력에서 넘어온 Date 사용)
            }
        }

        if(updateItem != null) {
            setUpdateItem();
        }

        return rootView;
    }

    private void initAnimationUI(View rootView) {
        /* Animation 관련 초기화 */
        MyAnimationListener animationListener = new MyAnimationListener();
        moodAnim = AnimationUtils.loadAnimation(getContext(), R.anim.mood_icon_animation);
        translateLeftAnim = AnimationUtils.loadAnimation(getContext(), R.anim.translate_left_animation);
        translateRightAnim = AnimationUtils.loadAnimation(getContext(), R.anim.translate_right_animation);
        translateLeftAnim.setAnimationListener(animationListener);
        translateRightAnim.setAnimationListener(animationListener);
        translateRightTitleAnim = AnimationUtils.loadAnimation(getContext(), R.anim.translate_right_animation);
        translateRightTitleAnim.setDuration(350);
    }

    private void initBasicUI(View rootView) {
        /* 타이틀 UI */
        titleTextView = (TextView)rootView.findViewById(R.id.titleTextView);
        titleTextView.startAnimation(translateRightTitleAnim);
        if(updateItem == null) {
            titleTextView.setText("일기작성");
        } else {
            titleTextView.setText("일기수정");
        }

        /* 일기작성 날짜 UI */
        dateTextView = (TextView)rootView.findViewById(R.id.dateTextView);
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDatePickerDialog();
            }
        });
        dateTextImageView = (ImageView)rootView.findViewById(R.id.dateTextImageView);
        dateTextImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDatePickerDialog();
            }
        });

        /* 날씨 UI */
        weatherView = (LinearLayout)rootView.findViewById(R.id.weatherView);
        weatherImageView = (ImageView)rootView.findViewById(R.id.weatherImageView);
        weatherImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherAddImageView.setImageResource(R.drawable.navigate_up);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });
        weatherAddImageView = (ImageView)rootView.findViewById(R.id.weatherAddImageView);
        weatherAddImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherAddImageView.setImageResource(R.drawable.navigate_up);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });

        locationTextView = (EditText)rootView.findViewById(R.id.locationTextView);
        contentsEditText = (EditText)rootView.findViewById(R.id.contentsEditText);
        addPictureImageView = (ImageView)rootView.findViewById(R.id.addPictureImageView);

        pictureImageView = (ImageView)rootView.findViewById(R.id.pictureImageView);
        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialog();
            }
        });
        pictureImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setDeletePictureDialog();

                return true;
            }
        });
        pictureContainer = (FrameLayout)rootView.findViewById(R.id.pictureContainer);
        pictureContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialog();
            }
        });

        /* 새로고침 뷰 */
        swipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.pastel_700));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(requestListener != null && updateItem == null) {
                    if(calDate == null) {
                        requestListener.onRequest("getCurrentLocation");            // 메인 액티비티로부터 현재 위치 정보 가져오기
                    } else {
                        requestListener.onRequest("getCurrentLocation", calDate);   // 메인 액티비티로부터 현재 위치 정보 가져오기 (단, 달력에서 넘어온 Date 사용)
                    }

                    //swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    public void setSwipeRefresh(boolean isRefresh) {
        swipeRefreshLayout.setRefreshing(isRefresh);
    }

    private void initWeatherViewUI(View rootView) {
        ImageButton weatherButton = (ImageButton)rootView.findViewById(R.id.weatherButton);
        ImageButton weatherButton2 = (ImageButton)rootView.findViewById(R.id.weatherButton2);
        ImageButton weatherButton3 = (ImageButton)rootView.findViewById(R.id.weatherButton3);
        ImageButton weatherButton4 = (ImageButton)rootView.findViewById(R.id.weatherButton4);
        ImageButton weatherButton5 = (ImageButton)rootView.findViewById(R.id.weatherButton5);
        ImageButton weatherButton6 = (ImageButton)rootView.findViewById(R.id.weatherButton6);
        ImageButton weatherButton7 = (ImageButton)rootView.findViewById(R.id.weatherButton7);

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherIndex = 0;
                setWeatherImageView2(weatherIndex);
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });

        weatherButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherIndex = 1;
                setWeatherImageView2(weatherIndex);
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });

        weatherButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherIndex = 2;
                setWeatherImageView2(weatherIndex);
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });

        weatherButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherIndex = 3;
                setWeatherImageView2(weatherIndex);
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });

        weatherButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherIndex = 4;
                setWeatherImageView2(weatherIndex);
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });

        weatherButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherIndex = 5;
                setWeatherImageView2(weatherIndex);
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });

        weatherButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                weatherIndex = 6;
                setWeatherImageView2(weatherIndex);
                if(isWeatherViewOpen) {
                    weatherView.startAnimation(translateLeftAnim);
                } else {
                    weatherView.setVisibility(View.VISIBLE);
                    weatherView.startAnimation(translateRightAnim);
                }
            }
        });
    }

    private void initMoodUI(View rootView) {
        moodButtonListener = new MoodButtonClickListener();     // 감정 선택에 따른 버튼 스케일 변화 리스너 초기화
        LinearLayout moodView1 = (LinearLayout)rootView.findViewById(R.id.angryView);
        LinearLayout moodView2 = (LinearLayout)rootView.findViewById(R.id.coolView);
        LinearLayout moodView3 = (LinearLayout)rootView.findViewById(R.id.cryingView);
        LinearLayout moodView4 = (LinearLayout)rootView.findViewById(R.id.illView);
        LinearLayout moodView5 = (LinearLayout)rootView.findViewById(R.id.laughView);
        LinearLayout moodView6 = (LinearLayout)rootView.findViewById(R.id.mehView);
        LinearLayout moodView7 = (LinearLayout)rootView.findViewById(R.id.sadView);
        LinearLayout moodView8 = (LinearLayout)rootView.findViewById(R.id.smileView);
        LinearLayout moodView9 = (LinearLayout)rootView.findViewById(R.id.yawnView);

        button1 = (Button)rootView.findViewById(R.id.button1);
        button2 = (Button)rootView.findViewById(R.id.button2);
        button3 = (Button)rootView.findViewById(R.id.button3);
        button4 = (Button)rootView.findViewById(R.id.button4);
        button5 = (Button)rootView.findViewById(R.id.button5);
        button6 = (Button)rootView.findViewById(R.id.button6);
        button7 = (Button)rootView.findViewById(R.id.button7);
        button8 = (Button)rootView.findViewById(R.id.button8);
        button9 = (Button)rootView.findViewById(R.id.button9);

        moodView1.setOnClickListener(moodButtonListener);
        moodView2.setOnClickListener(moodButtonListener);
        moodView3.setOnClickListener(moodButtonListener);
        moodView4.setOnClickListener(moodButtonListener);
        moodView5.setOnClickListener(moodButtonListener);
        moodView6.setOnClickListener(moodButtonListener);
        moodView7.setOnClickListener(moodButtonListener);
        moodView8.setOnClickListener(moodButtonListener);
        moodView9.setOnClickListener(moodButtonListener);
    }

    public void setWeatherImageView(String weatherStr) {                    // 날씨 문자열 값으로 날씨이미지 설정
        if(weatherStr.equals("맑음")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_1);
            weatherIndex = 0;
        } else if(weatherStr.equals("구름 조금")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_2);
            weatherIndex = 1;
        } else if(weatherStr.equals("구름 많음")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_3);
            weatherIndex = 2;
        } else if(weatherStr.equals("흐림")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_4);
            weatherIndex = 3;
        } else if(weatherStr.equals("비")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_5);
            weatherIndex = 4;
        } else if(weatherStr.equals("눈/비")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_6);
            weatherIndex = 5;
        } else if(weatherStr.equals("눈")){
            weatherImageView.setImageResource(R.drawable.weather_icon_7);
            weatherIndex = 6;
        } else {
            Log.d(LOG, "Unknown weather string : " + weatherStr);
        }
    }

    public void setWeatherImageView2(int weatherIndex) {                    // 날씨 인덱스 값으로 날씨이미지 설정
        if(weatherIndex == 0) {
            weatherImageView.setImageResource(R.drawable.weather_icon_1);
        } else if(weatherIndex == 1) {
            weatherImageView.setImageResource(R.drawable.weather_icon_2);
        } else if(weatherIndex == 2) {
            weatherImageView.setImageResource(R.drawable.weather_icon_3);
        } else if(weatherIndex == 3) {
            weatherImageView.setImageResource(R.drawable.weather_icon_4);
        } else if(weatherIndex == 4) {
            weatherImageView.setImageResource(R.drawable.weather_icon_5);
        } else if(weatherIndex == 5) {
            weatherImageView.setImageResource(R.drawable.weather_icon_6);
        } else if(weatherIndex == 6){
            weatherImageView.setImageResource(R.drawable.weather_icon_7);
        } else {
            Log.d(LOG, "Unknown weather index : " + weatherIndex);
        }
    }

    public void setLocationTextView(String location) {
        locationTextView.setText(location);
        address = location;
    }

    public void setDateTextView(String date) {
        dateTextView.setText(date);
    }

    public void setCurDate(int curYear, int curMonth, int curDay) {
        this.curYear = curYear;
        this.curMonth = curMonth;
        this.curDay = curDay;
    }

    public void setPictureImageView(Bitmap bitmap, Uri uri, int res) {
        pictureImageView.setVisibility(View.VISIBLE);
        addPictureImageView.setVisibility(View.GONE);

        if(bitmap != null) {
            pictureImageView.setImageBitmap(bitmap);
        }
        if(uri != null) {
            //pictureImageView.setImageURI(uri);
            Glide.with(this).load(uri).apply(RequestOptions.bitmapTransform(MainActivity.option)).into(pictureImageView);
        }
        if(res != -1) {
            pictureImageView.setImageResource(res);
        }
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setContents() {
        contents = contentsEditText.getText().toString();
    }

    private void setMoodIndex() {                                           // curButton 객체의 값으로 기분 인덱스 값 설정
        if(curButton == null) {
            /* 사용자가 아무런 기분도 선택하지 않는 상황 */
            /* 선택할 수 있도록 토스트바를 띄워줘야함 */
            moodIndex = -1;
        } else if(curButton == button1) {
            moodIndex = 0;
        } else if(curButton == button2) {
            moodIndex = 1;
        } else if(curButton == button3) {
            moodIndex = 2;
        } else if(curButton == button4) {
            moodIndex = 3;
        } else if(curButton == button5) {
            moodIndex = 4;
        } else if(curButton == button6) {
            moodIndex = 5;
        } else if(curButton == button7) {
            moodIndex = 6;
        } else if(curButton == button8) {
            moodIndex = 7;
        } else {
            moodIndex = 8;
        }
    }

    private void setMoodButton(int moodIndex) {                             // 기분 인덱스 값으로 curButton 객체 설정 및 버튼 크기 조절 (일기 수정시 사용)
        if (moodIndex == 0) {
            button1.setScaleX(1.4f);
            button1.setScaleY(1.4f);
            button1.startAnimation(moodAnim);
            curButton = button1;
        } else if (moodIndex == 1) {
            button2.setScaleX(1.4f);
            button2.setScaleY(1.4f);
            button2.startAnimation(moodAnim);
            curButton = button2;
        } else if (moodIndex == 2) {
            button3.setScaleX(1.4f);
            button3.setScaleY(1.4f);
            button3.startAnimation(moodAnim);
            curButton = button3;
        } else if (moodIndex == 3) {
            button4.setScaleX(1.4f);
            button4.setScaleY(1.4f);
            button4.startAnimation(moodAnim);
            curButton = button4;
        } else if (moodIndex == 4) {
            button5.setScaleX(1.4f);
            button5.setScaleY(1.4f);
            button5.startAnimation(moodAnim);
            curButton = button5;
        } else if (moodIndex == 5) {
            button6.setScaleX(1.4f);
            button6.setScaleY(1.4f);
            button6.startAnimation(moodAnim);
            curButton = button6;
        } else if (moodIndex == 6) {
            button7.setScaleX(1.4f);
            button7.setScaleY(1.4f);
            button7.startAnimation(moodAnim);
            curButton = button7;
        } else if (moodIndex == 7) {
            button8.setScaleX(1.4f);
            button8.setScaleY(1.4f);
            button8.startAnimation(moodAnim);
            curButton = button8;
        } else {
            button9.setScaleX(1.4f);
            button9.setScaleY(1.4f);
            button9.startAnimation(moodAnim);
            curButton = button9;
        }
    }

    public void setCalDate(Date calDate) {
        this.calDate = calDate;
    }

    public void setDialog() {
        dialog = new CustomDialog(getContext());
        dialog.show();
        dialog.setCancelable(true);

        dialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCameraButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCameraActivity();
                dialog.dismiss();
            }
        });
        dialog.setAlbumButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlbumAcitivity();
                dialog.dismiss();
            }
        });
    }

    public void setDatePickerDialog() {
        pickerDialog = new CustomDatePickerDialog(getContext(), curYear, curMonth, curDay);
        pickerDialog.show();
        pickerDialog.setCancelable(true);

        pickerDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickerDialog.dismiss();
            }
        });

        pickerDialog.setOkButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curYear = pickerDialog.getCurYear();
                curMonth = pickerDialog.getCurMonth() + 1;
                curDay = pickerDialog.getCurDay();
                Date newDate = new Date(curYear - 1900, curMonth - 1, curDay);
                dateText = MainActivity.dateFormat2.format(newDate);
                dateTextView.setText(MainActivity.dateFormat.format(newDate));

                pickerDialog.dismiss();
            }
        });
    }

    public void setDeletePictureDialog() {
        deleteDialog = new CustomDeleteDialog(getContext());
        deleteDialog.show();
        deleteDialog.setCancelable(true);

        deleteDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog.setDeleteButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updateItem == null) {            // 새 일기 작성시
                    deleteFileCache();
                } else {
                    if(filePath.equals("")) {       // 일기 수정시, 사용자가 기존에 올렸던 사진을 삭제한 경우
                        deleteRecentFilePath = true;
                    }
                }

                pictureImageView.setVisibility(View.GONE);
                addPictureImageView.setVisibility(View.VISIBLE);

                deleteDialog.dismiss();
            }
        });

        deleteDialog.setCancelButton2OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });
    }

    public void setDeleteNoteDialog() {
        deleteNoteDialog = new CustomDeleteDialog(getContext());
        deleteNoteDialog.show();
        deleteNoteDialog.setCancelable(true);

        deleteNoteDialog.setTitleTextView("일기 삭제");
        deleteNoteDialog.setDeleteTextView("일기를 삭제하시겠습니까?");

        deleteNoteDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNoteDialog.dismiss();
            }
        });

        deleteNoteDialog.setDeleteButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNoteDialog.dismiss();

                int id = updateItem.get_id();
                String path = updateItem.getPicture();

                if(path != null && !path.equals("")) {  // 사진 삭제(cropper를 이용해 편집한 사진 캐쉬를 삭제)
                    File file = new File(path);
                    file.delete();
                }

                callback.deleteDB(id);                  // 해당 db 삭제
                tabListener.onTabSelected(0);
            }
        });

        deleteNoteDialog.setCancelButton2OnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNoteDialog.dismiss();
            }
        });
    }

    public void showCameraActivity() {
        File file = createFile();
        Uri uri = FileProvider.getUriForFile(getContext(), "org.techtown.diary.fileprovider", file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        if(intent.resolveActivity(getContext().getPackageManager()) != null) {
            getActivity().startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    public void showAlbumAcitivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getActivity().startActivityForResult(intent, REQUEST_ALBUM);
    }

    public boolean isEmpty() {
        if(contentsEditText.getText().toString().equals("") || curButton == null) {
            return true;
        }

        return false;
    }

    public void setItem(Note item) {
        updateItem = item;
    }

    public void setUpdateItem() {
        int weatherIndex = updateItem.getWeather();
        String date = updateItem.getCreateDateStr();
        String address = updateItem.getAddress();
        String path = updateItem.getPicture();
        String contents = updateItem.getContents();
        int moodIndex = updateItem.getMood();
        String date2Str = updateItem.getCreateDateStr2();
        int starIndex = updateItem.getStarIndex();
        try {
            Date date2 = MainActivity.dateFormat2.parse(date2Str);
            curYear = date2.getYear() + 1900;
            curMonth = date2.getMonth() + 1;
            curDay = date2.getDate();
        } catch(Exception e) {
            e.printStackTrace();
        }

        checkStarButton(starIndex);
        setWeatherImageView2(weatherIndex);
        setDateTextView(date);
        setLocationTextView(address);
        setMoodButton(moodIndex);
        this.weatherIndex = weatherIndex;
        contentsEditText.setText(contents);

        if(!path.equals("") && path != null) {
            Glide.with(context).load(Uri.parse("file://" + path)).apply(RequestOptions.bitmapTransform(MainActivity.option)).into(pictureImageView);
            recentFilePath = path;                          // 수정하기 취소 시, 기존에 올렸던 파일을 복구하기위해 recentFilePath 에 미리 경로를 저장

            pictureImageView.setVisibility(View.VISIBLE);
            addPictureImageView.setVisibility(View.GONE);
        } else {
            pictureImageView.setVisibility(View.GONE);
            addPictureImageView.setVisibility(View.VISIBLE);
        }
    }

    private void checkStarButton(int index) {
        if(index == 0) {
            starIndex = 0;
            starButton.setImageDrawable(getResources().getDrawable(R.drawable.star_icon));
        } else {
            starIndex = 1;
            starButton.setImageDrawable(getResources().getDrawable(R.drawable.star_icon_color));
        }
    }

    public void deleteFileCache() {
        if(filePath != null && !filePath.equals("")) {
            File file = new File(filePath);
            file.delete();
            filePath = "";
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(requestListener != null) {
            requestListener.onRequest("checkGPS");
            Log.d(LOG, "onRequest(checkGPS) 호출됨.");
        }
    }

    public String getFilePath() {
        return filePath;
    }

    private File createFile() {
        // 파일 이름 생성
        Date date = new Date();
        String fileName = format.format(date) + "_" + System.currentTimeMillis();

        // 파일 경로 생성
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();

            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Diary");

            fileUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            String[] filePathColums = { MediaStore.MediaColumns.DATA };
            Cursor cursor = getContext().getContentResolver().query(fileUri, filePathColums, null, null);
            cursor.moveToFirst();

            int index = cursor.getColumnIndex(filePathColums[0]);
            String filePath = cursor.getString(index);

            cursor.close();

            return new File(filePath);
        } else {
            File storageFile = Environment.getExternalStorageDirectory();

            return new File(storageFile, fileName);
        }
    }

    public Bitmap decodeFile(File file, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;      // 비트맵을 메모리 할당 전에 먼저 비트맵 크기를 알 수 있음
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if(width > reqWidth || height > reqHeight) {
            final int halfWidth = width;
            final int halfHeight = height;

            while((halfWidth / inSampleSize) > reqWidth || (halfHeight / inSampleSize) > reqHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public int getPictureWidth() {
        return pictureImageView.getWidth();
    }

    public int getPictureHeight() {
        return pictureImageView.getHeight();
    }

    public Uri getFileUri() {
        return fileUri;
    }

    class SaveButtonClickListener implements  View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(tabListener != null) {
                /* 사용자가 일기내용을 작성하지 않았거나 기분을 고르지 않는 경우 */
                if(isEmpty()) {
                    Toast.makeText(getContext(), "일기를 작성해주세요.\n오늘 기분도 골라주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                setMoodIndex();                             // 현재 눌린 기분버튼 종류에 따라 moodIndex 설정
                setContents();                              // contentsEditText 에 사용자가 입력한 내용을 contents 에 저장
                address = locationTextView.getText().toString();    // GPS 로 받아온 위치 or 사용자가 직접 입력한 위치정보를 address(String)에 저장
                if(updateItem == null) {                    // 새로 일기를 작성하는 경우
                    if(dateText != null) {                  // 사용자가 날짜를 바꾼 경우
                        String date = dateText + " " + MainActivity.timeFormat2.format(new Date());
                        objs = new Object[]{weatherIndex, address, "", "", contents, moodIndex, filePath, curYear, curMonth, date, starIndex};
                        callback.insertDB2(objs);
                    } else {                                // 사용자가 날짜를 바꾸지 않은 경우
                        if(calDate != null) {               // 기분달력으로 넘어온 날짜로 작성하는 경우
                            String date = MainActivity.dateFormat2.format(calDate) + " " + MainActivity.timeFormat2.format(new Date());
                            objs = new Object[]{weatherIndex, address, "", "", contents, moodIndex, filePath, curYear, curMonth, date, starIndex};
                            callback.insertDB2(objs);
                        } else {
                            objs = new Object[]{weatherIndex, address, "", "", contents, moodIndex, filePath, curYear, curMonth, starIndex};
                            callback.insertDB(objs);
                        }
                    }

                    filePath = null;                        // filePath 를 null로 지정함으로써 detach()호출에도 삭제되지않음
                    tabListener.onTabSelected(0);   // 일기목록 프래그먼트로 이동
                } else {                                    // 일기를 수정하는 경우
                    if(filePath != null && !filePath.equals("")) {
                        updateItem.setPicture(filePath);

                        if(recentFilePath != null && !recentFilePath.equals("")) {
                            File file = new File(recentFilePath);
                            file.delete();
                        }
                    } else {
                        if(deleteRecentFilePath) {
                            updateItem.setPicture("");
                            if(recentFilePath != null && !recentFilePath.equals("")) {
                                File file = new File(recentFilePath);
                                file.delete();
                            }
                        } else {
                            updateItem.setPicture(recentFilePath);
                        }
                    }

                    updateItem.setWeather(weatherIndex);
                    updateItem.setAddress(locationTextView.getText().toString());
                    updateItem.setContents(contents);
                    updateItem.setMood(moodIndex);
                    updateItem.setStarIndex(starIndex);
                    if(dateText != null) {
                        String date = dateText + " " + MainActivity.timeFormat2.format(new Date());
                        updateItem.setYear(curYear);
                        updateItem.setDay(curMonth);
                        updateItem.setCreateDateStr2(date);
                        callback.updateDB2(updateItem);
                    } else {
                        callback.updateDB(updateItem);
                    }

                    filePath = null;                        // filePath 를 null로 지정함으로써 detach()호출에도 삭제되지않음
                    tabListener.onTabSelected(0);   // 일기목록 프래그먼트로 이동
                }
            }
        }
    }

    class MoodButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Button selectButton = (Button)getSelectedMoodButton(v);

            if(curButton == null) {
                selectButton.setScaleX(1.4f);
                selectButton.setScaleY(1.4f);
                selectButton.startAnimation(moodAnim);

                curButton = selectButton;
            } else if(curButton == selectButton){
                selectButton.setScaleX(1.0f);
                selectButton.setScaleY(1.0f);
                selectButton.clearAnimation();

                curButton = null;
            }
            else {
                curButton.setScaleX(1.0f);
                curButton.setScaleY(1.0f);
                curButton.clearAnimation();

                selectButton.setScaleX(1.4f);
                selectButton.setScaleY(1.4f);
                selectButton.startAnimation(moodAnim);
                curButton = selectButton;
            }
        }
    }

    class MyAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(isWeatherViewOpen) {
                weatherAddImageView.setImageResource(R.drawable.navigate_down);
                weatherView.setVisibility(View.GONE);
            }
            isWeatherViewOpen = !isWeatherViewOpen;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private View getSelectedMoodButton(View v) {
        int id = v.getId();

        switch(id) {
            case R.id.angryView:
                return button1;
            case R.id.coolView:
                return button2;
            case R.id.cryingView:
                return button3;
            case R.id.illView:
                return button4;
            case R.id.laughView:
                return button5;
            case R.id.mehView:
                return button6;
            case R.id.sadView:
                return button7;
            case R.id.smileView:
                return button8;
            case R.id.yawnView:
                return button9;
        }

        return null;
    }
}
