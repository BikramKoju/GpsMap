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

public class DriverLoginRegistryActivity extends AppCompatActivity {

    EditText emailDriver, passwordDriver;
    Button driverRegisterButton, driverLoginButton;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    private DatabaseReference driverDatabaseRef;
    private String onlineDriverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login_registry);

        mAuth = FirebaseAuth.getInstance();


        progressDialog = new ProgressDialog(this);

        emailDriver = findViewById(R.id.email_driver);
        driverLoginButton = findViewById(R.id.driver_login_btn);
        passwordDriver = findViewById(R.id.password_driver);
        driverRegisterButton = findViewById(R.id.driver_register_btn);

        driverLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailDriver.getText().toString();
                String password = passwordDriver.getText().toString();

                SigninDriver(email, password);

            }
        });

        driverRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailDriver.getText().toString();
                String password = passwordDriver.getText().toString();

                RegisterDriver(email, password);
            }
        });
    }

    private void SigninDriver(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write password...", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Driver Login");
            progressDialog.setMessage("Please wait, while we checking data");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(DriverLoginRegistryActivity.this, DriversMapActivity.class);
                                startActivity(intent);

                                Toast.makeText(DriverLoginRegistryActivity.this, "Driver Login Successful", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            } else {
                                Toast.makeText(DriverLoginRegistryActivity.this, "Driver Login unsuccessful", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

        }

    }

    private void RegisterDriver(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please write email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please write password...", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Driver Registration");
            progressDialog.setMessage("Please wait, while we register data");

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                onlineDriverId = mAuth.getCurrentUser().getUid();
                                driverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                        .child("users").child("drivers").child(onlineDriverId);

                                driverDatabaseRef.setValue(true);

                                Intent intent = new Intent(DriverLoginRegistryActivity.this, DriversMapActivity.class);
                                startActivity(intent);

                                Toast.makeText(DriverLoginRegistryActivity.this, "Driver Register Successful", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();


                            } else {
                                Toast.makeText(DriverLoginRegistryActivity.this, "Driver Register unsuccessful", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

        }
    }
}
