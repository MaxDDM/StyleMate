package com.example.stylemate.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Импорт Glide
import com.example.stylemate.R;
import com.example.stylemate.model.Outfit;
import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private List<Outfit> items;
    private final OnOutfitClickListener listener;
    private final Context context; // Нужен контекст для Glide

    private final int COLOR_BLUE = Color.parseColor("#3D7DFF");
    private final int COLOR_GRAY = Color.parseColor("#5C5C5C");

    public interface OnOutfitClickListener {
        void onHeartClick(Outfit outfit, int position);
        void onImageClick(Outfit outfit);
    }

    // Добавили Context в конструктор
    public OutfitAdapter(Context context, List<Outfit> items, OnOutfitClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

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

        // 1. ЗАГРУЗКА КАРТИНКИ ЧЕРЕЗ GLIDE
        Glide.with(context)
                .load(item.getImageUrl()) // Ссылка из Firebase
                .placeholder(R.drawable.ic_launcher_background) // Заглушка пока грузится (создай или выбери свою)
                .into(holder.imageView);

        // 2. Сердце
        holder.btnLike.setImageResource(R.drawable.ic_heart_outline); // Убедись, что иконка есть
        if (item.isLiked()) {
            holder.btnLike.setColorFilter(COLOR_BLUE);
        } else {
            holder.btnLike.setColorFilter(COLOR_GRAY);
        }

        // 3. Клики
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