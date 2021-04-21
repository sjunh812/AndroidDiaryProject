package org.techtown.diary.helper;

import org.techtown.diary.note.Note;

public interface OnTabItemSelectedListener {
    public void onTabSelected(int position);
    public void showWriteFragment(Note item);
}
