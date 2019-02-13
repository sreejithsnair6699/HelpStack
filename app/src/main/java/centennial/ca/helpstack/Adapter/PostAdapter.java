package centennial.ca.helpstack.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import centennial.ca.helpstack.CommentActivity;
import centennial.ca.helpstack.Model.Post;
import centennial.ca.helpstack.Model.User;
import centennial.ca.helpstack.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;

    private FirebaseUser firebaseUser;
    private FirebaseAuth auth;

    public PostAdapter(Context mContext, List<Post> mPost){
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, viewGroup, false);
        auth = FirebaseAuth.getInstance();
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPost.get(i);


        viewHolder.postTitle.setText(post.getPost_title());
        viewHolder.postDescription.setText(post.getDescription());
        viewHolder.postDepartment.setText(post.getDepartment());


        AuthorInfo(viewHolder.postAuthor, viewHolder.AuthorDepartment, viewHolder.postAuthorEmail, post.getPublisher());
        isLiked(post.getPostid(), viewHolder.btnLike);
        nrLikes(viewHolder.likes, post.getPostid());
        getComments(post.getPostid(), viewHolder.viewAllComments);

        viewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewHolder.btnLike.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
                }
                else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        viewHolder.contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Contact author
                if(!auth.getCurrentUser().getUid().equals(post.getPublisher())){
                    PopupMenu popup = new PopupMenu(mContext, viewHolder.contact);
                    popup.getMenuInflater()
                            .inflate(R.menu.menu_contact, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(post.getPublisher());

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    String emailId = user.getEmail();
                                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                            "mailto",emailId, null));
                                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, post.getPost_title());
                                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                                    mContext.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            return true;
                        }
                    });

                    popup.show();
                }


                // Edit Post
                else {
                    final PopupMenu popup = new PopupMenu(mContext, viewHolder.contact);
                    popup.getMenuInflater()
                            .inflate(R.menu.menu_edit_comment, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            //Edit comment
                            if(item.getItemId() == R.id.comment_edit){
                                EditComment(post.getPostid(), post.getPost_title(), post.getDescription(), post.getPublisher(), post.getDepartment());
                            }

                            // Delete comment
                            if(item.getItemId() == R.id.comment_delete){
                                deleteComment(post.getPostid());
                            }

                            return true;
                        }
                    });

                    popup.show();
                }
            }
        });

        viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostid());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    private void deleteComment(final String postId){
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle("Do you want to delete the post?");
        alertDialog.setCancelable(false);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("Posts").child(postId);
                reference.removeValue();

                Toast.makeText(mContext, "Post Deleted", Toast.LENGTH_LONG).show();
            }
        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void EditComment(final String postId, String title, String description, final String publisher, String department){
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        final View v = layoutInflater.inflate(R.layout.edit_post_layout, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle("Enter The Details");
        alertDialog.setCancelable(false);

        List<String> departmentList = new ArrayList<>();

        departmentList.clear();
        departmentList.add("Software");
        departmentList.add("Business");
        departmentList.add("Art Design");
        departmentList.add("Culinary");


        final EditText edtTitle = (EditText) v.findViewById(R.id.edtPostTitle);
        edtTitle.setText(title);

        final EditText edtDescription = (EditText) v.findViewById(R.id.edtPostDescription);
        edtDescription.setText(description);

        final Spinner spnDepartment = v.findViewById(R.id.department_selector);

        ArrayAdapter<String> rsadapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, departmentList);
        spnDepartment.setAdapter(rsadapter);
        spnDepartment.setSelection(itemSelected(department));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);

                Post post = new Post(postId, edtTitle.getText().toString(), edtDescription.getText().toString(), publisher, spnDepartment.getSelectedItem().toString());
                reference.setValue(post);

                    Toast.makeText(v.getContext(), "Post Updated successfully", Toast.LENGTH_LONG).show();
            }
        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });


        alertDialog.setView(v);
        alertDialog.show();
    }

    private int itemSelected(String item){
        int loc = 0;
        switch (item){
            case "Software" : loc = 0; break;
            case "Business" : loc = 1; break;
            case "Art Design" : loc = 2; break;
            case "Culinary" : loc = 3; break;
        }
        return loc;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView postTitle, postDescription, postAuthor, AuthorDepartment, postDepartment, postAuthorEmail, likes, viewAllComments;
        public ImageView btnLike, btnComment, contact;

        public ViewHolder(View itemView){
            super(itemView);

            postTitle = itemView.findViewById(R.id.postTitle);
            postDescription = itemView.findViewById(R.id.postDescription);
            postAuthor = itemView.findViewById(R.id.postAuthor);
            AuthorDepartment = itemView.findViewById(R.id.postDepartment);
            postDepartment = itemView.findViewById(R.id.txtPostDepartment);
            postAuthorEmail = itemView.findViewById(R.id.txtEmailId);
            viewAllComments = itemView.findViewById(R.id.txtViewAllComments);
            likes = itemView.findViewById(R.id.txtNumberOfLikes);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            contact = itemView.findViewById(R.id.contact);
        }
    }

    private void getComments(String postid, final TextView tv_comments){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0){
                    String noOfComments = dataSnapshot.getChildrenCount() + " Comments";
                    tv_comments.setText(noOfComments);
                }
                else
                    tv_comments.setText("0 Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isLiked(String postid, final ImageView imageView){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_action_liked);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_action_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void nrLikes(final TextView likes, String postid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void AuthorInfo(final TextView postAuthor, final TextView postAuthorDepartment, final TextView emailId, final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String name = user.getFirstname() + " " + user.getLastname();
                postAuthor.setText(name);
                emailId.setText(user.getEmail());
                postAuthorDepartment.setText(user.getDepartment());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
