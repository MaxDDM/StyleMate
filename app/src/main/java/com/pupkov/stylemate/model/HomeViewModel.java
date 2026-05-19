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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ViewModel для управления бизнес-логикой главного экрана.
 * Обеспечивает кэширование, фильтрацию образов и синхронизацию данных с Firebase.
 */
public class HomeViewModel extends AndroidViewModel {

    private final UserRepository repo = new UserRepository();
    private final UserCollectionsRepository repository;
    private String currentCollectionId = null;

    // LiveData для инкапсуляции и передачи состояний в UI-слой
    private final MutableLiveData<List<String>> _collections = new MutableLiveData<>();
    public LiveData<List<String>> collections = _collections;

    private final MutableLiveData<Boolean> _isEmptyState = new MutableLiveData<>();
    public LiveData<Boolean> isEmptyState = _isEmptyState;

    private final MutableLiveData<String> _selectedName = new MutableLiveData<>();
    public LiveData<String> selectedName = _selectedName;

    /**
     * allOutfits — неизменяемый локальный кэш всей коллекции
     * outfits — отфильтрованный поток данных, на который подписан адаптер списка
     */
    private List<Outfit> allOutfits = new ArrayList<>();
    private final MutableLiveData<List<Outfit>> _outfits = new MutableLiveData<>();
    public LiveData<List<Outfit>> outfits = _outfits;

    private final MutableLiveData<Boolean> _filterEmptyEvent = new MutableLiveData<>();
    public LiveData<Boolean> filterEmptyEvent = _filterEmptyEvent;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new UserCollectionsRepository();
        loadCollectionsList();
    }

    public String getCurrentCollectionId() { return currentCollectionId; }

    /**
     * Загружает коллекции
     */
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
            // Гостевой режим: извлечение локального следа последней сессии выборки
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

    /**
     * Контролирует фокус на текущей коллекции при обновлении списков
     */
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
                _outfits.setValue(allOutfits); // Публикация исходного массива в UI
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getApplication(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Выполняет многокритериальную фильтрацию
     * логическое И между разнородными категориями (тип, цвет, сезон)
     * и логическое ИЛИ при множественном выборе тегов внутри одной категории.
     */
    public void applyFilters(FilterState state) {
        if (allOutfits.isEmpty()) return;

        // Если все фильтры сброшены, восстанавливаем отображение полной коллекции из кэша
        if (state.isEmpty()) {
            _outfits.setValue(allOutfits);
            return;
        }

        List<Outfit> tempResult = new ArrayList<>();

        for (Outfit outfit : allOutfits) {
            // Фильтрация по типу и цвету
            boolean typeMatch = checkMatch(state.getSelectedTypes(), outfit.getFilter_types());
            boolean colorMatch = checkMatch(state.getSelectedColors(), outfit.getFilter_colors());

            // Сравнение одиночного строкового значения сезона с выбранным множеством
            boolean seasonMatch = true;
            if (!state.getSelectedSeasons().isEmpty()) {
                if (outfit.getFilter_season() == null || !state.getSelectedSeasons().contains(outfit.getFilter_season())) {
                    seasonMatch = false;
                }
            }

            // Объединение результатов: объект проходит фильтр только при выполнении всех условий
            if (typeMatch && colorMatch && seasonMatch) {
                tempResult.add(outfit);
            }
        }

        // Если в результате фильтрации массив пуст, отправляем UI сигнал для вызова Toast
        if (tempResult.isEmpty()) {
            _filterEmptyEvent.setValue(true);
            _filterEmptyEvent.setValue(false);
        } else {
            _outfits.setValue(tempResult);
        }
    }

    /**
     * Проверяет пересечение выбранных фильтров с тегами образа
     */
    private boolean checkMatch(Set<String> selectedFilters, Map<String, Boolean> itemTags) {
        if (selectedFilters.isEmpty()) return true; // Если категория фильтра пуста, валидны все объекты
        if (itemTags == null) return false;

        for (String filter : selectedFilters) {
            for (String tagKey : itemTags.keySet()) {
                if (tagKey.equalsIgnoreCase(filter)) {
                    return true; // Найдено пересечение множеств внутри категории
                }
            }
        }
        return false;
    }

    /**
     * Инвертирует статус лайк. Сначала мгновенно обновляет UI, затем пишет в БД.
     */
    public void toggleLike(String outfitId) {
        if (currentCollectionId == null) return;

        boolean newState = false;
        List<Outfit> currentList = _outfits.getValue();

        if (currentList != null) {
            // Поиск и инверсия флага в текущем отображаемом списке на экране
            for (Outfit o : currentList) {
                if (o.getId().equals(outfitId)) {
                    newState = !o.isLiked();
                    o.setLiked(newState);
                    break;
                }
            }
            _outfits.setValue(currentList);

            // Синхронизация измененного флага с базовым кэшем allOutfits
            for (Outfit o : allOutfits) {
                if (o.getId().equals(outfitId)) {
                    o.setLiked(newState);
                    break;
                }
            }
        }
        // Асинхронная отправка обновления в Firebase
        repository.toggleLikeInFirebase(getApplication(), currentCollectionId, outfitId, newState);
    }

    /**
     * Легковесное обновление только идентификаторов лайков (без перезагрузки списка образов).
     */
    private void refreshLikesOnly() {
        if (currentCollectionId == null) return;

        repository.getLikedIdsOnly(getApplication(), currentCollectionId, new UserCollectionsRepository.DataCallback<List<String>>() {
            @Override
            public void onDataLoaded(List<String> likedIds) {
                if (allOutfits == null) return;

                boolean changed = false;
                for (Outfit outfit : allOutfits) {
                    boolean isLikedInDb = likedIds.contains(outfit.getId());
                    // Если статус на сервере изменился (например, через другой экран), обновляем локально
                    if (outfit.isLiked() != isLikedInDb) {
                        outfit.setLiked(isLikedInDb);
                        changed = true;
                    }
                }

                // Перерисовываем список на UI только если обнаружены реальные расхождения данных
                if (changed) {
                    _outfits.setValue(_outfits.getValue());
                }
            }

            @Override
            public void onError(String error) {}
        });
    }

    /**
     * Актуализирует состояние папок и лайков
     */
    public void refreshData() {
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
                    refreshLikesOnly(); // Папка существует -> запускаем точечную синхронизацию лайков
                } else {
                    onCollectionSelected(data.get(0)); // Папка удалена -> переключаемся на первую доступную
                }
            }

            @Override
            public void onError(String error) {}
        });
    }
}