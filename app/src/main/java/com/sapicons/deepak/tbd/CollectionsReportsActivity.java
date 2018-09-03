package com.sapicons.deepak.tbd;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.sapicons.deepak.tbd.Fragments.ClubAccountsDisplayFragment;
import com.sapicons.deepak.tbd.Fragments.CollectionsReportsFragment;

public class CollectionsReportsActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections_reports);
        Log.d("ACTIVITY","CollectionsReportsActivity");
        startCollectionsReportsFragment();
    }

    public void startCollectionsReportsFragment(){
        //Bundle bundle = new Bundle();
        //bundle.putInt("is_collect", 1);
// set Fragmentclass Arguments
        Fragment fragment =new CollectionsReportsFragment();
        //fragment.setArguments(bundle);
        if(fragment!=null) {

            fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.activity_collections_reports_content_frame, fragment, "").commit();
        }
    }
}
