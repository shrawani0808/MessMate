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

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    // =====================================================
    // VARIABLES
    // =====================================================

    private final List<Member> memberList;

    private final OnDeleteClickListener deleteClickListener;

    private final OnUpdateClickListener updateClickListener;


    // =====================================================
    // INTERFACES
    // =====================================================

    public interface OnDeleteClickListener {
        void onDelete(Member member);
    }


    public interface OnUpdateClickListener {
        void onUpdate(Member member);
    }


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public MemberAdapter(
            List<Member> memberList,
            OnDeleteClickListener deleteClickListener,
            OnUpdateClickListener updateClickListener) {

        this.memberList = memberList;

        this.deleteClickListener = deleteClickListener;

        this.updateClickListener = updateClickListener;
    }


    // =====================================================
    // CREATE VIEW HOLDER
    // =====================================================

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_member,
                parent,
                false
        );

        return new MemberViewHolder(view);
    }


    // =====================================================
    // BIND VIEW HOLDER
    // =====================================================

    @Override
    public void onBindViewHolder(
            @NonNull MemberViewHolder holder,
            int position) {

        Member member = memberList.get(position);

        if (member == null) {
            return;
        }


        // =================================================
        // MEMBER NAME
        // =================================================

        String name = member.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "Member";
        }

        holder.txtMemberName.setText(name);


        // =================================================
        // PHONE
        // =================================================

        String phone = member.getPhone();

        if (phone == null || phone.trim().isEmpty()) {
            phone = "No phone number";
        }

        holder.txtMemberPhone.setText(phone);


        // =================================================
        // EMAIL
        // =================================================

        String email = member.getEmail();

        if (email == null || email.trim().isEmpty()) {
            email = "No email address";
        }

        holder.txtMemberEmail.setText(email);


        // =================================================
        // MEMBER INITIAL
        // =================================================

        String initial = "M";

        if (name != null && !name.trim().isEmpty()) {

            initial = name
                    .trim()
                    .substring(0, 1)
                    .toUpperCase();
        }

        holder.txtInitial.setText(initial);


        // =================================================
        // PAYMENT TYPE
        // =================================================

        String paymentType = member.getPaymentType();

        if (paymentType == null ||
                paymentType.trim().isEmpty()) {

            paymentType = "daily";
        }


        // =================================================
        // MEAL INFORMATION
        // =================================================

        String mealText = getMealText(member);


        // =================================================
        // PLAN TEXT
        // =================================================

        String planText;

        if ("monthly".equalsIgnoreCase(paymentType)) {

            planText =
                    "Monthly • " + mealText;

        } else {

            planText =
                    "Daily • " + mealText;
        }


        holder.txtMemberPlan.setText(planText);


        // =================================================
        // DELETE BUTTON
        // =================================================

        holder.btnDeleteMember.setOnClickListener(
                v -> {

                    if (deleteClickListener != null) {

                        deleteClickListener.onDelete(member);
                    }
                }
        );


        // =================================================
        // UPDATE MEMBER
        // =================================================

        /*
         * There is no separate edit button in your
         * item_member.xml.
         *
         * Therefore the complete member card is clickable.
         */

        holder.itemView.setOnClickListener(
                v -> {

                    if (updateClickListener != null) {

                        updateClickListener.onUpdate(member);
                    }
                }
        );
    }


    // =====================================================
    // GET MEAL TEXT
    // =====================================================

    private String getMealText(Member member) {

        boolean lunch = member.isLunchEnabled();

        boolean dinner = member.isDinnerEnabled();


        if (lunch && dinner) {

            return "Lunch + Dinner";

        } else if (lunch) {

            return "Lunch";

        } else if (dinner) {

            return "Dinner";

        } else {

            return "No Meal";
        }
    }


    // =====================================================
    // GET ITEM COUNT
    // =====================================================

    @Override
    public int getItemCount() {

        if (memberList == null) {
            return 0;
        }

        return memberList.size();
    }


    // =====================================================
    // VIEW HOLDER
    // =====================================================

    static class MemberViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtInitial;

        TextView txtMemberName;

        TextView txtMemberPhone;

        TextView txtMemberEmail;

        TextView txtMemberPlan;

        ImageButton btnDeleteMember;


        public MemberViewHolder(
                @NonNull View itemView) {

            super(itemView);


            // =============================================
            // INITIAL
            // =============================================

            txtInitial =
                    itemView.findViewById(
                            R.id.txtInitial
                    );


            // =============================================
            // NAME
            // =============================================

            txtMemberName =
                    itemView.findViewById(
                            R.id.txtMemberName
                    );


            // =============================================
            // PHONE
            // =============================================

            txtMemberPhone =
                    itemView.findViewById(
                            R.id.txtMemberPhone
                    );


            // =============================================
            // EMAIL
            // =============================================

            txtMemberEmail =
                    itemView.findViewById(
                            R.id.txtMemberEmail
                    );


            // =============================================
            // PLAN
            // =============================================

            txtMemberPlan =
                    itemView.findViewById(
                            R.id.txtMemberPlan
                    );


            // =============================================
            // DELETE
            // =============================================

            btnDeleteMember =
                    itemView.findViewById(
                            R.id.btnDeleteMember
                    );
        }
    }
}