package org.techtown.diary.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.techtown.diary.R;

public class MyTheme {
    // 상수
    private static final String LOG = "MyTheme";
    public static final String SHARED_PREFERENCES_NAME = "pref";
    public static final String FONT_KEY = "font_key";

    public static void applyTheme(@NonNull Context context) {
        int fontIndex = 0;      // default
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);

        if(pref != null) {
            fontIndex = pref.getInt(FONT_KEY, 0);
        }

        applyTheme(context, fontIndex);
    }

    public static void applyTheme(@NonNull Context context, int fontIndex) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(FONT_KEY, fontIndex);
        editor.commit();

        if (fontIndex == 0) {
            context.setTheme(R.style.Theme_DiaryProject);
            return;
        }

        if (fontIndex == 1) {
            context.setTheme(R.style.Theme_DiaryProject2);
            return;
        }

        if (fontIndex == 2) {
            context.setTheme(R.style.Theme_DiaryProject3);
            return;
        }

        if (fontIndex == 3) {
            context.setTheme(R.style.Theme_DiaryProject4);
            return;
        }

        if (fontIndex == 4) {
            context.setTheme(R.style.Theme_DiaryProject5);
            return;
        }
    }
}
