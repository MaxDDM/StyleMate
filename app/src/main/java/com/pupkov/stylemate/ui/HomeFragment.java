package com.pupkov.stylemate.ui;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.HomeViewModel;
import com.pupkov.stylemate.model.FilterState;
import com.pupkov.stylemate.model.Outfit;
import com.pupkov.stylemate.repository.ActiveUserInfo;
import com.pupkov.stylemate.repository.UserRepository;
import com.pupkov.stylemate.ui.new_select_test.NewSelectQ1Activity;
import com.pupkov.stylemate.ui.dialogs.UniversalInfoDialog;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Фрагмент главного экрана, отвечающий за отображение ленты образов,
 * управление коллекциями, фильтрацию и отображение контекстных подсказок.
 */
public class HomeFragment extends Fragment {

    private final UserRepository repo = new UserRepository();
    private RecyclerView rvCollections;
    private RecyclerView rvGrid;
    private View vOverlay;
    private View clEmptyState;
    private ImageButton btnContinueTest;
    private ImageButton btnLogout;

    private CollectionsNameAdapter adapter;
    private OutfitAdapter gridAdapter;
    private HomeViewModel viewModel;

    private boolean isListExpanded = false;
    private boolean isGuest;

    // Текущее состояние фильтрации (по умолчанию пустое)
    private FilterState currentFilterState = new FilterState(new HashSet<>(), new HashSet<>(), new HashSet<>());

    // Настройки сессии фильтрации (временной интервал сброса — 30 минут)
    private long lastPauseTime = 0;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Инициализация компонентов интерфейса
        View btnList = view.findViewById(R.id.btnList);
        rvCollections = view.findViewById(R.id.rvCollections);
        ImageButton btnEdit = view.findViewById(R.id.btnEdit);     // Новая кнопка
        ImageButton btnDelete = view.findViewById(R.id.btnDelete);
        vOverlay = view.findViewById(R.id.vOverlay);
        rvGrid = view.findViewById(R.id.rvOutfitsGrid);
        clEmptyState = view.findViewById(R.id.clEmptyState);
        btnContinueTest = view.findViewById(R.id.btnContinueTest);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Определение статуса авторизации пользователя
        String guestFlag = ActiveUserInfo.getDefaults("isRegistered", getContext());
        isGuest = guestFlag == null || guestFlag.isEmpty();

        // Настройка выпадающего списка коллекций
        rvCollections.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CollectionsNameAdapter(new ArrayList<>(), "Основная", new CollectionsNameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String clickedName) {
                if (clickedName == null) {
                    toggleListState();
                } else {
                    viewModel.onCollectionSelected(clickedName);
                    // Автоматически сворачиваем список при выборе другой коллекции
                    if (isListExpanded) {
                        toggleListState();
                    }
                }
            }

