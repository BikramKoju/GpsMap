package org.impactit.gpsmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button driverDashboard,customerDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        driverDashboard=findViewById(R.id.welcome_driver_btn);
        customerDashboard=findViewById(R.id.welcome_customer_btn);

        driverDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent driverscreen=new Intent(MainActivity.this,DriverLoginRegistryActivity.class);
                startActivity(driverscreen);
            }
        });

        customerDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customerscreen=new Intent(MainActivity.this,CustomerLoginRegisterActivity.class);
                startActivity(customerscreen);
            }
        });
    }
}
