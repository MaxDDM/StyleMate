package com.example.stylemate.repository;

import java.util.Arrays;
import java.util.List;

public class FiltersRepository {

    public List<String> getTypes() {
        return Arrays.asList("джинсы", "рубашки", "шорты", "джемперы, свитеры и кардиганы", "брюки", "пиджаки и костюмы",
                "худи и свитшоты", "спортивные костюмы", "футболки, поло и майки", "верхняя одежда");
    }

    public List<String> getColors() {
        return Arrays.asList("серый", "белый", "голубой", "хаки", "красный", "зеленый", "желтый",
                "фиолетовый", "синий", "коричневый", "черный", "бежевый", "розовый", "бордовый", "мультиколор", "оранжевый");
    }

    public List<String> getSeasons() {
        return Arrays.asList("весна", "лето", "осень", "зима");
    }
}