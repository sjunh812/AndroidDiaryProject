package org.techtown.diary.helper;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.R;
import org.techtown.diary.item.Note;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteViewHolder> implements OnNoteItemClickListener{
    private ArrayList<Note> items = new ArrayList<>();
    private Context context;
    private OnNoteItemClickListener listener;

    private int layoutType = 0;

    public NoteAdapter(Context context) {
        this.context = context;
    }

    public void addItem(Note item) {
        if(items.size() != 0) {
            items.add(items.get(items.size() - 1));

            for(int i = items.size() - 2; i >= 0; i--) {
                items.set(i + 1, items.get(i));
            }

            items.set(0, item);
        } else {
            items.add(item);
        }
    }

    public void setItems(ArrayList<Note> items) {
        this.items = items;
    }

    public void setLayoutType(int layoutType) {
        this.layoutType = layoutType;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item, parent, false);

        return new NoteViewHolder(itemView, layoutType);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note item = items.get(position);

        holder.setItem(item);
        holder.setOnItemClickListener(listener);
        holder.setLayoutType(layoutType);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnNoteItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onitemClick(NoteViewHolder holder, View view, int position) {
        if(listener != null) {
            listener.onitemClick(holder, view, position);
        }
    }
}
