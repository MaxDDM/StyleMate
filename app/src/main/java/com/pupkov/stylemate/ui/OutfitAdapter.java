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

    private final int COLOR_BLUE = Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = Color.parseColor("#5C5C5C");

    /**
     * Интерфейс обратного вызова для обработки пользовательских событий внутри элементов списка
     */
    public interface OnOutfitClickListener {
        void onHeartClick(Outfit outfit, int position);
        void onImageClick(Outfit outfit);
    }

    public OutfitAdapter(Context context, List<Outfit> items, OnOutfitClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
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

        // Делегирование обработки событий нажатия внешнему слушателю интерфейса
        holder.btnLike.setOnClickListener(v -> listener.onHeartClick(item, position));
        holder.itemView.setOnClickListener(v -> listener.onImageClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OutfitViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btnLike;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivOutfitImage);
            btnLike = itemView.findViewById(R.id.btnLike);
        }
    }
}