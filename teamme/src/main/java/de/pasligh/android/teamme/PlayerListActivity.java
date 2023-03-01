package de.pasligh.android.teamme;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.databinding.ActivityPlayerListBinding;
import de.pasligh.android.teamme.tools.PlayerRV_Adapter;

/**
 * An activity representing a list of Players. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PlayerDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PlayerListActivity extends AppCompatActivity implements View.OnClickListener {

    private PlayerRV_Adapter myAdapter;
    private BackendFacade facade;

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPlayerListBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_player_list);
        //setContentView(R.layout.activity_player_list);
        // Show the Up button in the action bar.
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.player);
        }

        View recyclerView = findViewById(R.id.player_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        binding.setPlayers(myAdapter.getPlayers());

        if (findViewById(R.id.player_detail_container) != null) {
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
            navigateUpToFromChild(PlayerListActivity.this
                    , backHome);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        myAdapter = new PlayerRV_Adapter(this, getFacade().getPlayers());
        recyclerView.setAdapter(myAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.player_detail_FAB) {
            // perhaps later
        } else {
            PlayerRV_Adapter.PlayerHolder
                    holder = (PlayerRV_Adapter.PlayerHolder) v.getTag();
            String id = String.valueOf(holder.getName().getText());
            jump2Player(id);
        }

    }

    private void jump2Player(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PlayerDetailFragment.ARG_ITEM_ID, id);
            PlayerDetailFragment fragment = new PlayerDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.player_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(getApplicationContext(), PlayerDetailActivity.class);
            intent.putExtra(PlayerDetailFragment.ARG_ITEM_ID, id);

            startActivity(intent);
        }
    }
}
