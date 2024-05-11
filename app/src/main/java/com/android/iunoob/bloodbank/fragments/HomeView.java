package com.android.iunoob.bloodbank.fragments;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.iunoob.bloodbank.R;
import com.android.iunoob.bloodbank.adapters.BloodRequestAdapter;
import com.android.iunoob.bloodbank.viewmodels.CustomUserData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeView extends Fragment {

    private RecyclerView recentPosts;
    private BloodRequestAdapter restAdapter;
    private List<CustomUserData> postLists;
    private ProgressDialog pd;

    public HomeView() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_view_fragment, container, false);
        recentPosts = view.findViewById(R.id.recyleposts);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recentPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postLists = new ArrayList<>();
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading...");
        pd.setCancelable(true);
        pd.setCanceledOnTouchOutside(false);
        restAdapter = new BloodRequestAdapter(postLists);

        RecyclerView.LayoutManager pmLayout = new LinearLayoutManager(getContext());
        recentPosts.setLayoutManager(pmLayout);
        recentPosts.setItemAnimator(new DefaultItemAnimator());
        recentPosts.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recentPosts.setAdapter(restAdapter);

        fetchPosts();
    }

    private void fetchPosts() {
        pd.show();

        // Perform data fetching asynchronously
        new AsyncTask<Void, Void, List<CustomUserData>>() {
            @Override
            protected List<CustomUserData> doInBackground(Void... voids) {
                List<CustomUserData> fetchedPosts = new ArrayList<>();
                DatabaseReference donorRef = FirebaseDatabase.getInstance().getReference().child("posts");
                donorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                CustomUserData customUserData = postSnapshot.getValue(CustomUserData.class);
                                if (customUserData != null) {
                                    fetchedPosts.add(customUserData);
                                }
                            }
                        }
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("HomeView", "Database error: " + databaseError.getMessage());
                        pd.dismiss();
                    }
                });
                return fetchedPosts;
            }

            @Override
            protected void onPostExecute(List<CustomUserData> fetchedPosts) {
                // Update RecyclerView with fetched posts
                postLists.addAll(fetchedPosts);
                restAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
}
