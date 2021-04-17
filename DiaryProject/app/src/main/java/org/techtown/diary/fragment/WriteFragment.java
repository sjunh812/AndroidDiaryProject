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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.diary.CustomDialog;
import org.techtown.diary.MainActivity;
import org.techtown.diary.R;
import org.techtown.diary.helper.OnRequestListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WriteFragment extends Fragment {
    // 상수
    private static final String LOG = "WriteFragment";
    public static final int REQUEST_CAMERA = 21;
    public static final int REQUEST_ALBUM = 22;

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

    // Helper
    private OnTabItemSelectedListener tabListener;
    private OnRequestListener requestListener;          // 메인 액티비티에서 현재 위치 정보를 가져오게 해주는 리스너
    private MoodButtonClickListener moodButtonListener;
    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    // Data
    private int moodIndex = -1;                         // 0~8 총 9개의 기분을 index 로 표현(-1은 사용자가 아무런 기분도 선택하지 않은 경우)
    private Uri fileUri;                                // 카메라로 찍고 난 후 저장되는 파일의 Uri

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
        contentsEditText = (EditText)rootView.findViewById(R.id.contentsEditText);
        pictureImageView.setOnClickListener(new View.OnClickListener() {
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

    public void showCameraActivity() {
        File file = createFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = FileProvider.getUriForFile(getContext(), "org.techtown.diary.fileprovider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        if(intent.resolveActivity(getContext().getPackageManager()) != null) {
            getActivity().startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    public void showAlbumAcitivity() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getActivity().startActivityForResult(intent, REQUEST_ALBUM);
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

            return new File(filePath);
        } else {
            File storageFile = Environment.getExternalStorageDirectory();
            File file = new File(storageFile, fileName);

            return file;
        }
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

    @Override
    public void onResume() {
        super.onResume();

        if(requestListener != null) {
            requestListener.onRequest("checkGPS");
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Bitmap rotateBitmap(Uri uri, Bitmap bitmap) {
        try {
            InputStream inStream = getContext().getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(inStream);
            inStream.close();

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();

            if(orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if(orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if(orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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
}
