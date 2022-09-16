package com.tu.tuchati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.ShopDetailsActivity;
import com.tu.tuchati.Models.MyfeaturedShopModel;
import com.tu.tuchati.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MyFeaturedShopAdapter extends RecyclerView.Adapter<MyFeaturedShopAdapter.MyHolder> {
    private Context context;
    public ArrayList<MyfeaturedShopModel> myfeaturedShopModellist;

    public MyFeaturedShopAdapter(Context context, ArrayList<MyfeaturedShopModel> myfeaturedShopModellist) {
        this.context = context;
        this.myfeaturedShopModellist = myfeaturedShopModellist;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.featured_item,parent,false);
        return new MyHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        MyfeaturedShopModel myfeaturedShopModel = myfeaturedShopModellist.get(position);
        String shopname =myfeaturedShopModel.getShopName();
        String shopimage = myfeaturedShopModel.getShopimage();
        String packgeType = myfeaturedShopModel.getPackageType();
        String uid = myfeaturedShopModel.getShopuid();
       // long timestart = myfeaturedShopModel.getTimestart();
        Long timestend = myfeaturedShopModel.getTimeend();

        //convert time to dd/mm/yy
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestend);
        String dateTime = DateFormat.format("EEE, MMM d, ''yy", cal).toString();

      //  long timedifferent = timestend-timestart;

        holder.shopName.setText(shopname);
        holder.timeend.setText("Until ("+dateTime+")");
        holder.packageType.setText(packgeType);

        try {
            Picasso.get().load(shopimage).placeholder(R.drawable.store_gray).into(holder.storeprofileImage);
        }
        catch (Exception e){
            holder.storeprofileImage.setImageResource(R.drawable.store_gray);
        }

        //handle click listener, show shop details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShopDetailsActivity.class);
            intent.putExtra("shopUid", uid);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return myfeaturedShopModellist.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        private ImageView storeprofileImage;
        private TextView shopName,timeend,packageType;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            storeprofileImage = itemView.findViewById(R.id.store_profile_image);
            shopName = itemView.findViewById(R.id.shopName);
            timeend = itemView.findViewById(R.id.timeend);
            packageType = itemView.findViewById(R.id.package_type);
        }
    }
}
