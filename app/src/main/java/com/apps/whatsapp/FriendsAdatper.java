package com.apps.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdatper extends FirebaseRecyclerAdapter<Contacts,FriendsAdatper.ContactsViewHolder>
{
    // for friend detail showing
  //  private onItemClickListener listener;

    private Context mContext;
    private RecyclerClick mRecyclerClick;

    public FriendsAdatper(@NonNull FirebaseRecyclerOptions<Contacts> options, Context mContext, RecyclerClick mRecyclerClick )
    {
        super(options);
        this.mContext = mContext;
        this.mRecyclerClick = mRecyclerClick; // where is your main class
    }

    @Override
    protected void onBindViewHolder(@NonNull ContactsViewHolder holder, final int position, @NonNull Contacts model)
    {

        holder.userProfileName.setText(model.getName());
        holder.userStatus.setText(model.getStatus());

        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
        //   String userImage = Picasso.get().load(model.getImage()).into(userProfileImage);

        // for searching friends, this is the object of the view from the contacts view holder class
        // so if the user clicks anywhere in the , textView or the image or the status he or she can see the profile
      //  String visit_user_id = getRef(position).getKey();

     //   Intent profileIntent = new Intent(, ProfileActivity.class);
     //   profileIntent.putExtra("visit_user_id", visit_user_id);
        //startActivity(profileIntent);


    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // converts the xml file to the layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdisplayfriendslayout,parent,false);

        return  new ContactsViewHolder(view, mRecyclerClick);
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        private TextView userProfileName, userStatus;
        public  CircleImageView userProfileImage;
        RecyclerClick mRecyclerClick;

        public ContactsViewHolder(@NonNull View itemView, RecyclerClick recyclerClick) {
            super(itemView);
            userProfileName = itemView.findViewById(R.id.user_profile_name);
            userProfileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            this.mRecyclerClick = recyclerClick;

            itemView.setOnClickListener(this);
          /*  // for showing the next data
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {

                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION&& listener!=null)
                    {

                      listener.onItemClick(getSnapshots().getSnapshot(position),position);

                    }
                }
            });
        }

           */
        }

        @Override
        public void onClick(View v) {
            mRecyclerClick.OnClick(getRef(getAdapterPosition()).getKey()); // here pass the data you want to //
        }

    }

  /*  public  interface  onItemClickListener
    {
        void onItemClick(DataSnapshot dataSnapshot, int position);
    }

    public void setOnItemClickListener(onItemClickListener listener)
    {
        this.listener = listener;

    }

   */



}
