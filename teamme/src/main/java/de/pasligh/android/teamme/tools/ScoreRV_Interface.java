package de.pasligh.android.teamme.tools;

import java.util.List;

import de.pasligh.android.teamme.objects.Score;

/**
 * Created by Thomas on 10.02.2016.
 */
public interface ScoreRV_Interface {

    public void recieveHolder(ScoreRV_Adapter.RoundResultViewHolder p_holder, List<Score> lisScore  );

}
