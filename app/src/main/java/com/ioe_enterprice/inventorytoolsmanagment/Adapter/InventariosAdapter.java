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

import java.util.List;

public class InventariosAdapter extends RecyclerView.Adapter<InventariosAdapter.InventoryViewHolder> {
    private final List<OngoingDomain> inventoryList;

    public InventariosAdapter(List<OngoingDomain> inventoryList) {
        this.inventoryList = inventoryList;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_ongoing, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        OngoingDomain item = inventoryList.get(position);

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
        return inventoryList.size();
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