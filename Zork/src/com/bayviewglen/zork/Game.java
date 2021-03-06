package com.bayviewglen.zork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter; 
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

/** "Game" Class - the main class of the game.
 * 
 *  Original Code Author: 	Michael Kolling
 *  Original Code Version:	1.0
 *  Original Published Date: July 1999
 * 
 *  Current Authors: 		Kirill Tregubov, Zacharia Burrafato, Andrew Douglas, Alim Halani
 *  Current Code Version:	0.3-alpha
 *  Current Published Date:	May 2018
 * 
 *  This class is the main class of the "Zork" application. Zork is a very
 *  simple, text based adventure game.  Users can walk around some scenery.
 *  That's all. It should really be extended to make it more interesting!
 * 
 *  To play this game, create an instance of this class and call the "play"
 *  routine.
 * 
 *  This main class creates and initializes all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates the
 *  commands that the parser returns.
 */

// LEGACY Search terms: Teleporter, Changeme..

class Game {
	public static final String GAME_NAME = "A Life Beyond"; // NAME
	public static final String GAME_VERSION = "0.3-alpha"; // VERSION
	private Parser parser;
	private BufferedWriter writer;
	private BufferedReader reader;
	public static final String FILE_LOCATION = "data\\"; // Change to "data/save.dat" if using Mac
	private final String DEFAULT_ROOM = "0-1";
	private Player player;
	public static Sound musicMainTheme;
	private TrialDriver trialDriver;
	private Trial currentTrial;
	private boolean completingTrial;
	public static Sound battleMusic = new Sound(Game.FILE_LOCATION + "battlemusic.wav");
	private Shop shop;
	public static boolean isMuted;

	// This is a MASTER object that contains all of the rooms and is easily accessible.
	// The key will be the name of the room -> no spaces (Use all caps and underscore -> Great Room would have a key of GREAT_ROOM
	// In a hashmap keys are case sensitive.
	// masterRoomMap.get("GREAT_ROOM") will return the Room Object that is the Great Room (assuming you have one).
	// private HashMap<String, Room> masterRoomMap;

