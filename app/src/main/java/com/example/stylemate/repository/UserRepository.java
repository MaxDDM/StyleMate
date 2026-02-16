package com.example.stylemate.repository;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.stylemate.R;
// Убедись, что FavouriteOutfits импортирован правильно.
// Если он лежит просто в корне пакета com.example.stylemate, импорт не нужен.
// Если перенес в model - добавь import.
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

    public UserProfile getUserProfile(Context context) {
        String email = ActiveUserInfo.getDefaults("isRegistered", context);

        final UserProfile[] user = new UserProfile[1];
        table.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user[0] = snapshot.child(email).getValue(UserProfile.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Возникла проблема, скорее всего нет соединения с интернетом", Toast.LENGTH_LONG).show();
            }
        });

        return user[0];
    }

    public void logout(Context context) {
        ActiveUserInfo.setDefaults("isRegistered", "0", context);
    }

    public boolean login(String name, String phone, String email, String birthDate, int avatarResId, String password, Context context) {
        UserProfile user = new UserProfile(name, phone, email, birthDate, password, avatarResId);

        final boolean[] res = {true};
        table.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(user.email).exists()) {
                    Toast.makeText(context, "Это имя занято", Toast.LENGTH_LONG).show();
                    res[0] = false;
                } else {
                    table.child(user.email).setValue(user);
                    Toast.makeText(context, "Успешная регистрация", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Возникла проблема, скорее всего нет соединения с интернетом", Toast.LENGTH_LONG).show();
                res[0] = false;
            }
        });

        return res[0];
    }

    public boolean checkCurrentPassword(String input, Context context) {
        UserProfile user = getUserProfile(context);

        if (Objects.equals(user.password, input)) {
            return true;
        }

        return false;
    }

    public void changePassword(String newPassword, Context context) {
        UserProfile user = getUserProfile(context);
        user.password = newPassword;

        table.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                table.child(user.email).setValue(user);
                Toast.makeText(context, "Установлен новый пароль", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Возникла проблема, скорее всего нет соединения с интернетом", Toast.LENGTH_LONG).show();
            }
        });
    }
}