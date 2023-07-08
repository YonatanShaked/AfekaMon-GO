package com.example.homework1.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.homework1.R;
import com.google.android.gms.maps.SupportMapFragment;

public class FragmentMap extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        // Init map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        // Async map
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(googleMap -> {

        });
        return view;
    }
}