	/**
	 * Create the game and initialize its internal map
	 */
	public Game() {
		try {
			// Load Player
			player = new Player();
			shop = new Shop(player);
			Shop shop = new Shop(player);
			initRooms();
			musicMainTheme = new Sound(FILE_LOCATION + "music1.wav");
			trialDriver = new TrialDriver(player);

			// Create Parser
			parser = new Parser(player);

			// Load game if saved
			if (gameIsSaved()) load();
			else player.setCurrentRoom(player.masterRoomMap.get(DEFAULT_ROOM));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 *  Main play routine (loops until quit)
	 */
	public void play() {
		// Initiate Music
		if (!isMuted)
			musicMainTheme.loop();
		//FlashingImages welcomeImage = new FlashingImages(FILE_LOCATION+"max.jpg",3000); //unused for now

		printWelcome();

		// Enter the main command loop: repeatedly reads / executes commands until the game is over
		boolean finished = false;

		// Player is playing game
		while (!finished) { // check if in trial mode !!!
			if (trialDriver.isGameBeaten()) {
				Utils.formattedPrint(true, "Thank you for playing the game! If you would like to restart, you can simply delete all the contents of your save.dat"
						+ " file located in " + FILE_LOCATION + "save.dat.");
				System.exit(0);
			}
			if (completingTrial) finished = playTrial();
			else finished = executeCommand();
		}

		// End Game
		System.out.println("Thank you for playing. Goodbye!");
		musicMainTheme.EndStop();
		battleMusic.EndStop();
	}

	private void initRooms() throws Exception {
		player.masterRoomMap = new HashMap<String, Room>();
		BufferedReader reader;
		try {
			HashMap<String, HashMap<String, String>> exits = new HashMap<String, HashMap<String, String>>();
			reader = new BufferedReader(new FileReader(FILE_LOCATION + "rooms.dat"));
			String line;

			while((line = reader.readLine()) != null) {
				if (line.contains("ID: ")) {
					// Create room
					Room room = new Room();
					// Read the ID
					String[] rawID = line.split(":")[1].split(" ");
					String roomID = "";
					for (int i = 1; i < rawID.length; i++) {
						roomID += Integer.parseInt(rawID[i], 2);
						if (i < rawID.length-1) roomID += "-";
					}
					//String roomID = line.substring(line.indexOf(":")+2).replaceAll(" ", "-");
					room.setRoomID(roomID);
					// Read the Name
					String roomName = reader.readLine();
					room.setRoomName(roomName.split(":")[1].trim()); // ATTENTION: Why is roomName not stored the way it's read?
					// Read the Description
					String roomDescription = reader.readLine();
					room.setDescription(Utils.formatStringForPrinting(roomDescription.substring(roomDescription.indexOf(":")+2).replaceAll("<br>", "\n").trim()));
					// Read the Exits
					String roomExits = reader.readLine();

					// An array of strings in the format "E-RoomName"
					String[] rooms = roomExits.split(":")[1].split(",");
					HashMap<String, String> temp = new HashMap<String, String>(); 
					try {
						for (String s : rooms)
							temp.put(s.split("=")[0].trim(), s.split("=")[1]);
						// LEGACY exits.put(roomName.substring(10).trim().toUpperCase().replaceAll(" ",  "_"), temp);
					} catch (Exception e) {}
					exits.put(roomID, temp);

					// Read items, assign to array, and store it
					String roomItems = reader.readLine();
					if (roomItems.contains("Items: ") && roomItems.split(":")[1].trim().length() > 0) {
						roomItems = roomItems.split(":")[1].trim();
						String[] itemsString = roomItems.split(", ");
						room.setItems(itemsString); // assign items to the room's variable
					}

					// Read Trials and Assign them 
					String trial = reader.readLine();
					if (trial.contains("Trial: ") && trial.substring(trial.indexOf(":")+2).trim().length() > 0) {
						room.setTrial(trial.substring(trial.indexOf(":")+2).trim());
					}
					// WORK IN PROGRESS

					// Read Entities and Create them 
					String roomEntities = reader.readLine();
					if (roomEntities.contains("Entities: ") && roomEntities.split(":")[1].trim().length() > 0) {
						roomEntities = roomEntities.split(":")[1].trim();

						String[] entityString;
						entityString = roomEntities.split("/ ");
						// Enemy, type
						String[][] entities = new String[4][entityString.length];
						String[] entitiesStrings;

						for (int i=0;i<entityString.length;i++) {
							entitiesStrings = entityString[i].split(" <");
							entities[0][i] = entitiesStrings[0];
							entities[1][i] = entitiesStrings[1].substring(0, entitiesStrings[1].length()-1);
							entities[2][i] = entitiesStrings[2].substring(0,entitiesStrings[2].length()-1);
							entities[3][i] = entitiesStrings[3].substring(0,entitiesStrings[3].length()-1);
						}
						room.setEntities(entities);
					}

					// Assign room to be stored as roomID
					player.masterRoomMap.put(roomID, room);
				}	

			}

			for (String key : player.masterRoomMap.keySet()) {
				Room roomTemp = player.masterRoomMap.get(key);
				HashMap<String, String> tempExits = exits.get(key);
				for (String s : tempExits.keySet()) {
					// s = direction
					// value is the room
					String roomName2 = tempExits.get(s);
					Room exitRoom = player.masterRoomMap.get(roomName2);
					roomTemp.setExit(s.charAt(0), exitRoom);
				}
			}

			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("The rooms.dat file was not found! Please download one from the game's repository and insert it into " + FILE_LOCATION);
		}
	}

	/**
	 * Prints welcome message
	 */
	private void printWelcome() {
		System.out.println("\n" + "Welcome to " + GAME_NAME + "!"
				+ "\n" + "We hope you enjoy playing " + GAME_NAME + ", an incredibly enjoyable adventure game!"
				+ "\n" + "You are currenty playing version " + GAME_VERSION
				+ "\n" + "You can type 'help' at any time if you need any."
				+ "\n" + "Good luck and have fun!"+ "\n");

		if (gameIsSaved()) {
			System.out.print("Welcome back, " + player.name
					+ "\nAutomatically loaded game state from " + timeGameWasSaved()
					+ "\nLast known location: " + player.getRoomName());

			String inventoryString = " Nothing was found...";
			if (!player.inventory.isEmpty()) inventoryString = player.inventory + "\n";
			Utils.formattedPrint(true, "Last known items in inventory:" + inventoryString);

			if (completingTrial) System.out.println("WARNING: You are currently completing a trial!");
			System.out.println(player.getRoomDescription());
		} else {
			completingTrial = true;
		}
	}

	private boolean executeCommand() {
		System.out.println("");
		Command command = parser.getCommand();
		return processCommand(command);
	}

	private boolean playTrial() {
		loadEntitiesInRoom();
		boolean finished = false;
		// Tutorial
		if (currentTrial == null) {
			int i = 0;
			if (player.doesRoomHaveTrial() && player.getRoomTrial().equals("tutorial")) {
				currentTrial = trialDriver.tutorial(i);
				i++;
			}

			while (!finished && player.getRoomHasEnemies()) finished = executeCommand();
			if (finished) return true;
			else { 
				currentTrial = trialDriver.tutorial(i);
				completingTrial = false;
				return false;
			}
		} // Shop
		else if (currentTrial.toString().equals("Shop")) {
			int i = 1;

			while (!finished) {
				System.out.println("");
				Command command = parser.getShopCommand(shop);
				//System.out.println(command.getCommand() + " " + command.getCommandType() + " " + command.getContextWord());

				try {
					if (command.getCommand().equals("go") && command.getContextWord().equals("east")) {
						currentTrial = trialDriver.shop(i, shop);
						completingTrial = false;
						return false;
					}
				} catch (Exception e) {}

				String commandType = command.getCommandType();
				String contextWord = command.getContextWord();

				if (commandType == null)
					System.out.println("You cannot do that...");
				else if (commandType.equals("quit"))
					return true;
				else if (commandType.equals("buy"))
					shop.purchaseItem(contextWord);
				else if (commandType.equals("check"))
					System.out.println("The " + Item.getItem(contextWord) + " costs $" + shop.getPrice(contextWord) + ".");
				else if (commandType.equals("price"))
					System.out.println(player.getMoneyString());
				else if (commandType.equals("place"))
					System.out.println("That's not an option... You might be trapped.");
				else if (commandType.equals("help")) {
					System.out.println("Here are the commands you can use in the shop:");
					System.out.print(parser.listShopCommands());
				}
			}
			if (finished) return true;
			return false; // should be unreachable
		} // Challenge Gate
		else if (currentTrial.toString().equals("Challenge Gate")) {
			int i = 1;
			currentTrial = trialDriver.challengeGate(i, currentTrial.getNumber());
			System.out.println("\n" + player.getRoomDescription());
			while (!finished && player.getRoomID().equals("13")) {
				System.out.println("");
				Command command = parser.getCommand();
				try {
					System.out.print(""); // needed to stop code from breaking
					if (command.getCommand().equals("go") && command.getContextWord().equals("south")) {
						if (player.getRoomHasEnemies()) {
							Utils.formattedPrint(false, "You did not kill the enemy you challenged. They got bored waiting for you and left.\n");
							player.getRoomResetEntities();
						}
						currentTrial = null;
						completingTrial = false;
					}	
				} catch (Exception e) {}
				finished = processCommand(command);
				if (!player.getRoomID().equals("13"))
					player.masterRoomMap.get("13").resetEntities();
			}
			if (finished) return true;
			else if (!completingTrial) return false;
			else { 
				i++;
				currentTrial = null;
				completingTrial = false;
				return false;
			}
		} // Trial One
		else if (currentTrial.toString().equals("Trial One")) {
			int i = 1;
			while (completingTrial && !finished && player.getRoomID().equals("1")) finished = executeCommand();
			if (finished) return true;
			else if (!completingTrial) return false;
			else {
				currentTrial = trialDriver.trialOne(i);
				while (completingTrial && !finished && player.getRoomHasEnemies()) finished = executeCommand();
				if (finished) return true;
				else if (!completingTrial) return false;
				else {
					currentTrial = trialDriver.trialOne(++i);
					while (completingTrial && !finished && player.getRoomID().equals("1-1")) finished = executeCommand();
					if (finished) return true;
					else if (!completingTrial) return false;
					else { 
						currentTrial = trialDriver.trialOne(++i);
						while (completingTrial && !finished && player.getRoomHasBosses()) finished = executeCommand();
						if (finished) return true;
						else if (!completingTrial) return false;
						else { 
							currentTrial = trialDriver.trialOne(++i);
							completingTrial = false;
							return false;
						}
					}
				}
			}
		} // Trial Two
		else if (currentTrial.toString().equals("Trial Two")) {
			int i = 1;
			while (completingTrial && !finished && player.getRoomID().equals("2")) finished = executeCommand();
			if (finished) return true;
			else if (!completingTrial) return false;
			else {
				currentTrial = trialDriver.trialTwo(i);
				while (completingTrial && !finished && player.getRoomID().equals("2-1")) finished = executeCommand();
				if (finished) return true;
				else if (!completingTrial) return false;
				else {
					while (completingTrial && !finished && player.getRoomHasBosses()) finished = executeCommand();
					if (finished) return true;
					else if (!completingTrial) return false;
					else { 
						currentTrial = trialDriver.trialTwo(++i);
						completingTrial = false;
						return false;
					}
				}
			}
		} // Trial Three
		else if (currentTrial.toString().equals("Trial Three")) {
			int i = 1;
			while (completingTrial && !finished) {
				finished = executeCommand();

				if (player.getRoomID().equals("3-7")) {
					currentTrial = trialDriver.trialThree(i);
					completingTrial = false;
					return false;
				}
			}
			if (finished) return true;
			else if (!completingTrial) return false;
		} // Trial Four
		else if (currentTrial.toString().equals("Trial Four")) {
			int i = 1;
			while (completingTrial && !finished && player.getRoomID().equals("4")) finished = executeCommand();
			if (finished) return true;
			else if (!completingTrial) return false;
			else {
				currentTrial = trialDriver.trialFour(i);
				int failCounter = 0;
				boolean isComplete = false;
				while (completingTrial && !finished && !isComplete) {
					if (failCounter >= 2) {
						System.out.println("\nThe floor opens beneath you and you fall into a pool of hydrochloric acid. You have died a painful death.\nRespawning...");
						player.setDefaultRoom();
						System.out.println("\n" + player.getRoomTravelDescription());
						currentTrial = null;
						completingTrial = false;
					}

					System.out.println("");
					Command command = parser.getRiddleCommand(i); // change
					String commandType = command.getCommandType();
					String contextWord = command.getContextWord();

					//System.out.println(commandType + " " + contextWord);

					if (commandType == null)
						System.out.println("You cannot do that...");
					else if (commandType.equals("quit"))
						return true;
					else if (commandType.equals("mute")) {
						isMuted = true;
						musicMainTheme.pause();
						System.out.println("Game sound has been muted.");
					} else if (commandType.equals("unmute")) {
						isMuted = false;
						musicMainTheme.loop();
						System.out.println("Game sound has been enabled.");
					} else if (commandType.equals("answer")) {
						if (contextWord == null) {
							failCounter++;
							System.out.println("You answered incorrectly!");
							currentTrial = trialDriver.trialFour(++i);
							if (i >= 6)
								isComplete = true;
						} else {
							System.out.println("You answered correctly!");
							currentTrial = trialDriver.trialFour(++i);
							if (i >= 6)
								isComplete = true;
						}
					}
					else if (commandType.equals("abandon")) {
						if (!completingTrial) {
							System.out.println("There is nothing to abandon!");
							return false;
						} else {
							System.out.println("Are you sure you want to abandon your current trial?");
							if (Parser.getYesNoAnswer()) {
								System.out.println("You have abandoned " + currentTrial.toString() + ".");
								currentTrial = null;
								completingTrial = false;

								player.setDefaultRoom();
								System.out.println("\n" + player.getRoomTravelDescription());
							} return false;
						}
					} else if (commandType.equals("help")) {
						System.out.println("Here are the commands you can use while completing Trial Four:");
						System.out.print(parser.listRiddleCommands());
					}
				}
				if (finished) return true;
				else if (!completingTrial) return false;
				else { 
					currentTrial = trialDriver.trialTwo(++i);
					completingTrial = false;
					return false;
				}
			}
		} // Trial Five
		else if (currentTrial.toString().equals("Trial Five")) {
			int i = 1;
			while (completingTrial && !finished && player.getRoomHasBosses()) finished = executeCommand();
			if (finished) return true;
			else if (!completingTrial) return false;
			else { 
				currentTrial = trialDriver.trialFive(i);
				completingTrial = false;
				return false;
			}
		} // Trial Six
		else if (currentTrial.toString().equals("Trial Six")) {
			int i = 2;
			while (completingTrial && !finished && player.getRoomID().equals("6")) finished = executeCommand();
			if (finished) return true;
			else if (!completingTrial) return false;
			else { 
				if (player.getRoomID().equals("6-1")) { 
					currentTrial = trialDriver.trialSix(1);
					player.setDefaultRoom();
					System.out.println("\n" + player.getRoomTravelDescription());
					currentTrial = null;
					completingTrial = false;
				}
				else {
					currentTrial = trialDriver.trialSix(i);
					while (completingTrial && !finished && player.getRoomID().equals("6-2")) finished = executeCommand();
					if (finished) return true;
					else if (!completingTrial) return false;
					else { 
						if (player.getRoomID().equals("6-3")) { 
							currentTrial = trialDriver.trialSix(1);
							player.setDefaultRoom();
							System.out.println("\n" + player.getRoomTravelDescription());
							currentTrial = null;
							completingTrial = false;
						}
						else {
							currentTrial = trialDriver.trialSix(++i);
							completingTrial = false;
							return false;
						}
					}
				}
			}
		} // Trial Seven
		else if (currentTrial.toString().equals("Trial Seven")) {
			int i = 1;
			while (completingTrial && !finished && player.getRoomHasBosses()) finished = executeCommand();
			if (finished) return true;
			else if (!completingTrial) return false;
			else {
				currentTrial = trialDriver.trialSeven(i);
				completingTrial = false;
				return false;
			}
		} // Trial Eight
		else if (currentTrial.toString().equals("Trial Eight")) {
			int i = 1;
			while (completingTrial && !finished && player.getRoomHasBosses()) finished = executeCommand();
			if (finished) return true;
			else if (!completingTrial) return false;
			else {
				currentTrial = trialDriver.trialEight(i);
				completingTrial = false;
				return false;
			}
		}
		return false; // if all else breaks
	}

	/*
	 * Work in Progress
	 */
	private boolean processCommand(Command command) {
		if(command.isUnknown()) {
			System.out.println("You cannot do that...");
			return false;
		}

		String commandName = command.getCommand();
		String commandType = command.getCommandType();
		String contextWord = command.getContextWord();
		Integer numbers[] = command.getNumbers();
		//System.out.println(commandName + "\n" + commandType + "\n" + contextWord);

		// help
		if (commandName.equalsIgnoreCase("help")) printHelp();
		// list
		else if (commandName.equalsIgnoreCase("list")) printCommands(); // might need to add contextWord
		// go
		else if (commandName.equalsIgnoreCase("go")) { 
			goRoom(command, commandName);
		} // start
		else if (commandType.equalsIgnoreCase("trial")) {
			if (completingTrial) {
				System.out.println("You cannot start a trial while completing one.");
				return false;
			}
			try {
				if (command.getFirstNumber() == 1) {
					currentTrial = trialDriver.trialOne(0);
					completingTrial = true;
				} else if (command.getFirstNumber() == 2) {
					if (trialDriver.isFirstTrialComplete()) {
						currentTrial = trialDriver.trialTwo(0);
						completingTrial = true;
					} else {
						System.out.println("You must complete Trial One first!");
						completingTrial = false;
					}
				} else if (command.getFirstNumber() == 3) {
					if (trialDriver.isFirstTrialComplete()) {
						currentTrial = trialDriver.trialThree(0);
						completingTrial = true;
					} else {
						System.out.println("You must complete Trial One first!");
						completingTrial = false;
					}
				} else if (command.getFirstNumber() == 4) {
					if (trialDriver.isFirstTrialComplete()) {
						currentTrial = trialDriver.trialFour(0);
						completingTrial = true;
					} else {
						System.out.println("You must complete Trial One first!");
						completingTrial = false;
					}
				} else if (command.getFirstNumber() == 5) {
					if (trialDriver.isFirstTrialComplete()) {
						currentTrial = trialDriver.trialFive(0);
						completingTrial = true;
					} else {
						System.out.println("You must complete Trial One first!");
						completingTrial = false;
					}
				} else if (command.getFirstNumber() == 6) {
					if (trialDriver.areFiveTrialsComplete()) {
						currentTrial = trialDriver.trialSix(0);
						completingTrial = true;
					} else {
						System.out.println("You must complete Trials 1 through 5 first!");
						completingTrial = false;
					}
				} else if (command.getFirstNumber() == 7) {
					if (trialDriver.areSixTrialsComplete()) {
						currentTrial = trialDriver.trialSeven(0);
						completingTrial = true;
					} else {
						System.out.println("You must complete Trials 1 through 6 first!");
						completingTrial = false;
					}
				} else if (command.getFirstNumber() == 8) {
					if (trialDriver.areBaseTrialsComplete()) {
						currentTrial = trialDriver.trialEight(0);
						completingTrial = true;
					} else {
						System.out.println("You must complete Trials 1 through 7 first!");
						completingTrial = false;
					}
				}else {
					System.out.println("Unable to start that trial! Please try again.");
					return false;
				}
			} catch (Exception e) {
				System.out.println("Unable to start that trial! Please try again.");
				return false;
			}
		} // abandon
		else if (commandName.equalsIgnoreCase("abandon")) {
			if (!completingTrial) {
				System.out.println("There is nothing to abandon!");
				return false;
			}
			if (currentTrial.toString().equals("tutorial")) {
				System.out.println("You cannot abandon the tutorial!");
				return false;
			}
			System.out.println("Are you sure you want to abandon your current trial?");
			if (Parser.getYesNoAnswer()) {
				System.out.println("You have abandoned " + currentTrial.toString() + ".");
				currentTrial = null;
				completingTrial = false;

				player.setDefaultRoom();
				System.out.println("\n" + player.getRoomTravelDescription());
			} return false;
		} // heal
		else if (commandName.equalsIgnoreCase("heal")) {
			if (player.getRoomID().equals("12")) new HealingCenter(player);
			else System.out.println("You are not in the Healing Center, therefore you cannot be healed!");
		} // battle
		else if (commandType.equalsIgnoreCase("battle")) { // || commandName.equalsIgnoreCase("fight") || commandName.equalsIgnoreCase("challenge") || commandName.equalsIgnoreCase("attack")
			if (contextWord == null) {
				System.out.println("You cannot battle that!");
			} else {
				if (contextWord.equals("NPC"))
					System.out.println("You cannot battle an NPC! They wouldn't want to hurt you.");
				else if (player.getRoomHasRepeatedEnemies(contextWord)) {
					System.out.println("There are multiple enemies in this room with that name! Please be more specific!");
				} else if (player.getRoomHasEnemies() && player.getRoomFindEnemy(contextWord).getType().equals(Entity.TYPES[Entity.BOSS_INDEX])) {
					System.out.println("You must defeat all enemies before challenging the boss!");
				}
				else {
					int battleResult = player.getRoomStartBattle(contextWord);
					if (battleResult == 2) {
						player.setDefaultRoom();
						System.out.println("Respawning...");
						System.out.println("\n"+player.getRoomDescription());
						player.stats.setCurrentHP(player.stats.getMaximumHP());
						currentTrial = null;
						completingTrial = false;
					} else if (!completingTrial || battleResult == 0) System.out.println("\n" + player.getRoomDescription());
				}
			}
		} // talk
		else if (commandName.equalsIgnoreCase("talk")) {
			if (contextWord == null) {
				System.out.println("You cannot talk to that!");
			} else {
				if (contextWord.equalsIgnoreCase("ENEMY"))
					Utils.formattedPrint(false, "You cannot peacefully talk with an enemy! Engage in battle with them instead!");
				else {
					Utils.formattedPrint(false, player.getRoomFindNPC(contextWord).name + ": " + player.getRoomFindNPC(contextWord).getDialogue());
				}
			}
		} // eat
		else if (commandName.equalsIgnoreCase("consume")) { // add check if it's consumable - add joke
			if (contextWord != null) {
				try {
					try {
						if (command.getFirstNumber() != null) {
							System.out.println("You can only consume one thing at a time! How big do you think your mouth is?!");
							return false;
						}
					} catch (Exception e) {
					}
					if (player.consumeItem(contextWord)) {
						System.out.println("You consumed " + Item.getItem(contextWord));
					} else if (!player.inventory.hasItem(contextWord)) System.out.println("That item is not in your inventory!");
					else System.out.println("You cannot consume that.");
				} catch (Exception e) {
					System.out.println("That's not an item! Even if it was, do you really think you should be consuming something at a time like this?");
				}
			} else System.out.println("What would you like to consume?");
		} // look
		else if (commandName.equalsIgnoreCase("look")) { // might want to add look around?
			if (contextWord != null) {
				if (commandType.contains(" ")) System.out.println("You cannot look at that! Please be more specific.");
				// Item
				if (commandType.equals("item")) {
					if (contextWord.equalsIgnoreCase("equipped")) System.out.println(player.checkEquippedItems());
					String check = player.itemCanBeLookedAt(contextWord);
					if (check.equals("roomrepeated")) System.out.println("Please be more specific. There are multiple items with this name!");
					else if (check.equals("contains")) System.out.println(Item.getItem(contextWord).getDescription() + "\n" + Item.getItem(contextWord).stats);
					else System.out.println("That item is not in your inventory or in the " + player.getRoomName() + ".");
				} // Inventory
				else if (commandType.equals("inventory")) {
					if (player.inventory.isEmpty()) System.out.println("Your inventory is empty!");
					else Utils.formattedPrint(false, "Your inventory contains:" + player.inventory);
				} // Place
				else if (commandType.equals("place")) {
					if (contextWord.equals("")) System.out.println(player.getRoomDescription());
					else {
						int count = 0;
						String roomName = null;
						for (String key : player.masterRoomMap.keySet()) {
							Room roomTemp = player.masterRoomMap.get(key);
							if (Utils.containsIgnoreCase(roomTemp.getRoomName(), contextWord)) {
								roomName = roomTemp.getRoomName();
								count++;
							}
						}
						if (count == 1) {
							if (roomName.equals(player.getRoomName()))  // doesn't work well / at all
								System.out.println(player.getRoomDescription());
							else System.out.println("In the future, you will be able to get directions to the " + roomName); // implement more
						}
						else System.out.println("You cannot look at that! Please be more specific.");
					}		
				} // Enemy
				else if (commandType.equals("enemy")) {
					Entity enemy = player.getRoomEnemy(contextWord);
					if (enemy != null) System.out.println(enemy.toString() + "'s stats are: " + "\n" + enemy.stats);
					else System.out.println("You cannot look at that! Please be more specific.");
				}
			} else System.out.print("What would you like to look at?");
		} // check
		else if (commandName.equalsIgnoreCase("check")) {
			if (Utils.containsIgnoreCase(contextWord, "equip")) System.out.println(player.checkEquippedItems());
			else if (Utils.containsIgnoreCase(contextWord, "money") || Utils.containsIgnoreCase(contextWord, "cash")) System.out.println(player.getMoneyString());
			else if (Utils.containsIgnoreCase(contextWord, "trial") || Utils.containsIgnoreCase(contextWord, "complete")) System.out.println(trialDriver);
			else if (Utils.containsIgnoreCase(contextWord, "stat") || Utils.containsIgnoreCase(contextWord, "info")) System.out.println(player.stats);
			else System.out.println("You cannot check that!");
		} // equip
		else if (commandName.equalsIgnoreCase("equip")) {
			if (Item.isItem(contextWord)) {
				if (player.inventory.hasItem(contextWord)) {
					if (Item.getItem(contextWord).type.equals(Item.TYPES[Item.WEAPON_INDEX])) {
						player.setEquippedWeapon(player.inventory.getItem(contextWord));
						System.out.println("You successfully equipped " + player.inventory.getItem(contextWord) + "!");
					} else if (Item.getItem(contextWord).type.equals(Item.TYPES[Item.ARMOR_INDEX])) {
						player.setEquippedArmor(player.inventory.getItem(contextWord));
						System.out.println("You successfully equipped " + player.inventory.getItem(contextWord) + "!");
					} else System.out.println("The given item is not a piece of armor, or a weapon.");
				} else System.out.println("You can only equip items in your inventory!");
			} else System.out.println("What would you like to equip?");
		} // take
		else if (commandName.equalsIgnoreCase("take") || (commandName.equalsIgnoreCase("pickup") || (commandName.equalsIgnoreCase("grab")))) { // add way to pick up amounts of stackable items
			if (contextWord != null) {
				if (player.itemCanBePickedUp(contextWord).equals("roomrepeated"))
					System.out.println("Please be more specific. There are multiple items with this name!");
				else if (player.itemCanBePickedUp(contextWord).equals("inventorycontains")) //may be needed in the future
					System.out.println("This item is already in your inventory!");
				else if (player.itemCanBePickedUp(contextWord).equals("roomnotcontains"))
					System.out.println("There is no " + Item.getItem(contextWord) + " in this room!");

				else if (player.itemCanBePickedUp(contextWord).isEmpty()) {
					if (numbers != null) {
						if (player.pickUpItem(contextWord, player.getRoomID(), numbers) == null) {
							player.updateItems(player, player.getRoomID());
							if (numbers[0] > 1) System.out.println(numbers[0] + " " + Item.getItem(contextWord) + "s were added to your inventory!");
							else System.out.println(numbers[0] + " " + Item.getItem(contextWord) + " was added to your inventory!");
						}
						else if (player.pickUpItem(contextWord, player.getRoomID(), numbers) == "toomuch") System.out.println("TOO MUCH");
						else System.out.println("Encountered an error while adding the item to your inventory!");
					} else {
						if (player.pickUpItem(contextWord, player.getRoomID()) == null) {
							player.updateItems(player, player.getRoomID());
							System.out.println("A " + Item.getItem(contextWord) + " was added to your inventory!");
						}
						else if (player.pickUpItem(contextWord, player.getRoomID()) == "toomuch") System.out.println("TOO MUCH");
						else System.out.println("Encountered an error while adding the item to your inventory!");
					}
				} else System.out.println("You cannot take that! Please be more specific.");
			} else {
				System.out.print("You cannot");
				if (commandName.equalsIgnoreCase("take")) System.out.println("take that.");
				else System.out.println("pick up that.");
			}
		} // mute
		else if (commandName.equalsIgnoreCase("mute")) {
			isMuted = true;
			musicMainTheme.pause();
			System.out.println("Game sound has been muted.");
		} // unmute
		else if (commandName.equalsIgnoreCase("unmute")) {
			isMuted = false;
			musicMainTheme.loop();
			System.out.println("Game sound has been enabled.");
		} // save
		else if (commandName.equalsIgnoreCase("save")) save();
		// quit
		else if (commandName.equalsIgnoreCase("quit") || commandName.equalsIgnoreCase("stop")) { // player wants to quit
			return true;
		} // wrong command
		else System.out.println("The command " + commandName + " has been unaccounted for. Kirill, please fix!");
		return false;
	}

	/**
	 * Processes a given command, assuming a related command was previously entered
	@Deprecated
	private void continueCommand(String originalCommand) { // work in progress??
		Command command = parser.getSecondaryCommand(player);
		String commandInput = command.command;
		Integer numbers[] = command.numbers;

		System.out.println("");
		if(command.isUnknown()) {
			System.out.println("You cannot do that...");
		}

		if (originalCommand.equals("eat")) {
			System.out.println("Whatever you say... You still can't eat at a time like this.");
		} // wrong command
		else System.out.println("You cannot do that...");
	}*/

	// User Commands

	/**
	 * Print list of commands used
	 */
	private void printHelp() {
		System.out.println("You are lost. You are alone. You wander..."
				+ "\nTo find out what commands are available, type in \"list commands\"");
		if (completingTrial) System.out.println("If you are stuck in a trial, you can always use the 'abandon' command.");
		else Utils.formattedPrint(false, "You can use the 'start' command anywhere to start completing a trial. Remember, there are 8 trials!");
	}

	private void printCommands() {
		System.out.println("All available commands:");
		System.out.print(parser.listCommands());
	}

	/** 
	 * Go to specified room (in the specified direction)
	 */
	private void goRoom(Command command, String givenCommand) {
		/*for (Entry<String, Room> entry : player.masterRoomMap.entrySet()) {
			String key = entry.getKey();
			System.out.println(key);
			Room value = entry.getValue();
			System.out.println(value.getRoomName());
			if (room.getRoomName().equals(command.getContextWord())) {
				System.out.println("SUCC");
			}
		}*/

		if (command.getContextWord() == null) {
			System.out.print("Please indicate a direction you would like to ");
			if (givenCommand.equalsIgnoreCase("walk")) System.out.println("walk in.");
			else System.out.println("go in.");
			return;
		}
		String direction = command.getContextWord();

		if (completingTrial && (player.getRoomHasEnemies() || player.getRoomHasBosses()))
			System.out.println(currentTrial.getLeaveReason());
		else {
			// Try to leave current room.
			Room nextRoom = player.getNextRoom(direction);
			if (nextRoom != null) {
				player.setCurrentRoom(nextRoom);
				player.updateItems(player, nextRoom.getRoomID());
				if (nextRoom.toString().equals("Shop")) {
					currentTrial = trialDriver.shop(0, shop);
					completingTrial = true;
				} else if (nextRoom.toString().equals("Challenge Gate")) {
					currentTrial = trialDriver.challengeGate(0, 0);
					completingTrial = true;
				} else {
					// Trial Three Special Case
					if (completingTrial && currentTrial.toString().equals("Trial Three") && !player.getRoomID().equals("3"))
						if (Math.random() <= 0.5) player.getRoomResetEntities();

					System.out.println(player.getRoomTravelDescription());
				}
			} else System.out.println("That's not an option... You might be trapped.");
		}
	}

	private void loadEntitiesInRoom() {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(FILE_LOCATION + "rooms.dat"));
			String line;
			ArrayList<String> roomIDs = new ArrayList<String>();
			ArrayList<String> entitiesString = new ArrayList<String>();

			while((line = reader.readLine()) != null) {
				if (Utils.containsIgnoreCase(line, "ID:")) {
					String[] rawID = line.split(":")[1].split(" ");
					String roomID = "";
					for (int i = 1; i < rawID.length; i++) {
						roomID += Integer.parseInt(rawID[i], 2);
						if (i < rawID.length-1) roomID += "-";
					}
					roomIDs.add(roomID);
				}
				if (Utils.containsIgnoreCase(line, "Entities:")) {
					entitiesString.add(line);
				}
			}

			for (Room room : player.masterRoomMap.values()) {
				String roomEntities = entitiesString.get(roomIDs.indexOf(room.getRoomID()));

				if (roomEntities.contains("Entities: ") && roomEntities.split(":")[1].trim().length() > 0) {
					roomEntities = roomEntities.split(":")[1].trim();

					String[] entityString;
					entityString = roomEntities.split("/ ");
					// Enemy, type
					String[][] entities = new String[4][entityString.length];
					String[] entitiesStrings;

					for (int i=0;i<entityString.length;i++) {
						entitiesStrings = entityString[i].split(" <");
						entities[0][i] = entitiesStrings[0];
						entities[1][i] = entitiesStrings[1].substring(0, entitiesStrings[1].length()-1);
						entities[2][i] = entitiesStrings[2].substring(0,entitiesStrings[2].length()-1);
						entities[3][i] = entitiesStrings[3].substring(0,entitiesStrings[3].length()-1);
					}
					room.resetEntities();
					room.setEntities(entities);
				}
			}

			reader.close();
		} catch (Exception e) {
			System.out.println("The rooms.dat file was not found! Please download one from the game's repository and insert it into " + FILE_LOCATION);
		}
	}

