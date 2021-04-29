package org.techtown.diary.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import org.techtown.diary.MainActivity;
import org.techtown.diary.R;
import org.techtown.diary.custom.MyRadioButton;
import org.techtown.diary.helper.MyApplication;
import org.techtown.diary.helper.MyTheme;
import org.techtown.diary.note.NoteDatabase;
import org.techtown.diary.note.NoteDatabaseCallback;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GraphFragment extends Fragment {
    // 상수
    private static final String LOG = "GraphFragment";

    // Radio Button UI
    private RadioGroup radioGroup;
    private MyRadioButton allButton;
    private MyRadioButton yearButton;
    private MyRadioButton monthButton;

    // 기분별 통계 UI
    private TextView moodTitleTextView;
    private TextView moodTotalCountTextView;
    private TextView angryCount;
    private TextView coolCount;
    private TextView cryingCount;
    private TextView illCount;
    private TextView laughCount;
    private TextView mehCount;
    private TextView sadCount;
    private TextView smileCount;
    private TextView yawnCount;
    private ImageView crown;
    private ImageView crown2;
    private ImageView crown3;
    private ImageView crown4;
    private ImageView crown5;
    private ImageView crown6;
    private ImageView crown7;
    private ImageView crown8;
    private ImageView crown9;


    // 차트 라이브러리 객체
    private PieChart chart1;              // 원형 그래프
    //private BarChart chart2;            // 막대 그래프
    //private LineChart chart3;           // 선 그래프

    // Helper
    private NoteDatabaseCallback callback;

    // 데이터
    private Context context;
    private ArrayList<Integer> colors = new ArrayList<>();      // 색깔 정보를 담은 ArrayList<Integer>
    int[] moodIconRes = {R.drawable.mood_angry_color, R.drawable.mood_cool_color,  R.drawable.mood_crying_color,
            R.drawable.mood_ill_color, R.drawable.mood_laugh_color, R.drawable.mood_meh_color,
            R.drawable.mood_sad, R.drawable.mood_smile_color, R.drawable.mood_yawn_color};
    private int curFontIndex = -1;                              // 현재 사용중인 폰트 종류
    private int selectRadioIndex = 0;                           // 전체보기 : 0, 올해 : 1, 이번달 : 2(default : 전체보기)
    private int maxMoodIndex = -1;                              // 제일 많은 개수를 가진 기분 종류
    private int maxCount = -1;                                  // 제일 많은 개수를 가진 기분의 count 값

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

        if(context instanceof NoteDatabaseCallback) {
            callback = (NoteDatabaseCallback)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(callback != null) {
            callback = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);

        // 휴대폰 내 저장되어있는 폰트 정보를 가져옴(SharedPreferences 이용)
        SharedPreferences pref = getContext().getSharedPreferences(MyTheme.SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        if(pref != null) {
            curFontIndex = pref.getInt(MyTheme.FONT_KEY, 0);
        }

        initChartUI(rootView);       // 차트 초기화

        // 기분별 통계 UI
        moodTotalCountTextView = (TextView)rootView.findViewById(R.id.moodTotalCountTextView);
        moodTitleTextView = (TextView)rootView.findViewById(R.id.moodTitleTextView);
        angryCount = (TextView)rootView.findViewById(R.id.angryCount);
        coolCount = (TextView)rootView.findViewById(R.id.coolCount);
        cryingCount = (TextView)rootView.findViewById(R.id.cryingCount);
        illCount = (TextView)rootView.findViewById(R.id.illCount);
        laughCount = (TextView)rootView.findViewById(R.id.laughCount);
        mehCount = (TextView)rootView.findViewById(R.id.mehCount);
        sadCount = (TextView)rootView.findViewById(R.id.sadCount);
        smileCount = (TextView)rootView.findViewById(R.id.smileCount);
        yawnCount = (TextView)rootView.findViewById(R.id.yawnCount);
        crown = (ImageView)rootView.findViewById(R.id.crown);
        crown2 = (ImageView)rootView.findViewById(R.id.crown2);
        crown3 = (ImageView)rootView.findViewById(R.id.crown3);
        crown4 = (ImageView)rootView.findViewById(R.id.crown4);
        crown5 = (ImageView)rootView.findViewById(R.id.crown5);
        crown6 = (ImageView)rootView.findViewById(R.id.crown6);
        crown7 = (ImageView)rootView.findViewById(R.id.crown7);
        crown8 = (ImageView)rootView.findViewById(R.id.crown8);
        crown9 = (ImageView)rootView.findViewById(R.id.crown9);

        // Radio Button 관련
        allButton = (MyRadioButton)rootView.findViewById(R.id.allButton);
        yearButton = (MyRadioButton)rootView.findViewById(R.id.yearButton);
        monthButton = (MyRadioButton)rootView.findViewById(R.id.monthButton);
        radioGroup = (RadioGroup)rootView.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                HashMap<Integer, Integer> hashMap = null;

                if(checkedId == R.id.allButton) {
                    moodTitleTextView.setText("전체");
                    selectRadioIndex = 0;
                    hashMap = callback.selectMoodCount(true, false, false);
                    //chart1.setCenterText("전체");     // 원형 그래프 가운데 text 표기

                } else if(checkedId == R.id.yearButton) {
                    moodTitleTextView.setText(MainActivity.yearFormat.format(new Date()) + "년");
                    selectRadioIndex = 1;
                    hashMap = callback.selectMoodCount(false, true, false);
                    //chart1.setCenterText(MainActivity.yearFormat.format(new Date()) + "년");     // 원형 그래프 가운데 text 표기

                } else if(checkedId == R.id.monthButton) {
                    moodTitleTextView.setText(Integer.parseInt(MainActivity.monthFormat.format(new Date())) + "월");
                    selectRadioIndex = 2;
                    hashMap = callback.selectMoodCount(false, false, true);
                   // chart1.setCenterText(Integer.parseInt(MainActivity.monthFormat.format(new Date())) + "월");      // 원형 그래프 가운데 text 표기
                }

                chart1.setCenterTextTypeface(getCurTypeFace());
                chart1.setCenterTextSize(17f);
                setData1(hashMap);
            }
        });

        setSelectedRadioButton();       // 선택된 라디오버튼 index 에 따라 라디오버튼 Checked 활성화

        return rootView;
    }

    private void initChartUI(View rootView) {
        chart1 = (PieChart)rootView.findViewById(R.id.chart1);
        //chart2 = (BarChart)rootView.findViewById(R.id.chart2);
        //chart3 = (LineChart)rootView.findViewById(R.id.chart3);

        // 원형 그래프(기분별)
        chart1.setUsePercentValues(true);
        chart1.getDescription().setEnabled(false);       // 추가 설명란 false
        chart1.setDrawHoleEnabled(false);
        chart1.setExtraOffsets(5,10,5,10);
        chart1.setHighlightPerTapEnabled(false);        // 특정부분 선택시 확대효과 여부

        //chart1.setTransparentCircleColor(getResources().getColor(R.color.white));   // 중간원과 바깥원 사이의 얇은 투명원의 색상 결정
        //chart1.setTransparentCircleAlpha(110);           // 중간원과 바깥원 사이의 얇은 투명원의 알파 값 결정
        //chart1.setTransparentCircleRadius(66f);          // 중간원과 바깥원 사이의 얇은 투명원의 반지름
        //chart1.setHoleRadius(58f);                       // 중간원의 반지름
        //chart1.setHoleColor(getResources().getColor(R.color.azure2));
        //chart1.setDrawCenterText(true);
        Legend legend1 = chart1.getLegend();             // 그래프의 구성요소들을 추가로 명시하는지 여부
        legend1.setEnabled(false);                        // 추가 구성요소 명시 false
        chart1.setEntryLabelColor(Color.WHITE);          // entry label 색상
        //chart1.setEntryLabelTextSize(12f);               // entry 구성요소 label 크기
        chart1.animateXY(1200, 1200);

        /*// 막대 그래프(요일별)
        chart2.setDrawValueAboveBar(true);              // 그래프에 특정 값 표기시에 막대그래프 위에 표기 true
        chart2.getDescription().setEnabled(false);      // 추가 설명란 false
        chart2.setDrawGridBackground(false);            // 그래프 격자 배경 그릴지 여부 false

        XAxis xAxis = chart2.getXAxis();                // x축 설정
        xAxis.setEnabled(false);                        // x축 요소들 선 표시(세로줄) false
        YAxis leftAxis = chart2.getAxisLeft();          // 왼쪽 y축
        leftAxis.setLabelCount(6, false);   // 왼쪽 y축에 표시할 label 개수
        leftAxis.setAxisMinimum(0f);                    // y축 최소값 0으로
        leftAxis.setGranularityEnabled(true);
        leftAxis.setGranularity(1f);
        YAxis rightAxis = chart2.getAxisRight();        // 오른쪽 y축
        rightAxis.setEnabled(false);                    // 오른쪽 y축 사용 x
        Legend legend2 = chart2.getLegend();            // 그래프의 구성요소들을 추가로 명시하는지 여부
        legend2.setEnabled(false);                      // 추가 구성요소 명시 false
        chart2.animateXY(1500, 1500);   // 애니메이션 설정
        setData2();

        // 선 그래프(기분 변화)
        chart3.getDescription().setEnabled(false);                  // 추가 설명란 false
        chart3.setDrawGridBackground(false);                        // 그래프 격자 배경 그릴지 여부 false
        //chart3.setBackgroundColor(Color.WHITE);                   // 배경색 지정(흰색)
        chart3.setExtraOffsets(22, 22,22,22); // 그래프 추가 offset
        Legend legend3 = chart3.getLegend();                        // 그래프의 구성요소들을 추가로 명시하는지 여부
        legend3.setEnabled(false);                                  // 추가 구성요소 명시 false

        XAxis xAxis3 = chart3.getXAxis();                           // x축 설정
        xAxis3.setPosition(XAxis.XAxisPosition.BOTTOM);             // x축 label 위치 설정
        xAxis3.setTextSize(10f);                                    // x축 label 크기
        xAxis3.setTextColor(Color.BLACK);                           // x축 label 색상
        xAxis3.setDrawGridLines(false);                             // x축 격자선 표시 여부 false
        xAxis3.setCenterAxisLabels(true);                           // x축 각 label 들을 각 칸 중간에 표기 할지 여부 true
        xAxis3.setGranularityEnabled(true);
        xAxis3.setGranularity(1f);
        xAxis3.setValueFormatter(new ValueFormatter() {             // x축을 구성할 요소의 포멧을 정의
            private final SimpleDateFormat mFormat = new SimpleDateFormat("MM-DD", Locale.KOREA);       // 월일 포멧 ex) 04-12

            @Override
            public String getFormattedValue(float value) {
                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis3 = chart3.getAxisLeft();                         // y축 설정
        leftAxis3.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);  // y축 label 위치 설정(그래프 밖)
        leftAxis3.setTextSize(10f);                                     // y축 label 크기
        leftAxis3.setTextColor(Color.BLACK);                            // y축 label 색상
        leftAxis3.setDrawGridLines(true);                               // y축 격자선 표시 여부 true
        leftAxis3.setGranularityEnabled(true);
        leftAxis3.setAxisMinimum(0f);                                   // y축 최소값 0
        leftAxis3.setAxisMaximum(170f);                                 // y축 최대값 170
        leftAxis3.setYOffset(-9f);                                      // y축 label offset
        YAxis rightAxis3 = chart3.getAxisRight();
        rightAxis3.setEnabled(false);
        setData3();*/
    }

    private void setData1(HashMap<Integer, Integer> hashMap) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        int totalCount = 0; // 상황에 맞는 총 기분 수를 0으로 초기화
        maxMoodIndex = -1;
        maxCount = -1;
        colors.clear();     // 상황에 맞는 색깔배열을 만들기 위해 초기화

        for(int i = 0; i < 9; i++) {
            int count = 0;

            if(hashMap.containsKey(i)) {
                count = hashMap.get(i);
                setMoodCount(i, count);
                totalCount += count;
                addColor(i);                // 기분 종류에 맞게 색깔 설정
                entries.add(new PieEntry(count, "", resizeDrawable(moodIconRes[i])));
            } else {
                setMoodCount(i, count);     // 개수 0가 경우
            }
        }

        moodTotalCountTextView.setText("(총 " + totalCount + "건 중)");        // 총 기분 개수
        setCrownImage();                                    // 제일 많은 개수를 가진 기분에 왕관이미지를 추가

        PieDataSet dataSet = new PieDataSet(entries, "기분별 비율");
        dataSet.setDrawIcons(true);                             // 아이콘 표시 여부
        dataSet.setSliceSpace(10f);                              // 그래프 간격
        dataSet.setIconsOffset(new MPPointF(0, 55));      // 아이콘 offset
        //dataSet.setSelectionShift(5f);                        // 특정부분 선택시 확대효과 크기
        dataSet.setColors(colors);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value) + "%";
            }
        });

        PieData data = new PieData(dataSet);
        data.setValueTextSize(17f);                         // 그래프 내 text 크기
        data.setValueTextColor(Color.WHITE);                // 그래프 내 text 색상
        if(context != null) {                               // 그래프 내 text 폰트
            data.setValueTypeface(getCurTypeFace());
        }

        chart1.setData(data);
        chart1.invalidate();
    }

