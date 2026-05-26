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
        dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("items");
    }

    public interface ItemsCallback {
        void onItemsLoaded(List<Item> items);
        void onError(String error);
    }

    public void getItemsByIds(List<String> itemIds, ItemsCallback callback) {
        List<Item> loadedItems = Collections.synchronizedList(new ArrayList<>());

        if (itemIds == null || itemIds.isEmpty()) {
            callback.onItemsLoaded(new ArrayList<>());
            return;
        }

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
                    hasError[0] = true;
                    callback.onError(error.getMessage());
                }

                private synchronized void checkCompletion() {
                    loadedCount[0]++;
                    if (loadedCount[0] == totalToLoad) {
                        callback.onItemsLoaded(new ArrayList<>(loadedItems));
                    }
                }
            });
        }
    }
}