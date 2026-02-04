package com.example.stylemate.repository;

import java.util.Arrays;
import java.util.List;

public class FiltersRepository {

    public List<String> getTypes() {
        return Arrays.asList("бриджи", "футболки", "джинсы", "пиджаки", "брюки", "шорты",
                "жилетки", "поло", "майки", "галстуки", "костюмы", "лонгсливы");
    }

    public List<String> getColors() {
        return Arrays.asList("серый", "белый", "голубой", "красный", "зеленый", "желтый",
                "фиолетовый", "синий", "коричневый", "черный", "бежевый", "розовый");
    }

    public List<String> getSeasons() {
        return Arrays.asList("весна", "лето", "осень", "зима");
    }
}