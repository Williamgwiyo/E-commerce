package com.tu.tuchati.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Models.ModelOrderedItem;
import com.tu.tuchati.R;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem>{

    private Context context;
    private ArrayList<ModelOrderedItem> modelOrderedItemArrayList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> modelOrderedItemArrayList) {
        this.context = context;
        this.modelOrderedItemArrayList = modelOrderedItemArrayList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditem, parent,false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {
        //get data
        ModelOrderedItem modelOrderedItem = modelOrderedItemArrayList.get(position);
        String getpId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        //set data

        holder.itemTitleTv.setText(name);
        holder.itemPriceEachTv.setText("Ksh "+price);
        holder.itemPriceTv.setText("Ksh "+cost);
        holder.itemQuantityTv.setText("[" +quantity+"]");

    }

    @Override
    public int getItemCount() {
        return modelOrderedItemArrayList.size();
    }

    class HolderOrderedItem extends RecyclerView.ViewHolder{

        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv;
        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);

        }
    }
}
