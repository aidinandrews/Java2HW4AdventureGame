/*
 * File: AdvRoom.java
 * ------------------
 * This file defines a class that models a single room in the
 * Adventure game.
 */

import java.util.*;

/* Class: AdvRoom */
/**
 * This class defines a single room in the Adventure game. A room is
 * characterized by the following properties:
 * 
 * <ul>
 * <li>A room number, which must be greater than zero
 * <li>Its name, which is a one-line string identifying the room
 * <li>Its description, which is a multiline array describing the room
 * <li>A list of objects contained in the room
 * <li>A flag indicating whether the room has been visited
 * <li>A motion table specifying the exits and where they lead </li>
 * 
 * The external format of the room data file is described in the assignment
 * handout. The comments on the methods exported by this class show how to use
 * the initialized data structure.
 */

public class AdvRoom  {

	private int index;
	private String name;
	private String[] description;
	private final ArrayList<AdvObject> objects = new ArrayList<>();
	private boolean wasVisited;
	private ArrayList<AdvMotionTableEntry> motionTables = new ArrayList<>();

	/* Method: getRoomNumber() */
	/**
	 * Returns the room number.
	 * 
	 * @usage int roomNumber = room.getRoomNumber();
	 * @return The room number
	 */
	public int getRoomIndex() {
		return index;
	}

	/* Method: getName() */
	/**
	 * Returns the room name, which is its one-line description.
	 * 
	 * @usage String name = room.getName();
	 * @return The room name
	 */
	public String getName() {
		return name;
	}

	/* Method: getDescription() */
	/**
	 * Returns an array of strings that correspond to the long description of
	 * the room (including the list of the objects in the room).
	 * 
	 * @usage String[] description = room.getDescription();
	 * @return An array of strings giving the long description of the room
	 */
	public String[] getDescription() {
		return description;
	}

	/* Method: addObject(obj) */
	/**
	 * Adds an object to the list of objects in the room.
	 * 
	 * @usage room.addObject(obj);
	 * @param obj advObject to be added
	 */
	public void addObject(AdvObject obj) {
		for (AdvObject o : objects) {
			if (o.getName().equals(obj.getName())){
				return;
			}
		}
		objects.add(obj);
	}

	/* Method: removeObject(obj) */
	/**
	 * Removes an object from the list of objects in the room.
	 * 
	 * @usage room.removeObject(obj);
	 * @param obj AdvObject to be removed
	 */
	public void removeObject(AdvObject obj) {
		for (int i = 0; i < objects.size(); i++) {
			AdvObject o = objects.get(i);
			if (o.getName().equals(obj.getName())) {
				objects.remove(i);
				return;
			}
		}
	}

	/* Method: containsObject(obj) */
	/**
	 * Checks whether the specified object is in the room.
	 * 
	 * @usage if (room.containsObject(obj)) . . .
	 * @param obj AdvObject being tested
	 * @return true if the object is in the room, and false otherwise
	 */
	public boolean containsObject(AdvObject obj) {
		for (AdvObject o : objects) {
			if (o.getName().equals(obj.getName())){
				return true;
			}
		}
		return false;
	}

	/* Method: getObjectCount() */
	/**
	 * Returns the number of objects in the room.
	 * 
	 * @usage int nObjects = room.getObjectCount();
	 * @return The number of objects in the room
	 */
	public int getObjectCount() {
		return objects.size();
	}

	/* Method: getObject(index) */
	/**
	 * Returns the specified element from the list of objects in the room.
	 * 
	 * @usage AdvObject obj = room.getObject(index);
	 * @return The AdvObject at the specified index position
	 */
	public AdvObject getObject(int index) {
		if (index < 0 || index > (objects.size() - 1)) {
			return null;
		}
		return objects.get(index);
	}

	/* Method: setVisited(flag) */
	/**
	 * Sets the flag indicating that this room has been visited according to the
	 * value of the parameter. Calling setVisited(true) means that the room has
	 * been visited; calling setVisited(false) restores its initial unvisited
	 * state.
	 * 
	 * @usage room.setVisited(flag);
	 * @param flag
	 *            The new state of the "visited" flag
	 */
	public void setVisited(boolean flag) {
		wasVisited = flag;
	}

	/* Method: hasBeenVisited() */
	/**
	 * Returns true if the room has previously been visited.
	 * 
	 * @usage if (room.hasBeenVisited()) . . .
	 * @return true if the room has been visited; false otherwise
	 */
	public boolean hasBeenVisited() {
		return wasVisited;
	}

	/* Method: getMotionTable() */
	/**
	 * Returns the motion table associated with this room, which is an array of
	 * directions, room numbers, and enabling objects stored in a
	 * AdvMotionTableEntry.
	 * 
	 * @usage AdvMotionTableEntry[] motionTable = room.getMotionTable();
	 * @return The array of motion table entries associated with this room
	 */
	public AdvMotionTableEntry[] getMotionTable() {
		AdvMotionTableEntry[] r = new AdvMotionTableEntry[motionTables.size()];
		for(int i = 0; i < motionTables.size();i++) {
			r[i] = motionTables.get(i);
		}
		return r;
	}

	/* Method: readFromFile(rd) */
	/**
	 * Reads the data for this room from the Scanner scan, which must have been
	 * opened by the caller. This method returns a room if the room
	 * initialization is successful; if there are no more rooms to read,
	 * readFromFile returns null.
	 * 
	 * @usage AdvRoom room = AdvRoom.readFromFile(scan);
	 * @param scan
	 *            A scanner open on the rooms data file
	 * @return a room if successfully read; null if at end of file
	 */
	public static AdvRoom readFromFile(Scanner scan) {
		if (!scan.hasNextLine()) return null;

		AdvRoom room = new AdvRoom();
		room.description = new String[]{};
		room.motionTables = new ArrayList<>();
		String line;

		while(!scan.hasNextInt()) {
			scan.nextLine();
		}
		room.index = scan.nextInt() - 1;
		scan.nextLine();
		room.name = scan.nextLine();
		while(!(line = scan.nextLine()).equals("-----")) {
			String[] newDesc = new String[room.description.length+1];
            System.arraycopy(room.description, 0, newDesc, 0, room.description.length);
			room.description = newDesc;
			room.description[room.description.length-1] = line;
		}
		while(scan.hasNextLine() && !(line = scan.nextLine()).isEmpty()){
			Scanner s = new Scanner(line);
			String dir = s.next();
			String[] idAndKey = s.next().split("/", 2);
			int id = Integer.parseInt(idAndKey[0]) - 1;
			String key = (idAndKey.length > 1) ? idAndKey[1] : null;
			room.motionTables.add(new AdvMotionTableEntry(dir, id, key));
		}
		return room;
	}
}
