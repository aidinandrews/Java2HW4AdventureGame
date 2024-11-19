/*
 * File: Adventure.java
 * --------------------
 * This program plays the Adventure game from Assignment #4.
 */

import java.io.*;
import java.util.*;

/* Class: Adventure */
/**
 * This class is the main program class for the Adventure game.
 */

public class Adventure {

	// Use this scanner for any console input
	private static Scanner scan = new Scanner(System.in);
	private static boolean runGame = true;
	private static final ArrayList<AdvRoom> rooms = new ArrayList<>();
	private static final Map<String, AdvObject> objects = new HashMap<>();
	private static final Map<String, AdvCommand> commands = new HashMap<>();
	private static final Map<String, String> synonyms = new HashMap<>();
	private static AdvRoom currentRoom;
	private static final ArrayList<AdvObject> inventory = new ArrayList<>();
	private static final boolean RUN_TEST = false;
	private static final boolean ALLOW_CUSTOM_GAMES = false;

	public Adventure() {
		assert !rooms.isEmpty() : "There are no rooms to explore in this adventure!";
		currentRoom = rooms.getFirst();
		currentRoom.setVisited(true);
		executeLookCommand();
	}

	/**
	 * This method is used only to test the program
	 */
	public static void setScanner(Scanner theScanner) {
		scan = theScanner;
	}

	/**
	 * This method is not used in the submitted program as it conflicts with the
	 * assumptions of the unit tests.  It does however allow the user to pick arbitrary
	 * file locations for custom games.
	 * @param resourcePath file location of a games' folder.
	 * @return is a list of game options for the player to pick.
	 */
	private static String[] getGameOptions(String resourcePath) {
		File directory = new File(resourcePath);
		if (!directory.exists()) return null;

		File[] fileList = directory.listFiles();
		if (fileList == null) return null;

		ArrayList<String> names = new ArrayList<>();
		for (File f : fileList) {
			String name = f.getName();
			int endIndex = name.indexOf("Rooms");
			if (endIndex == -1) continue; // If there is no 'rooms' file, there is no game.
			names.add(name.substring(0, endIndex));
		}
		return names.toArray(new String[0]);
	}

	/**
	 * Prompts the user to give a directory to a game folder.
	 * Also allows the user to pick one of the 'built in' games assumed to exist with
	 * this program.
	 *
	 * @param gameListDirectory is the directory of the games folder to be given back to the caller.
	 * @return is a list of names for games found in that directory.
	 */
	private static String[] chooseGameDirectory(String[] gameListDirectory)  {
		System.out.println("Enter 'default' for default games list or 'custom' to load a custom game.");
		String input = scan.nextLine();
		boolean correctEntry = input.equals("default") || input.equals("custom");
		while(!correctEntry) {
			System.out.println("Please enter either 'default' or 'custom'.");
			input = scan.nextLine();
			correctEntry = input.equals("default") || input.equals("custom");
		}
		String[] names;
		if (input.equals("default")) {
			gameListDirectory[0] = "src/main/resources";
			return getGameOptions(gameListDirectory[0]);
		}
		else {
			System.out.println("Please enter the directory to your custom game folder.");
			input = scan.nextLine();
			names = getGameOptions(input);
			while (names == null) {
				System.out.println("No game files were found at that directory.  " +
						"Please enter another path or 'default' for a list of default game options.");
				input = scan.nextLine();
				if (input.equals("default")) {
					gameListDirectory[0] = "src/main/resources";
					return getGameOptions(gameListDirectory[0]);
				}
				gameListDirectory[0] = input;
				names = getGameOptions(gameListDirectory[0]);
			}
			return names;
		}
	}

