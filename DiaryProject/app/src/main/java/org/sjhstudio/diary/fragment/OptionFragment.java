package org.sjhstudio.diary.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.sjhstudio.diary.AlarmActivity;
import org.sjhstudio.diary.BackupActivity;
import org.sjhstudio.diary.DarkModeActivity;
import org.sjhstudio.diary.FontActivity;
import org.sjhstudio.diary.R;
import org.sjhstudio.diary.helper.AppHelper;
import org.sjhstudio.diary.helper.MyTheme;
import org.sjhstudio.diary.note.NoteDatabaseCallback;

public class OptionFragment extends Fragment {
    public static final int REQUEST_FONT_CHANGE = 101;
    public static final int REQUEST_ALARM_SETTING = 102;
    public static final int REQUEST_DARK_MODE = 103;

    private TextView curFontTextView;
    private TextView allCountTextView;
    private TextView starCountTextView;

    private NoteDatabaseCallback callback;
    private int curFontIndex = 0;
    private int allCount = 0;
    private int starCount = 0;
    private Animation translateRightAnim;

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
        View rootView = inflater.inflate(R.layout.fragment_option, container, false);

        SharedPreferences pref = getContext().getSharedPreferences(MyTheme.SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        if(pref != null) {
            curFontIndex = pref.getInt(MyTheme.FONT_KEY, 0);
        }

        translateRightAnim = AnimationUtils.loadAnimation(getContext(), R.anim.translate_right_animation);
        translateRightAnim.setDuration(350);

        TextView titleTextView = (TextView)rootView.findViewById(R.id.titleTextView);
        titleTextView.startAnimation(translateRightAnim);
        curFontTextView = (TextView)rootView.findViewById(R.id.curFontTextView);
        setCurFontText();

        setCountTextView(rootView);

        /* 폰트 설정 */
        RelativeLayout fontLayout = (RelativeLayout)rootView.findViewById(R.id.fontLayout);
        fontLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FontActivity.class);
                getActivity().startActivityForResult(intent, REQUEST_FONT_CHANGE);
            }
        });

        /* 알림 설정 */
        RelativeLayout noticeLayout = (RelativeLayout)rootView.findViewById(R.id.noticeLayout);
        noticeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AlarmActivity.class);
                getActivity().startActivityForResult(intent, REQUEST_ALARM_SETTING);
            }
        });

        /* 다크모드 설정 */
        RelativeLayout darkmodLayout = (RelativeLayout)rootView.findViewById(R.id.darkmodeLayout);
        darkmodLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DarkModeActivity.class);
                getActivity().startActivityForResult(intent, REQUEST_DARK_MODE);
            }
        });


        /* 백업 및 복원하기 */
        RelativeLayout backupLayout = (RelativeLayout)rootView.findViewById(R.id.backupLayout);
        backupLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), BackupActivity.class);
                startActivity(intent);
            }
        });

        /* 스토어 리뷰 */
        RelativeLayout reviewLayout = (RelativeLayout)rootView.findViewById(R.id.reviewLayout);
        reviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getContext().getPackageName()));
                startActivity(intent);
            }
        });

        /* 피드백 보내기 */
        RelativeLayout ideaLayout = (RelativeLayout)rootView.findViewById(R.id.ideaLayout);
        ideaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppHelper helper = new AppHelper(getContext());
                String[] email = {"sjunh812@naver.com"};
                String appVersion = helper.getVersionName();
                String osVersion = helper.getOsName();
                String modelName = helper.getModelName();
                String contents = "안녕하세요!\n" +
                        "소중한 의견을 보내주셔서 감사합니다 :)\n" +
                        "신중하게 검토 후 답변드리겠습니다.\n" +
                        "----------------------------\n" +
                        "앱 버전 : " + appVersion +
                        "\n기기명 : " + modelName +
                        "\n안드로이드 OS : " + osVersion +
                        "\n----------------------------\n";

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/Text");
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                intent.putExtra(Intent.EXTRA_SUBJECT, "[" + getString(R.string.app_name) + "] " + getString(R.string.report));
                intent.putExtra(Intent.EXTRA_TEXT, contents);
                intent.setType("message/rfc822");
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void setCountTextView(View rootView) {
        allCountTextView = (TextView)rootView.findViewById(R.id.allCountTextView);
        starCountTextView = (TextView)rootView.findViewById(R.id.starCountTextView);
        allCount = callback.selectAllCount();
        starCount = callback.selectStarCount();

        allCountTextView.setText(allCount + "개");
        starCountTextView.setText(starCount + "개");
    }

    private void setCurFontText() {
        switch(curFontIndex) {
            case 100:
                curFontTextView.setText("시스템 서체");
                break;
            case -1:
                curFontTextView.setText("THE얌전해진언니체");
                break;
            case 0:
                curFontTextView.setText("교보 손글씨체");
                break;
            case 1:
                curFontTextView.setText("점꼴체");
                break;
            case 2:
                curFontTextView.setText("넥슨 배찌체");
                break;
            case 3:
                curFontTextView.setText("미니콩다방체");
                break;
            case 4:
                curFontTextView.setText("꼬마나비체");
                break;
            default:
                curFontTextView.setText("THE얌전해진언니체");
                break;
        }
    }
}
