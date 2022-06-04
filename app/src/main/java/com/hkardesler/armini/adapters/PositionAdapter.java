/*
 * *
 *  * Created by Haydar Kardesler on 4.06.2022 06:55
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */


package com.hkardesler.armini.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Shader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.hkardesler.armini.R;
import com.hkardesler.armini.helpers.TileDrawable;
import com.hkardesler.armini.impls.PositionItemClickListener;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.Scenario;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.ViewHolder> {

    private final ArrayList<Position> positions;
    private final LayoutInflater mInflater;
    private final Context context;
    private final PositionItemClickListener listener;

    public PositionAdapter(Context context, ArrayList<Position> positions, PositionItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.positions = positions;
        this.context = context;
        this.listener = listener;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_position, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return positions.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPositionName, txtMotorSpeed;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);
            txtPositionName = itemView.findViewById(R.id.txtPositionName);
            txtMotorSpeed = itemView.findViewById(R.id.txtMotorSpeed);
            cardView = itemView.findViewById(R.id.cardview);
        }

        public void setData(int position){
            Position scenarioPosition = positions.get(position);

            txtPositionName.setText(context.getString(R.string.position_name, scenarioPosition.getKey()));
            txtMotorSpeed.setText(context.getString(R.string.motor_speed, scenarioPosition.getMotorSpeed().getStringValue()));

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(getAdapterPosition());
                }
            });
        }


    }

}