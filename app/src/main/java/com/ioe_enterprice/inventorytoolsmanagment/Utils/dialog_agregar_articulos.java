package com.ioe_enterprice.inventorytoolsmanagment.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.AlmacenDomain;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.ArticuloDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;
import com.ioe_enterprice.inventorytoolsmanagment.Utils.SessionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class dialog_agregar_articulos extends AppCompatActivity {
    private TextInputEditText etSKU, etUPC, etDescripcion, etCantidad, etCosto;
    private Spinner spinnerAlmacen;
    private Button btnCancelar, btnGuardar, btnEscanearUPC;
    private SessionManager sessionManager;
    private List<AlmacenDomain> listaAlmacenes = new ArrayList<>();
    private AlmacenDomain almacenSeleccionado = null;
    private int inventarioFolio = 0;
    
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.219:1433/IOE_Business";
    private static final String DB_USER = "Admin1";
    private static final String DB_PASSWORD = "admin123";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private final ActivityResultLauncher<Intent> barcodeLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            IntentResult resultBarcode = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getData());
            if (resultBarcode != null && resultBarcode.getContents() != null) {
                String upcScanned = resultBarcode.getContents();
                etUPC.setText(upcScanned);
                buscarArticuloPorUPC(upcScanned);
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_agregar_articulos);
        
        // Inicializar SessionManager
        sessionManager = new SessionManager(this);
        
        // Obtener el folio de inventario de los extras
        inventarioFolio = getIntent().getIntExtra("INVENTARIO_FOLIO", 0);
        if (inventarioFolio == 0) {
            Toast.makeText(this, "Error: No se recibió el folio de inventario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Inicializar vistas
        etSKU = findViewById(R.id.etSKU);
        etUPC = findViewById(R.id.etUPC);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCantidad = findViewById(R.id.etCantidad);
        etCosto = findViewById(R.id.etCosto);
        spinnerAlmacen = findViewById(R.id.spinnerAlmacen);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEscanearUPC = findViewById(R.id.btnEscanearUPC);
        
        // Configurar listener para UPC para autocompleter los datos del artículo
        etUPC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    String upc = s.toString();
                    if (upc.length() >= 8) { // UPC mínimo para búsqueda
                        buscarArticuloPorUPC(upc);
                    }
                }
            }
        });
        
        // Cargar los almacenes para el spinner
        cargarAlmacenes();
        
        // Configurar listeners de botones
        btnEscanearUPC.setOnClickListener(v -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setPrompt("Escanea el código de barras");
            intentIntegrator.setBeepEnabled(true);
            intentIntegrator.setOrientationLocked(false);
            intentIntegrator.initiateScan();
        });
        
        btnCancelar.setOnClickListener(v -> finish());
        
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarArticulo();
            }
        });
        
        // Listener para el spinner de almacenes
        spinnerAlmacen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                almacenSeleccionado = listaAlmacenes.get(position);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                almacenSeleccionado = null;
            }
        });
    }
    
    /**
     * Carga los almacenes disponibles para la sucursal del usuario actual
     */
    private void cargarAlmacenes() {
        int sucursalID = sessionManager.getSucursalID();
        
        if (sucursalID <= 0) {
            Toast.makeText(this, "Error: No se encontró la sucursal del usuario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        executorService.execute(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                
                String query = "SELECT a.almacenID, a.almacenDescripcion, u.ubicacionID " +
                              "FROM tbAlmacen a " +
                              "INNER JOIN tbUbicacion u ON a.almacenID = u.almacenID " +
                              "WHERE a.status = 'A' AND a.sucursalID = ?";
                
                statement = connection.prepareStatement(query);
                statement.setInt(1, sucursalID);
                resultSet = statement.executeQuery();
                
                listaAlmacenes.clear();
                while (resultSet.next()) {
                    int almacenID = resultSet.getInt("almacenID");
                    String descripcion = resultSet.getString("almacenDescripcion");
                    int ubicacionID = resultSet.getInt("ubicacionID");
                    
                    AlmacenDomain almacen = new AlmacenDomain(almacenID, descripcion, ubicacionID);
                    listaAlmacenes.add(almacen);
                }
                
                runOnUiThread(() -> {
                    if (listaAlmacenes.isEmpty()) {
                        Toast.makeText(dialog_agregar_articulos.this, 
                                "No se encontraron almacenes para la sucursal", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // Crear adaptador para el spinner con la lista de almacenes
                        ArrayAdapter<AlmacenDomain> adapter = new ArrayAdapter<>(
                                dialog_agregar_articulos.this,
                                android.R.layout.simple_spinner_dropdown_item, 
                                listaAlmacenes);
                        spinnerAlmacen.setAdapter(adapter);
                    }
                });
                
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al cargar almacenes", e);
                runOnUiThread(() -> {
                    Toast.makeText(dialog_agregar_articulos.this, 
                            "Error al cargar almacenes: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    Log.e("DB_ERROR", "Error al cerrar conexión", e);
                }
            }
        });
    }
    
    /**
     * Busca un artículo por su UPC y autocompleta los campos
     * @param upc UPC del artículo a buscar
     */
    private void buscarArticuloPorUPC(String upc) {
        executorService.execute(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                
                String query = "SELECT articuloID, descripcion, UPC, costo " +
                              "FROM catArticulos " +
                              "WHERE UPC = ?";
                
                statement = connection.prepareStatement(query);
                statement.setString(1, upc);
                resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    int sku = resultSet.getInt("articuloID");
                    String descripcion = resultSet.getString("descripcion");
                    double costo = resultSet.getDouble("costo");
                    
                    runOnUiThread(() -> {
                        etSKU.setText(String.valueOf(sku));
                        etDescripcion.setText(descripcion);
                        etCosto.setText(String.valueOf(costo));
                        etCantidad.setText("1"); // Valor predeterminado
                        etCantidad.requestFocus();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(dialog_agregar_articulos.this, 
                                "No se encontró un artículo con el UPC: " + upc, 
                                Toast.LENGTH_SHORT).show();
                    });
                }
                
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al buscar artículo por UPC", e);
                runOnUiThread(() -> {
                    Toast.makeText(dialog_agregar_articulos.this, 
                            "Error al buscar artículo: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    Log.e("DB_ERROR", "Error al cerrar conexión", e);
                }
            }
        });
    }
    
    /**
     * Valida que todos los campos requeridos estén completos
     * @return true si son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        boolean isValid = true;
        
        if (etSKU.getText().toString().isEmpty()) {
            etSKU.setError("SKU requerido");
            isValid = false;
        }
        
        if (etUPC.getText().toString().isEmpty()) {
            etUPC.setError("UPC requerido");
            isValid = false;
        }
        
        if (etDescripcion.getText().toString().isEmpty()) {
            etDescripcion.setError("Descripción requerida");
            isValid = false;
        }
        
        if (etCantidad.getText().toString().isEmpty()) {
            etCantidad.setError("Cantidad requerida");
            isValid = false;
        }
        
        if (etCosto.getText().toString().isEmpty()) {
            etCosto.setError("Costo requerido");
            isValid = false;
        }
        
        if (almacenSeleccionado == null) {
            Toast.makeText(this, "Seleccione un almacén", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Guarda el artículo en la base de datos
     */
    private void guardarArticulo() {
        int usuarioID = sessionManager.getUserID();
        int sucursalID = sessionManager.getSucursalID();
        
        if (usuarioID <= 0 || sucursalID <= 0) {
            Toast.makeText(this, "Error: Información de usuario o sucursal no válida", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Obtener los valores de los campos
        int sku = Integer.parseInt(etSKU.getText().toString());
        long upc = Long.parseLong(etUPC.getText().toString());
        String descripcion = etDescripcion.getText().toString();
        double cantidad = Double.parseDouble(etCantidad.getText().toString());
        double costo = Double.parseDouble(etCosto.getText().toString());
        int ubicacionID = almacenSeleccionado.getUbicacionID();
        
        executorService.execute(() -> {
            Connection connection = null;
            PreparedStatement pstmt = null;
            PreparedStatement getInventarioIdStmt = null;
            ResultSet generatedKeys = null;
            ResultSet inventarioIdResult = null;
            
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                
                // Iniciar transacción
                connection.setAutoCommit(false);
                
                // Primero obtener el inventarioDocID usando el folio
                getInventarioIdStmt = connection.prepareStatement(
                        "SELECT inventarioDocID FROM cbInventarios WHERE inventarioFolio = ?");
                getInventarioIdStmt.setInt(1, inventarioFolio);
                inventarioIdResult = getInventarioIdStmt.executeQuery();
                
                int inventarioDocID = 0;
                if (inventarioIdResult.next()) {
                    inventarioDocID = inventarioIdResult.getInt("inventarioDocID");
                    Log.d("dialog_agregar_articulos", "inventarioDocID obtenido: " + inventarioDocID);
                } else {
                    runOnUiThread(() -> Toast.makeText(dialog_agregar_articulos.this, 
                            "Error: No se encontró el ID del inventario para el folio: " + inventarioFolio, 
                            Toast.LENGTH_LONG).show());
                    return;
                }
                
                // Insertar en dtInventariosArticulos
                String insertQuery = "INSERT INTO dtInventariosArticulos (inventarioDocID, SKU, UPC, descripcionCorta, " +
                        "ctdContada, stockTotal, ubicacionID, usuarioID, costo) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, inventarioDocID);
                pstmt.setInt(2, sku);
                pstmt.setLong(3, upc);
                pstmt.setString(4, descripcion);
                pstmt.setDouble(5, cantidad);
                pstmt.setDouble(6, 0); // Stock total se actualizará después
                pstmt.setInt(7, ubicacionID);
                pstmt.setInt(8, usuarioID);
                pstmt.setDouble(9, costo);
                
                int insertedRows = pstmt.executeUpdate();
                
                if (insertedRows > 0) {
                    generatedKeys = pstmt.getGeneratedKeys();
                    int inventarioArticuloID = 0;
                    if (generatedKeys.next()) {
                        inventarioArticuloID = generatedKeys.getInt(1);
                    }
                    
                    // Actualizar totales en cbInventarios
                    String updateQuery = "UPDATE cbInventarios SET totalArticulos = (SELECT COUNT(*) FROM dtInventariosArticulos " +
                            "WHERE inventarioDocID = ?), totalPiezas = (SELECT SUM(ctdContada) FROM dtInventariosArticulos " +
                            "WHERE inventarioDocID = ?) WHERE inventarioDocID = ?";
                    
                    pstmt = connection.prepareStatement(updateQuery);
                    pstmt.setInt(1, inventarioDocID);
                    pstmt.setInt(2, inventarioDocID);
                    pstmt.setInt(3, inventarioDocID);
                    pstmt.executeUpdate();
                    
                    // Confirmar transacción
                    connection.commit();
                    
                    // Crear objeto de artículo para devolver
                    final ArticuloDomain articulo = new ArticuloDomain();
                    articulo.setInventariosArtID(inventarioArticuloID);
                    articulo.setSKU(sku);
                    articulo.setUPC(upc);
                    articulo.setDescripcion(descripcion);
                    articulo.setCtdContada(cantidad);
                    articulo.setUbicacionID(ubicacionID);
                    articulo.setUsuarioID(usuarioID);
                    articulo.setAlmacenDescripcion(almacenSeleccionado.getAlmacenDescripcion());
                    
                    runOnUiThread(() -> {
                        // Devolver el ID del artículo insertado
                        Intent intent = new Intent();
                        intent.putExtra("INVENTARIO_ART_ID", articulo.getInventariosArtID());
                        setResult(RESULT_OK, intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(dialog_agregar_articulos.this, 
                                "Error al insertar artículo", Toast.LENGTH_SHORT).show();
                    });
                    connection.rollback();
                }
                
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al guardar artículo", e);
                try {
                    if (connection != null) {
                        connection.rollback();
                    }
                } catch (SQLException ex) {
                    Log.e("DB_ERROR", "Error al hacer rollback", ex);
                }
                runOnUiThread(() -> {
                    Toast.makeText(dialog_agregar_articulos.this, 
                            "Error al guardar artículo: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (generatedKeys != null) generatedKeys.close();
                    if (inventarioIdResult != null) inventarioIdResult.close();
                    if (getInventarioIdStmt != null) getInventarioIdStmt.close();
                    if (pstmt != null) pstmt.close();
                    if (connection != null) {
                        connection.setAutoCommit(true);
                        connection.close();
                    }
                } catch (SQLException e) {
                    Log.e("DB_ERROR", "Error al cerrar conexión", e);
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}