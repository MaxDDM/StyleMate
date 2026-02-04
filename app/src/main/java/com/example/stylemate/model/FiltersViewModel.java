package com.example.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stylemate.repository.FiltersRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FiltersViewModel extends ViewModel {

    private final FiltersRepository repository = new FiltersRepository();

    // Данные для отображения кнопок (Списки)
    public final LiveData<List<String>> typesList = new MutableLiveData<>(repository.getTypes());
    public final LiveData<List<String>> colorsList = new MutableLiveData<>(repository.getColors());
    public final LiveData<List<String>> seasonsList = new MutableLiveData<>(repository.getSeasons());

    // Состояние: Какие кнопки сейчас нажаты (активны)
    // Храним все выбранные названия в одном наборе ("синий", "зима")
    private final MutableLiveData<Set<String>> _selectedFilters = new MutableLiveData<>(new HashSet<>());
    public LiveData<Set<String>> selectedFilters = _selectedFilters;

    // Событие: "Фильтры применены" (Чтобы фрагмент закрылся)
    private final MutableLiveData<Boolean> _applyEvent = new MutableLiveData<>();
    public LiveData<Boolean> applyEvent = _applyEvent;


    // Логика нажатия на фильтр (Вкл/Выкл)
    public void toggleFilter(String filterName) {
        Set<String> currentSet = new HashSet<>(_selectedFilters.getValue()); // Копируем текущий набор

        if (currentSet.contains(filterName)) {
            currentSet.remove(filterName); // Если был - убираем
        } else {
            currentSet.add(filterName); // Если не было - добавляем
        }

        _selectedFilters.setValue(currentSet); // Обновляем LiveData
    }

    // Логика кнопки "Сбросить"
    public void resetAll() {
        _selectedFilters.setValue(new HashSet<>()); // Пустой набор
    }

    // Логика кнопки "Применить"
    public void apply() {
        // Тут можно сохранить данные в глобальное хранилище или передать в HomeViewModel
        // Пока просто даем сигнал фрагменту закрыться
        _applyEvent.setValue(true);
    }
}