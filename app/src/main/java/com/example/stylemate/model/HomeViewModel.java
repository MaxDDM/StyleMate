package com.example.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.stylemate.repository.UserCollectionsRepository;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final UserCollectionsRepository repository = new UserCollectionsRepository();

    // 1. Список коллекций (На него подпишется Фрагмент)
    private final MutableLiveData<List<String>> _collections = new MutableLiveData<>();
    public LiveData<List<String>> collections = _collections;

    // 2. Текущий выбор (Храним состояние здесь!)
    private final MutableLiveData<String> _selectedName = new MutableLiveData<>();
    public LiveData<String> selectedName = _selectedName;

    public HomeViewModel() {
        // При создании ViewModel сразу грузим данные
        loadData();
        // По дефолту выбрана "Основная"
        _selectedName.setValue("Основная");
    }

    private void loadData() {
        // Берем данные из Репозитория
        List<String> data = repository.getCollections();
        _collections.setValue(data);
    }

    // Метод, который вызовет Фрагмент, когда юзер нажмет на пункт
    public void onCollectionSelected(String name) {
        _selectedName.setValue(name);
    }
}