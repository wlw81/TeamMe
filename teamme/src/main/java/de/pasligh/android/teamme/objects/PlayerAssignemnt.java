package de.pasligh.android.teamme.objects;

import java.io.Serializable;

public class PlayerAssignemnt implements Serializable{

	private static final long serialVersionUID = 1184064512639132414L;
	
	private int id;
	private int orderNumber;
	private int team;
	private int game;
	private Player player;
	private boolean assigned;

	/**
	 * @return the orderNumber
	 */
	public int getOrderNumber() {
		return orderNumber;
	}

	/**
	 * @param orderNumber
	 *            the orderNumber to set
	 */
	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	/**
	 * @return the team
	 */
	public int getTeam() {
		return team;
	}

	/**
	 * @param team
	 *            the team to set
	 */
	public void setTeam(int team) {
		this.team = team;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @param player
	 *            the player to set
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * @return the assigned
	 */
	public boolean isAssigned() {
		return assigned;
	}

	/**
	 * @param assigned
	 *            the assigned to set
	 */
	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	/**
	 * @return the game
	 */
	public int getGame() {
		return game;
	}

	/**
	 * @param game the game to set
	 */
	public void setGame(int game) {
		this.game = game;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

}
