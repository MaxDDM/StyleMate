package com.example.stylemate.ui; // Пакет UI

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stylemate.R;
import com.example.stylemate.model.ProfileViewModel;

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

    private void initViews(View view) {
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvFavoritesTitle = view.findViewById(R.id.tvFavoritesTitle);
        rvFavorites = view.findViewById(R.id.rvFavorites);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvFavorites.setLayoutManager(layoutManager);

        // Создаем адаптер с пустым списком
        adapter = new FavoriteOutfitsAdapter(new ArrayList<>(), item -> {
            Intent intent = new Intent(requireContext(), CollectionDetailActivity.class);
            intent.putExtra("COLLECTION_TITLE", item.title);
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
        // А. Следим за списком избранного
        viewModel.favorites.observe(getViewLifecycleOwner(), list -> {
            adapter.updateList(list); // Обновляем адаптер
        });

        // Б. Следим за состоянием "Пусто/Не пусто"
        viewModel.isEmptyState.observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty) {
                // Если пусто - показываем заглушку
                tvEmptyState.setVisibility(View.VISIBLE);
                tvFavoritesTitle.setVisibility(View.GONE);
                rvFavorites.setVisibility(View.GONE);
                imgAvatar.setImageResource(R.drawable.ic_placeholder_avatar);
            } else {
                // Если есть данные - показываем список и аватарку
                tvEmptyState.setVisibility(View.GONE);
                tvFavoritesTitle.setVisibility(View.VISIBLE);
                rvFavorites.setVisibility(View.VISIBLE);
                imgAvatar.setImageResource(R.drawable.avatar);
            }
        });
    }
}