package com.example.chessandroid.classes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.chessandroid.R;

public class Dialog extends DialogFragment {
    TextView textView;
    ImageView imageView;
    String text;
    int imageSrc;

    public Dialog(int imageSrc, String text) {
        this.imageSrc = imageSrc;
        this.text = text;
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog, null);
        textView.findViewById(R.id.dialog_text);
        imageView.findViewById(R.id.dialog_pic);

        textView.setText(text);
        imageView.setImageResource(imageSrc);

        return new AlertDialog.Builder(getActivity()).setView(view).create();
    }
}
