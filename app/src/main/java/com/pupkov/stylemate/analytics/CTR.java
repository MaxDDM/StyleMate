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

public class CTR {
    static FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    static DatabaseReference tableOutfits = database.getReference("outfits");
    static DatabaseReference tableCollections = database.getReference("user_collections");
    static DatabaseReference tableAnalytics = database.getReference("analytics");
    public static void updateOutfitShows(int outfitId) {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                switch (resource.status) {
                    case SUCCESS:
                        tableOutfits.child(String.valueOf(outfitId)).child("countShows").setValue(resource.data + 1);
                        getOutfitShows(outfitId).removeObserver(this);
                        break;
                }
            }
        };

        getOutfitShows(outfitId).observeForever(observer);
    }

    static LiveData<Resource<Integer>> getOutfitShows(int outfitId) {
        MutableLiveData<Resource<Integer>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        tableOutfits.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer count;
                if(snapshot.child(String.valueOf(outfitId)).child("countShows").exists()) {
                    count = snapshot.child(String.valueOf(outfitId)).child("countShows").getValue(Integer.class);
                } else {
                    count = 0;
                }
                liveData.setValue(Resource.success(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }

    private LiveData<Resource<String>> getData(int outfitId, String dataName) {
        MutableLiveData<Resource<String>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        tableOutfits.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.child(String.valueOf(outfitId)).child(dataName).getValue(String.class);
                liveData.setValue(Resource.success(data));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }

    public LiveData<Resource<Integer>> countAppearanceInSelections(String style, String situation) {
        MutableLiveData<Resource<Integer>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.loading());

        tableCollections.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> data = snapshot.getChildren();

                int count = 0;
                for (DataSnapshot dataSnapshot : data) {
                    Iterable<DataSnapshot> collections = dataSnapshot.getChildren();
                    for (DataSnapshot collectionSnapshot : collections) {
                        if (style != null && Objects.equals(collectionSnapshot.child("style").getValue(String.class), style) ||
                                situation != null && Objects.equals(collectionSnapshot.child("situation").getValue(String.class), situation)) {
                            ++count;
                        }

                    }
                }
                liveData.setValue(Resource.success(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                liveData.setValue(Resource.error("Возникла ошибка"));
            }
        });

        return liveData;
    }

    public void setCTR(int outfitId) {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {

            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    Observer<Resource<String>> observer1 = new Observer<Resource<String>>() {
                        @Override
                        public void onChanged(Resource<String> resource1) {
                            if (Objects.requireNonNull(resource1.status) == Resource.Status.SUCCESS) {
                                Observer<Resource<String>> observer2 = new Observer<Resource<String>>() {
                                    @Override
                                    public void onChanged(Resource<String> resource2) {
                                        if (Objects.requireNonNull(resource2.status) == Resource.Status.SUCCESS) {
                                            Observer<Resource<Integer>> observer3 = new Observer<Resource<Integer>>() {
                                                @Override
                                                public void onChanged(Resource<Integer> resource3) {
                                                    tableAnalytics.child("CTR").child(String.valueOf(outfitId)).setValue(resource.data / resource3.data * 100);
                                                }
                                            };

                                            countAppearanceInSelections(resource1.data, resource2.data).observeForever(observer3);
                                        }
                                    }
                                };

                                getData(outfitId,"situation").observeForever(observer2);
                            }
                        }
                    };

                    getData(outfitId,"style").observeForever(observer1);
                }
            }
        };

        getOutfitShows(outfitId).observeForever(observer);
    }
}
