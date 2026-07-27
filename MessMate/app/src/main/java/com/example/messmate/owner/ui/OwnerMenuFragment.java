package com.example.messmate.owner.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.messmate.common.models.MenuCategoryModel;
import com.example.messmate.owner.ui.adapters.MenuAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OwnerMenuFragment extends Fragment {

    private ImageButton btnNotification;
    private RecyclerView rvMenuList;
    private MaterialButton btnAddItem;

    private MenuAdapter menuAdapter;
    private List<MenuCategoryModel> menuCategoryList;

    public OwnerMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Edge-to-edge window insets
        View rootOwnerMenu = view.findViewById(R.id.rootOwnerMenu);
        if (rootOwnerMenu != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootOwnerMenu, (v, insets) -> {
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
        rvMenuList = view.findViewById(R.id.rvMenuList);
        btnAddItem = view.findViewById(R.id.btnAddItem);

        // Load reference data matching MessMate UI
        setupMenuData();

        // Setup RecyclerView
        if (rvMenuList != null) {
            rvMenuList.setLayoutManager(new LinearLayoutManager(requireContext()));
            menuAdapter = new MenuAdapter(menuCategoryList);
            rvMenuList.setAdapter(menuAdapter);

            menuAdapter.setOnCategoryClickListener(category ->
                    Toast.makeText(requireContext(), "Edit " + category.getCategoryName(), Toast.LENGTH_SHORT).show()
            );
        }

        // Click listeners
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnAddItem != null) {
            btnAddItem.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Add Item Clicked", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupMenuData() {
        menuCategoryList = new ArrayList<>();
        menuCategoryList.add(new MenuCategoryModel("Breakfast", "Poha, Banana, Tea"));
        menuCategoryList.add(new MenuCategoryModel("Lunch", "Dal, Rice, Paneer Curry, Salad"));
        menuCategoryList.add(new MenuCategoryModel("Evening Snacks", "Upma, Chutney, Tea"));
        menuCategoryList.add(new MenuCategoryModel("Dinner", "Roti, Mix Veg, Dal Tadka"));
    }
}