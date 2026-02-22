package com.example.stylemate.model; // Пакет model

import android.content.Context;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.AndroidViewModel;

import com.example.stylemate.repository.UserRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final UserRepository repository = new UserRepository();

    // 1. Данные профиля
    private final MutableLiveData<UserProfile> _userProfile = new MutableLiveData<>();
    public LiveData<UserProfile> userProfile = _userProfile;

    // 2. Событие выхода
    private final MutableLiveData<Boolean> _logoutEvent = new MutableLiveData<>();
    public LiveData<Boolean> logoutEvent = _logoutEvent;

    public SettingsViewModel(Application application) {
        super(application);
        // Сразу загружаем данные
        loadData();
    }

    private void loadData() {
        // 3. getApplication() — это тот самый Context, который ты искал
        UserProfile profile = repository.getUserProfile(getApplication());
        _userProfile.setValue(profile);
    }

    public void onLogoutConfirmed(Context context) {
        repository.logout(context);
        _logoutEvent.setValue(true); // Сообщаем Activity, что пора на выход
    }

    // Сюда можно добавить методы для сохранения изменений (имя, телефон)
}