package com.example.messmate.presentation.owner.members;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class MemberListFragment extends Fragment {

    private RecyclerView recyclerMembers;

    private TextView txtMemberCount;

    private LinearLayout emptyLayout;

    private FloatingActionButton fabAddMember;

    private FirebaseFirestore firestore;

    private SessionManager sessionManager;

    private String ownerId;

    private final List<Member> memberList =
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
                        memberList,
                        this::confirmDeleteMember
                );


        recyclerMembers.setAdapter(adapter);
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


                            adapter.notifyDataSetChanged();

                            updateMemberCount();

                            updateEmptyState();
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

        LinearLayout layout =
                new LinearLayout(requireContext());

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding =
                (int) (
                        20 *
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                );

        layout.setPadding(
                padding,
                5,
                padding,
                0
        );


        EditText nameInput =
                new EditText(requireContext());

        nameInput.setHint(
                "Member Name"
        );


        EditText emailInput =
                new EditText(requireContext());

        emailInput.setHint(
                "Email Address"
        );

        emailInput.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );


        EditText phoneInput =
                new EditText(requireContext());

        phoneInput.setHint(
                "Phone Number"
        );

        phoneInput.setInputType(
                InputType.TYPE_CLASS_PHONE
        );


        layout.addView(nameInput);

        layout.addView(emailInput);

        layout.addView(phoneInput);


        AlertDialog dialog =
                new AlertDialog.Builder(
                        requireContext()
                )
                        .setTitle("Add Member")
                        .setView(layout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Add",
                                null
                        )
                        .create();


        dialog.setOnShowListener(
                dialogInterface -> {

                    dialog.getButton(
                            AlertDialog.BUTTON_POSITIVE
                    ).setOnClickListener(
                            v -> {

                                String name =
                                        nameInput
                                                .getText()
                                                .toString()
                                                .trim();

                                String email =
                                        emailInput
                                                .getText()
                                                .toString()
                                                .trim();

                                String phone =
                                        phoneInput
                                                .getText()
                                                .toString()
                                                .trim();


                                if (name.isEmpty()) {

                                    nameInput.setError(
                                            "Enter member name"
                                    );

                                    return;
                                }


                                if (email.isEmpty()) {

                                    emailInput.setError(
                                            "Enter email"
                                    );

                                    return;
                                }


                                if (phone.isEmpty()) {

                                    phoneInput.setError(
                                            "Enter phone number"
                                    );

                                    return;
                                }


                                addMember(
                                        name,
                                        email,
                                        phone
                                );

                                dialog.dismiss();
                            }
                    );
                }
        );


        dialog.show();
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

                .addOnSuccessListener(documentReference -> {

                    showToast(
                            "Member added successfully"
                    );

                    loadMembers();

                })

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
                memberList.size();

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

        if (memberList.isEmpty()) {

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