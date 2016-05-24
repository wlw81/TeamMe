package de.pasligh.android.teamme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.view.MenuItem;


import java.util.ArrayList;
import java.util.List;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.GameRecord;
import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.Flags;

/**
 * An activity representing a list of Games. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link GameRecordDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class GameRecordListActivity extends AppCompatActivity {


    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private BackendFacade facade;
    private long gameId = -1;
    private SimpleItemRecyclerViewAdapter myAdapter;

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamerecord_list);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        View recyclerView = findViewById(R.id.gamerecord_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.gamerecord_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            Intent backHome = new Intent(getApplicationContext(),
                    GameCreatorActivity.class);
            startActivity(backHome);
            navigateUpToFromChild(GameRecordListActivity.this
                    , backHome);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        myAdapter = new SimpleItemRecyclerViewAdapter(getFacade().getGames());
        recyclerView.setAdapter(myAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != getIntent().getExtras()) {
            gameId = getIntent().getExtras().getLong(Flags.GAME_ID);
            if (gameId >= 0) {
                jump2Game(String.valueOf(gameId));
                gameId = -1;
            }
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<GameRecord> mValues;
        private final List<ViewHolder> mHolder;
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());

        public SimpleItemRecyclerViewAdapter(List<GameRecord> items) {
            mValues = items;
            mHolder = new ArrayList<ViewHolder>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gamerecord_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(String.valueOf(holder.mItem.getId()));

            List<PlayerAssignment> assignments = getFacade().getAssignments(holder.mItem.getId());

            String caption = holder.mItem.getSport() + " " + dateFormat.format(holder.mItem.getStartedAt());
            if (assignments != null) {
                caption += " (" + getFacade().getAssignments(holder.mItem.getId()).size() + " " + getString(R.string.player) + ")";
            }

            holder.mContentView.setText(caption);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = String.valueOf(holder.mItem.getId());
                    jump2Game(id);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public GameRecord mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    private void jump2Game(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(GameRecordDetailFragment.ARG_ITEM_ID, id);
            GameRecordDetailFragment fragment = new GameRecordDetailFragment();
            fragment.setArguments(arguments);
            fragment.setApplicationContext(getApplicationContext());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.gamerecord_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(getApplicationContext(), GameRecordDetailActivity.class);
            intent.putExtra(GameRecordDetailFragment.ARG_ITEM_ID, id);

            startActivity(intent);
        }
    }
}
