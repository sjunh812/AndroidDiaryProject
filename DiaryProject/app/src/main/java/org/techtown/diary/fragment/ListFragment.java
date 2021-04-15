package org.techtown.diary.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.R;
import org.techtown.diary.note.NoteAdapter;
import org.techtown.diary.note.NoteViewHolder;
import org.techtown.diary.helper.OnNoteItemClickListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;
import org.techtown.diary.note.Note;

public class ListFragment extends Fragment {
    // UI
    private RadioGroup radioGroup;                     // 커스텀 라디오버튼을 담은 라디오그룹
    private AppCompatRadioButton radioButtonL;         // 커스텀 라디오버튼중 왼쪽(내용)
    private AppCompatRadioButton radioButtonR;         // 커스텀 라디오버튼중 오른쪽(사진)

    // Helper
    private NoteAdapter adapter;                       // 일기 목록을 담은 리사이클러 뷰의 어뎁터
    private OnTabItemSelectedListener tabListener;     // 메인 액티비티 하단 탭의 탭선택 콜백함수를 호출 해주는 리스너

    private int layoutType = 0;                        // 0:내용레이아웃, 1:사진레이아웃

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
        adapter.addItem(new Note(0, "0", "고양시 일산서구", "0", "0", "일기장 테스트 중입니다. \n테스트테스트~", "0", null, "2021-04-10"));
        adapter.addItem(new Note(0, "5", "고양시 일산서구", "0", "0", "웃음", "4", null, "2021-04-11"));
        adapter.addItem(new Note(0, "6", "고양시 일산서구", "0", "0", "so so", "5", null, "2021-04-11"));
        adapter.addItem(new Note(0, "0", "고양시 일산서구", "0", "0", "슬픔", "6", null, "2021-04-11"));
        adapter.addItem(new Note(0, "0", "고양시 일산서구", "0", "0", "취직하고싶다..\n좋은 곳으로 가고싶다아아\n네카쿠라배..", "8", null, "2021-04-11"));
        listRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onitemClick(NoteViewHolder holder, View view, int position) {
                Toast.makeText(getContext(), "아이템 " + position + " 이 선택됨.", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
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
}