	/**
	 * Higher level function for choosing a game from either the provided list of default
	 * games or a custom directory to a custom game somewhere on the user's system.
	 * @return is the directory of the chosen game.
	 */
	private static String chooseGame() {
		String input = "";
		String[]directory = new String[1];
		boolean choseGame = false;

		while(!choseGame) { // Try to allow the user to mess up a lot.
			String[] names = chooseGameDirectory(directory);
			System.out.println("Game options:");
			for (String name : names) {
				System.out.println(name);
			}
			System.out.println("input 'back' to go back to selecting a game directory.");
			System.out.println("input 'quit' to quit program.");

			while(!choseGame) {
				input = scan.nextLine();

				if (input.equals("back")) break;
				if (input.equals("quit")) return null;

				for (String name : names) {
					if (input.equals(name)) {
						choseGame = true;
						break;
					}
				}
				if (!choseGame) { System.out.println("input not recognised"); }
			}
		}
		return directory[0] + "/" + input;
	}

	/**
	 * Fills the rooms map with rooms and keys to those rooms, dependent on a
	 * properly formatted txt file.
	 * @param gameDirectory is the directory that contains the ...Rooms.txt file.
	 */
	private static void getRooms(String gameDirectory) {
		rooms.clear();

		Scanner s;
		File roomsFile = new File(gameDirectory + "Rooms.txt");

		try { s = new Scanner(roomsFile); }
		catch(FileNotFoundException exception) {
			System.out.println("Game did not have a Rooms file!");
			return;
		}

		while (s.hasNextLine()) {
			AdvRoom r = AdvRoom.readFromFile(s);
			if (r == null) continue;

			rooms.add(r);
			for (AdvMotionTableEntry e : r.getMotionTable()) // allow for custom directions:
				commands.put(e.getDirection(), new AdvMotionCommand(e.getDirection()));
		}
	}

	/**
	 * Fills the rooms map with objects, dependent on a properly formatted txt file.
	 * @param gameDirectory is the directory that contains the ...Objects.txt file.
	 */
	private static void getObjects(String gameDirectory) {
		Scanner s;
		File objsFile = new File(gameDirectory + "Objects.txt");

		try { s = new Scanner(objsFile); }
		catch(FileNotFoundException exception) { return; }

		while (s.hasNextLine()) {
			AdvObject obj = AdvObject.readFromFile(s);
			if (obj == null) continue;

			objects.put(obj.getName(), obj);
			rooms.get(obj.getInitialLocation()).addObject(obj);
		}
	}

	/**
	 * There are a number of commands that must be manually set, and this is that list.
	 * Movement commands are game-specific, and set via the ...Rooms.txt file of the
	 * given game.
	 */
	private static void setCommandMap() {
		commands.put("QUIT", AdvCommand.QUIT);
		commands.put("HELP", AdvCommand.HELP);
		commands.put("INVENTORY", AdvCommand.INVENTORY);
		commands.put("LOOK", AdvCommand.LOOK);
		commands.put("TAKE", AdvCommand.TAKE);
		commands.put("DROP", AdvCommand.DROP);
		// move commands are set in the setRooms() function.
	}

	/**
	 * Synonyms are different ways to key into rooms via direction keywords, items via
	 * item name keywords, ect.  This function fills a map with various synonyms to these
	 * keywords, allowing them to be used to call the same commands.
	 * @param gameDirectory is the directory that contains the ...Synonyms.txt file.
	 */
	private static void getSynonyms(String gameDirectory) {
		Scanner s;
		File objsFile = new File(gameDirectory + "Synonyms.txt");
		try { s = new Scanner(objsFile); }
		catch(FileNotFoundException exception) {
			// not having synonyms is not necessarily game-breaking, so no need to throw an error.
			return;
		}

		// Map the command ids to themselves, so we can just use the synonyms map when checking input:
		for (Map.Entry<String, AdvCommand> entry : commands.entrySet())
			synonyms.put(entry.getKey(), entry.getKey());
		for (Map.Entry<String, AdvObject> obj : objects.entrySet())
			synonyms.put(obj.getKey(), obj.getKey());

		String line;
		// Finally we can fill the synonyms list:
		while(s.hasNextLine() && !(line = s.nextLine()).isEmpty()) {
			String[] parts = line.split("=", 2);
			synonyms.put(parts[0], parts[1]);
		}
	}

