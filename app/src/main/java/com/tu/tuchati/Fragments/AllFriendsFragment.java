package com.tu.tuchati.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tu.tuchati.Activities.FindFriendsActivity;
import com.tu.tuchati.Activities.PersonProfileActivity;
import com.tu.tuchati.Activities.privateChatActivity;
import com.tu.tuchati.Models.FriendsModel;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllFriendsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllFriendsFragment() {
        // Required empty public constructor
    }
    FirebaseRecyclerOptions<FriendsModel> options;
    private DatabaseReference FriendsRef,usersRef;
    private FirebaseAuth auth;
    private String online_user_id;
    private RecyclerView friendsList;
    Button button;
    TextView nofriendRequest,textView5;

    Context context;
    List<User> userList;


    // TODO: Rename and change types and number of parameters
    public static AllFriendsFragment newInstance(String param1, String param2) {
        AllFriendsFragment fragment = new AllFriendsFragment();
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
        View view = inflater.inflate(R.layout.fragment_all_friends, container, false);
        auth = FirebaseAuth.getInstance();
        online_user_id = auth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        FriendsRef.keepSynced(true);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        friendsList= view.findViewById(R.id.friends_list);
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


        DisplayAllFriends();

        return view;
    }

    private void DisplayAllFriends() {
        options = new FirebaseRecyclerOptions.Builder<FriendsModel>()
                .setQuery(FriendsRef,FriendsModel.class)
                .build();

        FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsViewHolder holder, int position, @NonNull FriendsModel model) {
                final  String usersIDs = getRef(position).getKey();
                assert usersIDs != null;
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            nofriendRequest.setVisibility(View.GONE);
                            button.setVisibility(View.GONE);
                            textView5.setVisibility(View.GONE);
                            //retrive user
                            final String username = snapshot.child("username").getValue().toString();
                            final String profileimage = snapshot.child("profileImage").getValue().toString();
                            final String status = snapshot.child("status").getValue().toString();
                            holder.setFullname(username);
                            holder.setStatus(status);
                            holder.setProfileimage(context,profileimage);

                            holder.mView.setOnClickListener( v -> {
                                CharSequence options[] = new CharSequence[]
                                        {
                                                username + "'s Profile",
                                                "Send Message"
                                        };
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0)
                                        {
                                            Intent profileintent =  new Intent(getContext(), PersonProfileActivity.class);
                                            profileintent.putExtra("visit",usersIDs);
                                            startActivity(profileintent);
                                        }
                                        if (which == 1)
                                        {
                                            Intent chatintent =  new Intent(getContext(), privateChatActivity.class);
                                            chatintent.putExtra("hisUid",usersIDs);
                                            chatintent.putExtra("username",username);
                                            startActivity(chatintent);
                                        }
                                    }
                                });
                                builder.show();
                            });

                        }

                        else{
                            holder.setFullname("User deactivated Account");
                            holder.setStatus("");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);

                return new FriendsViewHolder(v);
            }
        };
        firebaseRecyclerAdapter.startListening();
        friendsList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView myImage = mView.findViewById(R.id.allUserProfileImage);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile_icon).into(myImage);
        }
        public void setFullname(String fullname)
        {
            TextView myName = mView.findViewById(R.id.allUsername);
            myName.setText(fullname);
        }
        public void setStatus(String status)
        {
            TextView mystatus = mView.findViewById(R.id.allStatus);
            mystatus.setText(status);
        }
    }
}