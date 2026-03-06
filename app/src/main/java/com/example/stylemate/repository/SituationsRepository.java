package com.example.stylemate.repository;

import java.util.ArrayList;
import java.util.List;

public class SituationsRepository {
    public static ArrayList<String> getSituations(int num) {
        switch(num) {
            case 1:
                return new ArrayList<>(List.of("работа_строгий"));
            case 2:
                return new ArrayList<>(List.of("работа_нестр"));
            case 3:
                return new ArrayList<>(List.of("работа_строгий", "работа_нестр"));
            case 4:
                return new ArrayList<>(List.of("отдых_море"));
            case 5:
                return new ArrayList<>(List.of("отдых_акт"));
            case 6:
                return new ArrayList<>(List.of("отдых_город"));
            case 7:
                return new ArrayList<>(List.of("отдых_море", "отдых_акт", "отдых_город"));
            case 8:
                return new ArrayList<>(List.of("вечеринка_офиц"));
            case 9:
                return new ArrayList<>(List.of("вечеринка_неофиц"));
            case 10:
                return new ArrayList<>(List.of("тренировка_зал"));
            case 11:
                return new ArrayList<>(List.of("тренировка_ул"));
            case 12:
                return new ArrayList<>(List.of("тренировка_зал, тренировка_ул"));
            default:
                return new ArrayList<>();
        }
    }
}