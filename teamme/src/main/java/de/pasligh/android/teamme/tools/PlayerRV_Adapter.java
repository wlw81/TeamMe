package de.pasligh.android.teamme.tools;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.Player;

/**
 * Created by Thomas on 03.06.2016.
 */
public class PlayerRV_Adapter extends RecyclerView.Adapter<PlayerRV_Adapter.PlayerHolder> {

    public List<Player> getPlayers() {
        return players;
    }

    List<Player> players;

    View.OnClickListener listener;

    public PlayerRV_Adapter(View.OnClickListener p_listener, List<Player> p_players) {
        players = p_players;
        listener = p_listener;
    }

    public PlayerRV_Adapter(List<Player> p_players) {
        players = p_players;
    }

    @Override
    public PlayerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_player, parent, false);
        v.setOnClickListener(listener);
        return new PlayerHolder(v);
    }

    @Override
    public void onBindViewHolder(PlayerHolder holder, int position) {
        Player ci = players.get(position);
        holder.name.setText(ci.getName());
    }

    @Override
    public int getItemCount() {
        return players.size();
    }


    public static class PlayerHolder extends RecyclerView.ViewHolder {

        public TextView getName() {
            return name;
        }

        public void setName(TextView name) {
            this.name = name;
        }

        protected TextView name;

        public PlayerHolder(View v) {
            super(v);
            v.setTag(this);
            name = (TextView) v.findViewById(R.id.playerCV_NameTV);
        }

    }

}
