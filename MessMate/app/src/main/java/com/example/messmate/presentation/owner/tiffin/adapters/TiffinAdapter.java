package com.example.messmate.presentation.owner.tiffin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.owner.members.model.Member;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class TiffinAdapter
        extends RecyclerView.Adapter<TiffinAdapter.TiffinViewHolder> {


    public interface OnCollectionChangedListener {

        void onCollectionChanged(
                Member member,
                String tiffin,
                boolean dinner
        );
    }


    private final List<Member> memberList;

    private final OnCollectionChangedListener listener;


    public TiffinAdapter(
            List<Member> memberList,
            OnCollectionChangedListener listener) {

        this.memberList = memberList;

        this.listener = listener;
    }


    @NonNull
    @Override
    public TiffinViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_tiffin_collection,
                                parent,
                                false
                        );

        return new TiffinViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull TiffinViewHolder holder,
            int position) {

        Member member =
                memberList.get(position);


        // =====================================================
        // MEMBER INFORMATION
        // =====================================================

        holder.txtMemberName.setText(
                member.getName()
        );


        holder.txtMemberPhone.setText(
                member.getPhone()
        );


        String name = member.getName();


        if (name != null &&
                !name.trim().isEmpty()) {

            holder.txtInitial.setText(
                    name.substring(0, 1)
                            .toUpperCase()
            );

        } else {

            holder.txtInitial.setText("M");
        }


        // =====================================================
        // RESET LISTENER BEFORE RESETTING VALUES
        // =====================================================

        holder.radioTiffin
                .setOnCheckedChangeListener(null);

        holder.switchDinner
                .setOnCheckedChangeListener(null);


        // =====================================================
        // DEFAULT STATE
        // =====================================================

        holder.radioNone.setChecked(true);

        holder.switchDinner.setChecked(false);


        // =====================================================
        // SUBMIT BUTTON
        // =====================================================

        holder.btnSubmit.setOnClickListener(
                v -> {

                    String tiffin = "none";


                    int selectedId =
                            holder.radioTiffin
                                    .getCheckedRadioButtonId();


                    if (selectedId ==
                            R.id.radioFull) {

                        tiffin = "full";

                    } else if (selectedId ==
                            R.id.radioHalf) {

                        tiffin = "half";
                    }


                    boolean dinner =
                            holder.switchDinner.isChecked();


                    listener.onCollectionChanged(
                            member,
                            tiffin,
                            dinner
                    );
                }
        );
    }


    @Override
    public int getItemCount() {

        return memberList.size();
    }


    // =========================================================
    // VIEW HOLDER
    // =========================================================

    static class TiffinViewHolder
            extends RecyclerView.ViewHolder {


        TextView txtInitial;

        TextView txtMemberName;

        TextView txtMemberPhone;


        RadioGroup radioTiffin;

        RadioButton radioFull;

        RadioButton radioHalf;

        RadioButton radioNone;


        MaterialSwitch switchDinner;


        MaterialButton btnSubmit;


        public TiffinViewHolder(
                @NonNull View itemView) {

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


            radioTiffin =
                    itemView.findViewById(
                            R.id.radioTiffin
                    );


            radioFull =
                    itemView.findViewById(
                            R.id.radioFull
                    );


            radioHalf =
                    itemView.findViewById(
                            R.id.radioHalf
                    );


            radioNone =
                    itemView.findViewById(
                            R.id.radioNone
                    );


            switchDinner =
                    itemView.findViewById(
                            R.id.switchDinner
                    );


            btnSubmit =
                    itemView.findViewById(
                            R.id.btnSubmit
                    );
        }
    }
}