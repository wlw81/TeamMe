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
import android.widget.AdapterView;
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
import de.pasligh.android.teamme.objects.PlayerAssignment;

/**
 * Created by Thomas on 06.02.2016.
 */
public class PlayerSelectionRV_Adapter extends RecyclerView.Adapter<PlayerSelectionRV_Adapter.PlayerViewHolder> implements AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

    public List<PlayerAssignment> getAssignmentsDone() {

        List<PlayerAssignment> assignmentsDone = new ArrayList<>();
        for (PlayerAssignment p : assignments) {
            if (p.isRevealed()) {
                assignmentsDone.add(p);
            }
        }

        return assignmentsDone;
    }

    public List<PlayerAssignment> getAssignments() {
        return assignments;
    }

    private final static List<PlayerAssignment> assignments = new ArrayList<PlayerAssignment>();
    PlayerSelectionRV_Interface checkedChangeListener;
    Typeface tf;
    ArrayAdapter spinnerAdapter;
    Map<String, Integer> mapStarsPerPlayer;
    Context ctxt;

    public Map<String, Integer> getMapStarsPerPlayer() {
        return mapStarsPerPlayer;
    }

    public PlayerSelectionRV_Adapter(Context p_ctxt, List<PlayerAssignment> p_playerAssignments, Typeface p_tf, ArrayAdapter p_spinnerAdapter, PlayerSelectionRV_Interface p_checkedChangeListener, Map<String, Integer> p_mapStarsPerPlayer) {
        ctxt = p_ctxt;
        assignments.clear();
        assignments.addAll(p_playerAssignments);
        for (PlayerAssignment assignmentFromReactor : TeamReactor.getAssignments()) {
            if(null != assignmentFromReactor.getPlayer()){
                String name = assignmentFromReactor.getPlayer().getName();
                for (PlayerAssignment assignmentLocal : assignments) {
                    if (assignmentLocal.getPlayer().getName().equals(name)) {
                        assignmentLocal.setRevealed(true);
                        Log.i(Flags.LOGTAG, "Restore: " +assignmentLocal.toString());
                    }
                }
            }
        }
        tf = p_tf;
        spinnerAdapter = p_spinnerAdapter;
        mapStarsPerPlayer = p_mapStarsPerPlayer;
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
        pvh.captainToggle.setOnCheckedChangeListener(PlayerSelectionRV_Adapter.this);
        pvh.switchPlayer.setOnCheckedChangeListener(checkedChangeListener);
        pvh.spinner.setOnItemSelectedListener(PlayerSelectionRV_Adapter.this);
        pvh.cv.setOnLongClickListener(checkedChangeListener);

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
        holder.switchPlayer.setChecked(assignments.get(position).isRevealed());
        if(holder.switchPlayer.isChecked()){
            holder.expandView();
        }

        if (mapStarsPerPlayer.containsKey(name)) {
            int stars = mapStarsPerPlayer.get(assignments.get(position).getPlayer().getName());
            if (stars > 0) {
                holder.rating.setRating(stars);
            }
        }
        holder.spinner.setAdapter(spinnerAdapter);
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
            if (checked) {
                spinner.setVisibility(View.VISIBLE);
                captainToggle.setVisibility(View.VISIBLE);
            } else {
                spinner.setVisibility(View.GONE);
                captainToggle.setVisibility(View.GONE);
            }
        }

        public void collapseView() {
            showControls(false);
            if (cv.getLayoutParams().height == maxHeight) {
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
        }

        public void expandView() {
            showControls(true);
            if (cv.getLayoutParams().height == minHeight) {
                {
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
            }
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

            cv.setTag(this);
            captainToggle.setTag(this);
            switchPlayer.setTag(this);
            spinner.setTag(this);

            cv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    cv.getViewTreeObserver().removeOnPreDrawListener(this);
                    minHeight = cv.getHeight();
                    maxHeight = (int) (minHeight * 2);
                    ViewGroup.LayoutParams layoutParams = cv.getLayoutParams();
                    layoutParams.height = minHeight;
                    cv.setLayoutParams(layoutParams);
                    return true;
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        PlayerViewHolder pvh = (PlayerViewHolder) arg0.getTag();
        getAssignments().get(pvh.getAdapterPosition()).setTeam(arg2);
        Log.i(Flags.LOGTAG, "Team change: " + getAssignments().get(pvh.getAdapterPosition()) + " now in Team " + arg2);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        PlayerViewHolder pvh = (PlayerViewHolder) buttonView.getTag();
        if (isChecked) {
            getAssignments().get(pvh.getAdapterPosition()).setOrderNumber(1);
            Log.i(Flags.LOGTAG, "Team change: " + getAssignments().get(pvh.getAdapterPosition()).getPlayer() + " is now el capitano!");
        } else {
            getAssignments().get(pvh.getAdapterPosition()).setOrderNumber(-1);
            Log.i(Flags.LOGTAG, "Team change: " + getAssignments().get(pvh.getAdapterPosition()).getPlayer() + " is not the captain anymore");
        }

    }
}