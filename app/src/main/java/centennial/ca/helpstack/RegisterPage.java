package centennial.ca.helpstack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegisterPage extends AppCompatActivity {

    EditText edtCentennialID, edtFirstName, edtLastName, edtEmail, edtPassword, edtSecurityQues, edtPhoneNumber;
    Spinner spnRole, spnDepartment, spnCountryCode;

    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog pd;

    List<String> departmentList;
    List<String> roleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        initialize();
    }

    private void initialize(){
        edtCentennialID = (EditText) findViewById(R.id.txtCentennialId);
        edtFirstName = (EditText) findViewById(R.id.txtFirstName);
        edtLastName = (EditText) findViewById(R.id.txtLastName);
        edtEmail = (EditText) findViewById(R.id.txtEmail);
        edtPassword = (EditText) findViewById(R.id.txtPassword);
        spnCountryCode = findViewById(R.id.spinnerCountries);
        edtPhoneNumber = findViewById(R.id.txtPnoneNumber);
        spnRole = findViewById(R.id.prof_role_selector);
        spnDepartment = findViewById(R.id.prof_department_selector);
        edtSecurityQues = findViewById(R.id.txtSecurityQuestion);

        departmentList = new ArrayList<>();

        departmentList.clear();
        departmentList.add("Software");
        departmentList.add("Business");
        departmentList.add("Art Design");
        departmentList.add("Culinary");

        roleList = new ArrayList<>();

        roleList.clear();
        roleList.add("Student");
        roleList.add("Faculty");
        roleList.add("Office Staff");

        ArrayAdapter<String> roleadapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, roleList);
        spnRole.setAdapter(roleadapter);
        spnRole.setSelection(0);

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, departmentList);
        spnDepartment.setAdapter(departmentAdapter);
        spnDepartment.setSelection(0);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames);
        spnCountryCode.setAdapter(countryAdapter);
        spnCountryCode.setSelection(0);

        auth = FirebaseAuth.getInstance();
    }

    public void btnRegisterClicked(View view){
        pd = new ProgressDialog(RegisterPage.this);
        pd.setMessage("Please wait...");
        pd.show();

        String str_username = edtCentennialID.getText().toString();
        String str_firstname = edtFirstName.getText().toString();
        String str_lastname = edtLastName.getText().toString();
        String str_email = edtEmail.getText().toString();
        String str_password = edtPassword.getText().toString();
        String str_role = spnRole.getSelectedItem().toString();
        String str_department = spnDepartment.getSelectedItem().toString();
        String str_ques = edtSecurityQues.getText().toString();
        String str_phone = "+" + CountryData.countryAreaCodes[spnCountryCode.getSelectedItemPosition()] + edtPhoneNumber.getText().toString().trim();

        if(TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_firstname) || TextUtils.isEmpty(str_lastname) || TextUtils.isEmpty(str_email) ||
                TextUtils.isEmpty(str_password) || TextUtils.isEmpty(str_role) || TextUtils.isEmpty(str_department)){
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        }
        else if(str_password.length() < 6){
            Toast.makeText(this, "Password must have 6 characters", Toast.LENGTH_SHORT).show();
        }

        else if(str_phone.length() < 10){
            Toast.makeText(this, "Phone number must have 10 digits", Toast.LENGTH_SHORT).show();
        }
        else{
            register(str_username, str_firstname, str_lastname, str_email, str_password, str_phone, str_role, str_department, str_ques);
        }
    }

    private void  register(final String username, final String firstname, final String lastname, final String email, final String password,
                           final String phone, final String role, final String department, final String ques){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterPage.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("centennial_id", username);
                            hashMap.put("firstname", firstname);
                            hashMap.put("lastname", lastname);
                            hashMap.put("email", email);
                            hashMap.put("phone", phone);
                            hashMap.put("role", role);
                            hashMap.put("department", department);
                            hashMap.put("securityques", ques);


                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        sendEmailVerification();
                                        pd.dismiss();
                                        finish();
                                        Intent intent = new Intent(RegisterPage.this, LoginPage.class);
                                        intent.putExtra("phonenumber", phone);
                                        intent.putExtra("email", email);
                                        intent.putExtra("password", password);
                                        startActivity(intent);

                                    }
                                }
                            });
                        }
                        else{
                            pd.dismiss();
                            Toast.makeText(RegisterPage.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendEmailVerification(){
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterPage.this, "Successfully registered, Verification email sent!", Toast.LENGTH_LONG).show();
                        auth.signOut();
                    }
                    else {
                        Toast.makeText(RegisterPage.this, "Verification email not sent!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
