package org.techtown.diary.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.fonts.Font;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.techtown.diary.FontActivity;
import org.techtown.diary.R;
import org.techtown.diary.helper.MyTheme;

public class OptionFragment extends Fragment {
    public static final int REQUEST_FONT_CHANGE = 101;

    private TextView curFontTextView;

    private int curFontIndex = 0;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_option, container, false);

        SharedPreferences pref = getContext().getSharedPreferences(MyTheme.SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        if(pref != null) {
            curFontIndex = pref.getInt(MyTheme.FONT_KEY, 0);
        }

        curFontTextView = (TextView)rootView.findViewById(R.id.curFontTextView);
        setCurFontText();

        RelativeLayout fontLayout = (RelativeLayout)rootView.findViewById(R.id.fontLayout);
        fontLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FontActivity.class);
                getActivity().startActivityForResult(intent, REQUEST_FONT_CHANGE);
            }
        });

        return rootView;
    }

    private void setCurFontText() {
        switch(curFontIndex) {
            case 0:
                curFontTextView.setText("카페24 고운밤");
                break;
            case 1:
                curFontTextView.setText("교보손글씨");
                break;
            case 2:
                curFontTextView.setText("어비 푸딩체");
                break;
            case 3:
                curFontTextView.setText("할아버지의나눔");
                break;
            case 4:
                curFontTextView.setText("카페24 숑숑체");
                break;
            default:
                curFontTextView.setText("카페24 고운밤");
                break;
        }
    }
}
