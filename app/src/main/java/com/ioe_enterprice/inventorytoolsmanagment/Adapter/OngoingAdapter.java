package com.ioe_enterprice.inventorytoolsmanagment.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.ioe_enterprice.inventorytoolsmanagment.Activity.ConteoActivity;
import com.ioe_enterprice.inventorytoolsmanagment.Domain.OngoingDomain;
import com.ioe_enterprice.inventorytoolsmanagment.R;

import java.util.ArrayList;

public class OngoingAdapter extends RecyclerView.Adapter<OngoingAdapter.Viewholder> {
    private ArrayList<OngoingDomain> items;
    private ArrayList<OngoingDomain> itemsFull;
    private Context context;

    public OngoingAdapter(ArrayList<OngoingDomain> items) {
        this.items = new ArrayList<>(items);
        this.itemsFull = new ArrayList<>(items);
    }

    public void filter(String text) {
        items.clear();
        if (text.isEmpty()) {
            items.addAll(itemsFull);
        } else {
            text = text.toLowerCase();
            for (OngoingDomain item : itemsFull) {
                if (item.getTitle().toLowerCase().contains(text) ||
                        item.getDate().toLowerCase().contains(text)) {
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OngoingAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context= parent.getContext();
        View inflator = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_ongoing,parent,false);
        return new Viewholder(inflator);
    }

    @Override
    public void onBindViewHolder(@NonNull OngoingAdapter.Viewholder holder, int position) {
        OngoingDomain item = items.get(position);

        holder.tittle.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.progressBarPercent.setText(item.getProgressPercent() + "%");

        int drawableResourceId = holder.itemView.getResources()
                .getIdentifier(item.getPicPath(), "drawable", context.getPackageName());

        Glide.with(context)
                .load(drawableResourceId)
                .into(holder.pic);

        holder.progressBar.setProgress(item.getProgressPercent());

        // Cambiar estilos según la posición (ya está en tu código)
        if (position == 0) {
            holder.layout.setBackgroundResource(R.drawable.dark_background);
            holder.tittle.setTextColor(context.getColor(R.color.white));
            holder.date.setTextColor(context.getColor(R.color.white));
            holder.progressTxt.setTextColor(context.getColor(R.color.white));
            holder.progressBarPercent.setTextColor(context.getColor(R.color.white));
            holder.pic.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        } else {
            holder.layout.setBackgroundResource(R.drawable.light_background);
            holder.tittle.setTextColor(context.getColor(R.color.dark_blue));
            holder.date.setTextColor(context.getColor(R.color.dark_blue));
            holder.progressTxt.setTextColor(context.getColor(R.color.dark_blue));
            holder.progressBarPercent.setTextColor(context.getColor(R.color.dark_blue));
            holder.pic.setColorFilter(ContextCompat.getColor(context, R.color.dark_blue), PorterDuff.Mode.SRC_IN);
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_blue)));
        }

        // Agregar evento de clic para abrir activity_conteo
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ConteoActivity.class);
            intent.putExtra("INVENTARIO_FOLIO", item.getTitle()); // Enviar folio
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        TextView tittle,date,progressBarPercent,progressTxt;
        ProgressBar progressBar;
        ImageView pic;
        ConstraintLayout layout;

        public Viewholder (@NonNull View itemView){
            super(itemView);
            layout=itemView.findViewById(R.id.layout_view_holder);
            progressTxt = itemView.findViewById(R.id.progressTxt);
            tittle = itemView.findViewById(R.id.titleTxt);
            date = itemView.findViewById(R.id.dateTxt);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressBarPercent = itemView.findViewById(R.id.percentTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
