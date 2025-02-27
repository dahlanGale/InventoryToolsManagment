package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private List<String> almacenesList; // Lista de almacenes disponibles
    private List<String> selectedAlmacenes = new ArrayList<>(); // Almacenes seleccionados para el filtro
    private LinearLayout containerFiltrosAlmacen;
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo);

        // Inicializar la lista de artículos
        articuloList = new ArrayList<>();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerConteo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ConteoAdapter(articuloList, this);
        recyclerView.setAdapter(adapter);

        // Obtener el folio del inventario
        String inventarioFolio = getIntent().getStringExtra("INVENTARIO_FOLIO");
        if (inventarioFolio != null) {
            loadInventarioDetalles(inventarioFolio);
        } else {
            Log.e("ConteoActivity", "Error: No se recibió INVENTARIO_FOLIO");
        }

        // Configurar el filtro de búsqueda
        EditText etBuscar = findViewById(R.id.et_Buscar);
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s.toString()); // Aplicar el filtro
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Inicializar el contenedor de filtros de almacén
        containerFiltrosAlmacen = findViewById(R.id.containerFiltrosAlmacen);

        // Cargar los almacenes disponibles
        loadAlmacenes();
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
                                "ISNULL(a.almacenDescripcion, 'Sin Almacén') AS almacenDescripcion " +
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
                            resultSet.getString("almacenDescripcion")
                    ));
                }

                resultSet.close();
                statement.close();
                connection.close();

                runOnUiThread(() -> {
                    adapter.actualizarLista(tempList); // Actualizar la lista en el adaptador
                });

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al cargar detalles del inventario", e);
            }
        });
        executor.shutdown();
    }

    private void loadAlmacenes() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT DISTINCT almacenDescripcion FROM tbAlmacenes");
                ResultSet resultSet = statement.executeQuery();

                almacenesList = new ArrayList<>();
                while (resultSet.next()) {
                    almacenesList.add(resultSet.getString("almacenDescripcion"));
                }

                resultSet.close();
                statement.close();
                connection.close();

                runOnUiThread(() -> {
                    // Crear botones de filtrado para cada almacén
                    for (String almacen : almacenesList) {
                        Button button = new Button(this);
                        button.setText(almacen);
                        button.setBackgroundResource(R.drawable.button_filter_background);
                        button.setTextColor(getResources().getColor(R.color.dark_blue));
                        button.setOnClickListener(v -> {
                            if (selectedAlmacenes.contains(almacen)) {
                                selectedAlmacenes.remove(almacen);
                                button.setBackgroundResource(R.drawable.button_filter_background);
                            } else {
                                selectedAlmacenes.add(almacen);
                                button.setBackgroundResource(R.drawable.button_filter_background_selected);
                            }
                            applyAlmacenFilter(); // Aplicar el filtro
                        });
                        containerFiltrosAlmacen.addView(button);
                    }
                });

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al cargar almacenes", e);
            }
        });
        executor.shutdown();
    }

    private void applyAlmacenFilter() {
        if (selectedAlmacenes.isEmpty()) {
            adapter.actualizarLista(articuloList); // Mostrar todos los artículos si no hay filtros
        } else {
            List<ArticuloDomain> filteredList = new ArrayList<>();
            for (ArticuloDomain articulo : articuloList) {
                if (selectedAlmacenes.contains(articulo.getAlmacenDescripcion())) {
                    filteredList.add(articulo);
                }
            }
            adapter.actualizarLista(filteredList); // Aplicar el filtro
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent); // Indicar que hay datos actualizados
        finish(); // Cerrar la actividad y regresar al MainActivity
    }
}