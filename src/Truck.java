import java.util.ArrayList;
import java.util.LinkedList;

public class Truck {
    public static int TRUCK_CAPACITY;
    public static int TRUCK_WORKING_TIME;
    private final Location startingLocation;
    private int id;
    private Location currentLocation;
    private Location endLocation;
    private int capacity;
    private int workingtime;

    private Boolean used;
    private Boolean finished;

    private ArrayList<Location> route = new ArrayList<>();
    private ArrayList<Machine> machinesLoaded = new ArrayList<>();
    private LinkedList<Activity> actions = new LinkedList<>();
    private int jobsGiven;
    private String outputString = "";

    public Truck(int id, Location currentLocation, Location endLocation) {
        if (currentLocation == null || endLocation == null) {
            throw new NullPointerException();
        }
        this.id = id;
        this.startingLocation = currentLocation;
        this.currentLocation = currentLocation;
        this.endLocation = endLocation;
        finished = false;
        used = false;
    }
	
	public Truck(Truck t){
		this.startingLocation=t.startingLocation;
		this.id=t.id;
		this.currentLocation=t.currentLocation;
		this.endLocation=t.endLocation;
		this.capacity=t.capacity;
		this.workingtime=t.workingtime;
		this.used=t.used;
		this.finished=t.finished;
		for(Activity a:t.actions){
           if (a instanceof Drop) {
			   this.actions.add(new Drop((Drop)a));
            }
            if (a instanceof Collect) {
				this.actions.add(new Collect((Collect)a));
            }
            if (a instanceof GenericCollect) {
				this.actions.add(new GenericCollect((GenericCollect)a));
            }
            if (a instanceof GenericDrop) {
				this.actions.add(new GenericDrop((GenericDrop)a));
            }
		}
	}


    public boolean checkIfActionListIsPossible(LinkedList<Activity> actions) {
        int estimatedWorkingtime = 0;
        Location loc = startingLocation;
        for (Activity a : actions) {
            if (a instanceof Drop) {
                estimatedWorkingtime += Problem.timeToLocation(loc, ((Drop) a).getLocation()) + ((Drop) a).getMachineType().getServicetime();
                loc = ((Drop) a).getLocation();
            }
            if (a instanceof Collect) {
                estimatedWorkingtime += Problem.timeToLocation(loc, ((Collect) a).getMachine().getOriginalLocation())
                        + ((Collect) a).getMachine().getMachineType().getServicetime();
                loc = ((Collect) a).getMachine().getOriginalLocation();
            }
            if (a instanceof GenericCollect) {
                estimatedWorkingtime += Problem.timeToLocation(loc, ((GenericCollect) a).getDepotUsed().getLocation())
                        + ((GenericCollect) a).getMachine().getMachineType().getServicetime();
                loc = ((GenericCollect) a).getDepotUsed().getLocation();
            }
            if (a instanceof GenericDrop) {
                estimatedWorkingtime += Problem.timeToLocation(loc, ((GenericDrop) a).getDepotUsed().getLocation())
                        + ((GenericDrop) a).getMachine().getMachineType().getServicetime();
                loc = ((GenericDrop) a).getDepotUsed().getLocation();
            }


        }
        estimatedWorkingtime += Problem.timeToLocation(loc, this.endLocation);
        return estimatedWorkingtime <= 600;

    }

