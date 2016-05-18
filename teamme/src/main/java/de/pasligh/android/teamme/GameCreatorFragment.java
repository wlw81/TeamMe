package de.pasligh.android.teamme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.tools.Flags;

/**
 * A list fragment representing a list of games. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link GameStatisticsFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class GameCreatorFragment extends Fragment {

    private BackendFacade facade;


    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getContext());
        }
        return facade;
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GameCreatorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.activity_game_creator, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final GameRecord lastGameRecord = getFacade().getLastGamePlayed();
                if (null != lastGameRecord) {
                    int snackbarShowlenght = Snackbar.LENGTH_INDEFINITE;
                    String caption = getString(R.string.log);
                    if(!getFacade().getScores(lastGameRecord.getId()).isEmpty()){
                        snackbarShowlenght = Snackbar.LENGTH_LONG;
                         caption = getString(R.string.log_complete);
                    }

                    java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());

                    Snackbar
                            .make(getView(), lastGameRecord.getSport() + " " + dateFormat.format(lastGameRecord.getStartedAt()), snackbarShowlenght)
                            .setAction(caption, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent reportScores = new Intent(getContext(),
                                            ReportScoresActivity.class);
                                    reportScores.putExtra(Flags.GAME_ID, lastGameRecord.getId());
                                    startActivity(reportScores);
                                }
                            })
                            .show();
                }
            }
        }, 2000);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
