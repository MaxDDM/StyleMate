package com.pupkov.stylemate.repository;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pupkov.stylemate.model.Resource;
import com.pupkov.stylemate.model.UserProfile;
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
import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class UserRepository {

    // Инициализация сервисов Firebase: База данных, Аутентификация и Storage
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    DatabaseReference table = database.getReference("User");
    DatabaseReference connectedRef = database.getReference(".info/connected");
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    // Интерфейс обратного вызова для однократного получения данных профиля
    public interface ProfileCallback {
        void onLoaded(UserProfile profile);
        void onError(String error);
    }

    // Интерфейс обратного вызова для отслеживания загрузки аватарки
    public interface AvatarCallback {
        void onSuccess(String downloadUrl);
        void onError(String error);
    }

    // Однократный запрос профиля через асинхронный слушатель
    public void loadUserProfile(Context context, ProfileCallback callback) {
        if (!isLogged(context)) {
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

    // Получение профиля в виде LiveData с предварительной проверкой интернет-соединения
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

    // Выход из аккаунта и полная очистка локальных настроек устройства
    public void logout(Context context) {
        if (isLogged(context)) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
        }
        ActiveUserInfo.clearAllDefaults(context);
    }

    // Создание учетной записи и отправка email-ссылки для подтверждения почты
    public LiveData<Resource<Boolean>> sendEmail(String email, String password) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
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
                        // Если аккаунт уже создан, но не верифицирован — пробуем выслать письмо повторно
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

    // Проверка клика по верификационной ссылке и создание записи пользователя в Realtime Database
    public LiveData<Resource<Boolean>> checkEmailVerifiedAndRegister(String name, String phone, String email, String birthDate, String avatarUrl, String password) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Обновляем данные пользователя, чтобы считать свежий статус верификации почты
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        table.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                result.setValue(Resource.error("Ошибка Firebase Database: " + error.getMessage()));
                            }
                        });
                    } else {
                        result.setValue(Resource.success(false));
                    }
                } else {
                    result.setValue(Resource.error("Не удалось обновить статус: " + task.getException().getMessage()));
                }
            });
        } else {
            result.setValue(Resource.error("Сессия истекла. Нажмите подтверждение почты еще раз."));
        }

        return result;
    }

    // Вход пользователя в систему по почте и паролю
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
                            String errorMsg = (e != null) ? e.getMessage() : "Неизвестная ошибка сервера";
                            result.setValue(Resource.error("Ошибка: " + errorMsg));
                        }
                    }
                });

        return result;
    }

    // Проверка соответствия введенного пароля текущему (перед его изменением)
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

    // Синхронное обновление пароля в Realtime Database и в системе аутентификации Firebase
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

    // Проверка статуса авторизации через локальные настройки устройства
    public boolean isLogged(Context context) {
        return ActiveUserInfo.getDefaults("isRegistered", context) != null &&
                !ActiveUserInfo.getDefaults("isRegistered", context).isEmpty();
    }

    // Прямое обновление конкретного поля пользователя в базе данных
    public void changeParameter(String parameter, String value) {
        table.child(getUID()).child(parameter).setValue(value);
    }

    // Загрузка аватарки в Storage с последующим сохранением публичной ссылки в Realtime Database
    public void uploadAvatar(Context context, Uri imageUri, AvatarCallback callback) {
        if (!isLogged(context)) {
            callback.onError("Смена аватарки доступна только после регистрации");
            return;
        }

        String uid = getUID();
        StorageReference avatarRef = storageRef.child("avatars/" + uid + ".jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Файл успешно загружен в хранилище, запрашиваем URL-ссылку на него
                    avatarRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String url = downloadUri.toString();

                                // Обновляем поле avatarUrl в профиле базы данных
                                table.child(uid).child("avatarUrl").setValue(url)
                                        .addOnSuccessListener(unused -> {
                                            callback.onSuccess(url);
                                        })
                                        .addOnFailureListener(e -> {
                                            callback.onError("Не удалось сохранить ссылку: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                callback.onError("Не удалось получить ссылку на фото: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError("Ошибка загрузки фото: " + e.getMessage());
                });
    }
}