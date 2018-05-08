package com.bayviewglen.zork;

import java.util.HashMap;

/** "Player" Class - a class that creates a player and stores their data.
 * 
 * Current Authors: Kirill Tregubov, Zacharia Burrafato, Andrew Douglas, Alim Halani
 * Current Version: 0.2-alpha
 * Current Date:    April 2018
 */

public class Player {

	public String name;
	private Room currentRoom;
	public Inventory inventory;
	public Stats stats;
	public HashMap<String, Room> masterRoomMap;

	Player() {
		inventory = new Inventory();
		//pickedUpItems = new Inventory();
		stats = new Stats(Stats.ENTITY_INDEX, Stats.PLAYER_INDEX, "1,0,0,20,20,2,2,2,0.5,0.1");		
	}

	// Item Variables
	static Item itemOne = new Item("First Item", "This is a test item 1.");
	static Item itemTwo = new Item("Second Item", "This is a test item 1.");
	static Item itemThree = new Item("Item Three", "This is a test item 1.");
	static Item itemFour = new Item("Item Four", "This is a test item 1.");
	static Item itemFive = new Item("Item Five", "This is a test item 1.");
	static Item redApple = new Item("Red Apple", "Apple", Item.CONSUMABLE_INDEX, "5");
	static Item basicSword = new Item("Basic Sword", "This is a test item 1.", Item.WEAPON_INDEX, "10,0.5");
	static Item uniqueTest = new Item("Unique Test", "This is unique!", Item.WEAPON_INDEX, "20,0.0,0.25", new int[]{ Stats.DMG_REFLECT_INDEX });
	
	// Weapons - Common
	static Item splinteredBranch = new Item("Splintered Branch", "A branch that hurts...a litle.", Item.WEAPON_INDEX, "2,0.0");
	static Item woodenRapier = new Item("Wooden Rapier", "A wooden sword with a cool name.", Item.WEAPON_INDEX, "3,0.0");
	static Item copperKatana = new Item("Copper Katana", "A copper katana wielded by the samurai.", Item.WEAPON_INDEX, "4,0.0");
	static Item silverLongsword = new Item("Silver Longsword", "A sword of silver that is very long.", Item.WEAPON_INDEX, "5,0.0");

	// Weapons - Rare MSG: IMPLEMENT LIFEDRAIN
	static Item steelDagger = new Item("Steel Dagger", "A dagger crafted of the finest steel.", Item.WEAPON_INDEX, "7,0.0");
	static Item cobaltBroadsword = new Item("Cobalt Broadsword", "A large attack sword crafted from cobalt.", Item.WEAPON_INDEX, "8,1.0");
	static Item ironSpear = new Item("Iron Spear", "A powerful iron spear that can pack a punch.", Item.WEAPON_INDEX, "5,5.0");
	static Item lifeDrainStaff = new Item("Staff of Draining", "A magic staff crafted by the angels. It is said to steal the life force from any enemy it faces", Item.WEAPON_INDEX, "6,0.0,0.25", new int[]{ Stats.DMG_REFLECT_INDEX } );
	static Item goldenBambooSword = new Item("Golden Bamboo Sword", "A powerful sword crafted with the only golden bamboo wood on the planet.", Item.WEAPON_INDEX, "8,3.0");

	//Weapons - Epic
	static Item titaniumScythe = new Item("Titanium Scythe", "A scythe said to be wielded by the grim reaper himself.", Item.WEAPON_INDEX, "10,2.0");
	static Item terrorGauntlet = new Item("Gauntlet of Terror", "A gaunlet that strikes fear into the enemies it faces.", Item.WEAPON_INDEX, "12,2.0");
	static Item kongHammer = new Item("Donkey Kong Hammer", "The hammer that Mario himself used to defeat Donkey Kong.", Item.WEAPON_INDEX, "15,3.0");
	static Item vileBlade = new Item("The Vile Blade", "It's a vile sword? It's cool don't worry.", Item.WEAPON_INDEX, "20,1.0");

	//Weapons - Legendary MSG: IMPLEMENT DAMAGE REFLECT AND DOUBLE ATTACK
	static Item genjiDragonblade = new Item("Genji's Dragonblade", "Ryujin no ken wo kurae!", Item.WEAPON_INDEX, "20,0.0,0.25", new int[]{ Stats.DMG_REFLECT_INDEX });
	static Item gandalfStaff = new Item("Gandalf's Staff", "You shall not pass!", Item.WEAPON_INDEX, "20,0.0");
	static Item enchantedSabre = new Item("Enchanted Sabre", "A sword crafted by God himself.", Item.WEAPON_INDEX, "30,5.0,0.33", new int[]{ Stats.DMG_REFLECT_INDEX });
	static Item infinityGauntlet = new Item("Infinity Gauntlet", "The Infinity stones are not included.", Item.WEAPON_INDEX, "45,10.0,0.5", new int[]{ Stats.DMG_REFLECT_INDEX });
	static Item developerSword = new Item("Developer Sword", "A sword retrieved from the very heart of the syntax.", Item.WEAPON_INDEX, "10000,0.0");

