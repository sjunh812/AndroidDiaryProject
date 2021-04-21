package org.techtown.diary.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import org.techtown.diary.R;

public class CustomDeleteDialog extends Dialog {
    ImageButton cancelButton;
    Button deleteButton;
    Button cancelButton2;

    public CustomDeleteDialog(@NonNull Context context) {
        super(context);
    }

    public CustomDeleteDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_dialog_custom);

        cancelButton = (ImageButton)findViewById(R.id.cancelButton);
        deleteButton = (Button)findViewById(R.id.deleteButton);
        cancelButton2 = (Button)findViewById(R.id.cancelButton2);
    }

    public void setCancelButtonOnClickListener(View.OnClickListener listener) {
        cancelButton.setOnClickListener(listener);
    }

    public void setDeleteButtonOnClickListener(View.OnClickListener listener) {
        deleteButton.setOnClickListener(listener);
    }

    public void setCancelButton2OnClickListener(View.OnClickListener listener) {
        cancelButton2.setOnClickListener(listener);
    }
}
