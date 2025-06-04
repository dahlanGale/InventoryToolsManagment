package com.ioe_enterprice.inventorytoolsmanagment.Utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.ioe_enterprice.inventorytoolsmanagment.Activity.ScannerActivity;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.ArticuloDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;

public class dialog_agregar_articulos extends AppCompatActivity {

    private TextInputEditText etSKU, etUPC, etDescripcion, etCantidad, etAlmacen;
    private Button btnCancelar, btnGuardar, btnEscanearUPC;
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    // Launcher para iniciar ScannerActivity y recibir su resultado
    private final ActivityResultLauncher<Intent> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        String codigoBarras = result.getData().getStringExtra("CODIGO_BARRAS");
                        if (codigoBarras != null && !codigoBarras.isEmpty()) {
                            etUPC.setText(codigoBarras);
                            Toast.makeText(dialog_agregar_articulos.this, "Código escaneado: " + codigoBarras, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_agregar_articulos);

        // Inicializar vistas
        etSKU = findViewById(R.id.etSKU);
        etUPC = findViewById(R.id.etUPC);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCantidad = findViewById(R.id.etCantidad);
        etAlmacen = findViewById(R.id.etAlmacen);
        
        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEscanearUPC = findViewById(R.id.btnEscanearUPC);

        // Configurar el botón de cancelar
        btnCancelar.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        // Configurar el botón de guardar
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarArticulo();
            }
        });

        // Configurar el botón de escanear UPC
        btnEscanearUPC.setOnClickListener(v -> {
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

    // Validación de permisos de cámara
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

    // Validar que todos los campos obligatorios estén completos
    private boolean validarCampos() {
        boolean isValid = true;
        
        // Validar SKU
        if (TextUtils.isEmpty(etSKU.getText())) {
            etSKU.setError("El SKU es obligatorio");
            isValid = false;
        }
        
        // Validar UPC
        if (TextUtils.isEmpty(etUPC.getText())) {
            etUPC.setError("El UPC es obligatorio");
            isValid = false;
        }
        
        // Validar descripción
        if (TextUtils.isEmpty(etDescripcion.getText())) {
            etDescripcion.setError("La descripción es obligatoria");
            isValid = false;
        }
        
        // Validar cantidad
        if (TextUtils.isEmpty(etCantidad.getText())) {
            etCantidad.setError("La cantidad es obligatoria");
            isValid = false;
        }
        
        // Validar almacén
        if (TextUtils.isEmpty(etAlmacen.getText())) {
            etAlmacen.setError("El almacén es obligatorio");
            isValid = false;
        }
        
        return isValid;
    }
    
    // Guardar el artículo y regresar el resultado
    private void guardarArticulo() {
        try {
            // Obtener los valores de los campos
            int sku = Integer.parseInt(etSKU.getText().toString());
            long upc = Long.parseLong(etUPC.getText().toString());
            String descripcion = etDescripcion.getText().toString();
            Double cantidad = Double.parseDouble(etCantidad.getText().toString());
            String almacen = etAlmacen.getText().toString();
            
            // Crear el objeto ArticuloDomain
            // Nota: inventariosArtID, stockTotal, ubicacionID y usuarioID se configurarán en la base de datos
            // Aquí se establecen valores temporales
            ArticuloDomain nuevoArticulo = new ArticuloDomain(
                    0, // inventariosArtID temporal (se asignará en la base de datos)
                    sku,
                    upc,
                    descripcion,
                    cantidad,
                    0, // stockTotal (se actualizará según la base de datos)
                    0, // ubicacionID (se asignará en la base de datos)
                    0, // usuarioID (se obtendrá del usuario actual en ConteoActivity)
                    almacen
            );
            
            // Preparar el intent de resultado
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SKU", sku);
            resultIntent.putExtra("UPC", upc);
            resultIntent.putExtra("DESCRIPCION", descripcion);
            resultIntent.putExtra("CANTIDAD", cantidad);
            resultIntent.putExtra("ALMACEN", almacen);
            
            // Establecer el resultado como OK y enviar el intent
            setResult(Activity.RESULT_OK, resultIntent);
            
            // Cerrar la actividad
            finish();
            
        } catch (NumberFormatException e) {
            Log.e("AGREGAR_ARTICULO", "Error al convertir valores numéricos", e);
            Toast.makeText(this, "Error al procesar los datos. Verifique que los valores numéricos sean correctos", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("AGREGAR_ARTICULO", "Error al guardar el artículo", e);
            Toast.makeText(this, "Error al guardar el artículo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}