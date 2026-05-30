package com.pupkov.stylemate.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pupkov.stylemate.R;
import com.pupkov.stylemate.model.Outfit;
import java.util.List;

/**
 * Адаптер для управления и связывания данных, отвечающий за отображение карточек образов в сетке
 */
public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private List<Outfit> items;
    private final OnOutfitClickListener listener;
    private final Context context;

    // Хранит позицию элемента в режиме редактирования (-1 означает обычный режим)
    private int editPosition = -1;
    // Флаг для включения/отключения режима редактирования по лонг-прессу (по умолчанию включен)
    private boolean isLongClickEnabled = true;

    private final int COLOR_BLUE = Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = Color.parseColor("#5C5C5C");

    /**
     * Интерфейс обратного вызова для обработки пользовательских событий внутри элементов списка
     */
    public interface OnOutfitClickListener {
        void onHeartClick(Outfit outfit, int position);
        void onImageClick(Outfit outfit);
        default void onDislikeClick(Outfit outfit, int position) {}
    }

    public OutfitAdapter(Context context, List<Outfit> items, OnOutfitClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    /**
     * Метод для принудительного сброса режима редактирования из фрагмента
     */
    public void resetEditMode() {
        this.editPosition = -1;
        notifyDataSetChanged();
    }

    /**
     * Обновляет набор данных адаптера и инициирует полную перерисовку списка.
     */
    public void updateList(List<Outfit> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit, parent, false);
        return new OutfitViewHolder(view);
    }

    public void setLongClickEnabled(boolean enabled) {
        this.isLongClickEnabled = enabled;
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Outfit item = items.get(position);

        // Асинхронное кэширование и потоковая загрузка изображения с эффектом плавного проявления
        Glide.with(context)
                .load(item.getImageUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView);

        // Динамическое управление цветовым фильтром иконки добавления в избранное
        holder.btnLike.setImageResource(R.drawable.ic_heart_outline);
        if (item.isLiked()) {
            holder.btnLike.setColorFilter(COLOR_BLUE);
        } else {
            holder.btnLike.setColorFilter(COLOR_GRAY);
        }

        // Логика отображения режима редактирования
        if (editPosition == -1) {
            // Обычный режим: все карточки яркие, дизлайки скрыты
            holder.itemView.setAlpha(1.0f);
            holder.btnDislike.setVisibility(View.GONE);
        } else {
            if (position == editPosition) {
                // Карточка выбрана лонг-прессом: ВСЕГДА оставляем её яркой (alpha 1.0)
                holder.itemView.setAlpha(1.0f);

                // Проверяем: если лайк стоит, дизлайк НЕ показываем. Если лайка нет — показываем
                if (item.isLiked()) {
                    holder.btnDislike.setVisibility(View.GONE);
                } else {
                    holder.btnDislike.setVisibility(View.VISIBLE);
                }
            } else {
                // Все остальные невыбранные карточки затеняются/забеляются
                holder.itemView.setAlpha(0.4f);
                holder.btnDislike.setVisibility(View.GONE);
            }
        }

        // Привязка LongClick к корневому элементу карточки
        holder.itemView.setOnLongClickListener(v -> {
            if (!isLongClickEnabled) {
                return false; // false означает, что клик не обработан, лонг-пресс не сработает
            }
            int curPos = holder.getAdapterPosition();
            if (curPos != RecyclerView.NO_POSITION) {
                editPosition = curPos;
                notifyDataSetChanged();
            }
            return true;
        });

        // Обработка обычных кликов с учетом текущего режима
        holder.itemView.setOnClickListener(v -> {
            if (editPosition != -1) {
                resetEditMode();
            } else {
                listener.onImageClick(item);
            }
        });

        holder.btnLike.setOnClickListener(v -> {
            int curPos = holder.getBindingAdapterPosition();
            if (curPos != RecyclerView.NO_POSITION) {
                Outfit currentItem = items.get(curPos);
                listener.onHeartClick(currentItem, curPos);
            }
        });

        // Обработка обычного клика на дизлайк
        holder.btnDislike.setOnClickListener(v -> {
            if (editPosition == position) {
                listener.onDislikeClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OutfitViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnLike;
        ImageButton btnDislike;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivOutfitImage);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
        }
    }
}