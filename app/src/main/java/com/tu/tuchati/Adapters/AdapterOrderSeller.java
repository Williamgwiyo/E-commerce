package com.tu.tuchati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.SellerOrderDetailsActivity;
import com.tu.tuchati.Activities.privateChatActivity;
import com.tu.tuchati.FilterOrderSeller;
import com.tu.tuchati.Models.ModelOrderSeller;
import com.tu.tuchati.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderSeller extends RecyclerView.Adapter<AdapterOrderSeller.HolderOrderSeller> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderSeller> modelOrderSellerArrayList,filterList;
    private FilterOrderSeller filter;


    public AdapterOrderSeller(Context context, ArrayList<ModelOrderSeller> modelOrderSellerArrayList) {
        this.context = context;
        this.modelOrderSellerArrayList = modelOrderSellerArrayList;
        this.filterList = modelOrderSellerArrayList;
    }

    @NonNull
    @Override
    public HolderOrderSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent,false);
        return new HolderOrderSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderSeller holder, int position) {
        ModelOrderSeller modelOrderSeller = modelOrderSellerArrayList.get(position);
        String orderId = modelOrderSeller.getOrderId();
        String orderBy = modelOrderSeller.getOrderBy();
        String orderCost = modelOrderSeller.getOrderCost();
        String orderStatus = modelOrderSeller.getOrderStatus();
        String orderTime = modelOrderSeller.getOrderTime();
        String orderTo = modelOrderSeller.getOrderTo();

        loadUserInfo(modelOrderSeller, holder);

        //set data
        holder.amountTv.setText("Amount: " +orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("Order ID: " +orderId);

        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }
        else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        //convert time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.orderDateTv.setText(formatedDate);

       holder.itemView.setOnClickListener(v -> {
           //order details
           Intent intent = new Intent(context, SellerOrderDetailsActivity.class);
           intent.putExtra("orderId", orderId);
           intent.putExtra("orderBy", orderBy);
           context.startActivity(intent);
       });
    }

    private void loadUserInfo(ModelOrderSeller modelOrderSeller, HolderOrderSeller holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.keepSynced(true);
        ref.child(modelOrderSeller.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("username").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        holder.emailTv.setText(email);



                        holder.MessageBtn.setOnClickListener(v -> {
                            Intent chatintent =  new Intent(context, privateChatActivity.class);
                            chatintent.putExtra("hisUid",uid);
                            chatintent.putExtra("username",name);
                            context.startActivity(chatintent);
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelOrderSellerArrayList.size();
    }

    @Override
    public Filter getFilter() {
       if (filter == null){
           filter = new FilterOrderSeller(this,filterList);
       }
       return filter;
    }

    class HolderOrderSeller extends RecyclerView.ViewHolder{

        private TextView orderIdTv,emailTv,amountTv,statusTv,orderDateTv,MessageBtn;
        private ImageView nextIv;
        public HolderOrderSeller(@NonNull View itemView) {
            super(itemView);

            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            nextIv = itemView.findViewById(R.id.nextIv);
            MessageBtn = itemView.findViewById(R.id.MessageBtn);
        }
    }
}
