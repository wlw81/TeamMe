package de.pasligh.android.teamme.objects;

import java.io.Serializable;

public class Player implements Serializable{

	private static final long serialVersionUID = -7059822969888234480L;
	private String name;

	@Override
	public String toString() {
		return getName();
	}

	public Player(String p_name) {
		name = p_name;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object o) {
		return getName().equals(((Player)o).getName());
	}
	
}
