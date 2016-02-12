package de.pasligh.android.teamme.tools;

import android.content.Context;
import android.widget.Button;

import de.pasligh.android.teamme.objects.Score;

/**
 * Created by Thomas on 12.02.2016.
 */
public class ScoreButton extends Button {

    public Score getMyScore() {
        return myScore;
    }

    public void setMyScore(Score myScore) {
        this.myScore = myScore;
    }

    Score myScore;

    public ScoreButton(Context context, Score p_score) {
        super(context, null);
        setMyScore(p_score);
    }

}
