package com.example.messmate.presentation.owner.members;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.auth.SessionManager;
import com.example.messmate.presentation.owner.members.adapters.MemberAdapter;
import com.example.messmate.presentation.owner.members.model.Member;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MemberListFragment extends Fragment {

    // =========================================================
    // VIEWS
    // =========================================================

    private RecyclerView recyclerMembers;

    private TextView txtMemberCount;

    private LinearLayout emptyLayout;

    private FloatingActionButton fabAddMember;

    private SearchView searchViewMembers;

    // =========================================================
    // CATEGORY TABS
    // =========================================================

    private LinearLayout tabAllMembers;

    private LinearLayout tabMonthlyMembers;

    private LinearLayout tabDailyMembers;

    private TextView txtAllMembers;

    private TextView txtMonthlyMembers;

    private TextView txtDailyMembers;

    private View allIndicator;

    private View monthlyIndicator;

    private View dailyIndicator;

    // =========================================================
    // FILTER BUTTON
    // =========================================================

    private ImageButton btnFilterMembers;

    // =========================================================
    // FIREBASE
    // =========================================================

    private FirebaseFirestore firestore;

    private SessionManager sessionManager;

    private String ownerId;

    // =========================================================
    // MEMBER LISTS
    // =========================================================

    private final List<Member> allMembers =
            new ArrayList<>();

    private final List<Member> filteredMemberList =
            new ArrayList<>();

    private MemberAdapter adapter;

    // =========================================================
    // FILTER
    // =========================================================

    /*
     * 0 = All
     * 1 = Monthly
     * 2 = Daily
     */

    private int memberTypeFilter = 0;

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
                R.layout.fragment_member_list,
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

        // =====================================================
        // INITIALIZE
        // =====================================================

        initializeViews(view);

        firestore =
                FirebaseFirestore.getInstance();

        sessionManager =
                new SessionManager(
                        requireContext()
                );

        ownerId =
                sessionManager.getUid();

        // =====================================================
        // CATEGORY TABS
        // =====================================================

        setupCategoryTabs();

        // =====================================================
        // RECYCLER VIEW
        // =====================================================

        setupRecyclerView();

        // =====================================================
        // SEARCH
        // =====================================================

        setupSearch();

        // =====================================================
        // FILTER
        // =====================================================

        setupFilterButton();

        // =====================================================
        // ADD MEMBER
        // =====================================================

        fabAddMember.setOnClickListener(
                v -> showAddMemberDialog()
        );

        // =====================================================
        // LOAD MEMBERS
        // =====================================================

        if (ownerId != null &&
                !ownerId.trim().isEmpty()) {

            loadMembers();

        } else {

            showToast(
                    "Owner session not found"
            );
        }
    }

    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews(View view) {

        recyclerMembers =
                view.findViewById(
                        R.id.recyclerMembers
                );

        txtMemberCount =
                view.findViewById(
                        R.id.txtMemberCount
                );

        emptyLayout =
                view.findViewById(
                        R.id.emptyLayout
                );

        fabAddMember =
                view.findViewById(
                        R.id.fabAddMember
                );

        searchViewMembers =
                view.findViewById(
                        R.id.searchViewMembers
                );

        // =====================================================
        // TABS
        // =====================================================

        tabAllMembers =
                view.findViewById(
                        R.id.tabAllMembers
                );

        tabMonthlyMembers =
                view.findViewById(
                        R.id.tabMonthlyMembers
                );

        tabDailyMembers =
                view.findViewById(
                        R.id.tabDailyMembers
                );

        txtAllMembers =
                view.findViewById(
                        R.id.txtAllMembers
                );

        txtMonthlyMembers =
                view.findViewById(
                        R.id.txtMonthlyMembers
                );

        txtDailyMembers =
                view.findViewById(
                        R.id.txtDailyMembers
                );

        allIndicator =
                view.findViewById(
                        R.id.allIndicator
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
        // FILTER
        // =====================================================

        btnFilterMembers =
                view.findViewById(
                        R.id.btnFilterMembers
                );
    }

    // =========================================================
    // CATEGORY TABS
    // =========================================================

    private void setupCategoryTabs() {

        // =====================================================
        // ALL
        // =====================================================

        tabAllMembers.setOnClickListener(
                v -> {

                    memberTypeFilter = 0;

                    updateTabUI();

                    filterMembers();
                }
        );

        // =====================================================
        // MONTHLY
        // =====================================================

        tabMonthlyMembers.setOnClickListener(
                v -> {

                    memberTypeFilter = 1;

                    updateTabUI();

                    filterMembers();
                }
        );

        // =====================================================
        // DAILY
        // =====================================================

        tabDailyMembers.setOnClickListener(
                v -> {

                    memberTypeFilter = 2;

                    updateTabUI();

                    filterMembers();
                }
        );

        updateTabUI();
    }

    // =========================================================
    // UPDATE TAB UI
    // =========================================================

    private void updateTabUI() {

        if (txtAllMembers == null ||
                txtMonthlyMembers == null ||
                txtDailyMembers == null) {

            return;
        }

        int activeColor =
                getColor(R.color.primary);

        int inactiveColor =
                getColor(R.color.text_primary);

        txtAllMembers.setTextColor(
                inactiveColor
        );

        txtMonthlyMembers.setTextColor(
                inactiveColor
        );

        txtDailyMembers.setTextColor(
                inactiveColor
        );

        allIndicator.setVisibility(
                View.INVISIBLE
        );

        monthlyIndicator.setVisibility(
                View.INVISIBLE
        );

        dailyIndicator.setVisibility(
                View.INVISIBLE
        );

        if (memberTypeFilter == 0) {

            txtAllMembers.setTextColor(
                    activeColor
            );

            allIndicator.setVisibility(
                    View.VISIBLE
            );

        } else if (memberTypeFilter == 1) {

            txtMonthlyMembers.setTextColor(
                    activeColor
            );

            monthlyIndicator.setVisibility(
                    View.VISIBLE
            );

        } else {

            txtDailyMembers.setTextColor(
                    activeColor
            );

            dailyIndicator.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =========================================================
    // FILTER BUTTON
    // =========================================================

    private void setupFilterButton() {

        if (btnFilterMembers == null) {
            return;
        }

        btnFilterMembers.setOnClickListener(
                v -> showFilterDialog()
        );
    }

    // =========================================================
    // FILTER DIALOG
    // =========================================================

    private void showFilterDialog() {

        View dialogView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.dialog_filter_members,
                                null
                        );

        RadioButton rbAllMembers =
                dialogView.findViewById(
                        R.id.rbAllMembers
                );

        RadioButton rbMonthlyMembers =
                dialogView.findViewById(
                        R.id.rbMonthlyMembers
                );

        RadioButton rbDailyMembers =
                dialogView.findViewById(
                        R.id.rbDailyMembers
                );

        MaterialButton btnCancelFilter =
                dialogView.findViewById(
                        R.id.btnCancelFilter
                );

        // =====================================================
        // CURRENT FILTER
        // =====================================================

        if (memberTypeFilter == 0) {

            rbAllMembers.setChecked(true);

        } else if (memberTypeFilter == 1) {

            rbMonthlyMembers.setChecked(true);

        } else {

            rbDailyMembers.setChecked(true);
        }

        // =====================================================
        // DIALOG
        // =====================================================

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setView(dialogView)
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    if (dialog.getWindow() != null) {

                        dialog.getWindow()
                                .setBackgroundDrawableResource(
                                        android.R.color.transparent
                                );
                    }

                    rbAllMembers.setOnClickListener(
                            v -> {

                                memberTypeFilter = 0;

                                updateTabUI();

                                filterMembers();

                                dialog.dismiss();
                            }
                    );

                    rbMonthlyMembers.setOnClickListener(
                            v -> {

                                memberTypeFilter = 1;

                                updateTabUI();

                                filterMembers();

                                dialog.dismiss();
                            }
                    );

                    rbDailyMembers.setOnClickListener(
                            v -> {

                                memberTypeFilter = 2;

                                updateTabUI();

                                filterMembers();

                                dialog.dismiss();
                            }
                    );

                    btnCancelFilter.setOnClickListener(
                            v -> dialog.dismiss()
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {

        recyclerMembers.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        adapter =
                new MemberAdapter(
                        filteredMemberList,
                        this::confirmDeleteMember,
                        this::showUpdateMemberDialog
                );

        recyclerMembers.setAdapter(
                adapter
        );
    }

    // =========================================================
    // SEARCH
    // =========================================================

    private void setupSearch() {

        searchViewMembers.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(
                            String query) {

                        filterMembers();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(
                            String newText) {

                        filterMembers();

                        return true;
                    }
                }
        );
    }

    // =========================================================
    // FILTER MEMBERS
    // =========================================================

    private void filterMembers() {

        if (searchViewMembers == null ||
                adapter == null) {

            return;
        }

        String searchText =
                searchViewMembers
                        .getQuery()
                        .toString()
                        .trim()
                        .toLowerCase(
                                Locale.getDefault()
                        );

        filteredMemberList.clear();

        for (Member member : allMembers) {

            // =================================================
            // SEARCH
            // =================================================

            if (!matchesSearch(
                    member,
                    searchText
            )) {

                continue;
            }

            // =================================================
            // PAYMENT TYPE
            // =================================================

            boolean monthly =
                    "monthly".equalsIgnoreCase(
                            member.getPaymentType()
                    );

            // =================================================
            // MONTHLY FILTER
            // =================================================

            if (memberTypeFilter == 1 &&
                    !monthly) {

                continue;
            }

            // =================================================
            // DAILY FILTER
            // =================================================

            if (memberTypeFilter == 2 &&
                    monthly) {

                continue;
            }

            filteredMemberList.add(
                    member
            );
        }

        adapter.notifyDataSetChanged();

        updateMemberCount();

        updateEmptyState();

        updateTabLabels();
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

        String email =
                member.getEmail() == null
                        ? ""
                        : member.getEmail()
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

        return name.contains(searchText)
                || email.contains(searchText)
                || phone.contains(searchText);
    }

    // =========================================================
    // UPDATE TAB COUNTS
    // =========================================================

    private void updateTabLabels() {

        if (txtAllMembers == null ||
                txtMonthlyMembers == null ||
                txtDailyMembers == null) {

            return;
        }

        int allCount =
                allMembers.size();

        int monthlyCount = 0;

        int dailyCount = 0;

        for (Member member : allMembers) {

            if ("monthly".equalsIgnoreCase(
                    member.getPaymentType()
            )) {

                monthlyCount++;

            } else {

                dailyCount++;
            }
        }

        txtAllMembers.setText(
                "All (" + allCount + ")"
        );

        txtMonthlyMembers.setText(
                "Monthly Members (" +
                        monthlyCount +
                        ")"
        );

        txtDailyMembers.setText(
                "Daily Members (" +
                        dailyCount +
                        ")"
        );
    }

    // =========================================================
    // LOAD MEMBERS
    // =========================================================

    private void loadMembers() {

        if (ownerId == null ||
                ownerId.trim().isEmpty()) {

            return;
        }

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

                            for (
                                    QueryDocumentSnapshot document :
                                    querySnapshot
                            ) {

                                Member member =
                                        document.toObject(
                                                Member.class
                                        );

                                if (member == null) {
                                    continue;
                                }

                                member.setDocumentId(
                                        document.getId()
                                );

                                normalizeOldMember(
                                        member
                                );

                                allMembers.add(
                                        member
                                );
                            }

                            updateTabLabels();

                            filterMembers();
                        }
                )
                .addOnFailureListener(
                        e -> showToast(
                                "Failed to load members: " +
                                        e.getMessage()
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
    // ADD MEMBER DIALOG
    // =========================================================

    private void showAddMemberDialog() {

        View dialogView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.dialog_add_member,
                                null
                        );

        EditText nameInput =
                dialogView.findViewById(
                        R.id.etMemberName
                );

        EditText emailInput =
                dialogView.findViewById(
                        R.id.etMemberEmail
                );

        EditText phoneInput =
                dialogView.findViewById(
                        R.id.etMemberPhone
                );

        RadioButton radioDaily =
                dialogView.findViewById(
                        R.id.radioDaily
                );

        RadioButton radioMonthly =
                dialogView.findViewById(
                        R.id.radioMonthly
                );

        MaterialCheckBox checkLunch =
                dialogView.findViewById(
                        R.id.checkLunch
                );

        MaterialCheckBox checkDinner =
                dialogView.findViewById(
                        R.id.checkDinner
                );

        EditText fullRateInput =
                dialogView.findViewById(
                        R.id.etFullRate
                );

        EditText halfRateInput =
                dialogView.findViewById(
                        R.id.etHalfRate
                );

        EditText monthlyLunchInput =
                dialogView.findViewById(
                        R.id.etMonthlyLunchAmount
                );

        EditText monthlyDinnerInput =
                dialogView.findViewById(
                        R.id.etMonthlyDinnerAmount
                );

        LinearLayout dailyRateSection =
                dialogView.findViewById(
                        R.id.dailyRateSection
                );

        LinearLayout monthlySection =
                dialogView.findViewById(
                        R.id.monthlySection
                );

        MaterialButton btnCancel =
                dialogView.findViewById(
                        R.id.btnCancelMember
                );

        MaterialButton btnAdd =
                dialogView.findViewById(
                        R.id.btnAddMember
                );

        // =====================================================
        // DEFAULT PAYMENT TYPE
        // =====================================================

        radioDaily.setChecked(true);

        dailyRateSection.setVisibility(
                View.VISIBLE
        );

        monthlySection.setVisibility(
                View.GONE
        );

        // =====================================================
        // PAYMENT TYPE LISTENERS
        // =====================================================

        radioDaily.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        dailyRateSection.setVisibility(
                                View.VISIBLE
                        );

                        monthlySection.setVisibility(
                                View.GONE
                        );
                    }
                }
        );

        radioMonthly.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        dailyRateSection.setVisibility(
                                View.GONE
                        );

                        monthlySection.setVisibility(
                                View.VISIBLE
                        );
                    }
                }
        );

        // =====================================================
        // DIALOG
        // =====================================================

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setView(dialogView)
                        .create();

        dialog.setOnShowListener(
                dialogInterface -> {

                    if (dialog.getWindow() != null) {

                        dialog.getWindow()
                                .setBackgroundDrawableResource(
                                        android.R.color.transparent
                                );
                    }

                    btnCancel.setOnClickListener(
                            v -> dialog.dismiss()
                    );

                    btnAdd.setOnClickListener(
                            v -> {

                                addMemberFromDialog(
                                        nameInput,
                                        emailInput,
                                        phoneInput,
                                        radioDaily,
                                        radioMonthly,
                                        checkLunch,
                                        checkDinner,
                                        fullRateInput,
                                        halfRateInput,
                                        monthlyLunchInput,
                                        monthlyDinnerInput,
                                        dialog
                                );
                            }
                    );
                }
        );

        dialog.show();

        setDialogWidth(dialog);
    }

    // =========================================================
    // ADD MEMBER FROM DIALOG
    // =========================================================

    private void addMemberFromDialog(
            EditText nameInput,
            EditText emailInput,
            EditText phoneInput,
            RadioButton radioDaily,
            RadioButton radioMonthly,
            MaterialCheckBox checkLunch,
            MaterialCheckBox checkDinner,
            EditText fullRateInput,
            EditText halfRateInput,
            EditText monthlyLunchInput,
            EditText monthlyDinnerInput,
            AlertDialog dialog) {

        String name =
                nameInput.getText()
                        .toString()
                        .trim();

        String email =
                emailInput.getText()
                        .toString()
                        .trim();

        String phone =
                phoneInput.getText()
                        .toString()
                        .trim();

        // =====================================================
        // BASIC VALIDATION
        // =====================================================

        if (name.isEmpty()) {

            nameInput.setError(
                    "Enter member name"
            );

            nameInput.requestFocus();

            return;
        }

        if (email.isEmpty()) {

            emailInput.setError(
                    "Enter email"
            );

            emailInput.requestFocus();

            return;
        }

        if (phone.isEmpty()) {

            phoneInput.setError(
                    "Enter phone number"
            );

            phoneInput.requestFocus();

            return;
        }

        // =====================================================
        // MEAL
        // =====================================================

        boolean lunchEnabled =
                checkLunch.isChecked();

        boolean dinnerEnabled =
                checkDinner.isChecked();

        if (!lunchEnabled &&
                !dinnerEnabled) {

            showToast(
                    "Select Lunch or Dinner"
            );

            return;
        }

        // =====================================================
        // PAYMENT TYPE
        // =====================================================

        String paymentType =
                radioMonthly.isChecked()
                        ? "monthly"
                        : "daily";

        double fullRate = 0;

        double halfRate = 0;

        double monthlyLunchAmount = 0;

        double monthlyDinnerAmount = 0;

        // =====================================================
        // DAILY
        // =====================================================

        if ("daily".equals(paymentType)) {

            String fullRateText =
                    fullRateInput
                            .getText()
                            .toString()
                            .trim();

            String halfRateText =
                    halfRateInput
                            .getText()
                            .toString()
                            .trim();

            if (fullRateText.isEmpty()) {

                fullRateInput.setError(
                        "Enter full rate"
                );

                fullRateInput.requestFocus();

                return;
            }

            if (halfRateText.isEmpty()) {

                halfRateInput.setError(
                        "Enter half rate"
                );

                halfRateInput.requestFocus();

                return;
            }

            try {

                fullRate =
                        Double.parseDouble(
                                fullRateText
                        );

                halfRate =
                        Double.parseDouble(
                                halfRateText
                        );

            } catch (NumberFormatException e) {

                showToast(
                        "Enter valid rates"
                );

                return;
            }

            if (fullRate <= 0) {

                fullRateInput.setError(
                        "Rate must be greater than 0"
                );

                return;
            }

            if (halfRate <= 0) {

                halfRateInput.setError(
                        "Rate must be greater than 0"
                );

                return;
            }
        }

        // =====================================================
        // MONTHLY
        // =====================================================

        if ("monthly".equals(paymentType)) {

            if (lunchEnabled) {

                String text =
                        monthlyLunchInput
                                .getText()
                                .toString()
                                .trim();

                if (text.isEmpty()) {

                    monthlyLunchInput.setError(
                            "Enter monthly lunch rate"
                    );

                    monthlyLunchInput.requestFocus();

                    return;
                }

                try {

                    monthlyLunchAmount =
                            Double.parseDouble(text);

                } catch (NumberFormatException e) {

                    monthlyLunchInput.setError(
                            "Enter valid amount"
                    );

                    monthlyLunchInput.requestFocus();

                    return;
                }

                if (monthlyLunchAmount <= 0) {

                    monthlyLunchInput.setError(
                            "Amount must be greater than 0"
                    );

                    return;
                }
            }

            if (dinnerEnabled) {

                String text =
                        monthlyDinnerInput
                                .getText()
                                .toString()
                                .trim();

                if (text.isEmpty()) {

                    monthlyDinnerInput.setError(
                            "Enter monthly dinner rate"
                    );

                    monthlyDinnerInput.requestFocus();

                    return;
                }

                try {

                    monthlyDinnerAmount =
                            Double.parseDouble(text);

                } catch (NumberFormatException e) {

                    monthlyDinnerInput.setError(
                            "Enter valid amount"
                    );

                    monthlyDinnerInput.requestFocus();

                    return;
                }

                if (monthlyDinnerAmount <= 0) {

                    monthlyDinnerInput.setError(
                            "Amount must be greater than 0"
                    );

                    return;
                }
            }
        }

        // =====================================================
        // SAVE
        // =====================================================

        addMember(
                name,
                email,
                phone,
                paymentType,
                lunchEnabled,
                dinnerEnabled,
                fullRate,
                halfRate,
                monthlyLunchAmount,
                monthlyDinnerAmount
        );

        dialog.dismiss();
    }

    // =========================================================
    // ADD MEMBER TO FIRESTORE
    // =========================================================

    private void addMember(
            String name,
            String email,
            String phone,
            String paymentType,
            boolean lunchEnabled,
            boolean dinnerEnabled,
            double fullRate,
            double halfRate,
            double monthlyLunchAmount,
            double monthlyDinnerAmount) {

        Member member =
                new Member(
                        ownerId,
                        null,
                        name,
                        email,
                        phone
                );

        member.setPaymentType(
                paymentType
        );

        member.setLunchEnabled(
                lunchEnabled
        );

        member.setDinnerEnabled(
                dinnerEnabled
        );

        member.setFullRate(
                fullRate
        );

        member.setHalfRate(
                halfRate
        );

        member.setMonthlyLunchAmount(
                monthlyLunchAmount
        );

        member.setMonthlyDinnerAmount(
                monthlyDinnerAmount
        );

        firestore
                .collection("members")
                .add(member)
                .addOnSuccessListener(
                        documentReference -> {

                            showToast(
                                    "Member added successfully"
                            );

                            loadMembers();
                        }
                )
                .addOnFailureListener(
                        e -> showToast(
                                "Failed to add member: " +
                                        e.getMessage()
                        )
                );
    }

    // =========================================================
    // UPDATE MEMBER DIALOG
    // =========================================================

    private void showUpdateMemberDialog(
            Member member) {

        if (member == null) {

            showToast(
                    "Member data not found"
            );

            return;
        }

        String documentId =
                member.getDocumentId();

        if (documentId == null ||
                documentId.trim().isEmpty()) {

            showToast(
                    "Member ID not found"
            );

            return;
        }

        // =====================================================
        // INFLATE DIALOG
        // =====================================================

        View dialogView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.dialog_add_member,
                                null
                        );

        // =====================================================
        // INPUTS
        // =====================================================

        EditText nameInput =
                dialogView.findViewById(
                        R.id.etMemberName
                );

        EditText emailInput =
                dialogView.findViewById(
                        R.id.etMemberEmail
                );

        EditText phoneInput =
                dialogView.findViewById(
                        R.id.etMemberPhone
                );

        RadioButton radioDaily =
                dialogView.findViewById(
                        R.id.radioDaily
                );

        RadioButton radioMonthly =
                dialogView.findViewById(
                        R.id.radioMonthly
                );

        MaterialCheckBox checkLunch =
                dialogView.findViewById(
                        R.id.checkLunch
                );

        MaterialCheckBox checkDinner =
                dialogView.findViewById(
                        R.id.checkDinner
                );

        EditText fullRateInput =
                dialogView.findViewById(
                        R.id.etFullRate
                );

        EditText halfRateInput =
                dialogView.findViewById(
                        R.id.etHalfRate
                );

        EditText monthlyLunchInput =
                dialogView.findViewById(
                        R.id.etMonthlyLunchAmount
                );

        EditText monthlyDinnerInput =
                dialogView.findViewById(
                        R.id.etMonthlyDinnerAmount
                );

        LinearLayout dailyRateSection =
                dialogView.findViewById(
                        R.id.dailyRateSection
                );

        LinearLayout monthlySection =
                dialogView.findViewById(
                        R.id.monthlySection
                );

        MaterialButton btnCancel =
                dialogView.findViewById(
                        R.id.btnCancelMember
                );

        MaterialButton btnUpdate =
                dialogView.findViewById(
                        R.id.btnAddMember
                );

        // =====================================================
        // LOAD EXISTING DATA
        // =====================================================

        nameInput.setText(
                safeString(member.getName())
        );

        emailInput.setText(
                safeString(member.getEmail())
        );

        phoneInput.setText(
                safeString(member.getPhone())
        );

        // =====================================================
        // MEALS
        // =====================================================

        checkLunch.setChecked(
                member.isLunchEnabled()
        );

        checkDinner.setChecked(
                member.isDinnerEnabled()
        );

        // =====================================================
        // PAYMENT TYPE
        // =====================================================

        String paymentType =
                member.getPaymentType();

        if (paymentType == null ||
                paymentType.trim().isEmpty()) {

            paymentType = "daily";
        }

        if ("monthly".equalsIgnoreCase(
                paymentType
        )) {

            radioMonthly.setChecked(true);

            radioDaily.setChecked(false);

            dailyRateSection.setVisibility(
                    View.GONE
            );

            monthlySection.setVisibility(
                    View.VISIBLE
            );

        } else {

            radioDaily.setChecked(true);

            radioMonthly.setChecked(false);

            dailyRateSection.setVisibility(
                    View.VISIBLE
            );

            monthlySection.setVisibility(
                    View.GONE
            );
        }

        // =====================================================
        // DAILY RATES
        // =====================================================

        if (member.getFullRate() > 0) {

            fullRateInput.setText(
                    formatAmount(
                            member.getFullRate()
                    )
            );
        }

        if (member.getHalfRate() > 0) {

            halfRateInput.setText(
                    formatAmount(
                            member.getHalfRate()
                    )
            );
        }

        // =====================================================
        // MONTHLY AMOUNTS
        // =====================================================

        if (member.getMonthlyLunchAmount() > 0) {

            monthlyLunchInput.setText(
                    formatAmount(
                            member.getMonthlyLunchAmount()
                    )
            );
        }

        if (member.getMonthlyDinnerAmount() > 0) {

            monthlyDinnerInput.setText(
                    formatAmount(
                            member.getMonthlyDinnerAmount()
                    )
            );
        }

        // =====================================================
        // PAYMENT TYPE LISTENERS
        // =====================================================

        radioDaily.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        dailyRateSection.setVisibility(
                                View.VISIBLE
                        );

                        monthlySection.setVisibility(
                                View.GONE
                        );
                    }
                }
        );

        radioMonthly.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    if (isChecked) {

                        dailyRateSection.setVisibility(
                                View.GONE
                        );

                        monthlySection.setVisibility(
                                View.VISIBLE
                        );
                    }
                }
        );

        // =====================================================
        // CREATE DIALOG
        // =====================================================

        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setView(dialogView)
                        .create();

        // =====================================================
        // SHOW DIALOG
        // =====================================================

        dialog.setOnShowListener(
                dialogInterface -> {

                    if (dialog.getWindow() != null) {

                        dialog.getWindow()
                                .setBackgroundDrawableResource(
                                        android.R.color.transparent
                                );
                    }

                    // =================================================
                    // UPDATE BUTTON
                    // =================================================

                    btnUpdate.setText(
                            "Update Member"
                    );

                    // =================================================
                    // CANCEL
                    // =================================================

                    btnCancel.setOnClickListener(
                            v -> dialog.dismiss()
                    );

                    // =================================================
                    // UPDATE
                    // =================================================

                    btnUpdate.setOnClickListener(
                            v -> updateMemberFromDialog(
                                    member,
                                    nameInput,
                                    emailInput,
                                    phoneInput,
                                    radioDaily,
                                    radioMonthly,
                                    checkLunch,
                                    checkDinner,
                                    fullRateInput,
                                    halfRateInput,
                                    monthlyLunchInput,
                                    monthlyDinnerInput,
                                    dialog
                            )
                    );
                }
        );

        dialog.show();

        setDialogWidth(dialog);
    }

    // =========================================================
    // UPDATE MEMBER FROM DIALOG
    // =========================================================

    private void updateMemberFromDialog(
            Member member,
            EditText nameInput,
            EditText emailInput,
            EditText phoneInput,
            RadioButton radioDaily,
            RadioButton radioMonthly,
            MaterialCheckBox checkLunch,
            MaterialCheckBox checkDinner,
            EditText fullRateInput,
            EditText halfRateInput,
            EditText monthlyLunchInput,
            EditText monthlyDinnerInput,
            AlertDialog dialog) {

        // =====================================================
        // BASIC DETAILS
        // =====================================================

        String name =
                nameInput.getText()
                        .toString()
                        .trim();

        String email =
                emailInput.getText()
                        .toString()
                        .trim();

        String phone =
                phoneInput.getText()
                        .toString()
                        .trim();

        // =====================================================
        // VALIDATION
        // =====================================================

        if (name.isEmpty()) {

            nameInput.setError(
                    "Enter member name"
            );

            nameInput.requestFocus();

            return;
        }

        if (email.isEmpty()) {

            emailInput.setError(
                    "Enter email"
            );

            emailInput.requestFocus();

            return;
        }

        if (phone.isEmpty()) {

            phoneInput.setError(
                    "Enter phone number"
            );

            phoneInput.requestFocus();

            return;
        }

        // =====================================================
        // MEALS
        // =====================================================

        boolean lunchEnabled =
                checkLunch.isChecked();

        boolean dinnerEnabled =
                checkDinner.isChecked();

        if (!lunchEnabled &&
                !dinnerEnabled) {

            showToast(
                    "Select Lunch or Dinner"
            );

            return;
        }

        // =====================================================
        // PAYMENT TYPE
        // =====================================================

        String paymentType =
                radioMonthly.isChecked()
                        ? "monthly"
                        : "daily";

        double fullRate = 0;

        double halfRate = 0;

        double monthlyLunchAmount = 0;

        double monthlyDinnerAmount = 0;

        // =====================================================
        // DAILY RATES
        // =====================================================

        if ("daily".equals(paymentType)) {

            String fullRateText =
                    fullRateInput
                            .getText()
                            .toString()
                            .trim();

            String halfRateText =
                    halfRateInput
                            .getText()
                            .toString()
                            .trim();

            if (fullRateText.isEmpty()) {

                fullRateInput.setError(
                        "Enter full rate"
                );

                fullRateInput.requestFocus();

                return;
            }

            if (halfRateText.isEmpty()) {

                halfRateInput.setError(
                        "Enter half rate"
                );

                halfRateInput.requestFocus();

                return;
            }

            try {

                fullRate =
                        Double.parseDouble(
                                fullRateText
                        );

                halfRate =
                        Double.parseDouble(
                                halfRateText
                        );

            } catch (NumberFormatException e) {

                showToast(
                        "Enter valid rates"
                );

                return;
            }

            if (fullRate <= 0) {

                fullRateInput.setError(
                        "Rate must be greater than 0"
                );

                fullRateInput.requestFocus();

                return;
            }

            if (halfRate <= 0) {

                halfRateInput.setError(
                        "Rate must be greater than 0"
                );

                halfRateInput.requestFocus();

                return;
            }
        }

        // =====================================================
        // MONTHLY PACKAGE
        // =====================================================

        if ("monthly".equals(paymentType)) {

            // =================================================
            // LUNCH
            // =================================================

            if (lunchEnabled) {

                String lunchText =
                        monthlyLunchInput
                                .getText()
                                .toString()
                                .trim();

                if (lunchText.isEmpty()) {

                    monthlyLunchInput.setError(
                            "Enter monthly lunch rate"
                    );

                    monthlyLunchInput.requestFocus();

                    return;
                }

                try {

                    monthlyLunchAmount =
                            Double.parseDouble(
                                    lunchText
                            );

                } catch (NumberFormatException e) {

                    monthlyLunchInput.setError(
                            "Enter valid amount"
                    );

                    monthlyLunchInput.requestFocus();

                    return;
                }

                if (monthlyLunchAmount <= 0) {

                    monthlyLunchInput.setError(
                            "Amount must be greater than 0"
                    );

                    monthlyLunchInput.requestFocus();

                    return;
                }
            }

            // =================================================
            // DINNER
            // =================================================

            if (dinnerEnabled) {

                String dinnerText =
                        monthlyDinnerInput
                                .getText()
                                .toString()
                                .trim();

                if (dinnerText.isEmpty()) {

                    monthlyDinnerInput.setError(
                            "Enter monthly dinner rate"
                    );

                    monthlyDinnerInput.requestFocus();

                    return;
                }

                try {

                    monthlyDinnerAmount =
                            Double.parseDouble(
                                    dinnerText
                            );

                } catch (NumberFormatException e) {

                    monthlyDinnerInput.setError(
                            "Enter valid amount"
                    );

                    monthlyDinnerInput.requestFocus();

                    return;
                }

                if (monthlyDinnerAmount <= 0) {

                    monthlyDinnerInput.setError(
                            "Amount must be greater than 0"
                    );

                    monthlyDinnerInput.requestFocus();

                    return;
                }
            }
        }

        // =====================================================
        // DOCUMENT ID
        // =====================================================

        String documentId =
                member.getDocumentId();

        if (documentId == null ||
                documentId.trim().isEmpty()) {

            showToast(
                    "Member ID not found"
            );

            return;
        }

        // =====================================================
        // UPDATE MEMBER OBJECT
        // =====================================================

        member.setName(
                name
        );

        member.setEmail(
                email
        );

        member.setPhone(
                phone
        );

        member.setPaymentType(
                paymentType
        );

        member.setLunchEnabled(
                lunchEnabled
        );

        member.setDinnerEnabled(
                dinnerEnabled
        );

        member.setFullRate(
                fullRate
        );

        member.setHalfRate(
                halfRate
        );

        member.setMonthlyLunchAmount(
                monthlyLunchAmount
        );

        member.setMonthlyDinnerAmount(
                monthlyDinnerAmount
        );

        // =====================================================
        // UPDATE FIRESTORE
        // =====================================================

        firestore
                .collection("members")
                .document(documentId)
                .set(member)
                .addOnSuccessListener(
                        unused -> {

                            showToast(
                                    "Member updated successfully"
                            );

                            dialog.dismiss();

                            loadMembers();
                        }
                )
                .addOnFailureListener(
                        e -> showToast(
                                "Failed to update member: " +
                                        e.getMessage()
                        )
                );
    }

    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void confirmDeleteMember(Member member) {

        if (member == null) {

            showToast("Member not found");

            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Delete Member")
                .setMessage("Are you sure you want to delete this member?")
                .setPositiveButton(
                        "Remove",
                        (dialogInterface, which) -> {
                            deleteMember(member);
                        }
                )
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {

            // White dialog background
            if (dialog.getWindow() != null) {

                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.WHITE)
                );
            }

            // Black message
            TextView message = dialog.findViewById(
                    android.R.id.message
            );

            if (message != null) {

                message.setTextColor(Color.BLACK);
            }

            // Black title
            int titleId = getResources().getIdentifier(
                    "alertTitle",
                    "id",
                    requireContext().getPackageName()
            );

            TextView title = dialog.findViewById(titleId);

            if (title != null) {

                title.setTextColor(Color.BLACK);
            }

            // Green Remove button
            Button positiveButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    );

            if (positiveButton != null) {

                positiveButton.setTextColor(
                        Color.rgb(47, 132, 100)
                );
            }

            // Green Cancel button
            Button negativeButton =
                    dialog.getButton(
                            AlertDialog.BUTTON_NEGATIVE
                    );

            if (negativeButton != null) {

                negativeButton.setTextColor(
                        Color.rgb(47, 132, 100)
                );
            }
        });

        dialog.show();
    }
    // =========================================================
    // DELETE MEMBER
    // =========================================================

    private void deleteMember(
            Member member) {

        if (member == null ||
                member.getDocumentId() == null ||
                member.getDocumentId()
                        .trim()
                        .isEmpty()) {

            showToast(
                    "Member ID not found"
            );

            return;
        }

        firestore
                .collection("members")
                .document(
                        member.getDocumentId()
                )
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            showToast(
                                    "Member removed"
                            );

                            loadMembers();
                        }
                )
                .addOnFailureListener(
                        e -> showToast(
                                "Failed to remove member: " +
                                        e.getMessage()
                        )
                );
    }

    // =========================================================
    // MEMBER COUNT
    // =========================================================

    private void updateMemberCount() {

        int count =
                filteredMemberList.size();

        if (count == 1) {

            txtMemberCount.setText(
                    "1 member"
            );

        } else {

            txtMemberCount.setText(
                    count + " members"
            );
        }
    }

    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (filteredMemberList.isEmpty()) {

            emptyLayout.setVisibility(
                    View.VISIBLE
            );

            recyclerMembers.setVisibility(
                    View.GONE
            );

        } else {

            emptyLayout.setVisibility(
                    View.GONE
            );

            recyclerMembers.setVisibility(
                    View.VISIBLE
            );
        }
    }

    // =========================================================
    // RESUME
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (ownerId != null &&
                !ownerId.trim().isEmpty()) {

            loadMembers();
        }
    }

    // =========================================================
    // FORMAT AMOUNT
    // =========================================================

    private String formatAmount(
            double amount) {

        if (amount == (long) amount) {

            return String.valueOf(
                    (long) amount
            );
        }

        return String.valueOf(
                amount
        );
    }

    // =========================================================
    // SAFE STRING
    // =========================================================

    private String safeString(
            String value) {

        return value == null
                ? ""
                : value;
    }

    // =========================================================
    // DIALOG WIDTH
    // =========================================================

    private void setDialogWidth(
            AlertDialog dialog) {

        if (dialog.getWindow() != null) {

            float density =
                    getResources()
                            .getDisplayMetrics()
                            .density;

            dialog.getWindow().setLayout(
                    (int) (350 * density),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // =========================================================
    // COLOR
    // =========================================================

    private int getColor(
            int colorRes) {

        return ContextCompat.getColor(
                requireContext(),
                colorRes
        );
    }

    // =========================================================
    // TOAST
    // =========================================================

    private void showToast(
            String message) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}