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
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.techtown.diary.fragment.GraphFragment;
import org.techtown.diary.fragment.ListFragment;
import org.techtown.diary.fragment.WriteFragment;
import org.techtown.diary.helper.KMAGrid;
import org.techtown.diary.helper.MyApplication;
import org.techtown.diary.helper.OnRequestListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnTabItemSelectedListener,
        AutoPermissionsListener, OnRequestListener, MyApplication.OnResponseListener {
    // 상수
    private static final String LOG = "MainActivity";
    private static final int CHECK_ALL_PERMISSIONS = 101;

    // 프래그먼트
    private ListFragment listFragment;
    private WriteFragment writeFragment;
    private GraphFragment graphFragment;

    // UI
    private BottomNavigationView bottomNavigationView;
    private AlertDialog GPSDialog;                      // GPS 가 꺼져있는 경우 띄워지는 Dialog 창

    // Helper
    private GPSListener gpsListener;

    // 데이터
    private Location curLocation;                       // 현재 위치 정보
    private int locationCount = 0;                      // 현재 위치 정보를 찾은 경우 locationCount++ -> 위치 요청 종료

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoPermissions.Companion.loadAllPermissions(this, CHECK_ALL_PERMISSIONS);      // 위험권한 체크

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

    // from Fragment (ReqeustListener)
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
            if(reqeustCode == 101) {

            }
        } else {

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
        }
    }
}