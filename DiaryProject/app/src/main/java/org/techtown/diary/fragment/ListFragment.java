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
import org.techtown.diary.helper.NoteAdapter;
import org.techtown.diary.helper.NoteViewHolder;
import org.techtown.diary.helper.OnNoteItemClickListener;
import org.techtown.diary.helper.OnTabItemSelectedListener;
import org.techtown.diary.item.Note;

import lib.kingja.switchbutton.SwitchMultiButton;

public class ListFragment extends Fragment {
    private RadioGroup radioGroup;
    private AppCompatRadioButton radioButtonL;
    private AppCompatRadioButton radioButtonR;

    private NoteAdapter adapter;
    private OnTabItemSelectedListener listener;     // 메인액티비티 하단 탭의 탭선택 콜백함수를 호출 해주는 리스너

    private int layoutType = 0;     // 0:내용레이아웃, 1:사진레이아웃

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnTabItemSelectedListener) {
            listener = (OnTabItemSelectedListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(listener != null) {
            listener = null;
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
                if(listener != null) {
                    listener.onTabSelected(1);
                }
            }
        });

        RecyclerView listRecyclerView = (RecyclerView)rootView.findViewById(R.id.listRecyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        listRecyclerView.setLayoutManager(manager);
        adapter = new NoteAdapter(getContext());
        adapter.addItem(new Note(0, "0", "고양시 일산서구", "0", "0", "화남", "0", null, "2021-04-10"));
        adapter.addItem(new Note(0, "1", "고양시 일산서구", "0", "0", "쿨", "1", null, "2021-04-11"));
        adapter.addItem(new Note(0, "3", "고양시 일산서구", "0", "0", "울음", "2", null, "2021-04-11"));
        adapter.addItem(new Note(0, "4", "고양시 일산서구", "0", "0", "아픔", "3", null, "2021-04-11"));
        adapter.addItem(new Note(0, "5", "고양시 일산서구", "0", "0", "웃음", "4", null, "2021-04-11"));
        adapter.addItem(new Note(0, "6", "고양시 일산서구", "0", "0", "so so", "5", null, "2021-04-11"));
        adapter.addItem(new Note(0, "0", "고양시 일산서구", "0", "0", "슬픔", "6", null, "2021-04-11"));
        adapter.addItem(new Note(0, "0", "고양시 일산서구", "0", "0", "미소", "7", null, "2021-04-11"));
        adapter.addItem(new Note(0, "0", "고양시 일산서구", "0", "0", "졸림", "8", null, "2021-04-11"));
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
