package com.example.messmate.presentation.owner.billing;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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

    private double defaultTiffinRate = 50.0;

    // =========================================================
    // SEARCH DATA
    // =========================================================

    private final List<BillingMember> billingMembers = new ArrayList<>();

    // =========================================================
    // COLORS
    // =========================================================

    private final int GREEN = Color.rgb(47, 132, 100);
    private final int LIGHT_GREEN = Color.rgb(235, 247, 241);
    private final int WHITE = Color.WHITE;
    private final int TEXT_PRIMARY = Color.rgb(35, 35, 35);
    private final int TEXT_SECONDARY = Color.rgb(125, 125, 125);
    private final int BORDER = Color.rgb(232, 236, 234);

    // =========================================================
    // CALENDAR POPUP COLORS
    // =========================================================

    private final int FULL_TIFFIN_BG = Color.rgb(232, 247, 238);
    private final int FULL_TIFFIN_TEXT = Color.rgb(35, 120, 75);

    private final int HALF_TIFFIN_BG = Color.rgb(255, 239, 222);
    private final int HALF_TIFFIN_TEXT = Color.rgb(225, 105, 20);

    private final int NOT_COLLECTED_BG = Color.rgb(255, 232, 232);
    private final int NOT_COLLECTED_TEXT = Color.rgb(220, 55, 55);

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public BillingFragment() {
        // Required empty public constructor
    }

    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_billing, container, false);
    }

    // =========================================================
    // VIEW CREATED
    // =========================================================

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(requireContext());

        selectedMonth = Calendar.getInstance();

        tvBillingMonthSubtitle = view.findViewById(R.id.tvBillingMonthSubtitle);

        tvSelectedMonth = view.findViewById(R.id.tvSelectedMonth);

        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);

        btnNextMonth = view.findViewById(R.id.btnNextMonth);

        memberContainer = view.findViewById(R.id.memberContainer);

        edtBillingSearch = view.findViewById(R.id.edtBillingSearch);

        updateMonthText();

        setupBillingSearch();

        loadOwnerDefaultRate();

        // -----------------------------------------------------
        // PREVIOUS MONTH
        // -----------------------------------------------------

        btnPreviousMonth.setOnClickListener(v -> {

            selectedMonth.add(Calendar.MONTH, -1);

            updateMonthText();

            loadMembers();
        });

        // -----------------------------------------------------
        // NEXT MONTH
        // -----------------------------------------------------

        btnNextMonth.setOnClickListener(v -> {

            selectedMonth.add(Calendar.MONTH, 1);

            updateMonthText();

            loadMembers();
        });
    }

    // =========================================================
    // BILLING SEARCH
    // =========================================================

    private void setupBillingSearch() {

        edtBillingSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filterMembers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // =========================================================
    // FILTER MEMBERS
    // =========================================================

    private void filterMembers(String searchText) {

        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.getDefault());

        memberContainer.removeAllViews();

        if (billingMembers.isEmpty()) {

            showNoMembers();

            return;
        }

        int matchingMembers = 0;

        for (BillingMember member : billingMembers) {

            String name = member.name == null ? "" : member.name.toLowerCase(Locale.getDefault());

            String email = member.email == null ? "" : member.email.toLowerCase(Locale.getDefault());

            if (query.isEmpty() || name.contains(query) || email.contains(query)) {

                addMemberCard(member.memberId, member.name, member.email, member.memberRate);

                matchingMembers++;
            }
        }

        if (matchingMembers == 0) {

            showNoSearchResults();
        }
    }

    // =========================================================
    // NO SEARCH RESULTS
    // =========================================================

    private void showNoSearchResults() {

        LinearLayout emptyLayout = new LinearLayout(requireContext());

        emptyLayout.setOrientation(LinearLayout.VERTICAL);

        emptyLayout.setGravity(Gravity.CENTER);

        emptyLayout.setPadding(dp(20), dp(60), dp(20), dp(20));

        TextView title = new TextView(requireContext());

        title.setText("No members found");

        title.setTextSize(17);

        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        title.setTextColor(Color.BLACK);

        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(requireContext());

        subtitle.setText("Try a different name or email");

        subtitle.setTextSize(14);

        subtitle.setTextColor(Color.GRAY);

        subtitle.setGravity(Gravity.CENTER);

        emptyLayout.addView(title);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        subtitleParams.setMargins(0, dp(5), 0, 0);

        emptyLayout.addView(subtitle, subtitleParams);

        memberContainer.addView(emptyLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(250)));
    }

    // =========================================================
    // MONTH
    // =========================================================

    private void updateMonthText() {

        String month = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedMonth.getTime());

        tvSelectedMonth.setText(month);

        tvBillingMonthSubtitle.setText(month);
    }

    // =========================================================
    // GET OWNER ID
    // =========================================================

    private String getOwnerId() {

        if (sessionManager == null) {
            return null;
        }

        return sessionManager.getUid();
    }

    // =========================================================
    // LOAD OWNER DEFAULT RATE
    // =========================================================

    private void loadOwnerDefaultRate() {

        String ownerId = getOwnerId();

        if (ownerId == null || ownerId.isEmpty()) {

            loadMembers();

            return;
        }

        firestore.collection("owners").document(ownerId).get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()) {

                Double rate = documentSnapshot.getDouble("tiffinRate");

                if (rate != null && rate > 0) {

                    defaultTiffinRate = rate;
                }
            }

            loadMembers();

        }).addOnFailureListener(e -> loadMembers());
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

            Toast.makeText(requireContext(), "Owner session not found", Toast.LENGTH_SHORT).show();

            return;
        }

        memberContainer.removeAllViews();

        billingMembers.clear();

        firestore.collection("members").whereEqualTo("ownerId", ownerId).get().addOnSuccessListener(queryDocumentSnapshots -> {

            if (!isAdded()) {
                return;
            }

            memberContainer.removeAllViews();

            billingMembers.clear();

            if (queryDocumentSnapshots.isEmpty()) {

                showNoMembers();

                return;
            }

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                String memberId = document.getId();

                String name = document.getString("name");

                String email = document.getString("email");

                Double memberRate = document.getDouble("tiffinRate");

                if (name == null || name.trim().isEmpty()) {

                    name = "Member";
                }

                if (memberRate == null || memberRate <= 0) {

                    memberRate = defaultTiffinRate;

                    saveInitialMemberRate(memberId, memberRate);
                }

                billingMembers.add(new BillingMember(memberId, name, email, memberRate));
            }

            filterMembers(edtBillingSearch.getText().toString());

        }).addOnFailureListener(e -> {

            if (!isAdded()) {
                return;
            }

            Toast.makeText(requireContext(), "Failed to load members: " + e.getMessage(), Toast.LENGTH_LONG).show();

            showNoMembers();
        });
    }

    // =========================================================
    // SAVE INITIAL MEMBER RATE
    // =========================================================

    private void saveInitialMemberRate(String memberId, double rate) {

        Map<String, Object> data = new HashMap<>();

        data.put("tiffinRate", rate);

        firestore.collection("members").document(memberId).set(data, SetOptions.merge());
    }

    // =========================================================
    // NO MEMBERS
    // =========================================================

    private void showNoMembers() {

        LinearLayout emptyLayout = new LinearLayout(requireContext());

        emptyLayout.setOrientation(LinearLayout.VERTICAL);

        emptyLayout.setGravity(Gravity.CENTER);

        emptyLayout.setPadding(dp(20), dp(80), dp(20), dp(20));

        TextView title = new TextView(requireContext());

        title.setText("No members found");

        title.setTextSize(17);

        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        title.setTextColor(Color.BLACK);

        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(requireContext());

        subtitle.setText("Add members first");

        subtitle.setTextSize(14);

        subtitle.setTextColor(Color.GRAY);

        subtitle.setGravity(Gravity.CENTER);

        emptyLayout.addView(title);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        subtitleParams.setMargins(0, dp(5), 0, 0);

        emptyLayout.addView(subtitle, subtitleParams);

        memberContainer.addView(emptyLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(300)));
    }

    // =========================================================
    // DP HELPER
    // =========================================================

    private int dp(float value) {

        return (int) (value * getResources().getDisplayMetrics().density);
    }

    // =========================================================
    // GREEN BUTTON BACKGROUND
    // =========================================================

    private GradientDrawable createGreenButtonBackground() {

        GradientDrawable background = new GradientDrawable();

        background.setColor(GREEN);

        background.setCornerRadius(dp(12));

        return background;
    }

    // =========================================================
    // LIGHT BUTTON BACKGROUND
    // =========================================================

    private GradientDrawable createLightButtonBackground() {

        GradientDrawable background = new GradientDrawable();

        background.setColor(LIGHT_GREEN);

        background.setCornerRadius(dp(10));

        background.setStroke(dp(1), Color.rgb(210, 235, 224));

        return background;
    }

    // =========================================================
    // MEMBER CARD
    // =========================================================

    private void addMemberCard(String memberId, String name, String email, double memberRate) {

        LinearLayout card = new LinearLayout(requireContext());

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable background = new GradientDrawable();

        background.setColor(WHITE);

        background.setCornerRadius(dp(18));

        background.setStroke(dp(1), BORDER);

        card.setBackground(background);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        cardParams.setMargins(0, dp(6), 0, dp(6));

        memberContainer.addView(card, cardParams);

        card.setOnClickListener(v -> showMemberTiffinCalendar(memberId, name));

        LinearLayout header = new LinearLayout(requireContext());

        header.setOrientation(LinearLayout.HORIZONTAL);

        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView initial = new TextView(requireContext());

        initial.setGravity(Gravity.CENTER);

        initial.setTextSize(16);

        initial.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        initial.setTextColor(GREEN);

        if (name != null && !name.trim().isEmpty()) {

            initial.setText(name.substring(0, 1).toUpperCase());

        } else {

            initial.setText("M");
        }

        GradientDrawable initialBackground = new GradientDrawable();

        initialBackground.setShape(GradientDrawable.OVAL);

        initialBackground.setColor(LIGHT_GREEN);

        initial.setBackground(initialBackground);

        LinearLayout.LayoutParams initialParams = new LinearLayout.LayoutParams(dp(54), dp(54));

        header.addView(initial, initialParams);

        LinearLayout memberInfo = new LinearLayout(requireContext());

        memberInfo.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

        infoParams.setMargins(dp(12), 0, 0, 0);

        TextView tvName = new TextView(requireContext());

        tvName.setText(name);

        tvName.setTextSize(17);

        tvName.setTextColor(TEXT_PRIMARY);

        tvName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        tvName.setSingleLine(true);

        tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);

        memberInfo.addView(tvName);

        if (email != null && !email.trim().isEmpty()) {

            TextView tvEmail = new TextView(requireContext());

            tvEmail.setText(email);

            tvEmail.setTextSize(12);

            tvEmail.setTextColor(TEXT_SECONDARY);

            tvEmail.setSingleLine(true);

            tvEmail.setEllipsize(android.text.TextUtils.TruncateAt.END);

            LinearLayout.LayoutParams emailParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            emailParams.setMargins(0, dp(2), 0, 0);

            memberInfo.addView(tvEmail, emailParams);
        }

        header.addView(memberInfo, infoParams);

        card.addView(header);

        // =====================================================
        // RATE ROW
        // =====================================================

        LinearLayout rateRow = new LinearLayout(requireContext());

        rateRow.setOrientation(LinearLayout.HORIZONTAL);

        rateRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams rateRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        rateRowParams.setMargins(0, dp(14), 0, dp(12));

        LinearLayout rateInfo = new LinearLayout(requireContext());

        rateInfo.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams rateInfoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

        TextView rateLabel = new TextView(requireContext());

        rateLabel.setText("Tiffin Rate");

        rateLabel.setTextSize(11);

        rateLabel.setTextColor(TEXT_SECONDARY);

        TextView rateValue = new TextView(requireContext());

        rateValue.setText(formatRate(memberRate));

        rateValue.setTextSize(15);

        rateValue.setTextColor(TEXT_PRIMARY);

        rateValue.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        LinearLayout.LayoutParams rateValueParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        rateValueParams.setMargins(0, dp(2), 0, 0);

        rateInfo.addView(rateLabel);

        rateInfo.addView(rateValue, rateValueParams);

        rateRow.addView(rateInfo, rateInfoParams);

        Button editRateButton = new Button(requireContext());

        editRateButton.setText("Edit Rate");

        editRateButton.setAllCaps(false);

        editRateButton.setTextSize(12);

        editRateButton.setTextColor(GREEN);

        editRateButton.setGravity(Gravity.CENTER);

        editRateButton.setMinWidth(0);

        editRateButton.setMinimumWidth(0);

        editRateButton.setMinHeight(0);

        editRateButton.setMinimumHeight(0);

        editRateButton.setPadding(dp(13), 0, dp(13), 0);

        editRateButton.setBackground(createLightButtonBackground());

        LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(dp(92), dp(36));

        editRateButton.setOnClickListener(v -> showEditMemberRateDialog(memberId, rateValue));

        rateRow.addView(editRateButton, editParams);

        card.addView(rateRow, rateRowParams);

        View divider = new View(requireContext());

        divider.setBackgroundColor(Color.rgb(240, 242, 241));

        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));

        dividerParams.setMargins(0, 0, 0, dp(12));

        card.addView(divider, dividerParams);

        // =====================================================
        // CALCULATE MONTHLY BILL BUTTON
        // =====================================================

        Button calculateButton = new Button(requireContext());

        calculateButton.setText("Calculate Monthly Bill");

        calculateButton.setAllCaps(false);

        calculateButton.setTextSize(13);

        calculateButton.setTextColor(WHITE);

        calculateButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        calculateButton.setGravity(Gravity.CENTER);

        calculateButton.setMinWidth(0);

        calculateButton.setMinimumWidth(0);

        calculateButton.setMinHeight(0);

        calculateButton.setMinimumHeight(0);

        calculateButton.setPadding(dp(12), 0, dp(12), 0);

        calculateButton.setBackground(createGreenButtonBackground());

        LinearLayout.LayoutParams calculateParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(44));

        final String finalName = name;

        final String finalEmail = email;

        calculateButton.setOnClickListener(v -> calculateMonthlyBill(memberId, finalName, finalEmail));

        card.addView(calculateButton, calculateParams);
    }

    // =========================================================
    // FORMAT RATE
    // =========================================================

    private String formatRate(double rate) {

        return String.format(Locale.getDefault(), "₹ %.2f / tiffin", rate);
    }

    // =========================================================
    // EDIT MEMBER RATE
    // =========================================================

    private void showEditMemberRateDialog(String memberId, TextView rateValue) {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_tiffin_rate, null);

        EditText edtTiffinRate = dialogView.findViewById(R.id.edtTiffinRate);

        MaterialButton btnCancelRate = dialogView.findViewById(R.id.btnCancelRate);

        MaterialButton btnSaveRate = dialogView.findViewById(R.id.btnSaveRate);

        firestore.collection("members").document(memberId).get().addOnSuccessListener(documentSnapshot -> {

            Double currentRate = documentSnapshot.getDouble("tiffinRate");

            if (currentRate != null) {

                edtTiffinRate.setText(String.format(Locale.getDefault(), "%.2f", currentRate));

                edtTiffinRate.setSelection(edtTiffinRate.getText().length());
            }

            AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

            if (dialog.getWindow() != null) {

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            btnCancelRate.setOnClickListener(v -> dialog.dismiss());

            btnSaveRate.setOnClickListener(v -> {

                String value = edtTiffinRate.getText().toString().trim();

                if (value.isEmpty()) {

                    edtTiffinRate.setError("Enter rate");

                    return;
                }

                double newRate;

                try {

                    newRate = Double.parseDouble(value);

                } catch (NumberFormatException e) {

                    edtTiffinRate.setError("Invalid rate");

                    return;
                }

                if (newRate <= 0) {

                    edtTiffinRate.setError("Rate must be greater than 0");

                    return;
                }

                saveMemberRate(memberId, newRate, rateValue, dialog);
            });

            dialog.show();

            if (dialog.getWindow() != null) {

                dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.88), ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load member rate", Toast.LENGTH_SHORT).show());
    }

    // =========================================================
    // SAVE MEMBER RATE
    // =========================================================

    private void saveMemberRate(String memberId, double newRate, TextView rateValue, AlertDialog dialog) {

        Map<String, Object> data = new HashMap<>();

        data.put("tiffinRate", newRate);

        firestore.collection("members").document(memberId).set(data, SetOptions.merge()).addOnSuccessListener(unused -> {

            rateValue.setText(formatRate(newRate));

            // Keep local search data synchronized
            for (BillingMember member : billingMembers) {

                if (member.memberId.equals(memberId)) {

                    member.memberRate = newRate;

                    break;
                }
            }

            Toast.makeText(requireContext(), "Tiffin rate updated", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update rate: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // =========================================================
    // CALCULATE MONTHLY BILL
    // =========================================================

    private void calculateMonthlyBill(String memberId, String memberName, String email) {

        String ownerId = getOwnerId();

        if (ownerId == null || ownerId.isEmpty()) {

            Toast.makeText(requireContext(), "Owner session not found", Toast.LENGTH_SHORT).show();

            return;
        }

        firestore.collection("members").document(memberId).get().addOnSuccessListener(memberDocument -> {

            Double memberRate = memberDocument.getDouble("tiffinRate");

            if (memberRate == null || memberRate <= 0) {

                memberRate = defaultTiffinRate;
            }

            calculateBillFromRecords(ownerId, memberId, memberName, email, memberRate);
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to load member rate: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // =========================================================
    // CALCULATE FROM TIFFIN RECORDS
    // =========================================================

    private void calculateBillFromRecords(String ownerId, String memberId, String memberName, String email, double memberRate) {

        Calendar start = (Calendar) selectedMonth.clone();

        start.set(Calendar.DAY_OF_MONTH, 1);

        start.set(Calendar.HOUR_OF_DAY, 0);

        start.set(Calendar.MINUTE, 0);

        start.set(Calendar.SECOND, 0);

        start.set(Calendar.MILLISECOND, 0);

        Calendar end = (Calendar) start.clone();

        end.add(Calendar.MONTH, 1);

        firestore.collection("tiffin_records").whereEqualTo("ownerId", ownerId).whereEqualTo("memberDocumentId", memberId).get().addOnSuccessListener(query -> {

            double totalTiffins = 0;

            int totalDays = 0;

            int fullTiffins = 0;

            int halfTiffins = 0;

            for (QueryDocumentSnapshot document : query) {

                String dateString = document.getString("date");

                if (dateString == null || dateString.trim().isEmpty()) {

                    continue;
                }

                Date recordDate;

                try {

                    recordDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString);

                } catch (Exception e) {

                    continue;
                }

                if (recordDate == null) {
                    continue;
                }

                Calendar recordCalendar = Calendar.getInstance();

                recordCalendar.setTime(recordDate);

                boolean sameMonth = recordCalendar.get(Calendar.YEAR) == selectedMonth.get(Calendar.YEAR) && recordCalendar.get(Calendar.MONTH) == selectedMonth.get(Calendar.MONTH);

                if (!sameMonth) {
                    continue;
                }

                String tiffin = document.getString("tiffin");

                if ("full".equalsIgnoreCase(tiffin)) {

                    fullTiffins++;

                    totalTiffins += 1.0;

                    totalDays++;

                } else if ("half".equalsIgnoreCase(tiffin)) {

                    halfTiffins++;

                    totalTiffins += 0.5;

                    totalDays++;
                }
            }

            double totalAmount = totalTiffins * memberRate;

            showBillDialog(memberName, email, fullTiffins, halfTiffins, totalTiffins, totalDays, memberRate, totalAmount);
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to calculate bill: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // =========================================================
    // BILL DIALOG
    // =========================================================

    private void showBillDialog(String memberName, String email, int fullTiffins, int halfTiffins, double totalTiffins, int totalDays, double memberRate, double totalAmount) {

        if (!isAdded()) {
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_generate_bill, null);

        TextView tvBillTitle = dialogView.findViewById(R.id.tvBillTitle);

        TextView tvBillMember = dialogView.findViewById(R.id.tvBillMember);

        TextView tvBillMonth = dialogView.findViewById(R.id.tvBillMonth);

        TextView tvBillRate = dialogView.findViewById(R.id.tvBillRate);

        TextView tvFullTiffins = dialogView.findViewById(R.id.tvFullTiffins);

        TextView tvHalfTiffins = dialogView.findViewById(R.id.tvHalfTiffins);

        TextView tvTiffinQuantity = dialogView.findViewById(R.id.tvTiffinQuantity);

        TextView tvCollectedDays = dialogView.findViewById(R.id.tvCollectedDays);

        TextView tvTotalBill = dialogView.findViewById(R.id.tvTotalBill);

        MaterialButton btnCloseBill = dialogView.findViewById(R.id.btnCloseBill);

        MaterialButton btnGeneratePdf = dialogView.findViewById(R.id.btnGeneratePdf);

        String month = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(selectedMonth.getTime());

        tvBillTitle.setText("Monthly Bill");

        tvBillMember.setText("Member: " + memberName);

        tvBillMonth.setText(month);

        tvBillRate.setText(String.format(Locale.getDefault(), "₹ %.2f", memberRate));

        tvFullTiffins.setText(String.valueOf(fullTiffins));

        tvHalfTiffins.setText(String.valueOf(halfTiffins));

        tvTiffinQuantity.setText(String.format(Locale.getDefault(), "%.1f", totalTiffins));

        tvCollectedDays.setText(String.valueOf(totalDays));

        tvTotalBill.setText(String.format(Locale.getDefault(), "₹ %.2f", totalAmount));

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnCloseBill.setOnClickListener(v -> dialog.dismiss());

        btnGeneratePdf.setOnClickListener(v -> generatePdf(memberName, email, month, fullTiffins, halfTiffins, totalTiffins, totalDays, memberRate, totalAmount));

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.88), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // =========================================================
    // GENERATE PDF - MESSMATE PROFESSIONAL FORMAT
    // =========================================================

    private void generatePdf(String memberName, String email, String month, int fullTiffins, int halfTiffins, double totalTiffins, int totalDays, double memberRate, double totalAmount) {

        PdfDocument pdfDocument = new PdfDocument();

        final int PAGE_WIDTH = 595;

        final int PAGE_HEIGHT = 842;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        android.graphics.Canvas canvas = page.getCanvas();

        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);

        // =====================================================
        // COLORS
        // =====================================================

        final int GREEN = Color.rgb(35, 120, 82);

        final int LIGHT_GREEN = Color.rgb(239, 248, 243);

        final int PALE_GREEN = Color.rgb(248, 252, 249);

        final int BORDER = Color.rgb(205, 222, 214);

        final int TEXT = Color.rgb(35, 35, 35);

        final int GREY = Color.rgb(95, 95, 95);

        final int ORANGE = Color.rgb(232, 126, 18);

        final int RED = Color.rgb(195, 45, 45);

        final int FULL_BG = Color.rgb(230, 244, 235);

        final int HALF_BG = Color.rgb(255, 239, 218);

        final int NOT_BG = Color.rgb(255, 231, 231);

        // =====================================================
        // OUTER BORDER
        // =====================================================

        paint.setStyle(android.graphics.Paint.Style.STROKE);

        paint.setStrokeWidth(1.2f);

        paint.setColor(Color.rgb(225, 225, 225));

        canvas.drawRoundRect(12, 12, PAGE_WIDTH - 12, PAGE_HEIGHT - 12, 12, 12, paint);

        paint.setStyle(android.graphics.Paint.Style.FILL);

        // =====================================================
        // HEADER
        // =====================================================

        drawPdfText(canvas, paint, "MONTHLY BILL", 50, 58, 27, GREEN, true);

        String billId = "INV-" + new SimpleDateFormat("yyyyMM", Locale.getDefault()).format(new Date()) + "-" + String.format(Locale.getDefault(), "%04d", Math.abs((memberName + month).hashCode()) % 10000);

        String generatedDate = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());

        drawPdfTextRight(canvas, paint, "Bill ID: " + billId, 545, 47, 9.5f, TEXT, true);

        drawPdfTextRight(canvas, paint, "Date: " + generatedDate, 545, 66, 9.5f, TEXT, false);

        // =====================================================
        // MONTH PILL
        // =====================================================

        paint.setColor(LIGHT_GREEN);

        canvas.drawRoundRect(207, 79, 388, 111, 18, 18, paint);

        drawPdfText(canvas, paint, "▣", 229, 101, 16, GREEN, false);

        drawPdfText(canvas, paint, month, 257, 101, 12, GREEN, true);

        // =====================================================
        // GREEN DIVIDER
        // =====================================================

        paint.setColor(GREEN);

        canvas.drawRect(30, 126, PAGE_WIDTH - 30, 127.5f, paint);

        // =====================================================
        // MEMBER DETAILS
        // =====================================================

        drawPdfCard(canvas, paint, 30, 147, PAGE_WIDTH - 30, 240, Color.WHITE, BORDER);

        paint.setColor(LIGHT_GREEN);

        canvas.drawCircle(80, 192, 31, paint);

        String initial = "M";

        if (memberName != null && !memberName.trim().isEmpty()) {

            initial = memberName.substring(0, 1).toUpperCase(Locale.getDefault());
        }

        drawPdfTextCenter(canvas, paint, initial, 80, 201, 25, GREEN, true);

        paint.setColor(BORDER);

        canvas.drawRect(123, 165, 124, 219, paint);

        drawPdfText(canvas, paint, memberName == null ? "Member" : memberName, 143, 190, 15, TEXT, true);

        if (email != null && !email.trim().isEmpty()) {

            drawPdfText(canvas, paint, email, 143, 211, 10.5f, TEXT, false);
        }

        // =====================================================
        // BILL SUMMARY CARD
        // =====================================================

        drawPdfCard(canvas, paint, 30, 255, PAGE_WIDTH - 30, 505, Color.WHITE, BORDER);

        drawPdfText(canvas, paint, "▤", 46, 283, 19, GREEN, true);

        drawPdfText(canvas, paint, "BILL SUMMARY", 75, 283, 13, GREEN, true);

        drawPdfText(canvas, paint, "(" + month.toUpperCase(Locale.getDefault()) + ")", 183, 283, 8.5f, GREY, false);

        int rowY = 310;

        drawPdfSummaryRow(canvas, paint, "Tiffin Rate (per tiffin)", String.format(Locale.getDefault(), "₹ %.2f", memberRate), rowY, TEXT, true);

        rowY += 29;

        drawPdfSummaryRow(canvas, paint, "Tiffin Quantity", String.format(Locale.getDefault(), "%.1f", totalTiffins), rowY, TEXT, false);

        rowY += 29;

        int daysInMonth = selectedMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        drawPdfSummaryRow(canvas, paint, "Total Days in Month", String.valueOf(daysInMonth), rowY, TEXT, false);

        rowY += 29;

        drawPdfSummaryRow(canvas, paint, "Full Tiffins", String.valueOf(fullTiffins), rowY, GREEN, true);

        rowY += 29;

        drawPdfSummaryRow(canvas, paint, "Half Tiffins", String.valueOf(halfTiffins), rowY, ORANGE, true);

        rowY += 29;

        int notCollected = Math.max(0, daysInMonth - fullTiffins - halfTiffins);

        drawPdfSummaryRow(canvas, paint, "Not Collected", String.valueOf(notCollected), rowY, RED, true);

        // =====================================================
        // DASHED SEPARATOR
        // =====================================================

        paint.setColor(Color.rgb(155, 195, 176));

        paint.setStrokeWidth(1);

        for (int x = 47; x < 548; x += 7) {

            canvas.drawRect(x, 474, Math.min(x + 4, 548), 475, paint);
        }

        // =====================================================
        // TOTAL BILL
        // =====================================================

        drawPdfText(canvas, paint, "TOTAL BILL", 47, 492, 14, GREEN, true);

        drawPdfTextRight(canvas, paint, String.format(Locale.getDefault(), "₹ %.2f", totalAmount), 548, 492, 19, GREEN, true);

        // =====================================================
        // COLLECTION SUMMARY
        // =====================================================

        drawPdfCard(canvas, paint, 30, 519, PAGE_WIDTH - 30, 646, PALE_GREEN, BORDER);

        drawPdfText(canvas, paint, "▣", 46, 547, 17, GREEN, true);

        drawPdfText(canvas, paint, "COLLECTION SUMMARY", 75, 547, 12, GREEN, true);

        drawPdfText(canvas, paint, "(" + month.toUpperCase(Locale.getDefault()) + ")", 213, 547, 8.5f, GREY, false);

        int fullPercent = Math.round((fullTiffins * 100f) / Math.max(1, daysInMonth));

        int halfPercent = Math.round((halfTiffins * 100f) / Math.max(1, daysInMonth));

        int notPercent = Math.round((notCollected * 100f) / Math.max(1, daysInMonth));

        paint.setColor(BORDER);

        canvas.drawRect(196, 567, 197, 628, paint);

        canvas.drawRect(396, 567, 397, 628, paint);

        // =====================================================
        // FULL TIFFINS
        // =====================================================

        paint.setColor(FULL_BG);

        canvas.drawCircle(130, 585, 19, paint);

        drawPdfTextCenter(canvas, paint, String.valueOf(fullTiffins), 130, 592, 15, GREEN, true);

        drawPdfTextCenter(canvas, paint, "Full Tiffins", 130, 616, 10, GREEN, true);

        drawPdfTextCenter(canvas, paint, "(" + fullPercent + "%)", 130, 634, 8.5f, GREY, false);

        // =====================================================
        // HALF TIFFINS
        // =====================================================

        paint.setColor(HALF_BG);

        canvas.drawCircle(296, 585, 19, paint);

        drawPdfTextCenter(canvas, paint, String.valueOf(halfTiffins), 296, 592, 15, ORANGE, true);

        drawPdfTextCenter(canvas, paint, "Half Tiffins", 296, 616, 10, ORANGE, true);

        drawPdfTextCenter(canvas, paint, "(" + halfPercent + "%)", 296, 634, 8.5f, GREY, false);

        // =====================================================
        // NOT COLLECTED
        // =====================================================

        paint.setColor(NOT_BG);

        canvas.drawCircle(462, 585, 19, paint);

        drawPdfTextCenter(canvas, paint, String.valueOf(notCollected), 462, 592, 15, RED, true);

        drawPdfTextCenter(canvas, paint, "Not Collected", 462, 616, 10, RED, true);

        drawPdfTextCenter(canvas, paint, "(" + notPercent + "%)", 462, 634, 8.5f, GREY, false);

        // =====================================================
        // THANK YOU CARD
        // =====================================================

        drawPdfCard(canvas, paint, 30, 660, PAGE_WIDTH - 30, 733, PALE_GREEN, BORDER);

        paint.setColor(GREEN);

        canvas.drawCircle(75, 696, 24, paint);

        drawPdfTextCenter(canvas, paint, "▤", 75, 704, 21, Color.WHITE, true);

        drawPdfText(canvas, paint, "Thank You!", 110, 689, 14, GREEN, true);

        drawPdfText(canvas, paint, "Thank you for choosing our tiffin service.", 110, 708, 9.5f, TEXT, false);

        drawPdfText(canvas, paint, "We appreciate your trust and support.", 110, 724, 9.5f, TEXT, false);

        // =====================================================
        // FOOTER
        // =====================================================

        drawPdfTextCenter(canvas, paint, "Generated by MessMate", PAGE_WIDTH / 2f, 770, 9, GREY, false);

        // =====================================================
        // FINISH PAGE
        // =====================================================

        pdfDocument.finishPage(page);

        // =====================================================
        // FILE NAME
        // =====================================================

        String safeName = memberName == null ? "Member" : memberName.replaceAll("[^a-zA-Z0-9_-]", "_");

        String safeMonth = month.replaceAll("[^a-zA-Z0-9_-]", "_");

        String fileName = "MessMate_" + safeName + "_" + safeMonth + ".pdf";

        // =====================================================
        // SAVE PDF
        // =====================================================

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                ContentValues values = new ContentValues();

                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);

                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");

                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                android.net.Uri uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri == null) {

                    Toast.makeText(requireContext(), "Could not create PDF", Toast.LENGTH_LONG).show();

                    pdfDocument.close();

                    return;
                }

                OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);

                if (outputStream != null) {

                    pdfDocument.writeTo(outputStream);

                    outputStream.close();
                }

            } else {

                File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                if (!downloadsDirectory.exists()) {

                    downloadsDirectory.mkdirs();
                }

                File pdfFile = new File(downloadsDirectory, fileName);

                OutputStream outputStream = new FileOutputStream(pdfFile);

                pdfDocument.writeTo(outputStream);

                outputStream.close();
            }

            pdfDocument.close();

            Toast.makeText(requireContext(), "Bill PDF saved in Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {

            pdfDocument.close();

            Toast.makeText(requireContext(), "PDF error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // =========================================================
    // PDF CARD
    // =========================================================

    private void drawPdfCard(android.graphics.Canvas canvas, android.graphics.Paint paint, float left, float top, float right, float bottom, int fillColor, int borderColor) {

        paint.setStyle(android.graphics.Paint.Style.FILL);

        paint.setColor(fillColor);

        canvas.drawRoundRect(left, top, right, bottom, 10, 10, paint);

        paint.setStyle(android.graphics.Paint.Style.STROKE);

        paint.setStrokeWidth(0.8f);

        paint.setColor(borderColor);

        canvas.drawRoundRect(left, top, right, bottom, 10, 10, paint);

        paint.setStyle(android.graphics.Paint.Style.FILL);
    }

    // =========================================================
    // PDF SUMMARY ROW
    // =========================================================

    private void drawPdfSummaryRow(android.graphics.Canvas canvas, android.graphics.Paint paint, String label, String value, float y, int valueColor, boolean boldValue) {

        drawPdfText(canvas, paint, label, 47, y, 10.5f, Color.rgb(35, 35, 35), false);

        drawPdfTextRight(canvas, paint, value, 548, y, 10.5f, valueColor, boldValue);

        paint.setColor(Color.rgb(235, 238, 236));

        paint.setStrokeWidth(0.7f);

        canvas.drawRect(47, y + 13, 548, y + 14, paint);
    }

    // =========================================================
    // PDF TEXT
    // =========================================================

    private void drawPdfText(android.graphics.Canvas canvas, android.graphics.Paint paint, String text, float x, float y, float size, int color, boolean bold) {

        paint.setStyle(android.graphics.Paint.Style.FILL);

        paint.setColor(color);

        paint.setTextSize(size);

        paint.setTextAlign(android.graphics.Paint.Align.LEFT);

        paint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));

        canvas.drawText(text == null ? "" : text, x, y, paint);
    }

    // =========================================================
    // PDF TEXT RIGHT
    // =========================================================

    private void drawPdfTextRight(android.graphics.Canvas canvas, android.graphics.Paint paint, String text, float x, float y, float size, int color, boolean bold) {

        paint.setStyle(android.graphics.Paint.Style.FILL);

        paint.setColor(color);

        paint.setTextSize(size);

        paint.setTextAlign(android.graphics.Paint.Align.RIGHT);

        paint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));

        canvas.drawText(text == null ? "" : text, x, y, paint);
    }

    // =========================================================
    // PDF TEXT CENTER
    // =========================================================

    private void drawPdfTextCenter(android.graphics.Canvas canvas, android.graphics.Paint paint, String text, float x, float y, float size, int color, boolean bold) {

        paint.setStyle(android.graphics.Paint.Style.FILL);

        paint.setColor(color);

        paint.setTextSize(size);

        paint.setTextAlign(android.graphics.Paint.Align.CENTER);

        paint.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));

        canvas.drawText(text == null ? "" : text, x, y, paint);
    }

    // =========================================================
    // PDF SUMMARY ROW
    // =========================================================

    private void drawPdfSummaryRow(Canvas canvas, Paint paint, String label, String value, float y, int labelColor, int valueColor) {

        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));

        paint.setTextSize(10.5f);

        paint.setTextAlign(Paint.Align.LEFT);

        paint.setColor(labelColor);

        canvas.drawText(label, 48, y, paint);

        paint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

        paint.setTextAlign(Paint.Align.RIGHT);

        paint.setColor(valueColor);

        canvas.drawText(value, 550, y, paint);
    }

    // =========================================================
    // PDF DIVIDER
    // =========================================================

    private void drawPdfDivider(Canvas canvas, float y, float right) {

        Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        dividerPaint.setColor(Color.rgb(232, 236, 234));

        dividerPaint.setStrokeWidth(1);

        canvas.drawLine(48, y, right, y, dividerPaint);
    }

    // =========================================================
    // COLLECTION SUMMARY CIRCLE
    // =========================================================

    private void drawCollectionCircle(Canvas canvas, float x, float y, String value, int backgroundColor, int textColor) {

        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        circlePaint.setStyle(Paint.Style.FILL);

        circlePaint.setColor(backgroundColor);

        canvas.drawCircle(x, y, 18, circlePaint);

        circlePaint.setStyle(Paint.Style.STROKE);

        circlePaint.setStrokeWidth(1);

        circlePaint.setColor(textColor);

        circlePaint.setAlpha(80);

        canvas.drawCircle(x, y, 18, circlePaint);

        circlePaint.setStyle(Paint.Style.FILL);

        circlePaint.setAlpha(255);

        circlePaint.setColor(textColor);

        circlePaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));

        circlePaint.setTextSize(13);

        circlePaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(value, x, y + 5, circlePaint);
    }

    // =========================================================
    // MEMBER TIFFIN CALENDAR POPUP
    // =========================================================

    private void showMemberTiffinCalendar(String memberId, String memberName) {

        if (!isAdded()) {
            return;
        }

        Calendar popupMonth = (Calendar) selectedMonth.clone();

        popupMonth.set(Calendar.DAY_OF_MONTH, 1);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();

        LinearLayout root = new LinearLayout(requireContext());

        root.setOrientation(LinearLayout.VERTICAL);

        root.setPadding(dp(14), dp(12), dp(14), dp(12));

        TextView title = new TextView(requireContext());

        title.setText(memberName + " - Tiffin Calendar");

        title.setTextSize(18);

        title.setTextColor(GREEN);

        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        title.setGravity(Gravity.CENTER);

        root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(45)));

        LinearLayout monthRow = new LinearLayout(requireContext());

        monthRow.setOrientation(LinearLayout.HORIZONTAL);

        monthRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageButton previous = new ImageButton(requireContext());

        previous.setImageResource(R.drawable.ic_arrow_left);

        previous.setBackgroundColor(Color.TRANSPARENT);

        previous.setColorFilter(GREEN);

        TextView monthText = new TextView(requireContext());

        monthText.setTextSize(16);

        monthText.setTextColor(TEXT_PRIMARY);

        monthText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        monthText.setGravity(Gravity.CENTER);

        ImageButton next = new ImageButton(requireContext());

        next.setImageResource(R.drawable.ic_arrow_right);

        next.setBackgroundColor(Color.TRANSPARENT);

        next.setColorFilter(GREEN);

        monthRow.addView(previous, new LinearLayout.LayoutParams(dp(45), dp(45)));

        monthRow.addView(monthText, new LinearLayout.LayoutParams(0, dp(45), 1));

        monthRow.addView(next, new LinearLayout.LayoutParams(dp(45), dp(45)));

        root.addView(monthRow);

        LinearLayout popupCalendar = new LinearLayout(requireContext());

        popupCalendar.setOrientation(LinearLayout.VERTICAL);

        root.addView(popupCalendar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout legend = new LinearLayout(requireContext());

        legend.setOrientation(LinearLayout.HORIZONTAL);

        legend.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams legendParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42));

        legendParams.setMargins(0, dp(8), 0, dp(4));

        addLegendItem(legend, FULL_TIFFIN_BG, FULL_TIFFIN_TEXT, "Full");

        addLegendItem(legend, HALF_TIFFIN_BG, HALF_TIFFIN_TEXT, "Half");

        addLegendItem(legend, NOT_COLLECTED_BG, NOT_COLLECTED_TEXT, "Not Collected");

        root.addView(legend, legendParams);

        Button closeButton = new Button(requireContext());

        closeButton.setText("Close");

        closeButton.setAllCaps(false);

        closeButton.setTextColor(GREEN);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        root.addView(closeButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(45)));

        dialog.setView(root);

        loadMemberCalendarData(memberId, popupMonth, monthText, popupCalendar);

        previous.setOnClickListener(v -> {

            popupMonth.add(Calendar.MONTH, -1);

            loadMemberCalendarData(memberId, popupMonth, monthText, popupCalendar);
        });

        next.setOnClickListener(v -> {

            popupMonth.add(Calendar.MONTH, 1);

            loadMemberCalendarData(memberId, popupMonth, monthText, popupCalendar);
        });

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.94), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // =========================================================
    // LOAD MEMBER CALENDAR DATA
    // =========================================================

    private void loadMemberCalendarData(String memberId, Calendar month, TextView monthText, LinearLayout calendarContainer) {

        if (!isAdded()) {
            return;
        }

        String ownerId = getOwnerId();

        if (ownerId == null || ownerId.isEmpty()) {
            return;
        }

        monthText.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.getTime()));

        firestore.collection("tiffin_records").whereEqualTo("ownerId", ownerId).whereEqualTo("memberDocumentId", memberId).get().addOnSuccessListener(query -> {

            if (!isAdded()) {
                return;
            }

            Map<String, String> tiffinStatus = new HashMap<>();

            for (QueryDocumentSnapshot document : query) {

                String date = document.getString("date");

                String tiffin = document.getString("tiffin");

                if (date == null || date.trim().isEmpty()) {

                    continue;
                }

                if (tiffin == null) {

                    tiffin = "";
                }

                tiffinStatus.put(date, tiffin);
            }

            buildMemberPopupCalendar(month, tiffinStatus, calendarContainer);
        }).addOnFailureListener(e -> {

            if (!isAdded()) {
                return;
            }

            Toast.makeText(requireContext(), "Failed to load tiffin records", Toast.LENGTH_SHORT).show();

            buildMemberPopupCalendar(month, new HashMap<>(), calendarContainer);
        });
    }

    // =========================================================
    // BUILD MEMBER POPUP CALENDAR
    // =========================================================

    private void buildMemberPopupCalendar(Calendar month, Map<String, String> tiffinStatus, LinearLayout calendarContainer) {

        calendarContainer.removeAllViews();

        LinearLayout dayHeader = new LinearLayout(requireContext());

        dayHeader.setOrientation(LinearLayout.HORIZONTAL);

        dayHeader.setGravity(Gravity.CENTER);

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (String day : days) {

            TextView dayText = new TextView(requireContext());

            dayText.setText(day);

            dayText.setTextSize(11);

            dayText.setTextColor(TEXT_SECONDARY);

            dayText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            dayText.setGravity(Gravity.CENTER);

            dayHeader.addView(dayText, new LinearLayout.LayoutParams(0, dp(30), 1));
        }

        calendarContainer.addView(dayHeader);

        Calendar firstDay = (Calendar) month.clone();

        firstDay.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 1;

        int daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar previousMonth = (Calendar) month.clone();

        previousMonth.add(Calendar.MONTH, -1);

        int previousMonthDays = previousMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        int totalCells = firstDayOfWeek + daysInMonth;

        int rows = (int) Math.ceil(totalCells / 7.0);

        if (rows < 5) {
            rows = 5;
        }

        int currentDay = 1;

        int nextMonthDay = 1;

        for (int row = 0; row < rows; row++) {

            LinearLayout calendarRow = new LinearLayout(requireContext());

            calendarRow.setOrientation(LinearLayout.HORIZONTAL);

            calendarRow.setGravity(Gravity.CENTER);

            for (int column = 0; column < 7; column++) {

                int cell = row * 7 + column;

                FrameLayoutWithDate dateCell = new FrameLayoutWithDate(requireContext());

                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(0, dp(48), 1);

                if (cell < firstDayOfWeek) {

                    int previousDate = previousMonthDays - firstDayOfWeek + cell + 1;

                    TextView dateText = createPopupDateText();

                    dateText.setText(String.valueOf(previousDate));

                    dateText.setTextColor(Color.rgb(190, 190, 190));

                    dateCell.addView(dateText, new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.CENTER));

                } else if (currentDay <= daysInMonth) {

                    int day = currentDay++;

                    TextView dateText = createPopupDateText();

                    dateText.setText(String.valueOf(day));

                    String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(createDate(month, day));

                    String status = tiffinStatus.get(dateKey);

                    applyMemberPopupDateStatus(dateText, status);

                    dateCell.addView(dateText, new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.CENTER));

                } else {

                    TextView dateText = createPopupDateText();

                    dateText.setText(String.valueOf(nextMonthDay++));

                    dateText.setTextColor(Color.rgb(190, 190, 190));

                    dateCell.addView(dateText, new FrameLayout.LayoutParams(dp(34), dp(34), Gravity.CENTER));
                }

                calendarRow.addView(dateCell, cellParams);
            }

            calendarContainer.addView(calendarRow);
        }
    }

    // =========================================================
    // CREATE DATE TEXT
    // =========================================================

    private TextView createPopupDateText() {

        TextView dateText = new TextView(requireContext());

        dateText.setGravity(Gravity.CENTER);

        dateText.setTextSize(12);

        dateText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        dateText.setTextColor(TEXT_PRIMARY);

        return dateText;
    }

    // =========================================================
    // APPLY TIFFIN STATUS COLOR
    // =========================================================

    private void applyMemberPopupDateStatus(TextView dateText, String status) {

        if (status != null && "full".equalsIgnoreCase(status.trim())) {

            setCalendarCircle(dateText, FULL_TIFFIN_BG, FULL_TIFFIN_TEXT);

        } else if (status != null && "half".equalsIgnoreCase(status.trim())) {

            setCalendarCircle(dateText, HALF_TIFFIN_BG, HALF_TIFFIN_TEXT);

        } else {

            setCalendarCircle(dateText, NOT_COLLECTED_BG, NOT_COLLECTED_TEXT);
        }
    }

    // =========================================================
    // CREATE DATE
    // =========================================================

    private Date createDate(Calendar month, int day) {

        Calendar calendar = (Calendar) month.clone();

        calendar.set(Calendar.DAY_OF_MONTH, day);

        calendar.set(Calendar.HOUR_OF_DAY, 12);

        calendar.set(Calendar.MINUTE, 0);

        calendar.set(Calendar.SECOND, 0);

        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    // =========================================================
    // LEGEND ITEM
    // =========================================================

    private void addLegendItem(LinearLayout parent, int backgroundColor, int textColor, String text) {

        LinearLayout item = new LinearLayout(requireContext());

        item.setOrientation(LinearLayout.HORIZONTAL);

        item.setGravity(Gravity.CENTER_VERTICAL);

        item.setPadding(dp(5), 0, dp(5), 0);

        TextView circle = new TextView(requireContext());

        circle.setBackground(createCircleDrawable(backgroundColor));

        item.addView(circle, new LinearLayout.LayoutParams(dp(12), dp(12)));

        TextView label = new TextView(requireContext());

        label.setText(text);

        label.setTextSize(10);

        label.setTextColor(textColor);

        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        labelParams.setMargins(dp(4), 0, dp(5), 0);

        item.addView(label, labelParams);

        parent.addView(item);
    }

    // =========================================================
    // CIRCLE DRAWABLE
    // =========================================================

    private GradientDrawable createCircleDrawable(int color) {

        GradientDrawable drawable = new GradientDrawable();

        drawable.setShape(GradientDrawable.OVAL);

        drawable.setColor(color);

        return drawable;
    }

    // =========================================================
    // CALENDAR CIRCLE
    // =========================================================

    private void setCalendarCircle(TextView textView, int backgroundColor, int textColor) {

        GradientDrawable background = new GradientDrawable();

        background.setShape(GradientDrawable.OVAL);

        background.setColor(backgroundColor);

        textView.setBackground(background);

        textView.setTextColor(textColor);

        textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    }

    // =========================================================
    // BILLING MEMBER SEARCH MODEL
    // =========================================================

    private static class BillingMember {

        String memberId;

        String name;

        String email;

        double memberRate;

        BillingMember(String memberId, String name, String email, double memberRate) {

            this.memberId = memberId;

            this.name = name;

            this.email = email;

            this.memberRate = memberRate;
        }
    }

    // =========================================================
    // SIMPLE FRAME LAYOUT FOR CALENDAR CELLS
    // =========================================================

    private static class FrameLayoutWithDate extends FrameLayout {

        public FrameLayoutWithDate(@NonNull android.content.Context context) {

            super(context);

            setForegroundGravity(Gravity.CENTER);
        }
    }
}