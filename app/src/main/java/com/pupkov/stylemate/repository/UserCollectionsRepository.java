package com.pupkov.stylemate.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.pupkov.stylemate.analytics.AnalyticsManager;
import com.pupkov.stylemate.model.Outfit;
import com.pupkov.stylemate.ui.FavouriteOutfits;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;

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

    // интерфейс для возврата данных (списков)
    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String error);
    }

    // Интерфейс для загрузки одежды с привязкой к ID коллекции
    public interface CollectionDataCallback {
        void onDataLoaded(List<Outfit> outfits, String collectionId);
        void onError(String error);
    }

    // Получить названия коллекций текущего пользователя
    public void getCollectionNames(Context context, DataCallback<List<String>> callback) {
        String uid = "";

        if (repo.isLogged(context)) {
            uid = repo.getUID();

            dbRef.child("user_collections").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<String> names = new ArrayList<>();
                            for (DataSnapshot collection : snapshot.getChildren()) {
                                String name = collection.child("name").getValue(String.class);
                                if (name != null) names.add(name);
                            }
                            // Возвращаем список (если пустой — вернется пустой список с size=0)
                            callback.onDataLoaded(names);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        }
    }

    // Получить образы для конкретной коллекции с проверкой лайков и ID коллекции
    public void getOutfitsForCollection(String collectionName, Context context, CollectionDataCallback callback) {
        if (!repo.isLogged(context)) {
            // Логика для гостя: загружаем дефолтный стиль, лайков и ID коллекции нет
            String guestStyle = ActiveUserInfo.getDefaults("guest_style_name", context);
            String targetStyle = (guestStyle != null) ? guestStyle : "casual";
            loadOutfitsFromDbFiltered(targetStyle, null, null, null, (outfits) -> {
                callback.onDataLoaded(outfits, null);
            });

        } else {
            // Логика для авторизованного пользователя: ищем коллекцию по имени
            dbRef.child("user_collections").child(repo.getUID())
                    .orderByChild("name").equalTo(collectionName)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot match : snapshot.getChildren()) {
                                    // Извлекаем ID коллекции, параметры фильтрации и список лайков
                                    String collectionId = match.getKey();
                                    String style = match.child("style").getValue(String.class);
                                    String situation = match.child("situation").getValue(String.class);

                                    Map<String, Boolean> favorites = new HashMap<>();
                                    if (match.child("favorites").exists()) {
                                        for (DataSnapshot fav : match.child("favorites").getChildren()) {
                                            favorites.put(fav.getKey(), true);
                                        }
                                    }

                                    // ЧИТАЕМ ДИЗЛАЙКИ ИЗ Firebase
                                    Map<String, Boolean> dislikes = new HashMap<>();
                                    if (match.child("dislikes").exists()) {
                                        for (DataSnapshot dis : match.child("dislikes").getChildren()) {
                                            dislikes.put(dis.getKey(), true);
                                        }
                                    }

                                    // Передаем dislikes в метод фильтрации
                                    loadOutfitsFromDbFiltered(style, situation, favorites, dislikes, (outfits) -> {
                                        callback.onDataLoaded(outfits, collectionId);
                                    });
                                    return;
                                }
                            } else {
                                // Если коллекция не найдена — отдаем дефолтный casual
                                loadOutfitsFromDbFiltered("casual", null, null, null, (outfits) -> callback.onDataLoaded(outfits, null));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        }
    }

    // интерфейс для передачи отфильтрованного списка
    private interface InternalLoadCallback { void onLoad(List<Outfit> list); }

    // метод фильтрации по стилю и пересечению ситуаций
    private void loadOutfitsFromDbFiltered(String style, String collectionSituation, Map<String, Boolean> userFavorites, Map<String, Boolean> userDislikes, InternalLoadCallback callback) {
        Query query;

        // Выбираем стратегию выборки из Firebase
        if (style != null && !style.isEmpty()) {
            query = dbRef.child("outfits").orderByChild("style").equalTo(style);
        } else {
            query = dbRef.child("outfits");
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Outfit> filteredList = new ArrayList<>();

                // Парсим целевые ситуации коллекции (разделитель — запятая с пробелом)
                String[] targetSituations;
                if (collectionSituation != null && !collectionSituation.isEmpty()) {
                    targetSituations = collectionSituation.split(", ");
                } else {
                    targetSituations = new String[]{"any"};
                }

                for (DataSnapshot item : snapshot.getChildren()) {
                    Outfit outfit = item.getValue(Outfit.class);
                    if (outfit == null) continue;

                    String outfitId = item.getKey();
                    outfit.setId(outfitId);

                    // ПРОВЕРКА НА ДИЗЛАЙК: Если этот образ скрыт пользователем, сразу пропускаем его
                    if (userDislikes != null && userDislikes.containsKey(outfitId)) {
                        continue;
                    }

                    // Проверяем, лайкнута ли вещь пользователем
                    if (userFavorites != null && userFavorites.containsKey(outfitId)) {
                        outfit.setLiked(true);
                    } else {
                        outfit.setLiked(false);
                    }

                    // Парсим ситуации самой вещи
                    String situations = outfit.getFilter_situation();
                    String[] outfitSits;
                    if (situations == null) {
                        outfitSits = new String[]{"any"};
                    } else {
                        outfitSits = situations.split(", ");
                    }

                    boolean isSituationMatch = false;

                    // Алгоритм проверки пересечения списков ситуаций
                    for (String target : targetSituations) {
                        String cleanTarget = target.trim();

                        for (String outfitSit : outfitSits) {
                            if (outfitSit.equals("any") || cleanTarget.equals("any") || outfitSit.equals(cleanTarget)) {
                                isSituationMatch = true;
                                break;
                            }
                        }
                        if (isSituationMatch) {
                            break;
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

    // Получить все коллекции пользователя с первыми 4 картинками для экрана профиля
    public void getUserCollectionsWithPreviews(Context context, DataCallback<List<FavouriteOutfits>> callback) {
        if (!repo.isLogged(context)) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        // Кэшируем всю ветку outfits (ID -> imageUrl) для быстрого сопоставления
        dbRef.child("outfits").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot outfitsSnapshot) {
                Map<String, String> imagesMap = new HashMap<>();
                for (DataSnapshot item : outfitsSnapshot.getChildren()) {
                    String url = item.child("imageUrl").getValue(String.class);
                    imagesMap.put(item.getKey(), url);
                }

                // Переходим к сборке структуры коллекций
                loadUserCollectionsStructure(imagesMap, callback);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    // метод сборки превью-моделей на основе закэшированных картинок
    private void loadUserCollectionsStructure(Map<String, String> imagesMap, DataCallback<List<FavouriteOutfits>> callback) {
        dbRef.child("user_collections").child(repo.getUID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<FavouriteOutfits> resultList = new ArrayList<>();

                        for (DataSnapshot colSnapshot : snapshot.getChildren()) {
                            String colId = colSnapshot.getKey();
                            String colName = colSnapshot.child("name").getValue(String.class);
                            if (colName == null) colName = "Без названия";

                            List<String> previewUrls = new ArrayList<>();
                            DataSnapshot favs = colSnapshot.child("favorites");

                            // Отбираем максимум 4 картинки из лайкнутых вещей
                            for (DataSnapshot favItem : favs.getChildren()) {
                                if (previewUrls.size() >= 4) break;

                                String outfitId = favItem.getKey();
                                String url = imagesMap.get(outfitId);
                                if (url != null) {
                                    previewUrls.add(url);
                                }
                            }

                            // Безопасное извлечение урлов для конструктора
                            String p1 = previewUrls.size() > 0 ? previewUrls.get(0) : null;
                            String p2 = previewUrls.size() > 1 ? previewUrls.get(1) : null;
                            String p3 = previewUrls.size() > 2 ? previewUrls.get(2) : null;
                            String p4 = previewUrls.size() > 3 ? previewUrls.get(3) : null;

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

    // Переключение состояния лайка (добавление в favorites или удаление)
    public void toggleLikeInFirebase(Context context, String collectionId, String outfitId, boolean isLiked) {
        if (!repo.isLogged(context)) return;

        DatabaseReference favRef = dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("favorites")
                .child(outfitId);

        if (isLiked) {
            favRef.setValue(true);
            String uid = repo.getUID();
            AnalyticsManager.trackFirstLookAddition(uid);
            AnalyticsManager.trackOutfitFavorite(outfitId);
        } else {
            favRef.removeValue();
        }
    }

    // Загрузка всех лайкнутых вещей из конкретной коллекции в обход фильтров ситуации/стиля
    public void getCollectionFavorites(Context context, String collectionId, DataCallback<List<Outfit>> callback) {
        if (!repo.isLogged(context)) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        dbRef.child("user_collections").child(repo.getUID()).child(collectionId).child("favorites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onDataLoaded(new ArrayList<>());
                            return;
                        }

                        // Собираем массив ID из ключей структуры favorites
                        List<String> favoriteIds = new ArrayList<>();
                        for (DataSnapshot item : snapshot.getChildren()) {
                            favoriteIds.add(item.getKey());
                        }

                        // Загружаем полные объекты одежды по вытащенным ID
                        loadOutfitsByIds(favoriteIds, callback);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // метод пакетной загрузки объектов Outfit по списку их идентификаторов
    private void loadOutfitsByIds(List<String> ids, DataCallback<List<Outfit>> callback) {
        dbRef.child("outfits").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Outfit> resultList = new ArrayList<>();

                for (String id : ids) {
                    DataSnapshot itemSnapshot = snapshot.child(id);

                    if (itemSnapshot.exists()) {
                        Outfit outfit = itemSnapshot.getValue(Outfit.class);
                        if (outfit != null) {
                            outfit.setId(id);
                            outfit.setLiked(true); // Так как вещь из папки favorites, лайк гарантирован
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

    // метод получения чистого списка ID лайков без выгрузки самих вещей
    public void getLikedIdsOnly(Context context, String collectionId, DataCallback<List<String>> callback) {
        if (!repo.isLogged(context)) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("favorites")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> ids = new ArrayList<>();
                        for (DataSnapshot item : snapshot.getChildren()) {
                            ids.add(item.getKey());
                        }
                        callback.onDataLoaded(ids);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    /**
     * Метод получения чистого списка ID дизлайков (скрытых образов) без выгрузки самих вещей
     */
    public void getDislikedIdsOnly(Context context, String collectionId, DataCallback<List<String>> callback) {
        if (!repo.isLogged(context)) {
            callback.onDataLoaded(new ArrayList<>());
            return;
        }

        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("dislikes") // Стучимся в узел скрытых образов
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> ids = new ArrayList<>();
                        for (DataSnapshot item : snapshot.getChildren()) {
                            ids.add(item.getKey());
                        }
                        callback.onDataLoaded(ids);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // Переименование существующей коллекции пользователя
    public void renameCollection(Context context, String collectionId, String newName) {
        if (!repo.isLogged(context)) return;

        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("name")
                .setValue(newName);
    }

    // Полное удаление ветки коллекции из базы данных
    public void deleteCollection(Context context, String collectionId) {
        if (!repo.isLogged(context)) return;

        AnalyticsManager.trackCollectionDeletion(repo.getUID());
        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .removeValue();
    }

    public void dislikeOutfitInFirebase(Context context, String collectionId, String outfitId) {
        if (!repo.isLogged(context)) return;

        dbRef.child("user_collections")
                .child(repo.getUID())
                .child(collectionId)
                .child("dislikes")
                .child(outfitId)
                .setValue(true);
    }
}