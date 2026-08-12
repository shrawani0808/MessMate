package com.example.messmate.presentation.owner.members;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.auth.SessionManager;
import com.example.messmate.presentation.owner.members.adapters.MemberAdapter;
import com.example.messmate.presentation.owner.members.model.Member;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MemberListFragment extends Fragment {

    private RecyclerView recyclerMembers;

    private TextView txtMemberCount;

    private LinearLayout emptyLayout;

    private FloatingActionButton fabAddMember;

    // =====================================================
    // SEARCH
    // =====================================================

    private SearchView searchViewMembers;

    private FirebaseFirestore firestore;

    private SessionManager sessionManager;

    private String ownerId;

    // Original complete list loaded from Firestore
    private final List<Member> memberList =
            new ArrayList<>();

    // List currently displayed by RecyclerView
    private final List<Member> filteredMemberList =
            new ArrayList<>();

    private MemberAdapter adapter;


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


    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);

        firestore =
                FirebaseFirestore.getInstance();

        sessionManager =
                new SessionManager(requireContext());

        ownerId =
                sessionManager.getUid();


        setupRecyclerView();

        setupSearch();


        fabAddMember.setOnClickListener(
                v -> showAddMemberDialog()
        );


        if (ownerId != null &&
                !ownerId.isEmpty()) {

            loadMembers();

        } else {

            showToast(
                    "Owner session not found"
            );
        }
    }


    // =====================================================
    // INITIALIZE
    // =====================================================

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

        // Search bar
        searchViewMembers =
                view.findViewById(
                        R.id.searchViewMembers
                );
    }


    // =====================================================
    // RECYCLER VIEW
    // =====================================================

    private void setupRecyclerView() {

        recyclerMembers.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );


        adapter =
                new MemberAdapter(
                        filteredMemberList,
                        this::confirmDeleteMember
                );


        recyclerMembers.setAdapter(adapter);
    }


    // =====================================================
    // SEARCH
    // =====================================================

    private void setupSearch() {

        searchViewMembers.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(
                            String query) {

                        filterMembers(query);

                        return true;
                    }


                    @Override
                    public boolean onQueryTextChange(
                            String newText) {

                        filterMembers(newText);

                        return true;
                    }
                }
        );
    }


    // =====================================================
    // FILTER MEMBERS
    // =====================================================

    private void filterMembers(String query) {

        String searchText =
                query == null
                        ? ""
                        : query.trim().toLowerCase();


        filteredMemberList.clear();


        // If search is empty, show everyone
        if (searchText.isEmpty()) {

            filteredMemberList.addAll(
                    memberList
            );

        } else {

            for (Member member : memberList) {

                String name =
                        member.getName() == null
                                ? ""
                                : member.getName().toLowerCase();

                String email =
                        member.getEmail() == null
                                ? ""
                                : member.getEmail().toLowerCase();

                String phone =
                        member.getPhone() == null
                                ? ""
                                : member.getPhone().toLowerCase();


                if (name.contains(searchText)
                        || email.contains(searchText)
                        || phone.contains(searchText)) {

                    filteredMemberList.add(member);
                }
            }
        }


        adapter.notifyDataSetChanged();

        updateMemberCount();

        updateEmptyState();
    }


    // =====================================================
    // LOAD MEMBERS
    // =====================================================

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

                            memberList.clear();

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

                                memberList.add(member);
                            }


                            // Re-apply current search
                            String currentSearch =
                                    searchViewMembers
                                            .getQuery()
                                            .toString();

                            filterMembers(
                                    currentSearch
                            );
                        }
                )

                .addOnFailureListener(
                        e -> showToast(
                                "Failed to load members"
                        )
                );
    }


    // =====================================================
    // ADD MEMBER DIALOG
    // =====================================================

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


        com.google.android.material.button.MaterialButton
                btnCancel =
                dialogView.findViewById(
                        R.id.btnCancelMember
                );


        com.google.android.material.button.MaterialButton
                btnAdd =
                dialogView.findViewById(
                        R.id.btnAddMember
                );


        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create();


        dialog.setOnShowListener(dialogInterface -> {

            if (dialog.getWindow() != null) {

                dialog.getWindow()
                        .setBackgroundDrawableResource(
                                android.R.color.transparent
                        );
            }


            btnCancel.setOnClickListener(
                    v -> dialog.dismiss()
            );


            btnAdd.setOnClickListener(v -> {

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


                addMember(
                        name,
                        email,
                        phone
                );

                dialog.dismiss();
            });

        });


        dialog.show();


        if (dialog.getWindow() != null) {

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


    // =====================================================
    // ADD MEMBER TO FIRESTORE
    // =====================================================

    private void addMember(
            String name,
            String email,
            String phone) {

        Member member =
                new Member(
                        ownerId,
                        null,
                        name,
                        email,
                        phone
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

                .addOnFailureListener(e -> {

                    showToast(
                            "Failed to add member: "
                                    + e.getMessage()
                    );
                });
    }


    // =====================================================
    // DELETE CONFIRMATION
    // =====================================================

    private void confirmDeleteMember(
            Member member) {

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle("Remove Member")

                .setMessage(
                        "Are you sure you want to remove "
                                + member.getName()
                                + "?"
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Remove",
                        (dialog, which) ->
                                deleteMember(member)
                )

                .show();
    }


    // =====================================================
    // DELETE MEMBER
    // =====================================================

    private void deleteMember(
            Member member) {

        if (member.getDocumentId() == null) {

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
                                "Failed to remove member"
                        )
                );
    }


    // =====================================================
    // COUNT
    // =====================================================

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


    // =====================================================
    // EMPTY STATE
    // =====================================================

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


    // =====================================================
    // REFRESH
    // =====================================================

    @Override
    public void onResume() {

        super.onResume();

        if (ownerId != null &&
                !ownerId.isEmpty()) {

            loadMembers();
        }
    }


    private void showToast(String message) {

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}