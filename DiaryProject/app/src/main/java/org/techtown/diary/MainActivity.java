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
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.diary.custom.CustomStopWriteDialog;
import org.techtown.diary.fragment.CalendarFragment;
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
    /* 상수 */
    private static final String LOG = "MainActivity";                       // 로그 명
    private static final String SELECTED_TAB_INDEX = "selected_tab_index";  // 일시적으로 액티비티가 닫힌 순간 선택되어있던 탭 번호
    private static final int REQUEST_ALL_PERMISSIONS = 11;                  // 모든 위험권한 허용 요청시 사용(AutoPermissions)
    private static final int REQUEST_WEATHER_BY_GRID = 1;                   // 받아온 위도, 경도 정보를 기상청 격자포멧에 맞게 변경 요청시 사용
    private static final int REQUEST_DETAIL_ACTIVITY = 2;                   // 일기 상세 액티비티 요청시 사용

    /* Fragment */
    private ListFragment listFragment;                  // 일기 목록
    private CalendarFragment calendarFragment;          // 기분 달력
    private WriteFragment writeFragment;                // 일기 작성
    private GraphFragment graphFragment;                // 일기 통계
    private OptionFragment optionFragment;              // 더보기

    /* UI */
    private BottomNavigationView bottomNavigationView;  // 하단 탭
    private AlertDialog GPSDialog;                      // GPS 가 꺼져있는 경우 띄워지는 Dialog
    private CustomStopWriteDialog stopWriteDialog;      // 일기 작성 프래그먼트에서 back 키를 누르면 띄워지는 Dialog

    /* Helper */
    private GPSListener gpsListener;                    // 위치 정보를 가져오기 위해 필요한 리스너

    /* DateFormat */
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
    public static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat timeFormat = new SimpleDateFormat("a HH:mm");
    public static SimpleDateFormat timeFormat2 = new SimpleDateFormat("HH:mm:SS");
    public static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    public static SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
    public static SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

    //* Data */
    private NoteDatabase db;                            // 일기 목록을 담은 db
    private Location curLocation;                       // 현재 위치
    private String curWeatherStr;                       // 현재 날씨 String
    private Date curDate;                               // 현재 날짜
    private int locationCount = 0;                      // 현재 위치 정보를 찾으면 해당 값 증가 (locationCount++ = 위치요청(update) 종료)
    private Note updateItem = null;                     // 일기 목록에서 수정할 Note 객체
    private int selectedTabIndex = 0;                   // 현재 선택되어있는 탭 번호 (onSaveInstanceState() 호출시 Bundle 객체로 저장)
    public static MultiTransformation option = new MultiTransformation(new FitCenter(), new RoundedCorners(10));    // Glide 둥근 이미지 뷰
    private Date calDate = null;
    private long backPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyTheme.applyTheme(this);            // 인플레이션 이전에 테마적용(폰트, 다크모드)
        setContentView(R.layout.activity_main);

        AutoPermissions.Companion.loadAllPermissions(this, REQUEST_ALL_PERMISSIONS);    // 위험권한 체크

        /* DB 초기화 */
        db = new NoteDatabase(this);        // DB 객체 생성
        db.dbInit(NoteDatabase.DB_NAME);            // 지정된 이름의 일기 DB 생성
        db.createTable(NoteDatabase.NOTE_TABLE);    // Note 테이블 생성 (중복 제외)

        /* Fragment 초기화 */
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
                        selectedTabIndex = 0;
                        transaction.replace(R.id.container, listFragment);
                        transaction.commit();
                        return true;

                    case R.id.tab2:
                        selectedTabIndex = 1;
                        calendarFragment = new CalendarFragment();
                        transaction.replace(R.id.container, calendarFragment);
                        transaction.commit();
                        return true;

                    case R.id.tab3:
                        selectedTabIndex = 2;
                        writeFragment = new WriteFragment();

                        if(calDate != null) {                   // 기분달력으로부터 넘어온 경우
                            writeFragment.setCalDate(calDate);  // 기분달력에서 가져온 Date 정보 전달
                            calDate = null;                     // calDate 초기화
                        }

                        if(updateItem != null) {                // 일기 수정 시
                            writeFragment.setItem(updateItem);  // updateItem 을 WriteFragment 로 전달
                            updateItem = null;                  // updateItem 초기화
                        }

                        transaction.replace(R.id.container, writeFragment);
                        transaction.commit();
                        return true;

                    case R.id.tab4:
                        selectedTabIndex = 3;
                        transaction.replace(R.id.container, graphFragment);
                        transaction.commit();
                        return true;

                    case R.id.tab5:
                        selectedTabIndex = 4;
                        transaction.replace(R.id.container, optionFragment);
                        transaction.commit();
                        return true;
                }

                return false;
            }
        });

        if(savedInstanceState == null) {     // onSaveInstanceState() 호출 x
            onTabSelected(0);       // 일기목록 프래그먼트를 첫 화면으로 지정
        } else {
            /* 폰트설정 or 다크모드 설정 후 recreate() 호출, 기존 프래그먼트로 돌아와야하는 상황 */
            /* onSaveInstanceState() 호출 o */
            int index = savedInstanceState.getInt(SELECTED_TAB_INDEX);
            onTabSelected(index);           // 저장된 탭 번호에 맞는 프래그먼트 화면으로 지정
        }
    }

    public void getCurrentLocation(Date date) {                 // 날짜정보 + 위치정보 + 날씨정보
        if(date == null) {
            curDate = new Date();                               // 현재 날짜정보
        } else {
            curDate = date;
        }
        String curYear = yearFormat.format(curDate);            // yyyy
        String curMonth = monthFormat.format(curDate);          // MM
        String curDay = dayFormat.format(curDate);              // dd
        String _date = dateFormat.format(curDate);              // yyyy년 MM월 dd일

        if(writeFragment != null) {
            writeFragment.setDateTextView(_date);
            try {
                writeFragment.setCurDate(Integer.parseInt(curYear), Integer.parseInt(curMonth), Integer.parseInt(curDay));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        /* 위치 정보를 가져오기 위해 필요한 LocationManager 시스템 서비스 객체 정의 */
        /* LocationManager 에서 위치 정보를 얻기 위해 LocationListener 이용 */
        /* GPSListener 라는 이름으로 LocationListener 를 재정의 */
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        try {
            if(checkLocationPermission()) {
                curLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);   // 제일 최근 위치 정보를 저장

                gpsListener = new GPSListener();                               // 위치정보를 가져오기 위해 리스너 설정
                long minTime = 10000;                                          // 업데이트 주기 10초
                float minDistance = 0;                                         // 업데이트 거리간격 0
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);    // 위치 업데이트

                getCurrentAddress();                                           // 위치정보를 주소로 반환 (작성 프래그먼트의 locationTextView 갱신)
                getCurrentWeather();                                           // 위치정보를 이용해 날씨 반환 (작성 프래그먼트의 weatherImageView 갱신)
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
                String adminArea = address.getAdminArea();              // 인천광역시, 서울특별시..
                String locality = address.getLocality();                // 시
                String subLocality = address.getSubLocality();          // 구
                String thoroughfare = address.getThoroughfare();        // 동
                String subThoroughfare = address.getSubThoroughfare();  // 읍 면?
                String subStr = subLocality;

                if(locality == null) {                                  // 서울특별시나 인천광역시와 같이 '시'가 없는 지역인 경우 예외 처리
                    locality = adminArea;
                }

                if(subLocality == null) {                               // 구, 동이 없는 지역인 경우 예외 처리
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
        /* 현재 위치의 위도, 경도들 이용하여 기상청이 만든 격자포멧으로 변환 */
        Map<String, Double> map = KMAGrid.getKMAGrid(curLocation.getLatitude(), curLocation.getLongitude());

        double gridX = map.get("X");
        double gridY = map.get("Y");
        //Log.d(LOG, "LOG : gridX = " + gridX + ", gridY = " + gridY);

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

    public boolean checkLocationPermission() {      // 위치 위험권한 허용 여부 판단
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    public boolean checkGPS() {     // 사용자가 위치 기능을 켰는지 여부 판단
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    public void showGPSDialog() {       // checkGPS() 를 통해 위치기능을 켰는지 여부 확인 -> 켜지않은 경우 Dialog를 띄워줌
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

    public void showStopWriteDialog() {
        stopWriteDialog = new CustomStopWriteDialog(this);
        stopWriteDialog.show();

        stopWriteDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWriteDialog.dismiss();
            }
        });

        stopWriteDialog.setBackButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWriteDialog.dismiss();
                bottomNavigationView.setSelectedItemId(R.id.tab1);
            }
        });

        stopWriteDialog.setContinueButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWriteDialog.dismiss();
            }
        });
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

    @Override
    public void insertDB(Object[] objs) {
        if(db != null) {
            db.insert(NoteDatabase.NOTE_TABLE, objs);
        }
    }

    @Override
    public void insertDB2(Object[] objs) {
        if(db != null) {
            db.insert2(NoteDatabase.NOTE_TABLE, objs);
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
    public ArrayList<Note> selectPart(int year, int month) {
        ArrayList<Note> items = new ArrayList<>();
        if(db != null) {
            items = db.selectPart(NoteDatabase.NOTE_TABLE, year, month);
        }

        return items;
    }

    @Override
    public HashMap<Integer, Integer> selectMoodCount(boolean isAll, boolean isYear, boolean isMonth) {
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        if(db != null) {
            hashMap = db.selectMoodCount(isAll, isYear, isMonth);
        }

        return hashMap;
    }

    @Override
    public HashMap<Integer, Integer> selectMoodCountWeek(int weekOfDay) {
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        if(db != null) {
            hashMap = db.selectMoodCountWeek(weekOfDay);
        }

        return hashMap;
    }

    @Override
    public int selectLastYear() {
        if(db != null) {
            return db.selectLastYear();
        }

        return 0;
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

    @Override
    public void updateDB2(Note item) {
        if(db != null) {
            db.update2(NoteDatabase.NOTE_TABLE, item);
        }
    }

    /* OnTabItemSelectedListener 구현(하단 탭 선택간 이벤트 구현) */
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
            case 4:
                bottomNavigationView.setSelectedItemId(R.id.tab5);
                break;
            default:
                Log.e(LOG, "ERROR : 하단 탭 선택 에러 발생..");
                break;
        }
    }

    /* OnRequestListener 구현 */
    @Override
    public void onRequest(String command) {
        if(command.equals("getCurrentLocation")) {
            getCurrentLocation(null);
        } else if(command.equals("checkGPS")) {
            if(!checkGPS()) {
                showGPSDialog();
            }
        } else if(command.equals("showStopWriteDialog")) {
            showStopWriteDialog();
        }
    }

    /* OnRequestListener 구현 */
    /* 기분달력으로부터 일기작성으로 넘어간 경우에 호출 */
    @Override
    public void onRequest(String command, Date date) {
        if(command.equals("getCurrentLocation")) {
            getCurrentLocation(date);
        }
    }

    /* OnRequestListener 구현 */
    /* 일기목록에서 일기 클릭시 상세보기 액티비티로 전환 */
    @Override
    public void onRequestDetailActivity(Note item) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("item", item);

        startActivityForResult(intent, REQUEST_DETAIL_ACTIVITY);
    }

    /* OnRequestListener 구현 */
    /* 기분달력에서 일기가 없는 특정 날짜 데이터를 가지고 일기작성 액티비티로 전환 */
    @Override
    public void onRequestWriteFragmentFromCal(Date date) {
        calDate = date;
        onTabSelected(2);
    }

    /* OnResponseListener 구현 (Volley 응답시 호출) */
    @Override
    public void onResponse(int reqeustCode, int responseCode, String response) {
        if(responseCode == MyApplication.RESPONSE_OK) {
            if(reqeustCode == REQUEST_WEATHER_BY_GRID) {                // 기상청으로 날씨 요청
                XmlParserCreator creator = new XmlParserCreator() {     // Xml -> Gson
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

                    if(locationCount > 0) {     // 위치정보를 얻었기 때문에 LocationManager 에서 update 중지
                        stopLocationService();
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.e(LOG, "ERROR : Failure response code = " + responseCode);
        }
    }

    @Override
    public void showWriteFragment(Note item) {      // 수정하기로 WriteFragment 접근 시
        updateItem = item;
        onTabSelected(2);
    }

    @Override
    public void onBackPressed() {
        if(bottomNavigationView.getSelectedItemId() == R.id.tab1) {
            if(System.currentTimeMillis() > backPressTime + 2000) {
                backPressTime = System.currentTimeMillis();
                Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(System.currentTimeMillis() <= backPressTime + 2000) {
                super.onBackPressed();
            }
        } else if(bottomNavigationView.getSelectedItemId() == R.id.tab3) {
            showStopWriteDialog();
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
                        getContentResolver().delete(writeFragment.getFileUri(), null, null);
                    }
                }
                break;

            case WriteFragment.REQUEST_ALBUM:      // 앨범으로 부터
                if(resultCode == RESULT_OK) {
                    Log.d(LOG, "onActivityResult : REQUEST_ALBUM (RESULT_OK)");

                    Uri uri = data.getData();
                    CropImage.activity(uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
                } else {
                    Log.d(LOG, "onActivityResult : REQUEST_ALBUM (NOT RESULT_OK)");
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    Log.d(LOG, "onActivityResult : CROP_IMAGE_ACTIVITY_REQUEST_CODE (RESULT_OK)");

                    writeFragment.deleteFileCache();
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    String filePath = result.getUri().getPath();

                    if (writeFragment != null) {
                        writeFragment.setFilePath(filePath);
                        writeFragment.setPictureImageView(null, result.getUri(), -1);
                    }
                } else {
                    Log.d(LOG, "onActivityResult : CROP_IMAGE_ACTIVITY_REQUEST_CODE (NOT RESULT_OK)");
                }
                break;

            case OptionFragment.REQUEST_FONT_CHANGE:
                if(resultCode == RESULT_OK) {
                    recreate();
                }
                break;
            case REQUEST_DETAIL_ACTIVITY:
                if(resultCode == DetailActivity.RESULT_DELETE) {
                    int id = data.getIntExtra("id", -1);
                    if(id != -1) {
                        deleteDB(id);
                        if(selectedTabIndex == 0) {
                            if(listFragment != null) {
                                listFragment.update();
                            }
                        } else {
                            if(calendarFragment != null) {
                                onTabSelected(1);
                            }
                        }
                    }
                } else if(resultCode == DetailActivity.RESULT_UPDATE) {
                    Note item = (Note)data.getSerializableExtra("item");
                    if(item != null) {
                        showWriteFragment(item);
                    }
                }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAB_INDEX, selectedTabIndex);

        Log.d(LOG, "onSaveInstanceState()호출됨");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int i, String[] strings) {
        for(String permission : strings) {
            if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(getApplicationContext(), "날씨 및 작성 위치를 가져오기 위해 위치정보가 필요합니다.\n" +
                        "설정->위치->앱 권한에서 허용해주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onGranted(int i, String[] strings) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /* 위치 관리자 (LocationManager)에서 위치정보를 가져오기 위해 필요한 리스너 */
    /* LocationListener 를 상속받은 커스텀 GPSListener 클래스 선언 */
    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            curLocation = location;                                 // 가져온 위치정보를 curLocation 객체에 대입
            locationCount++;                                        // 위치정보를 찾았기 때문에 카운팅해 더이상 update 하지않게 함

            getCurrentAddress();                                    // 갱신된 위치정보를 주소로 반환 (작성 프래그먼트의 locationTextView 갱신)
            getCurrentWeather();                                    // 갱신된 위치정보를 날씨로 반환 (작성 프래그먼트의 weatherImageView 갱신)
        }
    }
}