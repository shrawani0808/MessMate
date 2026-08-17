package com.example.messmate.presentation.owner.tiffin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.auth.SessionManager;
import com.example.messmate.presentation.owner.members.model.Member;
import com.example.messmate.presentation.owner.tiffin.adapters.TiffinAdapter;
import com.example.messmate.presentation.owner.tiffin.modules.TiffinRecord;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TiffinFragment extends Fragment {

    // =========================================================
    // UI
    // =========================================================

    private TextView txtTodayDate;

    private EditText etSearchTiffin;

    private ImageButton btnFilter;

    // Member type
    private TextView txtMonthlyMembers;
    private TextView txtDailyMembers;

    private View monthlyIndicator;
    private View dailyIndicator;

    // Sections
    private LinearLayout monthlySection;
    private LinearLayout dailySection;

    private TextView txtMonthlySectionTitle;
    private TextView txtDailySectionTitle;

    private RecyclerView recyclerMonthly;
    private RecyclerView recyclerDaily;

    private LinearLayout emptyLayout;

    // =========================================================
    // SUMMARY
    // =========================================================

    private TextView txtLunchCollectedCount;
    private TextView txtLunchFullCount;
    private TextView txtLunchHalfCount;
    private TextView txtLunchNotCount;

    private TextView txtDinnerCollectedCount;
    private TextView txtDinnerFullCount;
    private TextView txtDinnerHalfCount;
    private TextView txtDinnerNotCount;

    private TextView txtTotalMembers;
    private TextView txtNotCollectedCount;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore firestore;

    private SessionManager sessionManager;

    private String ownerId;

    private String todayDate;

    // =========================================================
    // DATA
    // =========================================================

    private final List<Member> allMembers =
            new ArrayList<>();

    private final List<Member> monthlyMembers =
            new ArrayList<>();

    private final List<Member> dailyMembers =
            new ArrayList<>();

    private final List<Member> displayedMonthlyMembers =
            new ArrayList<>();

    private final List<Member> displayedDailyMembers =
            new ArrayList<>();

    private final Map<String, TiffinRecord> todayRecords =
            new HashMap<>();

    private TiffinAdapter monthlyAdapter;

    private TiffinAdapter dailyAdapter;

    // =========================================================
    // FILTER STATE
    // =========================================================

    /*
     * 0 = Both
     * 1 = Lunch Only
     * 2 = Dinner Only
     */
    private int mealFilter = 0;

    /*
     * 1 = Monthly
     * 2 = Daily
     */
    private int memberTypeFilter = 1;

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
                R.layout.fragment_tiffin,
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

        todayDate =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(
                        new Date()
                );

        txtTodayDate.setText(
                new SimpleDateFormat(
                        "EEEE, dd MMMM yyyy",
                        Locale.getDefault()
                ).format(
                        new Date()
                )
        );

        setupRecyclerViews();

        setupMemberTypeFilters();

        setupSearch();

        updateMemberTypeUI();

        if (ownerId != null &&
                !ownerId.isEmpty()) {

            loadMembers();

        } else {

            showToast(
                    "Owner session not found"
            );
        }
    }

    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initializeViews(View view) {

        txtTodayDate =
                view.findViewById(
                        R.id.txtTodayDate
                );

        etSearchTiffin =
                view.findViewById(
                        R.id.etSearchTiffin
                );

        btnFilter =
                view.findViewById(
                        R.id.btnFilter
                );

        // =====================================================
        // MEMBER TYPE
        // =====================================================

        txtMonthlyMembers =
                view.findViewById(
                        R.id.txtMonthlyMembers
                );

        txtDailyMembers =
                view.findViewById(
                        R.id.txtDailyMembers
                );

        monthlyIndicator =
                view.findViewById(
                        R.id.monthlyIndicator
                );

        dailyIndicator =
                view.findViewById(
                        R.id.dailyIndicator
                );

        // =====================================================
        // SECTIONS
        // =====================================================

        txtMonthlySectionTitle =
                view.findViewById(
                        R.id.txtMonthlySectionTitle
                );

        txtDailySectionTitle =
                view.findViewById(
                        R.id.txtDailySectionTitle
                );

        monthlySection =
                view.findViewById(
                        R.id.monthlySection
                );

        dailySection =
                view.findViewById(
                        R.id.dailySection
                );

        recyclerMonthly =
                view.findViewById(
                        R.id.recyclerMonthly
                );

        recyclerDaily =
                view.findViewById(
                        R.id.recyclerDaily
                );

        emptyLayout =
                view.findViewById(
                        R.id.emptyLayout
                );

        // =====================================================
        // SUMMARY
        // =====================================================

        txtLunchCollectedCount =
                view.findViewById(
                        R.id.txtLunchCollectedCount
                );

        txtLunchFullCount =
                view.findViewById(
                        R.id.txtLunchFullCount
                );

        txtLunchHalfCount =
                view.findViewById(
                        R.id.txtLunchHalfCount
                );

        txtLunchNotCount =
                view.findViewById(
                        R.id.txtLunchNotCount
                );

        txtDinnerCollectedCount =
                view.findViewById(
                        R.id.txtDinnerCollectedCount
                );

        txtDinnerFullCount =
                view.findViewById(
                        R.id.txtDinnerFullCount
                );

        txtDinnerHalfCount =
                view.findViewById(
                        R.id.txtDinnerHalfCount
                );

        txtDinnerNotCount =
                view.findViewById(
                        R.id.txtDinnerNotCount
                );

        txtTotalMembers =
                view.findViewById(
                        R.id.txtTotalMembers
                );

        txtNotCollectedCount =
                view.findViewById(
                        R.id.txtNotCollectedCount
                );

        // =====================================================
        // FILTER BUTTON
        // =====================================================

        btnFilter.setOnClickListener(
                v -> showFilterDialog()
        );
    }

    // =========================================================
    // RECYCLER VIEWS
    // =========================================================

    private void setupRecyclerViews() {

        recyclerMonthly.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerDaily.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        recyclerMonthly.setNestedScrollingEnabled(
                false
        );

        recyclerDaily.setNestedScrollingEnabled(
                false
        );

        monthlyAdapter =
                new TiffinAdapter(
                        displayedMonthlyMembers,
                        createStatusListener()
                );

        dailyAdapter =
                new TiffinAdapter(
                        displayedDailyMembers,
                        createStatusListener()
                );

        recyclerMonthly.setAdapter(
                monthlyAdapter
        );

        recyclerDaily.setAdapter(
                dailyAdapter
        );
    }

    // =========================================================
    // STATUS LISTENER
    // =========================================================

    private TiffinAdapter.OnStatusClickListener
    createStatusListener() {

        return new TiffinAdapter.OnStatusClickListener() {

            @Override
            public void onLunchStatusClicked(
                    Member member,
                    String selectedStatus) {

                saveLunchStatus(
                        member,
                        selectedStatus
                );
            }

            @Override
            public void onDinnerStatusClicked(
                    Member member,
                    String selectedStatus) {

                saveDinnerStatus(
                        member,
                        selectedStatus
                );
            }
        };
    }

    // =========================================================
    // FILTER DIALOG
    // =========================================================

    private void showFilterDialog() {

        View dialogView =
                LayoutInflater.from(
                        requireContext()
                ).inflate(
                        R.layout.dialog_filter_meals,
                        null
                );

        RadioButton rbBoth =
                dialogView.findViewById(
                        R.id.rbBoth
                );

        RadioButton rbLunchOnly =
                dialogView.findViewById(
                        R.id.rbLunchOnly
                );

        RadioButton rbDinnerOnly =
                dialogView.findViewById(
                        R.id.rbDinnerOnly
                );

        MaterialButton btnCancel =
                dialogView.findViewById(
                        R.id.btnCancelFilter
                );

        MaterialButton btnApply =
                dialogView.findViewById(
                        R.id.btnApplyFilter
                );

        if (mealFilter == 0) {

            rbBoth.setChecked(true);

        } else if (mealFilter == 1) {

            rbLunchOnly.setChecked(true);

        } else {

            rbDinnerOnly.setChecked(true);
        }

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setView(
                                dialogView
                        )
                        .create();

        btnCancel.setOnClickListener(
                v -> dialog.dismiss()
        );

        btnApply.setOnClickListener(
                v -> {

                    if (rbBoth.isChecked()) {

                        mealFilter = 0;

                    } else if (rbLunchOnly.isChecked()) {

                        mealFilter = 1;

                    } else if (rbDinnerOnly.isChecked()) {

                        mealFilter = 2;
                    }

                    filterMembers();

                    dialog.dismiss();
                }
        );

        dialog.show();

        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setBackgroundDrawableResource(
                            android.R.color.transparent
                    );

            dialog.getWindow().setLayout(
                    (int) (
                            350 *
                                    getResources()
                                            .getDisplayMetrics()
                                            .density
                    ),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // =========================================================
    // MEMBER TYPE FILTER
    // =========================================================

    private void setupMemberTypeFilters() {

        txtMonthlyMembers.setOnClickListener(
                v -> {

                    memberTypeFilter = 1;

                    updateMemberTypeUI();

                    filterMembers();
                }
        );

        txtDailyMembers.setOnClickListener(
                v -> {

                    memberTypeFilter = 2;

                    updateMemberTypeUI();

                    filterMembers();
                }
        );
    }

    // =========================================================
    // MEMBER TYPE UI
    // =========================================================

    private void updateMemberTypeUI() {

        if (!isAdded()) {
            return;
        }

        int green =
                ContextCompat.getColor(
                        requireContext(),
                        R.color.primary
                );

        int orange =
                ContextCompat.getColor(
                        requireContext(),
                        R.color.tiffin_orange
                );

        int dark =
                ContextCompat.getColor(
                        requireContext(),
                        R.color.text_primary
                );

        txtMonthlyMembers.setTextColor(
                memberTypeFilter == 1
                        ? green
                        : dark
        );

        txtDailyMembers.setTextColor(
                memberTypeFilter == 2
                        ? orange
                        : dark
        );

        monthlyIndicator.setVisibility(
                memberTypeFilter == 1
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        dailyIndicator.setVisibility(
                memberTypeFilter == 2
                        ? View.VISIBLE
                        : View.INVISIBLE
        );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        etSearchTiffin.addTextChangedListener(
                new TextWatcher() {

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

                        filterMembers();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );
    }

    // =========================================================
    // FILTER MEMBERS
    // =========================================================

    private void filterMembers() {

        if (monthlyAdapter == null ||
                dailyAdapter == null) {
            return;
        }

        String searchText =
                etSearchTiffin
                        .getText()
                        .toString()
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        displayedMonthlyMembers.clear();

        displayedDailyMembers.clear();

        for (Member member : allMembers) {

            if (!matchesSearch(
                    member,
                    searchText
            )) {
                continue;
            }

            if (!matchesMealFilter(
                    member
            )) {
                continue;
            }

            boolean monthly =
                    "monthly".equalsIgnoreCase(
                            member.getPaymentType()
                    );

            if (monthly &&
                    memberTypeFilter == 1) {

                displayedMonthlyMembers.add(
                        member
                );
            }

            if (!monthly &&
                    memberTypeFilter == 2) {

                displayedDailyMembers.add(
                        member
                );
            }
        }

        monthlyAdapter.notifyDataSetChanged();

        dailyAdapter.notifyDataSetChanged();

        updateSectionUI();
    }

    // =========================================================
    // SEARCH MATCH
    // =========================================================

    private boolean matchesSearch(
            Member member,
            String searchText) {

        if (searchText.isEmpty()) {
            return true;
        }

        String name =
                member.getName() == null
                        ? ""
                        : member.getName()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        String phone =
                member.getPhone() == null
                        ? ""
                        : member.getPhone()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        String email =
                member.getEmail() == null
                        ? ""
                        : member.getEmail()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        return name.contains(searchText)
                || phone.contains(searchText)
                || email.contains(searchText);
    }

    // =========================================================
    // MEAL FILTER MATCH
    // =========================================================

    private boolean matchesMealFilter(
            Member member) {

        boolean lunch =
                member.isLunchEnabled();

        boolean dinner =
                member.isDinnerEnabled();

        if (mealFilter == 0) {
            return lunch || dinner;
        }

        if (mealFilter == 1) {
            return lunch;
        }

        if (mealFilter == 2) {
            return dinner;
        }

        return true;
    }

    // =========================================================
    // SECTION UI
    // =========================================================

    private void updateSectionUI() {

        boolean showMonthly =
                !displayedMonthlyMembers.isEmpty();

        boolean showDaily =
                !displayedDailyMembers.isEmpty();

        monthlySection.setVisibility(
                showMonthly
                        ? View.VISIBLE
                        : View.GONE
        );

        dailySection.setVisibility(
                showDaily
                        ? View.VISIBLE
                        : View.GONE
        );

        if (!showMonthly &&
                !showDaily) {

            emptyLayout.setVisibility(
                    View.VISIBLE
            );

        } else {

            emptyLayout.setVisibility(
                    View.GONE
            );
        }

        String mealText;

        if (mealFilter == 1) {

            mealText = "Lunch Only";

        } else if (mealFilter == 2) {

            mealText = "Dinner Only";

        } else {

            mealText = "Both";
        }

        txtMonthlySectionTitle.setText(
                "Monthly Members (" +
                        mealText +
                        ")"
        );

        txtDailySectionTitle.setText(
                "Daily Members (" +
                        mealText +
                        ")"
        );
    }

    // =========================================================
    // LOAD MEMBERS
    // =========================================================

    private void loadMembers() {

        firestore
                .collection("members")
                .whereEqualTo(
                        "ownerId",
                        ownerId
                )
                .get()

                .addOnSuccessListener(
                        querySnapshot -> {

                            allMembers.clear();

                            monthlyMembers.clear();

                            dailyMembers.clear();

                            for (
                                    QueryDocumentSnapshot document :
                                    querySnapshot
                            ) {

                                Member member =
                                        document.toObject(
                                                Member.class
                                        );

                                member.setDocumentId(
                                        document.getId()
                                );

                                normalizeOldMember(
                                        member
                                );

                                allMembers.add(
                                        member
                                );

                                if (
                                        "monthly".equalsIgnoreCase(
                                                member.getPaymentType()
                                        )
                                ) {

                                    monthlyMembers.add(
                                            member
                                    );

                                } else {

                                    dailyMembers.add(
                                            member
                                    );
                                }
                            }

                            updateTotalMemberCount();

                            loadTodayRecords();
                        }
                )

                .addOnFailureListener(
                        e -> showToast(
                                "Failed to load members"
                        )
                );
    }

    // =========================================================
    // NORMALIZE OLD MEMBER
    // =========================================================

    private void normalizeOldMember(
            Member member) {

        if (member.getPaymentType() == null ||
                member.getPaymentType()
                        .trim()
                        .isEmpty()) {

            member.setPaymentType(
                    "daily"
            );
        }
    }

    // =========================================================
    // TOTAL MEMBERS
    // =========================================================

    private void updateTotalMemberCount() {

        txtTotalMembers.setText(
                "Total Members: " +
                        allMembers.size()
        );
    }

    // =========================================================
    // LOAD TODAY RECORDS
    // =========================================================

    private void loadTodayRecords() {

        firestore
                .collection("tiffin_records")
                .whereEqualTo(
                        "ownerId",
                        ownerId
                )
                .whereEqualTo(
                        "date",
                        todayDate
                )
                .get()

                .addOnSuccessListener(
                        querySnapshot -> {

                            todayRecords.clear();

                            for (
                                    QueryDocumentSnapshot document :
                                    querySnapshot
                            ) {

                                TiffinRecord record =
                                        document.toObject(
                                                TiffinRecord.class
                                        );

                                record.setDocumentId(
                                        document.getId()
                                );

                                String memberDocumentId =
                                        record.getMemberDocumentId();

                                if (memberDocumentId != null &&
                                        !memberDocumentId
                                                .trim()
                                                .isEmpty()) {

                                    todayRecords.put(
                                            memberDocumentId,
                                            record
                                    );
                                }
                            }

                            monthlyAdapter.setTodayRecords(
                                    todayRecords
                            );

                            dailyAdapter.setTodayRecords(
                                    todayRecords
                            );

                            updateSummary();

                            filterMembers();
                        }
                )

                .addOnFailureListener(
                        e -> {

                            todayRecords.clear();

                            monthlyAdapter.setTodayRecords(
                                    todayRecords
                            );

                            dailyAdapter.setTodayRecords(
                                    todayRecords
                            );

                            updateSummary();

                            filterMembers();
                        }
                );
    }

    // =========================================================
    // SUMMARY
    // =========================================================

    private void updateSummary() {

        int lunchFull = 0;
        int lunchHalf = 0;
        int lunchNot = 0;

        int dinnerFull = 0;
        int dinnerHalf = 0;
        int dinnerNot = 0;

        int noMeal = 0;

        for (Member member : allMembers) {

            String memberId =
                    member.getDocumentId();

            TiffinRecord record =
                    todayRecords.get(memberId);

            String lunchStatus =
                    getLunchStatus(record);

            String dinnerStatus =
                    getDinnerStatus(record);

            if (member.isLunchEnabled()) {

                if ("full".equalsIgnoreCase(
                        lunchStatus
                )) {

                    lunchFull++;

                } else if ("half".equalsIgnoreCase(
                        lunchStatus
                )) {

                    lunchHalf++;

                } else {

                    lunchNot++;
                }
            }

            if (member.isDinnerEnabled()) {

                if ("full".equalsIgnoreCase(
                        dinnerStatus
                )) {

                    dinnerFull++;

                } else if ("half".equalsIgnoreCase(
                        dinnerStatus
                )) {

                    dinnerHalf++;

                } else {

                    dinnerNot++;
                }
            }

            boolean lunchCollected =
                    "full".equalsIgnoreCase(
                            lunchStatus
                    ) ||
                            "half".equalsIgnoreCase(
                                    lunchStatus
                            );

            boolean dinnerCollected =
                    "full".equalsIgnoreCase(
                            dinnerStatus
                    ) ||
                            "half".equalsIgnoreCase(
                                    dinnerStatus
                            );

            if (!lunchCollected &&
                    !dinnerCollected) {

                noMeal++;
            }
        }

        int lunchCollected =
                lunchFull + lunchHalf;

        int dinnerCollected =
                dinnerFull + dinnerHalf;

        txtLunchCollectedCount.setText(
                String.valueOf(
                        lunchCollected
                )
        );

        txtLunchFullCount.setText(
                "Full: " + lunchFull
        );

        txtLunchHalfCount.setText(
                "Half: " + lunchHalf
        );

        txtLunchNotCount.setText(
                "Not: " + lunchNot
        );

        txtDinnerCollectedCount.setText(
                String.valueOf(
                        dinnerCollected
                )
        );

        txtDinnerFullCount.setText(
                "Full: " + dinnerFull
        );

        txtDinnerHalfCount.setText(
                "Half: " + dinnerHalf
        );

        txtDinnerNotCount.setText(
                "Not: " + dinnerNot
        );

        txtNotCollectedCount.setText(
                String.valueOf(
                        noMeal
                )
        );
    }

    // =========================================================
    // LUNCH STATUS
    // =========================================================

    private String getLunchStatus(
            TiffinRecord record) {

        if (record == null) {
            return "none";
        }

        String status =
                record.getLunchStatus();

        if (status == null ||
                status.trim().isEmpty()) {

            return "none";
        }

        return status;
    }

    // =========================================================
    // DINNER STATUS
    // =========================================================

    private String getDinnerStatus(
            TiffinRecord record) {

        if (record == null) {
            return "none";
        }

        String status =
                record.getDinnerStatus();

        if (status == null ||
                status.trim().isEmpty()) {

            return "none";
        }

        return status;
    }

    // =========================================================
    // SAVE LUNCH
    // =========================================================

    private void saveLunchStatus(
            Member member,
            String lunchStatus) {

        saveRecord(
                member,
                lunchStatus,
                getCurrentDinnerStatus(
                        member
                )
        );
    }

    // =========================================================
    // SAVE DINNER
    // =========================================================

    private void saveDinnerStatus(
            Member member,
            String dinnerStatus) {

        saveRecord(
                member,
                getCurrentLunchStatus(
                        member
                ),
                dinnerStatus
        );
    }

    // =========================================================
    // CURRENT LUNCH
    // =========================================================

    private String getCurrentLunchStatus(
            Member member) {

        TiffinRecord record =
                todayRecords.get(
                        member.getDocumentId()
                );

        return getLunchStatus(
                record
        );
    }

    // =========================================================
    // CURRENT DINNER
    // =========================================================

    private String getCurrentDinnerStatus(
            Member member) {

        TiffinRecord record =
                todayRecords.get(
                        member.getDocumentId()
                );

        return getDinnerStatus(
                record
        );
    }

    // =========================================================
    // SAVE RECORD
    // =========================================================

    private void saveRecord(
            Member member,
            String lunchStatus,
            String dinnerStatus) {

        if (member.getDocumentId() == null ||
                member.getDocumentId()
                        .trim()
                        .isEmpty()) {

            showToast(
                    "Member ID not found"
            );

            return;
        }

        // =====================================================
        // ONE RECORD PER MEMBER PER DAY
        // =====================================================

        String documentId =
                member.getDocumentId()
                        + "_"
                        + todayDate;

        // =====================================================
        // FIRESTORE DATA
        // =====================================================

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "ownerId",
                ownerId
        );

        data.put(
                "memberUid",
                member.getMemberUid()
        );

        data.put(
                "memberDocumentId",
                member.getDocumentId()
        );

        data.put(
                "memberName",
                member.getName()
        );

        data.put(
                "phone",
                member.getPhone()
        );

        data.put(
                "date",
                todayDate
        );

        data.put(
                "lunchStatus",
                lunchStatus
        );

        data.put(
                "dinnerStatus",
                dinnerStatus
        );

        // =====================================================
        // SAVE TO FIRESTORE
        // =====================================================

        firestore
                .collection("tiffin_records")
                .document(documentId)
                .set(
                        data,
                        SetOptions.merge()
                )

                .addOnSuccessListener(
                        unused -> {

                            loadTodayRecords();

                            showToast(
                                    "Collection updated"
                            );
                        }
                )

                .addOnFailureListener(
                        e -> showToast(
                                "Failed to save collection"
                        )
                );
    }

    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (ownerId != null &&
                !ownerId.isEmpty()) {

            loadMembers();
        }
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void showToast(
            String message) {

        if (!isAdded()) {
            return;
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}