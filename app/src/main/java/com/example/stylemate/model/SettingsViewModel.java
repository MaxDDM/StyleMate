package com.example.stylemate.model; // Пакет model

import android.content.Context;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.AndroidViewModel;

import com.example.stylemate.repository.UserRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final UserRepository repository = new UserRepository();

    // 1. Данные профиля
    private final MediatorLiveData<Resource<UserProfile>> _userProfile = new MediatorLiveData<>();
    public LiveData<Resource<UserProfile>> userProfile = _userProfile;
    private LiveData<Resource<UserProfile>> source;

    // 2. Событие выхода
    private final MutableLiveData<Boolean> _logoutEvent = new MutableLiveData<>();
    public LiveData<Boolean> logoutEvent = _logoutEvent;

    public SettingsViewModel(Application application) {
        super(application);
        // Сразу загружаем данные
        loadData();
    }

    private void loadData() {
        if (source != null) {
            _userProfile.removeSource(source);
        }

        source = repository.getUserProfile(getApplication());
        _userProfile.addSource(source, _userProfile::setValue);
    }

    public void onLogoutConfirmed(Context context) {
        repository.logout(context);
        _logoutEvent.setValue(true); // Сообщаем Activity, что пора на выход
    }

    // Сюда можно добавить методы для сохранения изменений (имя, телефон)
}