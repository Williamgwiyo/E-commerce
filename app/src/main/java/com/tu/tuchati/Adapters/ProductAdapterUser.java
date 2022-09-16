package com.tu.tuchati.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tu.tuchati.Activities.FilterProductsUser;
import com.tu.tuchati.Activities.ProductDetailsActivity;
import com.tu.tuchati.Activities.ShopDetailsActivity;
import com.tu.tuchati.Models.ProductsModel;
import com.tu.tuchati.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ProductAdapterUser extends RecyclerView.Adapter<ProductAdapterUser.ProductViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ProductsModel> productList, filterList;
    private FilterProductsUser filter;

    public ProductAdapterUser(Context context, ArrayList<ProductsModel> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_products_user,parent,false);

        return new ProductAdapterUser.ProductViewHolder(view);
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

        holder.addProductToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuantityDialog(productsModel);
            }
        });


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
            context.startActivity(intent);

        });
    }
    private double cost = 0;
    private double finalcost = 0;
    private int quantity = 0;
    private void showQuantityDialog(ProductsModel productsModel) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_addtocart,null);

        ImageView productIv = view.findViewById(R.id.productIv);
        TextView titleTv = view.findViewById(R.id.titleTv);
        TextView descriptionTv = view.findViewById(R.id.descriptionTv);
        TextView originalPriceTv = view.findViewById(R.id.originalPriceTv);
        TextView priceDiscountedTv = view.findViewById(R.id.priceDiscountedTv);
        TextView finalTv = view.findViewById(R.id.finalTv);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTv = view.findViewById(R.id.quantityTv);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model
        String productId = productsModel.getProductId();
        String title = productsModel.getProductTitle();
        String description= productsModel.getProductDescription();
        String image = productsModel.getProductImage();

        String price;
        if (productsModel.getDiscountAvailable().equals("true")){
            price = productsModel.getDiscountPrice();
            originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            priceDiscountedTv.setVisibility(View.GONE);
            price = productsModel.getProductPrice();
        }

        cost = Double.parseDouble(price.replaceAll("Ksh ", ""));
        finalcost = Double.parseDouble(price.replaceAll("Ksh ", ""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try{
            Picasso.get().load(image).placeholder(R.drawable.cart_gray).into(productIv);
        }
        catch (Exception e){
            productIv.setImageResource(R.drawable.cart_gray);
        }

        titleTv.setText(""+title);
        descriptionTv.setText(""+description);
        quantityTv.setText(""+quantity);
        originalPriceTv.setText("ksh "+productsModel.getProductPrice());
        priceDiscountedTv.setText("ksh "+productsModel.getDiscountPrice());
        finalTv.setText("ksh "+finalcost);

        AlertDialog dialog = builder.create();
        dialog.show();
        //increase qunatity of the product
        incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finalcost = finalcost + cost;
              quantity++;

              finalTv.setText("ksh "+finalcost);
              quantityTv.setText(""+quantity);
            }
        });
            //decrement quantity of product
        decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity>1){
                    finalcost = finalcost - cost;
                    quantity--;

                    finalTv.setText("ksh "+finalcost);
                    quantityTv.setText(""+quantity);
                }
            }
        });
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleTv.getText().toString().trim();
                String priceEach = originalPriceTv.getText().toString().trim().replace("ksh ","");
                String price = finalTv.getText().toString().trim().replace("ksh ","");
                String quantity = quantityTv.getText().toString().trim();

                //add to db
                addToCart(productId,title,priceEach,price,quantity);

                dialog.dismiss();

            }
        });
    }
    private int itemId = 1;
    private void addToCart(String productId, String title, String priceEach, String price, String quantity) {
        itemId++;

       EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                .doneTableColumn();

       Boolean b = easyDB.addData("Item_Id", itemId)
               .addData("Item_PID", productId)
               .addData("Item_Name",title)
               .addData("Item_Price_Each",priceEach)
               .addData("Item_Price",price)
               .addData("Item_Quantity",quantity)
               .doneDataAdding();

        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
        //update cart count
        ((ShopDetailsActivity)context).cartCount();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductsUser(this,filterList);
        }
        return filter;
    }
    //products view Holder

    class ProductViewHolder extends RecyclerView.ViewHolder{
        private ImageView productImage;
        private TextView product_title,product_description,discount_price,product_price,addProductToCart;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            product_title = itemView.findViewById(R.id.product_title);
            product_description = itemView.findViewById(R.id.product_description);
            discount_price = itemView.findViewById(R.id.discount_price);
            product_price = itemView.findViewById(R.id.product_price);
            addProductToCart = itemView.findViewById(R.id.addProductToCart);


        }
    }
}
