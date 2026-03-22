package com.example.stylemate.model;

import android.app.Application; // Важно: нужен Application для Context
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.ui.FavouriteOutfits;
import com.example.stylemate.repository.UserCollectionsRepository;
import com.example.stylemate.repository.UserRepository;

import java.util.List;

// Меняем ViewModel на AndroidViewModel, чтобы иметь доступ к Context (Application)
public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final UserCollectionsRepository collectionsRepository;

    // Данные профиля
    private final MutableLiveData<String> _userName = new MutableLiveData<>();
    public LiveData<String> userName = _userName;

    private final MutableLiveData<String> _userAvatarUrl = new MutableLiveData<>();
    public LiveData<String> userAvatarUrl = _userAvatarUrl;

    // Данные коллекций
    private final MutableLiveData<List<FavouriteOutfits>> _favorites = new MutableLiveData<>();
    public LiveData<List<FavouriteOutfits>> favorites = _favorites;

    private final MutableLiveData<Boolean> _isEmptyState = new MutableLiveData<>();
    public LiveData<Boolean> isEmptyState = _isEmptyState;

    private final MutableLiveData<Boolean> _navigateToHomeEvent = new MutableLiveData<>();
    public LiveData<Boolean> navigateToHomeEvent = _navigateToHomeEvent;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
        collectionsRepository = new UserCollectionsRepository();
        refreshData();
    }

    // Добавляем публичный метод для обновления, который вызывает приватный loadData
    public void refreshData() {
        loadProfileData();
        loadFavoritesData();
    }

    private void loadProfileData() {
        // Используем НОВЫЙ метод с колбэком
        userRepository.loadUserProfile(getApplication(), new UserRepository.ProfileCallback() {
            @Override
            public void onLoaded(UserProfile profile) {
                if (profile != null) {
                    _userName.setValue(profile.name != null ? profile.name : "Пользователь");
                    _userAvatarUrl.setValue(profile.avatarUrl); // Может быть null
                } else {
                    // Гость
                    _userName.setValue("Гость");
                    _userAvatarUrl.setValue(null);
                }
            }

            @Override
            public void onError(String error) {
                _userName.setValue("Ошибка");
            }
        });
    }

    private void loadFavoritesData() {
        collectionsRepository.getUserCollectionsWithPreviews(getApplication(), new UserCollectionsRepository.DataCallback<List<FavouriteOutfits>>() {
            @Override
            public void onDataLoaded(List<FavouriteOutfits> data) {
                _favorites.setValue(data);
                if (data == null || data.isEmpty()) {
                    if (ActiveUserInfo.getDefaults("guest_selection_name", getApplication()) == null ||
                    ActiveUserInfo.getDefaults("guest_selection_name", getApplication()).isEmpty()) {
                        _navigateToHomeEvent.setValue(true);
                        _isEmptyState.setValue(false);
                    }
                } else {
                    _navigateToHomeEvent.setValue(false);
                    boolean hasLikes = false;
                    for(FavouriteOutfits f : data) if(f.hasLikes()) { hasLikes = true; break; }
                    _isEmptyState.setValue(!hasLikes);
                }
            }
            @Override
            public void onError(String error) { }
        });
    }
}