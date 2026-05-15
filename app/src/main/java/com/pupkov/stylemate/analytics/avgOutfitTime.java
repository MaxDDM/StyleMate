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
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app//");
    static DatabaseReference tableAnalytics = database.getReference("Analytics");
    public static void changeAvgTime(int outfitId, double time) {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    Observer<Resource<Double>> observer1 = new Observer<Resource<Double>>() {
                        @Override
                        public void onChanged(Resource<Double> resource1) {
                            if (Objects.requireNonNull(resource1.status) == Resource.Status.SUCCESS) {
                                tableAnalytics.child("AvgTime").child(String.valueOf(outfitId)).setValue(((resource.data - 1) * resource1.data + time) / resource.data);
                            }
                        }
                    };

                    getAvgTime(outfitId).observeForever(observer1);
                }
            }
        };
        new CTR().getOutfitShows(outfitId).observeForever(observer);
    }

    public static LiveData<Resource<Double>> getAvgTime(int outfitId) {
        MutableLiveData<Resource<Double>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        tableAnalytics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double avgTime = 0D;
                if(snapshot.child("AvgTime").child(String.valueOf(outfitId)).exists()) {
                    avgTime = snapshot.child("AvgTime").child(String.valueOf(outfitId)).getValue(Double.class);
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
