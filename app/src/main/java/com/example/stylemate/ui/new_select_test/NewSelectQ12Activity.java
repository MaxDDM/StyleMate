package com.example.stylemate.ui.new_select_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.R;
import com.example.stylemate.model.TestViewModel;
import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.repository.SituationsRepository;
import com.example.stylemate.ui.MainActivity;

public class NewSelectQ12Activity extends AppCompatActivity {
    int ans = -1;

    TestViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select12);

        ImageButton test1Button = findViewById(R.id.btnSelTest112);
        ImageButton test2Button = findViewById(R.id.btnSelTest212);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest12);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest12);


        test1Button.setOnClickListener(v -> {
            ans = 1;

            test1Button.setBackgroundResource(R.drawable.ic_pic6);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic6);
        });

        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                int situation_id = ans + 7;

                viewModel = new ViewModelProvider(this).get(TestViewModel.class);
                // String name = ActiveUserInfo.getDefaults("collectionName", NewSelectQ12Activity.this);
                String fixedName = "Подборка";
                String isReg = ActiveUserInfo.getDefaults("isRegistered", NewSelectQ12Activity.this);
                if (isReg != null && !isReg.isEmpty()) {
                    // ЮЗЕР: Вызываем НОВЫЙ правильный метод
                    // Он сам возьмет правильный UID внутри себя
                    viewModel.createSituationCollection(fixedName, SituationsRepository.getSituations(situation_id));
                } else {
                    // ГОСТЬ: Сохраняем локально
                    viewModel.saveSituation(SituationsRepository.getSituations(situation_id));
                }
                Intent intent = new Intent(NewSelectQ12Activity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NewSelectQ12Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ12Activity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}