package org.techtown.diary.note;

import java.util.ArrayList;

public interface NoteDatabaseCallback {
    public void insertDB(Object[] objs);
    public ArrayList<Note> selectAllDB();
    public void deleteDB(int id);
    public void updateDB(Note item);
}
