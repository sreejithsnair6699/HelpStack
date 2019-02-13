package centennial.ca.helpstack;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import centennial.ca.helpstack.Model.Post;
import centennial.ca.helpstack.Model.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView name, email, centennialId, posts, role, department;
    String id, phone, ques;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });

        name = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        centennialId = findViewById(R.id.centennial_id);
        posts = findViewById(R.id.no_of_posts);
        role = findViewById(R.id.role);
        department = findViewById(R.id.department);

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dataFetcher();
        nrPostFetcher();

    }

    public void btnEditProfileClicked(View view){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                EditProfile(FirebaseAuth.getInstance().getCurrentUser().getUid(), user.getCentennial_id(), user.getFirstname(),
                        user.getLastname(), user.getEmail(), user.getRole(), user.getDepartment());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void dataFetcher(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                String str_name = user.getFirstname() + " " + user.getLastname();
                name.setText(str_name);
                email.setText(user.getEmail());
                centennialId.setText(user.getCentennial_id());
                role.setText(user.getRole());
                department.setText(user.getDepartment());
                phone = user.getPhoneNo();
                ques = user.getQues();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void nrPostFetcher(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if(post.getPublisher().equals(id)){
                        i++;
                    }
                }
                posts.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_profile_page, menu);

        // Associate searchable configuration with the SearchView

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sign_out) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sign Out");
            builder.setMessage("Are you sure about signing out ?");
            builder.setIcon(R.drawable.ic_sign_out_icon);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ProfileActivity.this, LoginPage.class);
                    startActivity(intent);
                    finishAffinity();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void EditProfile(final String userId, String centennialId, String firstName, String lastName, String email, String role, String department){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View v = layoutInflater.inflate(R.layout.edit_profile_layout, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Enter The Details");
        alertDialog.setCancelable(false);

        List<String> departmentList = new ArrayList<>();

        departmentList.clear();
        departmentList.add("Software");
        departmentList.add("Business");
        departmentList.add("Art Design");
        departmentList.add("Culinary");

        List<String> roleList = new ArrayList<>();

        roleList.clear();
        roleList.add("Student");
        roleList.add("Faculty");
        roleList.add("Office Staff");


        final EditText edtCentennialId = (EditText) v.findViewById(R.id.txtCentennialId);
        edtCentennialId.setText(centennialId);

        final EditText edtFirstName = (EditText) v.findViewById(R.id.txtFirstName);
        edtFirstName.setText(firstName);

        final EditText edtLastName = (EditText) v.findViewById(R.id.txtLastName);
        edtLastName.setText(lastName);

        final EditText edtEmail = (EditText) v.findViewById(R.id.txtEmail);
        edtEmail.setText(email);
        edtEmail.setEnabled(false);

        final Spinner spnRole = v.findViewById(R.id.prof_role_selector);
        final Spinner spnDepartment = v.findViewById(R.id.prof_department_selector);

        ArrayAdapter<String> roleadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roleList);
        spnRole.setAdapter(roleadapter);
        spnRole.setSelection(itemSelected(role));

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, departmentList);
        spnDepartment.setAdapter(departmentAdapter);
        spnDepartment.setSelection(itemSelected(department));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);

                User user = new User(userId, edtCentennialId.getText().toString(), edtFirstName.getText().toString(),
                        edtLastName.getText().toString(), edtEmail.getText().toString(), phone,
                        spnRole.getSelectedItem().toString(), spnDepartment.getSelectedItem().toString(), ques);
                reference.setValue(user);

                Toast.makeText(v.getContext(), "Profile Updated successfully", Toast.LENGTH_LONG).show();
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
            case "Student" : loc = 0; break;
            case "Faculty" : loc = 1; break;
            case "Office Staff" : loc = 2; break;
        }
        return loc;
    }
}
