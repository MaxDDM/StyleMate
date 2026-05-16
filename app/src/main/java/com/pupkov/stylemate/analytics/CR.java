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
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    static DatabaseReference tableOutfits = database.getReference("outfits");
    static DatabaseReference tableAnalytics = database.getReference("Analytics");
    public static void updateCountLink(int outfitId) {
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

    public static void setZeroCountLink() {
        tableOutfits.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    tableOutfits.child(Objects.requireNonNull(snapshot1.getKey())).child("countLinks").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static LiveData<Resource<Integer>> getCountLink(int outfitId) {
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

    public static void countCR(int outfitId) {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    Observer<Resource<Integer>> observer1 = new Observer<Resource<Integer>>() {
                        @Override
                        public void onChanged(Resource<Integer> resource1) {
                            if (Objects.requireNonNull(resource1.status) == Resource.Status.SUCCESS) {
                                if (resource1.data != 0) {
                                    tableAnalytics.child("CR").child(String.valueOf(outfitId)).setValue((double) resource.data / resource1.data);
                                } else {
                                    tableAnalytics.child("CR").child(String.valueOf(outfitId)).setValue(0d);
                                }
                                CTR.getOutfitShows(outfitId).removeObserver(this);
                            }
                        }
                    };

                    CTR.getOutfitShows(outfitId).observeForever(observer1);
                }
            }
        };

        getCountLink(outfitId).observeForever(observer);
    }

    public static void setCRforAllOutfits() {
        tableOutfits.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    countCR(Integer.parseInt(Objects.requireNonNull(snapshot1.getKey())));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
