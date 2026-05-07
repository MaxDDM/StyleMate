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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class TimeAnalytics {
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    static DatabaseReference tableAnalytics = database.getReference("Analytics");
    public static void saveDate(LocalDateTime date, String uid) {
        tableAnalytics.child("Dates").child(uid).child(String.valueOf(date)).setValue("true");
    }

    public static void countRR() {
        Observer<Resource<HashMap<String, HashMap<LocalDateTime, String>>>> observer = new Observer<Resource<HashMap<String, HashMap<LocalDateTime, String>>>>() {
            @Override
            public void onChanged(Resource<HashMap<String, HashMap<LocalDateTime, String>>> resource) {
                LocalDateTime dateNow = LocalDateTime.now();
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    int count1 = 0;
                    int count2 = 0;
                    for (String key : resource.data.keySet()) {
                        Boolean existsBefore = false;
                        Boolean existsAfter = false;
                        for (LocalDateTime date : Objects.requireNonNull(resource.data.get(key)).keySet()) {
                            if (date.isBefore(dateNow.minusDays(30))) {
                                existsBefore = true;
                            }
                            if (date.isAfter(dateNow.minusDays(30))) {
                                existsAfter = true;
                            }
                        }
                        if (existsBefore && existsAfter) {
                            ++count1;
                        }
                        if (existsBefore) {
                            ++count2;
                        }
                    }
                    tableAnalytics.child("RR").setValue((double) count1 / count2);
                }
            }
        };

        getDates().observeForever(observer);
    }

    public static void countAU(int days) {
        Observer<Resource<HashMap<String, HashMap<LocalDateTime, String>>>> observer = new Observer<Resource<HashMap<String, HashMap<LocalDateTime, String>>>>() {
            @Override
            public void onChanged(Resource<HashMap<String, HashMap<LocalDateTime, String>>> resource) {
                LocalDateTime dateNow = LocalDateTime.now();
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    int count = 0;
                    for (String key : resource.data.keySet()) {
                        for (LocalDateTime date : Objects.requireNonNull(resource.data.get(key)).keySet()) {
                            if (date.plusDays(days).isAfter(dateNow)) {
                                ++count;
                                break;
                            }
                        }
                    }
                    switch(days) {
                        case 30:
                            tableAnalytics.child("MAU").setValue(count);
                            break;
                        case 7:
                            tableAnalytics.child("WAU").setValue(count);
                            break;
                        case 1:
                            tableAnalytics.child("DAU").setValue(count);
                            break;
                    }
                }
            }
        };

        getDates().observeForever(observer);
    }

    public static LiveData<Resource<HashMap<String, HashMap<LocalDateTime, String>>>> getDates() {
        MutableLiveData<Resource<HashMap<String, HashMap<LocalDateTime, String>>>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        tableAnalytics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, HashMap<LocalDateTime, String>> dates = snapshot.child("Dates").getValue(HashMap.class);
                liveData.setValue(Resource.success(dates));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }
}
