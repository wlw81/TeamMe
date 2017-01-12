package de.pasligh.android.teamme.tools;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;

/**
 * Created by Thomas on 02.06.2016.
 */
public class GameRecordRV_Adapter extends RecyclerView.Adapter<GameRecordRV_Adapter.GameRecordRV_Holder> {

    public List<GameRecord> getGameRecords() {
        return gameRecords;
    }

    List<GameRecord> gameRecords;
    java.text.DateFormat dateFormat;
    View.OnClickListener listener;
    Context context;

    private BackendFacade facade;


    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(context);
        }
        return facade;
    }

    public GameRecordRV_Adapter(Context p_context, View.OnClickListener p_listener, List<GameRecord> p_Values) {
        gameRecords = p_Values;
        context = p_context;
        dateFormat = android.text.format.DateFormat.getDateFormat(p_context);
        listener = p_listener;
    }

    @Override
    public GameRecordRV_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_game_record, parent, false);
        v.setOnClickListener(listener);
        return new GameRecordRV_Holder(v);
    }

    @Override
    public void onBindViewHolder(GameRecordRV_Holder holder, int position) {
        GameRecord ci = gameRecords.get(position);
        holder.sport.setText(ci.getSport());
        int winnerTeam = TextHelper.getWinnerTeam_by_RoundsOrScore(getFacade().getScores(ci.getId()), getFacade().getRoundCount(ci.getId()), getFacade().getTeamCount(ci.getId()));

        if (winnerTeam >= 0 && winnerTeam < Flags.DRAW_TEAM) {
            holder.result.setText(context.getString(R.string.team) + " " + getFacade().getCaptain(ci.getId(), winnerTeam).getPlayer() + " " + context.getString(R.string.wins));
        } else {
            holder.result.setText(context.getString(R.string.draw));
        }

        holder.date.setText(dateFormat.format(ci.getStartedAt()));
        holder.setId(ci.getId());
    }

    @Override
    public int getItemCount() {
        return gameRecords.size();
    }


    public static class GameRecordRV_Holder extends RecyclerView.ViewHolder {

        protected TextView sport;
        protected TextView result;
        protected TextView date;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        private int id;

        public GameRecordRV_Holder(View v) {
            super(v);
            v.setTag(this);
            sport = (TextView) v.findViewById(R.id.gameRecordCV_SportTV);
            result = (TextView) v.findViewById(R.id.gameRecordCV_ResultTV);
            date = (TextView) v.findViewById(R.id.gameRecordCV_DateTV);
        }
    }
}

