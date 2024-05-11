package com.android.iunoob.bloodbank.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.android.iunoob.bloodbank.R;
import com.android.iunoob.bloodbank.fragments.AboutUs;
import com.android.iunoob.bloodbank.fragments.AchievmentsView;
import com.android.iunoob.bloodbank.fragments.BloodInfo;
import com.android.iunoob.bloodbank.fragments.HomeView;
import com.android.iunoob.bloodbank.fragments.NearByHospitalActivity;
import com.android.iunoob.bloodbank.fragments.SearchDonorFragment;
import com.android.iunoob.bloodbank.viewmodels.UserData;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseDatabase user_db;
    private FirebaseUser cur_user;
    private DatabaseReference userdb_ref;

    private TextView getUserName;
    private TextView getUserEmail;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        user_db = FirebaseDatabase.getInstance();
        cur_user = mAuth.getCurrentUser();
        userdb_ref = user_db.getReference("users");

        // Initialize ProgressDialog
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);

        // Find TextViews
        getUserName = findViewById(R.id.UserNameView);
        getUserEmail = findViewById(R.id.UserEmailView);

        // Fetch and display user data
        fetchAndDisplayUserData();

        // Other initialization code...

        // Toolbar setup...
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation drawer setup...
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Default fragment setup...
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentcontainer, new HomeView()).commit();
            navigationView.getMenu().getItem(0).setChecked(true);
        }
    }

    private void fetchAndDisplayUserData() {
        // Show ProgressDialog
        pd.show();

        // Query the database for the current user's data
        userdb_ref.child(cur_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if dataSnapshot has children
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    // Data exists, parse it into UserData object
                    UserData userData = dataSnapshot.getValue(UserData.class);
                    if (userData != null) {
                        // Update UI with user data
                        getUserName.setText(userData.getName());
                        getUserEmail.setText(userData.getEmail());
                    } else {
                        // UserData is null
                        Log.e("Dashboard", "UserData is null");
                    }
                } else {
                    // Data snapshot is empty or does not exist
                    Log.e("Dashboard", "Data snapshot is empty or does not exist");
                    // You can handle this scenario according to your application logic
                    Toast.makeText(getApplicationContext(), "No user data found", Toast.LENGTH_LONG).show();
                }

                // Dismiss ProgressDialog
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Database error occurred
                Log.e("Dashboard", "Database error: " + databaseError.getMessage());
                // Dismiss ProgressDialog
                pd.dismiss();
                // You can display a message or handle the error as needed
                Toast.makeText(getApplicationContext(), "Failed to fetch user data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.home) {
            fragment = new HomeView();
        } else if (id == R.id.userprofile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        } else if (id == R.id.user_achiev) {
            fragment = new AchievmentsView();
        } else if (id == R.id.logout) {
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.blood_storage) {
            fragment = new SearchDonorFragment();
        } else if (id == R.id.nearby_hospital) {
            fragment = new NearByHospitalActivity();
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentcontainer, fragment).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
