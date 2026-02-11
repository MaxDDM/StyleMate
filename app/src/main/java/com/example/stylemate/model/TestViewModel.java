package com.example.stylemate.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.stylemate.repository.StyleTestRepository;

public class TestViewModel extends AndroidViewModel {

    private final StyleTestRepository repository;

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
    public void calculateResult() {
        int winner = repository.calculateWinner();
        repository.clearState(getApplication());

        // Мы кладем результат в LiveData.
        // Activity, которая "подписана" на это, увидит изменение и среагирует.
        winnerStyle.setValue(winner);
    }
}