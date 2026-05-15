package com.pupkov.stylemate;

import com.pupkov.stylemate.analytics.AnalyticsManager;

import org.junit.Test;

public class AnalyticsTest {
    @Test
    public void runAnalytics() {
        // Вызываем расчеты
        AnalyticsManager.calculateAndSaveActivationRate();
        AnalyticsManager.calculateAverageTimeToFirstLike();
        AnalyticsManager.calculateAverageLikesPerUser();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
