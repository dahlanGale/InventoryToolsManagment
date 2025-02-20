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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ioe_enterprice.inventorytoolsmanagment.Domain.ArticuloDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;
import com.ioe_enterprice.inventorytoolsmanagment.Utils.SessionManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConteoAdapter extends RecyclerView.Adapter<ConteoAdapter.ConteoViewHolder> implements Filterable {
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";
    private List<ArticuloDomain> articuloList; // Lista principal (todos los datos)
    private List<ArticuloDomain> articuloListFiltrada; // Lista temporal para mostrar resultados filtrados
    private Context context;

    public ConteoAdapter(List<ArticuloDomain> articuloList, Context context) {
        this.articuloList = articuloList;
        this.articuloListFiltrada = new ArrayList<>(articuloList); // Inicialmente igual a la lista principal
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
        ArticuloDomain item = articuloListFiltrada.get(position); // Usar la lista filtrada para mostrar
        holder.skuTxt.setText("Sku: " + item.getSKU());
        holder.almacenTxt.setText("Almacén: " + item.getAlmacenDescripcion());
        holder.descripcionTxt.setText(item.getDescripcion());
        holder.ctdContadaEdit.setText(String.valueOf(item.getCtdContada()));
        holder.stockTotalTxt.setText("Stock: " + item.getStockTotal());

        // TextWatcher para detectar cambios en la cantidad contada
        if (holder.ctdContadaEdit.getTag() instanceof TextWatcher) {
            holder.ctdContadaEdit.removeTextChangedListener((TextWatcher) holder.ctdContadaEdit.getTag());
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    double nuevaCantidad = Double.parseDouble(s.toString());
                    if (item.getCtdContada() != nuevaCantidad) {
                        item.setCtdContada(nuevaCantidad);
                        updateCtdContadaEnBD(item.getInventariosArtID(), nuevaCantidad);
                    }
                }
            }
        };

        holder.ctdContadaEdit.addTextChangedListener(watcher);
        holder.ctdContadaEdit.setTag(watcher);

        // Botón para fijar el stock total
        holder.btnSetStock.setOnClickListener(v -> {
            double stockTotal = item.getStockTotal();
            holder.ctdContadaEdit.setText(String.valueOf(stockTotal));
            item.setCtdContada(stockTotal);
            updateCtdContadaEnBD(item.getInventariosArtID(), stockTotal);
        });
    }

    @Override
    public int getItemCount() {
        return articuloListFiltrada.size(); // Tamaño de la lista filtrada
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<ArticuloDomain> listaFiltrada = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    listaFiltrada.addAll(articuloList); // Restaurar todos los elementos
                } else {
                    String filtroPatron = constraint.toString().toLowerCase().trim();
                    for (ArticuloDomain articulo : articuloList) {
                        if (articulo.getDescripcion().toLowerCase().contains(filtroPatron) ||
                                String.valueOf(articulo.getSKU()).contains(filtroPatron) ||
                                String.valueOf(articulo.getUPC()).contains(filtroPatron)) {
                            listaFiltrada.add(articulo);
                        }
                    }
                }

                results.values = listaFiltrada;
                results.count = listaFiltrada.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                articuloListFiltrada.clear();
                articuloListFiltrada.addAll((List<ArticuloDomain>) results.values);
                notifyDataSetChanged(); // Notificar al RecyclerView que los datos han cambiado
            }
        };
    }

    // Método para actualizar la lista principal desde la actividad
    public void actualizarLista(List<ArticuloDomain> nuevaLista) {
        articuloList.clear();
        articuloList.addAll(nuevaLista);
        articuloListFiltrada.clear();
        articuloListFiltrada.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    // ViewHolder
    public static class ConteoViewHolder extends RecyclerView.ViewHolder {
        TextView skuTxt, almacenTxt, descripcionTxt, stockTotalTxt;
        EditText ctdContadaEdit;
        Button btnSetStock;

        public ConteoViewHolder(@NonNull View itemView) {
            super(itemView);
            skuTxt = itemView.findViewById(R.id.skuTxt);
            almacenTxt = itemView.findViewById(R.id.almacenTxt);
            descripcionTxt = itemView.findViewById(R.id.descripcionTxt);
            stockTotalTxt = itemView.findViewById(R.id.stockTotalTxt);
            ctdContadaEdit = itemView.findViewById(R.id.ctdContadaEdit);
            btnSetStock = itemView.findViewById(R.id.btnSetStock);
        }
    }

    // Método para actualizar la cantidad contada en la base de datos
    private void updateCtdContadaEnBD(int inventariosArtID, double nuevaCantidad) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                SessionManager sessionManager = new SessionManager(context);
                int usuarioID = sessionManager.getUserID();

                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE dtInventariosArticulos SET ctdContada = ?, usuarioID = ?, fechaConteo = CURRENT_TIMESTAMP WHERE inventariosArtID = ?");
                statement.setDouble(1, nuevaCantidad);
                statement.setInt(2, usuarioID);
                statement.setInt(3, inventariosArtID);

                statement.executeUpdate();
                statement.close();
                connection.close();
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al actualizar ctdContada", e);
            }
        });
        executor.shutdown();
    }
}