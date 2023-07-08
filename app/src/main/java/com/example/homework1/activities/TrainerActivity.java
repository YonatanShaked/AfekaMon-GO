package com.example.homework1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homework1.R;
import com.example.homework1.fragments.FragmentMap;
import com.example.homework1.fragments.FragmentTrainer;

public class TrainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);
        initViews();
    }

    private void initViews() {
        FragmentTrainer fragmentTrainer = new FragmentTrainer(getIntent().getExtras().getString("trainer"));
        getSupportFragmentManager().beginTransaction().add(R.id.topTen_LAY_list, fragmentTrainer).commit();

        FragmentMap fragmentMap = new FragmentMap();
        getSupportFragmentManager().beginTransaction().replace(R.id.topTen_LAY_map, fragmentMap).commit();

        ImageButton returnBtn = findViewById(R.id.return_button);
        returnBtn.setOnClickListener(v -> {
            Intent myIntent = new Intent(this, GameMenuActivity.class);
            myIntent.putExtra("caught", false);
            startActivity(myIntent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
