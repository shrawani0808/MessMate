package com.example.messmate.presentation.owner.billing;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.messmate.R;
import com.example.messmate.presentation.auth.SessionManager;
import com.example.messmate.presentation.owner.members.model.Member;
import com.example.messmate.presentation.owner.tiffin.modules.TiffinRecord;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BillingFragment extends Fragment {

    private TextView tvBillingMonthSubtitle;
    private TextView tvSelectedMonth;

    private LinearLayout memberContainer;

    private ImageButton btnPreviousMonth;
    private ImageButton btnNextMonth;

    private EditText edtBillingSearch;

    private FirebaseFirestore firestore;
    private SessionManager sessionManager;

    private Calendar selectedMonth;

    private final List<Member> billingMembers = new ArrayList<>();

    // =========================================================
    // COLORS
    // =========================================================

    private final int GREEN = Color.rgb(47, 132, 100);
    private final int LIGHT_GREEN = Color.rgb(235, 247, 241);
    private final int WHITE = Color.WHITE;

    private final int TEXT_PRIMARY = Color.rgb(35, 35, 35);
    private final int TEXT_SECONDARY = Color.rgb(125, 125, 125);
    private final int BORDER = Color.rgb(232, 236, 234);

    private final int FULL_BG = Color.rgb(232, 247, 238);
    private final int FULL_TEXT = Color.rgb(35, 120, 75);

    private final int HALF_BG = Color.rgb(255, 239, 222);
    private final int HALF_TEXT = Color.rgb(225, 105, 20);

    private final int NOT_BG = Color.rgb(255, 232, 232);
    private final int NOT_TEXT = Color.rgb(220, 55, 55);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BillingFragment() {
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_billing, container, false);
    }

    // =========================================================
    // VIEW CREATED
    // =========================================================

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(requireContext());

        selectedMonth = Calendar.getInstance();

        tvBillingMonthSubtitle =
                view.findViewById(R.id.tvBillingMonthSubtitle);

        tvSelectedMonth =
                view.findViewById(R.id.tvSelectedMonth);

        btnPreviousMonth =
                view.findViewById(R.id.btnPreviousMonth);

        btnNextMonth =
                view.findViewById(R.id.btnNextMonth);

        memberContainer =
                view.findViewById(R.id.memberContainer);

        edtBillingSearch =
                view.findViewById(R.id.edtBillingSearch);

        updateMonthText();

        setupSearch();

        loadMembers();

        btnPreviousMonth.setOnClickListener(v -> {

            selectedMonth.add(Calendar.MONTH, -1);

            updateMonthText();

            loadMembers();
        });

        btnNextMonth.setOnClickListener(v -> {

            selectedMonth.add(Calendar.MONTH, 1);

            updateMonthText();

            loadMembers();
        });
    }

    // =========================================================
    // UPDATE MONTH TEXT
    // =========================================================

    private void updateMonthText() {

        String month =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                ).format(selectedMonth.getTime());

        if (tvBillingMonthSubtitle != null) {
            tvBillingMonthSubtitle.setText(month);
        }

        if (tvSelectedMonth != null) {
            tvSelectedMonth.setText(month);
        }
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        edtBillingSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                filterMembers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // =========================================================
    // FILTER
    // =========================================================

    private void filterMembers(String search) {

        String query = search == null
                ? ""
                : search.trim().toLowerCase(Locale.getDefault());

        memberContainer.removeAllViews();

        int matches = 0;

        for (Member member : billingMembers) {

            String name = member.getName() == null
                    ? ""
                    : member.getName().toLowerCase(Locale.getDefault());

            String email = member.getEmail() == null
                    ? ""
                    : member.getEmail().toLowerCase(Locale.getDefault());

            if (query.isEmpty()
                    || name.contains(query)
                    || email.contains(query)) {

                addMemberCard(member);

                matches++;
            }
        }

        if (billingMembers.isEmpty()) {

            showEmptyMessage(
                    "No members found",
                    "Add members first"
            );

        } else if (matches == 0) {

            showEmptyMessage(
                    "No members found",
                    "Try a different name or email"
            );
        }
    }

    // =========================================================
    // LOAD MEMBERS
    // =========================================================

    private void loadMembers() {

        if (!isAdded()) {
            return;
        }

        String ownerId = getOwnerId();

        if (ownerId == null || ownerId.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Owner session not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        memberContainer.removeAllViews();

        billingMembers.clear();

        firestore.collection("members")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!isAdded()) {
                        return;
                    }

                    billingMembers.clear();

                    for (QueryDocumentSnapshot document : snapshot) {

                        Member member = document.toObject(Member.class);

                        member.setDocumentId(document.getId());

                        billingMembers.add(member);
                    }

                    filterMembers(
                            edtBillingSearch.getText().toString()
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            "Failed to load members: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    showEmptyMessage(
                            "Unable to load members",
                            "Please try again"
                    );
                });
    }

    // =========================================================
    // MEMBER CARD
    // =========================================================

    private void addMemberCard(Member member) {

        View cardView = getLayoutInflater().inflate(
                R.layout.item_billing_member,
                memberContainer,
                false
        );

        TextView txtInitial =
                cardView.findViewById(R.id.txtInitial);

        TextView txtMemberName =
                cardView.findViewById(R.id.txtMemberName);

        TextView txtMemberEmail =
                cardView.findViewById(R.id.txtMemberEmail);

        TextView txtMemberRate =
                cardView.findViewById(R.id.txtMemberRate);

        TextView txtTotalUnits =
                cardView.findViewById(R.id.txtTotalUnits);

        TextView txtTotalAmount =
                cardView.findViewById(R.id.txtTotalAmount);

        TextView txtFullTiffins =
                cardView.findViewById(R.id.txtFullTiffins);

        TextView txtHalfTiffins =
                cardView.findViewById(R.id.txtHalfTiffins);

        MaterialButton btnEditRate =
                cardView.findViewById(R.id.btnEditRate);

        MaterialButton btnGenerateBill =
                cardView.findViewById(R.id.btnGenerateBill);

        String name = member.getName();

        if (name == null || name.trim().isEmpty()) {
            name = "Member";
        }

        txtInitial.setText(
                name.substring(0, 1).toUpperCase(Locale.getDefault())
        );

        txtMemberName.setText(name);

        String email = member.getEmail();

        if (email == null || email.trim().isEmpty()) {

            txtMemberEmail.setVisibility(View.GONE);

        } else {

            txtMemberEmail.setVisibility(View.VISIBLE);

            txtMemberEmail.setText(email);
        }

        updateBillingCard(
                member,
                txtMemberRate,
                txtTotalUnits,
                txtTotalAmount,
                txtFullTiffins,
                txtHalfTiffins
        );

        // -----------------------------------------------------
        // EDIT BILLING CONFIGURATION
        // -----------------------------------------------------

        btnEditRate.setOnClickListener(v ->
                showEditBillingDialog(
                        member,
                        txtMemberRate
                )
        );

        // -----------------------------------------------------
        // CALCULATE BILL
        // -----------------------------------------------------

        btnGenerateBill.setOnClickListener(v ->
                calculateMonthlyBill(member)
        );

        // -----------------------------------------------------
        // OPEN TIFFIN CALENDAR
        // -----------------------------------------------------

        final String finalMemberName = name;

        cardView.setOnClickListener(v ->
                showMemberTiffinCalendar(
                        member.getDocumentId(),
                        finalMemberName
                )
        );

        memberContainer.addView(cardView);
    }

    // =========================================================
    // UPDATE CARD
    // =========================================================

    private void updateBillingCard(
            Member member,
            TextView rateText,
            TextView unitsText,
            TextView amountText,
            TextView fullText,
            TextView halfText) {

        String paymentType = normalizePaymentType(
                member.getPaymentType()
        );

        if ("monthly".equals(paymentType)) {

            rateText.setText("Monthly Package");

            double packageAmount = 0;

            if (member.isLunchEnabled()) {
                packageAmount += member.getMonthlyLunchAmount();
            }

            if (member.isDinnerEnabled()) {
                packageAmount += member.getMonthlyDinnerAmount();
            }

            amountText.setText(
                    formatMoney(packageAmount)
            );

            unitsText.setText("Package");

        } else {

            rateText.setText(
                    "Full " + formatMoney(member.getFullRate())
                            + " | Half "
                            + formatMoney(member.getHalfRate())
            );

            unitsText.setText("Daily");

            amountText.setText("Calculated");
        }

        loadCurrentMonthSummary(
                member,
                unitsText,
                amountText,
                fullText,
                halfText
        );
    }

    // =========================================================
    // LOAD CURRENT MONTH SUMMARY
    // =========================================================

    private void loadCurrentMonthSummary(
            Member member,
            TextView unitsText,
            TextView amountText,
            TextView fullText,
            TextView halfText) {

        String ownerId = getOwnerId();

        if (ownerId == null
                || ownerId.isEmpty()
                || member.getDocumentId() == null) {
            return;
        }

        firestore.collection("tiffin_records")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo(
                        "memberDocumentId",
                        member.getDocumentId()
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    int full = 0;
                    int half = 0;

                    double units = 0;

                    double dailyAmount = 0;

                    for (QueryDocumentSnapshot document : snapshot) {

                        TiffinRecord record =
                                document.toObject(TiffinRecord.class);

                        if (!isRecordInSelectedMonth(
                                record.getDate())) {
                            continue;
                        }

                        MealSummary summary =
                                calculateRecordAmount(
                                        record,
                                        member
                                );

                        full += summary.fullMeals;

                        half += summary.halfMeals;

                        units += summary.units;

                        dailyAmount += summary.amount;
                    }

                    fullText.setText(
                            String.valueOf(full)
                    );

                    halfText.setText(
                            String.valueOf(half)
                    );

                    if ("monthly".equals(
                            normalizePaymentType(
                                    member.getPaymentType()))) {

                        unitsText.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%.1f collected",
                                        units
                                )
                        );

                        double packageAmount =
                                calculateMonthlyPackage(member);

                        amountText.setText(
                                formatMoney(packageAmount)
                        );

                    } else {

                        unitsText.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "%.1f units",
                                        units
                                )
                        );

                        amountText.setText(
                                formatMoney(dailyAmount)
                        );
                    }
                });
    }

    // =========================================================
    // CALCULATE MONTHLY BILL
    // =========================================================

    private void calculateMonthlyBill(Member member) {

        if (!isAdded()) {
            return;
        }

        String ownerId = getOwnerId();

        if (ownerId == null || ownerId.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Owner session not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        firestore.collection("tiffin_records")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo(
                        "memberDocumentId",
                        member.getDocumentId()
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    int fullMeals = 0;
                    int halfMeals = 0;

                    int lunchFull = 0;
                    int lunchHalf = 0;

                    int dinnerFull = 0;
                    int dinnerHalf = 0;

                    double totalUnits = 0;

                    double dailyAmount = 0;

                    for (QueryDocumentSnapshot document : snapshot) {

                        TiffinRecord record =
                                document.toObject(TiffinRecord.class);

                        if (!isRecordInSelectedMonth(
                                record.getDate())) {
                            continue;
                        }

                        MealSummary summary =
                                calculateRecordAmount(
                                        record,
                                        member
                                );

                        fullMeals += summary.fullMeals;

                        halfMeals += summary.halfMeals;

                        lunchFull += summary.lunchFull;

                        lunchHalf += summary.lunchHalf;

                        dinnerFull += summary.dinnerFull;

                        dinnerHalf += summary.dinnerHalf;

                        totalUnits += summary.units;

                        dailyAmount += summary.amount;
                    }

                    double packageAmount =
                            calculateMonthlyPackage(member);

                    double finalAmount;

                    if ("monthly".equals(
                            normalizePaymentType(
                                    member.getPaymentType()))) {

                        finalAmount = packageAmount;

                    } else {

                        finalAmount = dailyAmount;
                    }

                    int daysInMonth =
                            selectedMonth.getActualMaximum(
                                    Calendar.DAY_OF_MONTH
                            );

                    showBillDialog(
                            member,
                            fullMeals,
                            halfMeals,
                            lunchFull,
                            lunchHalf,
                            dinnerFull,
                            dinnerHalf,
                            totalUnits,
                            dailyAmount,
                            packageAmount,
                            finalAmount,
                            daysInMonth
                    );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                requireContext(),
                                "Failed to calculate bill: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // RECORD CALCULATION
    // =========================================================

    private MealSummary calculateRecordAmount(
            TiffinRecord record,
            Member member) {

        MealSummary result = new MealSummary();

        // -----------------------------------------------------
        // LUNCH
        // -----------------------------------------------------

        if (member.isLunchEnabled()) {

            String lunch =
                    normalizeStatus(record.getLunchStatus());

            if ("full".equals(lunch)) {

                result.fullMeals++;
                result.lunchFull++;
                result.units += 1;

                if ("daily".equals(
                        normalizePaymentType(
                                member.getPaymentType()))) {

                    result.amount += member.getFullRate();
                }

            } else if ("half".equals(lunch)) {

                result.halfMeals++;
                result.lunchHalf++;
                result.units += 0.5;

                if ("daily".equals(
                        normalizePaymentType(
                                member.getPaymentType()))) {

                    result.amount += member.getHalfRate();
                }
            }
        }

        // -----------------------------------------------------
        // DINNER
        // -----------------------------------------------------

        if (member.isDinnerEnabled()) {

            String dinner =
                    normalizeStatus(record.getDinnerStatus());

            if ("full".equals(dinner)) {

                result.fullMeals++;
                result.dinnerFull++;
                result.units += 1;

                if ("daily".equals(
                        normalizePaymentType(
                                member.getPaymentType()))) {

                    result.amount += member.getFullRate();
                }

            } else if ("half".equals(dinner)) {

                result.halfMeals++;
                result.dinnerHalf++;
                result.units += 0.5;

                if ("daily".equals(
                        normalizePaymentType(
                                member.getPaymentType()))) {

                    result.amount += member.getHalfRate();
                }
            }
        }

        return result;
    }

    // =========================================================
    // MONTHLY PACKAGE
    // =========================================================

    private double calculateMonthlyPackage(Member member) {

        double amount = 0;

        if (member.isLunchEnabled()) {

            amount += member.getMonthlyLunchAmount();
        }

        if (member.isDinnerEnabled()) {

            amount += member.getMonthlyDinnerAmount();
        }

        return amount;
    }

    // =========================================================
    // BILL DIALOG
    // =========================================================

    private void showBillDialog(
            Member member,
            int fullMeals,
            int halfMeals,
            int lunchFull,
            int lunchHalf,
            int dinnerFull,
            int dinnerHalf,
            double totalUnits,
            double dailyAmount,
            double packageAmount,
            double finalAmount,
            int daysInMonth) {

        View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_generate_bill,
                null
        );

        TextView tvBillTitle =
                dialogView.findViewById(R.id.tvBillTitle);

        TextView tvBillMember =
                dialogView.findViewById(R.id.tvBillMember);

        TextView tvBillMonth =
                dialogView.findViewById(R.id.tvBillMonth);

        TextView tvBillType =
                dialogView.findViewById(R.id.tvBillType);

        TextView tvBillConfiguration =
                dialogView.findViewById(R.id.tvBillConfiguration);

        TextView tvLunchSummary =
                dialogView.findViewById(R.id.tvLunchSummary);

        TextView tvDinnerSummary =
                dialogView.findViewById(R.id.tvDinnerSummary);

        TextView tvFullTiffins =
                dialogView.findViewById(R.id.tvFullTiffins);

        TextView tvHalfTiffins =
                dialogView.findViewById(R.id.tvHalfTiffins);

        TextView tvTiffinQuantity =
                dialogView.findViewById(R.id.tvTiffinQuantity);

        TextView tvCollectedDays =
                dialogView.findViewById(R.id.tvCollectedDays);

        TextView tvTotalBill =
                dialogView.findViewById(R.id.tvTotalBill);

        MaterialButton btnCloseBill =
                dialogView.findViewById(R.id.btnCloseBill);

        MaterialButton btnGeneratePdf =
                dialogView.findViewById(R.id.btnGeneratePdf);

        String month = new SimpleDateFormat(
                "MMMM yyyy",
                Locale.getDefault()
        ).format(selectedMonth.getTime());

        tvBillTitle.setText("Monthly Bill");

        tvBillMember.setText(
                "Member: " + safeMemberName(member)
        );

        tvBillMonth.setText(month);

        tvBillType.setText(
                capitalize(
                        normalizePaymentType(
                                member.getPaymentType()
                        )
                )
        );

        tvBillConfiguration.setText(
                getMealConfigurationText(member)
        );

        tvLunchSummary.setText(
                "Full: " + lunchFull
                        + "   Half: " + lunchHalf
        );

        tvDinnerSummary.setText(
                "Full: " + dinnerFull
                        + "   Half: " + dinnerHalf
        );

        tvFullTiffins.setText(
                String.valueOf(fullMeals)
        );

        tvHalfTiffins.setText(
                String.valueOf(halfMeals)
        );

        tvTiffinQuantity.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f",
                        totalUnits
                )
        );

        tvCollectedDays.setText(
                String.valueOf(
                        calculateCollectedDays(
                                member
                        )
                )
        );

        tvTotalBill.setText(
                formatMoney(finalAmount)
        );

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );
        }

        btnCloseBill.setOnClickListener(
                v -> dialog.dismiss()
        );

        btnGeneratePdf.setOnClickListener(v ->
                generatePdf(
                        member,
                        month,
                        fullMeals,
                        halfMeals,
                        lunchFull,
                        lunchHalf,
                        dinnerFull,
                        dinnerHalf,
                        totalUnits,
                        dailyAmount,
                        packageAmount,
                        finalAmount,
                        daysInMonth
                )
        );

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels * 0.90
                    ),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // =========================================================
    // EDIT BILLING CONFIGURATION
    // =========================================================

    private void showEditBillingDialog(
            Member member,
            TextView cardRateText) {

        View dialogView = getLayoutInflater().inflate(
                R.layout.dialog_edit_member_billing,
                null
        );

        TextView tvDialogType =
                dialogView.findViewById(R.id.tvDialogType);

        EditText edtFullRate =
                dialogView.findViewById(R.id.edtFullRate);

        EditText edtHalfRate =
                dialogView.findViewById(R.id.edtHalfRate);

        EditText edtMonthlyLunch =
                dialogView.findViewById(R.id.edtMonthlyLunch);

        EditText edtMonthlyDinner =
                dialogView.findViewById(R.id.edtMonthlyDinner);

        MaterialButton btnCancel =
                dialogView.findViewById(R.id.btnCancelBilling);

        MaterialButton btnSave =
                dialogView.findViewById(R.id.btnSaveBilling);

        String paymentType =
                normalizePaymentType(
                        member.getPaymentType()
                );

        tvDialogType.setText(
                "Payment Type: "
                        + capitalize(paymentType)
        );

        edtFullRate.setText(
                formatNumber(member.getFullRate())
        );

        edtHalfRate.setText(
                formatNumber(member.getHalfRate())
        );

        edtMonthlyLunch.setText(
                formatNumber(
                        member.getMonthlyLunchAmount()
                )
        );

        edtMonthlyDinner.setText(
                formatNumber(
                        member.getMonthlyDinnerAmount()
                )
        );

        boolean daily =
                "daily".equals(paymentType);

        edtFullRate.setEnabled(daily);
        edtHalfRate.setEnabled(daily);

        edtMonthlyLunch.setEnabled(!daily);
        edtMonthlyDinner.setEnabled(!daily);

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();

        dialog.setOnShowListener(d -> {

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.WHITE)
                );
            }
        });

        btnCancel.setOnClickListener(
                v -> dialog.dismiss()
        );

        btnSave.setOnClickListener(v -> {

            if (daily) {

                Double fullRate =
                        parsePositive(
                                edtFullRate,
                                "Enter full meal rate"
                        );

                Double halfRate =
                        parsePositive(
                                edtHalfRate,
                                "Enter half meal rate"
                        );

                if (fullRate == null
                        || halfRate == null) {
                    return;
                }

                saveDailyConfiguration(
                        member,
                        fullRate,
                        halfRate,
                        cardRateText,
                        dialog
                );

            } else {

                Double lunchAmount =
                        parseNonNegative(
                                edtMonthlyLunch,
                                "Enter lunch package amount"
                        );

                Double dinnerAmount =
                        parseNonNegative(
                                edtMonthlyDinner,
                                "Enter dinner package amount"
                        );

                if (lunchAmount == null
                        || dinnerAmount == null) {
                    return;
                }

                saveMonthlyConfiguration(
                        member,
                        lunchAmount,
                        dinnerAmount,
                        cardRateText,
                        dialog
                );
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels * 0.90
                    ),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // =========================================================
    // SAVE DAILY CONFIG
    // =========================================================

    private void saveDailyConfiguration(
            Member member,
            double fullRate,
            double halfRate,
            TextView cardRateText,
            AlertDialog dialog) {

        Map<String, Object> data = new HashMap<>();

        data.put("fullRate", fullRate);
        data.put("halfRate", halfRate);

        firestore.collection("members")
                .document(member.getDocumentId())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    member.setFullRate(fullRate);
                    member.setHalfRate(halfRate);

                    cardRateText.setText(
                            "Full "
                                    + formatMoney(fullRate)
                                    + " • Half "
                                    + formatMoney(halfRate)
                    );

                    Toast.makeText(
                            requireContext(),
                            "Daily rates updated",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                requireContext(),
                                "Failed to update rates: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // SAVE MONTHLY CONFIG
    // =========================================================

    private void saveMonthlyConfiguration(
            Member member,
            double lunchAmount,
            double dinnerAmount,
            TextView cardRateText,
            AlertDialog dialog) {

        Map<String, Object> data = new HashMap<>();

        data.put(
                "monthlyLunchAmount",
                lunchAmount
        );

        data.put(
                "monthlyDinnerAmount",
                dinnerAmount
        );

        firestore.collection("members")
                .document(member.getDocumentId())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    member.setMonthlyLunchAmount(
                            lunchAmount
                    );

                    member.setMonthlyDinnerAmount(
                            dinnerAmount
                    );

                    cardRateText.setText(
                            "Monthly Package"
                    );

                    Toast.makeText(
                            requireContext(),
                            "Monthly package updated",
                            Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                requireContext(),
                                "Failed to update package: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // =========================================================
    // PDF
    // =========================================================

    private void generatePdf(
            Member member,
            String month,
            int fullMeals,
            int halfMeals,
            int lunchFull,
            int lunchHalf,
            int dinnerFull,
            int dinnerHalf,
            double totalUnits,
            double dailyAmount,
            double packageAmount,
            double finalAmount,
            int daysInMonth) {

        PdfDocument pdf = new PdfDocument();

        final int width = 595;
        final int height = 842;

        PdfDocument.PageInfo info =
                new PdfDocument.PageInfo.Builder(
                        width,
                        height,
                        1
                ).create();

        PdfDocument.Page page =
                pdf.startPage(info);

        Canvas canvas = page.getCanvas();

        Paint paint =
                new Paint(Paint.ANTI_ALIAS_FLAG);

        final int GREEN =
                Color.rgb(35, 120, 82);

        final int LIGHT_GREEN =
                Color.rgb(239, 248, 243);

        final int BORDER =
                Color.rgb(205, 222, 214);

        final int TEXT =
                Color.rgb(35, 35, 35);

        final int GREY =
                Color.rgb(95, 95, 95);

        // -----------------------------------------------------
        // BORDER
        // -----------------------------------------------------

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f);
        paint.setColor(Color.rgb(225, 225, 225));

        canvas.drawRoundRect(
                12,
                12,
                width - 12,
                height - 12,
                12,
                12,
                paint
        );

        paint.setStyle(Paint.Style.FILL);

        // -----------------------------------------------------
        // HEADER
        // -----------------------------------------------------

        drawPdfText(
                canvas,
                paint,
                "MONTHLY BILL",
                45,
                58,
                27,
                GREEN,
                true
        );

        String billId =
                "INV-"
                        + new SimpleDateFormat(
                        "yyyyMM",
                        Locale.getDefault()
                ).format(new Date())
                        + "-"
                        + String.format(
                        Locale.getDefault(),
                        "%04d",
                        Math.abs(
                                safeMemberName(member)
                                        .hashCode()
                        ) % 10000
                );

        drawPdfTextRight(
                canvas,
                paint,
                "Bill ID: " + billId,
                550,
                47,
                9.5f,
                TEXT,
                true
        );

        drawPdfTextRight(
                canvas,
                paint,
                "Date: "
                        + new SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale.getDefault()
                ).format(new Date()),
                550,
                66,
                9.5f,
                TEXT,
                false
        );

        // -----------------------------------------------------
        // MONTH
        // -----------------------------------------------------

        paint.setColor(LIGHT_GREEN);

        canvas.drawRoundRect(
                205,
                80,
                390,
                112,
                18,
                18,
                paint
        );

        drawPdfTextCenter(
                canvas,
                paint,
                month,
                width / 2f,
                101,
                12,
                GREEN,
                true
        );

        paint.setColor(GREEN);

        canvas.drawRect(
                30,
                127,
                width - 30,
                128.5f,
                paint
        );

        // -----------------------------------------------------
        // MEMBER CARD
        // -----------------------------------------------------

        drawPdfCard(
                canvas,
                paint,
                30,
                148,
                width - 30,
                245,
                Color.WHITE,
                BORDER
        );

        paint.setColor(LIGHT_GREEN);

        canvas.drawCircle(
                80,
                193,
                31,
                paint
        );

        String initial =
                safeMemberName(member)
                        .substring(0, 1)
                        .toUpperCase(Locale.getDefault());

        drawPdfTextCenter(
                canvas,
                paint,
                initial,
                80,
                201,
                25,
                GREEN,
                true
        );

        drawPdfText(
                canvas,
                paint,
                safeMemberName(member),
                135,
                190,
                15,
                TEXT,
                true
        );

        drawPdfText(
                canvas,
                paint,
                member.getEmail() == null
                        ? ""
                        : member.getEmail(),
                135,
                211,
                10.5f,
                GREY,
                false
        );

        drawPdfText(
                canvas,
                paint,
                "Payment: "
                        + capitalize(
                        normalizePaymentType(
                                member.getPaymentType()
                        )
                ),
                135,
                230,
                10,
                GREEN,
                true
        );

        // -----------------------------------------------------
        // SUMMARY CARD
        // -----------------------------------------------------

        drawPdfCard(
                canvas,
                paint,
                30,
                260,
                width - 30,
                535,
                Color.WHITE,
                BORDER
        );

        drawPdfText(
                canvas,
                paint,
                "BILL SUMMARY",
                47,
                288,
                13,
                GREEN,
                true
        );

        int y = 320;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Payment Type",
                capitalize(
                        normalizePaymentType(
                                member.getPaymentType()
                        )
                ),
                y
        );

        y += 28;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Lunch",
                member.isLunchEnabled()
                        ? "Enabled"
                        : "Disabled",
                y
        );

        y += 28;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Dinner",
                member.isDinnerEnabled()
                        ? "Enabled"
                        : "Disabled",
                y
        );

        y += 28;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Full Meals Collected",
                String.valueOf(fullMeals),
                y
        );

        y += 28;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Half Meals Collected",
                String.valueOf(halfMeals),
                y
        );

        y += 28;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Meal Units",
                String.format(
                        Locale.getDefault(),
                        "%.1f",
                        totalUnits
                ),
                y
        );

        y += 28;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Lunch Full / Half",
                lunchFull + " / " + lunchHalf,
                y
        );

        y += 28;

        drawPdfSummaryRow(
                canvas,
                paint,
                "Dinner Full / Half",
                dinnerFull + " / " + dinnerHalf,
                y
        );

        y += 28;

        if ("monthly".equals(
                normalizePaymentType(
                        member.getPaymentType()
                ))) {

            drawPdfSummaryRow(
                    canvas,
                    paint,
                    "Lunch Package",
                    member.isLunchEnabled()
                            ? formatMoney(
                            member.getMonthlyLunchAmount()
                    )
                            : "₹ 0.00",
                    y
            );

            y += 28;

            drawPdfSummaryRow(
                    canvas,
                    paint,
                    "Dinner Package",
                    member.isDinnerEnabled()
                            ? formatMoney(
                            member.getMonthlyDinnerAmount()
                    )
                            : "₹ 0.00",
                    y
            );

        } else {

            drawPdfSummaryRow(
                    canvas,
                    paint,
                    "Full Rate",
                    formatMoney(
                            member.getFullRate()
                    ),
                    y
            );

            y += 28;

            drawPdfSummaryRow(
                    canvas,
                    paint,
                    "Half Rate",
                    formatMoney(
                            member.getHalfRate()
                    ),
                    y
            );
        }

        // -----------------------------------------------------
        // TOTAL
        // -----------------------------------------------------

        paint.setColor(
                Color.rgb(155, 195, 176)
        );

        for (int x = 47; x < 548; x += 7) {

            canvas.drawRect(
                    x,
                    650,
                    Math.min(x + 4, 548),
                    651,
                    paint
            );
        }

        drawPdfText(
                canvas,
                paint,
                "TOTAL BILL",
                47,
                680,
                15,
                GREEN,
                true
        );

        drawPdfTextRight(
                canvas,
                paint,
                formatMoney(finalAmount),
                548,
                680,
                20,
                GREEN,
                true
        );

        // -----------------------------------------------------
        // COLLECTION SUMMARY
        // -----------------------------------------------------

        drawPdfCard(
                canvas,
                paint,
                30,
                705,
                width - 30,
                760,
                LIGHT_GREEN,
                BORDER
        );

        drawPdfText(
                canvas,
                paint,
                "Generated by MessMate",
                47,
                735,
                10,
                GREY,
                false
        );

        drawPdfTextRight(
                canvas,
                paint,
                daysInMonth
                        + " days in "
                        + month,
                548,
                735,
                10,
                GREY,
                false
        );

        pdf.finishPage(page);

        // -----------------------------------------------------
        // FILE
        // -----------------------------------------------------

        String safeName =
                safeMemberName(member)
                        .replaceAll(
                                "[^a-zA-Z0-9_-]",
                                "_"
                        );

        String safeMonth =
                month.replaceAll(
                        "[^a-zA-Z0-9_-]",
                        "_"
                );

        String fileName =
                "MessMate_"
                        + safeName
                        + "_"
                        + safeMonth
                        + ".pdf";

        try {

            if (Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.Q) {

                ContentValues values =
                        new ContentValues();

                values.put(
                        MediaStore.Downloads.DISPLAY_NAME,
                        fileName
                );

                values.put(
                        MediaStore.Downloads.MIME_TYPE,
                        "application/pdf"
                );

                values.put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                );

                android.net.Uri uri =
                        requireContext()
                                .getContentResolver()
                                .insert(
                                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                        values
                                );

                if (uri == null) {

                    pdf.close();

                    Toast.makeText(
                            requireContext(),
                            "Could not create PDF",
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                OutputStream outputStream =
                        requireContext()
                                .getContentResolver()
                                .openOutputStream(uri);

                if (outputStream != null) {

                    pdf.writeTo(outputStream);

                    outputStream.close();
                }

            } else {

                File directory =
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS
                        );

                if (!directory.exists()) {
                    directory.mkdirs();
                }

                File file =
                        new File(
                                directory,
                                fileName
                        );

                OutputStream outputStream =
                        new FileOutputStream(file);

                pdf.writeTo(outputStream);

                outputStream.close();
            }

            pdf.close();

            Toast.makeText(
                    requireContext(),
                    "Bill PDF saved in Downloads",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            pdf.close();

            Toast.makeText(
                    requireContext(),
                    "PDF error: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // =========================================================
    // TIFFIN CALENDAR
    // =========================================================

    private void showMemberTiffinCalendar(
            String memberId,
            String memberName) {

        if (!isAdded()) {
            return;
        }

        Calendar popupMonth =
                (Calendar) selectedMonth.clone();

        popupMonth.set(
                Calendar.DAY_OF_MONTH,
                1
        );

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                ).create();

        dialog.setOnShowListener(d -> {

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.WHITE)
                );
            }
        });

        LinearLayout root =
                new LinearLayout(requireContext());

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        TextView title =
                new TextView(requireContext());

        title.setText(
                memberName
                        + " - Tiffin Calendar"
        );

        title.setTextSize(18);

        title.setTextColor(GREEN);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(Gravity.CENTER);

        root.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(45)
                )
        );

        LinearLayout monthRow =
                new LinearLayout(requireContext());

        monthRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        monthRow.setGravity(
                Gravity.CENTER_VERTICAL
        );

        ImageButton previous =
                new ImageButton(requireContext());

        previous.setImageResource(
                R.drawable.ic_arrow_left
        );

        previous.setBackgroundColor(
                Color.TRANSPARENT
        );

        previous.setColorFilter(GREEN);

        TextView monthText =
                new TextView(requireContext());

        monthText.setTextSize(16);

        monthText.setTextColor(
                TEXT_PRIMARY
        );

        monthText.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        monthText.setGravity(Gravity.CENTER);

        ImageButton next =
                new ImageButton(requireContext());

        next.setImageResource(
                R.drawable.ic_arrow_right
        );

        next.setBackgroundColor(
                Color.TRANSPARENT
        );

        next.setColorFilter(GREEN);

        monthRow.addView(
                previous,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(45)
                )
        );

        monthRow.addView(
                monthText,
                new LinearLayout.LayoutParams(
                        0,
                        dp(45),
                        1
                )
        );

        monthRow.addView(
                next,
                new LinearLayout.LayoutParams(
                        dp(45),
                        dp(45)
                )
        );

        root.addView(monthRow);

        LinearLayout calendar =
                new LinearLayout(requireContext());

        calendar.setOrientation(
                LinearLayout.VERTICAL
        );

        root.addView(calendar);

        LinearLayout legend =
                new LinearLayout(requireContext());

        legend.setOrientation(
                LinearLayout.HORIZONTAL
        );

        legend.setGravity(Gravity.CENTER);

        addLegendItem(
                legend,
                FULL_BG,
                FULL_TEXT,
                "Full"
        );

        addLegendItem(
                legend,
                HALF_BG,
                HALF_TEXT,
                "Half"
        );

        addLegendItem(
                legend,
                NOT_BG,
                NOT_TEXT,
                "Not Collected"
        );

        root.addView(
                legend,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(42)
                )
        );

        Button close =
                new Button(requireContext());

        close.setText("Close");

        close.setAllCaps(false);

        close.setTextColor(GREEN);
        close.setBackgroundColor(
                Color.rgb(225, 225, 225)
        );
        close.setOnClickListener(
                v -> dialog.dismiss()
        );

        root.addView(
                close,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(45)
                )
        );

        dialog.setView(root);

        loadMemberCalendarData(
                memberId,
                popupMonth,
                monthText,
                calendar
        );

        previous.setOnClickListener(v -> {

            popupMonth.add(
                    Calendar.MONTH,
                    -1
            );

            loadMemberCalendarData(
                    memberId,
                    popupMonth,
                    monthText,
                    calendar
            );
        });

        next.setOnClickListener(v -> {

            popupMonth.add(
                    Calendar.MONTH,
                    1
            );

            loadMemberCalendarData(
                    memberId,
                    popupMonth,
                    monthText,
                    calendar
            );
        });

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .widthPixels * 0.94
                    ),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // =========================================================
    // CALENDAR DATA
    // =========================================================

    private void loadMemberCalendarData(
            String memberId,
            Calendar month,
            TextView monthText,
            LinearLayout container) {

        String ownerId = getOwnerId();

        if (ownerId == null
                || ownerId.isEmpty()) {
            return;
        }

        monthText.setText(
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                ).format(month.getTime())
        );

        firestore.collection("tiffin_records")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo(
                        "memberDocumentId",
                        memberId
                )
                .get()
                .addOnSuccessListener(snapshot -> {

                    Map<String, TiffinRecord> records =
                            new HashMap<>();

                    for (QueryDocumentSnapshot document
                            : snapshot) {

                        TiffinRecord record =
                                document.toObject(
                                        TiffinRecord.class
                                );

                        if (record.getDate() != null) {

                            records.put(
                                    record.getDate(),
                                    record
                            );
                        }
                    }

                    buildCalendar(
                            month,
                            records,
                            container
                    );
                })
                .addOnFailureListener(e ->
                        buildCalendar(
                                month,
                                new HashMap<>(),
                                container
                        )
                );
    }

    // =========================================================
    // BUILD CALENDAR
    // =========================================================

    private void buildCalendar(
            Calendar month,
            Map<String, TiffinRecord> records,
            LinearLayout container) {

        container.removeAllViews();

        LinearLayout header =
                new LinearLayout(requireContext());

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] days = {
                "Sun",
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat"
        };

        for (String day : days) {

            TextView text =
                    createCalendarText();

            text.setText(day);

            text.setTextSize(11);

            text.setTextColor(
                    TEXT_SECONDARY
            );

            header.addView(
                    text,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(30),
                            1
                    )
            );
        }

        container.addView(header);

        Calendar first =
                (Calendar) month.clone();

        first.set(
                Calendar.DAY_OF_MONTH,
                1
        );

        int firstDay =
                first.get(Calendar.DAY_OF_WEEK) - 1;

        int daysInMonth =
                month.getActualMaximum(
                        Calendar.DAY_OF_MONTH
                );

        int totalCells =
                firstDay + daysInMonth;

        int rows =
                (int) Math.ceil(
                        totalCells / 7.0
                );

        if (rows < 5) {
            rows = 5;
        }

        int currentDay = 1;

        for (int row = 0;
             row < rows;
             row++) {

            LinearLayout calendarRow =
                    new LinearLayout(
                            requireContext()
                    );

            calendarRow.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            for (int column = 0;
                 column < 7;
                 column++) {

                int cell =
                        row * 7 + column;

                FrameLayout cellLayout =
                        new FrameLayout(
                                requireContext()
                        );

                if (cell < firstDay
                        || currentDay > daysInMonth) {

                    TextView empty =
                            createCalendarText();

                    cellLayout.addView(
                            empty,
                            new FrameLayout.LayoutParams(
                                    dp(34),
                                    dp(34),
                                    Gravity.CENTER
                            )
                    );

                } else {

                    int day = currentDay++;

                    String key =
                            new SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                            ).format(
                                    createDate(
                                            month,
                                            day
                                    )
                            );

                    TiffinRecord record =
                            records.get(key);

                    // =================================================
                    // CHANGED CALENDAR UI
                    // Each date now has separate Lunch and Dinner
                    // status indicators.
                    // =================================================

                    LinearLayout dateContainer =
                            new LinearLayout(
                                    requireContext()
                            );

                    dateContainer.setOrientation(
                            LinearLayout.VERTICAL
                    );

                    dateContainer.setGravity(
                            Gravity.CENTER
                    );

                    TextView date =
                            new TextView(requireContext());

                    date.setText(
                            String.valueOf(day)
                    );

                    date.setGravity(
                            Gravity.CENTER
                    );

                    date.setTextSize(11);

                    date.setTextColor(
                            TEXT_PRIMARY
                    );

                    date.setTypeface(
                            Typeface.DEFAULT,
                            Typeface.BOLD
                    );

                    dateContainer.addView(
                            date,
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    dp(20)
                            )
                    );

                    LinearLayout statusRow =
                            new LinearLayout(
                                    requireContext()
                            );

                    statusRow.setOrientation(
                            LinearLayout.HORIZONTAL
                    );

                    statusRow.setGravity(
                            Gravity.CENTER
                    );

                    // -------------------------------------------------
                    // LUNCH STATUS
                    // -------------------------------------------------

                    TextView lunchStatus =
                            createMealStatusBadge("L");

                    // -------------------------------------------------
                    // DINNER STATUS
                    // -------------------------------------------------

                    TextView dinnerStatus =
                            createMealStatusBadge("D");

                    applyMealStatus(
                            lunchStatus,
                            record == null
                                    ? null
                                    : record.getLunchStatus()
                    );

                    applyMealStatus(
                            dinnerStatus,
                            record == null
                                    ? null
                                    : record.getDinnerStatus()
                    );

                    statusRow.addView(
                            lunchStatus,
                            new LinearLayout.LayoutParams(
                                    dp(15),
                                    dp(15)
                            )
                    );

                    LinearLayout.LayoutParams dinnerParams =
                            new LinearLayout.LayoutParams(
                                    dp(15),
                                    dp(15)
                            );

                    dinnerParams.setMargins(
                            dp(3),
                            0,
                            0,
                            0
                    );

                    statusRow.addView(
                            dinnerStatus,
                            dinnerParams
                    );

                    dateContainer.addView(
                            statusRow,
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    dp(18)
                            )
                    );

                    cellLayout.addView(
                            dateContainer,
                            new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    dp(45),
                                    Gravity.CENTER
                            )
                    );
                }

                calendarRow.addView(
                        cellLayout,
                        new LinearLayout.LayoutParams(
                                0,
                                dp(48),
                                1
                        )
                );
            }

            container.addView(calendarRow);
        }
    }

    // =========================================================
    // CREATE MEAL STATUS BADGE
    // =========================================================

    private TextView createMealStatusBadge(
            String label) {

        TextView badge =
                new TextView(requireContext());

        badge.setText(label);

        badge.setGravity(
                Gravity.CENTER
        );

        badge.setTextSize(8);

        badge.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        badge.setTextColor(
                Color.WHITE
        );

        return badge;
    }

    // =========================================================
    // APPLY INDIVIDUAL MEAL STATUS
    // =========================================================

    private void applyMealStatus(
            TextView badge,
            String status) {

        String normalized =
                normalizeStatus(status);

        if ("full".equals(normalized)) {

            setMealStatusBadge(
                    badge,
                    FULL_TEXT
            );

        } else if ("half".equals(normalized)) {

            setMealStatusBadge(
                    badge,
                    HALF_TEXT
            );

        } else {

            setMealStatusBadge(
                    badge,
                    NOT_TEXT
            );
        }
    }

    // =========================================================
    // SET MEAL STATUS BADGE
    // =========================================================

    private void setMealStatusBadge(
            TextView badge,
            int color) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.OVAL
        );

        drawable.setColor(color);

        badge.setBackground(drawable);
    }

    // =========================================================
    // CALENDAR STATUS
    // =========================================================

    /*
     * Kept as a helper for compatibility.
     *
     * The calendar itself now uses applyMealStatus()
     * separately for Lunch and Dinner.
     */
    private void applyCalendarStatus(
            TextView text,
            TiffinRecord record) {

        if (record == null) {

            setCalendarCircle(
                    text,
                    NOT_BG,
                    NOT_TEXT
            );

            return;
        }

        boolean full =
                "full".equalsIgnoreCase(
                        normalizeStatus(
                                record.getLunchStatus()
                        )
                )
                        || "full".equalsIgnoreCase(
                        normalizeStatus(
                                record.getDinnerStatus()
                        )
                );

        boolean half =
                "half".equalsIgnoreCase(
                        normalizeStatus(
                                record.getLunchStatus()
                        )
                )
                        || "half".equalsIgnoreCase(
                        normalizeStatus(
                                record.getDinnerStatus()
                        )
                );

        if (full) {

            setCalendarCircle(
                    text,
                    FULL_BG,
                    FULL_TEXT
            );

        } else if (half) {

            setCalendarCircle(
                    text,
                    HALF_BG,
                    HALF_TEXT
            );

        } else {

            setCalendarCircle(
                    text,
                    NOT_BG,
                    NOT_TEXT
            );
        }
    }

    // =========================================================
    // CALENDAR HELPERS
    // =========================================================

    private TextView createCalendarText() {

        TextView text =
                new TextView(requireContext());

        text.setGravity(Gravity.CENTER);

        text.setTextSize(12);

        text.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        return text;
    }

    private void setCalendarCircle(
            TextView text,
            int backgroundColor,
            int textColor) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.OVAL
        );

        drawable.setColor(backgroundColor);

        text.setBackground(drawable);

        text.setTextColor(textColor);
    }

    private void addLegendItem(
            LinearLayout parent,
            int backgroundColor,
            int textColor,
            String labelText) {

        LinearLayout item =
                new LinearLayout(
                        requireContext()
                );

        item.setOrientation(
                LinearLayout.HORIZONTAL
        );

        item.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView circle =
                new TextView(requireContext());

        circle.setBackground(
                createCircleDrawable(
                        backgroundColor
                )
        );

        item.addView(
                circle,
                new LinearLayout.LayoutParams(
                        dp(12),
                        dp(12)
                )
        );

        TextView label =
                new TextView(requireContext());

        label.setText(labelText);

        label.setTextSize(10);

        label.setTextColor(textColor);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                dp(4),
                0,
                dp(8),
                0
        );

        item.addView(label, params);

        parent.addView(item);
    }

    private GradientDrawable createCircleDrawable(
            int color) {

        GradientDrawable drawable =
                new GradientDrawable();

        drawable.setShape(
                GradientDrawable.OVAL
        );

        drawable.setColor(color);

        return drawable;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean isRecordInSelectedMonth(
            String date) {

        if (date == null
                || date.trim().isEmpty()) {
            return false;
        }

        try {

            Date parsed =
                    new SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                    ).parse(date);

            if (parsed == null) {
                return false;
            }

            Calendar record =
                    Calendar.getInstance();

            record.setTime(parsed);

            return record.get(Calendar.YEAR)
                    == selectedMonth.get(
                    Calendar.YEAR
            )
                    && record.get(Calendar.MONTH)
                    == selectedMonth.get(
                    Calendar.MONTH
            );

        } catch (Exception e) {

            return false;
        }
    }

    private int calculateCollectedDays(
            Member member) {

        // This value is displayed as a summary.
        // It is calculated from records that contain
        // at least one enabled meal status.

        return 0;
    }

    private String getMealConfigurationText(
            Member member) {

        if (member.isLunchEnabled()
                && member.isDinnerEnabled()) {

            return "Lunch + Dinner";

        } else if (member.isLunchEnabled()) {

            return "Lunch only";

        } else if (member.isDinnerEnabled()) {

            return "Dinner only";
        }

        return "No meals enabled";
    }

    private String normalizeStatus(String status) {

        if (status == null) {
            return "";
        }

        return status.trim().toLowerCase(
                Locale.getDefault()
        );
    }

    private String normalizePaymentType(
            String paymentType) {

        if (paymentType == null
                || paymentType.trim().isEmpty()) {

            return "daily";
        }

        return paymentType
                .trim()
                .toLowerCase(Locale.getDefault());
    }

    private String safeMemberName(Member member) {

        if (member.getName() == null
                || member.getName().trim().isEmpty()) {

            return "Member";
        }

        return member.getName();
    }

    private String capitalize(String value) {

        if (value == null
                || value.isEmpty()) {
            return "";
        }

        return value.substring(0, 1)
                .toUpperCase(Locale.getDefault())
                + value.substring(1);
    }

    private String formatMoney(double amount) {

        return String.format(
                Locale.getDefault(),
                "₹ %.2f",
                amount
        );
    }

    private String formatNumber(double value) {

        return String.format(
                Locale.getDefault(),
                "%.2f",
                value
        );
    }

    private Double parsePositive(
            EditText editText,
            String error) {

        String value =
                editText.getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {

            editText.setError(error);

            return null;
        }

        try {

            double number =
                    Double.parseDouble(value);

            if (number <= 0) {

                editText.setError(
                        "Value must be greater than 0"
                );

                return null;
            }

            return number;

        } catch (NumberFormatException e) {

            editText.setError(
                    "Enter a valid amount"
            );

            return null;
        }
    }

    private Double parseNonNegative(
            EditText editText,
            String error) {

        String value =
                editText.getText()
                        .toString()
                        .trim();

        if (value.isEmpty()) {

            editText.setError(error);

            return null;
        }

        try {

            double number =
                    Double.parseDouble(value);

            if (number < 0) {

                editText.setError(
                        "Value cannot be negative"
                );

                return null;
            }

            return number;

        } catch (NumberFormatException e) {

            editText.setError(
                    "Enter a valid amount"
            );

            return null;
        }
    }

    private String getOwnerId() {

        if (sessionManager == null) {
            return null;
        }

        return sessionManager.getUid();
    }

    private Date createDate(
            Calendar month,
            int day) {

        Calendar calendar =
                (Calendar) month.clone();

        calendar.set(
                Calendar.DAY_OF_MONTH,
                day
        );

        calendar.set(
                Calendar.HOUR_OF_DAY,
                12
        );

        calendar.set(
                Calendar.MINUTE,
                0
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        return calendar.getTime();
    }

    private int dp(float value) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void showEmptyMessage(
            String titleText,
            String subtitleText) {

        LinearLayout layout =
                new LinearLayout(
                        requireContext()
                );

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(Gravity.CENTER);

        layout.setPadding(
                dp(20),
                dp(60),
                dp(20),
                dp(20)
        );

        TextView title =
                new TextView(requireContext());

        title.setText(titleText);

        title.setTextSize(17);

        title.setTextColor(Color.BLACK);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setGravity(Gravity.CENTER);

        TextView subtitle =
                new TextView(requireContext());

        subtitle.setText(subtitleText);

        subtitle.setTextSize(14);

        subtitle.setTextColor(Color.GRAY);

        subtitle.setGravity(Gravity.CENTER);

        layout.addView(title);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(
                0,
                dp(5),
                0,
                0
        );

        layout.addView(
                subtitle,
                params
        );

        memberContainer.addView(
                layout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(250)
                )
        );
    }

    // =========================================================
    // PDF HELPERS
    // =========================================================

    private void drawPdfCard(
            Canvas canvas,
            Paint paint,
            float left,
            float top,
            float right,
            float bottom,
            int fill,
            int border) {

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(fill);

        canvas.drawRoundRect(
                left,
                top,
                right,
                bottom,
                10,
                10,
                paint
        );

        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(0.8f);

        paint.setColor(border);

        canvas.drawRoundRect(
                left,
                top,
                right,
                bottom,
                10,
                10,
                paint
        );

        paint.setStyle(Paint.Style.FILL);
    }

    private void drawPdfSummaryRow(
            Canvas canvas,
            Paint paint,
            String label,
            String value,
            float y) {

        drawPdfText(
                canvas,
                paint,
                label,
                47,
                y,
                10.5f,
                Color.rgb(35, 35, 35),
                false
        );

        drawPdfTextRight(
                canvas,
                paint,
                value,
                548,
                y,
                10.5f,
                GREEN,
                true
        );

        paint.setColor(
                Color.rgb(235, 238, 236)
        );

        canvas.drawRect(
                47,
                y + 13,
                548,
                y + 14,
                paint
        );
    }

    private void drawPdfText(
            Canvas canvas,
            Paint paint,
            String text,
            float x,
            float y,
            float size,
            int color,
            boolean bold) {

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(color);

        paint.setTextSize(size);

        paint.setTextAlign(Paint.Align.LEFT);

        paint.setTypeface(
                Typeface.create(
                        "sans",
                        bold
                                ? Typeface.BOLD
                                : Typeface.NORMAL
                )
        );

        canvas.drawText(
                text == null ? "" : text,
                x,
                y,
                paint
        );
    }

    private void drawPdfTextRight(
            Canvas canvas,
            Paint paint,
            String text,
            float x,
            float y,
            float size,
            int color,
            boolean bold) {

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(color);

        paint.setTextSize(size);

        paint.setTextAlign(Paint.Align.RIGHT);

        paint.setTypeface(
                Typeface.create(
                        "sans",
                        bold
                                ? Typeface.BOLD
                                : Typeface.NORMAL
                )
        );

        canvas.drawText(
                text == null ? "" : text,
                x,
                y,
                paint
        );
    }

    private void drawPdfTextCenter(
            Canvas canvas,
            Paint paint,
            String text,
            float x,
            float y,
            float size,
            int color,
            boolean bold) {

        paint.setStyle(Paint.Style.FILL);

        paint.setColor(color);

        paint.setTextSize(size);

        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTypeface(
                Typeface.create(
                        "sans",
                        bold
                                ? Typeface.BOLD
                                : Typeface.NORMAL
                )
        );

        canvas.drawText(
                text == null ? "" : text,
                x,
                y,
                paint
        );
    }

    // =========================================================
    // MEAL SUMMARY
    // =========================================================

    private static class MealSummary {

        int fullMeals;
        int halfMeals;

        int lunchFull;
        int lunchHalf;

        int dinnerFull;
        int dinnerHalf;

        double units;
        double amount;
    }
}