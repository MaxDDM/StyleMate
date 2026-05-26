package com.pupkov.stylemate.model;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.UserCollectionsRepository;
import com.pupkov.stylemate.repository.UserRepository;
import com.pupkov.stylemate.repository.StoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeViewModel extends AndroidViewModel {

    private final UserRepository repo = new UserRepository();
    private final UserCollectionsRepository repository;
    private final StoryRepository storyRepository;
    private String currentCollectionId = null;

    private final MutableLiveData<List<String>> _collections = new MutableLiveData<>();
    public LiveData<List<String>> collections = _collections;

    private final MutableLiveData<Boolean> _isEmptyState = new MutableLiveData<>();
    public LiveData<Boolean> isEmptyState = _isEmptyState;

    private final MutableLiveData<List<Story>> _stories = new MutableLiveData<>();
    public LiveData<List<Story>> stories = _stories;

    private final MutableLiveData<String> _selectedName = new MutableLiveData<>();
    public LiveData<String> selectedName = _selectedName;
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    private List<Outfit> allOutfits = new ArrayList<>();
    private final MutableLiveData<List<Outfit>> _outfits = new MutableLiveData<>();
    public LiveData<List<Outfit>> outfits = _outfits;

    private final MutableLiveData<Boolean> _filterEmptyEvent = new MutableLiveData<>();
    public LiveData<Boolean> filterEmptyEvent = _filterEmptyEvent;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new UserCollectionsRepository();
        storyRepository = new StoryRepository();
        loadCollectionsList();
        loadStories();
    }

    public String getCurrentCollectionId() { return currentCollectionId; }

    private void loadStories() {
        storyRepository.getStories(new StoryRepository.StoryCallback() {
            @Override
            public void onStoriesLoaded(List<Story> data) {
                _stories.setValue(data != null ? data : new ArrayList<>());
            }

            @Override
            public void onError(String error) {
                _toastMessage.setValue("Ошибка загрузки историй: " + error);
            }
        });
    }

    private void loadCollectionsList() {
        if (repo.isLogged(getApplication())) {
            repository.getCollectionNames(getApplication(), new UserCollectionsRepository.DataCallback<List<String>>() {
                @Override
                public void onDataLoaded(List<String> data) {
                    if (data == null || data.isEmpty()) {
                        setEmptyState();
                    } else {
                        updateCollectionsState(data);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getApplication(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            String name = ActiveUserInfo.getDefaults("guest_selection_name", getApplication());
            if (name == null) {
                setEmptyState();
            } else {
                updateCollectionsState(new ArrayList<>(Arrays.asList(name)));
            }
        }
    }

    private void setEmptyState() {
        _isEmptyState.setValue(true);
        _collections.setValue(new ArrayList<>());
        _outfits.setValue(new ArrayList<>());
        _selectedName.setValue(null);
    }

    private void updateCollectionsState(List<String> data) {
        _isEmptyState.setValue(false);
        _collections.setValue(data);
        String currentSelected = _selectedName.getValue();

        if (currentSelected != null && data.contains(currentSelected)) {
            loadOutfits(currentSelected);
        } else {
            onCollectionSelected(data.get(0));
        }
    }

    public void onCollectionSelected(String name) {
        _selectedName.setValue(name);
        loadOutfits(name);
    }

    private void loadOutfits(String collectionName) {
        repository.getOutfitsForCollection(collectionName, getApplication(), new UserCollectionsRepository.CollectionDataCallback() {
            @Override
            public void onDataLoaded(List<Outfit> data, String collectionId) {
                currentCollectionId = collectionId;
                allOutfits = data != null ? data : new ArrayList<>();
                _outfits.setValue(allOutfits);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getApplication(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void applyFilters(FilterState state) {
        if (allOutfits.isEmpty()) return;

        if (state.isEmpty()) {
            _outfits.setValue(allOutfits);
            return;
        }

        List<Outfit> tempResult = new ArrayList<>();

        for (Outfit outfit : allOutfits) {
            boolean typeMatch = checkMatch(state.getSelectedTypes(), outfit.getFilter_types());
            boolean colorMatch = checkMatch(state.getSelectedColors(), outfit.getFilter_colors());

            boolean seasonMatch = true;
            if (!state.getSelectedSeasons().isEmpty()) {
                if (outfit.getFilter_season() == null || !state.getSelectedSeasons().contains(outfit.getFilter_season())) {
                    seasonMatch = false;
                }
            }

            if (typeMatch && colorMatch && seasonMatch) {
                tempResult.add(outfit);
            }
        }

        if (tempResult.isEmpty()) {
            _filterEmptyEvent.setValue(true);
            _filterEmptyEvent.setValue(false);
        } else {
            _outfits.setValue(tempResult);
        }
    }

    private boolean checkMatch(Set<String> selectedFilters, Map<String, Boolean> itemTags) {
        if (selectedFilters.isEmpty()) return true;
        if (itemTags == null) return false;

        for (String filter : selectedFilters) {
            for (String tagKey : itemTags.keySet()) {
                if (tagKey.equalsIgnoreCase(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void toggleLike(String outfitId) {
        if (currentCollectionId == null) return;

        boolean newState = false;
        List<Outfit> currentList = _outfits.getValue();

        if (currentList != null) {
            for (Outfit o : currentList) {
                if (o.getId().equals(outfitId)) {
                    newState = !o.isLiked();
                    o.setLiked(newState);
                    break;
                }
            }
            _outfits.setValue(currentList);

            for (Outfit o : allOutfits) {
                if (o.getId().equals(outfitId)) {
                    o.setLiked(newState);
                    break;
                }
            }
        }
        repository.toggleLikeInFirebase(getApplication(), currentCollectionId, outfitId, newState);
    }

    private void refreshLikesOnly() {
        if (currentCollectionId == null) return;

        repository.getLikedIdsOnly(getApplication(), currentCollectionId, new UserCollectionsRepository.DataCallback<List<String>>() {
            @Override
            public void onDataLoaded(List<String> likedIds) {
                if (allOutfits == null) return;

                boolean changed = false;
                for (Outfit outfit : allOutfits) {
                    boolean isLikedInDb = likedIds.contains(outfit.getId());
                    if (outfit.isLiked() != isLikedInDb) {
                        outfit.setLiked(isLikedInDb);
                        changed = true;
                    }
                }

                if (changed) {
                    _outfits.setValue(_outfits.getValue());
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    public void refreshData() {
        loadStories();
        repository.getCollectionNames(getApplication(), new UserCollectionsRepository.DataCallback<List<String>>() {
            @Override
            public void onDataLoaded(List<String> data) {
                if (data == null || data.isEmpty()) {
                    setEmptyState();
                    currentCollectionId = null;
                    allOutfits.clear();
                    return;
                }

                _isEmptyState.setValue(false);
                _collections.setValue(data);

                String currentName = _selectedName.getValue();
                if (currentName != null && data.contains(currentName)) {
                    refreshLikesOnly();
                } else {
                    onCollectionSelected(data.get(0));
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    public void onCollectionRenamed(String newName) {
        String collectionId = getCurrentCollectionId();
        if (collectionId == null) return;

        repository.renameCollection(getApplication(), collectionId, newName);

        List<String> currentList = new ArrayList<>(_collections.getValue());
        String oldName = _selectedName.getValue();
        int index = currentList.indexOf(oldName);

        if (index != -1) {
            currentList.set(index, newName);
            _collections.setValue(currentList);
            _selectedName.setValue(newName);
        }
        _toastMessage.setValue("Название подборки изменено");
    }

    public void onCollectionDeleted() {
        String collectionId = getCurrentCollectionId();
        if (collectionId == null) return;

        repository.deleteCollection(getApplication(), collectionId);

        List<String> currentList = new ArrayList<>(_collections.getValue());
        currentList.remove(_selectedName.getValue());

        _collections.setValue(currentList);

        if (!currentList.isEmpty()) {
            onCollectionSelected(currentList.get(0));
        } else {
            setEmptyState();
        }
        _toastMessage.setValue("Подборка удалена");
    }

}