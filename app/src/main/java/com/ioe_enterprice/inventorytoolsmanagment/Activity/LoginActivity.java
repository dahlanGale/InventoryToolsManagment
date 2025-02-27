package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ioe_enterprice.inventorytoolsmanagment.R;
import com.ioe_enterprice.inventorytoolsmanagment.Utils.SessionManager;

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

    private SessionManager sessionManager;
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvError;
    private CheckBox cbRememberMe;

    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar vistas
        etUsername = findViewById(R.id.et_user);
        etPassword = findViewById(R.id.et_Password);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        sessionManager = new SessionManager(this);

        // Verificar si el usuario ya ha iniciado sesión y si la casilla "Recuérdame" está activada
        if (sessionManager.isRememberMe() && sessionManager.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cerrar la actividad de inicio de sesión
        }

        // Configurar el evento de clic del botón de inicio de sesión
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // Validar las credenciales en un hilo secundario
                validateCredentials(username, password);
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
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                String query = "SELECT usuarioID FROM tbUsuarios WHERE usuario = ? AND passwordUser = ?";
                statement = connection.prepareStatement(query);
                statement.setString(1, username);
                statement.setString(2, password);

                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    int usuarioID = resultSet.getInt("usuarioID");

                    // Guardar el ID del usuario y el estado de "Recuérdame"
                    sessionManager.saveUserID(usuarioID);
                    sessionManager.setRememberMe(cbRememberMe.isChecked());
                    sessionManager.setLoggedIn(true);

                    runOnUiThread(() -> {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    runOnUiThread(() -> {
                        tvError.setText("Nombre de usuario o contraseña incorrectos");
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
            future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Log.e("LOGIN_ERROR", "Timeout al validar las credenciales", e);
            future.cancel(true);
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