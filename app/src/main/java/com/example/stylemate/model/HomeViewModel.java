package com.example.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.stylemate.repository.UserCollectionsRepository;
import java.util.List;
import java.util.ArrayList;
import com.example.stylemate.model.Outfit;
import com.example.stylemate.R; // Чтобы видеть картинки
public class HomeViewModel extends ViewModel {

    private final UserCollectionsRepository repository = new UserCollectionsRepository();

    // 1. Список коллекций (На него подпишется Фрагмент)
    private final MutableLiveData<List<String>> _collections = new MutableLiveData<>();
    public LiveData<List<String>> collections = _collections;

    // 2. Текущий выбор (Храним состояние здесь!)
    private final MutableLiveData<String> _selectedName = new MutableLiveData<>();
    public LiveData<String> selectedName = _selectedName;

    private final MutableLiveData<List<Outfit>> _outfits = new MutableLiveData<>();
    public LiveData<List<Outfit>> outfits = _outfits;

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

    // Метод генерации данных (Заглушка)
    public void loadOutfitsForStyle(int styleId) {
        List<Outfit> list = new ArrayList<>();

        // ВАЖНО: Для эффекта Pinterest (разная высота) используй разные картинки
        // Тут просто пример, добавь свои картинки из drawables
        list.add(new Outfit(1, R.drawable.image1, false));
        list.add(new Outfit(2, R.drawable.image2, true));
        list.add(new Outfit(3, R.drawable.image3, false));
        list.add(new Outfit(4, R.drawable.image4, false));
        list.add(new Outfit(5, R.drawable.image5, true));
        list.add(new Outfit(6, R.drawable.image6, false));

        _outfits.setValue(list);
    }

    // Логика лайка (пока только визуально)
    public void toggleLike(int outfitId) {
        List<Outfit> currentList = _outfits.getValue();
        if (currentList != null) {
            for (Outfit o : currentList) {
                if (o.getId() == outfitId) {
                    o.setLiked(!o.isLiked());
                    break;
                }
            }
            _outfits.setValue(currentList); // Обновит адаптер
        }
    }
}