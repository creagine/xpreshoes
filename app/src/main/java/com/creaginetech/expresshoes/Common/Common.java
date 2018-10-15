package com.creaginetech.expresshoes.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.creaginetech.expresshoes.Model.User;
import com.creaginetech.expresshoes.Remote.APIService;
import com.creaginetech.expresshoes.Remote.RetrofitClient;

public class Common {

    public static String topicName = "news";

    public static User currentUser;

    public static String PHONE_TEXT = "userPhone";

    private static final String BASE_URL = "https://fcm.googleapis.com/";

    public static APIService getFCMService()
    {
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String code)
    {
        if(code.equals("0"))
            return "Placed";
        else if (code.equals("1"))
            return "On my way";
        else if (code.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }


    public static final String DELETE = "DELETE";
    public static final String USER_KEY = "User";
    public static final String PASSWORD_KEY = "Password";


    public static boolean isConnectedToInternet (Context context) // partt 15 check internet connection
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager !=null)
        {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null)
            {
                for (int i=0;i<info.length;i++)
                {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;

    }

}
