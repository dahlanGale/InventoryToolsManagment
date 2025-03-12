package com.ioe_enterprice.inventorytoolsmanagment.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ioe_enterprice.inventorytoolsmanagment.Adapter.InventariosAdapter;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.OngoingDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;
import com.ioe_enterprice.inventorytoolsmanagment.Utils.SessionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText etBuscar;
    private RecyclerView recyclerView;
    private InventariosAdapter adapter;
    private List<OngoingDomain> inventoryList; // Lista completa de 10 registros
    private TextView tvShowMore;
    private boolean isExpanded = false;
    private SessionManager sessionManager;
    private TextView tvWelcomeUser;
    private TextView tvWelcomeTime;
    private LinearLayout logoutBtm;

    // Datos de conexión a la base de datos
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar SessionManager
        sessionManager = new SessionManager(this);

        // Inicializar TextViews de bienvenida
        tvWelcomeUser = findViewById(R.id.tv_WelcomeUser);
        tvWelcomeTime = findViewById(R.id.tv_WelcomeTime);
        
        // Configurar los mensajes de bienvenida
        setWelcomeMessages();

        // Inicializar y configurar el botón de logout
        logoutBtm = findViewById(R.id.logoutBtm);
        logoutBtm.setOnClickListener(v -> {
            logout();
        });

        recyclerView = findViewById(R.id.viewOngoing);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        inventoryList = new ArrayList<>();
        adapter = new InventariosAdapter(inventoryList);
        recyclerView.setAdapter(adapter);

        etBuscar = findViewById(R.id.et_Buscar);
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tvShowMore = findViewById(R.id.tv_ShowMore);
        tvShowMore.setOnClickListener(view -> toggleListSize());

        loadActiveInventories();

    }

    private void setWelcomeMessages() {
        // Obtener el nombre del usuario
        String userName = sessionManager.getUserName();
        
        // Establecer el mensaje de bienvenida con el nombre del usuario
        tvWelcomeUser.setText("Hola " + userName);
        
        // Obtener la hora actual
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Determinar el saludo según la hora del día
        String greeting;
        if (hourOfDay >= 0 && hourOfDay < 12) {
            greeting = "¡Buenos días!";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "¡Buenas tardes!";
        } else {
            greeting = "¡Buenas noches!";
        }
        
        // Establecer el saludo según la hora
        tvWelcomeTime.setText(greeting);
    }

    private void toggleListSize() {
        isExpanded = !isExpanded;
        int limit = isExpanded ? 10 : 4; // Determina si se muestran 4 o 10 elementos
        adapter.updateList(limit); // Llama a `updateList(int limit)` en el adapter

        tvShowMore.setText(isExpanded ? "Ver menos" : "Ver más");
    }

    private void loadActiveInventories() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                // Conexión a la base de datos
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement statement = connection.createStatement();

                // Carga los registros desde la base de datos
                String query = "SELECT TOP 10 " +
                        "cb.inventarioFolio, " +
                        "CONVERT(VARCHAR, cb.fechaInventario, 120) AS fechaInventario, " +
                        "CASE WHEN cb.tipoInventario = 'ARTICULO' THEN 'ongoing3' ELSE 'ongoing4' END AS picPath, " +
                        "COUNT(CASE WHEN dt.ctdContada > -1 THEN 1 END) AS counted, " +
                        "COUNT(*) AS total, " +
                        "ROUND(CAST(COUNT(CASE WHEN dt.ctdContada > -1 THEN 1 END) AS FLOAT) / COUNT(*) * 100, 0) AS progressPercent " +
                        "FROM cbInventarios cb " +
                        "LEFT JOIN dtInventariosArticulos dt ON cb.inventarioDocID = dt.inventarioDocID " +
                        "WHERE cb.estatus = 'ABIERTO' " +
                        "GROUP BY cb.inventarioFolio, cb.fechaInventario, cb.tipoInventario " +
                        "ORDER BY cb.fechaInventario DESC";


                ResultSet resultSet = statement.executeQuery(query);
                List<OngoingDomain> tempList = new ArrayList<>();

                while (resultSet.next()) {
                    tempList.add(new OngoingDomain(
                            resultSet.getString("inventarioFolio"),
                            resultSet.getString("fechaInventario"),
                            resultSet.getInt("progressPercent"),
                            resultSet.getString("picPath"),
                            resultSet.getInt("counted"),
                            resultSet.getInt("total")
                    ));
                }

                resultSet.close();
                statement.close();
                connection.close();

                // Actualizar UI en el hilo principal
                runOnUiThread(() -> {
                    inventoryList.clear();
                    inventoryList.addAll(tempList);
                    adapter.updateFullList(); // Mostrar automáticamente los primeros 4 elementos
                });

            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al conectar a la base de datos", e);
            }
        });
        executor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActiveInventories();  // Recarga los inventarios al volver a MainActivity
        setWelcomeMessages();     // Actualiza los mensajes de bienvenida
    }

    /**
     * Método para cerrar la sesión del usuario
     */
    private void logout() {
        // Limpiar la sesión
        sessionManager.clearSession();
        
        // Redirigir al usuario a la pantalla de login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Eliminar actividades anteriores de la pila
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}