package com.example.stylemate.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class FilterState implements Serializable {
    private Set<String> selectedTypes;
    private Set<String> selectedColors;
    private Set<String> selectedSeasons;

    // Конструктор
    public FilterState(Set<String> types, Set<String> colors, Set<String> seasons) {
        this.selectedTypes = new HashSet<>(types);     // Делаем копии, чтобы разорвать связь
        this.selectedColors = new HashSet<>(colors);
        this.selectedSeasons = new HashSet<>(seasons);
    }

    // Геттеры
    public Set<String> getSelectedTypes() { return selectedTypes; }
    public Set<String> getSelectedColors() { return selectedColors; }
    public Set<String> getSelectedSeasons() { return selectedSeasons; }

    // Проверка: пусты ли фильтры вообще?
    public boolean isEmpty() {
        return selectedTypes.isEmpty() && selectedColors.isEmpty() && selectedSeasons.isEmpty();
    }
}