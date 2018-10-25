package com.creaginetech.expresshoes.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.HomeActivity;
import com.creaginetech.expresshoes.MainActivity;
import com.creaginetech.expresshoes.R;
import com.facebook.accountkit.AccountKit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class AccountFragment extends Fragment {

    Button btnName, btnAddress, btnLogout;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        btnName = view.findViewById(R.id.buttonUpdateName);
        btnAddress = view.findViewById(R.id.buttonUpdateAddress);
        btnLogout = view.findViewById(R.id.buttonLogout);

        Paper.init(getActivity());

        btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showChangePasswordDialog();

            }
        });

        btnAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showHomeAddressDialog();

            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Delete remember user and password (delete all key value saved after press logout (exit app))
                Paper.book().destroy();

                //Logout

                Intent signIn = new Intent(getActivity(),MainActivity.class);
                signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                AccountKit.logOut();
                startActivity(signIn);

            }
        });

        return view;
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("CHANGE HOME ADDRESS");
        alertDialog.setMessage("Please fill all information !");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout_home = inflater.inflate(R.layout.home_address_layout,null);

        final MaterialEditText edtHomeAddress = (MaterialEditText)layout_home.findViewById(R.id.edtHomeAddress);

        alertDialog.setView(layout_home);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                //set new home address
                Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("User")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getActivity(), "Update Address Successful", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        alertDialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("UPDATE NAME");
        alertDialog.setMessage("Please fill all information !");

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout_name = inflater.inflate(R.layout.update_name_layout,null);

        final MaterialEditText edtName = (MaterialEditText)layout_name.findViewById(R.id.edtName);

        alertDialog.setView(layout_name);

        //Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //For use SpotsDialog, please use AlertDialog from android.app, not from v7 like above AlertDialog
                final android.app.AlertDialog waitingDialog = new SpotsDialog(getActivity());
                waitingDialog.show();

                //Update Name
                Map<String,Object> update_name = new HashMap<>();
                update_name.put("name",edtName.getText().toString());

                FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(Common.currentUser.getPhone())
                        .updateChildren(update_name)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //Dismiss dialog
                                waitingDialog.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(getActivity(), "Name was updated", Toast.LENGTH_SHORT).show();

                            }
                        });


            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();

    }

}
