package de.pasligh.android.teamme.objects;

import java.io.Serializable;

public class PlayerAssignemnt implements Serializable{

	private static final long serialVersionUID = 1184064512639132414L;
	
	private int id;
	private int orderNumber;
	private int team;
	private int game;
	private Player player;
	private boolean revealed;

	@Override
	public String toString() {
		String s =  "#"+getId()+ " Team "+getTeam();
		if(getPlayer() != null){
			s = s.concat(" "+getPlayer().getName());
		}
		s = s.concat(" Revealed: "+isRevealed());
		return s;
	}

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
	 * @return the revealed
	 */
	public boolean isRevealed() {
		return revealed;
	}

	/**
	 * @param revealed
	 *            the revealed to set
	 */
	public void setRevealed(boolean revealed) {
		this.revealed = revealed;
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
