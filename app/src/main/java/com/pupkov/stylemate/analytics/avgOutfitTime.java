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

public class avgOutfitTime {
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    static DatabaseReference tableAnalytics = database.getReference("analytics");
    public static void changeAvgTime(int outfitId, double time) {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    Observer<Resource<Long>> observer1 = new Observer<Resource<Long>>() {
                        @Override
                        public void onChanged(Resource<Long> resource1) {
                            tableAnalytics.child("AvgTime").child(String.valueOf(outfitId)).setValue((resource.data * resource1.data + time) / (resource.data + 1));
                        }
                    };

                    getAvgTime(outfitId).observeForever(observer1);
                }
            }
        };

        CTR.getOutfitShows(outfitId).observeForever(observer);
    }

    public static LiveData<Resource<Long>> getAvgTime(int outfitId) {
        MutableLiveData<Resource<Long>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        tableAnalytics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long avgTime = 0L;
                if(snapshot.child("AvgTime").child(String.valueOf(outfitId)).exists()) {
                    avgTime = snapshot.child("AvgTime").child(String.valueOf(outfitId)).getValue(Long.class);
                }
                liveData.setValue(Resource.success(avgTime));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }

}
