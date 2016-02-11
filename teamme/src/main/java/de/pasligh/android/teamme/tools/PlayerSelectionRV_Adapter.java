package de.pasligh.android.teamme.tools;

import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
public class PlayerSelectionRV_Adapter extends RecyclerView.Adapter<PlayerSelectionRV_Adapter.PlayerViewHolder> implements CompoundButton.OnCheckedChangeListener {

    public List<PlayerAssignemnt> getAssignmentsDone() {
        return assignmentsDone;
    }

    List<PlayerAssignemnt> assignmentsBlank;
    List<PlayerAssignemnt> assignmentsDone;
    Typeface tf;
    Map<Switch, PlayerViewHolder> mapSwitchHolder;
    Map<PlayerViewHolder, PlayerAssignemnt> mapAssignments;
    ArrayAdapter spinnerAdapter;
    Map<String, Integer> mapStarsPerPlayer;

    public PlayerSelectionRV_Adapter(List<PlayerAssignemnt> persons, Typeface p_tf, ArrayAdapter p_spinnerAdapter, Map<String, Integer> p_mapStarsPerPlayer) {
        this.assignmentsBlank = persons;
        tf = p_tf;
        mapSwitchHolder = new HashMap<Switch, PlayerViewHolder>();
        spinnerAdapter = p_spinnerAdapter;
        mapAssignments = new HashMap<>();
        assignmentsDone = new ArrayList<>();
        mapStarsPerPlayer = p_mapStarsPerPlayer;
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
        pvh.toggle.setOnCheckedChangeListener(this);

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
        if(mapStarsPerPlayer.containsKey(name)){
            int stars = mapStarsPerPlayer.get(assignmentsBlank.get(position).getPlayer().getName());
            if(stars > 0){
                holder.rating.setNumStars(stars);
                holder.rating.setMax(5);
            }
        }
        holder.spinner.setAdapter(spinnerAdapter);
        mapSwitchHolder.put(holder.switchPlayer, holder);
        mapAssignments.put(holder, assignmentsBlank.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.playerSelectionCV_Switch) {
            if (isChecked) {
                int selectedSwitches = 0;
                for (Switch s : mapSwitchHolder.keySet()) {
                    if (s.isChecked()) {
                        selectedSwitches++;
                    }
                }

                if (selectedSwitches <= spinnerAdapter.getCount() - 1) {
                    mapSwitchHolder.get(buttonView).spinner.setVisibility(View.VISIBLE);
                    //mapSwitchHolder.get(buttonView).toggle.setVisibility(View.VISIBLE);
                    mapSwitchHolder.get(buttonView).rating.setVisibility(View.VISIBLE);
                    assignmentsDone.add(mapAssignments.get(mapSwitchHolder.get(buttonView)));
                    mapSwitchHolder.get(buttonView).expandView();
                } else {
                    buttonView.setChecked(false);
                }
            } else {
                mapSwitchHolder.get(buttonView).spinner.setVisibility(View.GONE);
                mapSwitchHolder.get(buttonView).toggle.setVisibility(View.GONE);
                mapSwitchHolder.get(buttonView).rating.setVisibility(View.GONE);
                assignmentsDone.remove(mapSwitchHolder.get(buttonView));
                mapSwitchHolder.get(buttonView).collapseView();
            }
        } else if (buttonView.getId() == R.id.playerSelectionCV_CaptainToggle) {
            {
                // todo
            }
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

        int minHeight = 0;

        private void toggleCardViewnHeight() {

            if (cv.getHeight() == minHeight) {
                // expand
                expandView(); //'height' is the height of screen which we have measured already.
            } else {
                // collapse
                collapseView();

            }
        }

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
            toggle = ((ToggleButton) itemView.findViewById(R.id.playerSelectionCV_CaptainToggle));


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