package com.pupkov.stylemate.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pupkov.stylemate.repository.UserCollectionsRepository;

import java.util.ArrayList;
import java.util.List;

public class CollectionDetailViewModel extends AndroidViewModel {

    private final UserCollectionsRepository repository;

    // ID текущей коллекции (нужен для запросов в БД)
    private String collectionId;

    // 1. Заголовок (уже было)
    private final MutableLiveData<String> _title = new MutableLiveData<>();
    public LiveData<String> title = _title;

    // 2. Список одежды (НОВОЕ)
    private final MutableLiveData<List<Outfit>> _outfits = new MutableLiveData<>();
    public LiveData<List<Outfit>> outfits = _outfits;

    // События (уже было)
    private final MutableLiveData<Boolean> _closeScreenEvent = new MutableLiveData<>();
    public LiveData<Boolean> closeScreenEvent = _closeScreenEvent;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;

    public CollectionDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new UserCollectionsRepository();
    }

    // Инициализация данных (Вызываем из Activity в onCreate)
    public void init(String id, String initialTitle) {
        this.collectionId = id;

        // Устанавливаем заголовок, если еще не стоит
        if (_title.getValue() == null) {
            _title.setValue(initialTitle);
        }

        // Загружаем список одежды
        loadOutfits();
    }

    public void refresh() {
        // Просто переиспользуем твой метод загрузки
        loadOutfits();
    }

    private void loadOutfits() {
        if (collectionId == null) return;

        // Вызываем наш НОВЫЙ метод репозитория
        repository.getCollectionFavorites(getApplication(), collectionId, new UserCollectionsRepository.DataCallback<List<Outfit>>() {
            @Override
            public void onDataLoaded(List<Outfit> data) {
                _outfits.setValue(data);
                if (data == null || data.isEmpty()) {
                    // Можно добавить проверку, чтобы закрывать только если это не новая папка,
                    // но по твоей логике с удалением последнего элемента - закрываем.
                    _closeScreenEvent.setValue(true);
                }
            }

            @Override
            public void onError(String error) {
                _toastMessage.setValue("Ошибка загрузки: " + error);
            }
        });
    }

    // ЛОГИКА МГНОВЕННОГО УДАЛЕНИЯ (НОВОЕ)
    public void removeOutfit(String outfitId) {
        List<Outfit> currentList = _outfits.getValue();
        if (currentList == null) return;

        // 1. Создаем копию списка (важно для корректного обновления UI)
        List<Outfit> updatedList = new ArrayList<>(currentList);

        // 2. Ищем и удаляем элемент локально
        boolean removed = false;
        for (int i = 0; i < updatedList.size(); i++) {
            if (updatedList.get(i).getId().equals(outfitId)) {
                updatedList.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            // 3. Мгновенно обновляем LiveData -> Адаптер увидит новый список и удалит ячейку
            _outfits.setValue(updatedList);

            // 4. В фоне отправляем запрос в БД на удаление лайка
            // false означает "убрать лайк"
            repository.toggleLikeInFirebase(getApplication(), collectionId, outfitId, false);
            // === ДОБАВЛЕНО: Если удалили последний элемент -> закрываем папку ===
            if (updatedList.isEmpty()) {
                _closeScreenEvent.setValue(true);
            }
        }
    }

    // --- Старые методы (оставил как есть) ---

    public void onCollectionRenamed(String newName) {
        // 1. Обновляем UI мгновенно
        _title.setValue(newName);
        _toastMessage.setValue("Название изменено");

        // 2. Отправляем изменения в базу
        repository.renameCollection(getApplication(),collectionId, newName);
    }

    // ИЗМЕНЕННЫЙ МЕТОД: Удаление
    public void onCollectionDeleted() {
        // 1. Отправляем команду на удаление в базу
        repository.deleteCollection(getApplication(), collectionId);

        // 2. Показываем сообщение и закрываем экран
        _toastMessage.setValue("Подборка удалена");
        _closeScreenEvent.setValue(true);
    }
}