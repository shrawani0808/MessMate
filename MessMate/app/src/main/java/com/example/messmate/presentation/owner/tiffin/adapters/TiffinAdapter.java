package com.example.messmate.presentation.owner.tiffin.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.owner.members.model.Member;
import com.example.messmate.presentation.owner.tiffin.modules.TiffinRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TiffinAdapter
        extends RecyclerView.Adapter<TiffinAdapter.TiffinViewHolder> {

    // =========================================================
    // CALLBACK
    // =========================================================

    public interface OnStatusClickListener {

        void onLunchStatusClicked(
                Member member,
                String selectedStatus
        );

        void onDinnerStatusClicked(
                Member member,
                String selectedStatus
        );
    }

    // =========================================================
    // VARIABLES
    // =========================================================

    private final List<Member> memberList;

    private final OnStatusClickListener listener;

    /*
     * Key:
     *     memberDocumentId
     *
     * Value:
     *     TiffinRecord
     *
     * Example:
     *
     * ABC123 -> today's TiffinRecord
     */
    private Map<String, TiffinRecord> todayRecords =
            new HashMap<>();

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public TiffinAdapter(
            List<Member> memberList,
            OnStatusClickListener listener) {

        this.memberList = memberList;
        this.listener = listener;
    }

    // =========================================================
    // SET TODAY RECORDS
    // =========================================================

    public void setTodayRecords(
            Map<String, TiffinRecord> records) {

        if (records == null) {

            todayRecords = new HashMap<>();

        } else {

            todayRecords =
                    new HashMap<>(records);
        }

        notifyDataSetChanged();
    }

    // =========================================================
    // CREATE VIEW HOLDER
    // =========================================================

    @NonNull
    @Override
    public TiffinViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_tiffin_collection,
                        parent,
                        false
                );

        return new TiffinViewHolder(view);
    }

    // =========================================================
    // BIND VIEW HOLDER
    // =========================================================

    @Override
    public void onBindViewHolder(
            @NonNull TiffinViewHolder holder,
            int position) {

        Member member =
                memberList.get(position);

        Context context =
                holder.itemView.getContext();

        // =====================================================
        // MEMBER NAME
        // =====================================================

        String name =
                member.getName();

        if (name == null ||
                name.trim().isEmpty()) {

            name = "Member";
        }

        holder.txtMemberName.setText(name);

        // =====================================================
        // MEMBER INITIAL
        // =====================================================

        String trimmedName =
                name.trim();

        String initial =
                trimmedName
                        .substring(
                                0,
                                1
                        )
                        .toUpperCase(
                                Locale.getDefault()
                        );

        holder.txtInitial.setText(initial);

        // =====================================================
        // PAYMENT TYPE
        // =====================================================

        boolean monthly =
                "monthly".equalsIgnoreCase(
                        member.getPaymentType()
                );

        if (monthly) {

            // -------------------------------------------------
            // MONTHLY PACKAGE
            // -------------------------------------------------

            holder.txtPackageType.setText(
                    "Monthly Package"
            );

            holder.txtPackageType.setTextColor(
                    ContextCompat.getColor(
                            context,
                            R.color.primary
                    )
            );

            holder.txtPackageType.setBackgroundResource(
                    R.drawable.bg_monthly_package
            );

            holder.txtInitial.setTextColor(
                    ContextCompat.getColor(
                            context,
                            R.color.primary
                    )
            );

            holder.txtInitial.setBackgroundResource(
                    R.drawable.bg_monthly_avatar
            );

        } else {

            // -------------------------------------------------
            // DAILY PACKAGE
            // -------------------------------------------------

            holder.txtPackageType.setText(
                    "Daily Package"
            );

            holder.txtPackageType.setTextColor(
                    ContextCompat.getColor(
                            context,
                            R.color.tiffin_orange
                    )
            );

            holder.txtPackageType.setBackgroundResource(
                    R.drawable.bg_daily_package
            );

            holder.txtInitial.setTextColor(
                    ContextCompat.getColor(
                            context,
                            R.color.tiffin_orange
                    )
            );

            holder.txtInitial.setBackgroundResource(
                    R.drawable.bg_daily_avatar
            );
        }

        // =====================================================
        // MEAL VISIBILITY
        // =====================================================

        boolean lunchEnabled =
                member.isLunchEnabled();

        boolean dinnerEnabled =
                member.isDinnerEnabled();

        holder.lunchSection.setVisibility(
                lunchEnabled
                        ? View.VISIBLE
                        : View.GONE
        );

        holder.dinnerSection.setVisibility(
                dinnerEnabled
                        ? View.VISIBLE
                        : View.GONE
        );

        // =====================================================
        // GET TODAY'S TIFFIN RECORD
        // =====================================================

        TiffinRecord todayRecord = null;

        /*
         * Firebase structure:
         *
         * members
         *   └── ABC123
         *
         * tiffinRecords
         *   └── RECORD_ID
         *        memberDocumentId: "ABC123"
         *
         * Therefore we use the member's Firestore
         * document ID to find today's record.
         */

        String memberDocumentId =
                member.getDocumentId();

        if (memberDocumentId != null &&
                !memberDocumentId
                        .trim()
                        .isEmpty()) {

            todayRecord =
                    todayRecords.get(
                            memberDocumentId
                    );
        }

        // =====================================================
        // GET CURRENT LUNCH STATUS
        // =====================================================

        String lunchStatus =
                getLunchStatus(
                        todayRecord
                );

        // =====================================================
        // GET CURRENT DINNER STATUS
        // =====================================================

        String dinnerStatus =
                getDinnerStatus(
                        todayRecord
                );

        // =====================================================
        // RADIO BUTTON COLORS
        // =====================================================

        setupRadioColors(
                holder,
                context
        );

        // =====================================================
        // UPDATE LUNCH UI
        // =====================================================

        setupLunchStatus(
                holder,
                lunchStatus
        );

        // =====================================================
        // UPDATE DINNER UI
        // =====================================================

        setupDinnerStatus(
                holder,
                dinnerStatus
        );

        // =====================================================
        // LUNCH FULL
        // =====================================================

        holder.rbLunchFull.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onLunchStatusClicked(
                                member,
                                "full"
                        );
                    }
                }
        );

        // =====================================================
        // LUNCH HALF
        // =====================================================

        holder.rbLunchHalf.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onLunchStatusClicked(
                                member,
                                "half"
                        );
                    }
                }
        );

        // =====================================================
        // LUNCH NONE
        // =====================================================

        holder.rbLunchNot.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onLunchStatusClicked(
                                member,
                                "none"
                        );
                    }
                }
        );

        // =====================================================
        // DINNER FULL
        // =====================================================

        holder.rbDinnerFull.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onDinnerStatusClicked(
                                member,
                                "full"
                        );
                    }
                }
        );

        // =====================================================
        // DINNER HALF
        // =====================================================

        holder.rbDinnerHalf.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onDinnerStatusClicked(
                                member,
                                "half"
                        );
                    }
                }
        );

        // =====================================================
        // DINNER NONE
        // =====================================================

        holder.rbDinnerNot.setOnClickListener(
                v -> {

                    if (listener != null) {

                        listener.onDinnerStatusClicked(
                                member,
                                "none"
                        );
                    }
                }
        );
    }

    // =========================================================
    // RADIO BUTTON COLORS
    // =========================================================

    private void setupRadioColors(
            TiffinViewHolder holder,
            Context context) {

        int green =
                ContextCompat.getColor(
                        context,
                        R.color.primary
                );

        int orange =
                ContextCompat.getColor(
                        context,
                        R.color.tiffin_orange
                );

        int red =
                Color.rgb(
                        229,
                        57,
                        53
                );

        // -----------------------------------------------------
        // LUNCH
        // -----------------------------------------------------

        holder.rbLunchFull.setButtonTintList(
                createColorStateList(
                        green
                )
        );

        holder.rbLunchHalf.setButtonTintList(
                createColorStateList(
                        orange
                )
        );

        holder.rbLunchNot.setButtonTintList(
                createColorStateList(
                        red
                )
        );

        // -----------------------------------------------------
        // DINNER
        // -----------------------------------------------------

        holder.rbDinnerFull.setButtonTintList(
                createColorStateList(
                        green
                )
        );

        holder.rbDinnerHalf.setButtonTintList(
                createColorStateList(
                        orange
                )
        );

        holder.rbDinnerNot.setButtonTintList(
                createColorStateList(
                        red
                )
        );
    }

    // =========================================================
    // CREATE COLOR STATE LIST
    // =========================================================

    private ColorStateList createColorStateList(
            int color) {

        int[][] states = new int[][]{

                new int[]{
                        android.R.attr.state_checked
                },

                new int[]{}
        };

        int[] colors = new int[]{

                color,
                color
        };

        return new ColorStateList(
                states,
                colors
        );
    }

    // =========================================================
    // GET LUNCH STATUS
    // =========================================================

    private String getLunchStatus(
            TiffinRecord record) {

        if (record == null) {

            return "none";
        }

        /*
         * Firebase:
         *
         * tiffinRecords
         *   └── RECORD_ID
         *        lunchStatus: "full"
         *
         * Possible values:
         *
         * full
         * half
         * none
         */

        String status =
                record.getLunchStatus();

        if (status == null ||
                status.trim().isEmpty()) {

            return "none";
        }

        return status;
    }

    // =========================================================
    // GET DINNER STATUS
    // =========================================================

    private String getDinnerStatus(
            TiffinRecord record) {

        if (record == null) {

            return "none";
        }

        /*
         * Firebase:
         *
         * tiffinRecords
         *   └── RECORD_ID
         *        dinnerStatus: "half"
         *
         * Possible values:
         *
         * full
         * half
         * none
         */

        String status =
                record.getDinnerStatus();

        if (status == null ||
                status.trim().isEmpty()) {

            return "none";
        }

        return status;
    }

    // =========================================================
    // SET LUNCH STATUS UI
    // =========================================================

    private void setupLunchStatus(
            TiffinViewHolder holder,
            String status) {

        holder.rbLunchFull.setChecked(
                "full".equalsIgnoreCase(
                        status
                )
        );

        holder.rbLunchHalf.setChecked(
                "half".equalsIgnoreCase(
                        status
                )
        );

        holder.rbLunchNot.setChecked(
                "none".equalsIgnoreCase(
                        status
                )
        );
    }

    // =========================================================
    // SET DINNER STATUS UI
    // =========================================================

    private void setupDinnerStatus(
            TiffinViewHolder holder,
            String status) {

        holder.rbDinnerFull.setChecked(
                "full".equalsIgnoreCase(
                        status
                )
        );

        holder.rbDinnerHalf.setChecked(
                "half".equalsIgnoreCase(
                        status
                )
        );

        holder.rbDinnerNot.setChecked(
                "none".equalsIgnoreCase(
                        status
                )
        );
    }

    // =========================================================
    // ITEM COUNT
    // =========================================================

    @Override
    public int getItemCount() {

        if (memberList == null) {

            return 0;
        }

        return memberList.size();
    }

    // =========================================================
    // VIEW HOLDER
    // =========================================================

    public static class TiffinViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtInitial;

        TextView txtMemberName;

        TextView txtPackageType;

        View lunchSection;

        View dinnerSection;

        RadioButton rbLunchFull;

        RadioButton rbLunchHalf;

        RadioButton rbLunchNot;

        RadioButton rbDinnerFull;

        RadioButton rbDinnerHalf;

        RadioButton rbDinnerNot;

        public TiffinViewHolder(
                @NonNull View itemView) {

            super(itemView);

            // -------------------------------------------------
            // MEMBER DETAILS
            // -------------------------------------------------

            txtInitial =
                    itemView.findViewById(
                            R.id.txtInitial
                    );

            txtMemberName =
                    itemView.findViewById(
                            R.id.txtMemberName
                    );

            txtPackageType =
                    itemView.findViewById(
                            R.id.txtPackageType
                    );

            // -------------------------------------------------
            // MEAL SECTIONS
            // -------------------------------------------------

            lunchSection =
                    itemView.findViewById(
                            R.id.lunchSection
                    );

            dinnerSection =
                    itemView.findViewById(
                            R.id.dinnerSection
                    );

            // -------------------------------------------------
            // LUNCH RADIO BUTTONS
            // -------------------------------------------------

            rbLunchFull =
                    itemView.findViewById(
                            R.id.rbLunchFull
                    );

            rbLunchHalf =
                    itemView.findViewById(
                            R.id.rbLunchHalf
                    );

            rbLunchNot =
                    itemView.findViewById(
                            R.id.rbLunchNot
                    );

            // -------------------------------------------------
            // DINNER RADIO BUTTONS
            // -------------------------------------------------

            rbDinnerFull =
                    itemView.findViewById(
                            R.id.rbDinnerFull
                    );

            rbDinnerHalf =
                    itemView.findViewById(
                            R.id.rbDinnerHalf
                    );

            rbDinnerNot =
                    itemView.findViewById(
                            R.id.rbDinnerNot
                    );
        }
    }
}