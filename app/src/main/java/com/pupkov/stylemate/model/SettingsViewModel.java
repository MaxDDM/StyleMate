package com.pupkov.stylemate.model;

import android.content.Context;
import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.pupkov.stylemate.repository.UserRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final UserRepository repository = new UserRepository();

    private final MediatorLiveData<Resource<UserProfile>> _userProfile = new MediatorLiveData<>();
    public LiveData<Resource<UserProfile>> userProfile = _userProfile;

    private LiveData<Resource<UserProfile>> source;

    private final MutableLiveData<Boolean> _logoutEvent = new MutableLiveData<>();
    public LiveData<Boolean> logoutEvent = _logoutEvent;

    public SettingsViewModel(Application application) {
        super(application);
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

        _logoutEvent.setValue(true);
    }
}