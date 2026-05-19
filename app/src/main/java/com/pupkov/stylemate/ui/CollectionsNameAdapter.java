package com.pupkov.stylemate.ui;

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

import com.pupkov.stylemate.R;

/**
 * Выпадающий адаптер-список для переключения активной коллекции
 */
public class CollectionsNameAdapter extends RecyclerView.Adapter<CollectionsNameAdapter.ViewHolder> {

    private List<String> collectionNames;
    private final OnItemClickListener listener;

    private boolean isExpanded = false;
    private String selectedName;

    private final int COLOR_BLUE = Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = Color.parseColor("#595959");

    /**
     * Слушатель кликов. Передает имя выбранной коллекции, либо null, если требуется просто изменить состояние раскрытия.
     */
    public interface OnItemClickListener {
        void onItemClick(String name);
    }

    public CollectionsNameAdapter(List<String> collectionNames, String initialName, OnItemClickListener listener) {
        this.collectionNames = collectionNames;
        this.selectedName = initialName;
        this.listener = listener;
    }

    /**
     * Переключение режима отображения (Свернут / Развернут).
     */
    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
        notifyDataSetChanged();
    }

    /**
     * Смена текущего выбранного элемента с обновлением UI.
     */
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
        String currentItemName;

        // формирование пустой "заглушки", если в раскрытом списке всего 1 элемент
        if (isExpanded && collectionNames.size() == 1 && position == 1) {
            holder.tvCollectionName.setText(" ");
            holder.arrowContainer.setVisibility(View.GONE);
            holder.lineSeparator.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
            return;
        }

        if (!isExpanded) {
            // Режим: Закрытый список (отображается только выбранный элемент)
            currentItemName = selectedName;

            holder.tvCollectionName.setTextColor(COLOR_GRAY);
            holder.lineSeparator.setBackgroundColor(COLOR_GRAY);
            holder.arrowContainer.setVisibility(View.VISIBLE);
            holder.ivArrow.setColorFilter(COLOR_GRAY);
            holder.ivArrow.setRotation(0f); // Стрелка вниз

        } else {
            // Режим: Раскрытый список со всеми доступными элементами
            currentItemName = collectionNames.get(position);

            if (currentItemName.equals(selectedName)) {
                holder.tvCollectionName.setTextColor(COLOR_BLUE);
                holder.lineSeparator.setBackgroundColor(COLOR_BLUE);
            } else {
                holder.tvCollectionName.setTextColor(COLOR_GRAY);
                holder.lineSeparator.setBackgroundColor(COLOR_GRAY);
            }

            // Управление триггером-стрелкой на первой позиции раскрытого списка
            if (position == 0) {
                holder.arrowContainer.setVisibility(View.VISIBLE);
                holder.ivArrow.setColorFilter(COLOR_BLUE);
                holder.ivArrow.setRotation(180f); // Поворот стрелки вверх
            } else {
                holder.arrowContainer.setVisibility(View.GONE);
            }
        }

        holder.tvCollectionName.setText(currentItemName);

        holder.itemView.setOnClickListener(v -> {
            if (!isExpanded) {
                listener.onItemClick(null);
            } else {
                listener.onItemClick(currentItemName);
            }
        });

        holder.arrowContainer.setOnClickListener(v -> listener.onItemClick(null));
    }

    @Override
    public int getItemCount() {
        if (!isExpanded) {
            return 1;
        }

        // Искусственное расширение контейнера для корректного отображения нижней границы/тени
        if (collectionNames.size() == 1) {
            return 2;
        }

        return collectionNames.size();
    }

    public void updateList(List<String> newList) {
        this.collectionNames = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCollectionName;
        View lineSeparator;
        FrameLayout arrowContainer;
        ImageView ivArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCollectionName = itemView.findViewById(R.id.tvCollectionName);
            lineSeparator = itemView.findViewById(R.id.lineSeparator);
            arrowContainer = itemView.findViewById(R.id.flArrowContainer);
            ivArrow = itemView.findViewById(R.id.ivItemArrow);
        }
    }
}