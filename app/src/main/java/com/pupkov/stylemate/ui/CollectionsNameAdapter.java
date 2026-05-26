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
public class CollectionsNameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Определяем константы для типов ячеек
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_CREATE_BUTTON = 1;
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
        void onCreateNewClick();
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

    @Override
    public int getItemViewType(int position) {
        // Если список раскрыт и мы дошли до последнего элемента
        if (isExpanded && position == collectionNames.size()) {
            return TYPE_CREATE_BUTTON;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (!isExpanded) {
            return 1;
        }
        return collectionNames.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_CREATE_BUTTON) {
            View view = inflater.inflate(R.layout.item_collection_create, parent, false);
            return new CreateButtonViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_collection_name_list, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_CREATE_BUTTON) {
            // Логика для кнопки "Создать подборку"
            CreateButtonViewHolder createHolder = (CreateButtonViewHolder) holder;
            createHolder.itemView.setOnClickListener(v -> listener.onCreateNewClick());
        }
        else {
            // Логика для обычной подборки
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            String currentItemName;

            if (!isExpanded) {
                // Закрытое состояние
                currentItemName = selectedName;
                itemHolder.tvCollectionName.setTextColor(COLOR_GRAY);
                itemHolder.arrowContainer.setVisibility(View.VISIBLE);
                itemHolder.ivArrow.setColorFilter(COLOR_GRAY);
                itemHolder.ivArrow.setRotation(0f); // Стрелка вниз
            } else {
                // Открытое состояние
                currentItemName = collectionNames.get(position);

                if (currentItemName.equals(selectedName)) {
                    itemHolder.tvCollectionName.setTextColor(COLOR_BLUE);
                } else {
                    itemHolder.tvCollectionName.setTextColor(COLOR_GRAY);
                }

                // Стрелка вверх только у первого элемента
                if (position == 0) {
                    itemHolder.arrowContainer.setVisibility(View.VISIBLE);
                    itemHolder.ivArrow.setColorFilter(COLOR_BLUE);
                    itemHolder.ivArrow.setRotation(180f);
                } else {
                    itemHolder.arrowContainer.setVisibility(View.GONE);
                }
            }

            itemHolder.tvCollectionName.setText(currentItemName);

            // Клики по тексту
            itemHolder.itemView.setOnClickListener(v -> {
                if (!isExpanded) {
                    listener.onItemClick(null); // Просто раскрываем
                } else {
                    listener.onItemClick(currentItemName); // Выбираем подборку
                }
            });

        }
    }

    public void updateList(List<String> newList) {
        this.collectionNames = newList;
        notifyDataSetChanged();
    }

    // 1. Холдер для обычного текста подборки
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvCollectionName;
        FrameLayout arrowContainer;
        ImageView ivArrow;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCollectionName = itemView.findViewById(R.id.tvCollectionName);
            arrowContainer = itemView.findViewById(R.id.flArrowContainer);
            ivArrow = itemView.findViewById(R.id.ivItemArrow);
        }
    }

    // 2. Холдер для кнопки "Создать подборку"
    public static class CreateButtonViewHolder extends RecyclerView.ViewHolder {
        public CreateButtonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}