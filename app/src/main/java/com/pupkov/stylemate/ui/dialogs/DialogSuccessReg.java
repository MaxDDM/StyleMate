package com.pupkov.stylemate.ui.dialogs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.analytics.RegisterCount;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.UserRepository;
import com.pupkov.stylemate.ui.RegisterActivity;
import com.pupkov.stylemate.ui.test.TestQ1Activity;

public class DialogSuccessReg extends DialogFragment {
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
        return inflater.inflate(R.layout.dialog_success_reg, container, false);
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

        Button btnReturn = view.findViewById(R.id.btnReturnSuc);
        Button btnContinue = view.findViewById(R.id.btnContinueSuc);


        btnReturn.setOnClickListener(v -> {
            dismiss();
        });

        btnContinue.setOnClickListener(v -> {
            RegisterCount.updateRegisterCount();

            assert getArguments() != null;
            String uid = getArguments().getString("uid");
            ActiveUserInfo.setDefaults("isRegistered", uid, requireContext());

            Intent intent = new Intent(requireContext(), TestQ1Activity.class);
            startActivity(intent);
        });
    }
}
