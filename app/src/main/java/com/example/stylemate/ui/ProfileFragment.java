package com.example.stylemate.ui; // Пакет UI

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stylemate.R;
import com.example.stylemate.model.ProfileViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView; // !!! ВАЖНО: Добавь этот импорт

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private FavoriteOutfitsAdapter adapter;

    // UI элементы делаем полями класса, чтобы к ним был доступ везде
    private TextView tvEmptyState;
    private TextView tvFavoritesTitle;
    private RecyclerView rvFavorites;
    private ImageView imgAvatar;
    private TextView tvUserName;

    public ProfileFragment() {
        // Пустой конструктор
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Инициализируем ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // 2. Находим View
        initViews(view);

        // 3. Настраиваем RecyclerView (пустой пока)
        setupRecyclerView();

        // 4. Настраиваем Кнопки (Настройки и т.д.)
        setupClickListeners(view);

        // 5. ПОДПИСЫВАЕМСЯ (Главная часть MVVM)
        observeViewModel();
    }

    // Этот метод срабатывает, когда переключаем вкладки (hide/show)
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // Если фрагмент стал ВИДИМЫМ (hidden = false)
        if (!hidden && viewModel != null) {
            viewModel.refreshData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Как только экран становится видимым — обновляем данные
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    private void initViews(View view) {
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvFavoritesTitle = view.findViewById(R.id.tvFavoritesTitle);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
    }

    private void setupRecyclerView() {
        // Если хочешь 2 колонки:
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvFavorites.setLayoutManager(layoutManager);

        // ВАЖНО: Передаем getContext() первым параметром
        adapter = new FavoriteOutfitsAdapter(getContext(), new ArrayList<>(), item -> {
            // ОБРАБОТКА КЛИКА ПО ПАПКЕ

            // ТУТ БУДЕТ ПЕРЕХОД:
            // Либо открывай CollectionDetailActivity (если она есть)
            // Либо передавай данные на главный экран
            Intent intent = new Intent(requireContext(), CollectionDetailActivity.class);
            intent.putExtra("COLLECTION_TITLE", item.getTitle());
            intent.putExtra("COLLECTION_ID", item.getId()); // Передаем ID!
            startActivity(intent);
        });

        rvFavorites.setAdapter(adapter);
    }

    private void setupClickListeners(View view) {
        View btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void observeViewModel() {
        viewModel.userName.observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                tvUserName.setText(name);
            }
        });

        // --- ЭТОГО НЕ ХВАТАЛО: Подписка на АВАТАРКУ ---
        viewModel.userAvatarUrl.observe(getViewLifecycleOwner(), url -> {
            // Glide загрузит картинку по ссылке или поставит заглушку, если url == null
            if (imgAvatar != null) {
                com.bumptech.glide.Glide.with(this)
                        .load(url)
                        .apply(com.bumptech.glide.request.RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_placeholder_avatar) // Убедись, что этот ресурс существует!
                        .error(R.drawable.ic_placeholder_avatar)
                        .into(imgAvatar);
            }
        });
        // 1. Обновляем список, когда приходят данные
        viewModel.favorites.observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                adapter.updateList(list);
            }
        });

        // 2. Логика НАВИГАЦИИ (Если удалили последнюю подборку)
        viewModel.navigateToHomeEvent.observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                navigateToHome(); // Переходим на Home
            }
        });

        // 2. Управляем видимостью элементов (То, что ты просил)
        viewModel.isEmptyState.observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null && isEmpty) {
                // ЕСЛИ ПУСТО:
                tvEmptyState.setVisibility(View.VISIBLE);      // Показываем "Нет образов"
                tvFavoritesTitle.setVisibility(View.GONE);     // Скрываем заголовок "Избранное"
                rvFavorites.setVisibility(View.GONE);          // Скрываем список
            } else {
                // ЕСЛИ ЕСТЬ ДАННЫЕ:
                tvEmptyState.setVisibility(View.GONE);         // Скрываем "Нет образов"
                tvFavoritesTitle.setVisibility(View.VISIBLE);  // Показываем заголовок "Избранное"
                rvFavorites.setVisibility(View.VISIBLE);       // Показываем список
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            // Мы ищем FrameLayout кнопки (потому что у вас кастомная панель)
            View btnHome = getActivity().findViewById(R.id.btnHome);

            if (btnHome != null) {
                // Программно нажимаем на кнопку, чтобы сработал ваш код в MainActivity
                btnHome.performClick();
            }
        }
    }
}