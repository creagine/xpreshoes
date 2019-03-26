package com.creaginetech.expresshoes.Service;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Model.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        if (Common.currentUser != null)
            updateTokenToFirebase(tokenRefreshed);

    }

    private void updateTokenToFirebase(String tokenRefreshed) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(tokenRefreshed,false); //false because this token from client app
        tokens.child(Common.currentUser.getPhone()).setValue(token);
    }
}