	//Armor - Normal 
	static Item cardboardArmor= new Item("Cardboard Armor", "A suit made of cardboard that your mom gave you for you're birthday.", Item.ARMOR_INDEX, "0");
	static Item leatherSuit= new Item("Leather Suit", "A lovely leather suit to protect you from scratches.", Item.ARMOR_INDEX, "1");
	static Item knightArmor= new Item("Knight Armor", "A well-crafted suit of armor from the times of King Arthur.", Item.ARMOR_INDEX, "3");
	static Item titaniumArmor= new Item("Titanium Blast Plate Armor", "Military grade armor used to protect space shuttles.", Item.ARMOR_INDEX, "5");
	static Item vibraniumArmor= new Item("Vibranium Heavy Armor", "A suit of armor crafted from the strongest metal in the world.", Item.ARMOR_INDEX, "7");
	static Item electromagneticShield= new Item("Electromagnetic Shield Generator", "An electromagnetic barrier that deflects attacks away.", Item.ARMOR_INDEX, "10");

	//Armor - Enchanted - MSG: IMPLEMENT DAMAGE REFLECT
	static Item enchantedSteelArmor= new Item("Enchanted Steel Armor", "Magical steel Armor that deflects damage.", Item.ARMOR_INDEX, "7,0.25", new int[]{ Stats.DMG_REFLECT_INDEX });
	static Item enchantedForceField= new Item("Enchanted Force Field", "A powerful force field that blocks and deflects damage.", Item.ARMOR_INDEX, "10,0.25", new int[]{ Stats.DMG_REFLECT_INDEX });
	static Item skynightArmor= new Item("Skynight Armor", "An armor suit forged in the heart of the skynight forgery.", Item.ARMOR_INDEX, "12,0.33", new int[]{ Stats.DMG_REFLECT_INDEX });
	static Item darkAngelArmor= new Item("Enchanted Steel Armor", "A suit of armor taken from the dark angels.", Item.ARMOR_INDEX, "15,0.33", new int[]{ Stats.DMG_REFLECT_INDEX });
	static Item vibraniumKineticSuit= new Item("Enchanted Steel Armor", "A suit crafted from vibranium that has been enchanted with deflection properties.", Item.ARMOR_INDEX, "20,0.33", new int[]{ Stats.DMG_REFLECT_INDEX });

	//Healing Items / Other Consumables - MSG: ADD ATK+ FOR COMBAT POTION
	static Item smallHeal= new Item("Small Heal Potion", "A magical potion that restores 5 health.", Item.CONSUMABLE_INDEX, "5");
	static Item mediumHeal= new Item("Medium Heal Potion", "A magical potion that restores 20 health.", Item.CONSUMABLE_INDEX, "20");
	static Item largeHeal= new Item("Large Heal Potion", "A magical potion that restores 50 health.", Item.CONSUMABLE_INDEX, "50");
	static Item skyfireRoot= new Item("Skyfire Root", "A root that grows inside volcanos. It restores you to full health.", Item.CONSUMABLE_INDEX, "10000");
	static Item combatPotion= new Item("Combat Potion", "Military grade potion that heals you to full health and boost attack power for 1 battle.", Item.CONSUMABLE_INDEX, "10000");

	public static Item items[] = { itemOne, itemTwo, itemThree, itemFour, itemFive, redApple, basicSword, uniqueTest,
			splinteredBranch, woodenRapier, copperKatana, silverLongsword,
			steelDagger, cobaltBroadsword, ironSpear, lifeDrainStaff, goldenBambooSword,
			titaniumScythe, terrorGauntlet, kongHammer, vileBlade,
			genjiDragonblade, gandalfStaff, enchantedSabre, infinityGauntlet, developerSword,
			cardboardArmor,leatherSuit, knightArmor, titaniumArmor, vibraniumArmor, electromagneticShield,
			enchantedSteelArmor, enchantedForceField, skynightArmor, darkAngelArmor, vibraniumKineticSuit,
			smallHeal, mediumHeal, largeHeal, skyfireRoot, combatPotion };

	public String teleport(String roomName) {
		Room roomTemp = masterRoomMap.get(roomName);
		try {
			if (roomTemp != null) return roomName;
		} catch (Exception e) { }
		return null;
	}

