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

    // 1. Списки доступных опций (из репозитория) — остаются как были
    public final LiveData<List<String>> typesList = new MutableLiveData<>(repository.getTypes());
    public final LiveData<List<String>> colorsList = new MutableLiveData<>(repository.getColors());
    public final LiveData<List<String>> seasonsList = new MutableLiveData<>(repository.getSeasons());

    // 2. СОСТОЯНИЕ: Теперь у нас ТРИ отдельных набора для галочек
    private final MutableLiveData<Set<String>> _selectedTypes = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> selectedTypes = _selectedTypes;

    private final MutableLiveData<Set<String>> _selectedColors = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> selectedColors = _selectedColors;

    private final MutableLiveData<Set<String>> _selectedSeasons = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> selectedSeasons = _selectedSeasons;

    // 3. СОБЫТИЕ: Теперь Apply передает не просто Boolean, а готовый объект FilterState
    private final MutableLiveData<FilterState> _applyEvent = new MutableLiveData<>();
    public LiveData<FilterState> applyEvent = _applyEvent;


    // Логика нажатия на фильтр (Вкл/Выкл)
    // Метод теперь принимает категорию, чтобы знать, в какой список лезть
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

    // Вспомогательный метод для обновления Set внутри LiveData
    private void updateSet(MutableLiveData<Set<String>> liveData, String item) {
        Set<String> currentSet = new HashSet<>(liveData.getValue()); // Копируем текущий
        if (currentSet.contains(item)) {
            currentSet.remove(item);
        } else {
            currentSet.add(item);
        }
        liveData.setValue(currentSet); // Триггерим обновление UI
    }

    // Метод для инициализации (вызывается при открытии шторки)
    public void setInitialState(FilterState state) {
        if (state != null) {
            _selectedTypes.setValue(new HashSet<>(state.getSelectedTypes()));
            _selectedColors.setValue(new HashSet<>(state.getSelectedColors()));
            _selectedSeasons.setValue(new HashSet<>(state.getSelectedSeasons()));
        }
    }

    // Логика кнопки "Сбросить"
    // Сбросить всё
    public void resetAll() {
        _selectedTypes.setValue(new HashSet<>());
        _selectedColors.setValue(new HashSet<>());
        _selectedSeasons.setValue(new HashSet<>());
    }

    // Логика кнопки "Применить"
    // Применить: Собираем всё в кучу и отправляем
    public void apply() {
        FilterState state = new FilterState(
                _selectedTypes.getValue(),
                _selectedColors.getValue(),
                _selectedSeasons.getValue()
        );
        _applyEvent.setValue(state);
    }
}