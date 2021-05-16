package org.techtown.diary.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.techtown.diary.MainActivity;
import org.techtown.diary.custom.CustomDatePickerDialog;
import org.techtown.diary.custom.CustomDeleteDialog;
import org.techtown.diary.custom.CustomDialog;
import org.techtown.diary.R;
import org.techtown.diary.helper.OnRequestListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;
import org.techtown.diary.note.Note;
import org.techtown.diary.note.NoteDatabaseCallback;

import java.io.File;
import java.io.InputStream;
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
    private TextView dateTextView;
    private ImageView dateTextImageView;
    private TextView locationTextView;
    private FrameLayout pictureContainer;
    private ImageView pictureImageView;
    private ImageView addPictureImageView;
    private EditText contentsEditText;
    private ImageButton saveButton;
    private Button closeButton;
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
    private int weatherIndex = 0;                       // 날씨 정보(0:맑음, 1:구름 조금, 2:구름 많음, 3:흐림, 4:비, 5:눈/비, 6:눈)
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

        moodButtonListener = new MoodButtonClickListener();     // 감정 선택에 따른 버튼 스케일 변화 리스너 초기화
        moodAnim = AnimationUtils.loadAnimation(getContext(), R.anim.mood_icon_animation);

        titleTextView = (TextView)rootView.findViewById(R.id.titleTextView);
        if(updateItem == null) {
            titleTextView.setText("일기작성");
        } else {
            titleTextView.setText("일기수정");
        }

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

        weatherImageView = (ImageView)rootView.findViewById(R.id.weatherImageView);
        locationTextView = (TextView)rootView.findViewById(R.id.locationTextView);
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

        button1 = (Button)rootView.findViewById(R.id.button1);
        button2 = (Button)rootView.findViewById(R.id.button2);
        button3 = (Button)rootView.findViewById(R.id.button3);
        button4 = (Button)rootView.findViewById(R.id.button4);
        button5 = (Button)rootView.findViewById(R.id.button5);
        button6 = (Button)rootView.findViewById(R.id.button6);
        button7 = (Button)rootView.findViewById(R.id.button7);
        button8 = (Button)rootView.findViewById(R.id.button8);
        button9 = (Button)rootView.findViewById(R.id.button9);

        button1.setOnClickListener(moodButtonListener);
        button2.setOnClickListener(moodButtonListener);
        button3.setOnClickListener(moodButtonListener);
        button4.setOnClickListener(moodButtonListener);
        button5.setOnClickListener(moodButtonListener);
        button6.setOnClickListener(moodButtonListener);
        button7.setOnClickListener(moodButtonListener);
        button8.setOnClickListener(moodButtonListener);
        button9.setOnClickListener(moodButtonListener);

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
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    if(isEmpty()) {
                        Toast.makeText(getContext(), "일기를 작성해주세요.\n오늘 기분도 골라주세요.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    setMoodIndex();                             // 현재 눌린 기분버튼 종류에 따라 moodIndex 설정
                    setContents();                              // contentsEditText 에 사용자가 입력한 내용을 contents 에 저장

                    if(updateItem == null) {                    // 새 일기 작성
                        if(dateText != null) {                  // 사용자가 날짜를 바꾼 경우
                            String date = dateText + " " + MainActivity.timeFormat2.format(new Date());
                            objs = new Object[]{weatherIndex, address, "", "", contents, moodIndex, filePath, curYear, curMonth, date, starIndex};
                            callback.insertDB2(objs);
                        } else {                                // 일기 작성시에 자동으로 측정된 오늘 날짜로 작성한 경우
                            if(calDate != null) {
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
                    } else {                                    // 기존 일기 수정
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
                        tabListener.onTabSelected(0);       // 일기목록 프래그먼트로 이동
                    }
                }
            }
        });

        ImageButton deleteButton = (ImageButton)rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    if(updateItem == null) {
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
                if(updateItem == null) {            // 새일기 작성시
                    deleteFileCache();
                } else {
                    if(filePath.equals("")) {
                        deleteRecentFilePath = true;
                    }
                }

                //setPictureImageView(null, null, R.drawable.add_image_64_color);
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

        if(!path.equals("") && path != null) {
            Glide.with(context).load(Uri.parse("file://" + path)).apply(RequestOptions.bitmapTransform(MainActivity.option)).into(pictureImageView);
            recentFilePath = path;
            //filePath = path;

            pictureImageView.setVisibility(View.VISIBLE);
            addPictureImageView.setVisibility(View.GONE);
        } else {
            pictureImageView.setVisibility(View.GONE);
            addPictureImageView.setVisibility(View.VISIBLE);
        }

        contentsEditText.setText(contents);
        setMoodButton(moodIndex);
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

    class MoodButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Button selectButton = (Button)v;

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
            } else {
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
}
