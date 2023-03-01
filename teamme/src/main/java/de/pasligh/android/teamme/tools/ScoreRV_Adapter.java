package de.pasligh.android.teamme.tools;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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


    public Map<Integer, List> getRoundResultMap() {
        return roundResultMap;
    }

    Map<Integer, List> roundResultMap;
    ScoreRV_Interface listener;

    Context ctxt;

    public ScoreRV_Adapter(Context p_context, List<Score> p_scores, ScoreRV_Interface p_listener) {
        ctxt = p_context;
        listener = p_listener;
        updateScores(p_scores, false);
    }

    public void updateScores(List<Score> p_scores, boolean reload) {

        roundResultMap = new HashMap<Integer, List>();
        for (Score s : p_scores) {
            if (roundResultMap.get(s.getRoundNr()) == null) {
                roundResultMap.put((int) s.getRoundNr(), new ArrayList<Score>());
            }

            roundResultMap.get(s.getRoundNr()).add(s);
        }
        if (reload) {
            notifyDataSetChanged();
        }
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
        score = (Score) roundResultMap.get(position).get(0);
        holder.playerName.setText(String.valueOf(score.getRoundNr() + 1));
        holder.getLayoutButtons().removeAllViews();

        List<Score> lisScore = getRoundResultMap().get(score.getRoundNr());
        holder.setIntWinnerTeam(TextHelper.getWinnerTeam_by_OverallScore(lisScore));
        if (holder.getIntWinnerTeam() >= 0 && holder.getIntWinnerTeam() < Flags.DRAW_TEAM) {
            holder.getResult().setText(ctxt.getString(R.string.team) + " " + TeamReactor.getAssignmentsByTeam(holder.getIntWinnerTeam()).get(0).getPlayer().getName() + " " + ctxt.getString(R.string.wins));
        } else {
            holder.getResult().setText(ctxt.getString(R.string.draw));
        }
        listener.recieveHolder(holder, getRoundResultMap().get(position));
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    public class RoundResultViewHolder extends RecyclerView.ViewHolder {
        TextView round;
        LinearLayout layoutButtons;
        TextView playerName;

        public int getIntWinnerTeam() {
            return intWinnerTeam;
        }

        public void setIntWinnerTeam(int intWinnerTeam) {
            this.intWinnerTeam = intWinnerTeam;
        }

        int intWinnerTeam = -1;

        public TextView getResult() {
            return result;
        }

        TextView result;

        public LinearLayout getLayoutButtons() {
            return layoutButtons;
        }

        RoundResultViewHolder(View itemView) {
            super(itemView);
            playerName = (TextView) itemView.findViewById(R.id.RoundResultNumberTV);
            round = (TextView) itemView.findViewById(R.id.RoundResultTitleTV);
            layoutButtons = (LinearLayout) itemView.findViewById(R.id.RoundResult_TeamButtonsLayout);
            result = (TextView) itemView.findViewById(R.id.RoundResultResultTV);
        }
    }

}