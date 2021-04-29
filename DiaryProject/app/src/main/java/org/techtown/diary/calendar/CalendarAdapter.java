package org.techtown.diary.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.R;
import org.techtown.diary.note.Note;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private Context context;

    private ArrayList<Note> items = new ArrayList<>();

    public CalendarAdapter(Context context) {
        this.context = context;
    }

    public void setItems(ArrayList<Note> items) {
        this.items = items;
    }

    public void addItem(Note item) {
        items.add(item);
    }

    public void clearItems() {
        items.clear();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.calendar_item, parent, false);

        return new CalendarViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        Note item = items.get(position);

        holder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
