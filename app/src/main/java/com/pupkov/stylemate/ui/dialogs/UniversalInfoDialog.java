package com.pupkov.stylemate.ui.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.WindowManager;

import com.pupkov.stylemate.R;

public class UniversalInfoDialog extends DialogFragment {

    private static final String ARG_TEXT = "arg_text";
    private static final String ARG_SHOW_ARROW = "arg_show_arrow";

    public static UniversalInfoDialog newInstance(String text, boolean showArrow) {
        UniversalInfoDialog fragment = new UniversalInfoDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putBoolean(ARG_SHOW_ARROW, showArrow);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_universal_tutorial, container, false);

        TextView tvMessage = view.findViewById(R.id.tvMessage);
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        ImageView ivArrow = view.findViewById(R.id.ivArrow);

        if (getArguments() != null) {
            String text = getArguments().getString(ARG_TEXT, "");
            boolean showArrow = getArguments().getBoolean(ARG_SHOW_ARROW, false);

            tvMessage.setText(text);

            if (showArrow) {
                ivArrow.setVisibility(View.VISIBLE);
            } else {
                ivArrow.setVisibility(View.GONE);
            }
        }

        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66FFFFFF")));
            }
        }
    }
}