package com.pupkov.stylemate;

import com.pupkov.stylemate.analytics.AnalyticsManager;

import org.junit.Test;

public class AnalyticsTest {
    @Test
    public void runAnalytics() {
        // Вызываем расчеты
        AnalyticsManager.calculateAndSaveActivationRate();
        AnalyticsManager.calculateAverageTimeToFirstLike();
        AnalyticsManager.calculateAverageLooksPerUser();
        System.out.println("Analytics update triggered!");
    }
}
