package com.creaginetech.expresshoes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Model.User;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 7171;
    Button btnContinue;
    TextView txtSlogan;

    FirebaseDatabase database;
    DatabaseReference users;


    //calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Note: add this code before setContentView method
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("fonts/cf.otf")
//                .setFontAttrId(R.attr.fontPath)
//                .build());

        AccountKit.initialize(this);
        setContentView(R.layout.activity_main);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("User");



        btnContinue = (Button)findViewById(R.id.btn_continue);

        txtSlogan = (TextView)findViewById(R.id.txtSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Nabila.ttf");
        txtSlogan.setTypeface(face);



        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginSystem();
            }
        });

        //Check Session Facebook Account Kit
        if (AccountKit.getCurrentAccessToken() != null)
        {
            //Create Dialog
            final AlertDialog waitingDialog = new SpotsDialog(this);
            waitingDialog.show();
            waitingDialog.setMessage("Please wait");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    //Copy code from exists user
                    users.child(account.getPhoneNumber().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    User localUser = dataSnapshot.getValue(User.class);
                                    //copy from SigninActivity
                                    Intent homeIntent = new Intent(MainActivity.this, MainNewActivity.class);
                                    Common.currentUser = localUser;
                                    startActivity(homeIntent);
                                    //Dissmis dialog
                                    waitingDialog.dismiss();
                                    finish();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                }

                @Override
                public void onError(AccountKitError accountKitError) {

                }
            });
        }

    }

    private void startLoginSystem() {
        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }

//    private void loginremember(final String phone, final String pwd) {
//        //copy login code from signin.class
//
//        //Init Firebase
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        final DatabaseReference table_user = database.getReference("User");
//
//        if (Common.isConnectedToInternet(getBaseContext())) {
//
//            //part cek internet connection
//            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
//            mDialog.setMessage("Please waiting....");
//            mDialog.show();
//
//            table_user.addValueEventListener(new ValueEventListener() {
//
//
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    //Check if user no exist in database
//                    if (dataSnapshot.child(phone).exists()) {
//                        //Get User Information
//                        mDialog.dismiss();
//                        User user = dataSnapshot.child(phone).getValue(User.class);
//                        user.setPhone(phone);
//                        if (user.getPassword().equals(pwd)) {
//                            {
//                                Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
//                                Common.currentUser = user;
//                                startActivity(homeIntent);
//                                finish();
//                            }
//                        } else {
//                            Toast.makeText(MainActivity.this, "Wrong Password !!!", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        mDialog.dismiss();
//                        Toast.makeText(MainActivity.this, "User no exist in Database", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//        else
//        {
//            Toast.makeText(MainActivity.this, "Please Check your connection", Toast.LENGTH_SHORT).show(); //check internet connnection
//            return;
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE)
        {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError() != null)
            {
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            else if (result.wasCancelled())
            {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
                return;
            }
            else
            {
                if (result.getAccessToken() != null)
                {
                    //Show Dialog
                    final AlertDialog waitingDialog = new SpotsDialog(this);
                    waitingDialog.show();
                    waitingDialog.setMessage("Please wait");
                    waitingDialog.setCancelable(false);

                    //Get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            final String userPhone = account.getPhoneNumber().toString();

                            //Check if exists on Firebase Users
                            users.orderByKey().equalTo(userPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(userPhone).exists()) //if not exists
                                            {
                                                //we will create new user and login
                                                User newUser = new User();
                                                newUser.setPhone(userPhone);
                                                newUser.setName("");

                                                //Add to Firebase
                                                users.child(userPhone)
                                                        .setValue(newUser)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                    Toast.makeText(MainActivity.this, "User register successful !", Toast.LENGTH_SHORT).show();

                                                                //Login
                                                                users.child(userPhone)
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                                User localUser = dataSnapshot.getValue(User.class);
                                                                                //copy from SigninActivity
                                                                                Intent homeIntent = new Intent(MainActivity.this, RestaurantList.class);
                                                                                Common.currentUser = localUser;
                                                                                startActivity(homeIntent);
                                                                                //Dissmis dialog
                                                                                waitingDialog.dismiss();
                                                                                finish();

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });


                                                            }
                                                        });

                                            }
                                            else //if exists
                                            {
                                                //we will just login
                                                //Login
                                                users.child(userPhone)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                                User localUser = dataSnapshot.getValue(User.class);
                                                                //copy from SigninActivity
                                                                Intent homeIntent = new Intent(MainActivity.this, RestaurantList.class);
                                                                Common.currentUser = localUser;
                                                                startActivity(homeIntent);
                                                                //Dissmis dialog
                                                                waitingDialog.dismiss();
                                                                finish();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}
