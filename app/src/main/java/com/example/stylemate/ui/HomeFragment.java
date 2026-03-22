package com.example.stylemate.ui;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager; // Важный импорт

import com.example.stylemate.R;
import com.example.stylemate.model.HomeViewModel;
import com.example.stylemate.model.FilterState; // Импортируем наш новый класс
import com.example.stylemate.model.Outfit;
import com.example.stylemate.repository.ActiveUserInfo;
import com.example.stylemate.repository.UserRepository;
import com.example.stylemate.ui.new_select_test.NewSelectQ1Activity;
import com.example.stylemate.ui.dialogs.UniversalInfoDialog;

import java.util.ArrayList;
import java.util.HashSet;

public class HomeFragment extends Fragment {

    private final UserRepository repo = new UserRepository();
    private RecyclerView rvCollections;
    private RecyclerView rvGrid; // --- НОВОЕ: Ссылка на сетку товаров
    private View vOverlay;
    private View btnCreate;      // --- НОВОЕ: Кнопка создать
    private View clEmptyState; // --- НОВОЕ: Наша заглушка (ConstraintLayout)
    private ImageButton btnContinueTest;
    private ImageButton btnLogout; // --- НОВОЕ: Кнопки
    // Лаунчер для получения результата из Карточки

    private CollectionsNameAdapter adapter;
    private OutfitAdapter gridAdapter; // --- НОВОЕ: Адаптер для сетки
    private HomeViewModel viewModel;

    private boolean isListExpanded = false;
    private boolean isGuest;     // --- НОВОЕ: Флаг гостя

    // Храним текущие фильтры (по умолчанию пустые)
    private FilterState currentFilterState = new FilterState(new HashSet<>(), new HashSet<>(), new HashSet<>());

    // Для таймера 30 минут
    private long lastPauseTime = 0;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 минут в миллисекундах

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Инициализация View
        View btnList = view.findViewById(R.id.btnList);
        rvCollections = view.findViewById(R.id.rvCollections);
        vOverlay = view.findViewById(R.id.vOverlay);

        // --- НОВОЕ: Ищем новые элементы
        rvGrid = view.findViewById(R.id.rvOutfitsGrid);
        btnCreate = view.findViewById(R.id.btnCreate);

        clEmptyState = view.findViewById(R.id.clEmptyState);
        btnContinueTest = view.findViewById(R.id.btnContinueTest);
        btnLogout = view.findViewById(R.id.btnLogout);

        // --- НОВОЕ: 1. Получаем данные о пользователе (Гость / Стиль)
        String guestFlag = ActiveUserInfo.getDefaults("isRegistered", getContext());
        isGuest = guestFlag == null || guestFlag.isEmpty();

        // --- ЛОГИКА ВЫПАДАЮЩЕГО СПИСКА (ТВОЙ СТАРЫЙ КОД) ---
        rvCollections.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CollectionsNameAdapter(new ArrayList<>(), "Основная", clickedName -> {
            if (clickedName == null) {
                toggleListState();
            } else {
                viewModel.onCollectionSelected(clickedName);
            }
        });
        rvCollections.setAdapter(adapter);


        // --- НОВОЕ: 2. ЛОГИКА СЕТКИ ТОВАРОВ (PINTEREST STYLE) ---

        // Настраиваем StaggeredGridLayoutManager (2 колонки, вертикально)
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        // Убираем дергание при скролле
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        rvGrid.setLayoutManager(layoutManager);

        // Создаем адаптер
        // ВАЖНО: Первым аргументом передаем getContext(), так как мы обновили адаптер для Glide
        gridAdapter = new OutfitAdapter(getContext(), new ArrayList<>(), new OutfitAdapter.OnOutfitClickListener() {
            @Override
            public void onHeartClick(Outfit outfit, int position) {
                if (isGuest) {
                    Toast.makeText(getContext(), "Войдите или зарегистрируйтесь, чтобы сохранять", Toast.LENGTH_SHORT).show();
                } else {
                    // outfit.getId() теперь возвращает String (ID из базы), это правильно
                    viewModel.toggleLike(outfit.getId());
                    // 2. --- НОВАЯ ЛОГИКА: ОБУЧЕНИЕ ЛАЙКАМ ---
                    Context context = getContext();
                    String isShown = ActiveUserInfo.getDefaults("IS_FIRST_LIKE_SHOWN", context);

                    // Если flag != "true" (значит null или "false"), показываем
                    if (!"true".equals(isShown)) {
                        String text = "Вы поставили лайк образу\nТеперь он сохранен в папке в\nличном кабинете.";

                        // Создаем диалог (false = СТРЕЛКА НЕ НУЖНА)
                        UniversalInfoDialog dialog = UniversalInfoDialog.newInstance(text, false);
                        dialog.show(getParentFragmentManager(), "FirstLikeTag");

                        // Записываем, что показали
                        ActiveUserInfo.setDefaults("IS_FIRST_LIKE_SHOWN", "true", context);
                    }
                }
            }

            @Override
            public void onImageClick(Outfit outfit) {
                Intent intent = new Intent(getContext(), OutfitDetailActivity.class);

                // Передаем основные данные
                intent.putExtra("outfit_id", outfit.getId());
                intent.putExtra("image_url", outfit.getImageUrl());
                intent.putExtra("style", outfit.getStyle());
                intent.putExtra("season", outfit.getFilter_season()); // Важно: нужен этот геттер!
                // 1. Передаем статус лайка (чтобы сердечко было синим)
                intent.putExtra("is_liked", outfit.isLiked());
                if (viewModel.getCurrentCollectionId() != null) {
                    intent.putExtra("collection_id", viewModel.getCurrentCollectionId());
                }

                // Передаем список ID вещей
                // Преобразуем Map<String, Boolean> в ArrayList<String>
                if (outfit.getItems() != null) {
                    ArrayList<String> ids = new ArrayList<>(outfit.getItems().keySet());
                    intent.putStringArrayListExtra("item_ids", ids);
                }

                startActivity(intent);
            }
        });
        rvGrid.setAdapter(gridAdapter);

