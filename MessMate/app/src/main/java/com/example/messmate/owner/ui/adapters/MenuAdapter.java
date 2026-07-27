package com.example.messmate.owner.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.common.models.MenuCategoryModel;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final List<MenuCategoryModel> menuList;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(MenuCategoryModel category);
    }

    public MenuAdapter(List<MenuCategoryModel> menuList) {
        this.menuList = menuList;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_category, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuCategoryModel category = menuList.get(position);

        holder.tvCategoryName.setText(category.getCategoryName());
        holder.tvMenuItems.setText(category.getItemsDescription());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList != null ? menuList.size() : 0;
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategoryName, tvMenuItems;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvMenuItems = itemView.findViewById(R.id.tvMenuItems);
        }
    }
}