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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserRepository {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/");
    DatabaseReference table = database.getReference("User");

    // Метод: Получить избранные луки
    public List<FavouriteOutfits> getFavoriteOutfits() {
        List<FavouriteOutfits> data = new ArrayList<>();

        // Имитация данных (потом заменим на БД или API)
        data.add(new FavouriteOutfits(R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4, "На спорте \uD83D\uDCAA"));
        data.add(new FavouriteOutfits(R.drawable.image5, R.drawable.image6, R.drawable.image7, R.drawable.image8, "На свидание \uD83D\uDC80"));
        data.add(new FavouriteOutfits(R.drawable.image1, R.drawable.image3, R.drawable.image2, R.drawable.image4, "Для офиса"));
        data.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        data.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));
        data.add(new FavouriteOutfits(R.drawable.image8, R.drawable.image7, R.drawable.image6, R.drawable.image5, "Прогулка"));

        return data;
    }

    public LiveData<Resource<Boolean>> exists(String email, Context context) {
        MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        table.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(email).exists()) {
                    liveData.setValue(Resource.success(true));
                } else {
                    liveData.setValue(Resource.success(false));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Возникла проблема, скорее всего нет соединения с интернетом", Toast.LENGTH_LONG).show();
            }
        });

        return liveData;
    }

    public LiveData<Resource<UserProfile>> getUserProfile(Context context) {
        MutableLiveData<Resource<UserProfile>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        String email = ActiveUserInfo.getDefaults("isRegistered", context);

        table.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile user = snapshot.child(email).getValue(UserProfile.class);
                liveData.setValue(Resource.success(user));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Возникла проблема, скорее всего нет соединения с интернетом", Toast.LENGTH_LONG).show();
            }
        });

        return liveData;
    }

    public void logout(Context context) {
        ActiveUserInfo.setDefaults("isRegistered", "", context);
    }

    public LiveData<Resource<Boolean>> login(String name, String phone, String email, String birthDate, int avatarResId, String password, Context context) {
        UserProfile user = new UserProfile(name, phone, email, birthDate, password, avatarResId);

        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        table.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                table.child(user.email).setValue(user);
                result.setValue(Resource.success(true));
                Toast.makeText(context, "Успешная регистрация", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Возникла проблема, скорее всего нет соединения с интернетом", Toast.LENGTH_LONG).show();
                result.setValue(Resource.success(false));
            }
        });

        return result;
    }

    public LiveData<Resource<Boolean>> checkCurrentPassword(String input, Context context) {
        MutableLiveData<Resource<Boolean>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        String email = ActiveUserInfo.getDefaults("isRegistered", context);

        table.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile user = snapshot.child(email).getValue(UserProfile.class);
                assert user != null;
                result.setValue(Resource.success(Objects.equals(user.password, input)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Возникла проблема, скорее всего нет соединения с интернетом", Toast.LENGTH_LONG).show();
            }
        });

        return result;
    }

    public void changePassword(String newPassword, Context context) {
        String email = ActiveUserInfo.getDefaults("isRegistered", context);

        table.child(email).child("password").setValue(newPassword);
    }
}