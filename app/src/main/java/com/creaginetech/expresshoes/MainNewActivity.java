package com.creaginetech.expresshoes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.creaginetech.expresshoes.Fragment.AccountFragment;
//import com.creaginetech.expresshoes.Fragment.CartFragment;
import com.creaginetech.expresshoes.Fragment.HomeNewFragment;
import com.creaginetech.expresshoes.Fragment.OrderFragment;
import com.creaginetech.expresshoes.Helper.BottomNavigationViewHelper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainNewActivity extends AppCompatActivity {

    //calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);

        //inisialisasi bottomnav
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        //on item selected bottomnav
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //load fragment default (HomeFragment)
        loadFragment(new HomeNewFragment());

        //hilangkan animasi bottomnav
        BottomNavigationViewHelper.removeShiftMode(navigation);

    }

    //method on item selected listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeNewFragment();
                    loadFragment(fragment);
                    break;
                case R.id.navigation_orders:
                    fragment = new OrderFragment();
                    loadFragment(fragment);
                    break;
                case R.id.navigation_account:
                    fragment = new AccountFragment();
                    loadFragment(fragment);
                    break;
            }

            return true;

        }
    };

    //method load fragment
    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
            finishAffinity();
        }

}
