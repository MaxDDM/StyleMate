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

/**
 * Репозиторий для работы с вещами в Firebase.
 */
public class ItemsRepository {

    private final DatabaseReference dbRef;

    public ItemsRepository() {
        dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("items");
    }

    /**
     * Интерфейс обратного вызова для асинхронной передачи результата загрузки вещей.
     */
    public interface ItemsCallback {
        void onItemsLoaded(List<Item> items);
        void onError(String error);
    }

    /**
     * Загрузка вещей по списку их идентификаторов.
     * Запускает параллельные асинхронные запросы к Firebase для каждого ID.
     */
    public void getItemsByIds(List<String> itemIds, ItemsCallback callback) {
        // Использование потокобезопасной обертки, так как колбэки Firebase могут возвращаться в разных потоках
        List<Item> loadedItems = Collections.synchronizedList(new ArrayList<>());

        if (itemIds == null || itemIds.isEmpty()) {
            callback.onItemsLoaded(new ArrayList<>());
            return;
        }

        // счетчики и флаги для координации параллельных сетевых запросов
        final int totalToLoad = itemIds.size();
        final int[] loadedCount = {0};
        final boolean[] hasError = {false};

        for (String id : itemIds) {
            dbRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (hasError[0]) return;

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
                    hasError[0] = true; // Блокировка отправки последующих успешных ответов при сбое
                    callback.onError(error.getMessage());
                }

                /**
                 * Синхронизированный барьер завершения.
                 * Гарантирует, что callback сработает строго после того, как завершится последний сетевой запрос.
                 */
                private synchronized void checkCompletion() {
                    loadedCount[0]++;
                    if (loadedCount[0] == totalToLoad) {
                        // Создание копии списка для предотвращения ConcurrentModificationException в UI-потоке
                        callback.onItemsLoaded(new ArrayList<>(loadedItems));
                    }
                }
            });
        }
    }
}