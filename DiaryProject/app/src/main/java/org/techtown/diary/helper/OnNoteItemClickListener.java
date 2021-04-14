package org.techtown.diary.helper;

import android.view.View;

import org.techtown.diary.note.NoteViewHolder;

public interface OnNoteItemClickListener {
    public void onitemClick(NoteViewHolder holder, View view, int position);
}
