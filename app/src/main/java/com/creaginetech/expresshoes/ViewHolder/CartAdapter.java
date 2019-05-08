package com.creaginetech.expresshoes.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
//import com.creaginetech.expresshoes.CartActivity;
import com.creaginetech.expresshoes.CartNewActivity;
import com.creaginetech.expresshoes.Common.Common;
import com.creaginetech.expresshoes.Database.Database;
//import com.creaginetech.expresshoes.Fragment.CartFragment;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Order;
import com.creaginetech.expresshoes.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class CartAdapter extends RecyclerView.Adapter<CartViewHolder>{

    private List<Order> listData = new ArrayList<>();
    private CartNewActivity cart;

    public CartAdapter(List<Order> listData, CartNewActivity cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView = inflater.inflate(R.layout.cart_layout,parent,false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, final int position) {
//        TextDrawable drawable = TextDrawable.builder()
//                .buildRound(""+listData.get(position).getQuantity(), Color.RED);
//        holder.img_cart_count.setImageDrawable(drawable);


        //memunculkan gambar di cart
        Picasso.with(cart.getBaseContext())
                .load(listData.get(position).getImage())
                .resize(70,70) //70dp
                .centerCrop()
                .into(holder.cart_image);

        holder.btn_quantity.setNumber(listData.get(position).getQuantity());

        holder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order = listData.get(position);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //Update tctTotal di cart
                //Calculate total price
                int total = 0;
                List<Order> orders = new Database(cart).getCarts(Common.currentUser.getPhone());
                for (Order itemCart : orders)
                    total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(itemCart.getQuantity()));

                int totalitems = 0;
                for (Order itemCart :orders)
                    totalitems +=((Integer.parseInt(itemCart.getQuantity())));

                int totalpayment = total + (int)cart.deliveryFee;

                cart.txtTotalItems.setText(String.valueOf(totalitems));
                cart.txtTotalPrice.setText(NumberFormat.getInstance(Locale.GERMAN).format(totalpayment));


            }
        });

        int price = (Integer.parseInt(listData.get(position).getPrice()))*(Integer.parseInt(listData.get(position).getQuantity()));
        holder.txt_price.setText(NumberFormat.getInstance(Locale.GERMAN).format(price));

        holder.txt_cart_name.setText(listData.get(position).getProductName());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public Order getItem(int position)
    {
        return listData.get(position);
    }

    public void removeItem(int position)
    {
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item,int position)
    {
        listData.add(position,item);
        notifyItemInserted(position);
    }
}
