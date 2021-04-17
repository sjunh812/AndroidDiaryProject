package org.techtown.diary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.diary.fragment.GraphFragment;
import org.techtown.diary.fragment.ListFragment;
import org.techtown.diary.fragment.WriteFragment;
import org.techtown.diary.helper.KMAGrid;
import org.techtown.diary.helper.MyApplication;
import org.techtown.diary.helper.OnRequestListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;
import org.techtown.diary.weather.WeatherItem;
import org.techtown.diary.weather.WeatherResult;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnTabItemSelectedListener,
        AutoPermissionsListener, OnRequestListener, MyApplication.OnResponseListener {
    // 상수
    private static final String LOG = "MainActivity";
    private static final int REQUEST_ALL_PERMISSIONS = 11;
    private static final int REQUEST_WEATHER_BY_GRID = 1;

    // 프래그먼트
    private ListFragment listFragment;
    private WriteFragment writeFragment;
    private GraphFragment graphFragment;

    // UI
    private BottomNavigationView bottomNavigationView;
    private AlertDialog GPSDialog;                      // GPS 가 꺼져있는 경우 띄워지는 Dialog 창

    // Helper
    private GPSListener gpsListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");

    // 데이터
    private Location curLocation;                       // 현재 위치 정보
    private String curWeatherStr;                       // 현재 날씨
    private Date curDate;                               // 현재 날짜
    private int locationCount = 0;                      // 현재 위치 정보를 찾은 경우 locationCount++ -> 위치 요청 종료

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoPermissions.Companion.loadAllPermissions(this, REQUEST_ALL_PERMISSIONS);      // 위험권한 체크

        listFragment = new ListFragment();
        writeFragment = new WriteFragment();
        graphFragment = new GraphFragment();

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
                        transaction.replace(R.id.container, writeFragment);
                        transaction.commit();

                        return true;
                    case R.id.tab3:
                        transaction.replace(R.id.container, graphFragment);
                        transaction.commit();

                        return true;
                }

                return false;
            }
        });
        onTabSelected(0);       // 일기목록 프래그먼트를 첫 화면으로 지정
    }

    // 현재 위치 정보 가져오기
    public void getCurrentLocation() {
        // 현재 날짜 가져오기
        curDate = new Date();
        String date = dateFormat.format(curDate);
        if(writeFragment != null) {
            writeFragment.setDateTextView(date);
        }

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            if(checkLocationPermission()) {
                curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(curLocation != null) {
                    double latitude = curLocation.getLatitude();               // 위도
                    double longitude = curLocation.getLongitude();             // 경도
                    Log.d(LOG, "LOG : Latitude = " + latitude + ", Longitude = " + longitude);
                }

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

    // 현재 위치정보를 이용해 주소로 변환
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

    // 현재 날씨 정보 가져오기
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

    // 위치 권한 확인
    public boolean checkLocationPermission() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;
    }

    // GPS 확인
    public boolean checkGPS() {
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }

        return true;
    }

    // GPS 확인 Dialog 보여주기
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

    // 위치 서비스 중단
    public void stopLocationService() {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            locationManager.removeUpdates(gpsListener);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 하단 탭 선택간 이벤트 구현
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
            default:
                Log.e(LOG, "ERROR : Tab Position ERROR..");
        }
    }

    // from Fragment(ReqeustListener)
    @Override
    public void onRequest(String command) {
        if(command.equals("getCurrentLocation")) {
            getCurrentLocation();
        }  else if(command.equals("checkGPS")) {
            if(!checkGPS()) {
                showGPSDialog();
            }
        }
    }

    // Volley 응답시 실행되는 자동 호출 함수
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

                    for(int i = 0; i < result.body.data.size(); i++) {
                        WeatherItem item = result.body.data.get(i);

                        switch(item.getDay()) {
                            case 0:
                                Log.d(LOG, "<오늘> " + item.getHour() + "시\n");
                                break;
                            case 1:
                                Log.d(LOG, "<내일>\n" + item.getHour() + "시\n");
                                break;
                            default:
                                Log.d(LOG, "<모레>\n" + item.getHour() + "시\n");
                                break;
                        }

                        Log.d(LOG, "날씨 : " + item.getWfKor() + "\n");
                        Log.d(LOG, "기온 : " + item.getTemp() + "\n");
                    }

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

    // back 키 이벤트
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
            case WriteFragment.REQUEST_CAMERA:
                if(resultCode == RESULT_OK) {
                    if(writeFragment != null) {
                        Log.d(LOG, "onActivityResult : REQUEST_CAMERA (RESULT_OK) " + writeFragment.getFileUri());
                        CropImage.activity(writeFragment.getFileUri()).setGuidelines(CropImageView.Guidelines.ON).start(this);
                    }
                } else {
                    Log.d(LOG, "onActivityResult : REQUEST_CAMERA (NOT RESULT_OK)");
                    if(writeFragment != null) {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.add_image_64_color);
                        writeFragment.setPictureImageView(bitmap);
                    }
                }
                break;

            case WriteFragment.REQUEST_ALBUM:
                if(resultCode == RESULT_OK) {
                    Log.d(LOG, "onActivityResult : REQUEST_ALBUM (RESULT_OK)");

                    Uri uri = data.getData();
                    CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
                } else {
                    Log.d(LOG, "onActivityResult : REQUEST_ALBUM (NOT RESULT_OK)");
                    if(writeFragment != null) {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.add_image_64_color);
                        writeFragment.setPictureImageView(bitmap);
                    }
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    Log.d(LOG, "onActivityResult : CROP_IMAGE_ACTIVITY_REQUEST_CODE (RESULT_OK)");
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    String filePath = result.getUri().getPath();

                    if (writeFragment != null) {
                        Bitmap bitmap = writeFragment.decodeFile(new File(filePath), writeFragment.getPictureWidth(), writeFragment.getPictureHeight());
                        writeFragment.setPictureImageView(bitmap);
                    }
                } else {
                    Log.d(LOG, "onActivityResult : CROP_IMAGE_ACTIVITY_REQUEST_CODE (NOT RESULT_OK)");
                    if(writeFragment != null) {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.add_image_64_color);
                        writeFragment.setPictureImageView(bitmap);
                    }
                }
                break;
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

    // 위치 관리자로부터 위치 정보를 가져오는 리스너
    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            curLocation = location;
            locationCount++;                                        // 현재 위치 정보를 찾았기 때문에 카운팅

            double latitude = location.getLatitude();               // 위도
            double longitude = location.getLongitude();             // 경도

            getCurrentAddress();                                    // 갱신된 위치정보를 주소로 반환(작성 프래그먼트의 locationTextView 갱신)
            getCurrentWeather();                                    // 갱신된 위치정보를 날씨로 반환(작성 프래그먼트의 weatherImageView 갱신)
        }
    }
}