package com.creaginetech.expresshoes.Activity;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.creaginetech.expresshoes.Fragment.AccountFragment;
import com.creaginetech.expresshoes.Fragment.CartFragment;
import com.creaginetech.expresshoes.Fragment.HomeFragment;
import com.creaginetech.expresshoes.Fragment.TransactionFragment;
import com.creaginetech.expresshoes.Helper.BottomNavigationViewHelper;
import com.creaginetech.expresshoes.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inisialisasi bottomnav
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        //on item selected bottomnav
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //load fragment default (HomeFragment)
        loadFragment(new HomeFragment());

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
                    fragment = new HomeFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_transactions:
                    fragment = new TransactionFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_account:
                    fragment = new AccountFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_cart:
                    fragment = new CartFragment();
                    loadFragment(fragment);
                    return true;
            }

            return false;

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

}
