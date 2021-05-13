package org.techtown.diary.helper;


import org.techtown.diary.note.Note;

import java.util.Date;

public interface OnRequestListener {
    public void onRequest(String command);
    public void onRequest(String command, Date date);
    public void onRequestDetailActivity(Note item);
    public void onRequestWriteFragmentFromCal(Date date);
}
