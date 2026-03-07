package com.example.stylemate.repository;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.stylemate.model.Outfit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserCollectionsRepository {

    private final DatabaseReference dbRef;

    public UserCollectionsRepository() {
        // Ссылка на корень БД
        dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
    }

    // Интерфейс для возврата данных
    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String error);
    }

    // =========================================================================
    // МЕТОД 1: Получить список названий коллекций
    // =========================================================================
    public void getCollectionNames(Context context, DataCallback<List<String>> callback) {
        String rawEmail = ActiveUserInfo.getDefaults("isRegistered", context);
        boolean isLogged = rawEmail != null && !rawEmail.equals("0");

        if (!isLogged) {
            // ГОСТЬ
            List<String> guestList = new ArrayList<>();
            guestList.add("Основная");
            callback.onDataLoaded(guestList);
        } else {
            // ЮЗЕР
            String safeEmail = rawEmail.replace(".", "|");

            dbRef.child("user_collections").child(safeEmail)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<String> names = new ArrayList<>();
                            for (DataSnapshot collection : snapshot.getChildren()) {
                                String name = collection.child("name").getValue(String.class);
                                if (name != null) {
                                    names.add(name);
                                }
                            }
                            if (names.isEmpty()) names.add("Основная");
                            callback.onDataLoaded(names);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        }
    }

    // =========================================================================
    // МЕТОД 2: Получить одежду (С учетом СТИЛЯ и СИТУАЦИИ)
    // =========================================================================
    public void getOutfitsForCollection(String collectionName, Context context, DataCallback<List<Outfit>> callback) {
        String rawEmail = ActiveUserInfo.getDefaults("isRegistered", context);
        boolean isLogged = rawEmail != null && !rawEmail.equals("0");

        if (!isLogged) {
            // --- ГОСТЬ ---
            String guestStyle = ActiveUserInfo.getDefaults("guest_style_name", context);
            String targetStyle = (guestStyle != null) ? guestStyle : "casual";

            // Гостю грузим стиль, ситуация = null (значит любая)
            loadOutfitsFromDbFiltered(collectionName, targetStyle, callback, context);

        } else {
            // --- ЮЗЕР ---
            String safeEmail = rawEmail.replace(".", "|");

            dbRef.child("user_collections").child(safeEmail)
                    .orderByChild("name").equalTo(collectionName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot match : snapshot.getChildren()) {
                                    // 1. Достаем параметры коллекции
                                    String style = match.child("style").getValue(String.class);

                                    // Сезон игнорируем (он только для фильтров UI)

                                    // 2. Грузим
                                    loadOutfitsFromDbFiltered(collectionName, style != null ? style : "casual", callback, context);
                                    return;
                                }
                            } else {
                                // Если коллекции нет (баг?), грузим casual
                                loadOutfitsFromDbFiltered(collectionName, "casual", callback, context);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        }
    }

    // =========================================================================
    // ВНУТРЕННИЙ МЕТОД ЗАГРУЗКИ (ИЗ ПАПКИ OUTFITS)
    // =========================================================================
    private void loadOutfitsFromDbFiltered(String collectionName, String style, DataCallback<List<Outfit>> callback, Context context) {

        // !!! ИСПРАВЛЕНО: Теперь ищем в папке "outfits" !!!
        dbRef.child("outfits")
                .orderByChild("style").equalTo(style)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Outfit> filteredList = new ArrayList<>();

                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                String email = ActiveUserInfo.getDefaults("isRegistered", context);
                                if (snapshot1.child("user_collections").child(email).child(collectionName).child("situation").exists()) {
                                    String[] situations = snapshot1.child("user_collections").child(email).child(collectionName).child("situation").getValue(String.class).split(",");

                                    for (DataSnapshot item : snapshot.getChildren()) {
                                        Outfit outfit = item.getValue(Outfit.class);
                                        if (outfit == null) continue;

                                        // !!! ВАЖНО: Присваиваем ID из ключа Firebase, чтобы работали лайки !!!
                                        outfit.setId(item.getKey());

                                        // --- ФИЛЬТРАЦИЯ ПО СИТУАЦИИ ---
                                        for (int i = 0; i < situations.length; ++i) {
                                            boolean isSituationMatch = true;
                                            if (situations[i] != null && !situations[i].isEmpty() && !situations[i].equals("any")) {

                                                // Получаем ситуацию ОДЕЖДЫ
                                                String outfitSit = outfit.getFilter_situation();

                                                // ЛАЙФХАК: Если поля в базе нет (null), считаем его "any" (универсальным)
                                                if (outfitSit == null) {
                                                    outfitSit = "any";
                                                }

                                                // Если вещь НЕ универсальна ("any") И не совпадает с требованием -> удаляем
                                                if (!outfitSit.equals("any") && !outfitSit.equals(situations[i])) {
                                                    isSituationMatch = false;
                                                }
                                            }

                                            if (isSituationMatch) {
                                                filteredList.add(outfit);
                                            }
                                        }
                                    }
                                    callback.onDataLoaded(filteredList);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error1) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}