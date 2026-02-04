package com.example.stylemate.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.stylemate.R;

public class MainActivity extends AppCompatActivity {

    private View bgHomeSelected, bgProfileSelected;
    private ImageView iconHome, iconProfile;

    // СОЗДАЕМ ПЕРЕМЕННЫЕ ДЛЯ ФРАГМЕНТОВ
    // Мы создаем их один раз здесь и будем использовать всю жизнь
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment profileFragment = new ProfileFragment();

    // Запоминаем, какой фрагмент сейчас активен
    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. НАХОДИМ ЭЛЕМЕНТЫ
        View btnHome = findViewById(R.id.btnHome);
        View btnProfile = findViewById(R.id.btnProfile);
        bgHomeSelected = findViewById(R.id.bgHomeSelected);
        bgProfileSelected = findViewById(R.id.bgProfileSelected);
        iconHome = findViewById(R.id.iconHome);
        iconProfile = findViewById(R.id.iconProfile);

        // 2. ИНИЦИАЛИЗАЦИЯ (ВАЖНЫЙ МОМЕНТ)
        // Мы сразу добавляем HomeFragment на экран
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "HOME")
                .commit();

        // Красим кнопки под Home
        updateIcons(true);

        // 3. НАСТРАИВАЕМ КЛИКИ
        btnHome.setOnClickListener(v -> {
            // Если нажали Home, переключаемся на homeFragment
            switchFragment(homeFragment);
            updateIcons(true); // true = мы дома
        });

        btnProfile.setOnClickListener(v -> {
            // Если нажали Profile, переключаемся на profileFragment
            switchFragment(profileFragment);
            updateIcons(false); // false = мы в профиле
        });
    }

    // --- НОВАЯ ЛОГИКА ПЕРЕКЛЮЧЕНИЯ (Hide / Show) ---
    private void switchFragment(Fragment targetFragment) {
        // Если мы уже на этом экране - ничего не делать
        if (activeFragment == targetFragment) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        // 1. Прячем текущий активный фрагмент (он не умирает, просто исчезает)
        transaction.hide(activeFragment);

        // 2. Показываем целевой фрагмент
        if (!targetFragment.isAdded()) {
            // Если фрагмент открывается ПЕРВЫЙ раз в жизни - добавляем его (.add)
            transaction.add(R.id.fragment_container, targetFragment);
        } else {
            // Если он уже был открыт раньше - просто показываем (.show)
            transaction.show(targetFragment);
        }

        transaction.commit();

        // Запоминаем, что теперь активен новый фрагмент
        activeFragment = targetFragment;
    }

    // Метод просто для перекраски иконок (чтобы не дублировать код)
    private void updateIcons(boolean isHomeActive) {
        if (isHomeActive) {
            iconHome.setColorFilter(Color.parseColor("#3D5AFE"));
            bgHomeSelected.setVisibility(View.VISIBLE);
            iconProfile.setColorFilter(Color.parseColor("#505050"));
            bgProfileSelected.setVisibility(View.GONE);
        } else {
            iconHome.setColorFilter(Color.parseColor("#505050"));
            bgHomeSelected.setVisibility(View.GONE);
            iconProfile.setColorFilter(Color.parseColor("#3D5AFE"));
            bgProfileSelected.setVisibility(View.VISIBLE);
        }
    }
}