package com.pupkov.stylemate.model;

import android.content.Context;
import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.pupkov.stylemate.repository.UserRepository;

/**
 * ViewModel для экрана настроек.
 * Нужна для загрузки данных пользователя и обработки кнопки "Выйти из аккаунта".
 */
public class SettingsViewModel extends AndroidViewModel {

    private final UserRepository repository = new UserRepository();

    // LiveData, которая умеет переключаться на разные источники данных
    private final MediatorLiveData<Resource<UserProfile>> _userProfile = new MediatorLiveData<>();
    public LiveData<Resource<UserProfile>> userProfile = _userProfile;

    // Переменная для хранения текущего источника данных из базы
    private LiveData<Resource<UserProfile>> source;

    private final MutableLiveData<Boolean> _logoutEvent = new MutableLiveData<>();
    public LiveData<Boolean> logoutEvent = _logoutEvent;

    public SettingsViewModel(Application application) {
        super(application);
        // Запускаем загрузку данных сразу при создании экрана
        loadData();
    }

    /**
     * Загружает данные профиля из репозитория.
     */
    private void loadData() {
        // Если до этого уже что-то загружали — отвязываемся от старого источника, чтобы не было путаницы
        if (source != null) {
            _userProfile.removeSource(source);
        }

        // Просим у базы свежие данные профиля
        source = repository.getUserProfile(getApplication());

        // Как только в базе что-то изменится, UI сразу об этом узнает.
        _userProfile.addSource(source, _userProfile::setValue);
    }

    /**
     * Метод для подтверждения выхода из профиля.
     * Очищает данные в репозитории и дает команду экрану на закрытие.
     */
    public void onLogoutConfirmed(Context context) {
        // Стираем данные пользователя на устройстве
        repository.logout(context);

        // Говорим Activity, что выход успешно завершен
        _logoutEvent.setValue(true);
    }
}