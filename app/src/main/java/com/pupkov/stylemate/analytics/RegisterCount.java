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

public class RegisterCount {
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    static DatabaseReference table = database.getReference("Analytics");

    public static void updateRegisterCount() {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                switch (resource.status) {
                    case SUCCESS:
                        table.child("RegisterCount").setValue(resource.data);
                        getUserCount().removeObserver(this);
                        break;
                }
            }
        };

        getUserCount().observeForever(observer);
    }

    private static LiveData<Resource<Integer>> getUserCount() {
        MutableLiveData<Resource<Integer>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        table.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer count = snapshot.child("RegisterCount").getValue(Integer.class);
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
