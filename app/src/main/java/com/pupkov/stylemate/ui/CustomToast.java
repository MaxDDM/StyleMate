package com.pupkov.stylemate.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.pupkov.stylemate.R;

public class CustomToast {

    public static void show(Context context, String message) {
        if (context == null) return;

        TextView textView = new TextView(context);
        textView.setText(message);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(15);
        textView.setTypeface(null);
        textView.setPadding(40, 24, 40, 24);

        textView.setBackgroundResource(R.drawable.bg_toast_white);

        textView.setElevation(10f);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(textView);

        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 160);
        toast.show();
    }
}