    public void deliver(Drop drop, Depot depot, ArrayList<Drop> drops) {
        if (!used) {
            used = true;
        }
        Machine machine = machinesLoaded.stream().filter(x -> x.getMachineType().getId() == drop.getMachineType().getId()).findAny().orElse(null);
        if (machine != null) {

            workingtime = workingtime + Problem.timeToLocation(this.currentLocation, drop.getLocation()) + drop.getMachineType().getServicetime();

            route.add(currentLocation);
            currentLocation = drop.getLocation();
            machinesLoaded.remove(machine);

            drop.setMachineUsed(machine);
            machine.setLocation(drop.getLocation());
            outputString = outputString.concat(currentLocation.getId() + ":" + machine.getId() + " ");
            for (Drop d : drops) {
                d.setPriority(false);
            }
            actions.add(drop);


        } else {
            if (this.currentLocation.getId() != depot.getLocation().getId()) {
                outputString = outputString.concat(this.currentLocation.getId() + " ");
            }
            route.add(currentLocation);
            workingtime += Problem.timeToLocation(this.currentLocation, depot.getLocation()) + drop.getMachineType().getServicetime();
            machine = Problem.machines.stream().filter(x -> !x.isUsed() && x.getLocation().getId() == depot.getLocation().getId() && drop.getMachineType().getId() == x.getMachineType().getId()).findFirst().orElseThrow(NullPointerException::new);
            machinesLoaded.add(machine);

            actions.add(new GenericCollect(machine, depot));

            capacity = machinesLoaded.stream().mapToInt(x -> x.getMachineType().getVolume()).sum();

            outputString = outputString.concat(depot.getLocation().getId() + ":" + machine.getId());
            ArrayList<Machine> machinesToRemove = new ArrayList<>();
            for (Machine machineLoaded : machinesLoaded) {
                if (machineLoaded.isFromCollect() && !machineLoaded.getMachineType().isLimited()) {
                    workingtime += machineLoaded.getMachineType().getServicetime();
                    outputString = outputString.concat(":" + machineLoaded.getId());
                    machineLoaded.setLocation(depot.getLocation());
                    machineLoaded.setUsed(true);
                    machinesToRemove.add(machineLoaded);
                    actions.add(new GenericDrop(machineLoaded, depot));
                }
            }
            for (Machine machineRemove : machinesToRemove) {
                machinesLoaded.remove(machineRemove);
            }


            route.add(depot.getLocation());

            route.add(drop.getLocation());
            currentLocation = drop.getLocation();
            workingtime += Problem.timeToLocation(depot.getLocation(), drop.getLocation()) + drop.getMachineType().getServicetime();
            machine.setLocation(drop.getLocation());

            // actions.add(drop);

            int estimatedDropTime = workingtime;
            Drop lastDrop = drop;
            for (int i = 1; i < drops.size(); i++) {

                if (capacity + drops.get(i).getMachineType().getVolume() <= TRUCK_CAPACITY && drops.get(i).hasPriority()
                        && workingtime + drops.get(i).getMachineType().getServicetime() * 2
                        + Problem.timeToLocation(drops.get(i).getLocation(), this.endLocation)
                        + Problem.timeToLocation(drop.getLocation(), drops.get(i).getLocation()) <= TRUCK_WORKING_TIME
                        && drops.get(i).getId() != drop.getId()) {
                    int finalI = i;
                    Machine p = Problem.machines.stream().filter(x -> !x.isUsed() && x.getLocation().getId() == depot.getLocation().getId()
                            && drops.get(finalI).getMachineType().getId() == x.getMachineType().getId()).findFirst().orElse(null);
                    if (p != null && !machinesLoaded.contains(p)
                            && estimatedDropTime + p.getMachineType().getServicetime()
                            + Problem.timeToLocation(lastDrop.getLocation(), drops.get(i).getLocation())
                            + Problem.timeToLocation(drops.get(i).getLocation(), Problem.findOptimalDepot(this))
                            + Problem.timeToLocation(Problem.findOptimalDepot(this), this.endLocation) + this.calculateEmptyTime() <= TRUCK_WORKING_TIME - (p.getMachineType().isLimited() ? 50 : 0)) {
                        machinesLoaded.add(p);
                        actions.add(new GenericCollect(p, depot));

                        capacity = machinesLoaded.stream().mapToInt(x -> x.getMachineType().getVolume()).sum();
                        estimatedDropTime = estimatedDropTime + p.getMachineType().getServicetime() + Problem.timeToLocation(lastDrop.getLocation(), drops.get(i).getLocation());
                        lastDrop = drops.get(i);
                        workingtime += p.getMachineType().getServicetime();
                        outputString = outputString.concat(":" + p.getId());

                    }
                }
                drops.get(i).setPriority(false);

            }
            machinesLoaded.remove(machine);

            drop.setMachineUsed(machine);

            outputString = outputString.concat(" " + currentLocation.getId() + ":" + machine.getId() + " ");
            actions.add(drop);


        }
        capacity = machinesLoaded.stream().mapToInt(x -> x.getMachineType().getVolume()).sum();

    }

    public void fetch(Collect collect) {
        if (!used) {
            used = true;
            outputString = outputString.concat(this.currentLocation.getId() + " ");

        }
        workingtime = workingtime + Problem.timeToLocation(this.currentLocation, collect.getMachine().getLocation())
                + collect.getMachine().getMachineType().getServicetime();

        route.add(currentLocation);
        currentLocation = collect.getMachine().getLocation();
        machinesLoaded.add(collect.getMachine());
        outputString = outputString.concat(currentLocation.getId() + ":" + collect.getMachine().getId() + " ");
        capacity = machinesLoaded.stream().mapToInt(x -> x.getMachineType().getVolume()).sum();
        collect.getMachine().setFromCollect(true);

        actions.add(collect);

    }

