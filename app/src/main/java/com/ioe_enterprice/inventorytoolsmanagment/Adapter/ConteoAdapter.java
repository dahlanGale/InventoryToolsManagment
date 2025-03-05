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
import android.widget.Toast;

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
    private List<ArticuloDomain> articuloList; // Lista principal (todos los datos)
    private List<ArticuloDomain> articuloListFiltrada; // Lista temporal para mostrar resultados filtrados
    private Context context;
    private static final String DB_URL = "jdbc:jtds:sqlserver://192.168.10.246:1433/IOE_Business";
    private static final String DB_USER = "IOEMaster";
    private static final String DB_PASSWORD = "Master.2024";

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
        ArticuloDomain item = articuloListFiltrada.get(position);
        holder.inventariosArtID = item.getInventariosArtID();
        
        holder.skuTxt.setText("Sku: " + item.getSKU());
        holder.upcTxt.setText("UPC: " + item.getUPC());
        holder.almacenTxt.setText("Almacén: " + item.getAlmacenDescripcion());
        holder.descripcionTxt.setText(item.getDescripcion());
        
        // Manejar valores nulos en ctdContada
        if (item.getCtdContada() != null) {
            holder.ctdContadaEdit.setText(String.valueOf(item.getCtdContada()));
        } else {
            holder.ctdContadaEdit.setText("");
        }
        
        holder.stockTotalTxt.setText("Stock: " + item.getStockTotal());

        // Limpiar el TextWatcher anterior para evitar duplicados
        if (holder.ctdContadaEdit.getTag() instanceof TextWatcher) {
            holder.ctdContadaEdit.removeTextChangedListener((TextWatcher) holder.ctdContadaEdit.getTag());
        }

        // Crear un nuevo TextWatcher
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                // Verificar si el texto contiene espacios y eliminarlos
                String texto = s.toString();
                if (texto.contains(" ")) {
                    texto = texto.replace(" ", "");
                    holder.ctdContadaEdit.setText(texto);
                    holder.ctdContadaEdit.setSelection(texto.length());
                    return;
                }

                try {
                    if (texto.isEmpty()) {
                        // Si el campo está vacío, permitir que quede vacío (null)
                        Log.d("CONTEO_DEBUG", "Campo vacío detectado para inventariosArtID: " + holder.inventariosArtID);
                        
                        // Buscar el artículo en la lista filtrada por su ID único
                        for (ArticuloDomain articulo : articuloListFiltrada) {
                            if (articulo.getInventariosArtID() == holder.inventariosArtID) {
                                // Asegurar que se actualiza correctamente a null
                                articulo.setCtdContada(null);
                                // Imprimir el valor después de actualizar
                                Log.d("CONTEO_DEBUG", "Artículo actualizado a null, valor actual: " + articulo.getCtdContada());
                                updateCtdContadaEnBD(articulo.getInventariosArtID(), null);
                                break;
                            }
                        }
                    } else {
                        // Convertir a double y validar que no sea negativo
                        double nuevaCantidad = Double.parseDouble(texto);
                        if (nuevaCantidad < 0) {
                            nuevaCantidad = 0;
                            holder.ctdContadaEdit.setText("0");
                            holder.ctdContadaEdit.setSelection(1);
                            // Mostrar mensaje al usuario
                            Toast.makeText(context, "Valor inválido, no puede ingresar negativos", Toast.LENGTH_SHORT).show();
                        }

                        // Buscar el artículo en la lista filtrada por su ID único
                        for (ArticuloDomain articulo : articuloListFiltrada) {
                            if (articulo.getInventariosArtID() == holder.inventariosArtID) {
                                Double valorActual = articulo.getCtdContada();
                                // Comparar valores considerando nulos
                                if (valorActual == null || valorActual != nuevaCantidad) {
                                    articulo.setCtdContada(nuevaCantidad);
                                    updateCtdContadaEnBD(articulo.getInventariosArtID(), nuevaCantidad);
                                }
                                break;
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // Si hay un error al convertir (formato inválido), establecer campo vacío y actualizar a null
                    holder.ctdContadaEdit.setText("");
                    
                    // Buscar el artículo en la lista filtrada por su ID único
                    for (ArticuloDomain articulo : articuloListFiltrada) {
                        if (articulo.getInventariosArtID() == holder.inventariosArtID) {
                            articulo.setCtdContada(null);
                            updateCtdContadaEnBD(articulo.getInventariosArtID(), null);
                            break;
                        }
                    }
                    Log.e("CONTEO_ERROR", "Error al convertir cantidad: " + e.getMessage());
                }
            }
        };

        // Asignar el TextWatcher al EditText
        holder.ctdContadaEdit.addTextChangedListener(watcher);
        holder.ctdContadaEdit.setTag(watcher);

        // Botón para fijar el stock total
        holder.btnSetStock.setOnClickListener(v -> {
            try {
                double stockTotal = item.getStockTotal();
                if (stockTotal < 0) {
                    stockTotal = 0; // Asegurarse de que stockTotal no sea negativo
                }
                holder.ctdContadaEdit.setText(String.valueOf(stockTotal));
                holder.ctdContadaEdit.setSelection(holder.ctdContadaEdit.getText().length());

                // Buscar el artículo en la lista filtrada por su ID único
                for (ArticuloDomain articulo : articuloListFiltrada) {
                    if (articulo.getInventariosArtID() == holder.inventariosArtID) {
                        articulo.setCtdContada(stockTotal);
                        updateCtdContadaEnBD(articulo.getInventariosArtID(), stockTotal);
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e("CONTEO_ERROR", "Error al fijar stock: " + e.getMessage());
            }
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
                    listaFiltrada.addAll(articuloList); // Usar la lista principal, no la filtrada
                } else {
                    String filtroPatron = constraint.toString().toLowerCase().trim();
                    for (ArticuloDomain articulo : articuloList) { // Filtrar desde la lista principal
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
                if (results.values != null) {
                    articuloListFiltrada.addAll((List<ArticuloDomain>) results.values);
                }
                notifyDataSetChanged(); // Notificar al RecyclerView que los datos han cambiado
            }
        };
    }

    // Método para actualizar la lista principal desde la actividad
    public void actualizarLista(List<ArticuloDomain> nuevaLista) {
        articuloList = new ArrayList<>(nuevaLista);
        articuloListFiltrada.clear();
        articuloListFiltrada.addAll(nuevaLista);
        notifyDataSetChanged();
    }

    // ViewHolder
    public static class ConteoViewHolder extends RecyclerView.ViewHolder {
        TextView skuTxt, upcTxt, almacenTxt, descripcionTxt, stockTotalTxt;
        EditText ctdContadaEdit;
        Button btnSetStock;
        int inventariosArtID; // Variable para almacenar el ID único

        public ConteoViewHolder(@NonNull View itemView) {
            super(itemView);
            skuTxt = itemView.findViewById(R.id.skuTxt);
            upcTxt = itemView.findViewById(R.id.upcTxt);
            almacenTxt = itemView.findViewById(R.id.almacenTxt);
            descripcionTxt = itemView.findViewById(R.id.descripcionTxt);
            stockTotalTxt = itemView.findViewById(R.id.stockTotalTxt);
            ctdContadaEdit = itemView.findViewById(R.id.ctdContadaEdit);
            btnSetStock = itemView.findViewById(R.id.btnSetStock);
        }
    }

    // Método para actualizar la cantidad contada en la base de datos
    private void updateCtdContadaEnBD(int inventariosArtID, Double nuevaCantidad) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Class.forName("net.sourceforge.jtds.jdbc.Driver");
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                SessionManager sessionManager = new SessionManager(context);
                int usuarioID = sessionManager.getUserID();

                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE dtInventariosArticulos SET ctdContada = ?, usuarioID = ?, fechaConteo = CURRENT_TIMESTAMP WHERE inventariosArtID = ?");
                if (nuevaCantidad == null) {
                    statement.setNull(1, java.sql.Types.DOUBLE);
                    // Log para verificar que se está enviando NULL a la base de datos
                    Log.d("DB_UPDATE", "Enviando NULL a la base de datos para inventariosArtID: " + inventariosArtID);
                } else {
                    statement.setDouble(1, nuevaCantidad);
                    // Log para verificar el valor que se está enviando
                    Log.d("DB_UPDATE", "Enviando valor: " + nuevaCantidad + " a la base de datos para inventariosArtID: " + inventariosArtID);
                }
                statement.setInt(2, usuarioID);
                statement.setInt(3, inventariosArtID);

                int rowsAffected = statement.executeUpdate();
                // Log para verificar que la actualización se completó correctamente
                Log.d("DB_UPDATE", "Filas afectadas: " + rowsAffected);
                
                statement.close();
                connection.close();
            } catch (Exception e) {
                Log.e("DB_ERROR", "Error al actualizar ctdContada", e);
            }
        });
        executor.shutdown();
    }
}