package com.pupkov.stylemate.analytics;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ServerValue;

public class AnalyticsManager {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    private static final DatabaseReference analyticsRef = database.getReference("Analytics");
    private static final DatabaseReference usersRef = database.getReference("User");
    private static final DatabaseReference outfitsRef = database.getReference("outfits");

    /**
     * Вызывать в toggleLikeInFirebase, когда isLiked == true
     */
    public static void trackFirstLookAddition(String userId) {
        DatabaseReference userRef = usersRef.child(userId);

        // Достаем данные пользователя одним запросом
        userRef.get().addOnSuccessListener(snapshot -> {
            Boolean alreadyTracked = snapshot.child("hasAddedLook").getValue(Boolean.class);
            Long regTimestamp = snapshot.child("registrationTimestamp").getValue(Long.class);

            if (alreadyTracked == null || !alreadyTracked) {
                long currentTime = System.currentTimeMillis();

                // 1. Ставим флаг, чтобы больше не заходить в это условие
                userRef.child("hasAddedLook").setValue(true);

                // 2. Считаем время до первого добавления (в секундах)
                if (regTimestamp != null && regTimestamp > 0) {
                    long diffInSeconds = (currentTime - regTimestamp) / 1000;
                    userRef.child("timeToFirstLike").setValue(diffInSeconds);
                }

                // 3. Инкрементируем глобальный счетчик активации
                incrementActiveUsersCounter();
            }
        });
    }

