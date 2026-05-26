package com.pupkov.stylemate.repository;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pupkov.stylemate.model.Story;

import java.util.ArrayList;
import java.util.List;

public class StoryRepository {

    private final DatabaseReference dbRef;

    public StoryRepository() {
        dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
    }

    public interface StoryCallback {
        void onStoriesLoaded(List<Story> stories);
        void onError(String error);
    }

    public void getStories(StoryCallback callback) {
        dbRef.child("stories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Story> stories = new ArrayList<>();

                for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                    String id = storySnapshot.getKey();
                    String imageUrl = storySnapshot.child("imageUrl").getValue(String.class);
                    String link = storySnapshot.child("link").getValue(String.class);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        stories.add(new Story(id, imageUrl, link));
                    }
                }

                callback.onStoriesLoaded(stories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}