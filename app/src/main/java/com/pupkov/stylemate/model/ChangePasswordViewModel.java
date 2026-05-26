package com.pupkov.stylemate.model;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.pupkov.stylemate.repository.UserRepository;

public class ChangePasswordViewModel extends ViewModel {

    private final UserRepository repository = new UserRepository();

    private final MediatorLiveData<Resource<Boolean>> _oldPasswordCorrect = new MediatorLiveData<>();
    public LiveData<Resource<Boolean>> oldPasswordCorrect = _oldPasswordCorrect;

    private LiveData<Resource<Boolean>> source;

    private final MutableLiveData<Boolean> _passwordChanged = new MutableLiveData<>();
    public LiveData<Boolean> passwordChanged = _passwordChanged;

    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent = _errorEvent;

    public void verifyOldPassword(String input, Context context) {
        if (source != null) {
            _oldPasswordCorrect.removeSource(source);
        }

        source = repository.checkCurrentPassword(input, context);

        _oldPasswordCorrect.addSource(source, _oldPasswordCorrect::setValue);
    }

    public void submitNewPassword(String input, Context context) {
        if (input.length() < 4) {
            _errorEvent.setValue("Слишком короткий пароль");
            return;
        }

        repository.changePassword(input, context);

        _passwordChanged.setValue(true);
    }
}