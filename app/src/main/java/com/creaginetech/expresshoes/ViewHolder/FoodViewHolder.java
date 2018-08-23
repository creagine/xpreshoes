package com.creaginetech.expresshoes.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.creaginetech.expresshoes.Activity.FoodListActivity;
import com.creaginetech.expresshoes.Database.Database;
import com.creaginetech.expresshoes.Interface.ItemClickListener;
import com.creaginetech.expresshoes.Model.Food;
import com.creaginetech.expresshoes.Model.Order;
import com.creaginetech.expresshoes.R;

import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView food_name, food_price, food_description, tvnumber;
    public ImageView food_image;
    protected Button btn_plus, btn_minus;
    Food currentFood;


    private ItemClickListener itemClickListener;

    public FoodViewHolder(View itemView) {
        super(itemView);

        food_name = itemView.findViewById(R.id.food_name);
        food_image = itemView.findViewById(R.id.food_image);
        tvnumber = itemView.findViewById(R.id.number);
        btn_plus = itemView.findViewById(R.id.plus);
        btn_minus = itemView.findViewById(R.id.minus);

        btn_plus.setTag(R.integer.btn_plus_view, itemView);
        btn_minus.setTag(R.integer.btn_minus_view, itemView);
        btn_plus.setOnClickListener(this);
        btn_minus.setOnClickListener(this);

        //Init View
        food_description = itemView.findViewById(R.id.food_description);
        food_price = itemView.findViewById(R.id.food_price);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

//        itemClickListener.onClick(view,getAdapterPosition(),false);

        if (view.getId() == btn_plus.getId()){

            View tempview = (View) btn_plus.getTag(R.integer.btn_plus_view);
            TextView tv = (TextView) tempview.findViewById(R.id.number);
            int number = Integer.parseInt(tv.getText().toString()) + 1;
            tv.setText(String.valueOf(number));
            if(number > 0){FoodListActivity.sheetBehavior.setState(STATE_EXPANDED);
            } else {
                FoodListActivity.sheetBehavior.setState(STATE_HIDDEN);
            }
            FoodListActivity.adapter.getItem(getAdapterPosition()).setNumber(number);
//            new Database(itemView.getContext()).addToCart(new Order(
//                    //selesaikan bagian ini buat + - to cart
//                    FoodListActivity.adapter.getRef(getAdapterPosition()).getKey(),
//                    currentFood.getName1(),
//                    String.valueOf(number),
//                    currentFood.getPrice(),
//                    currentFood.getDiscount()
//            ));

        } else if(view.getId() == btn_minus.getId()) {

            View tempview = (View) btn_minus.getTag(R.integer.btn_minus_view);
            TextView tv = (TextView) tempview.findViewById(R.id.number);
            int number = Integer.parseInt(tv.getText().toString()) - 1;
            tv.setText(String.valueOf(number));
            if(number > 0){FoodListActivity.sheetBehavior.setState(STATE_EXPANDED);
            } else {
                FoodListActivity.sheetBehavior.setState(STATE_HIDDEN);
            }
            FoodListActivity.adapter.getItem(getAdapterPosition()).setNumber(number);
        }

    }
}
