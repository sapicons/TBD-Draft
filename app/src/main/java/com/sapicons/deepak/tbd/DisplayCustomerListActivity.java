package com.sapicons.deepak.tbd;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.sapicons.deepak.tbd.Fragments.CustomerDisplayFragment;

public class DisplayCustomerListActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_customer_list);

        Log.d("ACTIVITY","DisplayCustomerListActivity");


        startCustomerDisplayFragment();

    }


    private  void startCustomerDisplayFragment(){
        Fragment fragment =new CustomerDisplayFragment();

        if(fragment!=null) {
            fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.activity_display_customer_content_frame, fragment, "").commit();
        }
    }

}
