package org.techtown.diary.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.MPPointF;

import org.techtown.diary.R;

import java.util.ArrayList;

public class GraphFragment extends Fragment {
    private PieChart chart1;
    private BarChart chart2;
    private LineChart chart3;

    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);

        // 원형 그래프(기분별)
        chart1 = (PieChart)rootView.findViewById(R.id.chart1);
        chart1.setUsePercentValues(true);
        chart1.getDescription().setEnabled(false);
        //chart1.setCenterText("기분별 비율")
        chart1.setTransparentCircleColor(Color.WHITE);
        chart1.setTransparentCircleAlpha(110);
        chart1.setTransparentCircleRadius(65f);
        chart1.setHoleRadius(60f);
        //chart1.setDrawCenterText(true);
        chart1.setHighlightPerTapEnabled(true);
        Legend legend1 = chart1.getLegend();
        legend1.setEnabled(false);
        chart1.setEntryLabelColor(Color.WHITE);
        chart1.setEntryLabelTextSize(17f);
        setData1();

        chart2 = (BarChart)rootView.findViewById(R.id.chart2);
        chart3 = (LineChart)rootView.findViewById(R.id.chart3);

        return rootView;
    }

    private void setData1() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_angry_color_small)));
        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_cool_color_small)));
        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_crying_color_small)));
        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_ill_color_small)));
        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_laugh_color_small)));
        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_meh_color_small)));
        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_sad_small)));
        entries.add(new PieEntry(10f, "", getResources().getDrawable(R.drawable.mood_smile_color_small)));
        entries.add(new PieEntry(20f, "", getResources().getDrawable(R.drawable.mood_yawn_color_small)));

        PieDataSet dataSet = new PieDataSet(entries, "기분별 비율");
        dataSet.setDrawIcons(true);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, -38));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = getColors();
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(15f);
        data.setValueTextColor(Color.WHITE);
        if(context != null) {
            data.setValueTypeface(Typeface.createFromAsset(context.getAssets(), "ridibatang.otf"));
        }

        chart1.setData(data);
        chart1.invalidate();
    }

    private ArrayList<Integer> getColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.red));
        colors.add(getResources().getColor(R.color.blue));
        colors.add(getResources().getColor(R.color.skyblue));
        colors.add(getResources().getColor(R.color.lightgreen));
        colors.add(getResources().getColor(R.color.yellow));
        colors.add(getResources().getColor(R.color.gray));
        colors.add(getResources().getColor(R.color.black));
        colors.add(getResources().getColor(R.color.orange));
        colors.add(getResources().getColor(R.color.pink));

        return colors;
    }
}
