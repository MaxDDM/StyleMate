package com.example.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.stylemate.repository.UserRepository;

public class ChangePasswordViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    // СОБЫТИЯ (Events) для Фрагмента

    // 1. Успешный ввод старого пароля (переход на шаг 2)
    private final MutableLiveData<Boolean> _oldPasswordCorrect = new MutableLiveData<>();
    public LiveData<Boolean> oldPasswordCorrect = _oldPasswordCorrect;

    // 2. Успешная смена пароля (закрыть окно)
    private final MutableLiveData<Boolean> _passwordChanged = new MutableLiveData<>();
    public LiveData<Boolean> passwordChanged = _passwordChanged;

    // 3. Ошибка (показать тост и иконку)
    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent = _errorEvent;


    // Логика проверки старого пароля
    public void verifyOldPassword(String input) {
        if (repository.checkCurrentPassword(input)) {
            _oldPasswordCorrect.setValue(true);
        } else {
            _errorEvent.setValue("Неверный текущий пароль");
        }
    }

    // Логика сохранения нового
    public void submitNewPassword(String input) {
        if (input.length() < 4) {
            _errorEvent.setValue("Слишком короткий пароль");
            return;
        }

        repository.changePassword(input);
        _passwordChanged.setValue(true);
    }
}