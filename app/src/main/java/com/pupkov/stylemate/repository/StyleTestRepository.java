package com.pupkov.stylemate.repository;

import android.content.Context;
import java.util.Arrays;

public class StyleTestRepository {

    private static StyleTestRepository instance;
    // Массив для подсчета баллов по стилям. Индексы 1-5 соответствуют стилям одежды.
    private int[] scores = new int[6];

    private static final String KEY_SCORES = "test_scores_array";
    private static final String KEY_TIMESTAMP = "test_last_action_time";
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // Таймаут сессии (30 минут)

    // Реализация паттерна Singleton для управления единым состоянием теста
    public static synchronized StyleTestRepository getInstance() {
        if (instance == null) {
            instance = new StyleTestRepository();
        }
        return instance;
    }

    private StyleTestRepository() {
        Arrays.fill(scores, 0);
    }

    // Логика начисления баллов по ответам

    public void processQuestion1(int answerId) {
        switch (answerId) {
            case 1: addPoints(2, 3); break;
            case 2: addPoints(1, 5); break;
            case 3: addPoints(1, 2, 4); break;
            case 4: addPoints(1, 4, 5); break;
        }
    }

    public void processQuestion2(int answerId) {
        switch (answerId) {
            case 1: addPoints(1, 3, 5); break;
            case 2: addPoints(2, 4); break;
            case 3: addPoints(1, 2, 4); break;
        }
    }

    public void processQuestion3(int answerId) {
        switch (answerId) {
            case 1: addPoints(1, 3); break;
            case 2: addPoints(1, 3, 5); break;
            case 3: addPoints(1, 2, 4); break;
        }
    }

    public void processQuestion4(int answerId) {
        switch (answerId) {
            case 1: addPoints(1, 2, 4); break;
            case 2: addPoints(1, 3, 5); break;
            case 3: addPoints(1, 3); break;
        }
    }

    public void processQuestion5(int answerId) {
        if (answerId >= 1 && answerId <= 5) {
            scores[answerId]++;
        }
    }

    private void addPoints(int... styles) {
        for (int style : styles) {
            if (style >= 1 && style <= 5) {
                scores[style]++;
            }
        }
    }

    // Подсчет результатов

    public int calculateWinner() {
        int maxScore = -1;
        boolean allZero = true;

        // Определяем максимальный балл среди всех стилей
        for (int i = 1; i <= 5; i++) {
            if (scores[i] > 0) allZero = false;
            if (scores[i] > maxScore) {
                maxScore = scores[i];
            }
        }

        if (allZero) return 1;

        // Разрешение конфликтов: если у стилей одинаковый балл, выбираем по жесткому приоритету
        int[] priorityList = {3, 4, 2, 5, 1};

        for (int styleIndex : priorityList) {
            if (scores[styleIndex] == maxScore) {
                return styleIndex;
            }
        }

        return 1;
    }

    // Сопоставление индекса стиля с его строковым идентификатором для Firebase
    public String getStyleName(int index) {
        switch (index) {
            case 2: return "classic";
            case 3: return "grange";
            case 4: return "old_money";
            case 5: return "sport";
            default: return "casual";
        }
    }

    // Управление состоянием сессии

    // Сериализация текущих баллов в строку через запятую и сохранение времени активности
    public void saveState(Context context) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
            sb.append(scores[i]);
            if (i < scores.length - 1) sb.append(",");
        }

        ActiveUserInfo.setDefaults(KEY_SCORES, sb.toString(), context);
        ActiveUserInfo.setDefaults(KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()), context);
    }

    // Проверка времени жизни сессии и десериализация сохраненных баллов
    public boolean restoreStateOrExpire(Context context) {
        String savedTimeStr = ActiveUserInfo.getDefaults(KEY_TIMESTAMP, context);

        if (savedTimeStr == null) {
            Arrays.fill(scores, 0);
            return true;
        }

        long savedTime = Long.parseLong(savedTimeStr);
        long currentTime = System.currentTimeMillis();

        // Проверяем, не превысил ли перерыв между вопросами 30 минут
        if ((currentTime - savedTime) > SESSION_TIMEOUT_MS) {
            clearState(context);
            return false; // Сессия прервалась
        }

        // Восстановление массива баллов из строки
        String savedScoresStr = ActiveUserInfo.getDefaults(KEY_SCORES, context);
        if (savedScoresStr != null) {
            String[] split = savedScoresStr.split(",");
            for (int i = 0; i < split.length && i < scores.length; i++) {
                try {
                    scores[i] = Integer.parseInt(split[i]);
                } catch (NumberFormatException e) {
                    scores[i] = 0;
                }
            }
        }
        return true;
    }

    // Полный сброс прогресса тестирования в памяти и локальных настройках
    public void clearState(Context context) {
        Arrays.fill(scores, 0);
        ActiveUserInfo.setDefaults(KEY_SCORES, null, context);
        ActiveUserInfo.setDefaults(KEY_TIMESTAMP, null, context);
    }
}