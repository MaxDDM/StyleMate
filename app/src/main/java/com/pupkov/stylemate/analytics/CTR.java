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
    static DatabaseReference tableAnalytics = database.getReference("Analytics");
    public static void updateOutfitShows(int outfitId) {
        Observer<Resource<Integer>> observer = new Observer<Resource<Integer>>() {
            @Override
            public void onChanged(Resource<Integer> resource) {
                if (Objects.requireNonNull(resource.status) == Resource.Status.SUCCESS) {
                    tableOutfits.child(String.valueOf(outfitId)).child("countShows").setValue(resource.data + 1);
                    getOutfitShows(outfitId).removeObserver(this);
                }
            }
        };

        getOutfitShows(outfitId).observeForever(observer);
    }

    public static void setZeroOutfitShows() {
        tableOutfits.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    tableOutfits.child(Objects.requireNonNull(snapshot1.getKey())).child("countShows").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static LiveData<Resource<Integer>> getOutfitShows(int outfitId) {
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

    private static LiveData<Resource<String>> getData(int outfitId, String dataName) {
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

    public static LiveData<Resource<Integer>> countAppearanceInSelections(String style, String situation) {
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

    public static void setCTR(int outfitId) {
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
                                                    if (Objects.requireNonNull(resource3.status) == Resource.Status.SUCCESS) {
                                                        if(resource3.data != 0) {
                                                            tableAnalytics.child("CTR").child(String.valueOf(outfitId)).setValue(resource.data / resource3.data * 100);
                                                        } else {
                                                            tableAnalytics.child("CTR").child(String.valueOf(outfitId)).setValue(0);
                                                        }
                                                        countAppearanceInSelections(resource1.data, resource2.data).removeObserver(this);
                                                    }
                                                }
                                            };

                                            countAppearanceInSelections(resource1.data, resource2.data).observeForever(observer3);
                                        }
                                        getData(outfitId,"situation").removeObserver(this);
                                    }
                                };

                                getData(outfitId,"situation").observeForever(observer2);
                                getData(outfitId, "style").removeObserver(this);
                            }
                        }
                    };

                    getData(outfitId,"style").observeForever(observer1);
                    getOutfitShows(outfitId).removeObserver(this);
                }
            }
        };

        getOutfitShows(outfitId).observeForever(observer);
    }

    public static void setCTRforAllOutfits() {
        tableOutfits.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    setCTR(Integer.parseInt(Objects.requireNonNull(snapshot1.getKey())));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
