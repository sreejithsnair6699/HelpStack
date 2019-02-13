package centennial.ca.helpstack;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import centennial.ca.helpstack.Adapter.PostAdapter;
import centennial.ca.helpstack.Model.Post;
import centennial.ca.helpstack.Model.User;

public class MainPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    public static List<String> departmentList = new ArrayList<>();

    private EditText searchView;
    private EditText filterTextView;
    private Spinner filterView;
    InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Help Stack");

        recyclerView = findViewById(R.id.query_recycler_view);

        postLists = new ArrayList<>();

        postAdapter = new PostAdapter(this, postLists);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(postAdapter);

        searchView = findViewById(R.id.edt_search_post);
        filterView = findViewById(R.id.filter_department_selector);
        filterTextView = findViewById(R.id.edt_filter_post);

        departmentList.clear();
        departmentList.add("Select Department");
        departmentList.add("Software");
        departmentList.add("Business");
        departmentList.add("Art Design");
        departmentList.add("Culinary");


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, departmentList);
        filterView.setAdapter(adapter);
        filterView.setSelection(0);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        readPosts();

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchPosts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        filterTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void readPosts(){
        postLists.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);

                    postLists.add(post);
                }
                postAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main_page, menu);

        // Associate searchable configuration with the SearchView

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            if(searchView.getVisibility() != View.VISIBLE){
                searchView.setVisibility(View.VISIBLE);
                searchView.requestFocus();
                filterView.setVisibility(View.GONE);

                inputMethodManager.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
            }
            else{
                searchView.setVisibility(View.GONE);
                filterView.setVisibility(View.GONE);
                searchView.setText("");
                readPosts();
                inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
            }
            return true;
        }

        if (id == R.id.filter) {
            if(filterView.getVisibility() != View.VISIBLE){
                filterView.setVisibility(View.VISIBLE);
                searchView.setVisibility(View.GONE);
                filterView.setSelection(0);
                filterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(i>0)
                            filterTextView.setText(adapterView.getItemAtPosition(i).toString());
                        else{
                            filterTextView.setText("");
                            readPosts();
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            }
            else{
                filterView.setVisibility(View.GONE);
                searchView.setVisibility(View.GONE);
                readPosts();
            }
            return true;
        }
        if (id == R.id.profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private  void searchPosts(String s){
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_title")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    postLists.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private  void filterPosts(String s){
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("department")
                .equalTo(s);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    postLists.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void btnAddPostClicked(View view){
        startActivity(new Intent(MainPage.this, PostActivity.class));
    }
}
