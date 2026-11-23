package com.antiarnaque.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antiarnaque.app.R;

/** Conseils de sécurité, affichés sans aucune donnée personnelle. */
public final class TipsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tips, container, false);
        ListView list = view.findViewById(R.id.list_tips);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.security_tips, android.R.layout.simple_list_item_1);
        adapter.setNotifyOnChange(false);
        list.setAdapter(adapter);
        return view;
    }
}
