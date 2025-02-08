package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ioe_enterprice.inventorytoolsmanagment.Adapter.InventariosAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.OngoingDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private EditText etBuscar;
    private RecyclerView recyclerView;
    private InventariosAdapter adapter;
    private List<OngoingDomain> inventoryList; // Lista completa de 10 registros
    private List<OngoingDomain> displayedList; // Lista filtrada que se muestra en el RecyclerView
    private TextView tvShowMore;
    private boolean isExpanded = false;

    // Datos de conexión a la base de datos
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.viewOngoing);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inventoryList = new ArrayList<>();
        adapter = new InventariosAdapter(inventoryList);
        recyclerView.setAdapter(adapter);

        etBuscar = findViewById(R.id.et_Buscar);
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvShowMore = findViewById(R.id.tv_ShowMore);
        tvShowMore.setOnClickListener(view -> toggleListSize());

        loadActiveInventories();
    }

    private void toggleListSize() {
        isExpanded = !isExpanded;
        adapter.updateList(isExpanded ? 10 : 4);
        tvShowMore.setText(isExpanded ? "Ver menos" : "Ver más");
    }

    private void loadActiveInventories() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                // Conexión a la base de datos
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement statement = connection.createStatement();

                // Carga los registros desde la base de datos
                String query = "SELECT " +
                        "cb.inventarioFolio, " +
                        "CONVERT(VARCHAR, cb.fechaInventario, 120) AS fechaInventario, " +
                        "CASE WHEN cb.tipoInventario = 'ARTICULO' THEN 'ongoing3' ELSE 'ongoing4' END AS picPath, " +
                        "ROUND(CAST(COUNT(CASE WHEN dt.ctdContada > 0 THEN 1 END) AS FLOAT) / COUNT(*) * 100, 0) AS progressPercent " +
                        "FROM cbInventarios cb " +
                        "LEFT JOIN dtInventariosArticulos dt ON cb.inventarioDocID = dt.inventarioDocID " +
                        "WHERE cb.estatus = 'ABIERTO' " +
                        "GROUP BY cb.fechaInventario, cb.tipoInventario";

                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    inventoryList.add(new OngoingDomain(
                            resultSet.getString("inventarioFolio"),
                            resultSet.getString("fechaInventario"),
                            resultSet.getInt("progressPercent"),
                            resultSet.getString("picPath")
                    ));
                }

                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al conectar a la base de datos", e);
            }
        });
        executor.shutdown();
    }
}