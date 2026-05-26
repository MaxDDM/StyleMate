package com.pupkov.stylemate.repository;

import android.content.Context;
import java.util.Arrays;

public class StyleTestRepository {

    private static StyleTestRepository instance;
    private int[] scores = new int[6];

    private static final String KEY_SCORES = "test_scores_array";
    private static final String KEY_TIMESTAMP = "test_last_action_time";
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000;

    public static synchronized StyleTestRepository getInstance() {
        if (instance == null) {
            instance = new StyleTestRepository();
        }
        return instance;
    }

    private StyleTestRepository() {
        Arrays.fill(scores, 0);
    }

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

    public int calculateWinner() {
        int maxScore = -1;
        boolean allZero = true;

        for (int i = 1; i <= 5; i++) {
            if (scores[i] > 0) allZero = false;
            if (scores[i] > maxScore) {
                maxScore = scores[i];
            }
        }

        if (allZero) return 1;

        int[] priorityList = {3, 4, 2, 5, 1};

        for (int styleIndex : priorityList) {
            if (scores[styleIndex] == maxScore) {
                return styleIndex;
            }
        }

        return 1;
    }

    public String getStyleName(int index) {
        switch (index) {
            case 2: return "classic";
            case 3: return "grange";
            case 4: return "old_money";
            case 5: return "sport";
            default: return "casual";
        }
    }

    public void saveState(Context context) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
            sb.append(scores[i]);
            if (i < scores.length - 1) sb.append(",");
        }

        ActiveUserInfo.setDefaults(KEY_SCORES, sb.toString(), context);
        ActiveUserInfo.setDefaults(KEY_TIMESTAMP, String.valueOf(System.currentTimeMillis()), context);
    }

    public boolean restoreStateOrExpire(Context context) {
        String savedTimeStr = ActiveUserInfo.getDefaults(KEY_TIMESTAMP, context);

        if (savedTimeStr == null) {
            Arrays.fill(scores, 0);
            return true;
        }

        long savedTime = Long.parseLong(savedTimeStr);
        long currentTime = System.currentTimeMillis();

        if ((currentTime - savedTime) > SESSION_TIMEOUT_MS) {
            clearState(context);
            return false;
        }

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

    public void clearState(Context context) {
        Arrays.fill(scores, 0);
        ActiveUserInfo.setDefaults(KEY_SCORES, null, context);
        ActiveUserInfo.setDefaults(KEY_TIMESTAMP, null, context);
    }
}