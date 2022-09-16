package com.tu.tuchati.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tu.tuchati.Adapters.MyFeaturedShopAdapter;
import com.tu.tuchati.Models.MyfeaturedShopModel;
import com.tu.tuchati.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeaturedShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeaturedShopFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeaturedShopFragment() {
        // Required empty public constructor
    }
    RecyclerView featuredRV;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private String currentUserID;
    private ArrayList<MyfeaturedShopModel> myfeaturedShoplist;
    private MyFeaturedShopAdapter featuredAdapter;

    private TextView shopName,packageType;
    private CircleImageView store_profile_image;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeaturedShopFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeaturedShopFragment newInstance(String param1, String param2) {
        FeaturedShopFragment fragment = new FeaturedShopFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragmentz
        View view= inflater.inflate(R.layout.fragment_featured_shop, container, false);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        featuredRV = view.findViewById(R.id.featuredRV);
       loadFeaturedShops();
        return view;
    }
    private void loadFeaturedShops() {
        myfeaturedShoplist = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.keepSynced(true);
        ref.orderByChild("uid").equalTo(currentUserID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myfeaturedShoplist.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    String approve = "" + ds.child("approve").getValue();
                    if (approve.equals("true")){
                        MyfeaturedShopModel myfeaturedShopModel = ds.getValue(MyfeaturedShopModel.class);

                    // String shopCity =""+ds.child("city").getValue();
                    // String shopuid =""+ds.child("uid").getValue();

                    //show the shop for the time that it has been selected to show only
                    long timecurrent = System.currentTimeMillis();
                    if (timecurrent > myfeaturedShopModel.getTimestart() && timecurrent < myfeaturedShopModel.getTimeend()) {
                        //show only user city shops

                        myfeaturedShoplist.add(myfeaturedShopModel);

                    }
                    else{
                        Toast.makeText(getContext(), "Time finished", Toast.LENGTH_SHORT).show();
                    }
                }

                }
                featuredAdapter = new MyFeaturedShopAdapter(getContext(),myfeaturedShoplist);
                featuredRV.setAdapter(featuredAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        loadFeaturedShops();
    }
}