            @Override
            public void onCreateNewClick() {
                // Сворачиваем список перед переходом
                if (isListExpanded) {
                    toggleListState();
                }

                if (isGuest) {
                    Toast.makeText(getContext(), "Доступно только зарегистрированным пользователям", Toast.LENGTH_SHORT).show();
                } else {
                    // Берем текущее имя напрямую из ViewModel без создания лишних observe
                    String currentName = viewModel.selectedName.getValue();
                    ActiveUserInfo.setDefaults("collectionName", currentName, requireContext());

                    Intent intent = new Intent(requireContext(), NewSelectQ1Activity.class);
                    startActivity(intent);
                }
            }
        });
        rvCollections.setAdapter(adapter);

        // Настройка сетки отображения образов
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvGrid.setLayoutManager(layoutManager);

        // Инициализация адаптера ленты образов
        gridAdapter = new OutfitAdapter(getContext(), new ArrayList<>(), new OutfitAdapter.OnOutfitClickListener() {
            @Override
            public void onHeartClick(Outfit outfit, int position) {
                // Проверка прав доступа: ограничение функционала добавления в избранное для гостей
                if (isGuest) {
                    Toast.makeText(getContext(), "Войдите или зарегистрируйтесь, чтобы сохранять", Toast.LENGTH_SHORT).show();
                } else {
                    viewModel.toggleLike(outfit.getId());

                    // Логика демонстрации диалога при первом добавлении в избранное
                    Context context = getContext();
                    String isShown = ActiveUserInfo.getDefaults("IS_FIRST_LIKE_SHOWN", context);

                    if (!"true".equals(isShown)) {
                        String text = "Вы поставили лайк образу\nТеперь он сохранен в папке в\nличном кабинете.";
                        UniversalInfoDialog dialog = UniversalInfoDialog.newInstance(text, false);
                        dialog.show(getParentFragmentManager(), "FirstLikeTag");
                        ActiveUserInfo.setDefaults("IS_FIRST_LIKE_SHOWN", "true", context);
                    }
                }
            }

            // Инициализация перехода к детальному просмотру образа с передачей параметров
            @Override
            public void onImageClick(Outfit outfit) {
                Intent intent = new Intent(getContext(), OutfitDetailActivity.class);
                intent.putExtra("outfit_id", outfit.getId());
                intent.putExtra("image_url", outfit.getImageUrl());
                intent.putExtra("style", outfit.getStyle());
                intent.putExtra("season", outfit.getFilter_season());
                intent.putExtra("is_liked", outfit.isLiked());

                if (viewModel.getCurrentCollectionId() != null) {
                    intent.putExtra("collection_id", viewModel.getCurrentCollectionId());
                }

                // Конвертация структуры связей Map в массив для передачи идентификаторов вещей
                if (outfit.getItems() != null) {
                    ArrayList<String> ids = new ArrayList<>(outfit.getItems().keySet());
                    intent.putStringArrayListExtra("item_ids", ids);
                }
                startActivity(intent);
            }
        });
        rvGrid.setAdapter(gridAdapter);

        // Слушатель прокрутки сетки для фиксации достижения конца списка
        rvGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && !recyclerView.canScrollVertically(1)) {
                    Context context = getContext();
                    String isShown = ActiveUserInfo.getDefaults("IS_END_SCROLL_SHOWN", context);

                    if (!"true".equals(isShown)) {
                        String text = "Вы долистали до конца.\nСоздайте новую подборку,\nчтобы увидеть больше образов.";
                        UniversalInfoDialog dialog = UniversalInfoDialog.newInstance(text, false);
                        dialog.show(getParentFragmentManager(), "EndListTag");
                        ActiveUserInfo.setDefaults("IS_END_SCROLL_SHOWN", "true", context);
                    }
                }
            }
        });

        // Регистрация слушателя для получения состояния фильтров из шторки
        getParentFragmentManager().setFragmentResultListener(
                FiltersBottomSheetFragment.REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    FilterState state = (FilterState) result.getSerializable(FiltersBottomSheetFragment.RESULT_KEY);
                    if (state != null) {
                        this.currentFilterState = state;
                        viewModel.applyFilters(state);
                    }
                }
        );

        // Настройка наблюдателей LiveData

        // Наблюдатель за списком доступных коллекций пользователя
        viewModel.collections.observe(getViewLifecycleOwner(), list -> {
            adapter.updateList(list);

            if (list != null && list.size() >= 2) {
                Context context = getContext();
                if (context != null) {
                    String isShown = ActiveUserInfo.getDefaults("IS_COLLECTION_SWITCH_SHOWN", context);

                    if (!"true".equals(isShown)) {
                        String text = "Все ваши подборки будут\nпоявляться в меню сверху";
                        UniversalInfoDialog dialog = UniversalInfoDialog.newInstance(text, true);

                        if (isAdded()) {
                            dialog.show(getParentFragmentManager(), "CollectionGuideTag");
                            ActiveUserInfo.setDefaults("IS_COLLECTION_SWITCH_SHOWN", "true", context);
                        }
                    }
                }
            }
        });

        viewModel.selectedName.observe(getViewLifecycleOwner(), name -> adapter.setSelectedName(name));

        viewModel.outfits.observe(getViewLifecycleOwner(), outfits -> gridAdapter.updateList(outfits));

        // Обработка события отсутствия результатов при фильтрации
        viewModel.filterEmptyEvent.observe(getViewLifecycleOwner(), isEmpty -> {
            if (isEmpty != null && isEmpty) {
                Toast.makeText(getContext(), "С такими фильтрами ничего не найдено", Toast.LENGTH_SHORT).show();
                btnList.performClick();
            }
        });

        // Наблюдатель за состоянием отображения заглушки пустого экрана (Empty State)
        viewModel.isEmptyState.observe(getViewLifecycleOwner(), isEmpty -> {
            View bottomNav = requireActivity().findViewById(R.id.bottomNavBar);
            if (isEmpty != null && isEmpty) {
                clEmptyState.setVisibility(View.VISIBLE);
                rvGrid.setVisibility(View.GONE);
                btnList.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                rvCollections.setVisibility(View.GONE);
                if (bottomNav != null) bottomNav.setVisibility(View.GONE);
            } else {
                clEmptyState.setVisibility(View.GONE);
                rvGrid.setVisibility(View.VISIBLE);
                btnList.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                rvCollections.setVisibility(View.VISIBLE);
                if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
            }
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Настройка слушателей нажатий элементов

        vOverlay.setOnClickListener(v -> {
            if (isListExpanded) toggleListState();
        });

        btnList.setOnClickListener(v -> {
            FiltersBottomSheetFragment filtersFragment = FiltersBottomSheetFragment.newInstance(currentFilterState);
            filtersFragment.show(getParentFragmentManager(), "FiltersTag");
        });

        btnEdit.setOnClickListener(v -> {
            String currentTitle = viewModel.selectedName.getValue();
            EditCollectionBottomSheet bottomSheet = EditCollectionBottomSheet.newInstance(currentTitle);
            bottomSheet.setListener(newTitle -> viewModel.onCollectionRenamed(newTitle));
            bottomSheet.show(getParentFragmentManager(), "EditCollectionTag");
        });

        btnDelete.setOnClickListener(v -> {
            DeleteCollectionDialog dialog = new DeleteCollectionDialog();
            dialog.setListener(() -> viewModel.onCollectionDeleted());
            dialog.show(getParentFragmentManager(), "DeleteDialog");
        });

        btnContinueTest.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), NewSelectQ1Activity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            repo.logout(requireContext());
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    /**
     * Изменяет состояние отображения выпадающего списка коллекций.
     */
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

    @Override
    public void onPause() {
        super.onPause();
        lastPauseTime = System.currentTimeMillis();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Проверка времени нахождения фрагмента в фоне для сброса фильтров
        if (lastPauseTime > 0) {
            long diff = System.currentTimeMillis() - lastPauseTime;
            if (diff > SESSION_TIMEOUT) {
                resetFiltersByTimeout();
            }
        }

        if (viewModel != null) {
            viewModel.refreshData();
        }
    }

    /**
     * Сбрасывает текущие фильтры при превышении лимита времени неактивности сессии.
     */
    private void resetFiltersByTimeout() {
        currentFilterState = new FilterState(new HashSet<>(), new HashSet<>(), new HashSet<>());
        viewModel.applyFilters(currentFilterState);
        lastPauseTime = 0;
    }
}