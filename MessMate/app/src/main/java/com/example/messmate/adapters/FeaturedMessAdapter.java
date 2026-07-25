package com.example.messmate.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.messmate.R;
import com.example.messmate.models.MessModel;
import java.util.List;

public class FeaturedMessAdapter extends RecyclerView.Adapter<FeaturedMessAdapter.ViewHolder> {

    private Context context;
    private List<MessModel> featuredList;
    private OnMessClickListener listener;

    public interface OnMessClickListener {
        void onMessClick(MessModel mess);
    }

    public FeaturedMessAdapter(Context context, List<MessModel> featuredList, OnMessClickListener listener) {
        this.context = context;
        this.featuredList = featuredList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_mess, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessModel mess = featuredList.get(position);
        holder.tvName.setText(mess.getName());
        holder.tvRating.setText("★ " + mess.getRating());
        holder.tvPrice.setText("₹" + mess.getPricePerMonth() + " / month");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMessClick(mess);
        });
    }

    @Override
    public int getItemCount() {
        return featuredList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRating, tvPrice;
        ImageView imgMess;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFeaturedName);
            tvRating = itemView.findViewById(R.id.tvFeaturedRating);
            tvPrice = itemView.findViewById(R.id.tvFeaturedPrice);
            imgMess = itemView.findViewById(R.id.imgFeaturedMess);
        }
    }
}