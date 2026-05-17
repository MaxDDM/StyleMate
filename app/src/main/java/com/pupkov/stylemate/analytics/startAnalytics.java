package com.pupkov.stylemate.analytics;

public class startAnalytics {
    public static void startAnalytics() {
        CR.setCRforAllOutfits();
        CTR.setCTRforAllOutfits();
        TestCompleteCount.setTestCompleteCount();
        TimeAnalytics.countRR();
        TimeAnalytics.countAU(1);
        TimeAnalytics.countAU(7);
        TimeAnalytics.countAU(30);
    }
}
