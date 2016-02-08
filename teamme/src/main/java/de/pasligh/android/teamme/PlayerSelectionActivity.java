package de.pasligh.android.teamme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.Player;
import de.pasligh.android.teamme.tools.Flags;
import de.pasligh.android.teamme.tools.PlayerSelectionRV_Adapter;
import de.pasligh.android.teamme.tools.TeamReactor;

public class PlayerSelectionActivity extends Activity implements View.OnClickListener {

    private BackendFacade facade;
    PlayerSelectionRV_Adapter adapter;

    public BackendFacade getFacade() {
        if (null == facade) {
            facade = new BackendFacade(getApplicationContext());
        }
        return facade;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_selection);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.playersAssignedFAB);
        fab.setOnClickListener(this);

        RecyclerView rv = (RecyclerView) findViewById(R.id.playerSelectionRV);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);

        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.random));
        for (int i = 1; i <= getIntent().getIntExtra(Flags.TEAMCOUNT, -1); i++) {
            list.add(getString(R.string.team) + " " + i);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, list);

        adapter = new PlayerSelectionRV_Adapter(getFacade().getPlayers(), Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf"), dataAdapter);
        rv.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

        if (adapter.getPreSelected().isEmpty()) {
            TeamReactor.decideTeams(getIntent().getIntExtra(Flags.TEAMCOUNT, -1), getIntent().getIntExtra(Flags.PLAYERCOUNT, -1));
            Intent callChooser = new Intent(getApplicationContext(),
                    TeamChooser.class);
            callChooser.putExtra(Flags.SPORT, getIntent().getStringExtra(Flags.SPORT));
            startActivity(callChooser);
        }

    }

}
