package com.example.stylemate.model; // Пакет model

import android.content.Context;

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

    public SettingsViewModel(Context context) {
        loadData(context);
    }

    private void loadData(Context context) {
        UserProfile profile = repository.getUserProfile(context);
        _userProfile.setValue(profile);
    }

    public void onLogoutConfirmed(Context context) {
        repository.logout(context);
        _logoutEvent.setValue(true); // Сообщаем Activity, что пора на выход
    }

    // Сюда можно добавить методы для сохранения изменений (имя, телефон)
}