package com.pupkov.stylemate.analytics;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class AnalyticsManager {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance("https://stylemate-fdd7b-default-rtdb.europe-west1.firebasedatabase.app");
    private static final DatabaseReference analyticsRef = database.getReference("Analytics");
    private static final DatabaseReference usersRef = database.getReference("User");
    private static final DatabaseReference itemsRef = database.getReference("items");

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
    public static void trackItemFavorite(String itemId) {
        DatabaseReference itemRef = itemsRef.child(itemId);

        itemRef.child("countFavorites").runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                currentData.setValue(current == null ? 1 : current + 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean b, DataSnapshot s) {
                // Как только лайк записан, обновляем Favorite Rate для этой вещи
                calculateItemFavoriteRate(itemId);
            }
        });
    }

    /**
     * Считает FR для конкретной вещи: (лайки / просмотры) * 100
     */
    public static void calculateItemFavoriteRate(String itemId) {
        DatabaseReference itemRef = itemsRef.child(itemId);

        itemRef.get().addOnSuccessListener(snapshot -> {
            Integer favorites = snapshot.child("countFavorites").getValue(Integer.class);

            // Если у тебя поле называется countShows, достаем его
            Integer shows = snapshot.child("countShows").getValue(Integer.class);

            if (favorites != null && shows != null && shows > 0) {
                double fr = (favorites * 100.0) / shows;
                itemRef.child("favoriteRate").setValue(fr);
            }
        });
    }

    public static void trackCollectionCountChange(String userId) {
        // Запускаем транзакцию на ВЕСЬ объект пользователя
        DatabaseReference userRef = database.getReference("User").child(userId);

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                // Теперь достаем collectionsCount из "ребенка"
                Integer count = currentData.child("collectionsCount").getValue(Integer.class);
                if (count == null) count = 0;

                int newCount = count + 1;
                currentData.child("collectionsCount").setValue(newCount);

                // Теперь это сработает, так как мы находимся в корне пользователя
                if (newCount == 2) {
                    Long regTime = currentData.child("registrationTimestamp").getValue(Long.class);

                    if (regTime != null && regTime > 0) {
                        long diffInSeconds = (System.currentTimeMillis() - regTime) / 1000;
                        // Поле появится на том же уровне, что и collectionsCount
                        currentData.child("timeToSecondCollection").setValue(diffInSeconds);
                    }

                    // Обновляем глобальную статку (вне транзакции текущего юзера)
                    incrementCounter(analyticsRef.child("usersWithTwoCollections"), 1);
                }
                else if (newCount == 3) {
                    incrementCounter(analyticsRef.child("usersWithTwoCollections"), -1);
                    incrementCounter(analyticsRef.child("usersWithThreePlusCollections"), 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean b, DataSnapshot s) {}
        });
    }

    /**
     * Вызывать при удалении коллекции.
     * 1. Уменьшает счетчик коллекций.
     * 2. Фиксирует факт удаления для юзера (если впервые).
     */
    public static void trackCollectionDeletion(String userId) {
        DatabaseReference userRef = database.getReference("User").child(userId);

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                // 1. Работаем со счетчиком коллекций
                Integer count = currentData.child("collectionsCount").getValue(Integer.class);
                if (count == null) count = 0;

                int newCount = Math.max(0, count - 1);
                currentData.child("collectionsCount").setValue(newCount);

                // Обновляем глобальные счетчики в Analytics
                DatabaseReference statsRef = database.getReference("Analytics");

                // Если было 3, а стало 2
                if (count == 3) {
                    incrementCounter(statsRef.child("usersWithThreePlusCollections"), -1);
                    incrementCounter(statsRef.child("usersWithTwoCollections"), 1);
                }
                // Если было 2, а стало 1
                else if (count == 2) {
                    incrementCounter(statsRef.child("usersWithTwoCollections"), -1);
                }

                // 2. Флаг "Удалил хотя бы одну"
                Boolean alreadyDeleted = currentData.child("hasDeletedCollection").getValue(Boolean.class);
                if (alreadyDeleted == null || !alreadyDeleted) {
                    currentData.child("hasDeletedCollection").setValue(true);

                    Long regTimestamp = currentData.child("registrationTimestamp").getValue(Long.class);
                    if (regTimestamp != null && regTimestamp > 0) {
                        long diffInSeconds = (System.currentTimeMillis() - regTimestamp) / 1000;
                        currentData.child("timeToFirstDeletion").setValue(diffInSeconds);
                    }
                    // Увеличиваем глобальный счетчик людей, которые хоть раз удаляли
                    incrementCounter(statsRef.child("totalUsersWhoDeletedSomething"), 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError e, boolean b, DataSnapshot s) {}
        });
    }

    // Вспомогательный метод для изменения глобальных цифр
    private static void incrementCounter(DatabaseReference ref, int value) {
        ref.runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                currentData.setValue(current == null ? Math.max(0, value) : Math.max(0, current + value));
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(DatabaseError e, boolean b, DataSnapshot s) {}
        });
    }

    public static void calculateAndSaveActivationRate() {
        analyticsRef.get().addOnSuccessListener(snapshot -> {
            Integer totalWithLooks = snapshot.child("totalUsersWithLooks").getValue(Integer.class);
            Integer totalRegistered = snapshot.child("RegisterCount").getValue(Integer.class);

            if (totalWithLooks != null && totalRegistered != null && totalRegistered > 0) {
                // Считаем процент
                double percentage = (totalWithLooks * 100.0) / totalRegistered;

                // Записываем обратно в Analytics
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

                // Для удобства можно записать еще и в минутах (опционально)
                analyticsRef.child("averageTimeToFirstLikeMinutes").setValue(averageTime / 60.0);
            }
        });
    }

    /**
     * Считает среднее кол-во добавленных образов на всех зарегистрированных пользователей
     */
    public static void calculateAverageLooksPerUser() {
        // 1. Сначала берем общее кол-во юзеров
        analyticsRef.child("RegisterCount").get().addOnSuccessListener(regSnapshot -> {
            Integer totalUsers = regSnapshot.getValue(Integer.class);

            if (totalUsers != null && totalUsers > 0) {

                // 2. Затем считаем сумму всех лайков в items
                itemsRef.get().addOnSuccessListener(itemsSnapshot -> {
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
}