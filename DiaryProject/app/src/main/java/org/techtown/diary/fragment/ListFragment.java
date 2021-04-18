package org.techtown.diary.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.CustomAlignDialog;
import org.techtown.diary.CustomDialog;
import org.techtown.diary.R;
import org.techtown.diary.note.NoteAdapter;
import org.techtown.diary.note.NoteDatebase;
import org.techtown.diary.note.NoteViewHolder;
import org.techtown.diary.helper.OnNoteItemClickListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;
import org.techtown.diary.note.Note;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ListFragment extends Fragment {
    // 상수
    private static final String LOG = "ListFragment";

    // UI
    private TextView selectedDateTextView;
    private RadioGroup radioGroup;                     // 커스텀 라디오버튼을 담은 라디오그룹
    private AppCompatRadioButton radioButtonL;         // 커스텀 라디오버튼중 왼쪽(내용)
    private AppCompatRadioButton radioButtonR;         // 커스텀 라디오버튼중 오른쪽(사진)
    private CustomAlignDialog dialog;

    // Helper
    private NoteAdapter adapter;                       // 일기 목록을 담은 리사이클러 뷰의 어뎁터
    private OnTabItemSelectedListener tabListener;     // 메인 액티비티 하단 탭의 탭선택 콜백함수를 호출 해주는 리스너
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    // Data
    private int year;
    private int layoutType = 0;                        // 0:내용 레이아웃, 1:사진 레이아웃
    private String[] years = {"2020년", "2021년"};
    private String[] months = {"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};
    private int selectedYear;
    private int selectedMonth;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnTabItemSelectedListener) {
            tabListener = (OnTabItemSelectedListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(tabListener != null) {
            tabListener = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        year = getCurrentYear();

        selectedDateTextView = (TextView)rootView.findViewById(R.id.selectedDateTextView);

        Button alignButton = (Button)rootView.findViewById(R.id.alignButton);
        alignButton.setOnTouchListener(mOnTouchListener);
        alignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDialog();
            }
        });
/*        Spinner yearSpinner = (Spinner)rootView.findViewById(R.id.yearSpinner);
        MyArrayAdapter arrayAdapter = new MyArrayAdapter(getContext(), android.R.layout.simple_spinner_item, years);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(arrayAdapter);

        Spinner monthSpinner = (Spinner)rootView.findViewById(R.id.monthSpinner);
        MyArrayAdapter arrayAdapter2 = new MyArrayAdapter(getContext(), android.R.layout.simple_spinner_item, months
        );
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(arrayAdapter2);*/

        radioGroup = (RadioGroup)rootView.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioButtonL) {
                    adapter.setLayoutType(0);
                    adapter.notifyDataSetChanged();
                } else if(checkedId == R.id.radioButtonR) {
                    adapter.setLayoutType(1);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        radioButtonL = (AppCompatRadioButton)rootView.findViewById(R.id.radioButtonL);
        radioButtonR = (AppCompatRadioButton)rootView.findViewById(R.id.radioButtonR);

        radioButtonL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonL.setTextColor(Color.WHITE);
                radioButtonR.setTextColor(getResources().getColor(R.color.skyblue));
            }
        });
        radioButtonR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonL.setTextColor(getResources().getColor(R.color.skyblue));
                radioButtonR.setTextColor(Color.WHITE);
            }
        });

        Button writeButton = (Button)rootView.findViewById(R.id.writeButton);           // 작성 프래그먼트로 이동(메인액티비티를 거침)
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    tabListener.onTabSelected(1);
                }
            }
        });

        RecyclerView listRecyclerView = (RecyclerView)rootView.findViewById(R.id.listRecyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        listRecyclerView.setLayoutManager(manager);
        adapter = new NoteAdapter(getContext());
        adapter.addItem(new Note(0, 0, "고양시 일산서구", "0", "0", "일기장 테스트 중입니다. \n테스트테스트~", 0, null, "2021-04-10"));
        adapter.addItem(new Note(0, 5, "고양시 일산서구", "0", "0", "웃음", 4, null, "2021-04-11"));
        adapter.addItem(new Note(0, 6, "고양시 일산서구", "0", "0", "so so", 5, null, "2021-04-11"));
        adapter.addItem(new Note(0, 0, "고양시 일산서구", "0", "0", "슬픔", 6, null, "2021-04-11"));
        adapter.addItem(new Note(0, 0, "고양시 일산서구", "0", "0", "취직하고싶다..\n좋은 곳으로 가고싶다아아\n네카쿠라배..", 8, null, "2021-04-11"));
        listRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onitemClick(NoteViewHolder holder, View view, int position) {
                Toast.makeText(getContext(), "아이템 " + position + " 이 선택됨.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    public void setDialog() {
        dialog = new CustomAlignDialog(getContext());
        dialog.show();
        dialog.setCancelable(true);                     // 다이얼로그외 공간 클릭시 취소 가능 여부
        MyArrayAdapter yearAdapter = new MyArrayAdapter(getContext(), android.R.layout.simple_spinner_item, years);
        MyArrayAdapter monthAdapter = new MyArrayAdapter(getContext(), android.R.layout.simple_spinner_item, months);

        dialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setYesButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateTextView.setText(selectedYear + "년 " + selectedMonth + "월");
                dialog.dismiss();
            }
        });
        dialog.setAllButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateTextView.setText("전체보기");
                dialog.dismiss();
            }
        });
        dialog.setYearSpinnerAdapter(yearAdapter);
        dialog.setYearSpinnerItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int size = years.length;
                int gap = size - (position + 1);

                selectedYear = year - gap;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dialog.setMonthSpinnerAdapter(monthAdapter);
        dialog.setMonthSpinnerItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public int getCurrentYear() {
        Date date = new Date();
        String yearStr = yearFormat.format(date);
        int year = Integer.parseInt(yearStr);

        return year;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(radioButtonL.isChecked()) {
            radioButtonL.setTextColor(Color.WHITE);
            radioButtonR.setTextColor(getResources().getColor(R.color.skyblue));
        } else {
            radioButtonL.setTextColor(getResources().getColor(R.color.skyblue));
            radioButtonR.setTextColor(Color.WHITE);
        }
    }

    class MyArrayAdapter extends ArrayAdapter<String> {
        public MyArrayAdapter(@NonNull Context context, int resource, @NonNull Object[] objects) {
            super(context, resource, (String[]) objects);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ridibatang.otf");
            ((TextView)view).setTypeface(font);
            //((TextView)view).setTextSize(12);
            ((TextView)view).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            return view;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ridibatang.otf");
            ((TextView)view).setTypeface(font);
            ((TextView)view).setHeight(80);
            //((TextView)view).setTextSize(12);

            return view;
        }
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Button button = (Button) v;
            int action = event.getAction();

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        button.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
                        break;
                    case MotionEvent.ACTION_UP:
                        button.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                        break;
                }
            }

            return false;
        }
    };
}
