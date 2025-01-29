package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.os.Bundle;
import android.util.Log;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventariosAdapter adapter;
    private List<OngoingDomain> inventoryList;

    // Datos de conexión a la base de datos
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.viewOngoing);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inventoryList = new ArrayList<>();
        adapter = new InventariosAdapter(inventoryList);
        recyclerView.setAdapter(adapter);

        // Cargar los inventarios desde la base de datos
        loadActiveInventories();
    }

    private void loadActiveInventories() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            try {
                // Conexión a la base de datos
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement statement = connection.createStatement();

                // Consulta SQL
                String query = "SELECT " +
                        "cb.inventarioFolio, " +
                        "CONVERT(VARCHAR, cb.fechaInventario, 120) AS fechaInventario, " +
                        "CASE WHEN cb.tipoInventario = 'ARTICULO' THEN 'ongoing3' ELSE 'ongoing4' END AS picPath, " +
                        "ROUND(CAST(COUNT(CASE WHEN dt.ctdContada > 0 THEN 1 END) AS FLOAT) / COUNT(*) * 100, 0) AS progressPercent " +
                        "FROM cbInventarios cb " +
                        "LEFT JOIN dtInventariosArticulos dt ON cb.inventarioDocID = dt.inventarioDocID " +
                        "WHERE cb.estatus = 'ABIERTO' " +
                        "GROUP BY cb.inventarioFolio, cb.fechaInventario, cb.tipoInventario";

                ResultSet resultSet = statement.executeQuery(query);

                // Procesar los resultados
                while (resultSet.next()) {
                    inventoryList.add(new OngoingDomain(
                            resultSet.getString("inventarioFolio"),
                            resultSet.getString("fechaInventario"),
                            resultSet.getInt("progressPercent"),
                            resultSet.getString("picPath")
                    ));
                }

                // Actualizar el RecyclerView en el hilo principal
                runOnUiThread(() -> adapter.notifyDataSetChanged());

                // Cerrar recursos
                resultSet.close();
                statement.close();
                connection.close();

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al conectar a la base de datos", e);
            }
        });

        try {
            // Esperar máximo 5 segundos
            future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.e("DB_ERROR", "Timeout al conectar a la base de datos", e);
            future.cancel(true); // Cancelar la tarea si excede el tiempo
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error en la ejecución", e);
        } finally {
            executor.shutdown();
        }
    }
}