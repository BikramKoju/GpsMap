package org.impactit.gpsmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity {

    EditText emailCustomer, passwordCustomer;
    Button customerRegisterButton, customerLoginButton;
    private ProgressDialog loadingbar;
    private FirebaseAuth mAuth;
    private DatabaseReference customerDatabaseRef;
    private String onlineCustomerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);

        loadingbar = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();

        emailCustomer = findViewById(R.id.email_customer);
        passwordCustomer = findViewById(R.id.password_customer);
        customerRegisterButton = findViewById(R.id.customer_register_btn);
        customerLoginButton = findViewById(R.id.customer_login_btn);

        customerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailCustomer.getText().toString();
                String password = passwordCustomer.getText().toString();

                RegisterCustomer(email, password);
            }
        });

        customerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailCustomer.getText().toString();
                String password = passwordCustomer.getText().toString();

                SignInCustomer(email, password);

            }
        });

    }

    private void SignInCustomer(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingbar.setTitle("Customer Login");
            loadingbar.setMessage("Please wait, while we check your data");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                   Intent intent = new Intent(CustomerLoginRegisterActivity.this, CustomerMapActivity.class);
                                startActivity(intent);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer login Successful", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();

                            } else {
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer login unsuccessful", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });

        }
    }


    private void RegisterCustomer(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write password...", Toast.LENGTH_SHORT).show();
        } else {
            loadingbar.setTitle("Customer Registration");
            loadingbar.setMessage("Please wait, while we register data");
            loadingbar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                onlineCustomerId = mAuth.getCurrentUser().getUid();
                                customerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child("customers").child(onlineCustomerId);


                                customerDatabaseRef.setValue(true);

                                Intent driverIntent = new Intent(CustomerLoginRegisterActivity.this, CustomerMapActivity.class);
                                startActivity(driverIntent);

                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Register Successful", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            } else {
                                Toast.makeText(CustomerLoginRegisterActivity.this, "Customer Register unsuccessful", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                        }
                    });

        }
    }
}
