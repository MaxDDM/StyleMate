package com.example.stylemate.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stylemate.repository.ItemsRepository;
import com.example.stylemate.repository.UserCollectionsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OutfitDetailViewModel extends AndroidViewModel {

    private final ItemsRepository repository;
    private final UserCollectionsRepository collectionsRepository;

    // Данные для UI
    private final MutableLiveData<List<Item>> _items = new MutableLiveData<>();
    public LiveData<List<Item>> items = _items;

    private final MutableLiveData<String> _title = new MutableLiveData<>();
    public LiveData<String> title = _title;

    private final MutableLiveData<String> _totalPrice = new MutableLiveData<>();
    public LiveData<String> totalPrice = _totalPrice;

    public OutfitDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new ItemsRepository();
        collectionsRepository = new UserCollectionsRepository();
    }

    // Этот метод мы вызовем из Activity сразу после получения Intent
    public void init(Outfit outfit) {
        if (outfit == null) return;

        // 1. Генерируем заголовок
        generateTitle(outfit.getFilter_season(), outfit.getStyle());

        // 2. Загружаем вещи
        loadItems(outfit.getItems());
    }

    // --- НОВЫЙ МЕТОД: ЛАЙК ---
    public void toggleLike(String collectionId, String outfitId, boolean isLiked) {
        collectionsRepository.toggleLikeInFirebase(getApplication(), collectionId, outfitId, isLiked);
    }

    private void generateTitle(String season, String style) {
        // Преобразуем английские ключи в русский текст
        String styleRu = convertStyle(style);

        // Формат: "Образ на [лето] в [классическом] стиле"
        String result = "Образ " + season + " в " + styleRu + " стиле";
        _title.setValue(result);
    }

    private void loadItems(Map<String, Boolean> itemsMap) {
        if (itemsMap == null || itemsMap.isEmpty()) {
            _items.setValue(new ArrayList<>());
            return;
        }

        // Превращаем Map {"id1": true, "id2": true} в List ["id1", "id2"]
        List<String> ids = new ArrayList<>(itemsMap.keySet());

        repository.getItemsByIds(ids, new ItemsRepository.ItemsCallback() {
            @Override
            public void onItemsLoaded(List<Item> loadedItems) {
                _items.setValue(loadedItems);
                calculateTotalPrice(loadedItems); // Считаем сумму
            }

            @Override
            public void onError(String error) {
                // Можно добавить LiveData для ошибок, если нужно
            }
        });
    }

    private void calculateTotalPrice(List<Item> items) {
        int total = 0;
        for (Item item : items) {
            // Парсим цену "2399 Р" -> 2399
            if (item.getPrice() != null) {
                String cleanPrice = item.getPrice().replaceAll("[^0-9]", ""); // Убираем " Р" и пробелы
                try {
                    total += Integer.parseInt(cleanPrice);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        _totalPrice.setValue(total + " Р");
    }

    // --- ХЕЛПЕРЫ ПЕРЕВОДА ---

    private String convertStyle(String styleKey) {
        if (styleKey == null) return "любом";
        switch (styleKey.toLowerCase()) {
            case "casual": return "повседневном"; // или "кэжуал"
            case "classic": return "классическом";
            case "grunge": return "гранж";
            case "old_money": return "олд мани";
            case "sport": return "спортивном";
            default: return styleKey;
        }
    }
}