    public boolean isValidList(LinkedList<Activity> listt1) {
        if (TRUCK_WORKING_TIME < calculateWorkingTimeWithList(listt1)) {
            return false;
        }
        if (TRUCK_CAPACITY < calculateMaxCapacity(listt1)) {
            return false;
        }

        return true;
    }

    public int calculateMaxCapacity(LinkedList<Activity> action) {
        int maxCapacity = Integer.MIN_VALUE;
        ArrayList<Machine> machinesLoaded = new ArrayList<>();
        for (Activity a : action) {
            if (machinesLoaded.contains(getMachineFromActivity(a))) {
                machinesLoaded.remove(getMachineFromActivity(a));
            } else {
                machinesLoaded.add(getMachineFromActivity(a));
            }
            int currentCap = machinesLoaded.stream().mapToInt(x -> x.getMachineType().getVolume()).sum();
            if (currentCap > maxCapacity) {
                maxCapacity = currentCap;
            }
        }
        return maxCapacity;
    }

    public Machine getMachineFromActivity(Activity a) {
        Machine m = null;
        if (a instanceof Drop) {
            return ((Drop) a).getMachineUsed();
        }
        if (a instanceof Collect) {
            return ((Collect) a).getMachine();
        }
        if (a instanceof GenericCollect) {
            return ((GenericCollect) a).getMachine();
        }

        if (a instanceof GenericDrop) {
            return ((GenericDrop) a).getMachine();

        }
        throw new RuntimeException("Wrong Activity");
    }

    public void finishAtDepots(Location l) {
        if (capacity != 0 || !machinesLoaded.isEmpty()) {
            emptyAtDepots(l);
        } else {
            route.add(currentLocation);
        }
        route.add(endLocation);
        workingtime += Problem.timeToLocation(currentLocation, endLocation);
        outputString = outputString.concat(" " + (endLocation.getId()));


    }

    public void emptyAtDepots(Location l) {
        route.add(currentLocation);
        workingtime = workingtime + Problem.timeToLocation(this.currentLocation, l);
        currentLocation = l;
        outputString = outputString.concat(String.valueOf(currentLocation.getId()));

        for (Machine m : machinesLoaded) {
            workingtime += m.getMachineType().getServicetime();
            outputString = outputString.concat(":" + m.getId());
            m.setLocation(l);
            m.setUsed(true);
            actions.add(new GenericDrop(m, Problem.depots.stream().filter(x -> l.getId() == x.getId()).findFirst().orElseThrow(NullPointerException::new)));


        }
        outputString = outputString.concat(" ");
        route.add(l);
        machinesLoaded.clear();
        capacity = machinesLoaded.stream().mapToInt(x -> x.getMachineType().getVolume()).sum();


    }


    public boolean isAtDepot(ArrayList<Depot> depots) {
        return depots.stream().anyMatch(x -> x.getLocation().getId() == this.currentLocation.getId());
    }

    public int calculateDistanceTraveled() {
        return calculateDistanceTraveledWithList(actions);
    }

    public int calculateDistanceTraveledWithList(LinkedList<Activity> action) {

        int distance = 0;
        if (!action.isEmpty()) {
            if (action.get(0) instanceof Drop) {
                distance += Problem.distanceToLocation(startingLocation, getLocationFromActivity(action.get(0)));
            }
            if (action.get(0) instanceof Collect) {
                distance += Problem.distanceToLocation(startingLocation, getLocationFromActivity(action.get(0)));
            }
            if (action.get(0) instanceof GenericCollect) {
                distance += Problem.distanceToLocation(startingLocation, getLocationFromActivity(action.get(0)));
            }

            if (action.get(0) instanceof GenericDrop) {
                distance += Problem.distanceToLocation(startingLocation, getLocationFromActivity(action.get(0)));

            }

            for (int i = 0; i < action.size() - 1; i++) {

                distance += Problem.distanceToLocation(getLocationFromActivity(action.get(i)), getLocationFromActivity(action.get(i + 1)));
            }
            distance += Problem.distanceToLocation(getLocationFromActivity(action.get(action.size() - 1)), endLocation);
        } else {
            distance = Problem.distanceToLocation(startingLocation, endLocation);
        }
        return distance;
    }