	/*
	 * Save Method
	 */
	public void save() { // save money, player stats
		if (completingTrial) {
			System.out.println("You cannot save while completing a trial!");
			return;
		}
		try {
			writer = new BufferedWriter(new FileWriter(FILE_LOCATION + "save.dat", true));
			writer.write("Name: " + player.name + "; ");	// save room currently in
			writer.write("Room: " + player.getRoomID() + "; ");	// save room currently in
			writer.write("Inventory: " + player.inventory.saveInventory() + "; ");	// save inventory	
			writer.write("Money: " + player.getMoney() + "; ");
			writer.write("Stats: " + player.stats.save() + "; ");
			writer.write("isMuted: " + isMuted + "; ");
			writer.write("Time Saved: " + LocalDateTime.now() + "; ");
			if(trialDriver.areAnyTrialsComplete()) writer.write(trialDriver.toString().replaceAll(",", ""));
			writer.newLine();
			writer.close();
			System.out.println("Your game has been successfully saved!");
		} catch(IOException e) {
			System.out.println("Error Saving: " + e);
		}
	}

	/*
	 * Boolean that checks if game has been saved
	 */
	public boolean gameIsSaved() {
		try {
			reader = new BufferedReader(new FileReader(FILE_LOCATION + "save.dat"));
			return reader.readLine() != null;
		} catch(IOException e) {
			return false;
		}
	}

