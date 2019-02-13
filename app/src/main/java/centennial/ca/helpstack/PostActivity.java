package centennial.ca.helpstack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    String title, description;

    EditText edtTitle, edtDescription;

    Spinner departmentSelector;


    public static List<String> departmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        Toolbar toolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });

        departmentList.clear();
        departmentList.add("Software");
        departmentList.add("Business");
        departmentList.add("Art Design");
        departmentList.add("Culinary");


        departmentSelector = findViewById(R.id.department_selector);
        ArrayAdapter<String> rsadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, departmentList);
        departmentSelector.setAdapter(rsadapter);
        departmentSelector.setSelection(0);

        edtTitle = findViewById(R.id.edtPostTitle);
        edtDescription = findViewById(R.id.edtPostDescription);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_create_post, menu);

        // Associate searchable configuration with the SearchView

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.create_post) {
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Posting...");
            pd.show();

            String str_title;
            String str_description;
            String str_department;


            str_title = edtTitle.getText().toString();
            str_description = edtDescription.getText().toString();
            str_department = departmentSelector.getSelectedItem().toString();

            if(TextUtils.isEmpty(str_title) || TextUtils.isEmpty(str_description)){
                pd.dismiss();
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            }
            else{
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                String postid = reference.push().getKey();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("postid", postid);
                hashMap.put("post_title", str_title);
                hashMap.put("description", str_description);
                hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                hashMap.put("department", str_department);


                reference.child(postid).setValue(hashMap);

                pd.dismiss();

                startActivity(new Intent(PostActivity.this, MainPage.class));
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