        // =========================================================================
        // --- НОВОЕ: ЛОГИКА 2 - ОБУЧЕНИЕ "КОНЕЦ СПИСКА" ---
        // =========================================================================
        rvGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // dy > 0 означает, что пользователь скроллит вниз.
                // !canScrollVertically(1) означает, что список больше не может крутиться вниз (мы на дне).
                if (dy > 0 && !recyclerView.canScrollVertically(1)) {

                    Context context = getContext();
                    // Проверяем, показывали ли уже это сообщение
                    String isShown = ActiveUserInfo.getDefaults("IS_END_SCROLL_SHOWN", context);

                    if (!"true".equals(isShown)) {
                        String text = "Вы долистали до конца.\nСоздайте новую подборку,\nчтобы увидеть больше образов.";

                        // Показываем диалог (false = стрелка не нужна, текст по центру)
                        UniversalInfoDialog dialog = UniversalInfoDialog.newInstance(text, false);
                        // Используем уникальный тег, чтобы не путать с другими диалогами
                        dialog.show(getParentFragmentManager(), "EndListTag");

                        // Запоминаем, что показали
                        ActiveUserInfo.setDefaults("IS_END_SCROLL_SHOWN", "true", context);
                    }
                }
            }
        });

        // =========================================================================
        // --- ЭТАП 5: СВЯЗКА ФИЛЬТРОВ (Fragment Result Listener) ---
        // Слушаем результат от FiltersBottomSheetFragment
        // =========================================================================

        getParentFragmentManager().setFragmentResultListener(
                FiltersBottomSheetFragment.REQUEST_KEY, // Ключ запроса (должен совпадать)
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    // Достаем объект FilterState из Bundle
                    android.util.Log.d("FILTER_DEBUG", "Фрагмент: Получил сигнал!"); // <--- ЛОГ
                    FilterState state = (FilterState) result.getSerializable(FiltersBottomSheetFragment.RESULT_KEY);

                    if (state != null) {
                        // 1. Сохраняем себе, чтобы потом передать обратно в шторку
                        this.currentFilterState = state;
                        // 2. Применяем
                        viewModel.applyFilters(state);
                    }
                }
        );


        // --- ПОДПИСКА НА ДАННЫЕ (OBSERVERS) ---

        // 1. Список коллекций
        viewModel.collections.observe(getViewLifecycleOwner(), list -> {
            adapter.updateList(list);

            // ЛОГИКА 3: ОБУЧЕНИЕ "ВЫБОР КОЛЛЕКЦИИ"
            // Если коллекций стало 2 или больше
            if (list != null && list.size() >= 2) {
                Context context = getContext();
                if (context != null) {
                    // Проверяем память
                    String isShown = ActiveUserInfo.getDefaults("IS_COLLECTION_SWITCH_SHOWN", context);

                    if (!"true".equals(isShown)) {
                        String text = "Все ваши подборки будут\nпоявляться в меню сверху";

                        UniversalInfoDialog dialog = UniversalInfoDialog.newInstance(text, true);

                        // Используем уникальный тег
                        if (isAdded()) {
                            dialog.show(getParentFragmentManager(), "CollectionGuideTag");
                            // Запоминаем
                            ActiveUserInfo.setDefaults("IS_COLLECTION_SWITCH_SHOWN", "true", context);
                        }
                    }
                }
            }
        });

        // 2. Выбранная коллекция
        viewModel.selectedName.observe(getViewLifecycleOwner(), name -> adapter.setSelectedName(name));

        // 3. Список картинок (обновляем адаптер)
        viewModel.outfits.observe(getViewLifecycleOwner(), outfits -> gridAdapter.updateList(outfits));

        // 4. --- НОВОЕ: Обработка пустого результата фильтрации ---
        viewModel.filterEmptyEvent.observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null && isEmpty) {
                Toast.makeText(getContext(), "С такими фильтрами ничего не найдено", Toast.LENGTH_SHORT).show();
                btnList.performClick();
            }
        });


        // --- ОБРАБОТЧИКИ НАЖАТИЙ ---

        vOverlay.setOnClickListener(v -> {
            if (isListExpanded) toggleListState();
        });

        // Открытие фильтров
        btnList.setOnClickListener(v -> {
            // Используем наш newInstance, чтобы передать текущие галочки
            FiltersBottomSheetFragment filtersFragment = FiltersBottomSheetFragment.newInstance(currentFilterState);
            filtersFragment.show(getParentFragmentManager(), "FiltersTag");
        });

        btnCreate.setOnClickListener(v -> {
            if (isGuest) {
                Toast.makeText(getContext(), "Доступно только зарегистрированным пользователям", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.selectedName.observe(getViewLifecycleOwner(), name -> {
                    Intent intent = new Intent(requireContext(), NewSelectQ1Activity.class);
                    ActiveUserInfo.setDefaults("collectionName", name, requireContext());
                    startActivity(intent);
                });
            }
        });

        // --- НОВОЕ: Подписка на состояние "Пусто" ---
        viewModel.isEmptyState.observe(getViewLifecycleOwner(), isEmpty -> {
            View bottomNav = requireActivity().findViewById(R.id.bottomNavBar);
            if (isEmpty != null && isEmpty) {
                // ПОКАЗЫВАЕМ ЗАГЛУШКУ
                clEmptyState.setVisibility(View.VISIBLE);

                // Скрываем основной контент (чтобы не просвечивал/не кликался)
                rvGrid.setVisibility(View.GONE);
                view.findViewById(R.id.btnList).setVisibility(View.GONE);
                view.findViewById(R.id.btnCreate).setVisibility(View.GONE);
                if (bottomNav != null) bottomNav.setVisibility(View.GONE);
            } else {
                // СКРЫВАЕМ ЗАГЛУШКУ
                clEmptyState.setVisibility(View.GONE);

                // Показываем контент
                rvGrid.setVisibility(View.VISIBLE);
                view.findViewById(R.id.btnList).setVisibility(View.VISIBLE);
                view.findViewById(R.id.btnCreate).setVisibility(View.VISIBLE);
                // --- ВОЗВРАЩАЕМ НИЖНЮЮ НАВИГАЦИЮ ---
                if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
            }
        });


        // --- НОВОЕ: Обработка кнопок заглушки ---

        // 1. Пройти тест заново
        btnContinueTest.setOnClickListener(v -> {
            // Переход на экран Теста (замени TestActivity.class на свой класс теста)
            Intent intent = new Intent(requireContext(), NewSelectQ1Activity.class);
            startActivity(intent);
            // Можно добавить finish(), если хочешь закрыть главную, но обычно тест идет поверх
        });

        // 2. Выйти из аккаунта
        btnLogout.setOnClickListener(v -> {
            repo.logout(requireContext());

            // 4. Переход на экран Авторизации/Входа (AuthActivity)
            // Замени AuthActivity.class на твой класс экрана входа!
            Intent intent = new Intent(requireContext(), AuthActivity.class);

            // Очищаем историю переходов, чтобы кнопкой "Назад" нельзя было вернуться в профиль
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
        });

        return view;
    }

    private void toggleListState() {
        if (isListExpanded) {
            isListExpanded = false;
            adapter.setExpanded(false);
            rvCollections.setBackgroundResource(android.R.color.transparent);
            vOverlay.setVisibility(View.GONE);
        } else {
            isListExpanded = true;
            adapter.setExpanded(true);
            rvCollections.setBackgroundResource(R.drawable.bg_dropdown_list);
            vOverlay.setVisibility(View.VISIBLE);
        }
    }

    // === ЛОГИКА ТАЙМЕРА 30 МИНУТ ===

    @Override
    public void onPause() {
        super.onPause();
        // Запоминаем время, когда юзер свернул приложение или ушел с фрагмента
        lastPauseTime = System.currentTimeMillis();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Проверяем: если приложение было свернуто, и прошло > 30 минут
        if (lastPauseTime > 0) {
            long diff = System.currentTimeMillis() - lastPauseTime;
            if (diff > SESSION_TIMEOUT) {
                // Время вышло -> Сбрасываем фильтры
                resetFiltersByTimeout();
            }
        }

        // Чтобы лайки синхронизировались с тем, что мы наделали в папке Избранное
        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    private void resetFiltersByTimeout() {
        // 1. Очищаем локальное состояние
        currentFilterState = new FilterState(new HashSet<>(), new HashSet<>(), new HashSet<>());
        // 2. Очищаем ViewModel
        viewModel.applyFilters(currentFilterState);
        // 3. Можно показать уведомление (необязательно)
        // Toast.makeText(getContext(), "Сессия истекла, фильтры сброшены", Toast.LENGTH_SHORT).show();

        lastPauseTime = 0; // Сбрасываем таймер
    }
}