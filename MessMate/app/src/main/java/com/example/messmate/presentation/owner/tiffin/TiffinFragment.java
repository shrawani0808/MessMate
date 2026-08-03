package com.example.messmate.presentation.owner.tiffin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.messmate.presentation.owner.members.model.Member;
import com.example.messmate.presentation.owner.tiffin.adapters.TiffinAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TiffinFragment extends Fragment {

    private RecyclerView recyclerTiffin;

    private LinearLayout emptyLayout;

    private TextView txtTodayDate;

    private TextView txtFullCount;
    private TextView txtHalfCount;
    private TextView txtNotCollectedCount;

    private FirebaseFirestore firestore;

    private SessionManager sessionManager;

    private String ownerId;

    private String todayDate;

    private final List<Member> memberList =
            new ArrayList<>();

    private TiffinAdapter adapter;


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


    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(
                view,
                savedInstanceState
        );


        initializeViews(view);

        firestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(requireContext());
        ownerId = sessionManager.getUid();

        todayDate = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(new Date());

        txtTodayDate.setText(new SimpleDateFormat(
                "EEEE, dd MMMM yyyy",
                        Locale.getDefault()).format(new Date()));

        setupRecyclerView();

        if (ownerId != null && !ownerId.isEmpty()) {

            loadMembers();

        } else {

            showToast("Owner session not found");
        }
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initializeViews(View view) {

        recyclerTiffin = view.findViewById(R.id.recyclerTiffin);
        emptyLayout = view.findViewById(R.id.emptyLayout);
        txtTodayDate = view.findViewById(R.id.txtTodayDate);
        txtFullCount = view.findViewById(R.id.txtFullCount);
        txtHalfCount = view.findViewById(R.id.txtHalfCount);
        txtNotCollectedCount = view.findViewById(R.id.txtNotCollectedCount);
    }


    // =========================================================
    // RECYCLER VIEW
    // =========================================================

    private void setupRecyclerView() {
        recyclerTiffin.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TiffinAdapter(memberList, this::saveCollection);
        recyclerTiffin.setAdapter(adapter);
    }


    // =========================================================
    // LOAD MEMBERS
    // =========================================================

    private void loadMembers() {

        firestore.collection("members").whereEqualTo("ownerId", ownerId).get()

                .addOnSuccessListener(querySnapshot -> {

                    memberList.clear();


                    querySnapshot.getDocuments().forEach(document -> {

                        Member member = document.toObject(Member.class);


                        if (member != null) {

                            member.setDocumentId(document.getId());


                            memberList.add(member);
                        }
                    });


                    adapter.notifyDataSetChanged();


                    updateEmptyState();


                    loadTodaySummary();
                })

                .addOnFailureListener(e -> {

                    showToast("Failed to load members");
                });
    }


    // =========================================================
    // SAVE TIFFIN / DINNER
    // =========================================================

    private void saveCollection(Member member, String tiffin, boolean dinner) {


        if (member.getDocumentId() == null) {

            showToast("Member ID not found");

            return;
        }


        Map<String, Object> data = new HashMap<>();


        data.put("ownerId", ownerId);


        data.put("memberDocumentId", member.getDocumentId());


        data.put("memberUid", member.getMemberUid());


        data.put("memberName", member.getName());


        data.put("phone", member.getPhone());


        data.put("date", todayDate);


        data.put("tiffin", tiffin);


        data.put("dinner", dinner);


        /*
         * One document per member per date.
         *
         * This makes updating today's collection
         * much easier.
         */

        String documentId = member.getDocumentId() + "_" + todayDate;


        firestore.collection("tiffin_records").document(documentId).set(data, SetOptions.merge())

                .addOnSuccessListener(unused -> {

                    loadTodaySummary();
                })

                .addOnFailureListener(e -> {

                            showToast("Failed to save collection: " + e.getMessage());
                        }
                );
    }


    // =========================================================
    // TODAY'S SUMMARY
    // =========================================================

    private void loadTodaySummary() {

        firestore.collection("tiffin_records").whereEqualTo("ownerId", ownerId).whereEqualTo("date", todayDate).get()

                .addOnSuccessListener(querySnapshot -> {

                    int full = 0;

                    int half = 0;

                    int notCollected = 0;


                    for (com.google.firebase.firestore.DocumentSnapshot document : querySnapshot) {

                        String tiffin = document.getString("tiffin");


                        if ("full".equals(tiffin)) {

                            full++;

                        } else if ("half".equals(tiffin)) {

                            half++;

                        } else {

                            notCollected++;
                        }
                    }


                    txtFullCount.setText(String.valueOf(full));


                    txtHalfCount.setText(String.valueOf(half));


                    txtNotCollectedCount.setText(String.valueOf(notCollected));
                });
    }


    // =========================================================
    // EMPTY STATE
    // =========================================================

    private void updateEmptyState() {

        if (memberList.isEmpty()) {

            emptyLayout.setVisibility(View.VISIBLE);

            recyclerTiffin.setVisibility(View.GONE);

        } else {

            emptyLayout.setVisibility(View.GONE);

            recyclerTiffin.setVisibility(View.VISIBLE);
        }
    }


    // =========================================================
    // REFRESH
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();


        if (ownerId != null && !ownerId.isEmpty()) {

            loadMembers();
        }
    }


    private void showToast(String message) {

        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}