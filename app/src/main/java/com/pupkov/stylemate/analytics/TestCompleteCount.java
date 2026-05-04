package com.pupkov.stylemate.analytics;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pupkov.stylemate.model.Resource;

import java.util.Objects;

public class TestCompleteCount {
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    DatabaseReference tableUser = database.getReference("User");
    DatabaseReference tableCollections = database.getReference("user_collections");
    DatabaseReference tableAnalytics = database.getReference("Analytics");

    public void setTestCompleteCount() {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    Observer<Resource<Integer>> observer1 = new Observer<Resource<Integer>>() {
                        @Override
                        public void onChanged(Resource<Integer> resource1) {
                            if (Objects.requireNonNull(resource1.status) == Resource.Status.SUCCESS) {
                                tableAnalytics.child("TestCompleteCount").setValue(resource.data - resource1.data);
                            }
                        }
                    };

                    getCount(tableCollections).observeForever(observer1);
                }
            }
        };

        getCount(tableUser).observeForever(observer);
    }

    public LiveData<Resource<Integer>> getCount(DatabaseReference table) {
        MutableLiveData<Resource<Integer>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        table.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer count = Math.toIntExact(snapshot.getChildrenCount());
                liveData.setValue(Resource.success(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }
}
