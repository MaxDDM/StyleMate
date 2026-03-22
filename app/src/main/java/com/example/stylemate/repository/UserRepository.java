package com.example.stylemate.repository;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.stylemate.R;
// Убедись, что FavouriteOutfits импортирован правильно.
// Если он лежит просто в корне пакета com.example.stylemate, импорт не нужен.
// Если перенес в model - добавь import.
import com.example.stylemate.model.Resource;
import com.example.stylemate.ui.AuthActivity;
import com.example.stylemate.ui.FavouriteOutfits;
import com.example.stylemate.model.UserProfile;
import com.example.stylemate.ui.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserRepository {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    DatabaseReference table = database.getReference("User");
    DatabaseReference connectedRef = database.getReference(".info/connected");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // --- НОВЫЙ ИНТЕРФЕЙС ДЛЯ CALLBACK ---
    public interface ProfileCallback {
        void onLoaded(UserProfile profile);
        void onError(String error);
    }

    // --- НОВЫЙ МЕТОД ДЛЯ ПРОФИЛЯ (One-shot request) ---
    public void loadUserProfile(Context context, ProfileCallback callback) {

        if (!isLogged(context)) {
            // Гость
            callback.onLoaded(null);
            return;
        }

        table.child(getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserProfile user = snapshot.getValue(UserProfile.class);
                    callback.onLoaded(user);
                } else {
                    callback.onError("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public LiveData<Resource<UserProfile>> getUserProfile(Context context) {
        MutableLiveData<Resource<UserProfile>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        if (!isLogged(context)) {
            liveData.setValue(Resource.success(new UserProfile()));
            return liveData;
        }

        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);

                if (connected == null || !connected) {
                    liveData.setValue(Resource.error("Нет соединения с сервером"));
                    return;
                }

                table.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserProfile user = snapshot.child(getUID()).getValue(UserProfile.class);
                        liveData.setValue(Resource.success(user));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        liveData.setValue(Resource.error("Возникла ошибка"));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }

    public void logout(Context context) {
        if (isLogged(context)) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
        }

        ActiveUserInfo.clearAllDefaults(context);
    }

    public LiveData<Resource<Boolean>> sendEmail(String email, String password) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // отправляем письмо для подтверждения
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verifyTask -> {
                                        if (verifyTask.isSuccessful()) {
                                            result.setValue(Resource.success(true));
                                        } else {
                                            result.setValue(Resource.error("Нет связи с сервером"));
                                        }
                                    });
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(((FirebaseAuthUserCollisionException) e).getErrorCode())) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if (verifyTask.isSuccessful()) {
                                                    result.setValue(Resource.success(true));
                                                } else {
                                                    result.setValue(Resource.error("Возникла ошибка"));
                                                }
                                            });
                                } else {
                                    result.setValue(Resource.success(false));
                                }
                            }
                        } else {
                            result.setValue(Resource.error("Нет связи с сервером"));
                        }
                    }
                });

        return result;
    }

    public LiveData<Resource<Boolean>> checkEmailVerifiedAndRegister(String name, String phone, String email, String birthDate, String avatarUrl, String password) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        table.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserProfile userPr = new UserProfile(name, phone, email, birthDate, password, avatarUrl);
                                String uid = mAuth.getCurrentUser().getUid();
                                if (snapshot.child(uid).exists()){
                                    result.setValue(Resource.error("Пользователь уже зарегистрирован"));
                                } else {
                                    table.child(uid).setValue(userPr);
                                    result.setValue(Resource.success(true));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                result.setValue(Resource.error("Возникла ошибка"));
                            }
                        });
                    } else {
                        result.setValue(Resource.success(false));
                    }
                } else {
                    result.setValue(Resource.error("Нет связи с сервером"));
                }
            });
        }

        return result;
    }

    public LiveData<Resource<Boolean>> loginUser(String email, String password) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.setValue(Resource.success(true));
                    } else {
                        Exception e = task.getException();

                        if (e instanceof FirebaseAuthInvalidCredentialsException || e instanceof FirebaseAuthUserCollisionException
                        || e instanceof FirebaseAuthInvalidUserException) {
                            result.setValue(Resource.error("Неверный email или пароль"));
                        } else {
                            result.setValue(Resource.error("Нет связи с сервером"));
                        }
                    }
                });

        return result;
    }

    public LiveData<Resource<Boolean>> checkCurrentPassword(String input, Context context) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        if (!isLogged(context)) {
            result.setValue(Resource.error("Смена пароля недоступна в режиме гостя"));
            return result;
        }

        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);

                if (connected == null || !connected) {
                    result.setValue(Resource.error("Нет соединения с сервером"));
                    return;
                }

                table.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserProfile user = snapshot.child(getUID()).getValue(UserProfile.class);
                        assert user != null;
                        result.setValue(Resource.success(Objects.equals(user.password, input)));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        result.setValue(Resource.error("Возникла ошибка"));
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                result.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return result;
    }

    public void changePassword(String newPassword, Context context) {
        if (isLogged(context)) {
            table.child(getUID()).child("password").setValue(newPassword);

            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updatePassword(newPassword);
        }
    }

    public String getUID() {
        assert mAuth.getCurrentUser() != null;
        return mAuth.getCurrentUser().getUid();
    }

    public boolean isLogged(Context context) {
        return ActiveUserInfo.getDefaults("isRegistered", context) != null &&
                !ActiveUserInfo.getDefaults("isRegistered", context).isEmpty();
    }

    public void changeParameter(String parameter, String value) {
        table.child(getUID()).child(parameter).setValue(value);
    }
}