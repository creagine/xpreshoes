package com.creaginetech.expresshoes.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.R;

public class ServiceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView serviceName,servicePrice;
    public ImageView serviceImage, fav_image;
    public Button quick_cart;

    private ItemClickListener itemClickListener;

    public ServiceViewHolder(View itemView) {
        super(itemView);

        //edit sesuaikan service_item
        serviceName = (TextView)itemView.findViewById(R.id.service_name);
        serviceImage = (ImageView)itemView.findViewById(R.id.service_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);
        servicePrice = (TextView) itemView.findViewById(R.id.service_price);
        quick_cart = (Button) itemView.findViewById(R.id.btn_quick_cart);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);
    }

}