	/**
	 * Sets up all the data structures for a new adventure.
	 */
	private static void setup() {
		runGame = true;
		inventory.clear();
		commands.clear();
		rooms.clear();
		objects.clear();
		synonyms.clear();

		String gameDirectory;

		while (rooms.isEmpty()) {
			if (ALLOW_CUSTOM_GAMES) {
				if (RUN_TEST) gameDirectory = "src/main/resources/Crowther";
				else gameDirectory = chooseGame();
				if (gameDirectory == null) return;
			}
			else { // unit tests:
				gameDirectory = scan.nextLine();
			}

			setCommandMap();
			getRooms(gameDirectory);
			getObjects(gameDirectory);
			getSynonyms(gameDirectory);

			if (rooms.isEmpty())
				System.out.println("The game has no rooms!  Please select a different game.");
		}
	}

	/**
	 * Runs the adventure program
	 */
	public static void main(String[] args) {
		setup();
		Adventure adventure = new Adventure();
		adventure.run();
	}

	/**
	 * Prompts the user for an input and converts the input into a command info if it is valid.
	 * @return is the converted command info.
	 */
	private AdvCommand.Info getCommand() {
		String input = scan.nextLine().trim().toUpperCase();
		System.out.println(input);
		String[] commandParts = input.split(" ", 64);
		AdvCommand.Info info = new AdvCommand.Info();

		if (!synonyms.containsKey(commandParts[0])) return info;

		String commandKey = synonyms.get(commandParts[0]);
		info.command = commands.get(commandKey);

		if (commandParts.length > 1)
			// modifiers allow for more information to be stored in a command.
			// They are used in various ways, and sometimes not at all in the different
			// command functions.
            info.modifiers = Arrays.copyOfRange(commandParts, 1, commandParts.length);

		return info;
	}

	/**
	 * Runs the text adventure.
	 */
	private void run() {
		while (runGame) {
			int roomIndex = currentRoom.getRoomIndex();

			while (runGame && roomIndex == currentRoom.getRoomIndex()) {
				AdvCommand.Info commandInfo = getCommand();

				if (commandInfo.command == null)
					System.out.println("invalid input");
				else
					// roomIndex will not match if the command is a valid move command, breaking the while loop.
					commandInfo.command.execute(this, commandInfo.modifiers);
			}
		}
	}

	/**
	 * Helper function that will recursively call the motion command if there is a
	 * 'Forced' direction in the current room.  Some forced directions will still need
	 * a key, so it is necessary to run the motion command checks each time.
	 */
	private void checkForced() {
		for (AdvMotionTableEntry mt : currentRoom.getMotionTable()) {
			if (mt.getDirection().equals("FORCED")) {
				// Look will not be caught unless checked here if there are 2+ forced directions in a row.
				if (!currentRoom.hasBeenVisited()) executeLookCommand();
				executeMotionCommand("FORCED");
				break;
			}
		}
	}

	/* Method: executeMotionCommand(direction) */
	/**
	 * Executes a motion command. This method is called from the
	 * AdvMotionCommand class to move to a new room.
	 *
	 * @param direction
	 *            The string indicating the direction of motion
	 */
	public void executeMotionCommand(String direction) {
		for (AdvMotionTableEntry mt : currentRoom.getMotionTable()) {
			boolean moveTo = false;

			// skip directions not desired by input.
			if (!mt.getDirection().equals(direction)) continue;
			else if (mt.getKeyName() == null) moveTo = true;
			else // Key needed:
				for (AdvObject o : inventory)
					if (o.getName().equals(mt.getKeyName())) {
						moveTo = true;
						System.out.println(o.getName() + " used.");
						break;
					}

			if (moveTo) {
				// A room index of -1 indicates the game is beaten:
				if (mt.getDestinationRoom() == -1) { runGame = false; return; }
				currentRoom = rooms.get(mt.getDestinationRoom());

				// Print room description:
				if (currentRoom.hasBeenVisited()) System.out.println(currentRoom.getName());
				else { executeLookCommand(); currentRoom.setVisited(true); }

				checkForced(); // if there is a forced direction inside the new room, take it.
				return;
			}
		}
		System.out.println("Invalid direction.");
	}

