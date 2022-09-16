package com.tu.tuchati.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.tu.tuchati.Activities.CreateGroupActivity;
import com.tu.tuchati.Activities.CreatePostActivity;
import com.tu.tuchati.Activities.EmailLoginActivity;
import com.tu.tuchati.Activities.FindFriendsActivity;
import com.tu.tuchati.Activities.FriendsActivity;
import com.tu.tuchati.Activities.MyOrderActivity;
import com.tu.tuchati.Activities.MyProfileActivity;
import com.tu.tuchati.Activities.MyShopActivity;
import com.tu.tuchati.Activities.PaymentActivity;
import com.tu.tuchati.Activities.PeopleNearbyActivity;
import com.tu.tuchati.Activities.SettingsActivity;
import com.tu.tuchati.Activities.ShopSetupActivity;
import com.tu.tuchati.Adapters.ChatlistAdapter;
import com.tu.tuchati.Models.ChatModel;
import com.tu.tuchati.Models.ChatlistModel;
import com.tu.tuchati.Models.User;
import com.tu.tuchati.R;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment implements View.OnClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public ChatsFragment() {
        // Required empty public constructor
    }

    private View view;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView navigationBar;
    private Button logout_btn;
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ChatlistModel> chatlistModels;
    List<User> userList;
    String currentUserID;
    DatabaseReference reference;
    private ProgressDialog progressDialog;
    FirebaseUser currentUser;
    ChatlistAdapter chatlistAdapter;
    private Button startchatting;


    private RelativeLayout loginlogout;
    private TextView languages,your_orders,wishlist,address,help,rate_us,settings,my_orders,start_selling,people_nearby,create_group,textView5,mywallet,nofriendrequest;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
        view= inflater.inflate(R.layout.fragment_chats, container, false);

        //init
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        chatlistModels = new ArrayList<>();



        reference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(currentUser.getUid());
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatlistModels.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ChatlistModel chatlist = ds.getValue(ChatlistModel.class);
                    chatlistModels.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        onSetNavigationDrawerEvents();

        // startchatting.setVisibility(View.GONE);
        startchatting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadChats() {

        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               userList.clear();

               for (DataSnapshot ds: snapshot.getChildren()){
                   User user = ds.getValue(User.class);
                  for (ChatlistModel chatlist: chatlistModels){
                      if (user.getUid() !=null && user.getUid().equals(chatlist.getId())){
                          startchatting.setVisibility(View.GONE);
                          nofriendrequest.setVisibility(View.GONE);
                          textView5.setVisibility(View.GONE);

                          userList.add(user);
                          break;
                      }
                  }
                  //adapter
                   chatlistAdapter = new ChatlistAdapter(getContext(),userList);
                  //setadapter
                   recyclerView.setAdapter(chatlistAdapter);
                   //set last message
                   for (int i=0; i<userList.size(); i++){
                       lastMessage(userList.get(i).getUid());
                   }

               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(String uid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String theLastMessage ="default";
                    for (DataSnapshot ds: snapshot.getChildren()){
                        ChatModel chat = ds.getValue(ChatModel.class);
                        if (chat==null){
                            continue;
                        }
                        String sender = chat.getSender();
                        String receiver = chat.getReceiver();
                        if (sender == null || receiver == null){
                            continue;
                        }
                        if (chat.getReceiver().equals(currentUser.getUid()) &&
                                chat.getSender().equals(uid) ||
                                chat.getReceiver().equals(uid) &&
                                        chat.getSender().equals(currentUser.getUid())){
                            if (chat.getType().equals("image")){
                                theLastMessage = "Sent a photo";
                            }
                            else{
                                theLastMessage = chat.getMessage();
                            }
                        }
                    }
                    chatlistAdapter.setLastMessageMap(uid,theLastMessage);
                    chatlistAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onSetNavigationDrawerEvents() {
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawerLayout);
        navigationView = (NavigationView) view.findViewById(R.id.navView);

        navigationBar = view.findViewById(R.id.navigationBar);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();


        startchatting = view.findViewById(R.id.startChatting);
        settings = view.findViewById(R.id.settings);
        people_nearby = view.findViewById(R.id.people_nearby);
        logout_btn=view.findViewById(R.id.logout_btn);
        start_selling=view.findViewById(R.id.start_selling);
        logout_btn=view.findViewById(R.id.logout_btn);
        your_orders=view.findViewById(R.id.add_new_post);
        languages = view.findViewById(R.id.languages);
        wishlist=view.findViewById(R.id.profile);
        address=view.findViewById(R.id.my_friends);
        help=view.findViewById(R.id.search_people);
        rate_us=view.findViewById(R.id.rate_us);
        create_group = view.findViewById(R.id.create_group);
        textView5 = view.findViewById(R.id.textView5);
        nofriendrequest = view.findViewById(R.id.nofriendRequest);
        mywallet = view.findViewById(R.id.myWallet);
        my_orders =view.findViewById(R.id.my_orders);



        progressDialog = new ProgressDialog(getContext());

        navigationBar.setOnClickListener(this);
        languages.setOnClickListener(this);
        create_group.setOnClickListener(this);
        people_nearby.setOnClickListener(this);
        start_selling.setOnClickListener(this);
        your_orders.setOnClickListener(this);
        help.setOnClickListener(this);
        my_orders.setOnClickListener(this);
        mywallet.setOnClickListener(this);
        address.setOnClickListener(this);
        rate_us.setOnClickListener(this);
        wishlist.setOnClickListener(this);
        settings.setOnClickListener(this);
        logout_btn.setOnClickListener(v -> {
            LogOutUser();
        });

    }
    private void LogOutUser() {

        String timestamp = String.valueOf(System.currentTimeMillis());

        //set offline with timestamp
        checkOnlineStatus(timestamp);
        firebaseAuth.signOut();
        Intent intent = new Intent(getActivity(), EmailLoginActivity.class);
        startActivity(intent);
    }
    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserID);
        dbRef.keepSynced(true);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update status of online user
        dbRef.updateChildren(hashMap);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.navigationBar:
                drawerLayout.openDrawer(navigationView,true);
                break;

            case R.id.add_new_post:
                Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.search_people:
                Intent frindfriends = new Intent(getActivity(), FindFriendsActivity.class);
                startActivity(frindfriends);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.my_friends:
                Intent friends = new Intent(getActivity(), FriendsActivity.class);
                startActivity(friends);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.rate_us:
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.profile:
                Intent profile = new Intent(getActivity(), MyProfileActivity.class);
                startActivity(profile);
                drawerLayout.closeDrawer(navigationView,true);
                break;
            case R.id.settings:
                Intent profileSetting = new Intent(getActivity(), SettingsActivity.class);
                startActivity(profileSetting);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.myWallet:
                Intent wallet = new Intent(getActivity(), PaymentActivity.class);
                startActivity(wallet);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.my_orders:
                Intent myorders = new Intent(getActivity(), MyOrderActivity.class);
                startActivity(myorders);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.start_selling:
                CheckIfShopCreated();
                drawerLayout.closeDrawer(navigationView,true);
                break;


            case R.id.people_nearby:
                Intent peoplenearby = new Intent(getActivity(), PeopleNearbyActivity.class);
                startActivity(peoplenearby);
                drawerLayout.closeDrawer(navigationView,true);
                break;

            case R.id.create_group:
                Intent create_group = new Intent(getActivity(), CreateGroupActivity.class);
                startActivity(create_group);
                drawerLayout.closeDrawer(navigationView,true);
                break;


        }

    }
    private void CheckIfShopCreated() {
        progressDialog.setTitle("Please wait....");
        progressDialog.setMessage("checking if shop is created");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s: snapshot.getChildren()){
                            String accountType = ""+s.child("accountType").getValue();
                            if (accountType.equals("User"))
                            {

                                progressDialog.dismiss();
                                Intent intent = new Intent(getActivity(), ShopSetupActivity.class);
                                startActivity(intent);
                                start_selling.setText("Start Selling");

                                Animatoo.animateSwipeRight(getActivity());

                            }
                            else if(accountType.equals("Seller")) {

                                progressDialog.dismiss();
                                Intent intent = new Intent(getActivity(), MyShopActivity.class);
                                startActivity(intent);
                                start_selling.setText("My Shop");

                                Animatoo.animateSwipeRight(getActivity());
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void changeTextofshop(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.keepSynced(true);
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s: snapshot.getChildren()){
                            String accountType = ""+s.child("accountType").getValue();
                            if (accountType.equals("User"))
                            {

                                progressDialog.dismiss();

                                start_selling.setText("Start Selling");
                                my_orders.setVisibility(View.VISIBLE);

                                Animatoo.animateSwipeRight(getActivity());

                            }
                            else if(accountType.equals("Seller")) {

                                progressDialog.dismiss();

                                start_selling.setText("My Shop");
                                my_orders.setVisibility(View.GONE);

                                Animatoo.animateSwipeRight(getActivity());
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    @Override
    public void onStart() {
        super.onStart();
        changeTextofshop();
    }
}