	/*
	 * Returns time the last time the game was saved
	 */
	public String timeGameWasSaved() {
		try {
			if (gameIsSaved()) {
				String saveFile = null, line;
				reader = new BufferedReader(new FileReader(FILE_LOCATION + "save.dat"));
				while ((line = reader.readLine()) != null) {
					saveFile = line;
				}

				return saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 7)+13, Utils.ordinalIndexOf(saveFile, ":", 7)+21)
						+ " on " + saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 7) + 10, Utils.ordinalIndexOf(saveFile, ":", 7) + 12)
						+ "/" + saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 7) + 7, Utils.ordinalIndexOf(saveFile, ":", 7) + 9)
						+ "/" + saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 7) + 2, Utils.ordinalIndexOf(saveFile, ":", 7) + 6);
			}
		} catch(IOException e) { }
		return null;
	}

	/*
	 * Load Method - loads save file (if it exists)
	 */
	public void load() {
		String saveFile = null, line;
		try {
			if (gameIsSaved()) {
				reader = new BufferedReader(new FileReader(FILE_LOCATION + "save.dat"));
				while ((line = reader.readLine()) != null) {	// while loop to determine last line in save file
					saveFile = line; // assigns the latest save to variable saveFile
				}
				reader.close();

				// Find and assign currentRoom to the room in the save file
				player.setName(saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 1) + 2, Utils.ordinalIndexOf(saveFile, ";", 1)));
				player.setCurrentRoom(player.masterRoomMap.get(saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 2) + 2, Utils.ordinalIndexOf(saveFile, ";", 2))));

				// Find and load inventory
				int x = 0, index = 0;
				for (int i = 1; x != -1; i++) {
					index = i;
					x = Utils.ordinalIndexOf(saveFile, ",", index);
					if (x == -1)
						index --;
				}
				String[] savedInventory = new String[index];
				for (int i = 0; i < index; i++) { // assign saved inventory to an array
					if (i == 0)
						savedInventory[i] = saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 3)+2, Utils.ordinalIndexOf(saveFile, ",", i));
					else
						savedInventory[i] = saveFile.substring(Utils.ordinalIndexOf(saveFile, ",", i)+2, Utils.ordinalIndexOf(saveFile, ",", i+1));
				}

				player.inventory.loadInventory(savedInventory);
				player.updateItems(player, player.getRoomID());


				player.loadMoney(Integer.parseInt(saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 4)+2, Utils.ordinalIndexOf(saveFile, ";", 4))));
				player.stats.loadStats(saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 5)+2, Utils.ordinalIndexOf(saveFile, ";", 5)));
				if (saveFile.substring(Utils.ordinalIndexOf(saveFile, ":", 6)+2, Utils.ordinalIndexOf(saveFile, ";", 6)).equals("true"))
					isMuted = true;


				completingTrial = false;
				if (saveFile.substring(saveFile.lastIndexOf(':')-16, saveFile.lastIndexOf(':')).equals("Completed Trials"))
					trialDriver.loadTrials(saveFile.substring(saveFile.lastIndexOf(':')+2));
				// completed trials
			}
		} catch(IOException e) {
			System.out.println("Error Loading Save: " + e);
		}
	}
}