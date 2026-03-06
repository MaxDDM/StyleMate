package com.example.stylemate.model;

import android.app.Application; // Важно: нужен Application для Context
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stylemate.ui.FavouriteOutfits;
import com.example.stylemate.repository.UserCollectionsRepository;

import java.util.List;

// Меняем ViewModel на AndroidViewModel, чтобы иметь доступ к Context (Application)
public class ProfileViewModel extends AndroidViewModel {

    private final UserCollectionsRepository repository;

    private final MutableLiveData<List<FavouriteOutfits>> _favorites = new MutableLiveData<>();
    public LiveData<List<FavouriteOutfits>> favorites = _favorites;

    private final MutableLiveData<Boolean> _isEmptyState = new MutableLiveData<>();
    public LiveData<Boolean> isEmptyState = _isEmptyState;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserCollectionsRepository();
        loadData();
    }

    // Добавляем публичный метод для обновления, который вызывает приватный loadData
    public void refreshData() {
        loadData();
    }

    private void loadData() {
        // Передаем Application как Context
        repository.getUserCollectionsWithPreviews(getApplication(), new UserCollectionsRepository.DataCallback<List<FavouriteOutfits>>() {
            @Override
            public void onDataLoaded(List<FavouriteOutfits> data) {
                // 1. Сохраняем список (даже если он с пустыми папками, пусть адаптер их получит)
                _favorites.setValue(data);

                // 2. ПРОВЕРЯЕМ: А есть ли вообще хоть один лайк во всех папках?
                boolean hasAnyLikes = false;

                if (data != null) {
                    for (FavouriteOutfits folder : data) {
                        if (folder.hasLikes()) {
                            hasAnyLikes = true;
                            break; // Нашли хотя бы одну картинку — всё, выходим, не пусто!
                        }
                    }
                }

                // 3. Если лайков нет совсем — показываем экран "Пусто"
                // (hasAnyLikes == false -> isEmptyState = true)
                _isEmptyState.setValue(!hasAnyLikes);
            }

            @Override
            public void onError(String error) {
                // Можно добавить LiveData для ошибки, если нужно
                _isEmptyState.setValue(true);
            }
        });
    }
}