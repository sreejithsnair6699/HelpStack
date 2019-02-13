package centennial.ca.helpstack;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import centennial.ca.helpstack.Model.User;

public class LoginPage extends Activity {

    EditText edtEmail, edtPassword;
    String str_email, str_password, str_ques, str_phone = "";

    FirebaseAuth auth;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        auth = FirebaseAuth.getInstance();

        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        String phone = getIntent().getStringExtra("phonenumber");


        if(email != null && password != null){
            edtEmail.setText(email);
            edtPassword.setText(password);
            str_email = edtEmail.getText().toString();
            str_password = edtPassword.getText().toString();
            str_phone = phone;
        }
    }

    public void onBackPressed(){
        super.onBackPressed();
        Intent intent = new Intent(this, StartPage.class);
        startActivity(intent);
        finish();
    }

    public void btnLoginClicked(View view){
        str_email = edtEmail.getText().toString();
        str_password = edtPassword.getText().toString();


        if(TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)){
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        }
        else {
            login();
        }
    }

    private void verifyEmailId(String ques){
        FirebaseUser firebaseUser = auth.getCurrentUser();
        Boolean isEmailVerified = firebaseUser.isEmailVerified();

        if(isEmailVerified){
            securityQues(ques);
        }
        else {
            Toast.makeText(this, "Please verify your email", Toast.LENGTH_LONG).show();
            auth.signOut();
        }
    }

    private void login(){

        pd = new ProgressDialog(LoginPage.this);
        pd.setMessage("Please wait...");
        pd.show();

        auth.signInWithEmailAndPassword(str_email, str_password)
                .addOnCompleteListener(LoginPage.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                    .child(auth.getCurrentUser().getUid());

                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    pd.dismiss();

                                    dataFetcher();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    pd.dismiss();
                                }
                            });
                        }
                        else {
                            pd.dismiss();
                            Toast.makeText(LoginPage.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void dataFetcher(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                str_ques = user.getQues();
                verifyEmailId(str_ques);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void forgotPasswordClicked(View view){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View v = layoutInflater.inflate(R.layout.reset_password_layout, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);

        final EditText emailForReset = v.findViewById(R.id.edtResetPassEmail);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SEND RESET LINK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String useremail = emailForReset.getText().toString().trim();

                if(useremail.equals("")){
                    Toast.makeText(v.getContext(), "Please enter your registered email ID", Toast.LENGTH_LONG).show();
                }
                else {
                    auth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(v.getContext(), "Password reset email sent!", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(v.getContext(), "Error in sending password reset link. Please check the email ID entered.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                Toast.makeText(v.getContext(), "Profile Updated successfully", Toast.LENGTH_LONG).show();
            }
        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });


        alertDialog.setView(v);
        alertDialog.show();
    }

    private void securityQues(final String ques){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View v = layoutInflater.inflate(R.layout.security_ques_layout, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setCancelable(false);

        final EditText answer = v.findViewById(R.id.edtSecurity_ques);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SUBMIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String securityanswer = answer.getText().toString();
                Log.d("SECURITY", securityanswer);
                Log.d("SECURITY", ques);

                if(securityanswer.equals(ques)){
                    Intent intent = new Intent(LoginPage.this, MainPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else{
                    auth.signOut();
                }
            }
        });

        alertDialog.setView(v);
        alertDialog.show();
    }

}
