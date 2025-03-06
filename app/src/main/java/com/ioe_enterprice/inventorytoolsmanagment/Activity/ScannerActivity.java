package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.zxing.Result;
import com.ioe_enterprice.inventorytoolsmanagment.R;

public class ScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        
        // Configuración del escáner
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String codigo = result.getText();
                        Toast.makeText(ScannerActivity.this, "Código escaneado: " + codigo, Toast.LENGTH_SHORT).show();
                        
                        // Devolver el resultado a la actividad que lo solicitó
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("CODIGO_BARRAS", codigo);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });
            }
        });
        
        // Configurar botón de cerrar
        ExtendedFloatingActionButton btnCerrar = findViewById(R.id.btn_close_scanner);
        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
        
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}
