package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.ioe_enterprice.inventorytoolsmanagment.Adapter.ArchiveAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Adapter.MyTeamAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.TeamDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;
import com.ioe_enterprice.inventorytoolsmanagment.databinding.ActivityProfileBinding;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    private RecyclerView.Adapter adapterArchive, adapterTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initRecyclerViewArchive();
        initRecyclerViewMyTeam();
    }

    private void initRecyclerViewMyTeam() {
        ArrayList<TeamDomain> items = new ArrayList<>();
        items.add(new TeamDomain("Food App Application", "Project in Progress"));
        items.add(new TeamDomain("AI Python", "Completed"));
        items.add(new TeamDomain("Weather App Backend", "Project in Progress"));
        items.add(new TeamDomain("Kotlin developers", "Completed"));

        binding.viewTeam.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapterTeam = new MyTeamAdapter(items);
        binding.viewTeam.setAdapter(adapterTeam);
    }

    private void initRecyclerViewArchive() {
        ArrayList<String> items = new ArrayList<>();
        items.add("UI/UX ScreenShot");
        items.add("Kotlin Source Code");
        items.add("Source Codes");

        binding.viewArchive.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterArchive = new ArchiveAdapter(items);
        binding.viewArchive.setAdapter(adapterArchive);
    }
}