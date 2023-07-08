package com.example.homework1.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.homework1.R;
import com.example.homework1.models.Trainer;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentTrainer extends Fragment {
    private final String trainerName;
    private MaterialTextView trainer_name;
    private MaterialTextView trainer_caught;

    public FragmentTrainer(String trainerName) {
        this.trainerName = trainerName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trainer, container, false);
        findViews(view);
        initViews();
        return view;
    }

    private void initViews() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference trainersRef = db.getReference("trainers");

        trainersRef.orderByChild("name").equalTo(trainerName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Trainer trainer = data.getValue(Trainer.class);
                    if (trainer != null) {
                        trainer_name.setText(trainer.getName());
                        String caught = getString(R.string.trainer_caught_str) + trainer.getAfekaMons();
                        trainer_caught.setText(caught);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void findViews(View view) {
        trainer_name = view.findViewById(R.id.trainer_name);
        trainer_caught = view.findViewById(R.id.trainer_caught);
    }
}