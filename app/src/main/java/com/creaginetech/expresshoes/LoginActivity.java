package com.creaginetech.expresshoes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText editText;
    private Button btnContinue;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        //check jika user sdh login langsung masuk mainNewActivity
        checkCurrentUser();

        editText = findViewById(R.id.editTextPhone);
        btnContinue = findViewById(R.id.buttonContinue);

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    btnContinue.performClick();

                    return true;
                }
                return false;
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = "62";

                String number = editText.getText().toString().trim();

                if (number.isEmpty() || number.length() < 10) {
                    editText.setError("Valid number is required");
                    editText.requestFocus();
                    return;
                }

                String phoneNumber = "+" + code + number;

                Intent intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("phonenumber", phoneNumber);
                startActivity(intent);
            }
        });

    }

    private void checkCurrentUser() {

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {

            String userPhone = mAuth.getCurrentUser().getPhoneNumber();

            usersRef.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User localUser = dataSnapshot.getValue(User.class);

                            Common.currentUser = localUser;


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            Intent homeIntent = new Intent(LoginActivity.this, MainNewActivity.class);
            startActivity(homeIntent);
            finish();
        }

    }
}
