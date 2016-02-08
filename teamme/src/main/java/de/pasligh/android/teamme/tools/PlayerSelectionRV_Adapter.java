package de.pasligh.android.teamme.tools;

import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.Player;

/**
 * Created by Thomas on 06.02.2016.
 */
public class PlayerSelectionRV_Adapter extends RecyclerView.Adapter<PlayerSelectionRV_Adapter.PlayerViewHolder> implements CompoundButton.OnCheckedChangeListener {

    List<Player> players;
    Typeface tf;
    Map<Switch, PlayerViewHolder> map;
    ArrayAdapter spinnerAdapter;

    public Set<Player> getPreSelected() {
        return preSelected;
    }

    Set<Player> preSelected;

    public PlayerSelectionRV_Adapter(List<Player> persons, Typeface p_tf, ArrayAdapter p_spinnerAdapter) {
        this.players = persons;
        tf = p_tf;
        map = new HashMap<Switch, PlayerViewHolder>();
        preSelected = new HashSet<Player>();
        spinnerAdapter = p_spinnerAdapter;
    }

    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_player_selection, parent, false);
        PlayerViewHolder pvh = new PlayerViewHolder(v);

        pvh.playerName.setTypeface(tf);
        pvh.spinner.setSelection(0);
        pvh.spinner.setVisibility(View.GONE);
        pvh.rating.setEnabled(false);
        pvh.rating.setVisibility(View.GONE);
        pvh.toggle.setVisibility(View.GONE);
        pvh.switchPlayer.setOnCheckedChangeListener(this);

        return pvh;
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    @Override
    public void onBindViewHolder(PlayerViewHolder holder, int position) {
        holder.playerName.setText(players.get(position).getName());
        holder.spinner.setAdapter(spinnerAdapter);
        map.put(holder.switchPlayer, holder);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
            map.get(buttonView).spinner.setVisibility(View.VISIBLE);
            map.get(buttonView).toggle.setVisibility(View.VISIBLE);
            map.get(buttonView).rating.setVisibility(View.VISIBLE);
        } else {
            map.get(buttonView).spinner.setVisibility(View.GONE);
            map.get(buttonView).toggle.setVisibility(View.GONE);
            map.get(buttonView).rating.setVisibility(View.GONE);
        }
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView playerName;
        Spinner spinner;
        RatingBar rating;
        Switch switchPlayer;
        View item;
        ToggleButton toggle;

        PlayerViewHolder(View itemView) {
            super(itemView);
            item = itemView;
            cv = (CardView) itemView.findViewById(R.id.playerSelectionCV);
            playerName = (TextView) itemView.findViewById(R.id.playerSelectionCV_NameTV);
            spinner = ((Spinner) itemView.findViewById(R.id.playerSelectionCV_TeamSpinner));
            rating = ((RatingBar) itemView.findViewById(R.id.playerSelectionCV_RatingBar));
            switchPlayer = ((Switch) itemView.findViewById(R.id.playerSelectionCV_Switch));
            toggle = ((ToggleButton) itemView.findViewById(R.id.playerSelectionCV_CaptainToggle));
        }
    }

}