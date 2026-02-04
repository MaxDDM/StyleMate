package com.example.stylemate.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.stylemate.repository.UserCollectionsRepository;

public class CollectionDetailViewModel extends ViewModel {

    private final UserCollectionsRepository repository = new UserCollectionsRepository();

    // 1. Текущий заголовок (Activity следит за ним)
    private final MutableLiveData<String> _title = new MutableLiveData<>();
    public LiveData<String> title = _title;

    // 2. Событие: "Закрыть экран" (после удаления)
    private final MutableLiveData<Boolean> _closeScreenEvent = new MutableLiveData<>();
    public LiveData<Boolean> closeScreenEvent = _closeScreenEvent;

    // 3. Событие: "Показать тост"
    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> toastMessage = _toastMessage;


    // Метод инициализации (вызываем при старте Activity)
    public void setInitialTitle(String title) {
        if (_title.getValue() == null) { // Чтобы не сбрасывать при повороте экрана
            _title.setValue(title);
        }
    }

    // Логика: Пользователь переименовал подборку
    public void onCollectionRenamed(String newName) {
        String oldName = _title.getValue();

        // 1. Сообщаем репозиторию
        repository.renameCollection(oldName, newName);

        // 2. Обновляем LiveData (Activity увидит это и поменяет текст)
        _title.setValue(newName);

        // 3. Просим показать уведомление
        _toastMessage.setValue("Название изменено");
    }

    // Логика: Пользователь удалил подборку
    public void onCollectionDeleted() {
        String currentName = _title.getValue();

        // 1. Удаляем в базе
        repository.deleteCollection(currentName);

        // 2. Показываем тост
        _toastMessage.setValue("Подборка удалена");

        // 3. Даем сигнал закрыть экран
        _closeScreenEvent.setValue(true);
    }
}