package com.pupkov.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pupkov.stylemate.repository.FiltersRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FiltersViewModel extends ViewModel {

    private final FiltersRepository repository = new FiltersRepository();

    public final LiveData<List<String>> typesList = new MutableLiveData<>(repository.getTypes());
    public final LiveData<List<String>> colorsList = new MutableLiveData<>(repository.getColors());
    public final LiveData<List<String>> seasonsList = new MutableLiveData<>(repository.getSeasons());

    private final MutableLiveData<Set<String>> _selectedTypes = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> selectedTypes = _selectedTypes;

    private final MutableLiveData<Set<String>> _selectedColors = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> selectedColors = _selectedColors;

    private final MutableLiveData<Set<String>> _selectedSeasons = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> selectedSeasons = _selectedSeasons;

    private final MutableLiveData<FilterState> _applyEvent = new MutableLiveData<>();
    public LiveData<FilterState> applyEvent = _applyEvent;


    public void toggleFilter(String filterName, FilterCategory category) {
        switch (category) {
            case TYPE:
                updateSet(_selectedTypes, filterName);
                break;
            case COLOR:
                updateSet(_selectedColors, filterName);
                break;
            case SEASON:
                updateSet(_selectedSeasons, filterName);
                break;
        }
    }

    private void updateSet(MutableLiveData<Set<String>> liveData, String item) {
        Set<String> currentSet = new HashSet<>(liveData.getValue());
        if (currentSet.contains(item)) {
            currentSet.remove(item);
        } else {
            currentSet.add(item);
        }
        liveData.setValue(currentSet);
    }

    public void setInitialState(FilterState state) {
        if (state != null) {
            _selectedTypes.setValue(new HashSet<>(state.getSelectedTypes()));
            _selectedColors.setValue(new HashSet<>(state.getSelectedColors()));
            _selectedSeasons.setValue(new HashSet<>(state.getSelectedSeasons()));
        }
    }

    public void resetAll() {
        _selectedTypes.setValue(new HashSet<>());
        _selectedColors.setValue(new HashSet<>());
        _selectedSeasons.setValue(new HashSet<>());
    }

    public void apply() {
        FilterState state = new FilterState(
                _selectedTypes.getValue(),
                _selectedColors.getValue(),
                _selectedSeasons.getValue()
        );
        _applyEvent.setValue(state);
    }
}