package org.techtown.diary.note;

import java.util.ArrayList;
import java.util.HashMap;

public interface NoteDatabaseCallback {
    public void insertDB(Object[] objs);
    public ArrayList<Note> selectAllDB();
    public void deleteDB(int id);
    public void updateDB(Note item);
    public ArrayList<Note> selectPart(int year, int month);
    public HashMap<Integer, Integer> selectMoodCount(boolean isAll, boolean isYear, boolean isMonth);
    public HashMap<Integer, Integer> selectMoodCountWeek(int weekOfDay);
    public int selectLastYear();
}
