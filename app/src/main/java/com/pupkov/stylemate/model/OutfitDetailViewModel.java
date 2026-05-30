package com.pupkov.stylemate.model;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pupkov.stylemate.repository.ItemsRepository;
import com.pupkov.stylemate.repository.UserCollectionsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ViewModel для управления состоянием экрана деталей образа.
 * Отвечает за загрузку вещей, генерацию заголовков и расчет стоимости.
 */
public class OutfitDetailViewModel extends AndroidViewModel {

    private final ItemsRepository repository;
    private final UserCollectionsRepository collectionsRepository;

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

    /**
     * Первичная инициализация состояния экрана на основе данных, полученных из Intent.
     */
    public void init(Outfit outfit) {
        if (outfit == null) return;

        generateTitle(outfit.getFilter_season(), outfit.getStyle());
        loadItems(outfit.getItems());
    }

    /**
     * Синхронизация статуса лайка с удаленным репозиторием Firebase.
     */
    public void toggleLike(String collectionId, String outfitId, boolean isLiked) {
        collectionsRepository.toggleLikeInFirebase(getApplication(), collectionId, outfitId, isLiked);
    }

    /**
     * Формирование читаемого заголовка образа с локализацией стиля на русский язык.
     */
    private void generateTitle(String season, String style) {
        String styleRu = convertStyle(style);
        String result = "Образ " + season + " в " + styleRu + " стиле";
        _title.setValue(result);
    }

    /**
     * Асинхронный запрос списка вещей из Firebase по их уникальным идентификаторам.
     */
    private void loadItems(Map<String, Boolean> itemsMap) {
        if (itemsMap == null || itemsMap.isEmpty()) {
            _items.setValue(new ArrayList<>());
            return;
        }

        // Извлечение ID вещей из Map структуры Firebase для передачи плоского списка в репозиторий
        List<String> ids = new ArrayList<>(itemsMap.keySet());

        repository.getItemsByIds(ids, new ItemsRepository.ItemsCallback() {
            @Override
            public void onItemsLoaded(List<Item> loadedItems) {
                _items.setValue(loadedItems);
                calculateTotalPrice(loadedItems);
            }

            @Override
            public void onError(String error) {
            }
        });
    }

    /**
     * Вычисление суммарной стоимости всех вещей в образе.
     */
    private void calculateTotalPrice(List<Item> items) {
        int total = 0;
        for (Item item : items) {
            if (item.getPrice() != null) {
                // Регулярное выражение удаляет всё, кроме цифр, для безопасного парсинга (например, "2 399 Р" -> "2399")
                String cleanPrice = item.getPrice().replaceAll("[^0-9]", "");
                try {
                    total += Integer.parseInt(cleanPrice);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        _totalPrice.setValue(total + " Р");
    }

    /**
     * Маппинг строковых ключей стилей из базы данных в склоняемые русскоязычные эквиваленты.
     */
    private String convertStyle(String styleKey) {
        if (styleKey == null) return "любом";
        switch (styleKey.toLowerCase()) {
            case "casual": return "повседневном";
            case "classic": return "классическом";
            case "grange": return "гранж";
            case "old_money": return "олд мани";
            case "sport": return "спортивном";
            default: return styleKey;
        }
    }

    public void dislikeOutfit(String collectionId, String outfitId) {
        if (collectionId == null || outfitId == null) return;
        collectionsRepository.dislikeOutfitInFirebase(getApplication(), collectionId, outfitId);
    }
}