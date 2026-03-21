package com.example.stylemate.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.R;
import com.example.stylemate.model.TestViewModel;
import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.repository.SituationsRepository;
import com.example.stylemate.ui.new_select_test.NewSelectQ10Activity;
import com.example.stylemate.ui.new_select_test.NewSelectQ11Activity;
import com.example.stylemate.ui.test.TestQ5Activity;

public class SetSelectionNameActivity extends AppCompatActivity {

    private TestViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_selection_name);

        EditText selectionName = findViewById(R.id.selectionName);
        ImageButton continueButton = findViewById(R.id.continueSelectionName);

        continueButton.setOnClickListener(v -> {
            if (selectionName.getText().toString().isEmpty()) {
                Toast.makeText(SetSelectionNameActivity.this,"Вы не ввели название", Toast.LENGTH_LONG).show();
            } else {
                int testNumber = getIntent().getIntExtra("testNumber", 1);

                viewModel = new ViewModelProvider(this).get(TestViewModel.class);

                if (testNumber == 1) {
                    int ans = getIntent().getIntExtra("ans", 0);

                    viewModel.getSessionValidState().observe(this, isValid -> {
                        if (!isValid) {
                            Toast.makeText(this, "Время сессии истекло. Пожалуйста, зарегистрируйтесь.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SetSelectionNameActivity.this, AuthActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                    viewModel.getWinnerStyle().observe(this, winnerStyleIndex -> {
                        // Этот код сработает, когда ViewModel закончит считать

                        Toast.makeText(this, "Тест завершен! Победил стиль №: " + winnerStyleIndex, Toast.LENGTH_LONG).show();

                        // Переход на главную
                        Intent intent = new Intent(SetSelectionNameActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });

                    viewModel.checkSession();

                    int skip = getIntent().getIntExtra("notSkip", 0);
                    if (skip == 1) {
                        viewModel.processAnswer(5, ans);
                    }

                    viewModel.calculateResult(selectionName.getText().toString());
                } else {
                    int situation_id = getIntent().getIntExtra("situation_id", 0);

                    String name = selectionName.getText().toString();
                    String isReg = ActiveUserInfo.getDefaults("isRegistered", SetSelectionNameActivity.this);
                    if (isReg != null && !isReg.isEmpty()) {
                        viewModel.createSituationCollection(name, SituationsRepository.getSituations(situation_id));
                    } else {
                        viewModel.saveSituation(SituationsRepository.getSituations(situation_id));
                    }

                    Intent intent = new Intent(SetSelectionNameActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }
}