package com.creaginetech.expresshoes.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.R;

//pengganti restaurantViewHolder
public class ShopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txt_shop_name, txt_shop_address;
    public ImageView img_shop;

    private ItemClickListener itemClickListener;

    public ShopViewHolder(View itemView){
        super(itemView);

        txt_shop_name = itemView.findViewById(R.id.shop_name);
        img_shop = itemView.findViewById(R.id.shop_image);
        txt_shop_address = itemView.findViewById(R.id.shop_address);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }

}
