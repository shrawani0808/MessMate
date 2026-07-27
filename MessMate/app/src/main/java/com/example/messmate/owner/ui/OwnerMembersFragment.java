package com.example.messmate.owner.ui;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.common.models.MemberModel;
import com.example.messmate.owner.ui.adapters.MembersAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class OwnerMembersFragment extends Fragment {

    private ImageButton btnNotification;
    private EditText etSearchMembers;
    private RecyclerView rvMembersList;
    private FloatingActionButton fabAddMember;

    private MembersAdapter membersAdapter;
    private List<MemberModel> memberModelList;

    public OwnerMembersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // WindowInsets handling
        View rootOwnerMembers = view.findViewById(R.id.rootOwnerMembers);
        if (rootOwnerMembers != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootOwnerMembers, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );
                return insets;
            });
        }

        // Initialize Views
        btnNotification = view.findViewById(R.id.btnNotification);
        etSearchMembers = view.findViewById(R.id.etSearchMembers);
        rvMembersList = view.findViewById(R.id.rvMembersList);
        fabAddMember = view.findViewById(R.id.fabAddMember);

        // Prepare dummy data matching UI screenshot
        setupMemberList();

        // Setup RecyclerView & Adapter
        if (rvMembersList != null) {
            rvMembersList.setLayoutManager(new LinearLayoutManager(requireContext()));
            membersAdapter = new MembersAdapter(memberModelList);
            rvMembersList.setAdapter(membersAdapter);

            membersAdapter.setOnMemberClickListener(memberModel ->
                    Toast.makeText(requireContext(), "Clicked: " + memberModel.getName(), Toast.LENGTH_SHORT).show()
            );
        }

        // Setup Button Listeners
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
            );
        }

        if (fabAddMember != null) {
            fabAddMember.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Add MemberModel Clicked", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupMemberList() {
        memberModelList = new ArrayList<>();
        memberModelList.add(new MemberModel("Aarav Sharma", "Monthly Plan", "Active", android.R.drawable.ic_menu_gallery));
        memberModelList.add(new MemberModel("Rohit Patil", "Tiffin Plan", "Active", android.R.drawable.ic_menu_gallery));
        memberModelList.add(new MemberModel("Sneha Joshi", "Monthly Plan", "Active", android.R.drawable.ic_menu_gallery));
        memberModelList.add(new MemberModel("Vikram More", "Tiffin Plan", "Inactive", android.R.drawable.ic_menu_gallery));
    }
}