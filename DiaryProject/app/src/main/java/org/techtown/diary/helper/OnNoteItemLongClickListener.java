package org.techtown.diary.helper;

import android.view.View;

import org.techtown.diary.note.NoteViewHolder;

public interface OnNoteItemLongClickListener {
    public void onLongClick(NoteViewHolder holder, View view, int position);
}
