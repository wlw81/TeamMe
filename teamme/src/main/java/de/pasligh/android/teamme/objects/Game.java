package de.pasligh.android.teamme.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Game implements Serializable {

    private static final long serialVersionUID = -3420071497019663100L;
    private Date startedAt;
    private List<PlayerAssignemnt> assignments;

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    private String sport;

    public Game() {
        super();
    }

    public Game(Set<PlayerAssignemnt> p_assignments) {
        super();
        assignments = new ArrayList<PlayerAssignemnt>();
        startedAt = new Date();
        sport = "unknown";
        Iterator<PlayerAssignemnt> iterator = p_assignments.iterator();
        while (iterator.hasNext()) {
            assignments.add(iterator.next());
        }
    }

    /**
     * @return the startedAt
     */
    public Date getStartedAt() {
        return startedAt;
    }

    /**
     * @return the assignments
     */
    public List<PlayerAssignemnt> getAssignments() {
        return assignments;
    }

}
