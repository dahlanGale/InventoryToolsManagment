package com.ioe_enterprice.inventorytoolsmanagment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ProgressBar;
import android.widget.ImageView;

import com.ioe_enterprice.inventorytoolsmanagment.Domain.OngoingDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;

import java.util.ArrayList;
import java.util.List;

public class InventariosAdapter extends RecyclerView.Adapter<InventariosAdapter.InventoryViewHolder> {
    private final List<OngoingDomain> inventoryList; // Lista completa
    private List<OngoingDomain> filteredList; // Lista mostrada en RecyclerView

    public InventariosAdapter(List<OngoingDomain> inventoryList) {
        this.inventoryList = inventoryList;
        this.filteredList = new ArrayList<>(inventoryList.subList(0, Math.min(4, inventoryList.size())));
    }

    // Método para actualizar la lista con un límite de elementos
    public void updateList(int limit) {
        filteredList.clear();
        filteredList.addAll(inventoryList.subList(0, Math.min(limit, inventoryList.size())));
        notifyDataSetChanged();
    }

    // Método para filtrar los elementos según el texto ingresado en etBuscar
    public void filter(String text) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(inventoryList.subList(0, Math.min(4, inventoryList.size()))); // Mostrar 4 por defecto
        } else {
            text = text.toLowerCase();
            for (OngoingDomain item : inventoryList) {
                if (item.getTitle().toLowerCase().contains(text) ||
                        item.getDate().toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_ongoing, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        OngoingDomain item = filteredList.get(position);
        holder.dateTxt.setText(item.getDate());
        holder.titleTxt.setText(item.getTitle());
        holder.progressTxt.setText("Progreso");
        holder.progressBar.setProgress(item.getProgressPercent());
        holder.percentTxt.setText(item.getProgressPercent() + "%");

        int imageResId = holder.itemView.getContext().getResources().getIdentifier(
                item.getPicPath(), "drawable", holder.itemView.getContext().getPackageName());
        if (imageResId != 0) {
            holder.pic.setImageResource(imageResId);
        } else {
            holder.pic.setImageResource(R.drawable.ongoing1);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView dateTxt, titleTxt, progressTxt, percentTxt;
        ProgressBar progressBar;
        ImageView pic;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTxt = itemView.findViewById(R.id.dateTxt);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            progressTxt = itemView.findViewById(R.id.progressTxt);
            percentTxt = itemView.findViewById(R.id.percentTxt);
            progressBar = itemView.findViewById(R.id.progressBar);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
