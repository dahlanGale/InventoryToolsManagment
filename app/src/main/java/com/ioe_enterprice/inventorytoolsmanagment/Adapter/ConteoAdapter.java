package com.ioe_enterprice.inventorytoolsmanagment.Adapter;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ioe_enterprice.inventorytoolsmanagment.Domain.ArticuloDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;
import com.ioe_enterprice.inventorytoolsmanagment.Utils.SessionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConteoAdapter extends RecyclerView.Adapter<ConteoAdapter.ConteoViewHolder> {
    private List<ArticuloDomain> articuloList;
    private Context context;

    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

    public ConteoAdapter(List<ArticuloDomain> articuloList, Context context) {
        this.articuloList = articuloList;
        this.context = context;
    }

    @NonNull
    @Override
    public ConteoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_conteo, parent, false);
        return new ConteoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConteoViewHolder holder, int position) {
        ArticuloDomain item = articuloList.get(position);
        holder.skuTxt.setText("Sku: " + item.getSKU());
        holder.almacenTxt.setText("Almacén: " + item.getAlmacenDescripcion());
        holder.descripcionTxt.setText(item.getDescripcion());
        holder.ctdContadaEdit.setText(String.valueOf(item.getCtdContada()));
        holder.stockTotalTxt.setText("Stock: " + item.getStockTotal());

        // Variable para evitar doble actualización
        final boolean[] isUpdating = {false};

        // Elimina cualquier TextWatcher previo para evitar duplicados
        if (holder.ctdContadaEdit.getTag() instanceof TextWatcher) {
            holder.ctdContadaEdit.removeTextChangedListener((TextWatcher) holder.ctdContadaEdit.getTag());
        }

        //TextWatcher para detectar cambios manuales en el EditText
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating[0] && !s.toString().isEmpty()) {  // Evita actualizar cuando el botón cambia el valor
                    double nuevaCantidad = Double.parseDouble(s.toString());
                    if (item.getCtdContada() != nuevaCantidad) {  // Evita llamadas innecesarias
                        item.setCtdContada(nuevaCantidad);
                        updateCtdContadaEnBD(item.getInventariosArtID(), nuevaCantidad);
                    }
                }
            }
        };

        // Agregar el TextWatcher y guardarlo en el tag
        holder.ctdContadaEdit.addTextChangedListener(watcher);
        holder.ctdContadaEdit.setTag(watcher);

        // Botón para fijar el stock total
        holder.btnSetStock.setOnClickListener(v -> {
            double stockTotal = item.getStockTotal();
            if (item.getCtdContada() != stockTotal) {
                isUpdating[0] = true; // 🔹 Desactiva temporalmente el TextWatcher
                holder.ctdContadaEdit.setText(String.valueOf(stockTotal));
                item.setCtdContada(stockTotal);
                updateCtdContadaEnBD(item.getInventariosArtID(), stockTotal);
                isUpdating[0] = false; // 🔹 Reactiva el TextWatcher
            }
        });
    }

    @Override
    public int getItemCount() {
        return articuloList.size();
    }

    public static class ConteoViewHolder extends RecyclerView.ViewHolder {
        TextView skuTxt, descripcionTxt, stockTotalTxt, almacenTxt;
        EditText ctdContadaEdit;
        Button btnSetStock;

        public ConteoViewHolder(@NonNull View itemView) {
            super(itemView);
            skuTxt = itemView.findViewById(R.id.skuTxt);
            descripcionTxt = itemView.findViewById(R.id.descripcionTxt);
            stockTotalTxt = itemView.findViewById(R.id.stockTotalTxt);
            ctdContadaEdit = itemView.findViewById(R.id.ctdContadaEdit);
            btnSetStock = itemView.findViewById(R.id.btnSetStock);
        }
    }

    // 🔹 Método para actualizar la cantidad contada y la fecha en la base de datos
    private void updateCtdContadaEnBD(int inventariosArtID, double nuevaCantidad) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                // Obtener usuarioID desde caché
                SessionManager sessionManager = new SessionManager(context);
                int usuarioID = sessionManager.getUserID();

                //Consulta SQL para actualizar ctdContada, usuarioID y fechaConteo
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE dtInventariosArticulos " +
                                "SET ctdContada = ?, usuarioID = ?, fechaConteo = CURRENT_TIMESTAMP " +
                                "WHERE inventariosArtID = ?");
                statement.setDouble(1, nuevaCantidad);
                statement.setInt(2, usuarioID);
                statement.setInt(3, inventariosArtID);

                int rowsUpdated = statement.executeUpdate();
                statement.close();
                connection.close();

                Log.d("DB_UPDATE", "Cantidad actualizada para inventariosArtID: " + inventariosArtID +
                        ", Filas afectadas: " + rowsUpdated + ", Fecha: NOW()");
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al actualizar ctdContada", e);
            }
        });
        executor.shutdown();
    }
}
