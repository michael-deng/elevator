import java.util.*;

class ElevatorControlSystem {
	private ArrayList<Elevator> elevators;

	/**
	 * Return the elevator with the correct id that we're looking for
	 */
	public Elevator status(int elevatorid) {
		for (Elevator e : elevators) {
			if (e.getid() == elevatorid) {
				return e;
			}
		}
		return null;
	}

	/**
	 * Update the status of an elevator
	 */
	public void update(Elevator e, int floorNum, Direction dir) {
		e.setFloor(floorNum);
		e.setDirection(dir);
		return;
	}

	/**
	 * Receive a pickup request
	 */
	public void pickUp(int start, int destination, Direction dir) {

		// First check if there is an elevator on this floor

		for (Elevator e : elevators) {
			if (!e.isFull() && e.getFloor() == start) {
				e.addGoal(destination);
				return;
			}
		}

		// Find the closest elevator coming to this floor in the same direction or immobile

		Elevator closest = null;
		int minFloorDiff = 0;
		for (Elevator e : elevators) {
			if (!e.isFull() && (e.getDirection() == dir || e.getDirection() == Direction.IMMOBILE)) {
				int difference = Math.abs(e.getFloor() - start);
				if (difference < minFloorDiff) {
					minFloorDiff = difference;
					closest = e;
				}
			}
		}
		closest.addGoal(destination);
		return;
	}

	/**
	 * Time step the simulation
	 */
	public void step() {
		for (Elevator e : elevators) {
			int currFloor = e.getFloor();
			ArrayList<Integer> goalsAbove = e.getGoalsAbove();
			ArrayList<Integer> goalsBelow = e.getGoalsBelow();
			switch (e.getDirection()) {
				case UP:
					if (goalsAbove.get(0) == currFloor) {
						goalsAbove.remove(0);
						if (goalsAbove.isEmpty()) {
							if (goalsBelow.isEmpty()) {
								update(e, currFloor, Direction.IMMOBILE);
							} else {
								update(e, currFloor, Direction.DOWN);
							}
						}
					} else {
						update(e, currFloor + 1, Direction.UP);
					}
				case DOWN:
					if (goalsBelow.get(0) == currFloor) {
						goalsBelow.remove(0);
						if (goalsBelow.isEmpty()) {
							if (goalsAbove.isEmpty()) {
								update(e, currFloor, Direction.IMMOBILE);
							} else {
								update(e, currFloor, Direction.UP);
							}
						}
					} else {
						update(e, currFloor - 1, Direction.DOWN);
					}
				case IMMOBILE:
					if (goalsBelow.size() > 0) {
						update(e, currFloor, Direction.DOWN);
					} else if (goalsAbove.size() > 0) {
						update(e, currFloor, Direction.UP);
					}	
			}
		}
	}
}

enum Direction { UP, DOWN, IMMOBILE }

class Elevator {
	private int id;
	private int floor;
	private Direction dir;
	private int capacity;
	private ArrayList<Integer> goalsAbove; // Goal floors above the current floor sorted increasing
	private ArrayList<Integer> goalsBelow; // Gal floors below the current floor sorted decreasing

	public int getid() {
		return id;
	}

	public void setid(int elevatorid) {
		id = elevatorid;
	}

	public int getFloor() {
		return floor;
	} 

	public void setFloor(int f) {
		floor = f;
	}

	public Direction getDirection() {
		return dir;
	}

	public void setDirection(Direction d) {
		dir = d;
	}

	public ArrayList<Integer> getGoalsAbove() {
		return goalsAbove;
	}

	public ArrayList<Integer> getGoalsBelow() {
		return goalsBelow;
	}

	/**
	 * Add a new target floor for this elevator
	 */
	public void addGoal(int newGoal) {
		if (newGoal < floor) {
			for (int i = 0; i < goalsBelow.size(); i++) {
				if (goalsBelow.get(i) < newGoal) {
					goalsBelow.add(i, newGoal);
					return;
				}
			}
			goalsBelow.add(newGoal);
		} else if (newGoal > floor) {
			for (int i = 0; i < goalsAbove.size(); i++) {
				if (goalsAbove.get(i) > newGoal) {
					goalsAbove.add(i, newGoal);
					return;
				}
			}
			goalsAbove.add(newGoal);
		} else {
			// Handle the case in which the new goal is the same as the current floor
		}
	}

	/**
	 * Returns whether or not an elevator is full
	 */
	public boolean isFull() {
		return goalsAbove.size() + goalsBelow.size() == capacity;
	}

}