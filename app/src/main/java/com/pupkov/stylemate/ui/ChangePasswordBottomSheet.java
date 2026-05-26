package com.pupkov.stylemate.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.pupkov.stylemate.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.pupkov.stylemate.model.ChangePasswordViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Шторка для смены пароля.
 * Сначала проверяет старый пароль, а затем переключает экран на ввод нового.
 */
public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    private ChangePasswordViewModel viewModel;
    private ViewFlipper viewFlipper;
    private EditText etOldPassword;
    private EditText etNewPassword;

    // Флаг, чтобы знать, где мы сейчас: false — ввод старого пароля, true — ввод нового
    private boolean isSecondStep = false;

    // Защита от слишком частых нажатий на Enter (раз в полсекунды)
    private static final long ENTER_DEBOUNCE_MS = 500;
    private long lastEnterTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                DisplayMetrics displayMetrics = new DisplayMetrics();
                requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                bottomSheet.getLayoutParams().height = (int) (displayMetrics.heightPixels * 0.75);

                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);

                // Управление клавиатурой при перетаскивании шторки пальцем
                behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        // Если шторку начали тянуть вниз — прячем клавиатуру
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            hideKeyboardDirectly();
                        }
                        // Если шторка вернулась на место (наверх) — снова показываем клавиатуру
                        else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            showKeyboardForCurrentStep();
                        }
                        // Если шторку закрыли полностью — прячем клавиатуру на всякий случай
                        else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            hideKeyboardDirectly();
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
                });
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Подключаем ViewModel для обработки логики паролей
        viewModel = new ViewModelProvider(this).get(ChangePasswordViewModel.class);

        viewFlipper = view.findViewById(R.id.viewFlipper);
        etOldPassword = view.findViewById(R.id.etOldPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);

        viewFlipper.setInAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
        viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left));

        setupInputUI(etOldPassword);
        setupInputUI(etNewPassword);

        etOldPassword.requestFocus();
        new Handler().postDelayed(this::showKeyboardForCurrentStep, 200);

        // Подписываемся на ответы от ViewModel
        observeViewModel();

        // Слушатель для кнопки Enter на первом шаге (ввод старого пароля)
        etOldPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event)) {
                if (checkDebounce()) return true;

                String oldPassword = etOldPassword.getText().toString().trim();
                if (oldPassword.isEmpty()) {
                    showError(etOldPassword, "Введите старый пароль");
                    return true;
                }

                // Отправляем старый пароль на проверку
                viewModel.verifyOldPassword(oldPassword, requireContext());
                return true;
            }
            return false;
        });

        // Слушатель для кнопки Enter на втором шаге (ввод нового пароля)
        etNewPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (isEnterPressed(actionId, event)) {
                if (checkDebounce()) return true;

                // Отправляем новый пароль на сохранение
                viewModel.submitNewPassword(etNewPassword.getText().toString(), requireContext());
                return true;
            }
            return false;
        });
    }

    /**
     * Следит за результатами проверок паролей.
     */
    private void observeViewModel() {
        // Слушаем результат проверки старого пароля
        viewModel.oldPasswordCorrect.observe(getViewLifecycleOwner(), resource -> {
            switch (resource.status) {
                case LOADING:
                    Toast.makeText(requireContext(), "Идёт процесс проверки пароля", Toast.LENGTH_LONG).show();
                    break;
                case SUCCESS:
                    if (Boolean.TRUE.equals(resource.data)) {
                        // Пароль подошел — переключаем экран на ввод нового пароля
                        isSecondStep = true;
                        if (viewFlipper.getDisplayedChild() == 0) {
                            viewFlipper.showNext();
                        }
                        etNewPassword.requestFocus();
                        showKeyboardForCurrentStep();
                    } else {
                        Toast.makeText(requireContext(), "Неверный пароль", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        // Слушаем результат успешной смены пароля
        viewModel.passwordChanged.observe(getViewLifecycleOwner(), success -> {
            if (success) {
                CustomToast.show(getContext(), "Пароль изменен");
                hideKeyboardDirectly();
                // Закрываем шторку после легкой задержки
                new Handler().postDelayed(this::dismiss, 150);
            }
        });

        // Слушаем ошибки валидации
        viewModel.errorEvent.observe(getViewLifecycleOwner(), message -> {
            EditText target = isSecondStep ? etNewPassword : etOldPassword;
            showError(target, message);
        });
    }

    private void setupInputUI(EditText editText) {
        // Когда пользователь начинает заново вводить текст, убираем значок ошибки
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Показывает значок ошибки в самом поле ввода, выдает тост и слегка вибрирует.
     */
    private void showError(EditText field, String message) {
        field.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error_circle, 0);
        CustomToast.show(getContext(), message);
        field.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
    }

    // Проверяет, была ли нажата клавиша Enter на клавиатуре
    private boolean isEnterPressed(int actionId, KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (actionId == EditorInfo.IME_NULL &&
                        event != null &&
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }

    // Защита, чтобы при случайном двойном клике код не срабатывал дважды
    private boolean checkDebounce() {
        long now = System.currentTimeMillis();
        if (now - lastEnterTime < ENTER_DEBOUNCE_MS) {
            return true;
        }
        lastEnterTime = now;
        return false;
    }

    // Прячет клавиатуру
    private void hideKeyboardDirectly() {
        if (getView() != null && getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    // Показывает клавиатуру для текущего активного поля
    private void showKeyboardForCurrentStep() {
        EditText target = isSecondStep ? etNewPassword : etOldPassword;
        if (target != null && getContext() != null) {
            target.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}