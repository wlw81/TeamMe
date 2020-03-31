package de.pasligh.android.teamme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.TeamReactor;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter {


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a TeamSectionFragment (defined as a static inner class
        // below) with the page number as its lone argument.
        Fragment fragment = new TeamSectionFragment();
        Bundle args = new Bundle();
        args.putInt(TeamSectionFragment.ARG_SECTION_NUMBER, (position + 1));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        Iterator<PlayerAssignment> iterator = TeamReactor.getAssignments()
                .iterator();
        Set<Integer> setTeamNr = new HashSet<Integer>();
        while (iterator.hasNext()) {
            PlayerAssignment next = iterator.next();
            setTeamNr.add(next.getTeam());
        }
        return setTeamNr.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Team" + " " + TeamReactor.getAssignmentsByTeam(position + 1).get(0).getPlayer().getName();
    }
}