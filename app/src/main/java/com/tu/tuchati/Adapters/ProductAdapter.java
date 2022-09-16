package com.tu.tuchati.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.FilterProducts;
import com.tu.tuchati.Activities.ProductDetailsActivity;
import com.tu.tuchati.Models.ProductsModel;
import com.tu.tuchati.R;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private Context context;
    public ArrayList<ProductsModel> productList, filterList;
    private FilterProducts filter;
    public ProductAdapter(Context context, ArrayList<ProductsModel> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
               View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.row_products,parent,false);

               return new ProductViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductsModel productsModel = productList.get(position);
        String id = productsModel.getProductId();
        String dicountedprice =productsModel.getDiscountPrice();
        String discountAvailable = productsModel.getDiscountAvailable();
        String moreThanOneOrder =productsModel.getMoreThanOneOrder();
        String productCategory =productsModel.getProductCategory();
        String productCondition =productsModel.getProductCondition();
        String productDescription =productsModel.getProductDescription();
        String productImage =productsModel.getProductImage();
        String productPrice =productsModel.getProductPrice();
        String productTitle =productsModel.getProductTitle();
        String timestamp =productsModel.getTimestamp();
        String uid =productsModel.getUid();

        holder.product_title.setText(productTitle);
        holder.product_description.setText(productDescription);
        holder.product_price.setText("Ksh "+productPrice);
        holder.discount_price.setText("Ksh "+dicountedprice);
        if (discountAvailable.equals("true")){
            holder.discount_price.setVisibility(View.VISIBLE);
            holder.product_price.setPaintFlags(holder.product_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            // holder.product_price.setVisibility(View.VISIBLE);
        }
        else{
            holder.discount_price.setVisibility(View.GONE);
        }
        try{
            Picasso.get().load(productImage).placeholder(R.drawable.store_gray).into(holder.productImage);
        }catch (Exception e){
            holder.productImage.setImageResource(R.drawable.store_gray);
        }

        
        holder.itemView.setOnClickListener(v -> {
           // detailsBottomSheet(productsModel);
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("product_title",productsModel.getProductTitle());
            intent.putExtra("product_price",productsModel.getProductPrice());
            intent.putExtra("discount_price",productsModel.getDiscountPrice());
            intent.putExtra("product_image",productsModel.getProductImage());
            intent.putExtra("discountAvailable",productsModel.getDiscountAvailable());
            intent.putExtra("morethanone",productsModel.getMoreThanOneOrder());
            intent.putExtra("productcondition",productsModel.getProductCondition());
            intent.putExtra("productcategory",productsModel.getProductCategory());
            intent.putExtra("productdescription",productsModel.getProductDescription());
            intent.putExtra("productid",productsModel.getProductId());
            intent.putExtra("uid",productsModel.getUid());
            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProducts(this,filterList);
        }
        return filter;
    }

//products view Holder
    class ProductViewHolder extends RecyclerView.ViewHolder{
        private ImageView productImage;
        private TextView product_title,product_description,discount_price,product_price;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            product_title = itemView.findViewById(R.id.product_title);
            product_description = itemView.findViewById(R.id.product_description);
            discount_price = itemView.findViewById(R.id.discount_price);
            product_price = itemView.findViewById(R.id.product_price);


        }
    }

}
