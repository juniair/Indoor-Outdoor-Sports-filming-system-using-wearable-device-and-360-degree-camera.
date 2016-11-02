package com.crv.myapplication.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crv.myapplication.R;
import com.crv.myapplication.model.Recode;
import com.crv.myapplication.util.OnItemClickListener;
import com.crv.myapplication.util.OnMenuItemClickListener;

import java.util.List;

public class RecodeAdapter extends RecyclerView.Adapter<RecodeAdapter.RecodeViewHolder> {

    private Context mContext;
    private List<Recode> recodeList;
    private OnMenuItemClickListener menuItemClickListener;
    private OnItemClickListener itemClickListener;


    public class RecodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public ImageView thumbnail, overflow;
        public RecodeViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            thumbnail.setOnClickListener(this);
            overflow = (ImageView) itemView.findViewById(R.id.overflow);
        }

        @Override
        public void onClick(View v) {

            if(itemClickListener != null) {
                itemClickListener.itemClicked(title);
            }
        }
    }


    public RecodeAdapter(Context mContext, List<Recode> recodeList) {
        this.mContext = mContext;
        this.recodeList = recodeList;
    }

    @Override
    public RecodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recode_card, parent, false);

        return new RecodeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecodeViewHolder holder, int position) {

        Recode recode = recodeList.get(position);
        holder.title.setText(recode.getTitle());
        Glide.with(mContext).load(recode.getThumbnailURL()).into(holder.thumbnail);

    }



    public void setMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return recodeList.size();
    }

}
