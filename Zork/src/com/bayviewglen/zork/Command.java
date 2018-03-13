package com.bayviewglen.zork;
/** "Command" Class - part of the "Zork" game.
 * 
 * Original Author:  Michael Kolling
 * Original Version: 1.0
 * Original Date:    July 1999
 * 
 * Current Authors: Kirill Tregubov, Zacharia Burrafato, Andrew Douglas, Alim Halani
 * Current Version: 0.1-alpha
 * Current Date:    March 2018
 *
 * This class holds information about a command that was issued by the user.
 * A command currently consists of two strings: a command word and a second
 * word (for example, if the command was "take map", then the two strings
 * obviously are "take" and "map").
 * 
 * The way this is used is: Commands are already checked for being valid
 * command words. If the user entered an invalid command (a word that is not
 * known) then the command word is <null>.
 *
 * If the command had only one word, then the second word is <null>.
 *
 * The second word is not checked at the moment. It can be anything. If this
 * game is extended to deal with items, then the second part of the command
 * should probably be changed to be an item rather than a String.
 */

class Command {
	private String commandWord;
	private String secondWord;
	private String thirdWord;

	/**
	 * Create a command object. First and second word must be supplied, but
	 * either one (or both) can be null. The command word should be null to
	 * indicate that this was a command that is not recognized by this game.
	 */
	public Command(String firstWord, String secondWord, String thirdWord) {
		commandWord = firstWord;
		this.secondWord = secondWord;
		this.thirdWord = thirdWord;
	}

	/**
	 * Return the command word (the first word) of this command. If the
	 * command was not understood, the result is null.
	 */
	public String getCommandWord() {
		return commandWord;
	}

	/**
	 * Return the second word of this command. Returns null if there was no
	 * second word.
	 */
	public String getSecondWord() {
		return secondWord;
	}

	/**
	 * Return the third word of this command. Returns null if there was no
	 * third word.
	 */
	public String getThirdWord() {
		return thirdWord;
	}

	/**
	 * Return true if this command was not understood.
	 */
	public boolean isUnknown() {
		return (commandWord == null);
	}

	/**
	 * Return true if the command has a second word.
	 */
	public boolean hasSecondWord() {
		return (secondWord != null);
	}

	/**
	 * Return true if the command has a third word.
	 */
	public boolean hasThirdWord() {
		return (thirdWord != null);
	}
}
