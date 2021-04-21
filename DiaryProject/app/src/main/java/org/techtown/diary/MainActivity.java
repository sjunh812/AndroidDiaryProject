package org.techtown.diary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.diary.fragment.GraphFragment;
import org.techtown.diary.fragment.ListFragment;
import org.techtown.diary.fragment.OptionFragment;
import org.techtown.diary.fragment.WriteFragment;
import org.techtown.diary.helper.KMAGrid;
import org.techtown.diary.helper.MyApplication;
import org.techtown.diary.helper.MyTheme;
import org.techtown.diary.helper.OnRequestListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;
import org.techtown.diary.note.Note;
import org.techtown.diary.note.NoteDatabaseCallback;
import org.techtown.diary.note.NoteDatabase;
import org.techtown.diary.weather.WeatherItem;
import org.techtown.diary.weather.WeatherResult;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnTabItemSelectedListener,
        AutoPermissionsListener, OnRequestListener, MyApplication.OnResponseListener, NoteDatabaseCallback {
    // 상수
    private static final String LOG = "MainActivity";
    private static final int REQUEST_ALL_PERMISSIONS = 11;  // 모든 위험권한 허용 요청시 사용(AutoPermissions)
    private static final int REQUEST_WEATHER_BY_GRID = 1;   // 받아온 위도, 경도 정보를 기상청 격자포멧에 맞게 변경 요청시 사용
    private static final String IS_RECREATE_KEY = "recreate_key";

    // 프래그먼트
    private ListFragment listFragment;
    private WriteFragment writeFragment;
    private GraphFragment graphFragment;
    private OptionFragment optionFragment;

    // UI
    private BottomNavigationView bottomNavigationView;  // 하단 탭
    private AlertDialog GPSDialog;                      // GPS 가 꺼져있는 경우 띄워지는 Dialog 창

    // Helper
    private GPSListener gpsListener;                    // 위치 정보를 가져오기 위해 필요한 리스너
    private NoteDatabase db;                            // 일기 목록을 담은 db
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("a HH:mm");

    // 데이터
    private Location curLocation;                       // 현재 위치 정보
    private String curWeatherStr;                       // 현재 날씨
    private Date curDate;                               // 현재 날짜
    private int locationCount = 0;                      // 현재 위치 정보를 찾은 경우 locationCount++ -> 위치 요청 종료
    private Note updateItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyTheme.applyTheme(this);
        setContentView(R.layout.activity_main);

        AutoPermissions.Companion.loadAllPermissions(this, REQUEST_ALL_PERMISSIONS);      // 위험권한 체크

        // db 초기화
        db = new NoteDatabase(this);
        db.dbInit(NoteDatabase.DB_NAME);
        db.createTable(NoteDatabase.NOTE_TABLE);

        listFragment = new ListFragment();
        graphFragment = new GraphFragment();
        optionFragment = new OptionFragment();

        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();

                switch(item.getItemId()) {
                    case R.id.tab1:
                        transaction.replace(R.id.container, listFragment);
                        transaction.commit();

                        return true;
                    case R.id.tab2:
                        writeFragment = new WriteFragment();
                        if(updateItem != null) {
                            writeFragment.setItem(updateItem);
                            updateItem = null;
                        }
                        transaction.replace(R.id.container, writeFragment);
                        transaction.commit();

                        return true;
                    case R.id.tab3:
                        transaction.replace(R.id.container, graphFragment);
                        transaction.commit();

                        return true;
                    case R.id.tab4:
                        transaction.replace(R.id.container, optionFragment);
                        transaction.commit();
                        return true;
                }

                return false;
            }
        });

        if(savedInstanceState == null) {
            onTabSelected(0);       // 일기목록 프래그먼트를 첫 화면으로 지정
        } else {
            onTabSelected(3);       // 일기목록 프래그먼트를 첫 화면으로 지정
        }
    }

    public void getCurrentLocation() {
        curDate = new Date();
        String date = dateFormat.format(curDate);

        if(writeFragment != null) {
            writeFragment.setDateTextView(date);
        }

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            if(checkLocationPermission()) {
                curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                gpsListener = new GPSListener();
                long minTime = 10000;                                          // 업데이트 주기 10초
                float minDistance = 0;                                         // 업데이트 거리간격 0
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);

                getCurrentAddress();                                           // 위치정보를 주소로 반환(작성 프래그먼트의 locationTextView 갱신)
                getCurrentWeather();                                           // 위치정보를 이용해 날씨 반환(작성 프래그먼트의 weatherImageView 갱신)
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void getCurrentAddress() {
        Geocoder geoCoder = new Geocoder(this);

        try {
            List<Address> list = geoCoder.getFromLocation(curLocation.getLatitude(), curLocation.getLongitude(), 1);

            if(list != null && list.size() > 0) {
                Address address = list.get(0);                          // 현재 주소 정보를 가진 Address 객체
                String locality = address.getLocality();                // 시
                String subLocality = address.getSubLocality();          // 구
                String thoroughfare = address.getThoroughfare();        // 동
                String subThoroughfare = address.getSubThoroughfare();  // 읍 면?
                String subStr = subLocality;

                if(subLocality == null) {
                    subStr = thoroughfare;
                    if(thoroughfare == null) {
                        subStr = subThoroughfare;
                        if(subThoroughfare == null) {
                            subStr = "";
                        }
                    }
                }
                StringBuilder stringBuilder = new StringBuilder().append(locality).append(" ").append(subStr);

                if(writeFragment != null) {
                    writeFragment.setLocationTextView(stringBuilder.toString());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void getCurrentWeather() {
        Map<String, Double> map = KMAGrid.getKMAGrid(curLocation.getLatitude(), curLocation.getLongitude());

        double gridX = map.get("X");
        double gridY = map.get("Y");
        Log.d(LOG, "LOG : gridX = " + gridX + ", gridY = " + gridY);

        String url = "https://www.kma.go.kr/wid/queryDFS.jsp";
        url += "?gridx=" + Math.round(gridX);
        url += "&gridy=" + Math.round(gridY);

        Map<String, String> params = new HashMap<>();
        MyApplication.request(REQUEST_WEATHER_BY_GRID, Request.Method.GET, url, params, this);
    }

    public void stopLocationService() {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.removeUpdates(gpsListener);
            Log.d(LOG, "위치 업데이트 종료");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkLocationPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }

    public boolean checkGPS() {
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }

        return true;
    }

    public void showGPSDialog() {
        if(GPSDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("위치 설정을 허용해주세요.")
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "위치 정보를 가져오려면 위치 설정을 허용해야합니다.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setPositiveButton("설정하러 가기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
            GPSDialog = builder.create();
        }

        GPSDialog.show();
    }

    public static String getDayOfWeek(Date date) {
        String dayOfWeek = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayNum = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayNum) {
            case 1:
                dayOfWeek = "일";
                break;
            case 2:
                dayOfWeek = "월";
                break;
            case 3:
                dayOfWeek = "화";
                break;
            case 4:
                dayOfWeek = "수";
                break;
            case 5:
                dayOfWeek = "목";
                break;
            case 6:
                dayOfWeek = "금";
                break;
            case 7:
                dayOfWeek = "토";
                break;
        }

        return dayOfWeek;
    }

    // DB 관련 함수
    @Override
    public void insertDB(Object[] objs) {
        if(db != null) {
            db.insert(NoteDatabase.NOTE_TABLE, objs);
        }
    }
    @Override
    public ArrayList<Note> selectAllDB() {
        ArrayList<Note> items = new ArrayList<>();
        if(db != null) {
            items = db.selectAll(NoteDatabase.NOTE_TABLE);
        }

        return items;
    }

    @Override
    public void deleteDB(int id) {
        if(db != null) {
            db.delete(NoteDatabase.NOTE_TABLE, id);
        }
    }

    @Override
    public void updateDB(Note item) {
        if(db != null) {
            db.update(NoteDatabase.NOTE_TABLE, item);
        }
    }

    // OnTabItemSelectedListener 구현(하단 탭 선택간 이벤트 구현)
    @Override
    public void onTabSelected(int position) {
        switch(position) {
            case 0:
                bottomNavigationView.setSelectedItemId(R.id.tab1);
                break;
            case 1:
                bottomNavigationView.setSelectedItemId(R.id.tab2);
                break;
            case 2:
                bottomNavigationView.setSelectedItemId(R.id.tab3);
                break;
            case 3:
                bottomNavigationView.setSelectedItemId(R.id.tab4);
                break;
            default:
                Log.e(LOG, "ERROR : 하단 탭 선택 에러 발생..");
                break;
        }
    }

    @Override
    public void showWriteFragment(Note item) {
        updateItem = item;
        onTabSelected(1);
        //getSupportFragmentManager().beginTransaction().replace(R.id.container, writeFragment).commit();
    }

    // OnRequestListener 구현
    @Override
    public void onRequest(String command) {
        if(command.equals("getCurrentLocation")) {
            getCurrentLocation();
        } else if(command.equals("checkGPS")) {
            if(!checkGPS()) {
                showGPSDialog();
            }
        }
    }

    // OnResponseListener 구현(Volley 응답시 실행되는 자동 호출 함수)
    @Override
    public void onResponse(int reqeustCode, int responseCode, String response) {
        if(responseCode == MyApplication.RESPONSE_OK) {
            if(reqeustCode == REQUEST_WEATHER_BY_GRID) {
                XmlParserCreator creator = new XmlParserCreator() {
                    @Override
                    public XmlPullParser createParser() {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                GsonXml gsonXml = new GsonXmlBuilder()
                        .setXmlParserCreator(creator)
                        .setSameNameLists(true)
                        .create();

                try {
                    WeatherResult result = gsonXml.fromXml(response, WeatherResult.class);
                    WeatherItem item = result.body.data.get(0);
                    curWeatherStr = item.getWfKor();

                    if(writeFragment != null) {
                        writeFragment.setWeatherImageView(curWeatherStr);
                    }

                    if(locationCount > 0) {
                        stopLocationService();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(LOG, "ERROR : Unknown request code = " + responseCode);
            }
        } else {
            Log.e(LOG, "ERROR : Failure response cole = " + responseCode);
        }
    }

    @Override
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId() == R.id.tab1) {
            super.onBackPressed();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.tab1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case WriteFragment.REQUEST_CAMERA:      // 카메라 앱으로 부터
                if(resultCode == RESULT_OK) {
                    if(writeFragment != null) {
                        Log.d(LOG, "onActivityResult : REQUEST_CAMERA (RESULT_OK) " + writeFragment.getFileUri());
                        CropImage.activity(writeFragment.getFileUri()).setGuidelines(CropImageView.Guidelines.ON).start(this);
                    }
                } else {
                    Log.d(LOG, "onActivityResult : REQUEST_CAMERA (NOT RESULT_OK)");
                    if(writeFragment != null) {
                        //writeFragment.checkDeleteCache();
                        getContentResolver().delete(writeFragment.getFileUri(), null, null);
                        Log.d(LOG, "파일 삭제완료");
                        //writeFragment.setFilePath("");
                        //writeFragment.setPictureImageView(null, null, R.drawable.add_image_64_color);
                    }
                }
                break;

            case WriteFragment.REQUEST_ALBUM:      // 앨범으로 부터
                if(resultCode == RESULT_OK) {
                    Log.d(LOG, "onActivityResult : REQUEST_ALBUM (RESULT_OK)");

                    Uri uri = data.getData();
                    CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
                } else {
                    //writeFragment.checkDeleteCache();
                    Log.d(LOG, "onActivityResult : REQUEST_ALBUM (NOT RESULT_OK)");
                    if(writeFragment != null) {
                        //writeFragment.setFilePath("");
                        //writeFragment.setPictureImageView(null, null, R.drawable.add_image_64_color);
                    }
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    writeFragment.checkDeleteCache();

                    Log.d(LOG, "onActivityResult : CROP_IMAGE_ACTIVITY_REQUEST_CODE (RESULT_OK)");
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    String filePath = result.getUri().getPath();

                    if (writeFragment != null) {
                        writeFragment.setFilePath(filePath);
                        writeFragment.setPictureImageView(null, result.getUri(), -1);
                    }
                } else {
                   // writeFragment.checkDeleteCache();

                    Log.d(LOG, "onActivityResult : CROP_IMAGE_ACTIVITY_REQUEST_CODE (NOT RESULT_OK)");
                    if(writeFragment != null) {
                        //writeFragment.setFilePath("");
                        //writeFragment.setPictureImageView(null, null, R.drawable.add_image_64_color);
                    }
                }
                break;
            case OptionFragment.REQUEST_FONT_CHANGE:
                if(resultCode == RESULT_OK) {
                    Log.d(LOG, "recreate()호출됨");
                    recreate();
                }
                break;
        }
    }

    // 위치 관리자로부터 위치 정보를 가져오는 리스너
    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            curLocation = location;
            locationCount++;                                        // 현재 위치 정보를 찾았기 때문에 카운팅

            getCurrentAddress();                                    // 갱신된 위치정보를 주소로 반환(작성 프래그먼트의 locationTextView 갱신)
            getCurrentWeather();                                    // 갱신된 위치정보를 날씨로 반환(작성 프래그먼트의 weatherImageView 갱신)
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int i, String[] strings) {

    }

    @Override
    public void onGranted(int i, String[] strings) {

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_RECREATE_KEY, true);
    }

    /*    public Bitmap decodeUri(Uri uri, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;      // 비트맵을 메모리 할당 전에 먼저 비트맵 크기를 알 수 있음
        try {
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
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
    }*/
}