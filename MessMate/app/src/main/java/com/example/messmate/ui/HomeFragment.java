package com.example.messmate.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.adapters.FeaturedMessAdapter;
import com.example.messmate.adapters.MessAdapter; // Reusing your existing MessAdapter
import com.example.messmate.database.DatabaseHelper;
import com.example.messmate.models.MessModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvFeatured, rvAllMesses;
    private EditText etSearch;
    private DatabaseHelper dbHelper;

    private MessAdapter allMessesAdapter;
    private FeaturedMessAdapter featuredAdapter;

    private List<MessModel> allMessesList = new ArrayList<>();
    private List<MessModel> featuredList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Views
        rvFeatured = view.findViewById(R.id.rvFeaturedMesses);
        rvAllMesses = view.findViewById(R.id.rvAllMesses);
        etSearch = view.findViewById(R.id.etHomeSearch);
        dbHelper = new DatabaseHelper(requireContext());

        // Load Data from SQLite
        loadData();

        // Setup Horizontal Featured List
        featuredAdapter = new FeaturedMessAdapter(requireContext(), featuredList, mess -> {
            Toast.makeText(requireContext(), "Clicked Featured: " + mess.getName(), Toast.LENGTH_SHORT).show();
        });
        rvFeatured.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFeatured.setAdapter(featuredAdapter);

        // Setup Vertical Main List
        allMessesAdapter = new MessAdapter(allMessesList, mess -> {
            Toast.makeText(requireContext(), "Selected: " + mess.getName(), Toast.LENGTH_SHORT).show();
        });
        rvAllMesses.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAllMesses.setAdapter(allMessesAdapter);

        // Real-time Search Listener
        setupSearch();

        return view;
    }

    private void loadData() {
        allMessesList.clear();
        featuredList.clear();

        // Fetch from Database (or mock data if DB is empty)
        List<MessModel> dbData = dbHelper.getAllMesses();
        if (dbData != null && !dbData.isEmpty()) {
            allMessesList.addAll(dbData);

            // Pick top rated messes for Featured section
            for (MessModel m : dbData) {
                if (m.getRating() >= 4.0) {
                    featuredList.add(m);
                }
            }
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMesses(s.toString());
            }

            @Override
            public void  afterTextChanged(Editable s) {}
        });
    }

    private void filterMesses(String query) {
        List<MessModel> filteredList = new ArrayList<>();
        for (MessModel mess : allMessesList) {
            if (mess.getName().toLowerCase().contains(query.toLowerCase()) ||
                    mess.getLocation().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(mess);
            }
        }
        allMessesAdapter.updateList(filteredList);
    }
}