package com.tu.tuchati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.ShopDetailsActivity;
import com.tu.tuchati.Activities.privateChatActivity;
import com.tu.tuchati.Models.Shops;
import com.tu.tuchati.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.HolderShop>{

    private Context context;
    public ArrayList<Shops> shopList;

    public ShopAdapter(Context context, ArrayList<Shops> shopList) {
        this.context = context;
        this.shopList = shopList;


    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {
        //get data
        Shops shopModel = shopList.get(position);
        String accountType = shopModel.getAccountType();
        String address = shopModel.getAddress();
        String city = shopModel.getCity();
        String country = shopModel.getCountry();
        Long latitude = shopModel.getLatitude();
        Long longitude = shopModel.getLongitude();
        String uid = shopModel.getUid();
        String profileImage = shopModel.getShopimage();
        String shopemail = shopModel.getShopemail();
        String shopName = shopModel.getShopName();
        String shopphone = shopModel.getShopphone();
        String username = shopModel.getUsername();

        loadReviews(shopModel, holder);

        //set data
        holder.shopName.setText(shopName);
        holder.phone.setText(shopphone);
        holder.address.setText(address);

        holder.callBtn.setOnClickListener(v -> {
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopphone))));
            Toast.makeText(context, ""+shopphone, Toast.LENGTH_SHORT).show();
        });
        holder.MessageBtn.setOnClickListener(v -> {
            Intent chatintent =  new Intent(context, privateChatActivity.class);
            chatintent.putExtra("hisUid",uid);
            chatintent.putExtra("username",username);
            context.startActivity(chatintent);
        });


        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.store_gray).into(holder.shop);
        }
        catch (Exception e){
            holder.shop.setImageResource(R.drawable.store_gray);
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShopDetailsActivity.class);
            intent.putExtra("shopUid", uid);
            context.startActivity(intent);
        });
    }
    private float ratingSum=0;
    private void loadReviews(Shops shopModel, HolderShop holder) {
        String shopUid = shopModel.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear data before adding data
                        ratingSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratingSum = ratingSum+rating;

                        }
                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    class HolderShop extends RecyclerView.ViewHolder{
        ImageView shop;
        TextView shopName,phone,address;
        private RatingBar ratingBar;
        private ImageButton callBtn,MessageBtn;
        public HolderShop(@NonNull View itemView) {
            super(itemView);

            shop = itemView.findViewById(R.id.shopIv);
            shopName = itemView.findViewById(R.id.shopNameTv);
            phone = itemView.findViewById(R.id.phoneTv);
            address = itemView.findViewById(R.id.addressTv);
            ratingBar=itemView.findViewById(R.id.shopratings);
            callBtn=itemView.findViewById(R.id.callBtn);
            MessageBtn=itemView.findViewById(R.id.MessageBtn);


        }
    }
}
