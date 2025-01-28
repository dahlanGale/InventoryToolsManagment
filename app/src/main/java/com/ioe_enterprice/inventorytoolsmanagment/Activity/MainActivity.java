package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.ioe_enterprice.inventorytoolsmanagment.Adapter.OngoingAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.OngoingDomain;
import com.ioe_enterprice.inventorytoolsmanagment.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private RecyclerView.Adapter adapterOngoing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initRecyclerView();
        //bottomNavigationInit();
    }

    //private void bottomNavigationInit() {
    //    binding.profileBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    //}

    private void initRecyclerView() {
        ArrayList<OngoingDomain> items=new ArrayList<>();
        items.add(new OngoingDomain("CR-W/HMC","27/01/2025",50,"ongoing1"));
        items.add(new OngoingDomain("$50","15/12/2025",100,"ongoing2"));
        items.add(new OngoingDomain("Foto HC/HMC","10/08/2025",100,"ongoing3"));
        items.add(new OngoingDomain("Prueba Insumos","03/02/2025",0,"ongoing4"));
        binding.viewOngoing.setLayoutManager(new GridLayoutManager(this,2));
        adapterOngoing=new OngoingAdapter(items);
        binding.viewOngoing.setAdapter(adapterOngoing);
    }
}