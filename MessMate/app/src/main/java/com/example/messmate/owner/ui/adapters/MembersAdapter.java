package com.example.messmate.owner.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.common.models.MemberModel;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {

    private final List<MemberModel> memberModelList;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(MemberModel memberModel);
    }

    public MembersAdapter(List<MemberModel> memberModelList) {
        this.memberModelList = memberModelList;
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        MemberModel memberModel = memberModelList.get(position);

        holder.tvMemberName.setText(memberModel.getName());
        holder.tvPlanType.setText(memberModel.getPlanType());
        holder.tvMemberStatus.setText(memberModel.getStatus());

        if (memberModel.getImageResId() != 0) {
            holder.imgMemberAvatar.setImageResource(memberModel.getImageResId());
        }

        // Apply Active / Inactive status colors dynamically
        if (memberModel.isActive()) {
            holder.tvMemberStatus.setTextColor(Color.parseColor("#2E7D32")); // Green
        } else {
            holder.tvMemberStatus.setTextColor(Color.parseColor("#757575")); // Grey
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMemberClick(memberModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberModelList != null ? memberModelList.size() : 0;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView imgMemberAvatar;
        TextView tvMemberName, tvPlanType, tvMemberStatus;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMemberAvatar = itemView.findViewById(R.id.imgMemberAvatar);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvPlanType = itemView.findViewById(R.id.tvPlanType);
            tvMemberStatus = itemView.findViewById(R.id.tvMemberStatus);
        }
    }
}