	public boolean didPickUpItem(String itemName, String roomID) {
		if (inventory.containsItem(itemName) && inventory.getItem(itemName).roomID.contains(roomID)) return true;
		return false;
	}

	/*
	 * Inventory Methods
	 */

	public String pickUpItem(String itemName, String roomID) { // add fix for consumables
		Item inputItem;
		try {
			inputItem = new Item(Item.getItem(itemName));

			if (1 > currentRoom.getItem(itemName).getAmount())
				return "toomuch";
			else if (inventory.containsItem(itemName))
				inputItem.setAmount(1 + inventory.getItem(itemName).getAmount());
			else
				if (inputItem.isStackable) inputItem.setAmount(1); // change so that user can specify how many to pick up*/

			if (!inputItem.roomID.contains(roomID)) {
				inputItem.roomID.add(roomID);
				inputItem.pickedUpAmounts.add(1);
			} else inputItem.pickedUpAmounts.add(inputItem.roomID.indexOf(roomID), 1);

			inventory.addToInventory(inputItem, itemName, roomID);
			return null;
		} catch (Exception e) {
			return "error";
		}
	}

	public String pickUpItem(String itemName, String roomID, Integer numbers[]) { // add fix for consumables
		Item inputItem;
		try {
			inputItem = new Item(Item.getItem(itemName));

			int amount = 1;
			if (inputItem.isStackable) amount = numbers[0];

			if (inventory.containsItem(itemName) && inventory.getItem(itemName).getAmount() < currentRoom.getItem(itemName).getAmount())
				inputItem.setAmount(amount + inventory.getItem(itemName).getAmount());
			else if (inventory.containsItem(itemName) && amount + inventory.getItem(itemName).getAmount() >= currentRoom.getItem(itemName).getAmount())
				return "toomuch";
			else if (amount > currentRoom.getItem(itemName).getAmount()) 
				return "toomuch";
			else
				if (inputItem.isStackable) inputItem.setAmount(amount); // change so that user can specify how many to pick up

			if (!inputItem.roomID.contains(roomID)) {
				inputItem.roomID.add(roomID);
				inputItem.pickedUpAmounts.add(numbers[0]);
			} else inputItem.pickedUpAmounts.set(inputItem.roomID.indexOf(roomID), amount + inputItem.pickedUpAmounts.get(inputItem.roomID.indexOf(roomID)));

			inventory.addToInventory(inputItem, itemName, roomID, amount);
			return null;
		} catch (Exception e) {
			return "error";
		}
	}

	public String itemCanBePickedUp(String itemName) { // add check for consumables
		Item inputItem;
		try {
			inputItem = Item.getItem(itemName);
		} catch (Exception e) {
			return "error";
		}
		if (currentRoom.hasRepeatedItems(itemName)) return "roomrepeated";
		//else if (inventory.hasRepeatedItems(itemName)) return "inventoryrepeated";
		else if (!currentRoom.containsItem(inputItem)) return "roomnotcontains";
		//else if (inventory.containsItem(itemName)) return "inventorycontains";
		else return "";
	}

	public String itemCanBeLookedAt(String itemName) {
		//Item inputItem = Item.getItem(itemName);
		if (currentRoom.hasRepeatedItems(itemName)) return "roomrepeated";
		else if (inventory.containsItem(itemName) || currentRoom.containsItem(Item.getItem(itemName))) return "contains";
		return "";
	}

	/*
	 * Room Getters
	 */
	public Room getRoom() {
		return currentRoom;
	}

	public String getRoomID() {
		return currentRoom.getRoomID();
	}

	public String getRoomName() {
		return currentRoom.getRoomName();
	}

	public String getRoomDescription() {
		return currentRoom.longDescription();
	}

	public String getRoomShortDescription() {
		return currentRoom.getDescription();
	}

	public String getRoomTravelDescription() {
		return currentRoom.travelDescription();
	}

	public Room getNextRoom(String direction) {
		return currentRoom.nextRoom(direction);
	}

	public int getRoomItemAmount() {
		return currentRoom.getItemAmount();
	}

	public int getRoomItemAmount(String itemName) {
		return currentRoom.getItemAmount(itemName);
	}

	public boolean doesRoomContainItem(String itemName) {
		Item inputItem = Item.getItem(itemName);
		return currentRoom.containsItem(inputItem);
	}

	public void updateItems(Player player, String roomID) {
		currentRoom.updateItems(player, roomID);
	}

	/*
	 * Room Setter
	 */
	public void setCurrentRoom(Room currentRoom, Player player) {
		this.currentRoom = currentRoom;
		//updateItems(player);
	}

	/*
	 * Misc Setters
	 */
	public void setName(String name) {
		this.name = name;
	}

	// toString method
	public String toString() {
		return name;
	}
}