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
        for (PlayerAssignemnt p : assignments) {
            if (p.isRevealed()) {
                assignmentsDone.add(p);
            }
        }

        return assignmentsDone;
    }

    public List<PlayerAssignemnt> getAssignments() {
        return assignments;
    }

    private final static List<PlayerAssignemnt> assignments = new ArrayList<PlayerAssignemnt>();
    CompoundButton.OnCheckedChangeListener checkedChangeListener;
    Typeface tf;
    ArrayAdapter spinnerAdapter;
    Map<String, Integer> mapStarsPerPlayer;
    Context ctxt;
    int maxplayers = -1;

    public PlayerSelectionRV_Adapter(Context p_ctxt, List<PlayerAssignemnt> p_playerAssignments, Typeface p_tf, ArrayAdapter p_spinnerAdapter, CompoundButton.OnCheckedChangeListener p_checkedChangeListener, Map<String, Integer> p_mapStarsPerPlayer, int p_maxPlayers) {
        ctxt = p_ctxt;
        assignments.addAll(p_playerAssignments);
        tf = p_tf;
        spinnerAdapter = p_spinnerAdapter;
        mapStarsPerPlayer = p_mapStarsPerPlayer;
        maxplayers = p_maxPlayers;
        checkedChangeListener = p_checkedChangeListener;
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
        pvh.switchPlayer.setOnCheckedChangeListener(checkedChangeListener);

        return pvh;
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    @Override
    public void onBindViewHolder(PlayerViewHolder holder, int position) {
        String name = assignments.get(position).getPlayer().getName();
        holder.playerName.setText(name);

        if (assignments.get(position).isRevealed()) {
            holder.showControls(true);
        } else {
            holder.showControls(false);
        }

        if (mapStarsPerPlayer.containsKey(name)) {
            int stars = mapStarsPerPlayer.get(assignments.get(position).getPlayer().getName());
            if (stars > 0) {
                holder.rating.setNumStars(stars);
                holder.rating.setMax(5);
            }
        }
        holder.spinner.setAdapter(spinnerAdapter);
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
                        Log.i(Flags.LOGTAG, "Selection " + assignments.get(position));
                        //mapSwitchHolder.get(buttonView).captainToggle.setVisibility(View.VISIBLE);
                        assignments.get(position).setRevealed(true);
                        notifyDataSetChanged();
                        holder.expandView();
                    } else {
                        buttonView.setChecked(false);
                    }
                } else {
                    Log.i(Flags.LOGTAG, "Unchecked " + assignments.get(position));
                    assignments.get(position).setRevealed(false);
                    notifyDataSetChanged();
                    holder.collapseView();
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
        int maxHeight = 0;

        public void showControls(boolean checked) {
            switchPlayer.setChecked(checked);
            if(checked){
                spinner.setVisibility(View.VISIBLE);
            }else{
                spinner.setVisibility(View.GONE);
            }
            //holder.captainToggle.setVisibility(visible);
        }

        public void collapseView() {
            showControls(false);
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
            showControls(true);
            ValueAnimator anim = ValueAnimator.ofInt(cv.getMeasuredHeightAndState(), maxHeight
            );
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
            switchPlayer.setTag(this);

            cv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    cv.getViewTreeObserver().removeOnPreDrawListener(this);
                    minHeight = cv.getHeight();
                    maxHeight = (int) (minHeight * 1.5);
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = minHeight;
                    cv.setLayoutParams(layoutParams);
                    return true;
                }
            });


        }
    }


}