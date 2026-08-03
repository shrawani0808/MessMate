package com.example.messmate.presentation.owner.members.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.owner.members.model.Member;

import java.util.List;

public class MemberAdapter
        extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(Member member);
    }

    private final List<Member> memberList;
    private final OnDeleteClickListener deleteListener;

    public MemberAdapter(
            List<Member> memberList,
            OnDeleteClickListener deleteListener) {

        this.memberList = memberList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_member,
                        parent,
                        false
                );

        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MemberViewHolder holder,
            int position) {

        Member member = memberList.get(position);

        holder.txtMemberName.setText(member.getName());

        holder.txtMemberPhone.setText(
                member.getPhone()
        );

        holder.txtMemberEmail.setText(
                member.getEmail()
        );


        String name = member.getName();

        if (name != null && !name.trim().isEmpty()) {

            holder.txtInitial.setText(
                    name.substring(0, 1).toUpperCase()
            );

        } else {

            holder.txtInitial.setText("M");
        }


        holder.btnDeleteMember.setOnClickListener(
                v -> deleteListener.onDelete(member)
        );
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }


    static class MemberViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtInitial;
        TextView txtMemberName;
        TextView txtMemberPhone;
        TextView txtMemberEmail;

        ImageButton btnDeleteMember;

        public MemberViewHolder(@NonNull View itemView) {

            super(itemView);

            txtInitial =
                    itemView.findViewById(
                            R.id.txtInitial
                    );

            txtMemberName =
                    itemView.findViewById(
                            R.id.txtMemberName
                    );

            txtMemberPhone =
                    itemView.findViewById(
                            R.id.txtMemberPhone
                    );

            txtMemberEmail =
                    itemView.findViewById(
                            R.id.txtMemberEmail
                    );

            btnDeleteMember =
                    itemView.findViewById(
                            R.id.btnDeleteMember
                    );
        }
    }
}