package com.example.messmate.presentation.owner.billing;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.messmate.R;
import com.example.messmate.presentation.auth.SessionManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BillingFragment extends Fragment {

    private TextView tvBillingMonthSubtitle;
    private TextView tvSelectedMonth;

    private LinearLayout memberContainer;

    private ImageButton btnPreviousMonth;
    private ImageButton btnNextMonth;

    private FirebaseFirestore firestore;
    private SessionManager sessionManager;

    private Calendar selectedMonth;

    private double defaultTiffinRate = 50.0;

    private final int GREEN = Color.rgb(47, 132, 100);
    private final int LIGHT_GREEN = Color.rgb(235, 247, 241);
    private final int WHITE = Color.WHITE;
    private final int TEXT_PRIMARY = Color.rgb(35, 35, 35);
    private final int TEXT_SECONDARY = Color.rgb(125, 125, 125);
    private final int BORDER = Color.rgb(232, 236, 234);


    // =========================================================
    // CREATE VIEW
    // =========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_billing,
                container,
                false
        );
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

        sessionManager =
                new SessionManager(requireContext());

        selectedMonth =
                Calendar.getInstance();


        tvBillingMonthSubtitle =
                view.findViewById(
                        R.id.tvBillingMonthSubtitle
                );

        tvSelectedMonth =
                view.findViewById(
                        R.id.tvSelectedMonth
                );

        btnPreviousMonth =
                view.findViewById(
                        R.id.btnPreviousMonth
                );

        btnNextMonth =
                view.findViewById(
                        R.id.btnNextMonth
                );

        memberContainer =
                view.findViewById(
                        R.id.memberContainer
                );


        updateMonthText();

        loadOwnerDefaultRate();


        btnPreviousMonth.setOnClickListener(v -> {

            selectedMonth.add(
                    Calendar.MONTH,
                    -1
            );

            updateMonthText();

            loadMembers();
        });


        btnNextMonth.setOnClickListener(v -> {

            selectedMonth.add(
                    Calendar.MONTH,
                    1
            );

            updateMonthText();

            loadMembers();
        });
    }


    // =========================================================
    // DP CONVERSION
    // =========================================================

    private int dp(float value) {

        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }


    // =========================================================
    // MONTH
    // =========================================================

    private void updateMonthText() {

        String month =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                ).format(
                        selectedMonth.getTime()
                );

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


        firestore
                .collection("owners")
                .document(ownerId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (documentSnapshot.exists()) {

                                Double rate =
                                        documentSnapshot.getDouble(
                                                "tiffinRate"
                                        );

                                if (rate != null && rate > 0) {

                                    defaultTiffinRate = rate;
                                }
                            }

                            loadMembers();
                        }
                )
                .addOnFailureListener(
                        e -> loadMembers()
                );
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


        firestore
                .collection("members")
                .whereEqualTo(
                        "ownerId",
                        ownerId
                )
                .get()
                .addOnSuccessListener(
                        queryDocumentSnapshots -> {

                            if (!isAdded()) {
                                return;
                            }

                            memberContainer.removeAllViews();


                            if (queryDocumentSnapshots.isEmpty()) {

                                showNoMembers();

                                return;
                            }


                            for (
                                    QueryDocumentSnapshot document :
                                    queryDocumentSnapshots
                            ) {

                                String memberId =
                                        document.getId();


                                String name =
                                        document.getString(
                                                "name"
                                        );


                                String email =
                                        document.getString(
                                                "email"
                                        );


                                Double memberRate =
                                        document.getDouble(
                                                "tiffinRate"
                                        );


                                if (
                                        name == null ||
                                                name.trim().isEmpty()
                                ) {

                                    name = "Member";
                                }


                                if (
                                        memberRate == null ||
                                                memberRate <= 0
                                ) {

                                    memberRate =
                                            defaultTiffinRate;

                                    saveInitialMemberRate(
                                            memberId,
                                            memberRate
                                    );
                                }


                                addMemberCard(
                                        memberId,
                                        name,
                                        email,
                                        memberRate
                                );
                            }
                        }
                )
                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load members: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            showNoMembers();
                        }
                );
    }


    // =========================================================
    // SAVE INITIAL MEMBER RATE
    // =========================================================

    private void saveInitialMemberRate(
            String memberId,
            double rate) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "tiffinRate",
                rate
        );

        firestore
                .collection("members")
                .document(memberId)
                .set(
                        data,
                        SetOptions.merge()
                );
    }


    // =========================================================
    // NO MEMBERS
    // =========================================================

    private void showNoMembers() {

        LinearLayout emptyLayout =
                new LinearLayout(
                        requireContext()
                );

        emptyLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        emptyLayout.setGravity(
                Gravity.CENTER
        );

        emptyLayout.setPadding(
                dp(20),
                dp(80),
                dp(20),
                dp(20)
        );


        TextView title =
                new TextView(
                        requireContext()
                );

        title.setText(
                "No members found"
        );

        title.setTextSize(17);

        title.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        title.setTextColor(
                Color.BLACK
        );

        title.setGravity(
                Gravity.CENTER
        );


        TextView subtitle =
                new TextView(
                        requireContext()
                );

        subtitle.setText(
                "Add members first"
        );

        subtitle.setTextSize(14);

        subtitle.setTextColor(
                Color.GRAY
        );

        subtitle.setGravity(
                Gravity.CENTER
        );


        emptyLayout.addView(title);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.setMargins(
                0,
                dp(5),
                0,
                0
        );

        emptyLayout.addView(
                subtitle,
                subtitleParams
        );


        memberContainer.addView(
                emptyLayout,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(300)
                )
        );
    }


    // =========================================================
    // MEMBER CARD
    // =========================================================

    private void addMemberCard(
            String memberId,
            String name,
            String email,
            double memberRate) {


        // -----------------------------------------------------
        // CARD
        // -----------------------------------------------------

        LinearLayout card =
                new LinearLayout(
                        requireContext()
                );

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(15),
                dp(16),
                dp(15)
        );


        GradientDrawable cardBackground =
                new GradientDrawable();

        cardBackground.setColor(WHITE);

        cardBackground.setCornerRadius(
                dp(16)
        );

        cardBackground.setStroke(
                dp(1),
                BORDER
        );


        card.setBackground(
                cardBackground
        );


        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                dp(6),
                0,
                dp(6)
        );


        memberContainer.addView(
                card,
                cardParams
        );


        // -----------------------------------------------------
        // MEMBER HEADER
        // -----------------------------------------------------

        LinearLayout header =
                new LinearLayout(
                        requireContext()
                );

        header.setOrientation(
                LinearLayout.HORIZONTAL
        );

        header.setGravity(
                Gravity.CENTER_VERTICAL
        );


        // INITIAL

        TextView initial =
                new TextView(
                        requireContext()
                );

        initial.setGravity(
                Gravity.CENTER
        );

        initial.setTextSize(
                15
        );

        initial.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        initial.setTextColor(
                GREEN
        );


        String firstLetter = "M";

        if (
                name != null &&
                        !name.trim().isEmpty()
        ) {

            firstLetter =
                    name.trim()
                            .substring(
                                    0,
                                    1
                            )
                            .toUpperCase(
                                    Locale.getDefault()
                            );
        }

        initial.setText(firstLetter);


        GradientDrawable initialBackground =
                new GradientDrawable();

        initialBackground.setShape(
                GradientDrawable.OVAL
        );

        initialBackground.setColor(
                LIGHT_GREEN
        );


        initial.setBackground(
                initialBackground
        );


        LinearLayout.LayoutParams initialParams =
                new LinearLayout.LayoutParams(
                        dp(40),
                        dp(40)
                );


        header.addView(
                initial,
                initialParams
        );


        // -----------------------------------------------------
        // NAME + EMAIL
        // -----------------------------------------------------

        LinearLayout memberInfo =
                new LinearLayout(
                        requireContext()
                );

        memberInfo.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        infoParams.setMargins(
                dp(11),
                0,
                0,
                0
        );


        TextView tvName =
                new TextView(
                        requireContext()
                );

        tvName.setText(name);

        tvName.setTextSize(15);

        tvName.setTextColor(
                TEXT_PRIMARY
        );

        tvName.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        memberInfo.addView(tvName);


        if (
                email != null &&
                        !email.trim().isEmpty()
        ) {

            TextView tvEmail =
                    new TextView(
                            requireContext()
                    );

            tvEmail.setText(email);

            tvEmail.setTextSize(11);

            tvEmail.setTextColor(
                    TEXT_SECONDARY
            );


            LinearLayout.LayoutParams emailParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            emailParams.setMargins(
                    0,
                    dp(2),
                    0,
                    0
            );


            memberInfo.addView(
                    tvEmail,
                    emailParams
            );
        }


        header.addView(
                memberInfo,
                infoParams
        );


        card.addView(header);


        // -----------------------------------------------------
        // RATE ROW
        // -----------------------------------------------------

        LinearLayout rateRow =
                new LinearLayout(
                        requireContext()
                );

        rateRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        rateRow.setGravity(
                Gravity.CENTER_VERTICAL
        );


        LinearLayout.LayoutParams rateRowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rateRowParams.setMargins(
                0,
                dp(14),
                0,
                dp(12)
        );


        // RATE INFO

        LinearLayout rateInfo =
                new LinearLayout(
                        requireContext()
                );

        rateInfo.setOrientation(
                LinearLayout.VERTICAL
        );


        LinearLayout.LayoutParams rateInfoParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );


        TextView rateLabel =
                new TextView(
                        requireContext()
                );

        rateLabel.setText(
                "Tiffin Rate"
        );

        rateLabel.setTextSize(11);

        rateLabel.setTextColor(
                TEXT_SECONDARY
        );


        TextView rateValue =
                new TextView(
                        requireContext()
                );

        rateValue.setText(
                formatRate(memberRate)
        );

        rateValue.setTextSize(15);

        rateValue.setTextColor(
                TEXT_PRIMARY
        );

        rateValue.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );


        LinearLayout.LayoutParams rateValueParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rateValueParams.setMargins(
                0,
                dp(2),
                0,
                0
        );


        rateInfo.addView(rateLabel);

        rateInfo.addView(
                rateValue,
                rateValueParams
        );


        rateRow.addView(
                rateInfo,
                rateInfoParams
        );


        // -----------------------------------------------------
        // EDIT RATE BUTTON
        // -----------------------------------------------------

        Button editRateButton =
                new Button(
                        requireContext()
                );

        editRateButton.setText(
                "Edit Rate"
        );

        editRateButton.setAllCaps(false);

        editRateButton.setTextSize(12);

        editRateButton.setTextColor(
                GREEN
        );

        editRateButton.setGravity(
                Gravity.CENTER
        );

        editRateButton.setMinWidth(0);

        editRateButton.setMinimumWidth(0);

        editRateButton.setMinHeight(0);

        editRateButton.setMinimumHeight(0);

        editRateButton.setPadding(
                dp(13),
                0,
                dp(13),
                0
        );


        GradientDrawable editBackground =
                new GradientDrawable();

        editBackground.setColor(
                LIGHT_GREEN
        );

        editBackground.setCornerRadius(
                dp(20)
        );

        editRateButton.setBackground(
                editBackground
        );


        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        dp(92),
                        dp(36)
                );


        editRateButton.setOnClickListener(
                v -> {

                    showEditMemberRateDialog(
                            memberId,
                            rateValue
                    );
                }
        );


        rateRow.addView(
                editRateButton,
                editParams
        );


        card.addView(
                rateRow,
                rateRowParams
        );


        // -----------------------------------------------------
        // DIVIDER
        // -----------------------------------------------------

        View divider =
                new View(
                        requireContext()
                );

        divider.setBackgroundColor(
                Color.rgb(240, 242, 241)
        );


        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(1)
                );

        dividerParams.setMargins(
                0,
                0,
                0,
                dp(12)
        );


        card.addView(
                divider,
                dividerParams
        );


        // -----------------------------------------------------
        // CALCULATE MONTHLY BILL BUTTON
        // -----------------------------------------------------

        Button calculateButton =
                new Button(
                        requireContext()
                );

        calculateButton.setText(
                "Calculate Monthly Bill"
        );

        calculateButton.setAllCaps(false);

        calculateButton.setTextSize(13);

        calculateButton.setTextColor(
                WHITE
        );

        calculateButton.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        calculateButton.setGravity(
                Gravity.CENTER
        );

        calculateButton.setMinWidth(0);

        calculateButton.setMinimumWidth(0);

        calculateButton.setMinHeight(0);

        calculateButton.setMinimumHeight(0);

        calculateButton.setPadding(
                dp(16),
                0,
                dp(16),
                0
        );


        GradientDrawable calculateBackground =
                new GradientDrawable();

        calculateBackground.setColor(
                GREEN
        );

        calculateBackground.setCornerRadius(
                dp(12)
        );


        calculateButton.setBackground(
                calculateBackground
        );


        LinearLayout.LayoutParams calculateParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(44)
                );

        calculateParams.gravity =
                Gravity.CENTER_HORIZONTAL;


        final String finalName = name;

        final String finalEmail = email;


        calculateButton.setOnClickListener(
                v -> {

                    calculateMonthlyBill(
                            memberId,
                            finalName,
                            finalEmail
                    );
                }
        );


        card.addView(
                calculateButton,
                calculateParams
        );
    }


    // =========================================================
    // FORMAT RATE
    // =========================================================

    private String formatRate(double rate) {

        return String.format(
                Locale.getDefault(),
                "₹ %.2f / tiffin",
                rate
        );
    }


    // =========================================================
    // EDIT MEMBER RATE DIALOG
    // =========================================================

    private void showEditMemberRateDialog(
            String memberId,
            TextView rateValue) {

        EditText input =
                new EditText(
                        requireContext()
                );


        input.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        input.setHint(
                "Enter rate per tiffin"
        );

        input.setSingleLine(true);

        input.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );


        firestore
                .collection("members")
                .document(memberId)
                .get()
                .addOnSuccessListener(
                        documentSnapshot -> {

                            Double currentRate =
                                    documentSnapshot.getDouble(
                                            "tiffinRate"
                                    );


                            if (currentRate != null) {

                                input.setText(
                                        String.valueOf(
                                                currentRate
                                        )
                                );

                                input.setSelection(
                                        input.length()
                                );
                            }


                            AlertDialog dialog =
                                    new AlertDialog.Builder(
                                            requireContext()
                                    )
                                            .setTitle(
                                                    "Edit Tiffin Rate"
                                            )
                                            .setMessage(
                                                    "Set the rate for this member"
                                            )
                                            .setView(input)
                                            .setNegativeButton(
                                                    "Cancel",
                                                    null
                                            )
                                            .setPositiveButton(
                                                    "Save",
                                                    null
                                            )
                                            .create();


                            dialog.setOnShowListener(
                                    d -> {

                                        dialog.getButton(
                                                        AlertDialog.BUTTON_POSITIVE
                                                )
                                                .setOnClickListener(
                                                        v -> {

                                                            String value =
                                                                    input.getText()
                                                                            .toString()
                                                                            .trim();


                                                            if (value.isEmpty()) {

                                                                input.setError(
                                                                        "Enter rate"
                                                                );

                                                                return;
                                                            }


                                                            double newRate;


                                                            try {

                                                                newRate =
                                                                        Double.parseDouble(
                                                                                value
                                                                        );

                                                            } catch (Exception e) {

                                                                input.setError(
                                                                        "Invalid rate"
                                                                );

                                                                return;
                                                            }


                                                            if (newRate <= 0) {

                                                                input.setError(
                                                                        "Rate must be greater than 0"
                                                                );

                                                                return;
                                                            }


                                                            saveMemberRate(
                                                                    memberId,
                                                                    newRate,
                                                                    rateValue,
                                                                    dialog
                                                            );
                                                        }
                                                );
                                    }
                            );


                            dialog.show();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load member rate",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                );
    }


    // =========================================================
    // SAVE MEMBER RATE
    // =========================================================

    private void saveMemberRate(
            String memberId,
            double newRate,
            TextView rateValue,
            AlertDialog dialog) {

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "tiffinRate",
                newRate
        );


        firestore
                .collection("members")
                .document(memberId)
                .set(
                        data,
                        SetOptions.merge()
                )
                .addOnSuccessListener(
                        unused -> {

                            rateValue.setText(
                                    formatRate(newRate)
                            );


                            Toast.makeText(
                                    requireContext(),
                                    "Tiffin rate updated",
                                    Toast.LENGTH_SHORT
                            ).show();


                            dialog.dismiss();
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to update rate: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // CALCULATE MONTHLY BILL
    // =========================================================

    private void calculateMonthlyBill(
            String memberId,
            String memberName,
            String email) {

        String ownerId = getOwnerId();


        if (ownerId == null || ownerId.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Owner session not found",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        firestore
                .collection("members")
                .document(memberId)
                .get()
                .addOnSuccessListener(
                        memberDocument -> {

                            Double memberRate =
                                    memberDocument.getDouble(
                                            "tiffinRate"
                                    );


                            if (
                                    memberRate == null ||
                                            memberRate <= 0
                            ) {

                                memberRate =
                                        defaultTiffinRate;
                            }


                            calculateBillFromRecords(
                                    ownerId,
                                    memberId,
                                    memberName,
                                    email,
                                    memberRate
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to load member rate: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // CALCULATE FROM TIFFIN RECORDS
    // =========================================================

    private void calculateBillFromRecords(
            String ownerId,
            String memberId,
            String memberName,
            String email,
            double memberRate) {


        Calendar start =
                (Calendar) selectedMonth.clone();


        start.set(
                Calendar.DAY_OF_MONTH,
                1
        );

        start.set(
                Calendar.HOUR_OF_DAY,
                0
        );

        start.set(
                Calendar.MINUTE,
                0
        );

        start.set(
                Calendar.SECOND,
                0
        );

        start.set(
                Calendar.MILLISECOND,
                0
        );


        Calendar end =
                (Calendar) start.clone();

        end.add(
                Calendar.MONTH,
                1
        );


        /*
         * IMPORTANT:
         *
         * Your TiffinFragment stores records in:
         *
         * tiffin_records
         *
         * and stores the date as:
         *
         * yyyy-MM-dd
         *
         * Therefore we read that collection here.
         */

        firestore
                .collection("tiffin_records")
                .whereEqualTo(
                        "ownerId",
                        ownerId
                )
                .whereEqualTo(
                        "memberDocumentId",
                        memberId
                )
                .get()
                .addOnSuccessListener(
                        query -> {

                            double totalTiffins = 0;

                            int totalDays = 0;

                            int fullTiffins = 0;

                            int halfTiffins = 0;


                            for (
                                    QueryDocumentSnapshot document :
                                    query
                            ) {

                                String dateString =
                                        document.getString(
                                                "date"
                                        );


                                if (
                                        dateString == null ||
                                                dateString.trim().isEmpty()
                                ) {

                                    continue;
                                }


                                Date recordDate;

                                try {

                                    recordDate =
                                            new SimpleDateFormat(
                                                    "yyyy-MM-dd",
                                                    Locale.getDefault()
                                            ).parse(
                                                    dateString
                                            );

                                } catch (Exception e) {

                                    continue;
                                }


                                if (recordDate == null) {
                                    continue;
                                }


                                Calendar recordCalendar =
                                        Calendar.getInstance();

                                recordCalendar.setTime(
                                        recordDate
                                );


                                /*
                                 * Compare year + month directly.
                                 *
                                 * This is safer because your
                                 * tiffin_records date is stored
                                 * as a String.
                                 */

                                boolean sameMonth =
                                        recordCalendar.get(
                                                Calendar.YEAR
                                        ) ==
                                                selectedMonth.get(
                                                        Calendar.YEAR
                                                )
                                                &&
                                                recordCalendar.get(
                                                        Calendar.MONTH
                                                ) ==
                                                        selectedMonth.get(
                                                                Calendar.MONTH
                                                        );


                                if (!sameMonth) {
                                    continue;
                                }


                                String tiffin =
                                        document.getString(
                                                "tiffin"
                                        );


                                if ("full".equalsIgnoreCase(tiffin)) {

                                    fullTiffins++;

                                    totalTiffins += 1.0;

                                    totalDays++;

                                } else if (
                                        "half".equalsIgnoreCase(tiffin)
                                ) {

                                    halfTiffins++;

                                    totalTiffins += 0.5;

                                    totalDays++;
                                }
                            }


                            double totalAmount =
                                    totalTiffins * memberRate;


                            showBillDialog(
                                    memberName,
                                    email,
                                    totalTiffins,
                                    totalDays,
                                    fullTiffins,
                                    halfTiffins,
                                    memberRate,
                                    totalAmount
                            );
                        }
                )
                .addOnFailureListener(
                        e -> {

                            Toast.makeText(
                                    requireContext(),
                                    "Failed to calculate bill: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }


    // =========================================================
    // BILL DIALOG
    // =========================================================

    private void showBillDialog(
            String memberName,
            String email,
            double totalTiffins,
            int totalDays,
            int fullTiffins,
            int halfTiffins,
            double memberRate,
            double totalAmount) {


        String month =
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.getDefault()
                ).format(
                        selectedMonth.getTime()
                );


        String message =
                "Member: "
                        + memberName
                        + "\n\n"
                        + "Month: "
                        + month
                        + "\n"
                        + "Tiffin Rate: ₹ "
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        memberRate
                )
                        + "\n"
                        + "Full Tiffins: "
                        + fullTiffins
                        + "\n"
                        + "Half Tiffins: "
                        + halfTiffins
                        + "\n"
                        + "Tiffin Quantity: "
                        + String.format(
                        Locale.getDefault(),
                        "%.1f",
                        totalTiffins
                )
                        + "\n"
                        + "Collected Days: "
                        + totalDays
                        + "\n\n"
                        + "TOTAL BILL: ₹ "
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        totalAmount
                );


        final double finalMemberRate =
                memberRate;


        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "Monthly Bill"
                )
                .setMessage(
                        message
                )
                .setNegativeButton(
                        "Close",
                        null
                )
                .setPositiveButton(
                        "Generate PDF",
                        (dialog, which) -> {

                            generatePdf(
                                    memberName,
                                    email,
                                    month,
                                    totalTiffins,
                                    totalDays,
                                    finalMemberRate,
                                    totalAmount
                            );
                        }
                )
                .show();
    }


    // =========================================================
    // GENERATE PDF
    // =========================================================

    private void generatePdf(
            String memberName,
            String email,
            String month,
            double totalTiffins,
            int totalDays,
            double memberRate,
            double totalAmount) {


        PdfDocument pdfDocument =
                new PdfDocument();


        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        1
                ).create();


        PdfDocument.Page page =
                pdfDocument.startPage(
                        pageInfo
                );


        android.graphics.Canvas canvas =
                page.getCanvas();


        android.graphics.Paint paint =
                new android.graphics.Paint();


        paint.setColor(Color.BLACK);

        paint.setTextSize(24);

        paint.setTypeface(
                Typeface.DEFAULT_BOLD
        );


        canvas.drawText(
                "MESSMATE",
                50,
                60,
                paint
        );


        paint.setTextSize(20);


        canvas.drawText(
                "Monthly Tiffin Bill",
                50,
                100,
                paint
        );


        paint.setTypeface(
                Typeface.DEFAULT
        );

        paint.setTextSize(15);


        int y = 150;


        canvas.drawText(
                "Member: " + memberName,
                50,
                y,
                paint
        );


        y += 30;


        if (
                email != null &&
                        !email.trim().isEmpty()
        ) {

            canvas.drawText(
                    "Email: " + email,
                    50,
                    y,
                    paint
            );

            y += 30;
        }


        canvas.drawText(
                "Billing Month: " + month,
                50,
                y,
                paint
        );


        y += 50;


        canvas.drawText(
                "Tiffin Rate:",
                50,
                y,
                paint
        );


        canvas.drawText(
                "₹ "
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        memberRate
                ),
                400,
                y,
                paint
        );


        y += 35;


        canvas.drawText(
                "Collected Days:",
                50,
                y,
                paint
        );


        canvas.drawText(
                String.valueOf(totalDays),
                400,
                y,
                paint
        );


        y += 35;


        canvas.drawText(
                "Tiffin Quantity:",
                50,
                y,
                paint
        );


        canvas.drawText(
                String.format(
                        Locale.getDefault(),
                        "%.1f",
                        totalTiffins
                ),
                400,
                y,
                paint
        );


        y += 60;


        paint.setTypeface(
                Typeface.DEFAULT_BOLD
        );

        paint.setTextSize(20);


        canvas.drawText(
                "TOTAL: ₹ "
                        + String.format(
                        Locale.getDefault(),
                        "%.2f",
                        totalAmount
                ),
                50,
                y,
                paint
        );


        paint.setTypeface(
                Typeface.DEFAULT
        );

        paint.setTextSize(12);


        canvas.drawText(
                "Generated by MessMate",
                50,
                790,
                paint
        );


        pdfDocument.finishPage(page);


        String safeName =
                memberName.replace(
                        " ",
                        "_"
                );


        String safeMonth =
                month.replace(
                        " ",
                        "_"
                );


        String fileName =
                "MessMate_Bill_"
                        + safeName
                        + "_"
                        + safeMonth
                        + ".pdf";


        try {

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

                Toast.makeText(
                        requireContext(),
                        "Could not create PDF",
                        Toast.LENGTH_LONG
                ).show();


                pdfDocument.close();

                return;
            }


            OutputStream outputStream =
                    requireContext()
                            .getContentResolver()
                            .openOutputStream(uri);


            if (outputStream != null) {

                pdfDocument.writeTo(
                        outputStream
                );

                outputStream.close();
            }


            pdfDocument.close();


            Toast.makeText(
                    requireContext(),
                    "Bill PDF saved in Downloads",
                    Toast.LENGTH_LONG
            ).show();


        } catch (Exception e) {

            pdfDocument.close();


            Toast.makeText(
                    requireContext(),
                    "PDF error: "
                            + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}