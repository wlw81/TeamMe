package de.pasligh.android.teamme.tools;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.PlayerAssignemnt;

/**
 * Created by Thomas on 06.02.2016.
 */
public class PlayerSelectionRV_Adapter extends RecyclerView.Adapter<PlayerSelectionRV_Adapter.PlayerViewHolder> {


    public List<PlayerAssignemnt> getAssignmentsDone() {

        List<PlayerAssignemnt> assignmentsDone = new ArrayList<>();
        for (PlayerAssignemnt p : assignmentsBlank) {
            if (p.isRevealed()) {
                assignmentsDone.add(p);
            }
        }

        return assignmentsDone;
    }

    private final static List<PlayerAssignemnt> assignmentsBlank = new ArrayList<PlayerAssignemnt>();

    Typeface tf;
    ArrayAdapter spinnerAdapter;
    Map<String, Integer> mapStarsPerPlayer;
    Context ctxt;
    int maxplayers = -1;

    public PlayerSelectionRV_Adapter(Context p_ctxt, List<PlayerAssignemnt> p_playerAssignments, Typeface p_tf, ArrayAdapter p_spinnerAdapter, Map<String, Integer> p_mapStarsPerPlayer, int p_maxPlayers) {
        ctxt = p_ctxt;
        assignmentsBlank.addAll(p_playerAssignments);
        tf = p_tf;
        spinnerAdapter = p_spinnerAdapter;
        mapStarsPerPlayer = p_mapStarsPerPlayer;
        maxplayers = p_maxPlayers;

    }

    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_player_selection, parent, false);
        final PlayerViewHolder pvh = new PlayerViewHolder(v);

        pvh.playerName.setTypeface(tf);
        pvh.spinner.setSelection(0);
        pvh.spinner.setVisibility(View.GONE);
        pvh.rating.setEnabled(false);
        pvh.captainToggle.setVisibility(View.GONE);

        return pvh;
    }

    @Override
    public int getItemCount() {
        return assignmentsBlank.size();
    }

    @Override
    public void onBindViewHolder(PlayerViewHolder holder, int position) {
        String name = assignmentsBlank.get(position).getPlayer().getName();
        holder.playerName.setText(name);

        if (assignmentsBlank.get(position).isRevealed()) {
            showControls(holder, true, View.VISIBLE);
        } else {
            showControls(holder, false, View.GONE);
        }

        if (mapStarsPerPlayer.containsKey(name)) {
            int stars = mapStarsPerPlayer.get(assignmentsBlank.get(position).getPlayer().getName());
            if (stars > 0) {
                holder.rating.setNumStars(stars);
                holder.rating.setMax(5);
            }
        }
        holder.spinner.setAdapter(spinnerAdapter);
        holder.switchPlayer.setOnCheckedChangeListener(new MyOnCheckListener(holder, position));
    }

    private void showControls(PlayerViewHolder holder, boolean checked, int visible) {
        if( visible == View.GONE){
            Log.i(Flags.LOGTAG, "Hide "+holder.playerName.getText().toString());
        }
        holder.switchPlayer.setChecked(checked);
        holder.spinner.setVisibility(visible);
        holder.captainToggle.setVisibility(visible);
    }

    class MyOnCheckListener implements CompoundButton.OnCheckedChangeListener {

        int position = -1;
        PlayerViewHolder holder;

        public MyOnCheckListener(PlayerViewHolder p_holder, int p_intPosition) {
            position = p_intPosition;
            holder = p_holder;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (buttonView.getId() == R.id.playerSelectionCV_Switch) {
                if (isChecked) {
                    if (getAssignmentsDone().size() < maxplayers) {
                        Log.i(Flags.LOGTAG, "Selection " + assignmentsBlank.get(position));
                        //mapSwitchHolder.get(buttonView).captainToggle.setVisibility(View.VISIBLE);
                        assignmentsBlank.get(position).setRevealed(true);
                        showControls(holder, true, View.VISIBLE);
                        holder.expandView();
                    } else {
                        buttonView.setChecked(false);
                    }
                } else {
                    Log.i(Flags.LOGTAG, "Unchecked " + assignmentsBlank.get(position));
                    assignmentsBlank.get(position).setRevealed(false);
                    holder.collapseView();
                    showControls(holder, false, View.GONE);
                }
            } else if (buttonView.getId() == R.id.playerSelectionCV_CaptainToggle) {
                {
                    // todo
                }
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView playerName;
        Spinner spinner;
        RatingBar rating;
        Switch switchPlayer;
        View item;
        ToggleButton captainToggle;

        int minHeight = 0;

        public void collapseView() {

            ValueAnimator anim = ValueAnimator.ofInt(cv.getMeasuredHeightAndState(),
                    minHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = val;
                    cv.setLayoutParams(layoutParams);

                }
            });
            anim.start();
        }

        public void expandView() {

            ValueAnimator anim = ValueAnimator.ofInt(cv.getMeasuredHeightAndState(),
                    (int) (minHeight * 1.5));
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = val;
                    cv.setLayoutParams(layoutParams);
                }
            });
            anim.start();

        }

        PlayerViewHolder(View itemView) {
            super(itemView);
            item = itemView;
            cv = (CardView) itemView.findViewById(R.id.playerSelectionCV);
            playerName = (TextView) itemView.findViewById(R.id.playerSelectionCV_NameTV);
            spinner = ((Spinner) itemView.findViewById(R.id.playerSelectionCV_TeamSpinner));
            rating = ((RatingBar) itemView.findViewById(R.id.playerSelectionCV_RatingBar));
            switchPlayer = ((Switch) itemView.findViewById(R.id.playerSelectionCV_Switch));
            captainToggle = ((ToggleButton) itemView.findViewById(R.id.playerSelectionCV_CaptainToggle));

            cv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    cv.getViewTreeObserver().removeOnPreDrawListener(this);
                    minHeight = cv.getHeight();
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = minHeight;
                    cv.setLayoutParams(layoutParams);
                    return true;
                }
            });


        }
    }


}