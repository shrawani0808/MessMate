package com.example.messmate.presentation.owner.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.messmate.R;
import com.example.messmate.presentation.auth.SessionManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OwnerDashboardFragment extends Fragment {

    // =========================================================
    // UI
    // =========================================================

    private TextView txtGreeting;
    private TextView txtTodayProgress;
    private TextView txtTodayPercentage;
    private TextView txtOwnerName;
    private TextView txtProfileInitial;
    private TextView txtTodayDate;

    private TextView txtTiffinCollected;
    private TextView txtDinnerCollected;

    private TextView txtTotalMembers;
    private TextView txtPending;

    private TextView txtOverview;

    // =========================================================
    // PROGRESS BARS
    // =========================================================

    private LinearProgressIndicator progressLunch;
    private LinearProgressIndicator progressDinner;
    private LinearProgressIndicator progressToday;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore firestore;
    private SessionManager sessionManager;

    private String ownerId;

    // =========================================================
    // EXISTING COUNTS
    // =========================================================

    private int totalMembers = 0;

    private int tiffinCollected = 0;
    private int dinnerCollected = 0;

    // =========================================================
    // NEW MEAL COUNTS
    // =========================================================

    private int lunchEligible = 0;
    private int dinnerEligible = 0;

    private int lunchPending = 0;
    private int dinnerPending = 0;

    // =========================================================
    // DATA
    // =========================================================

    private final List<MemberInfo> members =
            new ArrayList<>();

    private final Map<String, TodayRecord> todayRecords =
            new HashMap<>();

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
                R.layout.fragment_owner_dashboard,
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

        super.onViewCreated(
                view,
                savedInstanceState
        );

        initializeViews(view);

        firestore =
                FirebaseFirestore.getInstance();

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        ownerId =
                sessionManager.getUid();

        setDate();

        if (ownerId != null &&
                !ownerId.isEmpty()) {

            loadOwnerProfile();

            loadMemberCount();

            loadTodayCollection();

        } else {

            txtOverview.setText(
                    "Owner session not found."
            );
        }
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews(View view) {

        txtGreeting =
                view.findViewById(
                        R.id.txtGreeting
                );

        txtOwnerName =
                view.findViewById(
                        R.id.txtOwnerName
                );

        txtProfileInitial =
                view.findViewById(
                        R.id.txtProfileInitial
                );

        txtTodayDate =
                view.findViewById(
                        R.id.txtTodayDate
                );

        txtTiffinCollected =
                view.findViewById(
                        R.id.txtTiffinCollected
                );

        txtDinnerCollected =
                view.findViewById(
                        R.id.txtDinnerCollected
                );

        txtTotalMembers =
                view.findViewById(
                        R.id.txtTotalMembers
                );

        txtPending =
                view.findViewById(
                        R.id.txtPending
                );
        txtTodayProgress =
                view.findViewById(
                        R.id.txtTodayProgress
                );

        txtTodayPercentage =
                view.findViewById(
                        R.id.txtTodayPercentage
                );

        txtOverview =
                view.findViewById(
                        R.id.txtOverview
                );

        // =====================================================
        // PROGRESS BARS
        // =====================================================

        progressLunch =
                view.findViewById(
                        R.id.progressLunch
                );

        progressDinner =
                view.findViewById(
                        R.id.progressDinner
                );

        progressToday =
                view.findViewById(
                        R.id.collectionProgressBar
                );
    }

    // =========================================================
    // DATE
    // =========================================================

    private void setDate() {

        Date today =
                new Date();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "EEEE, dd MMMM yyyy",
                        Locale.getDefault()
                );

        txtTodayDate.setText(
                dateFormat.format(today)
        );

        java.util.Calendar calendar =
                java.util.Calendar.getInstance();

        int hour =
                calendar.get(
                        java.util.Calendar.HOUR_OF_DAY
                );

        String greeting;

        if (hour < 12) {

            greeting = "Good Morning";

        } else if (hour < 17) {

            greeting = "Good Afternoon";

        } else {

            greeting = "Good Evening";
        }

        txtGreeting.setText(
                greeting
        );
    }

    // =========================================================
    // LOAD OWNER PROFILE
    // =========================================================

    private void loadOwnerProfile() {

        firestore
                .collection("users")
                .document(ownerId)
                .get()

                .addOnSuccessListener(
                        documentSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            if (documentSnapshot.exists()) {

                                String name =
                                        documentSnapshot
                                                .getString(
                                                        "name"
                                                );

                                if (name != null &&
                                        !name.trim().isEmpty()) {

                                    txtOwnerName.setText(
                                            name
                                    );

                                    String firstLetter =
                                            name.substring(
                                                    0,
                                                    1
                                            ).toUpperCase(
                                                    Locale.getDefault()
                                            );

                                    txtProfileInitial.setText(
                                            firstLetter
                                    );

                                } else {

                                    txtOwnerName.setText(
                                            "Owner"
                                    );

                                    txtProfileInitial.setText(
                                            "O"
                                    );
                                }

                            } else {

                                txtOwnerName.setText(
                                        "Owner"
                                );

                                txtProfileInitial.setText(
                                        "O"
                                );
                            }
                        }
                );
    }

    // =========================================================
    // LOAD TOTAL MEMBERS
    // =========================================================

    private void loadMemberCount() {

        firestore
                .collection("members")
                .whereEqualTo(
                        "ownerId",
                        ownerId
                )
                .get()

                .addOnSuccessListener(
                        querySnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            totalMembers =
                                    querySnapshot.size();

                            txtTotalMembers.setText(
                                    String.valueOf(
                                            totalMembers
                                    )
                            );

                            loadTodayCollection();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            totalMembers = 0;

                            txtTotalMembers.setText(
                                    "0"
                            );

                            loadTodayCollection();
                        }
                );
    }

    // =========================================================
    // LOAD TODAY'S COLLECTION
    // =========================================================

    private void loadTodayCollection() {

        String today =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(
                        new Date()
                );

        /*
         * First load members because eligibility depends
         * on lunchEnabled and dinnerEnabled.
         */

        firestore
                .collection("members")
                .whereEqualTo(
                        "ownerId",
                        ownerId
                )
                .get()

                .addOnSuccessListener(
                        memberSnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            members.clear();

                            for (
                                    QueryDocumentSnapshot document :
                                    memberSnapshot
                            ) {

                                MemberInfo member =
                                        new MemberInfo();

                                member.documentId =
                                        document.getId();

                                member.name =
                                        document.getString(
                                                "name"
                                        );

                                Boolean lunch =
                                        document.getBoolean(
                                                "lunchEnabled"
                                        );

                                Boolean dinner =
                                        document.getBoolean(
                                                "dinnerEnabled"
                                        );

                                member.lunchEnabled =
                                        Boolean.TRUE.equals(
                                                lunch
                                        );

                                member.dinnerEnabled =
                                        Boolean.TRUE.equals(
                                                dinner
                                        );

                                members.add(
                                        member
                                );
                            }

                            totalMembers =
                                    members.size();

                            txtTotalMembers.setText(
                                    String.valueOf(
                                            totalMembers
                                    )
                            );

                            loadTodayTiffinRecords(
                                    today
                            );
                        }
                )

                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            members.clear();

                            totalMembers = 0;

                            txtTotalMembers.setText(
                                    "0"
                            );

                            loadTodayTiffinRecords(
                                    today
                            );
                        }
                );
    }

    // =========================================================
    // LOAD TODAY TIFFIN RECORDS
    // =========================================================

    private void loadTodayTiffinRecords(
            String today) {

        firestore
                .collection("tiffin_records")
                .whereEqualTo(
                        "ownerId",
                        ownerId
                )
                .whereEqualTo(
                        "date",
                        today
                )
                .get()

                .addOnSuccessListener(
                        querySnapshot -> {

                            if (!isAdded()) {
                                return;
                            }

                            todayRecords.clear();

                            for (
                                    QueryDocumentSnapshot document :
                                    querySnapshot
                            ) {

                                String memberDocumentId =
                                        document.getString(
                                                "memberDocumentId"
                                        );

                                if (memberDocumentId == null ||
                                        memberDocumentId
                                                .trim()
                                                .isEmpty()) {

                                    continue;
                                }

                                String lunchStatus =
                                        document.getString(
                                                "lunchStatus"
                                        );

                                String dinnerStatus =
                                        document.getString(
                                                "dinnerStatus"
                                        );

                                TodayRecord record =
                                        new TodayRecord();

                                record.lunchStatus =
                                        normalizeStatus(
                                                lunchStatus
                                        );

                                record.dinnerStatus =
                                        normalizeStatus(
                                                dinnerStatus
                                        );

                                todayRecords.put(
                                        memberDocumentId,
                                        record
                                );
                            }

                            calculateCollection();

                        }
                )

                .addOnFailureListener(
                        e -> {

                            if (!isAdded()) {
                                return;
                            }

                            todayRecords.clear();

                            calculateCollection();
                        }
                );
    }

    // =========================================================
    // CALCULATE COLLECTION
    // =========================================================

    private void calculateCollection() {

        // =====================================================
        // RESET
        // =====================================================

        tiffinCollected = 0;

        dinnerCollected = 0;

        lunchEligible = 0;

        dinnerEligible = 0;

        lunchPending = 0;

        dinnerPending = 0;

        // =====================================================
        // CALCULATE FROM MEMBERS
        // =====================================================

        for (MemberInfo member : members) {

            TodayRecord record =
                    todayRecords.get(
                            member.documentId
                    );

            String lunchStatus =
                    "none";

            String dinnerStatus =
                    "none";

            if (record != null) {

                lunchStatus =
                        normalizeStatus(
                                record.lunchStatus
                        );

                dinnerStatus =
                        normalizeStatus(
                                record.dinnerStatus
                        );
            }

            // =================================================
            // LUNCH
            // =================================================

            if (member.lunchEnabled) {

                lunchEligible++;

                if (isCollected(
                        lunchStatus
                )) {

                    tiffinCollected++;

                } else {

                    lunchPending++;
                }
            }

            // =================================================
            // DINNER
            // =================================================

            if (member.dinnerEnabled) {

                dinnerEligible++;

                if (isCollected(
                        dinnerStatus
                )) {

                    dinnerCollected++;

                } else {

                    dinnerPending++;
                }
            }
        }

        // =====================================================
        // UPDATE UI
        // =====================================================

        updateMealCards();

        updateProgressBars();

        updatePendingCount();

        updateOverview();
    }

    // =========================================================
    // UPDATE LUNCH / DINNER CARDS
    // =========================================================

    private void updateMealCards() {

        /*
         * Existing TextView IDs are kept unchanged.
         *
         * txtTiffinCollected now displays:
         *
         * Lunch collected / eligible
         *
         * Example:
         * 8 / 10
         */

        txtTiffinCollected.setText(
                tiffinCollected +
                        " / " +
                        lunchEligible
        );

        /*
         * Dinner:
         *
         * 6 / 8
         */

        txtDinnerCollected.setText(
                dinnerCollected +
                        " / " +
                        dinnerEligible
        );
    }

    // =========================================================
    // UPDATE ALL PROGRESS BARS
    // =========================================================

    private void updateProgressBars() {

        // =====================================================
        // LUNCH PROGRESS
        // =====================================================

        int lunchProgress = 0;

        if (lunchEligible > 0) {

            lunchProgress =
                    Math.round(
                            (tiffinCollected * 100f)
                                    / lunchEligible
                    );
        }

        progressLunch.setProgress(
                lunchProgress
        );

        // =====================================================
        // DINNER PROGRESS
        // =====================================================

        int dinnerProgress = 0;

        if (dinnerEligible > 0) {

            dinnerProgress =
                    Math.round(
                            (dinnerCollected * 100f)
                                    / dinnerEligible
                    );
        }

        progressDinner.setProgress(
                dinnerProgress
        );

        // =====================================================
        // MAIN TODAY'S PROGRESS
        // =====================================================

        int totalCollected =
                tiffinCollected +
                        dinnerCollected;

        int totalEligible =
                lunchEligible +
                        dinnerEligible;

        int todayProgress = 0;

        if (totalEligible > 0) {

            todayProgress =
                    Math.round(
                            (totalCollected * 100f)
                                    / totalEligible
                    );
        }

        progressToday.setProgress(
                todayProgress
        );
    }

    // =========================================================
    // PENDING
    // =========================================================

    private void updatePendingCount() {

        /*
         * Pending means all eligible meals that have
         * not yet been collected.
         *
         * Lunch pending + Dinner pending
         */

        int pending =
                lunchPending +
                        dinnerPending;

        if (pending < 0) {

            pending = 0;
        }

        txtPending.setText(
                String.valueOf(
                        pending
                )
        );
    }

    // =========================================================
    // OVERVIEW
    // =========================================================

    private void updateOverview() {

        int totalCollected =
                tiffinCollected +
                        dinnerCollected;

        int totalEligible =
                lunchEligible +
                        dinnerEligible;

        int totalPending =
                lunchPending +
                        dinnerPending;

        // =====================================================
        // CALCULATE PERCENTAGE
        // =====================================================

        int percentage = 0;

        if (totalEligible > 0) {

            percentage =
                    Math.round(
                            (totalCollected * 100f)
                                    / totalEligible
                    );
        }

        // =====================================================
        // UPDATE "0 of 0"
        // =====================================================

        if (txtTodayProgress != null) {

            txtTodayProgress.setText(
                    totalCollected +
                            " of " +
                            totalEligible +
                            " meals collected today"
            );
        }

        // =====================================================
        // UPDATE "0%"
        // =====================================================

        if (txtTodayPercentage != null) {

            txtTodayPercentage.setText(
                    percentage + "%"
            );
        }

        // =====================================================
        // EXISTING OVERVIEW TEXT
        // =====================================================

        if (totalMembers == 0) {

            txtOverview.setText(
                    "No members have been added yet."
            );

            return;
        }

        if (totalEligible == 0) {

            txtOverview.setText(
                    "No meals are scheduled for today."
            );

            return;
        }

        if (totalPending == 0) {

            txtOverview.setText(
                    "All scheduled meals have been collected today."
            );

            return;
        }

        txtOverview.setText(
                totalCollected +
                        " of " +
                        totalEligible +
                        " scheduled meals collected today."
        );
    }
    // =========================================================
    // CHECK COLLECTION
    // =========================================================

    private boolean isCollected(
            String status) {

        return "full".equalsIgnoreCase(
                status
        )
                ||
                "half".equalsIgnoreCase(
                        status
                );
    }

    // =========================================================
    // NORMALIZE STATUS
    // =========================================================

    private String normalizeStatus(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            return "none";
        }

        return status
                .trim()
                .toLowerCase(
                        Locale.getDefault()
                );
    }

    // =========================================================
    // REFRESH WHEN RETURNING TO DASHBOARD
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (sessionManager == null) {

            sessionManager =
                    new SessionManager(
                            requireContext()
                    );
        }

        ownerId =
                sessionManager.getUid();

        if (ownerId != null &&
                !ownerId.isEmpty()) {

            setDate();

            loadOwnerProfile();

            loadMemberCount();
        }
    }

    // =========================================================
    // MEMBER INFO
    // =========================================================

    private static class MemberInfo {

        String documentId;

        String name;

        boolean lunchEnabled;

        boolean dinnerEnabled;
    }

    // =========================================================
    // TODAY RECORD
    // =========================================================

    private static class TodayRecord {

        String lunchStatus;

        String dinnerStatus;
    }
}