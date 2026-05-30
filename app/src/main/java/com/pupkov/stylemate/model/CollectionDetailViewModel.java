package com.pupkov.stylemate.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pupkov.stylemate.repository.UserCollectionsRepository;

import java.util.ArrayList;
import java.util.List;

public class CollectionDetailViewModel extends AndroidViewModel {

    private final UserCollectionsRepository repository;
    private String collectionId;

    private final MutableLiveData<String> _title = new MutableLiveData<>();
    public LiveData<String> title = _title;

    private final MutableLiveData<List<Outfit>> _outfits = new MutableLiveData<>();
    public LiveData<List<Outfit>> outfits = _outfits;

    private final MutableLiveData<Boolean> _closeScreenEvent = new MutableLiveData<>();
    public LiveData<Boolean> closeScreenEvent = _closeScreenEvent;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    public CollectionDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new UserCollectionsRepository();
    }

    public void init(String id, String initialTitle) {
        this.collectionId = id;

        if (_title.getValue() == null) {
            _title.setValue(initialTitle);
        }

        loadOutfits();
    }

    public void refresh() {
        loadOutfits();
    }

    private void loadOutfits() {
        if (collectionId == null) return;

        repository.getCollectionFavorites(getApplication(), collectionId, new UserCollectionsRepository.DataCallback<List<Outfit>>() {
            @Override
            public void onDataLoaded(List<Outfit> data) {
                _outfits.setValue(data);
                if (data == null || data.isEmpty()) {
                    _closeScreenEvent.setValue(true);
                }
            }

            @Override
            public void onError(String error) {
                _toastMessage.setValue("Ошибка загрузки: " + error);
            }
        });
    }

    public void removeOutfit(String outfitId) {
        List<Outfit> currentList = _outfits.getValue();
        if (currentList == null) return;

        List<Outfit> updatedList = new ArrayList<>(currentList);

        boolean removed = false;
        for (int i = 0; i < updatedList.size(); i++) {
            if (updatedList.get(i).getId().equals(outfitId)) {
                updatedList.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            _outfits.setValue(updatedList);

            repository.toggleLikeInFirebase(getApplication(), collectionId, outfitId, false);

            if (updatedList.isEmpty()) {
                _closeScreenEvent.setValue(true);
            }
        }
    }
}