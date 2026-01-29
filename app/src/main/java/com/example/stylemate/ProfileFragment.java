package com.example.stylemate;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    // Пустой конструктор (обязателен для фрагментов)
    public ProfileFragment() {
    }

    // 1. Здесь мы говорим, какой файл верстки использовать
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // 2. А здесь пишем всю логику (как раньше в onCreate)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Кнопка настроек ---
        // Ищем элементы через view.findViewById
        View btnSettings = view.findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                // Вместо ProfileActivity.this пишем requireContext()
                Intent intent = new Intent(requireContext(), SettingsActivity.class);
                startActivity(intent);
            });
        }

        // КНОПКУ ДОМОЙ (btnHome) ОТСЮДА УБРАЛИ. Она теперь в MainActivity.

        // --- Основная логика ---
        TextView tvEmptyState = view.findViewById(R.id.tvEmptyState);
        TextView tvFavoritesTitle = view.findViewById(R.id.tvFavoritesTitle);
        RecyclerView rvFavorites = view.findViewById(R.id.rvFavorites);
        ImageView imgAvatar = view.findViewById(R.id.imgAvatar);
        TextView tvUserName = view.findViewById(R.id.tvUserName);

        // Данные
        List<FavouriteOutfits> myData = new ArrayList<>();

        // --- БЛОК ТЕСТИРОВАНИЯ ---
        myData.add(new FavouriteOutfits(R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4, "На спорте \uD83D\uDCAA"));
        myData.add(new FavouriteOutfits(R.drawable.image5, R.drawable.image6, R.drawable.image7, R.drawable.image8, "На свидание \uD83D\uDC80"));
        myData.add(new FavouriteOutfits(R.drawable.image1, R.drawable.image3, R.drawable.image2, R.drawable.image4, "Для офиса"));
        myData.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        myData.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        myData.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        // -------------------------

        if (myData.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            tvFavoritesTitle.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.GONE);
            imgAvatar.setImageResource(R.drawable.ic_placeholder_avatar);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            tvFavoritesTitle.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.VISIBLE);
            imgAvatar.setImageResource(R.drawable.avatar);

            // Используем requireContext() вместо this
            GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
            rvFavorites.setLayoutManager(layoutManager);

            FavoriteOutfitsAdapter adapter = new FavoriteOutfitsAdapter(myData, item -> {
                Intent intent = new Intent(requireContext(), CollectionDetailActivity.class);
                intent.putExtra("COLLECTION_TITLE", item.title);
                startActivity(intent);
            });

            rvFavorites.setAdapter(adapter);
        }
    }
}