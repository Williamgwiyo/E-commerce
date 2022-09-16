package com.tu.tuchati.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.tu.tuchati.Models.FindFriends;
import com.tu.tuchati.viewholders.FriendsViewHolder;
import com.tu.tuchati.R;
import com.tu.tuchati.databinding.ActivityFindFriendsBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FindFriendsActivity extends AppCompatActivity {

    ActivityFindFriendsBinding binding;
    private  DatabaseReference usersRef;
    // DatabaseReference DataRef;
    FirebaseRecyclerOptions<FindFriends> options;
    FirebaseRecyclerAdapter<FindFriends, FriendsViewHolder> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =  ActivityFindFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

      usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
      usersRef.keepSynced(true);

       binding.searchFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
       binding.searchFriendsRecyclerView.setHasFixedSize(true);
       LoadData("");

       binding.inputSearch.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {

           }

           @Override
           public void afterTextChanged(Editable s) {
               if (s.toString()!=null)
               {
                  LoadData(s.toString());
               }
               else
               {
                  LoadData("");
               }
           }
       });
       binding.backBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();
           }
       });

    }

    private void LoadData(String data) {
        Query query = usersRef
                .orderByChild("username")
                .startAt(data)
                .endAt(data+"\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<FindFriends>()
                .setQuery(query,FindFriends.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<FindFriends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull FindFriends model) {

                holder.xstatus.setText(model.getStatus());
                holder.userName.setText(model.getUsername());

                Glide.with(holder.profileImage
                        .getContext())
                        .load(model.getProfileImage())
                        .into(holder.profileImage);

                holder.itemView.setOnClickListener(v -> {
                    //get the key of the person
                    String visitUser = getRef(position).getKey();
                    //sent user to profile activity
                    Intent profileIntent = new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                    profileIntent.putExtra("visit", visitUser);
                    startActivity(profileIntent);
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);

                return new FriendsViewHolder(v);
            }
        };
        adapter.startListening();
        binding.searchFriendsRecyclerView.setAdapter(adapter);

    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();

    }

}