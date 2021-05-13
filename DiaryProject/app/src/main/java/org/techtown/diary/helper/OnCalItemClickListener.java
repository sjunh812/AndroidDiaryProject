package org.techtown.diary.helper;

import android.view.View;

import org.techtown.diary.calendar.CalendarViewHolder;
import org.techtown.diary.note.NoteViewHolder;

public interface OnCalItemClickListener {
    public void onItemClick(CalendarViewHolder holder, View view, int position);
}
