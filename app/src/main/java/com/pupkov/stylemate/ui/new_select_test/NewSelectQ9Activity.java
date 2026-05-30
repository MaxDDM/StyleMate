package com.pupkov.stylemate.ui.new_select_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.ui.MainActivity;

public class NewSelectQ9Activity extends AppCompatActivity {
    int ans = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.new_select9);

        ImageButton test1Button = findViewById(R.id.btnSelTest19);
        ImageButton test2Button = findViewById(R.id.btnSelTest29);
        ImageButton test3Button = findViewById(R.id.btnSelTest39);
        ImageButton test4Button = findViewById(R.id.btnSelTest49);
        ImageButton nextButton = findViewById(R.id.btnNextQuestSelTest9);
        ImageButton skipButton = findViewById(R.id.btnSkipQuestSelTest9);

        test1Button.setOnClickListener(v -> {
            ans = 1;
            setRadioSelection(test1Button, test2Button, test3Button, test4Button);
        });

        test2Button.setOnClickListener(v -> {
            ans = 2;
            setRadioSelection(test2Button, test1Button, test3Button, test4Button);
        });

        test3Button.setOnClickListener(v -> {
            ans = 3;
            setRadioSelection(test3Button, test1Button, test2Button, test4Button);
        });

        test4Button.setOnClickListener(v -> {
            ans = 4;
            setRadioSelection(test4Button, test1Button, test2Button, test3Button);
        });

        nextButton.setOnClickListener(v -> {
            if (ans != -1) {
                Intent intent;
                switch(ans) {
                    case 1:
                        intent = new Intent(NewSelectQ9Activity.this, NewSelectQ10Activity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(NewSelectQ9Activity.this, NewSelectQ13Activity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(NewSelectQ9Activity.this, NewSelectQ12Activity.class);
                        startActivity(intent);
                        break;
                    case 4:
                        intent = new Intent(NewSelectQ9Activity.this, NewSelectQ11Activity.class);
                        startActivity(intent);
                        break;
                }
            } else {
                Toast.makeText(NewSelectQ9Activity.this, "Вы не выбрали ни один из вариантов", Toast.LENGTH_LONG).show();
            }
        });

        skipButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewSelectQ9Activity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void setRadioSelection(ImageButton selected, ImageButton... others) {
        selected.setBackgroundResource(R.drawable.ic_pic6);
        for (ImageButton other : others) {
            other.setBackgroundResource(R.drawable.ic_pic5);
        }
    }
}