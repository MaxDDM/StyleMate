package com.example.stylemate.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.stylemate.R;
import com.example.stylemate.model.CollectionDetailViewModel;

public class CollectionDetailActivity extends AppCompatActivity {

    // Храним текущий заголовок в переменной, чтобы его менять
    private CollectionDetailViewModel viewModel;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail);

        // 1. Подключаем ViewModel
        viewModel = new ViewModelProvider(this).get(CollectionDetailViewModel.class);

        // 2. Получаем данные из Intent и отдаем их во ViewModel
        if (savedInstanceState == null) {
            String titleFromIntent = getIntent().getStringExtra("COLLECTION_TITLE");
            if (titleFromIntent != null) {
                viewModel.setInitialTitle(titleFromIntent);
            }
        }

        initViews();
        observeViewModel();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnEdit = findViewById(R.id.btnEdit);
        ImageButton btnDelete = findViewById(R.id.btnDelete);
        tvTitle = findViewById(R.id.tvCollectionTitle);

        btnBack.setOnClickListener(v -> finish());

        // === КНОПКА РЕДАКТИРОВАТЬ ===
        btnEdit.setOnClickListener(v -> {
            // Берем текущее название ПРЯМО из ViewModel
            String currentTitle = viewModel.title.getValue();

            EditCollectionBottomSheet bottomSheet = EditCollectionBottomSheet.newInstance(currentTitle);

            // Когда диалог вернет новое имя -> передаем его во ViewModel
            bottomSheet.setListener(newTitle -> {
                viewModel.onCollectionRenamed(newTitle);
            });

            bottomSheet.show(getSupportFragmentManager(), "EditCollectionTag");
        });

        // === КНОПКА УДАЛИТЬ ===
        btnDelete.setOnClickListener(v -> {
            DeleteCollectionDialog dialog = new DeleteCollectionDialog();

            // Когда диалог подтвердит удаление -> сообщаем ViewModel
            dialog.setListener(() -> {
                viewModel.onCollectionDeleted();
            });

            dialog.show(getSupportFragmentManager(), "DeleteDialog");
        });
    }

    private void observeViewModel() {
        // А. Следим за заголовком
        viewModel.title.observe(this, newTitle -> {
            tvTitle.setText(newTitle);
        });

        // Б. Следим за командой "Закрыть экран"
        viewModel.closeScreenEvent.observe(this, shouldClose -> {
            if (shouldClose) {
                finish();
            }
        });

        // В. Следим за тостами
        viewModel.toastMessage.observe(this, message -> {
            // Небольшой хак, чтобы тост не показывался повторно при повороте экрана,
            // но для простых приложений это ок.
            if (message != null && !message.isEmpty()) {
                CustomToast.show(this, message);
            }
        });
    }
}