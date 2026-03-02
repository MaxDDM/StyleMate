package com.example.stylemate.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.stylemate.R;
import com.example.stylemate.model.Product;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> items;

    public ProductAdapter(List<Product> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice());
        holder.tvBrand.setText(item.getBrand());
        holder.tvMaterial.setText(item.getMaterial());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice, tvBrand, tvMaterial;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProdName);
            tvPrice = itemView.findViewById(R.id.tvProdPrice);
            tvBrand = itemView.findViewById(R.id.tvProdBrand);
            tvMaterial = itemView.findViewById(R.id.tvProdMaterial);
        }
    }
}