package com.ioe_enterprice.inventorytoolsmanagment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ioe_enterprice.inventorytoolsmanagment.Adapter.OngoingAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.OngoingDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;
import com.ioe_enterprice.inventorytoolsmanagment.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private RecyclerView.Adapter adapterOngoing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initRecyclerView();
        //bottomNavigationInit();
    }

    //private void bottomNavigationInit() {
    //    binding.profileBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    //}

    private void initRecyclerView() {
        ArrayList<OngoingDomain> items=new ArrayList<>();
        items.add(new OngoingDomain("Food App","June 12, 2023",50,"ongoing1"));
        items.add(new OngoingDomain("AI Recoding","June 26, 2023",60,"ongoing2"));
        items.add(new OngoingDomain("Weather App","July 22, 2023",25,"ongoing3"));
        items.add(new OngoingDomain("E-Book App","June 13, 2023",80,"ongoing4"));
        binding.viewOngoing.setLayoutManager(new GridLayoutManager(this,2));
        adapterOngoing=new OngoingAdapter(items);
        binding.viewOngoing.setAdapter(adapterOngoing);
    }
}