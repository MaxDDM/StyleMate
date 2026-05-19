package com.pupkov.stylemate.ui;

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

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.analytics.TimeAnalytics;
import com.pupkov.stylemate.repository.UserRepository;

import java.time.LocalDateTime;

public class MainActivity extends AppCompatActivity {

    private View bgHomeSelected, bgProfileSelected;
    private ImageView iconHome, iconProfile;

    // Однократная инициализация фрагментов для кэширования в памяти
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private final UserRepository repo = new UserRepository();

    private Fragment activeFragment = homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Логирование времени начала сессии авторизованного пользователя
        if (repo.isLogged(MainActivity.this)) {
            TimeAnalytics.saveDate(LocalDateTime.now(), repo.getUID());
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View btnHome = findViewById(R.id.btnHome);
        View btnProfile = findViewById(R.id.btnProfile);
        bgHomeSelected = findViewById(R.id.bgHomeSelected);
        bgProfileSelected = findViewById(R.id.bgProfileSelected);
        iconHome = findViewById(R.id.iconHome);
        iconProfile = findViewById(R.id.iconProfile);

        // Установка начального экрана
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "HOME")
                .commit();

        updateIcons(true);

        btnHome.setOnClickListener(v -> {
            switchFragment(homeFragment);
            updateIcons(true);
        });

        btnProfile.setOnClickListener(v -> {
            switchFragment(profileFragment);
            updateIcons(false);
        });
    }

    // Оптимизированное переключение фрагментов без пересоздания их жизненного цикла
    private void switchFragment(Fragment targetFragment) {
        if (activeFragment == targetFragment) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        transaction.hide(activeFragment);

        if (!targetFragment.isAdded()) {
            transaction.add(R.id.fragment_container, targetFragment);
        } else {
            transaction.show(targetFragment);
        }

        transaction.commit();
        activeFragment = targetFragment;
    }

    // Управление визуальным состоянием навигационной панели
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