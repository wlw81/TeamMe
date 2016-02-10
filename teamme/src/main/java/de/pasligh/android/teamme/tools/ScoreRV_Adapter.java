package de.pasligh.android.teamme.tools;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.Score;

/**
 * Created by Thomas on 07.02.2016.
 */
public class ScoreRV_Adapter extends RecyclerView.Adapter<ScoreRV_Adapter.RoundResultViewHolder> {



    Map<Integer, List> roundResultMap;
    ScoreRV_Interface listener;

    List<RoundResultViewHolder> holderList;
    Typeface tf;

    public void updateScores(List<Score> p_scores, boolean reload){
        roundResultMap = new HashMap<Integer, List>();
        for (Score s : p_scores) {
            if (roundResultMap.get(s.getRoundNr()) == null) {
                roundResultMap.put((int) s.getRoundNr(), new ArrayList<Score>());
            }

            roundResultMap.get(s.getRoundNr()).add(s);
        }
        if(reload){
            notifyDataSetChanged();
        }
    }

    public ScoreRV_Adapter(List<Score> p_scores, Typeface p_tf, ScoreRV_Interface p_listener) {
        tf = p_tf;
        listener = p_listener;
        updateScores(p_scores, false);
    }

    @Override
    public RoundResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_roundresult, parent, false);
        RoundResultViewHolder pvh = new RoundResultViewHolder(v);
        return pvh;
    }

    @Override
    public int getItemCount() {
        return roundResultMap.size();
    }


    @Override
    public void onBindViewHolder(RoundResultViewHolder holder, int position) {
        Score score = null;
        try {
            score = (Score) roundResultMap.get(position).get(0);
            holder.playerName.setText(String.valueOf(score.getRoundNr() + 1));
            holder.playerName.setTypeface(tf);
            holder.round.setTypeface(tf);
        } catch (Exception e) {
            Log.e(Flags.LOGTAG, e.getMessage());
            holder.playerName.setText("?");
        } finally {
            listener.recieveHolder(holder, score);
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public class RoundResultViewHolder extends RecyclerView.ViewHolder {
        TextView round;
        LinearLayout layoutButtons;
        TextView playerName;

        public LinearLayout getLayoutButtons() {
            return layoutButtons;
        }

        RoundResultViewHolder(View itemView) {
            super(itemView);
            playerName = (TextView) itemView.findViewById(R.id.RoundResultNumberTV);
            round = (TextView) itemView.findViewById(R.id.RoundResultTitleTV);
            layoutButtons = (LinearLayout) itemView.findViewById(R.id.RoundResult_TeamButtonsLayout);
        }
    }

}