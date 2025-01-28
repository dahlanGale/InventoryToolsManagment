package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.ioe_enterprice.inventorytoolsmanagment.Adapter.ArchiveAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Adapter.MyTeamAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.TeamDomain;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        items.add(new TeamDomain("CR W y HMC", "Estatus: Activo"));
        items.add(new TeamDomain("Armazones $50", "Estatus: Revisión"));
        items.add(new TeamDomain("Mono Foto HC/HMC", "Estatus: Contabilizado"));
        items.add(new TeamDomain("Prueba Insumos", "Cancelado"));

        binding.viewTeam.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapterTeam = new MyTeamAdapter(items);
        binding.viewTeam.setAdapter(adapterTeam);
    }

    private void initRecyclerViewArchive() {
        ArrayList<String> items = new ArrayList<>();
        items.add("Jerarquía");
        items.add("Articulos");

        binding.viewArchive.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterArchive = new ArchiveAdapter(items);
        binding.viewArchive.setAdapter(adapterArchive);
    }
}