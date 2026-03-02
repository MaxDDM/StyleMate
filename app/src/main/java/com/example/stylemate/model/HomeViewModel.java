package com.example.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stylemate.repository.UserCollectionsRepository;
import com.example.stylemate.model.Outfit;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final UserCollectionsRepository repository = new UserCollectionsRepository();

    // 1. Список имен коллекций
    private final MutableLiveData<List<String>> _collections = new MutableLiveData<>();
    public LiveData<List<String>> collections = _collections;

    // 2. Текущее выбранное имя
    private final MutableLiveData<String> _selectedName = new MutableLiveData<>();
    public LiveData<String> selectedName = _selectedName;

    // 3. Список картинок (образов)
    private final MutableLiveData<List<Outfit>> _outfits = new MutableLiveData<>();
    public LiveData<List<Outfit>> outfits = _outfits;

    public HomeViewModel() {
        // 1. Грузим список имен ("Спорт", "Офис"...)
        loadCollectionsList();

        // 2. Ставим дефолт
        onCollectionSelected("Основная");
    }

    private void loadCollectionsList() {
        List<String> data = repository.getCollections();
        _collections.setValue(data);
    }

    // ЭТОТ МЕТОД ВЫЗЫВАЕТСЯ ПРИ КЛИКЕ НА СПИСОК
    public void onCollectionSelected(String name) {
        // 1. Обновляем выбранное имя (чтобы UI знал, что выделить синим)
        _selectedName.setValue(name);

        // 2. Запрашиваем у Репозитория картинки именно для ЭТОГО имени
        loadOutfits(name);
    }

    // Внутренний метод загрузки
    private void loadOutfits(String collectionName) {
        // Идем в репозиторий за данными
        List<Outfit> specificOutfits = repository.getOutfitsForCollection(collectionName);

        // Обновляем LiveData -> Фрагмент увидит это и обновит сетку
        _outfits.setValue(specificOutfits);
    }

    // Метод для обновления лайка (визуально)
    public void toggleLike(int outfitId) {
        List<Outfit> currentList = _outfits.getValue();
        if (currentList != null) {
            for (Outfit o : currentList) {
                if (o.getId() == outfitId) {
                    o.setLiked(!o.isLiked());
                    break;
                }
            }
            _outfits.setValue(currentList); // Триггерим обновление адаптера
        }
    }
}