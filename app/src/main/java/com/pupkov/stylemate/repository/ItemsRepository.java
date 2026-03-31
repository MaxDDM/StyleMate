package com.pupkov.stylemate.repository;

import androidx.annotation.NonNull;

import com.pupkov.stylemate.model.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemsRepository {

    private final DatabaseReference dbRef;

    public ItemsRepository() {
        // Используем ТОТ ЖЕ URL, что и в UserCollectionsRepository
        dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("items"); // Сразу ссылаемся на ветку items
    }

    // Используем такой же интерфейс колбэка, чтобы было единообразно
    public interface ItemsCallback {
        void onItemsLoaded(List<Item> items);
        void onError(String error);
    }

    /**
     * Загружает список вещей по их ID.
     * @param itemIds - список ключей (например ["item_101", "item_102"])
     */
    public void getItemsByIds(List<String> itemIds, ItemsCallback callback) {
        List<Item> loadedItems = Collections.synchronizedList(new ArrayList<>());

        if (itemIds == null || itemIds.isEmpty()) {
            callback.onItemsLoaded(new ArrayList<>());
            return;
        }

        // Счетчик для отслеживания загрузок (так как Firebase асинхронный)
        final int totalToLoad = itemIds.size();
        final int[] loadedCount = {0};
        final boolean[] hasError = {false};

        for (String id : itemIds) {
            dbRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (hasError[0]) return; // Если уже была ошибка, игнорируем

                    Item item = snapshot.getValue(Item.class);
                    if (item != null) {
                        item.setId(snapshot.getKey());
                        loadedItems.add(item);
                    }

                    checkCompletion();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (hasError[0]) return;
                    hasError[0] = true;
                    callback.onError(error.getMessage());
                }

                // Внутренний метод проверки: всё ли загрузилось?
                private synchronized void checkCompletion() {
                    loadedCount[0]++;
                    if (loadedCount[0] == totalToLoad) {
                        // Все запросы вернулись, отдаем список
                        callback.onItemsLoaded(new ArrayList<>(loadedItems));
                    }
                }
            });
        }
    }
}