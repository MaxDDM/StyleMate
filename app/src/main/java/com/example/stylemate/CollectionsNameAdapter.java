package com.example.stylemate;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class CollectionsNameAdapter extends RecyclerView.Adapter<CollectionsNameAdapter.ViewHolder> {

    private final List<String> collectionNames;
    private final OnItemClickListener listener;

    // --- СОСТОЯНИЕ ---
    private boolean isExpanded = false; // Развернут список или нет
    private String selectedName;        // Текущее выбранное имя

    // Цвета
    private final int COLOR_BLUE = Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = Color.parseColor("#595959");

    public interface OnItemClickListener {
        void onItemClick(String name);
    }

    public CollectionsNameAdapter(List<String> collectionNames, String initialName, OnItemClickListener listener) {
        this.collectionNames = collectionNames;
        this.selectedName = initialName;
        this.listener = listener;
    }

    // Метод для переключения режима (Свернут/Развернут)
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        notifyDataSetChanged(); // Полная перерисовка
    }

    // Обновляем выбранный элемент
    public void setSelectedName(String name) {
        this.selectedName = name;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection_name_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // ЛОГИКА ОТОБРАЖЕНИЯ ДАННЫХ
        String currentItemName;

        // 2. ОБРАБОТКА "ПРИЗРАЧНОГО" ВТОРОГО ЭЛЕМЕНТА
        // Проверяем: если список развернут, в нем 1 элемент, а мы сейчас рисуем позицию №1 (вторую строку)
        if (isExpanded && collectionNames.size() == 1 && position == 1) {
            // Настраиваем пустышку
            holder.tvCollectionName.setText(" "); // Пробел, чтобы сохранилась высота строки
            holder.arrowContainer.setVisibility(View.GONE); // Скрываем стрелку
            holder.lineSeparator.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null); // Убираем клик
            holder.itemView.setClickable(false);      // Отключаем нажатие
            return; // Выходим из метода, чтобы не выполнялся код ниже
        }

        if (!isExpanded) {
            // РЕЖИМ 1: СВЕРНУТО
            // Показываем ВСЕГДА только выбранное имя (оно будет единственным элементом)
            currentItemName = selectedName;

            // Вид: Серый (неактивный), так как список закрыт
            holder.tvCollectionName.setTextColor(COLOR_GRAY);
            holder.lineSeparator.setBackgroundColor(COLOR_GRAY);
            holder.arrowContainer.setVisibility(View.VISIBLE);
            holder.ivArrow.setColorFilter(COLOR_GRAY);
            holder.ivArrow.setRotation(0f);

        } else {
            // РЕЖИМ 2: РАЗВЕРНУТО
            // Берем реальное имя из списка по позиции
            currentItemName = collectionNames.get(position);

            // Вид: Если это выбранный элемент -> Синий, иначе -> Серый
            if (currentItemName.equals(selectedName)) {
                holder.tvCollectionName.setTextColor(COLOR_BLUE);
                holder.lineSeparator.setBackgroundColor(COLOR_BLUE);
            } else {
                holder.tvCollectionName.setTextColor(COLOR_GRAY);
                holder.lineSeparator.setBackgroundColor(COLOR_GRAY);
            }

            if (position == 0) {
                // У первого элемента: Стрелка есть, Синий цвет, Смотрит вверх
                holder.arrowContainer.setVisibility(View.VISIBLE);
                holder.ivArrow.setColorFilter(COLOR_BLUE);
                holder.ivArrow.setRotation(180f);
            } else {
                // У остальных: Стрелки нет
                holder.arrowContainer.setVisibility(View.GONE);
            }
        }

        holder.tvCollectionName.setText(currentItemName);

        // ОБРАБОТКА КЛИКА
        holder.itemView.setOnClickListener(v -> {
            if (!isExpanded) {
                // Если нажали на свернутый элемент -> просим Фрагмент открыть список
                listener.onItemClick(null); // null означает "просто открой"
            } else {
                // Если нажали в открытом списке -> выбираем элемент
                listener.onItemClick(currentItemName);
            }
        });

        // Клик по стрелке (она маленькая, лучше вешать клик на контейнер)
        holder.arrowContainer.setOnClickListener(v -> {
            // Клик по стрелке всегда работает как переключатель (открыть/закрыть)
            // В данном контексте можно просто передать null, фрагмент поймет
            listener.onItemClick(null);
        });
    }

    @Override
    public int getItemCount() {
        if (!isExpanded) {
            return 1; // Свернуто -> всегда 1 (заголовок)
        }

        // Развернуто:
        if (collectionNames.size() == 1) {
            return 2; // ХИТРОСТЬ: Если реальный элемент один, говорим, что их два
        }

        return collectionNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCollectionName;
        View lineSeparator;
        FrameLayout arrowContainer; // Наш кружок
        ImageView ivArrow;          // Сама картинка

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCollectionName = itemView.findViewById(R.id.tvCollectionName);
            lineSeparator = itemView.findViewById(R.id.lineSeparator);
            arrowContainer = itemView.findViewById(R.id.flArrowContainer);
            ivArrow = itemView.findViewById(R.id.ivItemArrow);
        }
    }
}