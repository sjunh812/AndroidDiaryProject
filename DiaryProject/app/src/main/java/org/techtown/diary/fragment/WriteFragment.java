package org.techtown.diary.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.techtown.diary.CustomDialog;
import org.techtown.diary.R;
import org.techtown.diary.helper.OnRequestListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;

import java.io.File;

public class WriteFragment extends Fragment {
    // 상수
    private static final String LOG = "WriteFragment";
    public static final int REQUEST_CAMER = 10;
    public static final int REQUEST_ALBUM = 11;
    public static int PICTURE_WIDTH = 100;
    public static int PICTURE_HEIGHT = 100;

    // UI
    private ImageView weatherImageView;
    private TextView dateTextView;
    private TextView locationTextView;
    private ImageView pictureImageView;
    private EditText contentsEditText;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private Button button9;
    private Button curButton = null;
    private CustomDialog dialog;

    private OnTabItemSelectedListener tabListener;
    private OnRequestListener requestListener;          // 메인 액티비티에서 현재 위치 정보를 가져오게 해주는 리스너
    private MoodButtonClickListener moodButtonListener;

    private int moodIndex = -1;                         // 0~8 총 9개의 기분을 index 로 표현(-1은 사용자가 아무런 기분도 선택하지 않은 경우)

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof OnTabItemSelectedListener) {
            tabListener = (OnTabItemSelectedListener)context;
        }
        if(context instanceof OnRequestListener) {
            requestListener = (OnRequestListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(tabListener != null) {
            tabListener = null;
        }
        if(requestListener != null) {
            requestListener = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_write, container, false);

        moodButtonListener = new MoodButtonClickListener();

        dateTextView = (TextView)rootView.findViewById(R.id.dateTextView);
        weatherImageView = (ImageView)rootView.findViewById(R.id.weatherImageView);
        locationTextView = (TextView)rootView.findViewById(R.id.locationTextView);
        pictureImageView = (ImageView)rootView.findViewById(R.id.pictureImageView);
        PICTURE_WIDTH = pictureImageView.getWidth();
        PICTURE_HEIGHT = pictureImageView.getHeight();
        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialog();
            }
        });

        contentsEditText = (EditText)rootView.findViewById(R.id.contentsEditText);

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

        Button saveButton = (Button)rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    tabListener.onTabSelected(0);       // 일기목록 프래그먼트로 이동
                }
            }
        });

        Button deleteButton = (Button)rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    tabListener.onTabSelected(0);
                }
            }
        });

        Button closeButton = (Button)rootView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    tabListener.onTabSelected(0);
                }
            }
        });

        if(requestListener != null) {
            requestListener.onRequest("getCurrentLocation");
        }

        return rootView;
    }

    public void setLocationTextView(String location) {
        locationTextView.setText(location);
    }

    public void setWeatherImageView(String weatherStr) {
        if(weatherStr.equals("맑음")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_1);
        } else if(weatherStr.equals("구름 조금")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_2);
        } else if(weatherStr.equals("구름 많음")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_3);
        } else if(weatherStr.equals("구름 흐림")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_4);
        } else if(weatherStr.equals("비")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_5);
        } else if(weatherStr.equals("눈/비")) {
            weatherImageView.setImageResource(R.drawable.weather_icon_6);
        } else {
            weatherImageView.setImageResource(R.drawable.weather_icon_7);
        }
    }

    public void setDateTextView(String date) {
        dateTextView.setText(date);
    }

    public void setPictureImageView(Bitmap bitmap) {
        pictureImageView.setImageBitmap(bitmap);
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

    public void showCameraActivity() {

    }

    public void showAlbumAcitivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_ALBUM);
    }

    private void buttonToMoodIndex() {
        if(curButton == null) {
            // 사용자가 아무런 기분도 선택하지 않는 상황
            // 선택할 수 있도록 토스트바를 띄워줘야함
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

    class MoodButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Button selectButton = (Button)v;

            if(curButton == null) {
                selectButton.setScaleX(1.4f);
                selectButton.setScaleY(1.4f);
                curButton = selectButton;
            } else if(curButton == selectButton){
                selectButton.setScaleX(1.0f);
                selectButton.setScaleY(1.0f);
                curButton = null;
            } else {
                curButton.setScaleX(1.0f);
                curButton.setScaleY(1.0f);
                selectButton.setScaleX(1.4f);
                selectButton.setScaleY(1.4f);
                curButton = selectButton;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(requestListener != null) {
            requestListener.onRequest("checkGPS");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if(requestCode == WriteFragment.REQUEST_CAMER) {

            } else if(requestCode == WriteFragment.REQUEST_ALBUM) {
                Log.d(LOG, "onActivityResult() 호출됨(REQUEST_ALBUM)");

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                try {
                    Cursor cursor = getContext().getContentResolver().query(selectedImage, filePathColumn, null, null);
                    cursor.moveToFirst();

                    int index = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(index);
                    cursor.close();

                    Bitmap resultBitmap = decodeFile(filePath, pictureImageView.getWidth(), pictureImageView.getHeight());
                    setPictureImageView(resultBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Bitmap decodeFile(String filePath, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;      // 비트맵을 메모리 할당 전에 먼저 비트맵 크기를 알 수 있음
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
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
}
