package org.techtown.diary.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.MainActivity;
import org.techtown.diary.custom.CustomAlignDialog;
import org.techtown.diary.R;
import org.techtown.diary.custom.CustomUpdateDialog;
import org.techtown.diary.helper.OnNoteItemLongClickListener;
import org.techtown.diary.note.NoteAdapter;
import org.techtown.diary.note.NoteDatabaseCallback;
import org.techtown.diary.note.NoteViewHolder;
import org.techtown.diary.helper.OnTabItemSelectedListener;
import org.techtown.diary.note.Note;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListFragment extends Fragment {
    // 상수
    private static final String LOG = "ListFragment";

    // UI
    private TextView selectedDateTextView;
    private RadioGroup radioGroup;                     // 커스텀 라디오버튼을 담은 라디오그룹
    private AppCompatRadioButton radioButtonL;         // 커스텀 라디오버튼중 왼쪽(내용)
    private AppCompatRadioButton radioButtonR;         // 커스텀 라디오버튼중 오른쪽(사진)
    private CustomAlignDialog alignDialog;
    private CustomUpdateDialog deleteDialog;

    // Helper
    private NoteAdapter adapter;                       // 일기 목록을 담은 리사이클러 뷰의 어뎁터
    private OnTabItemSelectedListener tabListener;     // 메인 액티비티 하단 탭의 탭선택 콜백함수를 호출 해주는 리스너
    private NoteDatabaseCallback callback;
    //public static SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
    private MyArrayAdapter yearAdapter;
    private MyArrayAdapter monthAdapter;
    private GestureDetector detector;
    private GestureListener gestureListener;

    // Data
    private int curYear;                               // 현재 년도 ex)2021
    private int lastYear;
    private int layoutType = 0;                        // 0:내용 레이아웃, 1:사진 레이아웃
    private String[] years;
    private String[] months = {"1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"};
    private int selectedYear;
    private int selectedMonth;
    private int selectedYearPos;
    private int selectedMonthPos;
    private Note selectedItem;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnTabItemSelectedListener) {
            tabListener = (OnTabItemSelectedListener)context;
        }
        if(context instanceof NoteDatabaseCallback) {
            callback = (NoteDatabaseCallback)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(tabListener != null) {
            tabListener = null;
        }
        if(callback != null) {
            callback = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        curYear = getCurrentYear();
        caluclateYearArray();
        initSpinnerPosition();

        gestureListener = new GestureListener();
        detector = new GestureDetector(getContext(), gestureListener);

        selectedDateTextView = (TextView)rootView.findViewById(R.id.selectedDateTextView);

        RecyclerView listRecyclerView = (RecyclerView)rootView.findViewById(R.id.listRecyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        listRecyclerView.setLayoutManager(manager);
        listRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                //outRect.top = 30;
                if(parent.getChildAdapterPosition(view) != 0) {
                    outRect.top = 30;
                }
            }
        });
        adapter = new NoteAdapter(getContext());
        adapter.setItems(callback.selectAllDB());
        listRecyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(new OnNoteItemLongClickListener() {
            @Override
            public void onLongClick(NoteViewHolder holder, View view, int position) {
                selectedItem = adapter.getItem(position);
                Log.d(LOG, "길게눌림");
                if(selectedItem != null) {
                    setDeleteDialog();
                    Log.d(LOG, "길게눌림");
                }
            }
        });

        ImageButton alignButton = (ImageButton)rootView.findViewById(R.id.alignButton);
        alignButton.setOnTouchListener(mOnTouchListener);
        alignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlignDialog();
            }
        });

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
                radioButtonR.setTextColor(getResources().getColor(R.color.pastel_500));
            }
        });
        radioButtonR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonL.setTextColor(getResources().getColor(R.color.pastel_500));
                radioButtonR.setTextColor(Color.WHITE);
            }
        });

        ImageButton writeButton = (ImageButton)rootView.findViewById(R.id.writeButton);           // 작성 프래그먼트로 이동(메인액티비티를 거침)
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tabListener != null) {
                    tabListener.onTabSelected(2);
                }
            }
        });

        yearAdapter = new MyArrayAdapter(getContext(), android.R.layout.simple_spinner_item, years);
        monthAdapter = new MyArrayAdapter(getContext(), android.R.layout.simple_spinner_item, months);

        return rootView;
    }

    private void initSpinnerPosition() {
        selectedYearPos = years.length - 1;
        selectedMonthPos = getCurrentMonth() - 1;

        Log.d(LOG, "selectedyYearPos : " + selectedYearPos + ", selectedMonthPos : " + selectedMonthPos);
    }

    public void setDeleteDialog() {
        deleteDialog = new CustomUpdateDialog(getContext());
        deleteDialog.show();
        deleteDialog.setCancelable(true);

        deleteDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
            }
        });

        deleteDialog.setDeleteButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedItem != null) {
                    int id = selectedItem.get_id();
                    String path = selectedItem.getPicture();

                    if(path != null && !path.equals("")) {
                        File file = new File(path);
                        file.delete();
                    }
                    // 해당 db 삭제
                    callback.deleteDB(id);
                    adapter.setItems(callback.selectAllDB());
                    adapter.notifyDataSetChanged();

                    deleteDialog.dismiss();
                }
            }
        });

        deleteDialog.setUpdateButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDialog.dismiss();
                tabListener.showWriteFragment(selectedItem);
            }
        });
    }

    public void setAlignDialog() {
        alignDialog = new CustomAlignDialog(getContext());
        alignDialog.show();
        alignDialog.setCancelable(true);                     // 다이얼로그외 공간 클릭시 취소 가능 여부

        alignDialog.setCancelButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alignDialog.dismiss();
            }
        });

        alignDialog.setYesButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateTextView.setText(selectedYear + "년 " + selectedMonth + "월");

                ArrayList<Note> items = callback.selectPart(selectedYear, selectedMonth);
                adapter.setItems(items);
                adapter.notifyDataSetChanged();

                alignDialog.dismiss();
            }
        });

        alignDialog.setAllButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateTextView.setText("전체보기");

                ArrayList<Note> items = callback.selectAllDB();
                adapter.setItems(items);
                adapter.notifyDataSetChanged();

                alignDialog.dismiss();
            }
        });

        alignDialog.setYearSpinnerAdapter(yearAdapter);
        alignDialog.setSelectedYearSpinner(selectedYearPos);
        alignDialog.setYearSpinnerItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int size = years.length;
                int gap = size - (position + 1);

                selectedYearPos = position;
                selectedYear = curYear - gap;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        alignDialog.setMonthSpinnerAdapter(monthAdapter);
        alignDialog.setSelectMonthSpinner(selectedMonthPos);
        alignDialog.setMonthSpinnerItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonthPos = position;
                selectedMonth = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public static int getCurrentYear() {
        Date date = new Date();
        String yearStr = MainActivity.yearFormat.format(date);
        int year = Integer.parseInt(yearStr);

        return year;
    }

    public static int getCurrentMonth() {
        Date date = new Date();
        String monthStr = MainActivity.monthFormat.format(date);
        int month = Integer.parseInt(monthStr);

        return month;
    }

    public void caluclateYearArray() {
        lastYear = callback.selectLastYear();
        if(lastYear == 0) {
            lastYear = curYear;
        }
        int yearDiff = curYear - lastYear;

        ArrayList<String> yearsArray = new ArrayList<>();
        for(int i = yearDiff; i > 0; i--) {
            yearsArray.add(curYear - i + "년");
        }
        yearsArray.add(curYear + "년");

        years = yearsArray.toArray(new String[yearDiff + 1]);
    }

    public void update() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(radioButtonL.isChecked()) {
            radioButtonL.setTextColor(Color.WHITE);
            radioButtonR.setTextColor(getResources().getColor(R.color.pastel_500));
        } else {
            radioButtonL.setTextColor(getResources().getColor(R.color.pastel_500));
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
            //Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ridibatang.otf");
            //((TextView)view).setTypeface(font);
            ((TextView)view).setGravity(Gravity.CENTER);
            ((TextView)view).setTextSize(17);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ((TextView)view).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }

            return view;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            //Typeface font = Typeface.createFromAsset(getContext().getAssets(), "ridibatang.otf");
            //((TextView)view).setTypeface(font);
            ((TextView)view).setGravity(Gravity.CENTER_VERTICAL);
            ((TextView)view).setHeight(88);
            ((TextView)view).setTextSize(17);

            return view;
        }
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageButton button = (ImageButton) v;
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

    private class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // 길게 터치한 경우
            if(selectedItem != null) {
                setDeleteDialog();
                Log.d(LOG, "길게눌림");
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
}
