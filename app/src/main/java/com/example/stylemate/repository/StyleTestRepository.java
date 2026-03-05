package com.example.stylemate.repository;

import android.content.Context;
import android.text.TextUtils;

import java.util.Arrays;

public class StyleTestRepository {

    private static StyleTestRepository instance;
    // Индексы 1-5 соответствуют стилям. 0 не используем.
    private int[] scores = new int[6];

    // Ключевые константы для сохранения
    private static final String KEY_SCORES = "test_scores_array";
    private static final String KEY_TIMESTAMP = "test_last_action_time";
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 минут

    // Singleton pattern
    public static synchronized StyleTestRepository getInstance() {
        if (instance == null) {
            instance = new StyleTestRepository();
        }
        return instance;
    }

    private StyleTestRepository() {
        // При создании обнуляем баллы
        Arrays.fill(scores, 0);
    }

    // -----------------------------------------------------
    // ЛОГИКА НАЧИСЛЕНИЯ БАЛЛОВ (ПО ВОПРОСАМ)
    // -----------------------------------------------------

    // Вопрос 1: Цвета
    public void processQuestion1(int answerId) {
        switch (answerId) {
            case 1: // темные
                addPoints(2, 3);
                break;
            case 2: // яркие
                addPoints(1, 5);
                break;
            case 3: // нейтральные
                addPoints(1, 2, 4);
                break;
            case 4: // земляные
                addPoints(1, 4, 5);
                break;
            // case 5 "цвет не важен" - ничего не делаем
        }
    }

    // Вопрос 2: Тип одежды
    public void processQuestion2(int answerId) {
        switch (answerId) {
            case 1: // худи
                addPoints(1, 3, 5);
                break;
            case 2: // пиджаки
                addPoints(2, 4);
                break;
            case 3: // рубашки
                addPoints(1, 2, 4);
                break;
            // case 4 "ношу разное" - ничего не делаем
        }
    }

    // Вопрос 3: Низ
    public void processQuestion3(int answerId) {
        switch (answerId) {
            case 1: // джинсы
                addPoints(1, 3);
                break;
            case 2: // спортивные
                addPoints(1, 3, 5);
                break;
            case 3: // брюки
                addPoints(1, 2, 4);
                break;
            // case 4 "разное" - ничего не делаем
        }
    }

    // Вопрос 4: Верхняя одежда
    public void processQuestion4(int answerId) {
        switch (answerId) {
            case 1: // пальто
                addPoints(1, 2, 4);
                break;
            case 2: // бомберы
                addPoints(1, 3, 5);
                break;
            case 3: // дубленки
                addPoints(1, 3);
                break;
            // case 4 "разное" - ничего не делаем
        }
    }

    // Вопрос 5: Идеальный стиль (Прямой выбор)
    public void processQuestion5(int answerId) {
        if (answerId >= 1 && answerId <= 5) {
            // Начисляем конкретному стилю (можно дать +2 балла за прямой выбор, но пока дам +1)
            scores[answerId]++;
        }
        // "Не определился" - ничего не делаем
    }

    // Вспомогательный метод добавления баллов
    private void addPoints(int... styles) {
        for (int style : styles) {
            if (style >= 1 && style <= 5) {
                scores[style]++;
            }
        }
    }

    // -----------------------------------------------------
    // ПОДСЧЕТ ПОБЕДИТЕЛЯ
    // -----------------------------------------------------

    public int calculateWinner() {
        int maxScore = -1;
        boolean allZero = true;

        // 1. Ищем максимальный балл и проверяем нули
        for (int i = 1; i <= 5; i++) {
            if (scores[i] > 0) allZero = false;
            if (scores[i] > maxScore) {
                maxScore = scores[i];
            }
        }

        // 2. Если все по нулям -> Стиль 1
        if (allZero) return 1;

        // 3. Проверяем по приоритету: 3 > 4 > 2 > 5 > 1
        int[] priorityList = {3, 4, 2, 5, 1};

        for (int styleIndex : priorityList) {
            if (scores[styleIndex] == maxScore) {
                return styleIndex; // Нашли победителя
            }
        }

        return 1; // Заглушка
    }

    // Перевод числа в строку для БД
    public String getStyleName(int index) {
        switch (index) {
            case 2: return "classic";
            case 3: return "grunge";
            case 4: return "old_money";
            case 5: return "sport";
            default: return "casual";
        }
    }

    // -----------------------------------------------------
    // СОХРАНЕНИЕ И СЕССИЯ (30 МИНУТ)
    // -----------------------------------------------------

    // Метод вызывает Activity при нажатии "Далее" или "Skip"
    public void saveState(Context context) {
        // Превращаем массив в строку "1,0,2,4,0,1"
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
            sb.append(scores[i]);
            if (i < scores.length - 1) sb.append(",");
        }

        // Сохраняем строку баллов
        ActiveUserInfo.setDefaults(KEY_SCORES, sb.toString(), context);
        // Сохраняем ТЕКУЩЕЕ ВРЕМЯ (таймер сброшен)
        ActiveUserInfo.setDefaults(KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()), context);
    }

    // Метод вызывает Activity в onCreate для проверки
    // Возвращает TRUE если сессия жива (данные восстановлены)
    // Возвращает FALSE если сессия протухла (>30 мин)
    public boolean restoreStateOrExpire(Context context) {
        String savedTimeStr = ActiveUserInfo.getDefaults(KEY_TIMESTAMP, context);

        if (savedTimeStr == null) {
            // Данных нет, новая сессия
            Arrays.fill(scores, 0);
            return true;
        }

        long savedTime = Long.parseLong(savedTimeStr);
        long currentTime = System.currentTimeMillis();

        // Проверка: прошло ли больше 30 минут?
        if ((currentTime - savedTime) > SESSION_TIMEOUT_MS) {
            // Сессия истекла!
            clearState(context);
            return false;
        }

        // Сессия жива, восстанавливаем баллы
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

    // Полная очистка (при выходе или истечении времени)
    public void clearState(Context context) {
        Arrays.fill(scores, 0);
        ActiveUserInfo.setDefaults(KEY_SCORES, null, context);
        ActiveUserInfo.setDefaults(KEY_TIMESTAMP, null, context);
    }
}