    public Location getLocationFromActivity(Activity a) {
        if (a instanceof Drop) {
            return ((Drop) a).getLocation();
        }
        if (a instanceof Collect) {
            return ((Collect) a).getMachine().getOriginalLocation();
        }
        if (a instanceof GenericCollect) {
            return ((GenericCollect) a).getMachine().getOriginalLocation();
        }

        if (a instanceof GenericDrop) {
            return ((GenericDrop) a).getDepotUsed().getLocation();
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {

        this.currentLocation = currentLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getWorkingtime() {
        return workingtime;
    }

    public void setWorkingtime(int workingtime) {
        this.workingtime = workingtime;
    }

    public Boolean isUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }


    public Boolean isFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public ArrayList<Machine> getMachinesLoaded() {
        return machinesLoaded;
    }

    public void setMachinesLoaded(ArrayList<Machine> machinesLoaded) {
        this.machinesLoaded = machinesLoaded;
    }

    public void addMachine(Machine m) {
        machinesLoaded.add(m);
    }

    @Override
    public String toString() {
        return "Truck{" +
                "id=" + id +
                ", currentLocation=" + currentLocation +
                ", endLocation=" + endLocation +
                ", capacity=" + capacity +
                ", workingtime=" + workingtime +
                ", used=" + used +
                ", finished=" + finished +
                ", route=" + route +
                ", machinesLoaded=" + machinesLoaded +
                '}';
    }

    public int getJobsGiven() {
        return jobsGiven;
    }

    public void increaseJobsGiven() {
        this.jobsGiven++;
    }

    public String getOutputString() {
        outputString = "" + startingLocation.getId();
        Location oldLocation = startingLocation;
        Location loc = startingLocation;
        for (Activity a : actions) {
            int id = -1;
            if (a instanceof Drop) {
                loc = ((Drop) a).getLocation();
                id = ((Drop) a).getMachineUsed().getId();
            }
            if (a instanceof Collect) {
                loc = ((Collect) a).getMachine().getOriginalLocation();
                id = ((Collect) a).getMachine().getId();
            }
            if (a instanceof GenericCollect) {
                loc = ((GenericCollect) a).getMachine().getOriginalLocation();
                id = ((GenericCollect) a).getMachine().getId();
            }

            if (a instanceof GenericDrop) {
                loc = ((GenericDrop) a).getDepotUsed().getLocation();
                id = ((GenericDrop) a).getMachine().getId();
            }
            if (loc.getId() != oldLocation.getId()) {
                outputString = outputString.concat(" " + loc.getId() + ":" + id);

            } else {
                outputString = outputString.concat(":" + id);

            }
            oldLocation = loc;

        }
        outputString = outputString.concat(" " + endLocation.getId());


        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }

    public int calculateEmptyTime() {
        int time = 0;
        for (Machine m : machinesLoaded) {
            time += m.getMachineType().getServicetime();

        }
        return time;
    }

    public LinkedList<Activity> getActions() {
        return actions;
    }

    public void setActions(LinkedList<Activity> actions) {
        this.actions = actions;
    }

    public int calculateWorkingTimeWithList(LinkedList<Activity> action) {
        int time = 0;
        if (!action.isEmpty()) {
            time += Problem.timeToLocation(startingLocation, getLocationFromActivity(action.get(0)));


            for (int i = 0; i < action.size() - 1; i++) {

                if (action.get(i) instanceof Drop) {
                    time += ((Drop) action.get(i)).getMachineType().getServicetime();
                }
                if (action.get(i) instanceof Collect) {
                    time += ((Collect) action.get(i)).getMachine().getMachineType().getServicetime();
                }
                if (action.get(i) instanceof GenericCollect) {
                    time += ((GenericCollect) action.get(i)).getMachine().getMachineType().getServicetime();
                }

                if (action.get(i) instanceof GenericDrop) {
                    time += ((GenericDrop) action.get(i)).getMachine().getMachineType().getServicetime();
                }
                time += Problem.timeToLocation(getLocationFromActivity(action.get(i)), getLocationFromActivity(action.get(i + 1)));
            }
            if (action.get(action.size() - 1) instanceof Drop) {
                time += ((Drop) action.get(action.size() - 1)).getMachineType().getServicetime();
            }
            if (action.get(action.size() - 1) instanceof Collect) {
                time += ((Collect) action.get(action.size() - 1)).getMachine().getMachineType().getServicetime();
            }
            if (action.get(action.size() - 1) instanceof GenericCollect) {
                time += ((GenericCollect) action.get(action.size() - 1)).getMachine().getMachineType().getServicetime();
            }

            if (action.get(action.size() - 1) instanceof GenericDrop) {
                time += ((GenericDrop) action.get(action.size() - 1)).getMachine().getMachineType().getServicetime();
            }
            time += Problem.timeToLocation(getLocationFromActivity(action.get(action.size() - 1)), endLocation);
        }
        return time;
    }

    public int calculateWorkingTime() {
        return calculateWorkingTimeWithList(actions);

    }

    public Location getStartingLocation() {
        return startingLocation;
    }
}
