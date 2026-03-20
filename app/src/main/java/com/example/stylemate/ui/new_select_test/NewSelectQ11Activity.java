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

public class NewSelectQ11Activity extends AppCompatActivity {
    int ans = -1;

    TestViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select11);

        ImageButton test1Button = findViewById(R.id.btnSelTest111);
        ImageButton test2Button = findViewById(R.id.btnSelTest211);
        ImageButton test3Button = findViewById(R.id.btnSelTest311);
        ImageButton test4Button = findViewById(R.id.btnSelTest411);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest11);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest11);


        test1Button.setOnClickListener(v -> {
            ans = 1;

            test1Button.setBackgroundResource(R.drawable.ic_pic6);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic6);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic6);
            test4Button.setBackgroundResource(R.drawable.ic_pic5);
        });

        test4Button.setOnClickListener(v -> {
            ans = 4;

            test1Button.setBackgroundResource(R.drawable.ic_pic5);
            test2Button.setBackgroundResource(R.drawable.ic_pic5);
            test3Button.setBackgroundResource(R.drawable.ic_pic5);
            test4Button.setBackgroundResource(R.drawable.ic_pic6);
        });

        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                int situation_id = ans + 3;

                viewModel = new ViewModelProvider(this).get(TestViewModel.class);
                // String name = ActiveUserInfo.getDefaults("collectionName", NewSelectQ11Activity.this);
                String fixedName = "Подборка";
                String isReg = ActiveUserInfo.getDefaults("isRegistered", NewSelectQ11Activity.this);
                if (isReg != null && !isReg.isEmpty()) {
                    // ЮЗЕР: Вызываем НОВЫЙ правильный метод
                    // Он сам возьмет правильный UID внутри себя
                    viewModel.createSituationCollection(fixedName, SituationsRepository.getSituations(situation_id));
                } else {
                    // ГОСТЬ: Сохраняем локально
                    viewModel.saveSituation(SituationsRepository.getSituations(situation_id));
                }
                Intent intent = new Intent(NewSelectQ11Activity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(NewSelectQ11Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ11Activity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}