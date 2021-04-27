package org.techtown.diary.fragment;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.techtown.diary.MainActivity;
import org.techtown.diary.R;
import org.techtown.diary.note.Note;
import org.techtown.diary.note.NoteDatabaseCallback;
import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CalendarFragment extends Fragment implements OnDateSelectedListener{
    private MaterialCalendarView calendarView;
    private TextView dateTextView;
    private RecyclerView recyclerView;

    private NoteDatabaseCallback callback;
    private ArrayList<Note> items;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

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
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        items = callback.selectAllDB();

        dateTextView = (TextView)rootView.findViewById(R.id.dateTextView);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        initCalendarView(rootView);

        return rootView;
    }

    private void initCalendarView(View rootView) {
        calendarView = (MaterialCalendarView)rootView.findViewById(R.id.calendar);
        calendarView.setOnDateChangedListener(this);
        calendarView.setSelectedDate(CalendarDay.today());
        calendarView.addDecorators(new SaturdayDecorator(), new SundayDecorator(), new TodayDecorator());

        for(Note note : items) {
            try {
                LocalDate localDate = LocalDate.parse(note.getCreateDateStr2());
                int moodIndex = note.getMood();

                calendarView.addDecorator(new MyDayDecorator(getContext(), CalendarDay.from(localDate), moodIndex));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        // 선택한 날짜에 맞게 dateTextView 설정
        int year = date.getYear();
        int month = date.getMonth();
        int day = date.getDay();

        dateTextView.setText(year + "년 " + month + "월 " + day + "일");
    }

    class MyDayDecorator implements DayViewDecorator {
        private Context context;
        private CalendarDay day;
        private int moodIndex;

        public MyDayDecorator(Context context, CalendarDay day, int moodIndex) {
            this.context = context;
            this.day = day;
            this.moodIndex = moodIndex;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(this.day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new RelativeSizeSpan(0.7f));
            switch(moodIndex) {
                case 0:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_angry_color));
                    break;
                case 1:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_cool_color));
                    break;
                case 2:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_crying_color));
                    break;
                case 3:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_ill_color));
                    break;
                case 4:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_laugh_color));
                    break;
                case 5:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_meh_color));
                    break;
                case 6:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_sad));
                    break;
                case 7:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_smile_color));
                    break;
                case 8:
                    view.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mood_yawn_color));
                    break;
            }
        }
    }

    class SaturdayDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            LocalDate date = day.getDate();
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            return dayOfWeek.getValue() == 6;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.skyblue)));
        }
    }

    class SundayDecorator implements DayViewDecorator {
        @Override
        public boolean shouldDecorate(CalendarDay day) {
            LocalDate date = day.getDate();
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            return dayOfWeek.getValue() == 7;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.RED));
        }
    }

    class TodayDecorator implements DayViewDecorator {

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(CalendarDay.today());
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new StyleSpan(Typeface.BOLD));
            view.addSpan(new RelativeSizeSpan(1.4f));
        }
    }
}
