package com.ioe_enterprice.inventorytoolsmanagment.Adapter;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ioe_enterprice.inventorytoolsmanagment.Domain.ArticuloDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;

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
        holder.skuTxt.setText("SKU: " + item.getSKU());
        holder.descripcionTxt.setText(item.getDescripcion());
        holder.ctdContadaEdit.setText(String.valueOf(item.getCtdContada()));

        // 📌 Agregar TextWatcher para detectar cambios en "ctdContada"
        holder.ctdContadaEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String newText = s.toString();
                if (!newText.isEmpty()) {
                    double nuevaCantidad = Double.parseDouble(newText);
                    item.setCtdContada(nuevaCantidad);
                    updateCtdContadaEnBD(item.getSKU(), nuevaCantidad);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return articuloList.size();
    }

    public static class ConteoViewHolder extends RecyclerView.ViewHolder {
        TextView skuTxt, descripcionTxt;
        EditText ctdContadaEdit;

        public ConteoViewHolder(@NonNull View itemView) {
            super(itemView);
            skuTxt = itemView.findViewById(R.id.skuTxt);
            descripcionTxt = itemView.findViewById(R.id.descripcionTxt);
            ctdContadaEdit = itemView.findViewById(R.id.ctdContadaEdit);
        }
    }

    // Método para actualizar la base de datos en segundo plano
    private void updateCtdContadaEnBD(int sku, double nuevaCantidad) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE dtInventariosArticulos SET ctdContada = ? WHERE SKU = ?");
                statement.setDouble(1, nuevaCantidad);
                statement.setInt(2, sku);
                statement.executeUpdate();
                statement.close();
                connection.close();
                Log.d("DB_UPDATE", "Cantidad actualizada para SKU: " + sku);
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al actualizar ctdContada", e);
            }
        });
        executor.shutdown();
    }
}
