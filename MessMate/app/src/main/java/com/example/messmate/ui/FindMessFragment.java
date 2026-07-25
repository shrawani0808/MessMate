package com.example.messmate.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.adapters.MessAdapter;
import com.example.messmate.database.DatabaseHelper;
import com.example.messmate.models.MessModel;

import java.util.List;

public class FindMessFragment extends Fragment {

    private RecyclerView rvMessList;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_mess, container, false);

        rvMessList = view.findViewById(R.id.rvMessList);
        rvMessList.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new DatabaseHelper(requireContext());
        List<MessModel> messList = dbHelper.getAllMesses();

        MessAdapter adapter = new MessAdapter(messList, item ->
                Toast.makeText(getContext(), "Clicked: " + item.getName(), Toast.LENGTH_SHORT).show()
        );

        rvMessList.setAdapter(adapter);

        return view;
    }
}