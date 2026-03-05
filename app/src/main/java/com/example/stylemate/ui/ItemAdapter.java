package com.example.stylemate.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stylemate.R;
import com.example.stylemate.model.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items = new ArrayList<>();
    private final OnItemClickListener listener;

    // Интерфейс для клика
    public interface OnItemClickListener {
        void onItemClick(String url);
    }

    public ItemAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);

        holder.tvName.setText(item.getType());      // "Футболка"
        holder.tvBrand.setText(item.getBrand());    // "Sela"
        holder.tvPrice.setText(item.getPrice());    // "2399 Р"
        holder.tvMaterial.setText(item.getMaterial()); // "хлопок"

        // Обработка клика по всему элементу (или можно только по скрепке)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item.getLink() != null && !item.getLink().isEmpty()) {
                listener.onItemClick(item.getLink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvBrand, tvMaterial;
        // ImageView ivLink; // Если хочешь кликать именно по скрепке, найди её здесь

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProdName);
            tvPrice = itemView.findViewById(R.id.tvProdPrice);
            tvBrand = itemView.findViewById(R.id.tvProdBrand);
            tvMaterial = itemView.findViewById(R.id.tvProdMaterial);
        }
    }
}