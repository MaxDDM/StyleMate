package com.example.stylemate.repository;

import android.content.Context;
import androidx.annotation.NonNull;

import com.example.stylemate.model.Outfit;
import com.example.stylemate.ui.FavouriteOutfits; // Импорт нашей модели

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserCollectionsRepository {

    private final DatabaseReference dbRef;

    private final UserRepository repo = new UserRepository();

    public UserCollectionsRepository() {
        dbRef = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
    }

    // Интерфейс для возврата данных
    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String error);
    }

    // Интерфейс для возврата данных + ID коллекции
    public interface CollectionDataCallback {
        void onDataLoaded(List<Outfit> outfits, String collectionId);
        void onError(String error);
    }

    // 1. Получить названия (без изменений)
    public void getCollectionNames(DataCallback<List<String>> callback) {
        String uid = "";

        if (repo.isLogged()) {
            uid = repo.getUID();
        }

        dbRef.child("user_collections").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> names = new ArrayList<>();
                        for (DataSnapshot collection : snapshot.getChildren()) {
                            String name = collection.child("name").getValue(String.class);
                            if (name != null) names.add(name);
                        }

                        // УБРАЛИ ПРОВЕРКУ НА ПУСТОТУ!
                        // Теперь если список пуст, мы вернем ПУСТОЙ СПИСОК (size=0)
                        callback.onDataLoaded(names);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // 2. Получить одежду + Лайки + ID коллекции
    public void getOutfitsForCollection(String collectionName, Context context, CollectionDataCallback callback) {
        if (!repo.isLogged()) {
            // ГОСТЬ: Лайков нет
            String guestStyle = ActiveUserInfo.getDefaults("guest_style_name", context);
            String targetStyle = (guestStyle != null) ? guestStyle : "casual";
            loadOutfitsFromDbFiltered(targetStyle, null, null, (outfits) -> {
                callback.onDataLoaded(outfits, null); // ID коллекции нет
            });

        } else {
            // ЮЗЕР
            dbRef.child("user_collections").child(repo.getUID())
                    .orderByChild("name").equalTo(collectionName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot match : snapshot.getChildren()) {
                                    // 1. ПОЛУЧАЕМ ID КОЛЛЕКЦИИ!
                                    String collectionId = match.getKey();

                                    // 2. Параметры фильтрации
                                    String style = match.child("style").getValue(String.class);
                                    String situation = match.child("situation").getValue(String.class);

                                    // 3. СПИСОК ЛАЙКОВ (Map<OutfitID, Boolean>)
                                    Map<String, Boolean> favorites = new HashMap<>();
                                    if (match.child("favorites").exists()) {
                                        for (DataSnapshot fav : match.child("favorites").getChildren()) {
                                            favorites.put(fav.getKey(), true);
                                        }
                                    }

                                    // 4. Грузим одежду и передаем список лайков для проверки
                                    loadOutfitsFromDbFiltered(style != null ? style : "casual", situation, favorites, (outfits) -> {
                                        callback.onDataLoaded(outfits, collectionId);
                                    });
                                    return;
                                }
                            } else {
                                // Коллекция не найдена, грузим дефолт
                                loadOutfitsFromDbFiltered("casual", null, null, (outfits) -> callback.onDataLoaded(outfits, null));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        }
    }

    // Внутренний метод загрузки
    private interface InternalLoadCallback { void onLoad(List<Outfit> list); }

    private void loadOutfitsFromDbFiltered(String style, String collectionSituation, Map<String, Boolean> userFavorites, InternalLoadCallback callback) {
        dbRef.child("outfits")
                .orderByChild("style").equalTo(style)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Outfit> filteredList = new ArrayList<>();

                        // --- ЛОГИКА НАПАРНИКА (Адаптированная под твою структуру) ---
                        // 1. Разбиваем строку ситуаций из коллекции на массив (например: "work,party")
                        String[] targetSituations;
                        if (collectionSituation != null && !collectionSituation.isEmpty()) {
                            targetSituations = collectionSituation.split(",");
                        } else {
                            targetSituations = new String[]{"any"};
                        }

                        for (DataSnapshot item : snapshot.getChildren()) {
                            Outfit outfit = item.getValue(Outfit.class);
                            if (outfit == null) continue;

                            String outfitId = item.getKey();
                            outfit.setId(outfitId);

                            // --- ТВОЯ ЛОГИКА (Проверка лайков) ---
                            if (userFavorites != null && userFavorites.containsKey(outfitId)) {
                                outfit.setLiked(true);
                            } else {
                                outfit.setLiked(false);
                            }

                            // --- ЛОГИКА НАПАРНИКА (Фильтрация по списку ситуаций) ---
                            String outfitSit = outfit.getFilter_situation();
                            if (outfitSit == null) outfitSit = "any"; // Если у одежды нет ситуации, она универсальна

                            boolean isSituationMatch = false;

                            // Проверяем, совпадает ли ситуация одежды хотя бы с одной из ситуаций коллекции
                            for (String target : targetSituations) {
                                String cleanTarget = target.trim(); // Убираем лишние пробелы

                                // Если одежда подходит ко всему ("any")
                                // ИЛИ если одежда совпадает с одной из требуемых ситуаций
                                if (outfitSit.equals("any") || cleanTarget.equals("any") || outfitSit.equals(cleanTarget)) {
                                    isSituationMatch = true;
                                    break; // Нашли совпадение, дальше перебирать не нужно
                                }
                            }

                            if (isSituationMatch) {
                                filteredList.add(outfit);
                            }
                        }
                        callback.onLoad(filteredList);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    // =========================================================================
    // НОВЫЙ МЕТОД: Получить коллекции с превью (для Профиля)
    // =========================================================================
    public void getUserCollectionsWithPreviews(Context context, DataCallback<List<FavouriteOutfits>> callback) {
        // Если не залогинен - возвращаем пустой список
        if (!repo.isLogged()) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        // ШАГ 1: Загружаем ВСЮ одежду, чтобы знать картинки по ID
        // (В реальном большом проекте так не делают, но для курсовой/стартапа это ОК)
        dbRef.child("outfits").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot outfitsSnapshot) {
                // Сохраняем все образы в Map для быстрого поиска: ID -> ImageUrl
                Map<String, String> imagesMap = new HashMap<>();
                for (DataSnapshot item : outfitsSnapshot.getChildren()) {
                    String url = item.child("imageUrl").getValue(String.class);
                    imagesMap.put(item.getKey(), url);
                }

                // ШАГ 2: Теперь грузим коллекции пользователя
                loadUserCollectionsStructure(imagesMap, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    private void loadUserCollectionsStructure(Map<String, String> imagesMap, DataCallback<List<FavouriteOutfits>> callback) {
        dbRef.child("user_collections").child(repo.getUID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<FavouriteOutfits> resultList = new ArrayList<>();

                        for (DataSnapshot colSnapshot : snapshot.getChildren()) {
                            // Получаем ID и имя коллекции
                            String colId = colSnapshot.getKey();
                            String colName = colSnapshot.child("name").getValue(String.class);
                            if (colName == null) colName = "Без названия";

                            // Ищем ID лайкнутых вещей внутри favorites
                            List<String> previewUrls = new ArrayList<>();
                            DataSnapshot favs = colSnapshot.child("favorites");

                            // Берем первые 4 (или меньше) лайка
                            for (DataSnapshot favItem : favs.getChildren()) {
                                if (previewUrls.size() >= 4) break; // Нам нужно только 4 для превью

                                String outfitId = favItem.getKey();
                                // Достаем картинку из нашей Map, которую загрузили шагом ранее
                                String url = imagesMap.get(outfitId);
                                if (url != null) {
                                    previewUrls.add(url);
                                }
                            }

                            // Заполняем модель (если картинок меньше 4, передаем null)
                            String p1 = previewUrls.size() > 0 ? previewUrls.get(0) : null;
                            String p2 = previewUrls.size() > 1 ? previewUrls.get(1) : null;
                            String p3 = previewUrls.size() > 2 ? previewUrls.get(2) : null;
                            String p4 = previewUrls.size() > 3 ? previewUrls.get(3) : null;

                            // Добавляем в список
                            resultList.add(new FavouriteOutfits(colId, colName, p1, p2, p3, p4));
                        }

                        callback.onDataLoaded(resultList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // 3. Метод записи лайка в Firebase
    public void toggleLikeInFirebase(String collectionId, String outfitId, boolean isLiked) {
        if (!repo.isLogged()) return; // Гости не пишут в базу

        DatabaseReference favRef = dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("favorites")
                .child(outfitId);

        if (isLiked) {
            favRef.setValue(true); // Ставим лайк
        } else {
            favRef.removeValue(); // Удаляем лайк
        }
    }

    // =========================================================================
    // НОВЫЙ МЕТОД: Загрузка лайков конкретной коллекции (Игнорируя фильтры)
    // =========================================================================
    public void getCollectionFavorites(String collectionId, DataCallback<List<Outfit>> callback) {
        // Гости не имеют БД, возвращаем пустоту
        if (!repo.isLogged()) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        // 1. Идем в папку: user_collections -> email -> collectionId -> favorites
        dbRef.child("user_collections").child(repo.getUID()).child(collectionId).child("favorites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Если папки favorites нет или она пустая
                        if (!snapshot.exists()) {
                            callback.onDataLoaded(new ArrayList<>());
                            return;
                        }

                        // 2. Собираем список ID (ключи)
                        List<String> favoriteIds = new ArrayList<>();
                        for (DataSnapshot item : snapshot.getChildren()) {
                            favoriteIds.add(item.getKey());
                        }

                        // 3. Грузим сами данные по этим ID
                        loadOutfitsByIds(favoriteIds, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // Вспомогательный метод: загружает одежду по списку ID
    private void loadOutfitsByIds(List<String> ids, DataCallback<List<Outfit>> callback) {
        // ОПТИМИЗАЦИЯ: Чтобы не делать 50 запросов, загрузим ветку outfits один раз.
        // Для курсовой/MVP это нормально. Для продакшена с 1млн товаров тут нужен другой подход (Query).
        dbRef.child("outfits").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Outfit> resultList = new ArrayList<>();

                // Бежим по списку наших ID из избранного
                for (String id : ids) {
                    // Ищем этот ID в общем списке одежды
                    DataSnapshot itemSnapshot = snapshot.child(id);

                    if (itemSnapshot.exists()) {
                        Outfit outfit = itemSnapshot.getValue(Outfit.class);
                        if (outfit != null) {
                            outfit.setId(id);
                            // ВАЖНО: Раз мы в папке избранного, значит лайк точно стоит
                            outfit.setLiked(true);
                            resultList.add(outfit);
                        }
                    }
                }
                callback.onDataLoaded(resultList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // =========================================================================
    // ОПТИМИЗАЦИЯ: Получить ТОЛЬКО список ID лайков (без загрузки самой одежды)
    // =========================================================================
    public void getLikedIdsOnly(String collectionId, DataCallback<List<String>> callback) {
        // Если гость - возвращаем пустой список
        if (!repo.isLogged()) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        // Запрашиваем только папку favorites
        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("favorites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> ids = new ArrayList<>();
                        // Собираем только ключи (ID)
                        for (DataSnapshot item : snapshot.getChildren()) {
                            ids.add(item.getKey());
                        }
                        // Возвращаем список строк ["id1", "id2"], а не тяжелые объекты
                        callback.onDataLoaded(ids);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // 1. Переименование коллекции
    public void renameCollection(String collectionId, String newName) {
        if (!repo.isLogged()) return;

        // Заходим в user_collections -> email -> id -> name и ставим новое значение
        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("name")
                .setValue(newName);
    }

    // 2. Полное удаление коллекции
    public void deleteCollection(String collectionId) {
        if (!repo.isLogged()) return;

        // Заходим в user_collections -> email -> id и удаляем ВСЮ ветку (вместе с лайками внутри)
        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .removeValue();
    }
}