package com.tu.tuchati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.ShopDetailsActivity;
import com.tu.tuchati.Models.FeatureModel;
import com.tu.tuchati.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.MyHolder> {

    private Context context;
    public ArrayList<FeatureModel> featureList;

    public FeatureAdapter(Context context, ArrayList<FeatureModel> featureList) {
        this.context = context;
        this.featureList = featureList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.horizontal_scroll_item_layout,parent,false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        FeatureModel featureModel = featureList.get(position);
        String city = featureModel.getCity();
        String country =featureModel.getCountry();
        String shopname =featureModel.getShopName();
        String shopimage = featureModel.getShopimage();
        String uid = featureModel.getUid();
        loadReviews(featureModel, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });

        //set data
        holder.fshopname.setText(shopname);
        holder.shoplocation.setText(city+", "+country);
        try {
            Picasso.get().load(shopimage).placeholder(R.drawable.store_gray).into(holder.shopImagefeatured);
        }
        catch (Exception e){
            holder.shopImagefeatured.setImageResource(R.drawable.store_gray);
        }
    }

    private float ratingSum=0;
    private void loadReviews(FeatureModel featureModel, MyHolder holder) {
        String shopUid = featureModel.getUid();
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

                        holder.shopratingbar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return featureList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
    private ImageView shopImagefeatured;
    private TextView fshopname,shoplocation;
    private RatingBar shopratingbar;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            shopImagefeatured = itemView.findViewById(R.id.shopImagefeatured);
            fshopname= itemView.findViewById(R.id.featured_shop_name);
            shoplocation = itemView.findViewById(R.id.feature_shop_location);
            shopratingbar = itemView.findViewById(R.id.shopratings);
        }
    }
}
