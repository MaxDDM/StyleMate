package com.pupkov.stylemate.model;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pupkov.stylemate.repository.UserRepository;

/**
 * ViewModel для экрана смены пароля.
 * Управляет шагами проверки старого пароля и сохранения нового.
 */
public class ChangePasswordViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    // Результат проверки старого пароля (загрузка, успех или ошибка)
    private final MediatorLiveData<Resource<Boolean>> _oldPasswordCorrect = new MediatorLiveData<>();
    public LiveData<Resource<Boolean>> oldPasswordCorrect = _oldPasswordCorrect;

    private LiveData<Resource<Boolean>> source;

    // Событие успешной смены пароля (когда станет true — закрываем шторку)
    private final MutableLiveData<Boolean> _passwordChanged = new MutableLiveData<>();
    public LiveData<Boolean> passwordChanged = _passwordChanged;

    // Событие ошибки валидации (например, если новый пароль слишком короткий)
    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent = _errorEvent;

    /**
     * Проверяет, правильно ли пользователь ввел текущий (старый) пароль.
     */
    public void verifyOldPassword(String input, Context context) {
        // Если до этого уже проверяли пароль — отвязываемся от старого запроса
        if (source != null) {
            _oldPasswordCorrect.removeSource(source);
        }

        // Делаем запрос в базу данных для проверки пароля
        source = repository.checkCurrentPassword(input, context);

        // Связываем результат из репозитория с нашей LiveData для UI
        _oldPasswordCorrect.addSource(source, _oldPasswordCorrect::setValue);
    }

    /**
     * Проверяет новый пароль на длину и отправляет его на сохранение в базу.
     */
    public void submitNewPassword(String input, Context context) {
        // Простая проверка: если пароль меньше 4 символов — показываем ошибку
        if (input.length() < 4) {
            _errorEvent.setValue("Слишком короткий пароль");
            return;
        }

        // Если всё хорошо — даем команду репозиторию обновить пароль в базе
        repository.changePassword(input, context);

        // Говорим экрану, что пароль успешно изменен
        _passwordChanged.setValue(true);
    }
}