/*    private void setData2() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        BitmapDrawable angryDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.mood_angry_color);
        Bitmap angryBitmap = angryDrawable.getBitmap();
        Bitmap angryNewBitmap = Bitmap.createScaledBitmap(angryBitmap, 55, 55, true);
        Drawable angryNewDrawable = new BitmapDrawable(angryNewBitmap);

        entries.add(new BarEntry(1f, 20f, angryNewDrawable));

        BarDataSet dataSet2 = new BarDataSet(entries, "요일별 기분");
        dataSet2.setIconsOffset(new MPPointF(0, -10));
        dataSet2.setColors(colors);

        BarData data = new BarData(dataSet2);
        data.setValueTextSize(12f);             // 표기 할 구성요소 별 y축 값 text 크기
        data.setDrawValues(false);              // 구성요소 별 y축 값 표기 여부 false
        data.setBarWidth(0.8f);                 // 막대의 너비

        chart2.setData(data);
        chart2.invalidate();
    }

    private void setData3() {
        ArrayList<Entry> values = new ArrayList<>();


        LineDataSet dataSet = new LineDataSet(values, "기분 변화");
        dataSet.setIconsOffset(new MPPointF(0, -17));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setColor(ColorTemplate.getHoloBlue());             // 선 색상
        dataSet.setValueTextColor(ColorTemplate.getHoloBlue());    // 선에 표기 할 text 색상
        dataSet.setLineWidth(1.5f);                                // 선 두께
        dataSet.setDrawCircles(true);                              // 선 그래프에서 원 모양 사용여부 true
        dataSet.setDrawValues(false);                              // 구성요소 별 y축 값 표기 여부 false
        dataSet.setFillAlpha(65);
        dataSet.setFillColor(ColorTemplate.getHoloBlue());
        dataSet.setHighLightColor(Color.rgb(244, 117, 117));       // 구성 요소 선택시 생기는 효과 색상
        dataSet.setDrawCircleHole(false);                          // 선 그래프에서 그릴 원 모양 안에 흰색 원 추가 여부

        // create a data object with the data sets
        LineData data = new LineData(dataSet);
        data.setValueTextColor(Color.BLUE);
        data.setValueTextSize(9f);

        // set data
        chart3.setData(data);
        chart3.invalidate();
    }*/

    private void setSelectedRadioButton() {
        switch(selectRadioIndex) {
            case 0:
                allButton.setChecked(true);
                break;
            case 1:
                yearButton.setChecked(true);
                break;
            case 2:
                monthButton.setChecked(true);
                break;
        }
    }

    private void setMoodCount(int moodIndex, int count) {
        if(maxCount < count) {
            maxCount = count;
            maxMoodIndex = moodIndex;
        } else if(maxCount == count) {      // 중복 값이 있는 max 라면 예외처리
            maxMoodIndex = -1;
        }

        switch(moodIndex) {
            case 0:
                angryCount.setText(String.valueOf(count));
                break;
            case 1:
                coolCount.setText(String.valueOf(count));
                break;
            case 2:
                cryingCount.setText(String.valueOf(count));
                break;
            case 3:
                illCount.setText(String.valueOf(count));
                break;
            case 4:
                laughCount.setText(String.valueOf(count));
                break;
            case 5:
                mehCount.setText(String.valueOf(count));
                break;
            case 6:
                sadCount.setText(String.valueOf(count));
                break;
            case 7:
                smileCount.setText(String.valueOf(count));
                break;
            case 8:
                yawnCount.setText(String.valueOf(count));
                break;
        }
    }

    private void setCrownImage() {
        crown.setVisibility(View.INVISIBLE);
        crown2.setVisibility(View.INVISIBLE);
        crown3.setVisibility(View.INVISIBLE);
        crown4.setVisibility(View.INVISIBLE);
        crown5.setVisibility(View.INVISIBLE);
        crown6.setVisibility(View.INVISIBLE);
        crown7.setVisibility(View.INVISIBLE);
        crown8.setVisibility(View.INVISIBLE);
        crown9.setVisibility(View.INVISIBLE);

        switch(maxMoodIndex) {
            case 0:
                crown.setVisibility(View.VISIBLE);
                break;
            case 1:
                crown2.setVisibility(View.VISIBLE);
                break;
            case 2:
                crown3.setVisibility(View.VISIBLE);
                break;
            case 3:
                crown4.setVisibility(View.VISIBLE);
                break;
            case 4:
                crown5.setVisibility(View.VISIBLE);
                break;
            case 5:
                crown6.setVisibility(View.VISIBLE);
                break;
            case 6:
                crown7.setVisibility(View.VISIBLE);
                break;
            case 7:
                crown8.setVisibility(View.VISIBLE);
                break;
            case 8:
                crown9.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void addColor(int moodIndex) {
        switch(moodIndex) {
            case 0:
                colors.add(getResources().getColor(R.color.pastel_red));
                break;
            case 1:
                colors.add(getResources().getColor(R.color.pastel_blue));
                break;
            case 2:
                colors.add(getResources().getColor(R.color.pastel_skyblue));
                break;
            case 3:
                colors.add(getResources().getColor(R.color.pastel_green));
                break;
            case 4:
                colors.add(getResources().getColor(R.color.pastel_yellow));
                break;
            case 5:
                colors.add(getResources().getColor(R.color.pastel_gray));
                break;
            case 6:
                colors.add(getResources().getColor(R.color.pastel_black));
                break;
            case 7:
                colors.add(getResources().getColor(R.color.pastel_orange));
                break;
            case 8:
                colors.add(getResources().getColor(R.color.pastel_pink));
                break;
        }
    }

    private Typeface getCurTypeFace() {
        Typeface typeface = null;

        switch(curFontIndex) {
            case 0:
                typeface = Typeface.createFromAsset(context.getAssets(), "font1.ttf");
                break;
            case 1:
                typeface = Typeface.createFromAsset(context.getAssets(), "font2.ttf");
                break;
            case 2:
                typeface = Typeface.createFromAsset(context.getAssets(), "font3.ttf");
                break;
            case 3:
                typeface = Typeface.createFromAsset(context.getAssets(), "font4.ttf");
                break;
            case 4:
                typeface = Typeface.createFromAsset(context.getAssets(), "font5.ttf");
                break;
            default:
                typeface = Typeface.createFromAsset(context.getAssets(), "font1.otf");
                break;
        }

        return typeface;
    }

    private Drawable resizeDrawable(int res) {
        BitmapDrawable drawable = (BitmapDrawable)getResources().getDrawable(res);
        Bitmap bitamp = drawable.getBitmap();
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitamp, 55, 55, true);
        Drawable newDrawable = new BitmapDrawable(newBitmap);

        return newDrawable;
    }
}
