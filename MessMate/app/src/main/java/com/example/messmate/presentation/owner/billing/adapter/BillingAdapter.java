package com.example.messmate.presentation.owner.billing.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.owner.billing.model.BillingData;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class BillingAdapter extends RecyclerView.Adapter<BillingAdapter.BillingViewHolder> {

    public interface OnGenerateBillListener {
        void onGenerateBill(BillingData data);
    }

    private final List<BillingData> billingList;

    private final OnGenerateBillListener listener;


    public BillingAdapter(List<BillingData> billingList, OnGenerateBillListener listener) {

        this.billingList = billingList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public BillingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_billing, parent, false);

        return new BillingViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BillingViewHolder holder, int position) {

        BillingData data = billingList.get(position);


        holder.txtMemberName.setText(data.memberName);


        holder.txtMemberEmail.setText(data.email);


        if (data.memberName != null && !data.memberName.trim().isEmpty()) {

            holder.txtInitial.setText(data.memberName.substring(0, 1).toUpperCase());

        } else {

            holder.txtInitial.setText("M");
        }


        holder.txtFullTiffins.setText(String.valueOf(data.fullTiffins));


        holder.txtHalfTiffins.setText(String.valueOf(data.halfTiffins));


        holder.txtTotalUnits.setText(String.format(Locale.getDefault(), "%.1f", data.totalUnits));


        holder.txtTotalAmount.setText(String.format(Locale.getDefault(), "₹ %.2f", data.totalAmount));


        holder.btnGenerateBill.setOnClickListener(v -> listener.onGenerateBill(data));
    }


    @Override
    public int getItemCount() {

        return billingList.size();
    }


    static class BillingViewHolder extends RecyclerView.ViewHolder {

        TextView txtInitial;
        TextView txtMemberName;
        TextView txtMemberEmail;

        TextView txtFullTiffins;
        TextView txtHalfTiffins;
        TextView txtTotalUnits;
        TextView txtTotalAmount;

        MaterialButton btnGenerateBill;


        BillingViewHolder(@NonNull View itemView) {

            super(itemView);


            txtInitial = itemView.findViewById(R.id.txtInitial);


            txtMemberName = itemView.findViewById(R.id.txtMemberName);


            txtMemberEmail = itemView.findViewById(R.id.txtMemberEmail);


            txtFullTiffins = itemView.findViewById(R.id.txtFullTiffins);


            txtHalfTiffins = itemView.findViewById(R.id.txtHalfTiffins);


            txtTotalUnits = itemView.findViewById(R.id.txtTotalUnits);


            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);


            btnGenerateBill = itemView.findViewById(R.id.btnGenerateBill);
        }
    }
}
