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

public class CR {
    public void updateCountLink(int outfitId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");

        DatabaseReference tableOutfits = database.getReference("outfits");

        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    tableOutfits.child(String.valueOf(outfitId)).child("countLinks").setValue(resource.data + 1);
                    getCountLink(outfitId).removeObserver(this);
                }
            }
        };

        getCountLink(outfitId).observeForever(observer);
    }

    public LiveData<Resource<Integer>> getCountLink(int outfitId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");

        DatabaseReference tableOutfits = database.getReference("outfits");

        MutableLiveData<Resource<Integer>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        tableOutfits.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer countLinks = 0;
                if(snapshot.child(String.valueOf(outfitId)).child("countLinks").exists()) {
                    countLinks = snapshot.child(String.valueOf(outfitId)).child("countLinks").getValue(Integer.class);
                }
                liveData.setValue(Resource.success(countLinks));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }

    public void countCR(int outfitId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");

        DatabaseReference tableAnalytics = database.getReference("Analytics");

        CTR ctr = new CTR();

        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    Observer<Resource<Integer>> observer1 = new Observer<Resource<Integer>>() {
                        @Override
                        public void onChanged(Resource<Integer> resource1) {
                            if (Objects.requireNonNull(resource1.status) == Resource.Status.SUCCESS) {
                                tableAnalytics.child("CR").child(String.valueOf(outfitId)).setValue((double) resource.data / resource1.data);
                                ctr.getOutfitShows(outfitId).removeObserver(this);
                            }
                        }
                    };

                    ctr.getOutfitShows(outfitId).observeForever(observer1);
                }
            }
        };

        getCountLink(outfitId).observeForever(observer);
    }
}
