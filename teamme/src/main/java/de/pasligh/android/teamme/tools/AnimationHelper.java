package de.pasligh.android.teamme.tools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by Thomas on 25.02.2016.
 */
public class AnimationHelper {

    public static Animator reveal(View myView) {
        Animator anim = null;// get the center for the clipping circle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try{
                myView.setVisibility(View.VISIBLE);
                int cx = myView.getWidth() / 2;
                int cy = myView.getHeight() / 2;

                // get the final radius for the clipping circle
                float finalRadius = (float) Math.hypot(cx, cy);
                anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
                anim.start();
            }catch(IllegalStateException e){
                myView.setVisibility(View.VISIBLE);
            }
        }
        return anim;
    }


    public static boolean hide(final View p_view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // get the center for the clipping circle
                int cx = p_view.getWidth() / 2;
                int cy = p_view.getHeight() / 2;

                // get the initial radius for the clipping circle
                float initialRadius = (float) Math.hypot(cx, cy);

                // create the animation (the final radius is zero)
                Animator anim =
                        ViewAnimationUtils.createCircularReveal(p_view, cx, cy, initialRadius, 0);

                // make the view invisible when the animation is done
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        p_view.setVisibility(View.INVISIBLE);
                    }
                });

                // start the animation
                anim.start();
            } catch (Exception e) {
                Log.i(Flags.LOGTAG, e.getMessage());
                return false;
            }
            return true;
        } else {
            return false;
        }


    }

}
