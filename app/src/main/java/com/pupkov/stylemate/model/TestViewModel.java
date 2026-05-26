package com.pupkov.stylemate.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.pupkov.stylemate.analytics.AnalyticsManager;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.StyleTestRepository;
import com.pupkov.stylemate.repository.UserRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestViewModel extends AndroidViewModel {

    private final StyleTestRepository repository;
    private final UserRepository repo = new UserRepository();

    // LiveData для передачи во View состояния сессии и индекса итогового стиля
    private final MutableLiveData<Boolean> isSessionValid = new MutableLiveData<>();
    private final MutableLiveData<Integer> winnerStyle = new MutableLiveData<>();

    public TestViewModel(@NonNull Application application) {
        super(application);
        repository = StyleTestRepository.getInstance();
    }

    public LiveData<Boolean> getSessionValidState() {
        return isSessionValid;
    }

    public LiveData<Integer> getWinnerStyle() {
        return winnerStyle;
    }

    // Проверка валидности текущей сессии
    public void checkSession() {
        boolean valid = repository.restoreStateOrExpire(getApplication());
        isSessionValid.setValue(valid);
    }

    // Распределение ответов по номерам вопросов и сохранение промежуточного состояния
    public void processAnswer(int questionNumber, int answerId) {
        switch (questionNumber) {
            case 1: repository.processQuestion1(answerId); break;
            case 2: repository.processQuestion2(answerId); break;
            case 3: repository.processQuestion3(answerId); break;
            case 4: repository.processQuestion4(answerId); break;
            case 5: repository.processQuestion5(answerId); break;
        }
        repository.saveState(getApplication());
    }

    // Сохранение текущего прогресса без изменения очков (при пропуске вопроса)
    public void saveProgressOnly() {
        repository.saveState(getApplication());
    }

    // Подсчет результатов и определение способа сохранения (бд или локально)
    public void calculateResult(String selectionName) {
        int winnerIndex = repository.calculateWinner();
        String styleName = repository.getStyleName(winnerIndex);

        if (repo.isLogged(getApplication())) {
            saveToFirebase(selectionName, styleName, winnerIndex);
        } else {
            saveToLocal(selectionName, styleName, winnerIndex);
        }
    }

    // Сохранение подборки по стилю в Firebase Realtime Database для авторизованных пользователей
    private void saveToFirebase(String selectionName, String styleName, int winnerIndex) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        // Формируем уникальный узел подборки внутри user_collections -> UID пользователя
        DatabaseReference newCollectionRef = dbRef
                .child("user_collections")
                .child(repo.getUID())
                .push();

        Map<String, Object> collectionData = new HashMap<>();
        collectionData.put("name", selectionName);
        collectionData.put("style", styleName);

        newCollectionRef.setValue(collectionData)
                .addOnSuccessListener(aVoid -> {
                    repository.clearState(getApplication());
                    AnalyticsManager.trackCollectionCountChange(repo.getUID());
                    ActiveUserInfo.setDefaults("is_guest", "false", getApplication());
                    winnerStyle.setValue(winnerIndex); // Оповещаем View об успешном завершении
                })
                .addOnFailureListener(e -> {
                    // При ошибке сети также пропускаем пользователя к результату
                    repository.clearState(getApplication());
                    winnerStyle.setValue(winnerIndex);
                });
    }

    // Создание и отправка в Firebase подборки на основе выбранных ситуаций
    public void createSituationCollection(String name, ArrayList<String> situations) {
        String uid = repo.getUID();
        if (uid == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        DatabaseReference newCollectionRef = dbRef.child("user_collections").child(uid).push();

        Map<String, Object> collectionData = new HashMap<>();
        collectionData.put("name", name);
        collectionData.put("situation", situationsToString(situations));

        newCollectionRef.setValue(collectionData);
        AnalyticsManager.trackCollectionCountChange(repo.getUID());
    }

    // Локальное сохранение результатов теста в SharedPreferences для неавторизованных (гостей)
    private void saveToLocal(String selectionName, String styleName, int winnerIndex) {
        ActiveUserInfo.setDefaults("guest_selection_name", selectionName, getApplication());
        ActiveUserInfo.setDefaults("guest_style_name", styleName, getApplication());

        repository.clearState(getApplication());
        winnerStyle.setValue(winnerIndex);
    }

    // Сохранение выбранных ситуаций в локальные настройки
    public void saveSituation(ArrayList<String> situations) {
        String situation = situationsToString(situations);
        ActiveUserInfo.setDefaults(situation, situation, getApplication());
    }

    // Преобразование списка ситуаций в строку через запятую для записи в базу данных
    private String situationsToString(ArrayList<String> situations) {
        StringBuilder situation = new StringBuilder();
        for (int i = 0; i < situations.size(); ++i) {
            if (i != situations.size() - 1) {
                situation.append(situations.get(i)).append(",");
            } else {
                situation.append(situations.get(i));
            }
        }
        return situation.toString();
    }
}