	/**
	 * Implements the QUIT command. This command should ask the user to confirm
	 * the quit request and, if so, should exit from the play method. If not,
	 * the program should continue as usual.
	 */
	public void executeQuitCommand() {
		System.out.println("Confirm quit by entering 'y', 'quit', or 'q' again.");
		String line = scan.nextLine();
		if (line.equals("quit") || line.equals("q")|| line.equals("y")) {
			System.out.println("Thank you for playing!");
			runGame = false;
		}
		else {
			System.out.println("Quit aborted.");
		}
	}

	/* Method: executeHelpCommand() */
	/**
	 * Implements the HELP command. Your code must include some help text for
	 * the user.
	 */
	public void executeHelpCommand(String[] modifiers) {
		// Generic help list:
		if (modifiers == null) {
			System.out.println("Commands:");
			System.out.println("LOOK: Gives a description of the current room and any visible objects inside it.");
			System.out.println("INVENTORY: Gives a description of each item in your inventory.");
			System.out.println("TAKE [item name]: Places a visible object in your inventory.");
			System.out.println("DROP [item name]: drops the corresponding item from your inventory into the room.");
			System.out.println("NORTH, SOUTH, EAST, WEST, IN, OUT: Move to the room in the given direction.");
			System.out.println("HELP [command/item name]: Lists all synonyms for the given command/item.");
		}
		else { // Give synonyms for modifier input:
			if (modifiers.length > 1) {
				System.out.println("Please ask about one word at a time.");
				return;
			}
			String name = modifiers[0];
			String[] set = synonyms.keySet().toArray(new String[0]);
			ArrayList<String> syn = new ArrayList<>();
			for (String k : set)
				if (synonyms.get(k).equals(name)) syn.add(k.toLowerCase());

			if (syn.isEmpty()) System.out.println("There are no synonyms for " + name + ".");
			else {
				System.out.print("Synonyms: ");
				for (int i = 0; i < syn.size() - 1; i++)
					System.out.print(syn.get(i) + ", ");
				System.out.println(syn.getLast() + ".");
			}
		}
	}

	/* Method: executeLookCommand() */
	/**
	 * Implements the LOOK command. This method should give the full description
	 * of the room and its contents.
	 */
	public void executeLookCommand() {
		for (String s : currentRoom.getDescription())
			System.out.println(s);

		if (currentRoom.getObjectCount() == 0) return;

		// Give a name/description of each object found in the room:
		System.out.print("\nYou see ");
		if (currentRoom.getObjectCount() == 1) {
			System.out.print(currentRoom.getObject((0)).getName().toLowerCase());
		}
		else if (currentRoom.getObjectCount() == 2) {
			System.out.print(currentRoom.getObject(0).getName().toLowerCase() +
					" and " + currentRoom.getObject(1).getName().toLowerCase());
		}
		else {
			int i = 0;
			for (; i < currentRoom.getObjectCount() - 1; i++) {
				AdvObject o = currentRoom.getObject(i);
				System.out.print(o.getName().toLowerCase() + ", ");
			}
			System.out.print("and " + currentRoom.getObject(i).getName().toLowerCase());
		}
		System.out.println(" ripe for the taking.");
	}

	/* Method: executeInventoryCommand() */
	/**
	 * Implements the INVENTORY command. This method should display a list of
	 * what the user is carrying.
	 */
	public void executeInventoryCommand() {
		if (inventory.isEmpty()) {
			System.out.println("You are destitute of objects.");
			return;
		}

		System.out.print("You hold ");
		if (inventory.size() == 1) {
			System.out.print(inventory.getFirst().getDescription().toLowerCase());
		}
		else if (inventory.size() == 2) {
			System.out.print(inventory.getFirst().getDescription().toLowerCase() +
					" and " + inventory.getLast().getDescription().toLowerCase());
		}
		else {
			for (int i = 0; i < inventory.size() - 1; i++) {
				AdvObject o = inventory.get(i);
				System.out.println(o.getDescription().toLowerCase() + ", ");
			}
			System.out.print("and " + inventory.getLast().getDescription().toLowerCase());
		}

		String flavor = switch(inventory.size()) {
			case 1 -> " lonesomely in one hand.";
			case 2 -> " companionably in both hands.";
			case 3 -> " firmly in your arms.";
			case 4 -> " awkwardly in both arms.";
			case 5 -> " uncomfortably in both arms.";
			case 6 -> " stacked teetering in your hands.";
			case 7 -> " strenuously on your person.";
			default -> " impossibly.";
		};
		System.out.println(flavor);
	}

