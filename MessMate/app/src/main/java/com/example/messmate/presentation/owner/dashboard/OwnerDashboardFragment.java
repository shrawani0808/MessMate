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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OwnerDashboardFragment extends Fragment {

    private TextView txtGreeting;
    private TextView txtOwnerName;
    private TextView txtProfileInitial;
    private TextView txtTodayDate;

    private TextView txtTiffinCollected;
    private TextView txtDinnerCollected;

    private TextView txtTotalMembers;
    private TextView txtPending;

    private TextView txtOverview;

    private FirebaseFirestore firestore;
    private SessionManager sessionManager;

    private String ownerId;

    private int totalMembers = 0;
    private int tiffinCollected = 0;
    private int dinnerCollected = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_owner_dashboard, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);

        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(requireContext());

        ownerId = sessionManager.getUid();

        setDate();

        if (ownerId != null && !ownerId.isEmpty()) {

            loadOwnerProfile();

            loadMemberCount();

            loadTodayCollection();

        } else {

            txtOverview.setText("Owner session not found.");
        }
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews(View view) {

        txtGreeting = view.findViewById(R.id.txtGreeting);

        txtOwnerName = view.findViewById(R.id.txtOwnerName);

        txtProfileInitial = view.findViewById(R.id.txtProfileInitial);

        txtTodayDate = view.findViewById(R.id.txtTodayDate);

        txtTiffinCollected = view.findViewById(R.id.txtTiffinCollected);

        txtDinnerCollected = view.findViewById(R.id.txtDinnerCollected);

        txtTotalMembers = view.findViewById(R.id.txtTotalMembers);

        txtPending = view.findViewById(R.id.txtPending);

        txtOverview = view.findViewById(R.id.txtOverview);
    }


    // =========================================================
    // DATE
    // =========================================================

    private void setDate() {

        Date today = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());

        txtTodayDate.setText(dateFormat.format(today));


        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour < 12) {

            greeting = "Good Morning";

        } else if (hour < 17) {

            greeting = "Good Afternoon";

        } else {

            greeting = "Good Evening";
        }

        txtGreeting.setText(greeting);
    }


    // =========================================================
    // LOAD OWNER PROFILE
    // =========================================================

    private void loadOwnerProfile() {

        firestore.collection("users").document(ownerId).get()

                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");

                        if (name != null && !name.trim().isEmpty()) {

                            txtOwnerName.setText(name);

                            String firstLetter = name.substring(0, 1).toUpperCase();

                            txtProfileInitial.setText(firstLetter);

                        } else {

                            txtOwnerName.setText("Owner");
                            txtProfileInitial.setText("O");
                        }

                    } else {

                        txtOwnerName.setText("Owner");
                        txtProfileInitial.setText("O");
                    }
                });
    }


    // =========================================================
    // LOAD TOTAL MEMBERS
    // =========================================================

    private void loadMemberCount() {

        firestore.collection("members").whereEqualTo("ownerId", ownerId).get()

                .addOnSuccessListener(querySnapshot -> {

                    totalMembers = querySnapshot.size();

                    txtTotalMembers.setText(String.valueOf(totalMembers));

                    updatePendingCount();

                })

                .addOnFailureListener(e -> {

                    txtTotalMembers.setText("0");
                });
    }


    // =========================================================
    // LOAD TODAY'S COLLECTION
    // =========================================================

    private void loadTodayCollection() {

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());


        firestore.collection("tiffin_records").whereEqualTo("ownerId", ownerId).get()

                .addOnSuccessListener(querySnapshot -> {

                    tiffinCollected = 0;
                    dinnerCollected = 0;

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String recordDate = document.getString("date");


                        // Only today's records
                        if (today.equals(recordDate)) {

                            /*
                             * IMPORTANT:
                             *
                             * Tiffin is now stored as a String:
                             *
                             * "full"
                             * "half"
                             * "none"
                             *
                             * So we must NOT use getBoolean("tiffin").
                             */

                            String tiffin = document.getString("tiffin");

                            Boolean dinner = document.getBoolean("dinner");


                            /*
                             * Full and Half both mean
                             * that a tiffin was collected.
                             *
                             * "none" means not collected.
                             */

                            if ("full".equals(tiffin) || "half".equals(tiffin)) {

                                tiffinCollected++;
                            }


                            if (Boolean.TRUE.equals(dinner)) {

                                dinnerCollected++;
                            }
                        }
                    }


                    txtTiffinCollected.setText(String.valueOf(tiffinCollected));


                    txtDinnerCollected.setText(String.valueOf(dinnerCollected));


                    updatePendingCount();

                    updateOverview();
                })

                .addOnFailureListener(e -> {

                    txtTiffinCollected.setText("0");

                    txtDinnerCollected.setText("0");

                    txtPending.setText("0");

                    txtOverview.setText("Unable to load today's collection.");
                });
    }


    // =========================================================
    // PENDING
    // =========================================================

    private void updatePendingCount() {

        int pending = totalMembers - tiffinCollected;

        if (pending < 0) {
            pending = 0;
        }

        txtPending.setText(String.valueOf(pending));
    }


    // =========================================================
    // OVERVIEW
    // =========================================================

    private void updateOverview() {

        if (totalMembers == 0) {

            txtOverview.setText("No members have been added yet.");

            return;
        }


        txtOverview.setText(tiffinCollected + " of " + totalMembers + " members collected today's tiffin.");
    }


    // =========================================================
    // REFRESH WHEN RETURNING TO DASHBOARD
    // =========================================================

    @Override
    public void onResume() {

        super.onResume();

        if (ownerId != null && !ownerId.isEmpty()) {

            loadMemberCount();

            loadTodayCollection();
        }
    }
}