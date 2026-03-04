package com.example.stylemate.model;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.example.stylemate.repository.UserRepository;

public class ChangePasswordViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    // СОБЫТИЯ (Events) для Фрагмента

    // 1. Успешный ввод старого пароля (переход на шаг 2)
    private final MediatorLiveData<Resource<Boolean>> _oldPasswordCorrect = new MediatorLiveData<Resource<Boolean>>();
    public LiveData<Resource<Boolean>> oldPasswordCorrect = _oldPasswordCorrect;
    private LiveData<Resource<Boolean>> source;

    // 2. Успешная смена пароля (закрыть окно)
    private final MutableLiveData<Boolean> _passwordChanged = new MutableLiveData<>();
    public LiveData<Boolean> passwordChanged = _passwordChanged;

    // 3. Ошибка (показать тост и иконку)
    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent = _errorEvent;


    // Логика проверки старого пароля
    public void verifyOldPassword(String input, Context context) {
        if (source != null) {
            _oldPasswordCorrect.removeSource(source);
        }

        source = repository.checkCurrentPassword(input, context);
        _oldPasswordCorrect.addSource(source, _oldPasswordCorrect::setValue);
    }

    // Логика сохранения нового
    public void submitNewPassword(String input, Context context) {
        if (input.length() < 4) {
            _errorEvent.setValue("Слишком короткий пароль");
            return;
        }

        repository.changePassword(input, context);
        _passwordChanged.setValue(true);
    }
}