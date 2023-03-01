package de.pasligh.android.teamme.tools;

import android.graphics.Typeface;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.R;
import de.pasligh.android.teamme.objects.PlayerAssignment;

/**
 * Created by Thomas on 06.02.2016.
 */
public class PlayerSelectionRV_Adapter extends RecyclerView.Adapter<PlayerSelectionRV_Adapter.PlayerViewHolder> implements AdapterView.OnItemSelectedListener {

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
    private PlayerSelectionRV_Interface checkedChangeListener;
    private Typeface tf;
    private ArrayAdapter spinnerAdapter;
    private Map<String, Integer> mapStarsPerPlayer;

    public Map<String, Integer> getMapStarsPerPlayer() {
        return mapStarsPerPlayer;
    }

    public PlayerSelectionRV_Adapter(List<PlayerAssignment> p_playerAssignments, Typeface p_tf, ArrayAdapter p_spinnerAdapter, PlayerSelectionRV_Interface p_checkedChangeListener, Map<String, Integer> p_mapStarsPerPlayer) {
        assignments.clear();
        assignments.addAll(p_playerAssignments);
        for (PlayerAssignment assignmentFromReactor : TeamReactor.getAssignments()) {
            if (null != assignmentFromReactor.getPlayer()) {
                String name = assignmentFromReactor.getPlayer().getName();
                for (PlayerAssignment assignmentLocal : assignments) {
                    if (assignmentLocal.getPlayer().getName().equals(name)) {
                        assignmentLocal.setRevealed(true);
                        Log.i(Flags.LOGTAG, "Restore: " + assignmentLocal.toString());
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
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_selection_item, parent, false);
        final PlayerViewHolder pvh = new PlayerViewHolder(v);

        pvh.rating.setEnabled(false);
        pvh.switchPlayer.setOnCheckedChangeListener(checkedChangeListener);
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
        if (!holder.switchPlayer.isChecked()) {
            collapseView(holder);
        } else {
            expandView(holder);
        }

        if (mapStarsPerPlayer.containsKey(name)) {
            int stars = mapStarsPerPlayer.get(assignments.get(position).getPlayer().getName());
            if (stars > 0) {
                holder.rating.setRating(stars);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static void collapseView(final PlayerViewHolder p_pvh) {
        p_pvh.showControls(false);
    }

    public static void expandView(final PlayerViewHolder p_pvh) {
        p_pvh.showControls(true);
    }

    public class PlayerViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout cv;
        private TextView playerName;
        private RatingBar rating;
        private CheckBox switchPlayer;
        private String debugId;

        public void showControls(boolean checked) {
            getAssignments().get(getAdapterPosition()).setRevealed(checked);
        }

        PlayerViewHolder(View itemView) {
            super(itemView);
            cv = (LinearLayout) itemView.findViewById(R.id.playerSelectionLL);
            playerName = (TextView) itemView.findViewById(R.id.playerSelectionCV_NameTV);
            rating = ((RatingBar) itemView.findViewById(R.id.playerSelectionCV_RatingBar));
            switchPlayer = ((CheckBox) itemView.findViewById(R.id.playerSelectionCB_Switch));
            cv.setTag(this);
            switchPlayer.setTag(this);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        PlayerViewHolder pvh = (PlayerViewHolder) arg0.getTag();
        getAssignments().get(pvh.getAdapterPosition()).setTeam(arg2);
        Log.i(Flags.LOGTAG, "Team change: " + getAssignments().get(pvh.getAdapterPosition()) + " now in Team " + arg2); //todo needs new implementation!
        Log.d(Flags.LOGTAG, Log.getStackTraceString(new Exception()));
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {

    }
}