    private static void incrementActiveUsersCounter() {
        analyticsRef.child("totalUsersWithLooks").runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                currentData.setValue(current == null ? 1 : current + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean b, DataSnapshot s) {}
        });
    }

    public static void trackTimeRegistration(String userId) {
        long timestamp = System.currentTimeMillis();

        // 1. Записываем время регистрации в профиль пользователя
        usersRef.child(userId).child("registrationTimestamp").setValue(timestamp);
        incrementCounter(analyticsRef.child("RegisterCount"), 1);
    }

    /**
     * Увеличивает счетчик лайков конкретного образа
     */
    public static void trackOutfitFavorite(String outfitId) {
        DatabaseReference outfitRef = outfitsRef.child(outfitId);

        outfitRef.child("countFavorites").runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                currentData.setValue(current == null ? 1 : current + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean b, DataSnapshot s) {
                // Как только лайк записан, обновляем Favorite Rate для этой вещи
                calculateOutfitFavoriteRate(outfitId);
            }
        });
    }

    /**
     * Считает FR для конкретного образа: (лайки / просмотры) * 100
     */
    public static void calculateOutfitFavoriteRate(String outfitId) {
        DatabaseReference outfitRef = outfitsRef.child(outfitId);

        outfitRef.get().addOnSuccessListener(snapshot -> {
            Integer favorites = snapshot.child("countFavorites").getValue(Integer.class);

            // Если у тебя поле называется countShows, достаем его
            Integer shows = snapshot.child("countShows").getValue(Integer.class);

            if (favorites != null && shows != null && shows > 0) {
                double fr = (favorites * 100.0) / shows;
                outfitRef.child("favoriteRate").setValue(fr);
            }
        });
    }

    public static void trackCollectionCountChange(String userId) {
        DatabaseReference userRef = usersRef.child(userId);
        userRef.runTransaction(new Transaction.Handler() {
            int globalAction = 0; // 1: встал в "две коллекции", 2: перешел в "три+"

            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer count = currentData.child("collectionsCount").getValue(Integer.class);
                if (count == null) count = 0;
                int newCount = count + 1;
                currentData.child("collectionsCount").setValue(newCount);

                if (newCount == 2) {
                    globalAction = 1;
                    Long regTime = currentData.child("registrationTimestamp").getValue(Long.class);
                    if (regTime != null) {
                        currentData.child("timeToSecondCollection").setValue((System.currentTimeMillis() - regTime) / 1000);
                    }
                } else if (newCount == 3) {
                    globalAction = 2;
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean committed, DataSnapshot s) {
                if (committed) {
                    if (globalAction == 1) incrementCounter(analyticsRef.child("usersWithTwoCollections"), 1);
                    else if (globalAction == 2) {
                        incrementCounter(analyticsRef.child("usersWithTwoCollections"), -1);
                        incrementCounter(analyticsRef.child("usersWithThreePlusCollections"), 1);
                    }
                }
            }
        });
    }

    /**
     * Вызывать при удалении коллекции.
     * 1. Уменьшает счетчик коллекций.
     * 2. Фиксирует факт удаления для юзера (если впервые).
     */
    public static void trackCollectionDeletion(String userId) {
        DatabaseReference userRef = usersRef.child(userId);
        userRef.runTransaction(new Transaction.Handler() {
            boolean shouldIncrementDeletedGlobal = false;
            int globalAction = 0; // 1: из 3 в 2, 2: из 2 в 1

            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer count = currentData.child("collectionsCount").getValue(Integer.class);
                if (count == null) count = 0;
                int newCount = Math.max(0, count - 1);
                currentData.child("collectionsCount").setValue(newCount);

                // Логика перемещения между группами статки
                if (count == 3) globalAction = 1;
                else if (count == 2) globalAction = 2;

                // Логика первого удаления
                Boolean alreadyDeleted = currentData.child("hasDeletedCollection").getValue(Boolean.class);
                if (alreadyDeleted == null || !alreadyDeleted) {
                    currentData.child("hasDeletedCollection").setValue(true);
                    shouldIncrementDeletedGlobal = true;
                    Long regTime = currentData.child("registrationTimestamp").getValue(Long.class);
                    if (regTime != null) {
                        currentData.child("timeToFirstDeletion").setValue((System.currentTimeMillis() - regTime) / 1000);
                    }
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean committed, DataSnapshot s) {
                if (committed) {
                    if (shouldIncrementDeletedGlobal) incrementCounter(analyticsRef.child("totalUsersWhoDeletedSomething"), 1);
                    if (globalAction == 1) {
                        incrementCounter(analyticsRef.child("usersWithThreePlusCollections"), -1);
                        incrementCounter(analyticsRef.child("usersWithTwoCollections"), 1);
                    } else if (globalAction == 2) {
                        incrementCounter(analyticsRef.child("usersWithTwoCollections"), -1);
                    }
                }
            }
        });
    }

    // Вспомогательный метод для изменения глобальных цифр
    private static void incrementCounter(DatabaseReference ref, int value) {
        // Используем атомарный инкремент на стороне сервера
        ref.setValue(ServerValue.increment(value));
    }

    public static void calculateAndSaveActivationRate() {
        analyticsRef.get().addOnSuccessListener(snapshot -> {
            Integer totalWithLooks = snapshot.child("totalUsersWithLooks").getValue(Integer.class);
            Integer totalRegistered = snapshot.child("RegisterCount").getValue(Integer.class);

            if (totalWithLooks != null && totalRegistered != null && totalRegistered > 0) {
                double percentage = (totalWithLooks * 100.0) / totalRegistered;

                analyticsRef.child("lookActivationRate").setValue(percentage);
            }
        });
    }

    /**
     * Метод для расчета среднего времени до первого добавления образа
     */
    public static void calculateAverageTimeToFirstLike() {
        usersRef.get().addOnSuccessListener(snapshot -> {
            long totalSeconds = 0;
            int usersWithRecord = 0;

            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                Long timeToLook = userSnapshot.child("timeToFirstLike").getValue(Long.class);

                if (timeToLook != null) {
                    totalSeconds += timeToLook;
                    usersWithRecord++;
                }
            }

            if (usersWithRecord > 0) {
                long averageTime = totalSeconds / usersWithRecord;

                analyticsRef.child("averageTimeToFirstLikeMinutes").setValue(averageTime / 60.0);
            }
        });
    }

    /**
     * Считает среднее кол-во добавленных образов на всех зарегистрированных пользователей
     */
    public static void calculateAverageLikesPerUser() {
        // 1. Сначала берем общее кол-во юзеров
        analyticsRef.child("RegisterCount").get().addOnSuccessListener(regSnapshot -> {
            Integer totalUsers = regSnapshot.getValue(Integer.class);

            if (totalUsers != null && totalUsers > 0) {

                // 2. Затем считаем сумму всех лайков в items
                outfitsRef.get().addOnSuccessListener(itemsSnapshot -> {
                    long totalLikes = 0;

                    for (DataSnapshot item : itemsSnapshot.getChildren()) {
                        Integer itemLikes = item.child("countFavorites").getValue(Integer.class);
                        if (itemLikes != null) {
                            totalLikes += itemLikes;
                        }
                    }

                    // 3. Считаем среднее и сохраняем
                    double average = (double) totalLikes / totalUsers;
                    analyticsRef.child("averageLooksPerUser").setValue(average);
                });
            }
        });
    }

    /**
     * Проходит по всей таблице outfits и сбрасывает аналитические поля в дефолтные значения.
     * Если нужно полностью УДАЛИТЬ поле из БД, замените setValue(0) на setValue(null).
     */
    public static void resetOutfitAnalyticsFields() {
        outfitsRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) return;

            for (DataSnapshot outfitSnapshot : snapshot.getChildren()) {
                DatabaseReference currentOutfitRef = outfitSnapshot.getRef();

                currentOutfitRef.child("countFavorites").setValue(0);
                currentOutfitRef.child("countShows").setValue(0);
                currentOutfitRef.child("favoriteRate").setValue(0.0);
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();
        });
    }
}