/*
 * Created by Haydar Kardesler.
 * Copyright (c) 2021. All Rights Reserved.
 *
 */

package com.hkardesler.armini.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.hkardesler.armini.R;
import com.hkardesler.armini.helpers.TileDrawable;
import com.hkardesler.armini.impls.ScenarioItemClickListener;
import com.hkardesler.armini.models.Scenario;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ScenarioAdapter extends RecyclerView.Adapter<ScenarioAdapter.ViewHolder> {

    private final ArrayList<Scenario> scenarios;
    private final LayoutInflater mInflater;
    private final Context context;
    private final ScenarioItemClickListener listener;

    public ScenarioAdapter(Context context, ArrayList<Scenario> scenarios, ScenarioItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.scenarios = scenarios;
        this.context = context;
        this.listener = listener;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_scenario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        return scenarios.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtScenarioName, txtPositionCount;
        CardView cardView;
        FrameLayout frameLayoutPattern;

        ViewHolder(View itemView) {
            super(itemView);
            txtScenarioName = itemView.findViewById(R.id.txtScenarioName);
            txtPositionCount = itemView.findViewById(R.id.txtPositionCount);
            frameLayoutPattern = itemView.findViewById(R.id.frameLayoutPattern);
            cardView = itemView.findViewById(R.id.card_view);
        }

        public void setData(int position){
            Scenario scenario = scenarios.get(position);

            txtScenarioName.setText(scenario.getName());
            int positionSize = scenario.getPositions().size();
            if(scenario.getPositions().size() > 1){
                txtPositionCount.setText(context.getString(R.string.position_count, positionSize));
            }else{
                txtPositionCount.setText(context.getString(R.string.single_position, positionSize));
            }
            int patternNumber = ThreadLocalRandom.current().nextInt(0, 8);
            TypedArray patternDrawableList = context.getResources().obtainTypedArray(R.array.patterns);

            frameLayoutPattern.setBackground(new TileDrawable(patternDrawableList.getDrawable(patternNumber), Shader.TileMode.REPEAT));
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(getAdapterPosition());
                }
            });
        }


    }

    public void update(ArrayList<Scenario> list) {
        scenarios.clear();
        scenarios.addAll(list);
        notifyDataSetChanged();
    }
    public void setData(ArrayList<Scenario> list) {
        scenarios.clear();
        scenarios.addAll(list);
    }
}