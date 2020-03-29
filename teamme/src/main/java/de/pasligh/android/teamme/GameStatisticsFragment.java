package de.pasligh.android.teamme;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.pasligh.android.teamme.backend.BackendFacade;
import de.pasligh.android.teamme.objects.PlayerAssignment;

/**
 * A fragment representing a single game detail screen. This fragment is either
 * contained in a {@link GameCreatorActivity} in two-pane mode (on tablets) or a
 * {@link GameStatisticsActivity} on handsets.
 */
public class GameStatisticsFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	private BackendFacade facade;
	private GraphicalView mChart;

	/** Colors to be used for the pie slices. */
	private static int[] COLORS = new int[] { Color.parseColor("#669900"),
			Color.parseColor("#0099CC"), Color.parseColor("#FF8800"),
			Color.parseColor("#CC0000") };

	/** The main series that will include all the data. */
	private CategorySeries mSeries = new CategorySeries("Players");
	/** The main renderer for the main dataset. */
	private DefaultRenderer mRenderer = new DefaultRenderer();

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public GameStatisticsFragment() {
	}

	private void initChart() {
		// set the start angle for the first slice in the pie chart
		mRenderer.setStartAngle(180);
		mRenderer.setLabelsTextSize(24);
		mRenderer.setLegendTextSize(24);
		// display values on the pie slices
		mRenderer.setDisplayValues(true);
	}

	private void addPlayerData(int p_gameCount) {
		int minimumGames = p_gameCount / 8;
		Map<String, Integer> gameCount_to_Player = new HashMap<String, Integer>();
		List<PlayerAssignment> assignments = getFacade().getAllAssignments();
		if (!assignments.isEmpty()) {
			for (PlayerAssignment addToCount : assignments) {
				if (gameCount_to_Player.containsKey(addToCount.getPlayer()
						.getName())) {
					gameCount_to_Player.put(addToCount.getPlayer().getName(),
							1 + gameCount_to_Player.get(addToCount.getPlayer()
									.getName()));
				} else {
					gameCount_to_Player
							.put(addToCount.getPlayer().getName(), 1);
				}
			}

			for (String playerName : gameCount_to_Player.keySet()) {
				int playerGameCount = gameCount_to_Player.get(playerName);
				if (playerGameCount > minimumGames) {
					mSeries.add(playerName, playerGameCount);
					SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
					renderer.setColor(COLORS[(mSeries.getItemCount() - 1)
							% COLORS.length]);
					mRenderer.addSeriesRenderer(renderer);
				}

			}

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_game_statistics,
				container, false);

		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/Roboto-Condensed-Regular.ttf");
		((TextView) rootView.findViewById(R.id.StatisticsLabelTextView))
				.setTypeface(tf);

		String gamecount;
		String date;
		int gamecountNumber = getFacade().getGameCount();
		if (gamecountNumber > 0) {
			gamecount = String.valueOf(gamecountNumber);
			date = DateFormat.getDateFormat(
					getActivity().getApplicationContext()).format(
					getFacade().getLastTimePlayed());
			((TextView) rootView.findViewById(R.id.GameCountTextView))
					.setText(gamecount);
			((TextView) rootView.findViewById(R.id.GameDateTextView))
					.setText(date);
		}else{
			((TextView) rootView.findViewById(R.id.GameCountTextView))
			.setText("0");
		}

		LinearLayout layout = (LinearLayout) rootView
				.findViewById(R.id.ChartLayout);
		if (mChart == null) {
			initChart();
			addPlayerData(gamecountNumber);
			mChart = ChartFactory.getPieChartView(getActivity()
					.getApplicationContext(), mSeries, mRenderer);
			layout.addView(mChart);
		} else {
			mChart.repaint();
		}

		return rootView;
	}

	/**
	 * @return the facade
	 */
	public BackendFacade getFacade() {
		if (null == facade) {
			facade = new BackendFacade(getActivity().getApplicationContext());
		}
		return facade;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getFacade().getObjDB_API().close();
	}
}
