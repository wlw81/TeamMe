package de.pasligh.android.teamme.tools;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.Score;

/**
 * Created by Thomas on 07.02.2016.
 */
public class ScoreRV_Adapter extends RecyclerView.Adapter<ScoreRV_Adapter.RoundResultViewHolder> {

    Map<Integer, Score> roundResultMap;


    public ScoreRV_Adapter(Map<Integer, Score> p_rounds) {
        this.roundResultMap = p_rounds;
    }

    @Override
    public RoundResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_player_selection, parent, false);
        RoundResultViewHolder pvh = new RoundResultViewHolder(v);

        return pvh;
    }

    @Override
    public int getItemCount() {
        return roundResultMap.size();
    }

    @Override
    public void onBindViewHolder(RoundResultViewHolder holder, int position) {
        holder.playerName.setText(roundResultMap.get(0).getRoundNr());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public static class RoundResultViewHolder extends RecyclerView.ViewHolder {
        TextView playerName;

        RoundResultViewHolder(View itemView) {
            super(itemView);
            playerName = (TextView) itemView.findViewById(R.id.RoundResultNumberTV);
        }
    }

}