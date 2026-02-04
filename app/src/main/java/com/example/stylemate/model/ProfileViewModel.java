package com.example.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stylemate.ui.FavouriteOutfits;
import com.example.stylemate.repository.UserRepository;

import java.util.List;

public class ProfileViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    // 1. Список избранного
    private final MutableLiveData<List<FavouriteOutfits>> _favorites = new MutableLiveData<>();
    public LiveData<List<FavouriteOutfits>> favorites = _favorites;

    // 2. Флаг: Пусто или нет? (Чтобы Фрагмент знал, показывать ли картинку "Пусто")
    private final MutableLiveData<Boolean> _isEmptyState = new MutableLiveData<>();
    public LiveData<Boolean> isEmptyState = _isEmptyState;

    public ProfileViewModel() {
        loadData();
    }

    private void loadData() {
        List<FavouriteOutfits> data = repository.getFavoriteOutfits();

        // Обновляем список
        _favorites.setValue(data);

        // Обновляем состояние "Пусто/Не пусто"
        _isEmptyState.setValue(data.isEmpty());
    }
}