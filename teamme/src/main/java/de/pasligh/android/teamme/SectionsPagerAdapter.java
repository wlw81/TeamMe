package de.pasligh.android.teamme;

import android.content.res.Resources;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.pasligh.android.teamme.objects.PlayerAssignment;
import de.pasligh.android.teamme.tools.TeamReactor;
import de.pasligh.android.teamme.tools.TeamView_Interface;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentStatePagerAdapter implements TeamView_Interface {


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
        TeamSectionFragment fragment = new TeamSectionFragment();
        Bundle args = new Bundle();
        args.putInt(TeamSectionFragment.ARG_SECTION_NUMBER, (position + 1));
        fragment.setArguments(args);
        fragment.getListener().add(this);
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
        List<PlayerAssignment> playerAssignments = TeamReactor.getAssignmentsByTeam(position + 1);
        if (!playerAssignments.isEmpty()) {
            return "Team" + " " + playerAssignments.get(0).getPlayer().getName();
        } else {
            return Resources.getSystem().getString(android.R.string.untitled);
        }
    }

    @Override
    public void notifyAdapter() {
        notifyDataSetChanged();
    }
}