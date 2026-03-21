package com.example.stylemate.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.repository.StyleTestRepository;
import com.example.stylemate.repository.UserRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestViewModel extends AndroidViewModel {

    private final StyleTestRepository repository;

    private final UserRepository repo = new UserRepository();

    // 1. STATE (Состояние идет СНИЗУ ВВЕРХ)
    // LiveData для статуса сессии
    private final MutableLiveData<Boolean> isSessionValid = new MutableLiveData<>();
    // LiveData для результата победителя (Integer)
    private final MutableLiveData<Integer> winnerStyle = new MutableLiveData<>();

    public TestViewModel(@NonNull Application application) {
        super(application);
        repository = StyleTestRepository.getInstance();
    }

    // --- Getters для наблюдения (чтобы Activity могла подписаться) ---
    public LiveData<Boolean> getSessionValidState() {
        return isSessionValid;
    }

    public LiveData<Integer> getWinnerStyle() {
        return winnerStyle;
    }


    // 2. EVENTS (Действия идут СВЕРХУ ВНИЗ)

    public void checkSession() {
        boolean valid = repository.restoreStateOrExpire(getApplication());
        // Отправляем данные наверх (во View)
        isSessionValid.setValue(valid);
    }

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

    public void saveProgressOnly() {
        repository.saveState(getApplication());
    }

    // Activity просто говорит "Посчитай!", но не требует ответа мгновенно
    // --- ФИНАЛИЗАЦИЯ ТЕСТА ---
    public void calculateResult(String selectionName) {
        int winnerIndex = repository.calculateWinner();
        String styleName = repository.getStyleName(winnerIndex); // "casual"

        if (repo.isLogged()) {
            saveToFirebase(selectionName, styleName, winnerIndex);
        } else {
            saveToLocal(styleName, selectionName, winnerIndex);
        }
    }

    private void saveToFirebase(String selectionName, String styleName, int winnerIndex) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        // user_collections -> ivan@mail|ru -> PUSH_ID
        DatabaseReference newCollectionRef = dbRef
                .child("user_collections")
                .child(repo.getUID())
                .push();

        // СТРОГО ПО ТВОЕЙ СТРУКТУРЕ
        Map<String, Object> collectionData = new HashMap<>();
        collectionData.put("name", selectionName);
        collectionData.put("style", styleName);
        // Поля "season", "situation" и "favorites" не добавляем, они необязательные/пустые

        newCollectionRef.setValue(collectionData)
                .addOnSuccessListener(aVoid -> {
                    // Успех
                    repository.clearState(getApplication());
                    // Ставим флаг, что это не гость (на всякий случай для UI)
                    ActiveUserInfo.setDefaults("is_guest", "false", getApplication());
                    // Переходим
                    winnerStyle.setValue(winnerIndex);
                })
                .addOnFailureListener(e -> {
                    // Ошибка сети - все равно пускаем, но данные в облако не ушли
                    repository.clearState(getApplication());
                    winnerStyle.setValue(winnerIndex);
                });
    }

    public void createSituationCollection(String name, ArrayList<String> situations) {
        String uid = repo.getUID();
        if (uid == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        // 1. Создаем уникальный ID
        DatabaseReference newCollectionRef = dbRef.child("user_collections").child(uid).push();

        Map<String, Object> collectionData = new HashMap<>();
        collectionData.put("name", name);
        collectionData.put("situation", situationsToString(situations));
        // Стиль намеренно не пишем (он будет null), чтобы сработал поиск по ситуации

        newCollectionRef.setValue(collectionData);
    }

    private void saveToLocal(String selectionName, String styleName, int winnerIndex) {
        // Для гостя сохраняем локально, чтобы HomeFragment знал, что показывать
        ActiveUserInfo.setDefaults("is_guest", "true", getApplication());
        ActiveUserInfo.setDefaults("guest_selection_name", "true", getApplication());
        ActiveUserInfo.setDefaults("guest_style_name", styleName, getApplication());

        repository.clearState(getApplication());
        winnerStyle.setValue(winnerIndex);
    }

    public void saveSituation(ArrayList<String> situations) {
        String situation = situationsToString(situations);
        ActiveUserInfo.setDefaults(situation, situation, getApplication());
    }

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