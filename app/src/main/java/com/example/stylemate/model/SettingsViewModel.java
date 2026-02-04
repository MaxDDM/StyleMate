package com.example.stylemate.model; // Пакет model

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stylemate.repository.UserRepository;

public class SettingsViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    // 1. Данные профиля
    private final MutableLiveData<UserProfile> _userProfile = new MutableLiveData<>();
    public LiveData<UserProfile> userProfile = _userProfile;

    // 2. Событие выхода
    private final MutableLiveData<Boolean> _logoutEvent = new MutableLiveData<>();
    public LiveData<Boolean> logoutEvent = _logoutEvent;

    public SettingsViewModel() {
        loadData();
    }

    private void loadData() {
        // Загружаем профиль
        UserProfile profile = repository.getUserProfile();
        _userProfile.setValue(profile);
    }

    public void onLogoutConfirmed() {
        repository.logout();
        _logoutEvent.setValue(true); // Сообщаем Activity, что пора на выход
    }

    // Сюда можно добавить методы для сохранения изменений (имя, телефон)
}