	/* Method: executeTakeCommand(obj) */
	/**
	 * Implements the TAKE command. This method should check that the object is
	 * in the room and deliver a suitable message if not.
	 *
	 * @param modifiers is the name of the AdvObject you want to take.
	 */
	public void executeTakeCommand(String[] modifiers) {
		ArrayList<String> invalidObjectNames = new ArrayList<>();

		if (modifiers == null) {
			System.out.println("Please include an object to pick up.");
			return;
		}

		for (String m : modifiers) {
			AdvObject o = objects.get(synonyms.get(m));
			if (o == null) continue;

			if (currentRoom.containsObject(o)) {
				inventory.add(o);
				currentRoom.removeObject(o);
				System.out.println(o.getName().toLowerCase() + " get.");
				break;
			}
			else invalidObjectNames.add(m.toLowerCase());
		}

		if (invalidObjectNames.isEmpty()) return;

		String initialInvalidObject = invalidObjectNames.getFirst();
		String w = (initialInvalidObject.charAt(initialInvalidObject.length() - 1) == 's') ? "are" : "is";
		switch(invalidObjectNames.size()) {
			case 1:
				System.out.println("There " + w + " no " + initialInvalidObject + " to pick up.");
				break;
			case 2:
				System.out.println("There " + w + " no " + initialInvalidObject + " or " + invalidObjectNames.getLast() + " to pick up.");
				break;
			default:
				System.out.print("There " + w + " no " + initialInvalidObject);
				for (int i = 1; i < invalidObjectNames.size() - 1; i++) {
					String o = invalidObjectNames.get(i);
					System.out.print( ", " + o);
				}
				System.out.println(", or " + invalidObjectNames.getLast() + " to pick up.");
		}
	}

	/* Method: executeDropCommand(obj) */
	/**
	 * Implements the DROP command. This method should check that the user is
	 * carrying the object and deliver a suitable message if not.
	 *
	 * @param modifiers The name of the AdvObject you want to drop
	 */
	public void executeDropCommand(String[] modifiers) {
		ArrayList<String> invalidObjects = new ArrayList<>();

		if (modifiers == null) {
			System.out.println("Please include an object(s) to drop.");
			return;
		}

		for (String m : modifiers) {
			boolean found = false;
			String s = synonyms.get(m);
			if (s == null) continue;

			for (AdvObject i : inventory) {
				if (s.equals(i.getName())) {
					currentRoom.addObject(i);
					inventory.remove(i);
					System.out.println(i.getName().toLowerCase() + " dropped.");
					found = true;
					break;
				}
			}
			if (!found) {
				invalidObjects.add(m.toLowerCase());
			}
		}

		if (invalidObjects.isEmpty()) return;

		String objName = invalidObjects.getFirst();
		String w = (objName.charAt(objName.length() - 1) == 's') ? "are" : "is";
		switch(invalidObjects.size()) {
			case 1:
				System.out.println("There " + w + " no " + objName + " to drop.");
				break;
			case 2:
				System.out.println("There " + w + " no " + objName + " or " + invalidObjects.getLast() + " to drop.");
				break;
			default:
				System.out.print("There " + w + " no " + objName);
				for (int i = 1; i < invalidObjects.size() - 1; i++) {
					String o = invalidObjects.get(i);
					System.out.print( ", " + o);
				}
				System.out.println(", or " + invalidObjects.getLast() + " to drop.");
		}
	}
}