package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;


import com.ioe_enterprice.inventorytoolsmanagment.Adapter.ConteoAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.ArticuloDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConteoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ConteoAdapter adapter;
    private List<ArticuloDomain> articuloList;
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo);

        // 📌 Asegúrate de inicializar `articuloList` antes de usarla
        articuloList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerConteo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ConteoAdapter(articuloList, this);
        recyclerView.setAdapter(adapter);

        String inventarioFolio = getIntent().getStringExtra("INVENTARIO_FOLIO");
        if (inventarioFolio != null) {
            loadInventarioDetalles(inventarioFolio);
        } else {
            Log.e("ConteoActivity", "Error: No se recibió INVENTARIO_FOLIO");
        }

        EditText etBuscar = findViewById(R.id.et_Buscar);
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    adapter.notifyDataSetChanged(); // 🔹 Restaurar lista completa si está vacío
                } else {
                    adapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });


    }

    private void loadInventarioDetalles(String inventarioFolio) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT ia.inventariosArtID, ia.SKU, ia.UPC, ia.descripcionCorta, ia.ctdContada, ia.stockTotal, " +
                                "ia.ubicacionID, ia.usuarioID, " +
                                "ISNULL(a.almacenDescripcion, 'Sin Almacén') AS almacenDescripcion " + // 🔹 Si no hay almacén, poner 'Sin Almacén'
                                "FROM dtInventariosArticulos ia " +
                                "LEFT JOIN dtProductosUbicacion pu ON ia.ubicacionID = pu.productosUbicacionID " +
                                "LEFT JOIN tbAlmacenes a ON pu.almacenID = a.almacenID " +
                                "WHERE ia.inventarioDocID = (SELECT inventarioDocID FROM cbInventarios WHERE inventarioFolio = ?)");
                statement.setString(1, inventarioFolio);

                ResultSet resultSet = statement.executeQuery();
                List<ArticuloDomain> tempList = new ArrayList<>();

                while (resultSet.next()) {
                    tempList.add(new ArticuloDomain(
                            resultSet.getInt("inventariosArtID"),
                            resultSet.getInt("SKU"),
                            resultSet.getLong("UPC"),
                            resultSet.getString("descripcionCorta"),
                            resultSet.getDouble("ctdContada"),
                            resultSet.getDouble("stockTotal"),
                            resultSet.getInt("ubicacionID"),
                            resultSet.getInt("usuarioID"),
                            resultSet.getString("almacenDescripcion")  // 🔹 Se agrega el nombre del almacén
                    ));
                }

                resultSet.close();
                statement.close();
                connection.close();

                runOnUiThread(() -> {
                    articuloList.clear();
                    articuloList.addAll(tempList);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al cargar detalles del inventario", e);
            }
        });
        executor.shutdown();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent); // 🔹 Indica que hay datos actualizados
        finish(); // Cierra la actividad y regresa al MainActivity
    }

}