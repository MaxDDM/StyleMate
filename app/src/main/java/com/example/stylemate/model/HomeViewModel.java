package com.example.stylemate.model;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stylemate.repository.UserCollectionsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeViewModel extends AndroidViewModel {

    private final UserCollectionsRepository repository;
    private String currentCollectionId = null; // Храним ID текущей коллекции
    // Списки данных
    private final MutableLiveData<List<String>> _collections = new MutableLiveData<>();
    public LiveData<List<String>> collections = _collections;

    private final MutableLiveData<Boolean> _isEmptyState = new MutableLiveData<>();
    public LiveData<Boolean> isEmptyState = _isEmptyState;
    private final MutableLiveData<String> _selectedName = new MutableLiveData<>();
    public LiveData<String> selectedName = _selectedName;

    // --- ИЗМЕНЕНИЕ: Два списка ---
    // 1. Полный список (кеш текущей коллекции), никогда не фильтруется
    private List<Outfit> allOutfits = new ArrayList<>();

    // 2. Список для отображения (LiveData), который меняется фильтрами
    private final MutableLiveData<List<Outfit>> _outfits = new MutableLiveData<>();
    public LiveData<List<Outfit>> outfits = _outfits;

    // Событие ошибки фильтрации (для показа Toast/Dialog во фрагменте)
    private final MutableLiveData<Boolean> _filterEmptyEvent = new MutableLiveData<>();
    public LiveData<Boolean> filterEmptyEvent = _filterEmptyEvent;


    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new UserCollectionsRepository();
        loadCollectionsList();
    }

    public String getCurrentCollectionId() { return currentCollectionId; }

    private void loadCollectionsList() {
        repository.getCollectionNames(new UserCollectionsRepository.DataCallback<List<String>>() {
            @Override
            public void onDataLoaded(List<String> data) {
                // 1. Проверяем на пустоту
                if (data == null || data.isEmpty()) {
                    // СПИСОК ПУСТ -> Включаем режим заглушки
                    _isEmptyState.setValue(true);
                    _collections.setValue(new ArrayList<>()); // Пустой список в адаптер
                    _outfits.setValue(new ArrayList<>());     // Пустая сетка
                    _selectedName.setValue(null);             // Нет выбранного имени
                } else {
                    // СПИСОК ЕСТЬ -> Обычный режим
                    _isEmptyState.setValue(false);
                    _collections.setValue(data);
                    String currentSelected = _selectedName.getValue();

                    // 1. Если текущая коллекция всё еще существует в списке -> ОБНОВЛЯЕМ ЕЁ ДАННЫЕ
                    if (currentSelected != null && data.contains(currentSelected)) {
                        // Мы принудительно грузим одежду заново, чтобы подтянуть актуальные лайки
                        loadOutfits(currentSelected);
                    }
                    // 2. Иначе (если ничего не выбрано или старую коллекцию удалили) -> берем первую
                    else {
                        onCollectionSelected(data.get(0));
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getApplication(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onCollectionSelected(String name) {
        _selectedName.setValue(name);
        loadOutfits(name);
    }

    private void loadOutfits(String collectionName) {
        // Используем новый callback, который возвращает и список, и ID коллекции
        repository.getOutfitsForCollection(collectionName, getApplication(), new UserCollectionsRepository.CollectionDataCallback() {
            @Override
            public void onDataLoaded(List<Outfit> data, String collectionId) {
                currentCollectionId = collectionId; // Сохраняем ID!
                allOutfits = data != null ? data : new ArrayList<>();
                _outfits.setValue(allOutfits);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getApplication(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ГЛАВНЫЙ МЕТОД ФИЛЬТРАЦИИ ---
    public void applyFilters(FilterState state) {
        android.util.Log.d("FILTER_DEBUG", "ViewModel: Начал фильтрацию. Всего вещей в базе: " + allOutfits.size()); // <--- ЛОГ
        // Если база пустая или фильтры пустые -> сбрасываем фильтр (показываем всё)
        if (allOutfits.isEmpty()) return;

        if (state.isEmpty()) {
            _outfits.setValue(allOutfits);
            return;
        }

        List<Outfit> tempResult = new ArrayList<>();

        for (Outfit outfit : allOutfits) {
            // 1. Проверка ТИПОВ (ИЛИ внутри категории)
            boolean typeMatch = checkMatch(state.getSelectedTypes(), outfit.getFilter_types());

            // 2. Проверка ЦВЕТОВ (ИЛИ внутри категории)
            boolean colorMatch = checkMatch(state.getSelectedColors(), outfit.getFilter_colors());

            // 3. Проверка СЕЗОНОВ (Сравниваем строку сезона с набором)
            boolean seasonMatch = true;
            if (!state.getSelectedSeasons().isEmpty()) {
                // Если у аутфита нет сезона, а фильтр включен -> не подходит
                // Если есть -> проверяем наличие в списке выбранных
                if (outfit.getFilter_season() == null || !state.getSelectedSeasons().contains(outfit.getFilter_season())) {
                    seasonMatch = false;
                }
            }

            // ЛОГИЧЕСКОЕ И: Должны совпасть все категории
            if (typeMatch && colorMatch && seasonMatch) {
                tempResult.add(outfit);
            }
        }

        if (tempResult.isEmpty()) {
            // Ничего не нашли -> Сообщаем UI, список на экране НЕ трогаем
            _filterEmptyEvent.setValue(true);
            // Нужно сбросить ивент сразу (можно использовать SingleLiveEvent, но пока так)
            _filterEmptyEvent.setValue(false);
        } else {
            // Нашли -> Обновляем экран
            _outfits.setValue(tempResult);
        }
    }

    // Хелпер: проверяет, есть ли пересечение между выбранными фильтрами и тегами вещи
    private boolean checkMatch(Set<String> selectedFilters, Map<String, Boolean> itemTags) {
        if (selectedFilters.isEmpty()) return true; // Фильтр не выбран -> подходит всё
        if (itemTags == null) return false;

        // Бежим по выбранным фильтрам (например, "белый", "синий")
        for (String filter : selectedFilters) {
            // Бежим по тегам вещи (например, "Белый": true, "Красный": true)
            for (String tagKey : itemTags.keySet()) {
                // Сравниваем игнорируя регистр (case-insensitive)
                if (tagKey.equalsIgnoreCase(filter)) {
                    return true; // Нашли совпадение!
                }
            }
        }
        return false;
    }

    // --- ЛАЙКИ (ОБНОВЛЕНО) ---
    public void toggleLike(String outfitId) {
        if (currentCollectionId == null) return; // Некуда сохранять

        boolean newState = false;

        // 1. Обновляем UI мгновенно
        List<Outfit> currentList = _outfits.getValue();
        if (currentList != null) {
            for (Outfit o : currentList) {
                if (o.getId().equals(outfitId)) {
                    newState = !o.isLiked(); // Переключаем
                    o.setLiked(newState);
                    break;
                }
            }
            _outfits.setValue(currentList); // Триггерим обновление адаптера

            // Обновляем в кеше тоже
            for (Outfit o : allOutfits) {
                if (o.getId().equals(outfitId)) {
                    o.setLiked(newState);
                    break;
                }
            }
        }

        // 2. Отправляем в базу
        repository.toggleLikeInFirebase(currentCollectionId, outfitId, newState);
    }

    private void refreshLikesOnly() {
        if (currentCollectionId == null) return;

        // Вызываем наш НОВЫЙ легкий метод
        repository.getLikedIdsOnly(currentCollectionId,new UserCollectionsRepository.DataCallback<List<String>>() {
            @Override
            public void onDataLoaded(List<String> likedIds) {
                if (allOutfits == null) return;

                boolean changed = false;

                // Пробегаем по уже загруженной одежде в памяти
                for (Outfit outfit : allOutfits) {
                    // Проверяем: есть ли ID этой одежды в списке лайков из базы?
                    boolean isLikedInDb = likedIds.contains(outfit.getId());

                    // Если статус отличается от того, что на экране -> меняем
                    if (outfit.isLiked() != isLikedInDb) {
                        outfit.setLiked(isLikedInDb);
                        changed = true;
                    }
                }

                // Если были изменения, обновляем UI
                if (changed) {
                    // Если используешь фильтры, то лучше вызвать applyFilters(currentFilterState)
                    // Если нет, то просто _outfits.setValue(allOutfits);
                    _outfits.setValue(_outfits.getValue());
                }
            }

            @Override
            public void onError(String error) { /* Игнорируем */ }
        });
    }

    // Метод для обновления данных (вызывается из onResume)
    public void refreshData() {
        // ШАГ 1: Всегда проверяем актуальный список папок (это легкий запрос имен)
        repository.getCollectionNames(new UserCollectionsRepository.DataCallback<List<String>>() {
            @Override
            public void onDataLoaded(List<String> data) {
                // СЛУЧАЙ А: Удалили последнюю коллекцию
                if (data == null || data.isEmpty()) {
                    _isEmptyState.setValue(true);
                    _collections.setValue(new ArrayList<>());
                    _outfits.setValue(new ArrayList<>());
                    _selectedName.setValue(null);
                    currentCollectionId = null;
                    allOutfits.clear(); // Очищаем кэш
                    return; // Заглушка покажется сама благодаря observer в фрагменте
                }

                // СЛУЧАЙ Б: Коллекции есть
                _isEmptyState.setValue(false);
                _collections.setValue(data);

                String currentName = _selectedName.getValue();

                // Проверяем: та папка, на которой мы стояли, всё еще существует?
                if (currentName != null && data.contains(currentName)) {
                    // Папка на месте -> запускаем ЛЕГКОЕ обновление лайков (без перерисовки картинок)
                    refreshLikesOnly();
                }
                else {
                    // Папки больше нет (её удалили, но остались другие) ->
                    // Грузим первую доступную папку полностью (Тяжелый запрос, но это неизбежно)
                    onCollectionSelected(data.get(0));
                }
            }

            @Override
            public void onError(String error) {
                // Обработка ошибки
            }
        });
    }
}