package com.example.stylemate.ui.dialogs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.stylemate.R;
import com.example.stylemate.ui.ActiveUserInfo;
import com.example.stylemate.ui.test.TestQ1Activity;

public class SkipRegDialog extends DialogFragment {

    private SkipRegDialog.OnDeleteListener listener;

    public interface OnDeleteListener {
        void onConfirmDelete();
    }

    public void setListener(SkipRegDialog.OnDeleteListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_skip_reg, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnSkip = view.findViewById(R.id.btnSkipReg);
        Button btnReturn = view.findViewById(R.id.btnReturnToReg);


        btnSkip.setOnClickListener(v -> {
            ActiveUserInfo.setDefaults("isRegistered", "No", requireContext());

            Intent intent = new Intent(requireContext(), TestQ1Activity.class);
            startActivity(intent);
        });

        btnReturn.setOnClickListener(v -> dismiss());
    }
}
