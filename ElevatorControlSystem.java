import java.util.*;

enum Direction { UP, DOWN, IMMOBILE }

class ElevatorControlSystem {
	private ArrayList<Elevator> elevators; // References all the elevators in our system
	private HashMap<Integer, Request> pickUpRequests; // Maps floor to the request at that floor

	/**
	 * Add a new elevator to our control system
	 */
	public void addElevator(Elevator e) {
		if (elevators.size() < 16) {
			elevators.add(e);
		} else {
			System.out.println("Maximum number of elevators is 16.");
		}
	}

	/**
	 * Return the elevator with the correct id that we're looking for
	 * I don't need to use this method because I can use getters to access elevator fields
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
	public void pickUp(int start, ArrayList<Passenger> newPassengers, Direction dir) {

		// First check if there is an elevator on this floor and if so, onboard the passengers
		for (Elevator e : elevators) {
			if (e.hasSpace() && e.getFloor() == start) {
				for (Passenger p : newPassengers) {
					e.addGoal(p.destination);
					e.addPassenger(p);
				}
				return;
			}
		}

		// Find the closest elevator either coming to this floor in the same direction or currently immobile
		Elevator closest = null;
		int minFloorDiff = 0;
		for (Elevator e : elevators) {
			if (e.hasSpace() && (e.getDirection() == dir || e.getDirection() == Direction.IMMOBILE)) {
				int difference = Math.abs(e.getFloor() - start);
				if (difference < minFloorDiff) {
					minFloorDiff = difference;
					closest = e;
				}
			}
		}
		closest.addGoal(start);

		// If there already exists a pickup request from this floor, add the new passengers to that request
		if (pickUpRequests.containsKey(start)) {
			pickUpRequests.get(start).passengers.addAll(newPassengers);
		} 

		// Otherwise, add the new request into the list of requests
		else {
			pickUpRequests.put(start, new Request(newPassengers, closest));
		}
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

						// Drop off passengers
						e.dropOff(currFloor);

						// Pick up passengers
						if (pickUpRequests.containsKey(currFloor) && pickUpRequests.get(currFloor).elevator == e) {
							for (Passenger p : pickUpRequests.get(currFloor).passengers) {
								e.addGoal(p.destination);
								e.addPassenger(p);
							}
							pickUpRequests.remove(currFloor);
						}

						// Check if we have to change directions
						if (goalsAbove.isEmpty()) {
							if (goalsBelow.isEmpty()) {
								update(e, currFloor, Direction.IMMOBILE);
							} else {
								update(e, currFloor, Direction.DOWN);
							}
						}

					// Move the elevator one floor if we don't have to load/offload passengers
					} else {
						update(e, currFloor + 1, Direction.UP);
					}
				case DOWN:
					if (goalsBelow.get(0) == currFloor) {
						goalsBelow.remove(0);

						// Drop off passengers
						e.dropOff(currFloor);

						// Pick up passengers
						if (pickUpRequests.containsKey(currFloor) && pickUpRequests.get(currFloor).elevator == e) {
							for (Passenger p : pickUpRequests.get(currFloor).passengers) {
								e.addGoal(p.destination);
								e.addPassenger(p);
							}
							pickUpRequests.remove(currFloor);
						}

						// Check if we have to change directions
						if (goalsBelow.isEmpty()) {
							if (goalsAbove.isEmpty()) {
								update(e, currFloor, Direction.IMMOBILE);
							} else {
								update(e, currFloor, Direction.UP);
							}
						}

					// Move the elevator one floor if we don't have to load/offload passengers
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


class Elevator {
	private int id;
	private int floor;
	private Direction dir;
	private int capacity; // This is a softcap, so the number of passengers may exceed capacity but no more passengers can be added past this limit
	private ArrayList<Passenger> passengers; // Key is floor, value is number of passengers getting off at that floor
	private ArrayList<Integer> goalsAbove; // Goal floors above the current floor sorted increasing
	private ArrayList<Integer> goalsBelow; // Goal floors below the current floor sorted decreasing

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

	public void addPassenger(Passenger p) {
		passengers.add(p);
	}

	/**
	 * Offload all passengers whose destinations are at the input floor
	 */
	public void dropOff(int floor) {
		for (Passenger p : new ArrayList<>(passengers)) {
			if (p.destination == floor) {
				passengers.remove(p);
			}
		}
	}

	public ArrayList<Passenger> getPassengers() {
		return passengers;
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
				int g = goalsBelow.get(i);
				if (g == newGoal) {
					return; // Do nothing if new goal is already in our goals list
				}
				if (g < newGoal) {
					goalsBelow.add(i, newGoal);
					return;
				}
			}
			goalsBelow.add(newGoal);
		} else if (newGoal > floor) {
			for (int i = 0; i < goalsAbove.size(); i++) {
				int g = goalsAbove.get(i);
				if (g == newGoal) {
					return; // Do nothing if new goal is already in our goals list
				}
				if (g > newGoal) {
					goalsAbove.add(i, newGoal);
					return;
				}
			}
			goalsAbove.add(newGoal);
		} else {
			System.out.println("Can't add the current floor as a goal.");
		}
	}

	/**
	 * Returns whether or not an elevator is full
	 */
	public boolean hasSpace() {
		return capacity - passengers.size() > 0;
	}
}


class Passenger {
	public int destination;

	public Passenger(int d) {
		destination = d;
	}
}


class Request {
	public ArrayList<Passenger> passengers;
	public Elevator elevator;

	public Request(ArrayList<Passenger> p, Elevator e) {
		passengers = p;
		elevator = e;
	}
}


