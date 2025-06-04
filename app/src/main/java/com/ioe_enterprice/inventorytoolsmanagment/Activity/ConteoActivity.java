package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.219:1433/IOE_Business";
    private static final String DB_USER = "Admin1";
    private static final String DB_PASSWORD = "admin123";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private EditText etBuscar;
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private String tipoConteo;
    private Button btnAgregarArticulos;
    private String inventarioFolio; // Para almacenar el folio del inventario
    private static final int REQUEST_AGREGAR_ARTICULO = 1001;

    // Launcher para iniciar ScannerActivity y recibir su resultado
    private final ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        String codigoBarras = result.getData().getStringExtra("CODIGO_BARRAS");
                        if (codigoBarras != null && !codigoBarras.isEmpty()) {
                            etBuscar.setText(codigoBarras);
                            adapter.getFilter().filter(codigoBarras);
                            Toast.makeText(ConteoActivity.this, "Código escaneado: " + codigoBarras, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

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

        // Inicializar el botón btnAgregarArticulos
        btnAgregarArticulos = findViewById(R.id.btnAgregarArticulos);
        
        // Configurar el clic del botón Agregar Artículos
        btnAgregarArticulos.setOnClickListener(v -> {
            // Iniciar el diálogo para agregar artículos
            Intent intent = new Intent(this, com.ioe_enterprice.inventorytoolsmanagment.Utils.dialog_agregar_articulos.class);
            // Pasar el folio del inventario a la actividad dialog_agregar_articulos
            try {
                int inventarioFolioInt = Integer.parseInt(inventarioFolio);
                intent.putExtra("INVENTARIO_FOLIO", inventarioFolioInt);
                Log.d("ConteoActivity", "Enviando INVENTARIO_FOLIO: " + inventarioFolioInt);
            } catch (NumberFormatException e) {
                Log.e("ConteoActivity", "Error al convertir inventarioFolio a entero: " + inventarioFolio, e);
                Toast.makeText(this, "Error: El folio del inventario no es válido", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivityForResult(intent, REQUEST_AGREGAR_ARTICULO);
        });

        // Obtener el folio del inventario
        inventarioFolio = getIntent().getStringExtra("INVENTARIO_FOLIO");
        if (inventarioFolio != null) {
            loadInventarioDetalles(inventarioFolio);
        } else {
            Log.e("ConteoActivity", "Error: No se recibió INVENTARIO_FOLIO");
        }

        String tipoConteo = getIntent().getStringExtra("TIPO_CONTEO");
        if (tipoConteo != null) {
            tipoConteo(tipoConteo);
        }
        else {
            Log.e("ConteoActivity", "Error: No se recibió TIPO_CONTEO");
        }

        // Configurar el filtro de búsqueda
        etBuscar = findViewById(R.id.et_Buscar);
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
        
        // Configurar el botón de escaneo
        ImageButton btnEscanear = findViewById(R.id.btnEscanear);
        btnEscanear.setOnClickListener(view -> {
            // Comprobar si tenemos permiso de cámara
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Solicitar permiso si no lo tenemos
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            } else {
                // Iniciar el escáner si tenemos permiso
                startScanner();
            }
        });
    }
    
    // Método para iniciar la actividad de escaneo
    private void startScanner() {
        Intent intent = new Intent(this, ScannerActivity.class);
        scannerLauncher.launch(intent);
    }
    
    // Manejo de respuesta de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, iniciar el escáner
                startScanner();
            } else {
                // Permiso denegado, mostrar mensaje
                Toast.makeText(this, "Se requiere permiso de cámara para escanear códigos de barras", Toast.LENGTH_SHORT).show();
            }
        }
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
                    // Verificar explícitamente si ctdContada es NULL en la base de datos
                    Double ctdContada = null;
                    Object ctdContadaObj = resultSet.getObject("ctdContada");
                    if (ctdContadaObj != null) {
                        ctdContada = resultSet.getDouble("ctdContada");
                        Log.d("DB_LOAD", "ctdContada cargado: " + ctdContada + " para artículo: " + resultSet.getInt("inventariosArtID"));
                    } else {
                        Log.d("DB_LOAD", "ctdContada NULL para artículo: " + resultSet.getInt("inventariosArtID"));
                    }
                    
                    ArticuloDomain articulo = new ArticuloDomain(
                            resultSet.getInt("inventariosArtID"),
                            resultSet.getInt("SKU"),
                            resultSet.getLong("UPC"),
                            resultSet.getString("descripcionCorta"),
                            ctdContada, // Usar nuestro valor verificado
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
        super.onBackPressed();
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent); // Indicar que hay datos actualizados
        finish(); // Cerrar la actividad y regresar al MainActivity
    }

    private void tipoConteo(String tipoConteo) {
        this.tipoConteo = tipoConteo;

        // Configurar el adaptador para el tipo de conteo
        if (tipoConteo.equals("ARTICULO")) {
            btnAgregarArticulos.setVisibility(View.VISIBLE);
        } else {
            btnAgregarArticulos.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_AGREGAR_ARTICULO && resultCode == Activity.RESULT_OK) {
            // Procesar el resultado del diálogo de agregar artículos
            if (data != null) {
                int sku = data.getIntExtra("SKU", 0);
                long upc = data.getLongExtra("UPC", 0);
                String descripcion = data.getStringExtra("DESCRIPCION");
                Double cantidad = data.getDoubleExtra("CANTIDAD", 0.0);
                String almacen = data.getStringExtra("ALMACEN");
                
                // Crear un nuevo ArticuloDomain con los datos recibidos
                ArticuloDomain nuevoArticulo = new ArticuloDomain(
                        0, // inventariosArtID temporal (se asignará en la base de datos)
                        sku,
                        upc,
                        descripcion,
                        cantidad,
                        0, // stockTotal (se actualizará según la base de datos)
                        0, // ubicacionID (se asignará en la base de datos)
                        0, // usuarioID (se obtendrá del usuario actual)
                        almacen
                );
                
                // Agregar el nuevo artículo a la base de datos
                agregarArticuloABaseDeDatos(nuevoArticulo);
            }
        }
    }
    
    private void agregarArticuloABaseDeDatos(ArticuloDomain articulo) {
        // Mostrar mensaje de carga
        Toast.makeText(this, "Agregando artículo...", Toast.LENGTH_SHORT).show();
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                
                // Primero obtener el inventarioDocID usando el folio
                PreparedStatement getInventarioIdStatement = connection.prepareStatement(
                        "SELECT inventarioDocID FROM cbInventarios WHERE inventarioFolio = ?");
                getInventarioIdStatement.setString(1, inventarioFolio);
                ResultSet inventarioIdResult = getInventarioIdStatement.executeQuery();
                
                int inventarioDocID = 0;
                if (inventarioIdResult.next()) {
                    inventarioDocID = inventarioIdResult.getInt("inventarioDocID");
                } else {
                    runOnUiThread(() -> Toast.makeText(ConteoActivity.this, 
                            "Error: No se encontró el ID del inventario", Toast.LENGTH_LONG).show());
                    return;
                }
                
                // Verificar si el artículo ya existe en el inventario
                PreparedStatement checkArticuloStatement = connection.prepareStatement(
                        "SELECT inventariosArtID FROM dtInventariosArticulos WHERE inventarioDocID = ? AND SKU = ?");
                checkArticuloStatement.setInt(1, inventarioDocID);
                checkArticuloStatement.setInt(2, articulo.getSKU());
                ResultSet checkResult = checkArticuloStatement.executeQuery();
                
                if (checkResult.next()) {
                    // El artículo ya existe, mostrar mensaje
                    runOnUiThread(() -> Toast.makeText(ConteoActivity.this, 
                            "El artículo con SKU " + articulo.getSKU() + " ya existe en este inventario", 
                            Toast.LENGTH_LONG).show());
                } else {
                    // Insertar el nuevo artículo
                    PreparedStatement insertStatement = connection.prepareStatement(
                            "INSERT INTO dtInventariosArticulos (inventarioDocID, SKU, UPC, descripcionCorta, ctdContada) " +
                                    "VALUES (?, ?, ?, ?, ?)");
                    insertStatement.setInt(1, inventarioDocID);
                    insertStatement.setInt(2, articulo.getSKU());
                    insertStatement.setLong(3, articulo.getUPC());
                    insertStatement.setString(4, articulo.getDescripcion());
                    insertStatement.setDouble(5, articulo.getCtdContada());
                    
                    int rowsAffected = insertStatement.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        // Éxito, refrescar la lista
                        runOnUiThread(() -> {
                            Toast.makeText(ConteoActivity.this, 
                                    "Artículo agregado correctamente", Toast.LENGTH_SHORT).show();
                            // Recargar los detalles del inventario para mostrar el nuevo artículo
                            loadInventarioDetalles(inventarioFolio);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(ConteoActivity.this, 
                                "No se pudo agregar el artículo", Toast.LENGTH_LONG).show());
                    }
                    
                    insertStatement.close();
                }
                
                checkResult.close();
                checkArticuloStatement.close();
                inventarioIdResult.close();
                getInventarioIdStatement.close();
                connection.close();
                
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al agregar artículo a la base de datos", e);
                runOnUiThread(() -> Toast.makeText(ConteoActivity.this, 
                        "Error al agregar artículo: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
        executor.shutdown();
    }
}