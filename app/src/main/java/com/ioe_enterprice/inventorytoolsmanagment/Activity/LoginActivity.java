package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ioe_enterprice.inventorytoolsmanagment.R;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvError;

    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etUsername = findViewById(R.id.et_user);  // Cambia a etUsername
        etPassword = findViewById(R.id.et_Password);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);


        // Configurar el evento de clic del botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener los valores de los campos
                String username = etUsername.getText().toString().trim();  // Cambia a username
                String password = etPassword.getText().toString().trim();

                // Validar las credenciales en un hilo secundario
                validateCredentials(username, password);  // Cambia a username
            }
        });
    }

    private void validateCredentials(String username, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try {
                // Conexión a la base de datos
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                // Consulta SQL para validar las credenciales
                String query = "SELECT usuarioID FROM tbUsuarios WHERE usuario = ? AND passwordUser = ?";  // Cambia a username
                statement = connection.prepareStatement(query);
                statement.setString(1, username);  // Cambia a username
                statement.setString(2, password);

                // Ejecutar la consulta
                resultSet = statement.executeQuery();

                // Verificar si se encontró un usuario con las credenciales proporcionadas
                if (resultSet.next()) {
                    // Si las credenciales son válidas, redirigir a la actividad principal
                    runOnUiThread(() -> {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Cerrar la actividad de inicio de sesión
                    });
                } else {
                    // Mostrar mensaje de error en el hilo principal
                    runOnUiThread(() -> {
                        tvError.setText("Nombre de usuario o contraseña incorrectos");  // Cambia el mensaje de error
                        tvError.setVisibility(View.VISIBLE);
                    });
                }

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al conectar a la base de datos", e);
                runOnUiThread(() -> {
                    tvError.setText("Error al conectar a la base de datos");
                    tvError.setVisibility(View.VISIBLE);
                });
            } finally {
                // Cerrar recursos
                try {
                    if (resultSet != null) resultSet.close();
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                } catch (Exception e) {
                    Log.e("DB_ERROR", "Error al cerrar la conexión", e);
                }
            }
        });

        try {
            // Esperar máximo 5 segundos
            future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.e("LOGIN_ERROR", "Timeout al validar las credenciales", e);
            future.cancel(true); // Cancelar la tarea si excede el tiempo
            runOnUiThread(() -> {
                tvError.setText("Tiempo de espera agotado. Intente nuevamente.");
                tvError.setVisibility(View.VISIBLE);
            });
        } catch (Exception e) {
            Log.e("LOGIN_ERROR", "Error en la ejecución", e);
            runOnUiThread(() -> {
                tvError.setText("Error al iniciar sesión. Intente nuevamente.");
                tvError.setVisibility(View.VISIBLE);
            });
        } finally {
            executor.shutdown();
        }
    }
}