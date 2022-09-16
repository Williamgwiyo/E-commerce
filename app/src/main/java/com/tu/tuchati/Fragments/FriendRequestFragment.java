package com.tu.tuchati.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tu.tuchati.Activities.FindFriendsActivity;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FriendRequestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendRequestFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FriendRequestFragment() {
        // Required empty public constructor
    }

    FirebaseRecyclerOptions<User> options;
    private DatabaseReference RequstRef,UsersRef,FriendsRef;
    private FirebaseAuth mAuth;
    private String CURRENT_STATE,saveCurrentDate,currentUserId;
    RecyclerView requestRecyclerView;
    Button button;
    TextView nofriendRequest,textView5;

    // TODO: Rename and change types and number of parameters
    public static FriendRequestFragment newInstance(String param1, String param2) {
        FriendRequestFragment fragment = new FriendRequestFragment();
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
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_friend_request, container, false);
        requestRecyclerView = view.findViewById(R.id.requestRecyclerView);
        RequstRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        RequstRef.keepSynced(true);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);
        UsersRef.keepSynced(true);
        CURRENT_STATE = "not_friends";
        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        nofriendRequest = view.findViewById(R.id.nofriendRequest);
        textView5= view.findViewById(R.id.textView5);
        button=view.findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FindFriendsActivity.class);
                getContext().startActivity(intent);
            }
        });



        options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(RequstRef.child(currentUserId),User.class)
                .build();



        FirebaseRecyclerAdapter<User,RequestsViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<User, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull User model) {
                //make the accept and reject buttons visible
                String list_user_id = getRef(position).getKey();

                holder.acceptRequest.setVisibility(View.VISIBLE);
                holder.rejectRequest.setVisibility(View.VISIBLE);

                holder.acceptRequest.setOnClickListener(v -> {
                    Calendar calFordDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
                    saveCurrentDate = currentDate.format(calFordDate.getTime());

                    HashMap friends = new HashMap();
                    friends.put("date",""+saveCurrentDate);
                    friends.put("status", "friends");

                    FriendsRef.child(currentUserId).child(list_user_id).setValue(friends).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //add again to the receiver
                            HashMap friends = new HashMap();
                            friends.put("date",""+saveCurrentDate);
                            friends.put("status", "friends");

                            FriendsRef.child(list_user_id).child(currentUserId).setValue(friends).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    RequstRef.child(currentUserId).child(list_user_id)
                                            //for the reciever
                                            .removeValue()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful())
                                                {
                                                    //for the sender
                                                    RequstRef.child(list_user_id).child(currentUserId)
                                                            .removeValue()
                                                            .addOnCompleteListener(task1 -> {
                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(getContext(), "You are now Friends", Toast.LENGTH_SHORT).show();
                                                                }

                                                            });

                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                    FriendsRef.child(list_user_id).child(currentUserId).child("date").setValue(saveCurrentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FriendsRef.child(currentUserId).child(list_user_id).child("date").setValue(saveCurrentDate)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                });
                holder.rejectRequest.setOnClickListener(v -> RequstRef.child(currentUserId).child(list_user_id)
                        //for the reciever
                        .removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                            {
                                //for the sender
                                RequstRef.child(list_user_id).child(currentUserId)
                                        .removeValue()
                                        .addOnCompleteListener(task1 -> {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(getContext(), "Request rejected", Toast.LENGTH_SHORT).show();
                                            }

                                        });

                            }
                        }));

                //getting the request type
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            String type = snapshot.getValue().toString();
                            nofriendRequest.setVisibility(View.GONE);
                            button.setVisibility(View.GONE);
                            textView5.setVisibility(View.GONE);
                            if (type.equals("received")){
                                //display the request
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //if the user exists
                                        if (snapshot.exists()){
                                            final String requestUserName= snapshot.child("username").getValue().toString();
                                            final String requestUserstatus= snapshot.child("status").getValue().toString();
                                            final String requestprofileImage= snapshot.child("profileImage").getValue().toString();

                                            Picasso.get().load(requestprofileImage).into(holder.allUserProfileImage);

                                            holder.allUsername.setText(requestUserName);
                                            holder.allStatus.setText(requestUserstatus);
                                        }
                                        else {//user does not exist
                                            holder.allUsername.setVisibility(View.GONE);
                                            holder.allStatus.setVisibility(View.GONE);
                                            holder.acceptRequest.setVisibility(View.GONE);
                                            holder.rejectRequest.setVisibility(View.GONE);
                                            holder.allUserProfileImage.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            else if (type.equals("sent")){
                                holder.acceptRequest.setVisibility(View.GONE);
                                holder.rejectRequest.setVisibility(View.GONE);

                                //display the request
                                UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //for person who sent request

                                        final String requestUserName= snapshot.child("username").getValue().toString();
                                        final String requestUserstatus= snapshot.child("status").getValue().toString();
                                        final String requestprofileImage= snapshot.child("profileImage").getValue().toString();

                                        Picasso.get().load(requestprofileImage).into(holder.allUserProfileImage);

                                        holder.allUsername.setText(requestUserName);
                                        holder.allStatus.setText("You sent request to "+requestUserName);



                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        else{
                            nofriendRequest.setVisibility(View.GONE);
                            button.setVisibility(View.GONE);
                            textView5.setVisibility(View.GONE);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);

                return new RequestsViewHolder(v);
            }

        };
        //set adapter on recyclerview
        requestRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

        requestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        CircleImageView allUserProfileImage;
        TextView allUsername,allStatus;
        Button acceptRequest,rejectRequest;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            allUserProfileImage = itemView.findViewById(R.id.allUserProfileImage);
            allUsername = itemView.findViewById(R.id.allUsername);
            allStatus = itemView.findViewById(R.id.allStatus);
            acceptRequest = itemView.findViewById(R.id.accpt);
            rejectRequest = itemView.findViewById(R.id.reject);

        }
    }


}