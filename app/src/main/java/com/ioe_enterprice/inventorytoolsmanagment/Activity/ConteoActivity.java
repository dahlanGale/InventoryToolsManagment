package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConteoActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ConteoAdapter adapter;
    private List<ArticuloDomain> articuloList;
    private Set<String> almacenesSet; // Usamos un Set para almacenes únicos
    private String selectedAlmacen = null; // Almacén seleccionado para el filtro (null si ninguno está seleccionado)
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
        almacenesSet = new HashSet<>(); // Inicializar el Set de almacenes

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
                    ArticuloDomain articulo = new ArticuloDomain(
                            resultSet.getInt("inventariosArtID"),
                            resultSet.getInt("SKU"),
                            resultSet.getLong("UPC"),
                            resultSet.getString("descripcionCorta"),
                            resultSet.getObject("ctdContada") != null ? resultSet.getDouble("ctdContada") : null,
                            resultSet.getDouble("stockTotal"),
                            resultSet.getInt("ubicacionID"),
                            resultSet.getInt("usuarioID"),
                            resultSet.getString("almacenDescripcion")
                    );
                    tempList.add(articulo);
                    almacenesSet.add(articulo.getAlmacenDescripcion()); // Agregar almacén al Set
                }

                resultSet.close();
                statement.close();
                connection.close();

                runOnUiThread(() -> {
                    articuloList.clear();
                    articuloList.addAll(tempList);
                    adapter.actualizarLista(tempList); // Actualizar la lista en el adaptador
                    createAlmacenButtons(); // Crear los botones de filtrado
                });

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al cargar detalles del inventario", e);
            }
        });
        executor.shutdown();
    }

    private void createAlmacenButtons() {
        containerFiltrosAlmacen.removeAllViews(); // Limpiar botones anteriores

        // Crear botones de filtrado para cada almacén
        for (String almacen : almacenesSet) {
            Button button = new Button(this);
            button.setText(almacen);
            button.setBackgroundResource(R.drawable.button_filter_background);
            button.setTextColor(getResources().getColor(R.color.black));
            button.setPadding(16, 8, 16, 8);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                // Si el almacén ya está seleccionado, lo deseleccionamos
                if (almacen.equals(selectedAlmacen)) {
                    selectedAlmacen = null;
                    button.setBackgroundResource(R.drawable.button_filter_background);
                    button.setTextColor(getResources().getColor(R.color.black));
                } else {
                    // Deseleccionar el botón anterior si existe
                    if (selectedAlmacen != null) {
                        for (int i = 0; i < containerFiltrosAlmacen.getChildCount(); i++) {
                            Button otherButton = (Button) containerFiltrosAlmacen.getChildAt(i);
                            if (otherButton.getText().toString().equals(selectedAlmacen)) {
                                otherButton.setBackgroundResource(R.drawable.button_filter_background);
                                otherButton.setTextColor(getResources().getColor(R.color.black));
                                break;
                            }
                        }
                    }
                    // Seleccionar el nuevo almacén
                    selectedAlmacen = almacen;
                    button.setBackgroundResource(R.drawable.button_filter_background_selected);
                    button.setTextColor(getResources().getColor(R.color.white));
                }
                applyAlmacenFilter();
            });

            containerFiltrosAlmacen.addView(button);
        }
    }

    private void applyAlmacenFilter() {
        if (selectedAlmacen == null) {
            // Mostrar todos los artículos si no hay filtro
            adapter.actualizarLista(new ArrayList<>(articuloList));
        } else {
            List<ArticuloDomain> filteredList = new ArrayList<>();
            for (ArticuloDomain articulo : articuloList) {
                if (selectedAlmacen.equals(articulo.getAlmacenDescripcion())) {
                    filteredList.add(articulo);
                }
            }
            adapter.actualizarLista(filteredList);
        }
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent); // Indicar que hay datos actualizados
        finish(); // Cerrar la actividad y regresar al MainActivity
    }
}