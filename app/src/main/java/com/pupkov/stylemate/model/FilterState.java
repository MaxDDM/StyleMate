package com.pupkov.stylemate.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Неизменяемый контейнер состояния фильтров.
 * Используется для безопасной передачи выбранных тегов между экранами приложения
 */
public class FilterState implements Serializable {
    private final Set<String> selectedTypes;
    private final Set<String> selectedColors;
    private final Set<String> selectedSeasons;

    /**
     * Создает снимок состояния фильтров.
     */
    public FilterState(Set<String> types, Set<String> colors, Set<String> seasons) {
        this.selectedTypes = types != null ? new HashSet<>(types) : new HashSet<>();
        this.selectedColors = colors != null ? new HashSet<>(colors) : new HashSet<>();
        this.selectedSeasons = seasons != null ? new HashSet<>(seasons) : new HashSet<>();
    }

    public Set<String> getSelectedTypes() { return selectedTypes; }
    public Set<String> getSelectedColors() { return selectedColors; }
    public Set<String> getSelectedSeasons() { return selectedSeasons; }

    /**
     * Проверка на наличие активных фильтров.
     * Используется для скрытия или показа индикатора фильтрации на главном экране.
     */
    public boolean isEmpty() {
        return selectedTypes.isEmpty() && selectedColors.isEmpty() && selectedSeasons.isEmpty();
    }
}