package com.pupkov.stylemate.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class FilterState implements Serializable {
    private final Set<String> selectedTypes;
    private final Set<String> selectedColors;
    private final Set<String> selectedSeasons;

    public FilterState(Set<String> types, Set<String> colors, Set<String> seasons) {
        this.selectedTypes = types != null ? new HashSet<>(types) : new HashSet<>();
        this.selectedColors = colors != null ? new HashSet<>(colors) : new HashSet<>();
        this.selectedSeasons = seasons != null ? new HashSet<>(seasons) : new HashSet<>();
    }

    public Set<String> getSelectedTypes() { return selectedTypes; }
    public Set<String> getSelectedColors() { return selectedColors; }
    public Set<String> getSelectedSeasons() { return selectedSeasons; }

    public boolean isEmpty() {
        return selectedTypes.isEmpty() && selectedColors.isEmpty() && selectedSeasons.isEmpty();
    }
}