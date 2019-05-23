import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Problem {
    public static ArrayList<Location> locations = new ArrayList<>();
    public static ArrayList<Depot> depots = new ArrayList<>();
    public ArrayList<Truck> trucks = new ArrayList<>();
    public static ArrayList<MachineType> machineTypes = new ArrayList<>();
    public static ArrayList<Machine> machines = new ArrayList<>();
    public static ArrayList<Drop> drops = new ArrayList<>();
    public static ArrayList<Collect> collects = new ArrayList<>();
    private ArrayList<Integer> options = new ArrayList<>();

    private ArrayList<Truck> bestSolution = new ArrayList<>();
    private int bestSolutionDistance = Integer.MAX_VALUE;
    private double temperature = 10;
    private double factor = 0.999999;

    private LinkedList<Integer> currentList = new LinkedList<>();
    private LinkedList<Integer> bestList = new LinkedList<>();
    private int improves = 0;

    int failedInserts = 0;
    int failedFullSwaps = 0;
    int failedSwaps = 0;
    int failedsingeSwap = 0;
    long[] time = new long[4];

    int[] attempts = new int[4];

    private static int[][] TIMEMATRIX;
    private static int[][] DISTANCEMATRIX;
    private File file;
    private int lastBestDistance = Integer.MAX_VALUE;

    public Problem(File file) {
        this.file = file;
        String line = null;

        try {
            Scanner sc = new Scanner(this.file);
            while (sc.hasNext()) {
                line = sc.nextLine();
                if (line.contains("TRUCK_CAPACITY")) {
                    Truck.TRUCK_CAPACITY = Integer.parseInt(line.split(": ")[1]);
                }
                if (line.contains("TRUCK_WORKING_TIME")) {
                    Truck.TRUCK_WORKING_TIME = Integer.parseInt(line.split(": ")[1]);
                }
                if (line.contains("LOCATIONS")) {
                    int maxLocations = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < maxLocations; i++) {
                        locations.add(new Location(sc.nextInt(), sc.nextDouble(), sc.nextDouble(), sc.next()));
                    }
                }
                if (line.contains("DEPOTS")) {
                    int maxDepots = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < maxDepots; i++) {
                        int id = sc.nextInt();
                        int locid = sc.nextInt();
                        depots.add(new Depot(id,
                                locations.stream().filter(x -> x.getId() == locid).findFirst().orElse(null)));
                    }
                }
                if (line.contains("TRUCKS")) {
                    int maxTrucks = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < maxTrucks; i++) {
                        int id = sc.nextInt();
                        int startloc = sc.nextInt();
                        int endloc = sc.nextInt();
                        trucks.add(new Truck(id,
                                locations.stream().filter(x -> x.getId() == startloc).findFirst().orElse(null),
                                locations.stream().filter(x -> x.getId() == endloc).findFirst().orElse(null)));
                    }
                }
                if (line.contains("MACHINE_TYPES")) {
                    int maxMachineTypes = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < maxMachineTypes; i++) {
                        machineTypes.add(new MachineType(sc.nextInt(), sc.nextInt(), sc.nextInt(), sc.next()));
                    }
                }
                if (line.contains("MACHINES")) {
                    int maxMachines = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < maxMachines; i++) {
                        int id = sc.nextInt();
                        int typeid = sc.nextInt();
                        int locid = sc.nextInt();
                        machines.add(new Machine(id,
                                machineTypes.stream().filter(x -> x.getId() == typeid).findFirst().orElse(null),
                                locations.stream().filter(x -> x.getId() == locid).findFirst().orElse(null)));
                    }
                }
                if (line.contains("DROPS")) {
                    int maxDrops = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < maxDrops; i++) {
                        int id = sc.nextInt();
                        int typeid = sc.nextInt();
                        int locid = sc.nextInt();
                        drops.add(new Drop(id,
                                machineTypes.stream().filter(x -> x.getId() == typeid).findFirst().orElse(null),
                                locations.stream().filter(x -> x.getId() == locid).findFirst().orElse(null)));
                    }
                }
                if (line.contains("COLLECTS")) {
                    int maxCollects = Integer.parseInt(line.split(" ")[1]);
                    for (int i = 0; i < maxCollects; i++) {
                        int id = sc.nextInt();
                        int machineid = sc.nextInt();
                        collects.add(new Collect(id,
                                machines.stream().filter(x -> x.getId() == machineid).findFirst().orElse(null)));
                    }
                }
                if (line.contains("TIME_MATRIX")) {
                    int maxLocations = Integer.parseInt(line.split(" ")[1]);
                    TIMEMATRIX = new int[maxLocations][maxLocations];
                    for (int i = 0; i < maxLocations; i++) {
                        for (int j = 0; j < maxLocations; j++) {
                            TIMEMATRIX[i][j] = sc.nextInt();
                        }
                    }
                }
                if (line.contains("DISTANCE_MATRIX")) {
                    int maxLocations = Integer.parseInt(line.split(" ")[1]);
                    DISTANCEMATRIX = new int[maxLocations][maxLocations];

                    for (int i = 0; i < maxLocations; i++) {
                        for (int j = 0; j < maxLocations; j++) {
                            DISTANCEMATRIX[i][j] = sc.nextInt();
                        }
                    }
                    options.add(0);
                    for (int i = 0; i < drops.size(); i++) {
                        options.add(1000 + i);
                    }
                    for (int i = 0; i < collects.size(); i++) {
                        options.add(2000 + i);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void writeSolutionFile(String extra) throws IOException {
        int distance = bestSolution.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum();
        String os = System.getProperty("os.name").toLowerCase();
        String folderSeparator=os.contains("win")?"\\":"/";
        new File("OutputFiles").mkdir();
        File f = new File("OutputFiles"+folderSeparator + file.getName().substring(0, file.getName().length() - 4) + extra + "_" + distance + "_solution" + ".txt");
      //  f.getParentFile().mkdirs();
        FileWriter fileWriter = new FileWriter(f);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("PROBLEM: " + file.getName());
        System.out.println("Distance: " + distance + " " + extra);
        System.out.println("Temperature: " + temperature);
        System.out.println("Current distance: " + trucks.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum());
        System.out.println("Attempts: ");
        for (int i : attempts) {
            System.out.println(i);
        }
        System.out.println("improves: " + improves);
        System.out.println("FailedSWaps: " + failedFullSwaps + " " + failedInserts + " " + failedSwaps + " " + failedsingeSwap);

        for (long l : time) {
            System.out.println(l);

        }
        printWriter.println("DISTANCE: " + distance);
        int trucksUsed = bestSolution.stream().mapToInt(i -> i.getActions().isEmpty() ? 0 : 1).sum();
        printWriter.println("TRUCKS: " + trucksUsed);
     /*   File debug = new File("OutputFiles\\" + file.getName().substring(0, file.getName().length() - 4) + "_list" + ".csv");
        FileWriter fw = new FileWriter(debug, true);
        for (int i = 0; i < currentList.size(); i++) {
            fw.write('\n');
            fw.write(currentList.get(i) + "," + bestList.get(i) + ",");
        }
        fw.write("," + extra);
        fw.close();
        currentList.clear();
        bestList.clear();*/


        for (Truck t : bestSolution.stream().filter(x -> !x.getActions().isEmpty()).collect(Collectors.toList())) {
            printWriter.print(t.getId() + " " + t.calculateDistanceTraveled() + " " + t.calculateWorkingTime() + " " + t.getOutputString());
            printWriter.println();


        }
        if (temperature < 0.1) {
            temperature = 5;
        }

        lastBestDistance = distance;

        printWriter.close();
        fileWriter.close();
        System.out.println();
    }

    public void optimize(int minutes) {
        //TODO: RANDOM SEED, static now for debugging
        Random rand = new Random();
        String os = System.getProperty("os.name").toLowerCase();
        String folderSeparator=os.contains("win")?"\\":"/";

        for (int i = 0; i < minutes * 3; i++) {
            Long time = System.currentTimeMillis();
            if (i == minutes * 3 - 2 || i == minutes * 3 - 1) {
                temperature = 0.5;
            }
            while (time + 20 * 1000 > System.currentTimeMillis()) {
                selectStep(rand.nextInt());
            }
            try {
                this.writeSolutionFile(folderSeparator + (i + 1) * 20 + "s");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void selectStep(int seed) {
      /*  if (currentList.isEmpty() || currentList.get(currentList.size() - 1) != trucks.stream().mapToInt(Truck::calculateDistanceTraveled).sum()) {
            currentList.add(this.trucks.stream().mapToInt(Truck::calculateDistanceTraveled).sum());

            bestList.add(this.bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum());
        }*/

        Random rand = new Random(seed);
        int action = rand.nextInt(25);
        if (action == -1) {
            attempts[0] = attempts[0] + 1;
           /* int temp = improves;
            long tijd = System.nanoTime();*/
            switchLists(trucks.get(rand.nextInt(trucks.size())), trucks.get(rand.nextInt(trucks.size())));
            /*if (temp == improves) {
                failedFullSwaps++;
            }
            time[0] = (time[0] + (System.nanoTime() - tijd));*/
        } else if (action <= 3) {
            attempts[1] = attempts[1] + 1;
            //long tijd = System.nanoTime();
            int temp = improves;
            insert(rand.nextInt());
           if (temp == improves) {
                failedInserts++;
            }
          //  time[1] = (time[1] + (System.nanoTime() - tijd));

        } else if (action <= 22) {
            attempts[2] = attempts[2] + 1;

          //  long tijd = System.nanoTime();
            int temp = improves;
            swap(rand.nextInt());
            if (temp == improves) {
                failedSwaps++;
            }
            //time[2] = (time[2] + (System.nanoTime() - tijd));

        } else {
            attempts[3] = attempts[3] + 1;
           // long tijd = System.nanoTime();
            int temp = improves;
            swapViaMachineType(rand.nextInt());
            if (temp == improves) {
                failedsingeSwap++;
            }
           // time[3] = (time[3] + (System.nanoTime() - tijd));


        }


    }

    public void swapViaMachineType(int seed) {
        Random rand = new Random(seed);

        Truck t = trucks.get(rand.nextInt(trucks.size()));
        if (t.getActions().isEmpty()) {
            return;
        }

        int actionIndex = rand.nextInt(t.getActions().size());
        Activity a = t.getActions().get(actionIndex);
        int pairIndex = findPairedActivityIndex(a, t.getActions(), actionIndex);
        LinkedList<Activity> listt1 = t.getActions();

        int bestOption = -1;
        int resultForBestOption = Integer.MAX_VALUE - 1;

        int oldDistance = Integer.MAX_VALUE;
        for (int i = 0; i < trucks.size(); i++) {
            Truck volunteerTruck = trucks.get(i);
            if (volunteerTruck.getActions().isEmpty() || t.getId() == volunteerTruck.getId()) {
                break;
            }
            oldDistance = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();

            Activity firstAct = volunteerTruck.getActions().stream().filter(x -> volunteerTruck.getMachineFromActivity(x).getMachineType().getId()
                    == volunteerTruck.getMachineFromActivity(a).getMachineType().getId()).findAny().orElse(null);
            if (firstAct == null) {
                break;
            }
            int firstIndex = volunteerTruck.getActions().indexOf(firstAct);
            int secondIndex = findPairedActivityIndex(firstAct, volunteerTruck.getActions(), firstIndex);
            if (actionIndex > pairIndex) {
                int temp = actionIndex;
                actionIndex = pairIndex;
                pairIndex = temp;
            }

            if (firstIndex > secondIndex) {
                int temp = firstIndex;
                firstIndex = secondIndex;
                secondIndex = temp;
            }
            if (isGeneric(t.getActions().get(actionIndex)) + isGeneric(t.getActions().get(pairIndex))
                    + isGeneric(volunteerTruck.getActions().get(firstIndex)) + isGeneric(volunteerTruck.getActions().get(secondIndex)) == 2) {

                if (isGeneric(t.getActions().get(actionIndex)) + isGeneric(volunteerTruck.getActions().get(secondIndex)) == 2) {
                    swapSingleAction(pairIndex, secondIndex, t, volunteerTruck, false);

                    Activity tempDrop = t.getActions().remove(pairIndex);
                    Activity tempCollect = t.getActions().remove(actionIndex);
                    if (resultForBestOption >= t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()
                            && t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions())) {
                        resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                        bestOption = trucks.indexOf(volunteerTruck) * 1000000 + pairIndex * 1000 + secondIndex;
                    }
                    t.getActions().add(actionIndex, tempCollect);
                    t.getActions().add(pairIndex, tempDrop);
                    swapSingleAction(pairIndex, secondIndex, t, volunteerTruck, false);


                    swapSingleAction(actionIndex, firstIndex, t, volunteerTruck, false);


                    tempDrop = volunteerTruck.getActions().remove(secondIndex);
                    tempCollect = volunteerTruck.getActions().remove(firstIndex);

                    if (resultForBestOption >= t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()
                            && t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions())) {
                        resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                        bestOption = trucks.indexOf(volunteerTruck) * 1000000 + actionIndex * 1000 + firstIndex;
                    }
                    volunteerTruck.getActions().add(firstIndex, tempCollect);
                    volunteerTruck.getActions().add(secondIndex, tempDrop);
                    swapSingleAction(actionIndex, firstIndex, t, volunteerTruck, false);


                }

                if (isGeneric(t.getActions().get(actionIndex)) + isGeneric(volunteerTruck.getActions().get(secondIndex)) == 2) {

                    swapSingleAction(pairIndex, secondIndex, t, volunteerTruck, false);

                    Activity tempDrop = t.getActions().remove(pairIndex);

                    Activity tempCollect = t.getActions().remove(actionIndex);

                    if (resultForBestOption >= t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()
                            && t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions())) {
                        resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                        bestOption = trucks.indexOf(volunteerTruck) * 1000000 + pairIndex * 1000 + secondIndex;
                    }
                    t.getActions().add(actionIndex, tempCollect);
                    t.getActions().add(pairIndex, tempDrop);

                    swapSingleAction(pairIndex, secondIndex, t, volunteerTruck, false);


                    swapSingleAction(actionIndex, firstIndex, t, volunteerTruck, false);

                    tempDrop = t.getActions().remove(pairIndex);

                    tempCollect = t.getActions().remove(actionIndex);
                    if (resultForBestOption >= t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()
                            && t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions())) {
                        resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                        bestOption = trucks.indexOf(volunteerTruck) * 1000000 + actionIndex * 1000 + firstIndex;
                    }
                    t.getActions().add(actionIndex, tempCollect);
                    t.getActions().add(pairIndex, tempDrop);
                    swapSingleAction(actionIndex, firstIndex, t, volunteerTruck, false);


                }


            } else if ((isGeneric(t.getActions().get(actionIndex)) + isGeneric(t.getActions().get(pairIndex))
                    + isGeneric(volunteerTruck.getActions().get(firstIndex)) + isGeneric(volunteerTruck.getActions().get(secondIndex)) == 3)) {
                if (isGeneric(t.getActions().get(actionIndex)) + isGeneric(t.getActions().get(pairIndex)) == 2) {

                    t.getActions().remove(pairIndex);
                    t.getActions().remove(actionIndex);
                    if (trucks.stream().mapToInt(Truck::calculateDistanceTraveled).sum() <= bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum()) {
                        fullCopySolution(trucks);
                    }
                    return;
                } else {
                    volunteerTruck.getActions().remove(secondIndex);
                    volunteerTruck.getActions().remove(firstIndex);
                    if (trucks.stream().mapToInt(Truck::calculateDistanceTraveled).sum() <= bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum()) {
                        fullCopySolution(trucks);
                    }
                    return;
                }
            } else if ((isGeneric(t.getActions().get(actionIndex)) + isGeneric(t.getActions().get(pairIndex))
                    + isGeneric(volunteerTruck.getActions().get(firstIndex)) + isGeneric(volunteerTruck.getActions().get(secondIndex)) == 4)) {
                throw new RuntimeException("wtf code");
            }

            swapSingleAction(pairIndex, secondIndex, t, volunteerTruck, false);


            if (resultForBestOption >= t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()
                    && t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions())) {
                resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                bestOption = trucks.indexOf(volunteerTruck) * 1000000 + pairIndex * 1000 + secondIndex;
            }

            swapSingleAction(pairIndex, secondIndex, t, volunteerTruck, false);


            swapSingleAction(actionIndex, firstIndex, t, volunteerTruck, false);


            if (resultForBestOption >= t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()
                    && t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions())) {
                resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                bestOption = trucks.indexOf(volunteerTruck) * 1000000 + actionIndex * 1000 + firstIndex;
            }
            swapSingleAction(actionIndex, firstIndex, t, volunteerTruck, false);


        }


        if (resultForBestOption <= oldDistance && bestOption != -1) {

            swapSingleAction((bestOption % 1000000) / 1000, bestOption % 1000, t, trucks.get(bestOption / 1000000), true);
            temperature *= factor;
            improves++;
            if (trucks.stream().mapToInt(Truck::calculateDistanceTraveled).sum() <=
                    (bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum() == 0 ? Integer.MAX_VALUE
                            : bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum())) {
                fullCopySolution(trucks);
                bestSolutionDistance = bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum();
            }
        } else if (bestOption != -1 && Math.exp((oldDistance - resultForBestOption) / temperature) > rand.nextDouble()) {
            swapSingleAction((bestOption % 1000000) / 1000, bestOption % 1000, t, trucks.get(bestOption / 1000000), true);
            temperature *= factor;
            improves++;

        }
    }

    public void swapSingleAction(int index1, int index2, Truck t1, Truck t2, boolean finalMove) {
        Activity act1 = t1.getActions().get(index1);
        Activity act2 = t2.getActions().get(index2);
        Activity a = t1.getActions().get(findPairedActivityIndex(act1, t1.getActions(), index1));
        Activity b = t2.getActions().get(findPairedActivityIndex(act2, t2.getActions(), index2));
        if (act1 instanceof Collect) {
            if (b instanceof Drop) {
                ((Drop) b).setMachineUsed(((Collect) act1).getMachine());
            }
            if (b instanceof GenericDrop) {
                ((GenericDrop) b).setMachine(((Collect) act1).getMachine());
            }
        } else if (act1 instanceof GenericCollect) {
            if (b instanceof Drop) {
                ((Drop) b).setMachineUsed(((GenericCollect) act1).getMachine());
            }
            if (b instanceof GenericDrop) {
                ((GenericDrop) b).setMachine(((GenericCollect) act1).getMachine());
            }
        } else if (act1 instanceof Drop) {
            if (b instanceof Collect) {
                ((Drop) act1).setMachineUsed(((Collect) b).getMachine());
            }
            if (b instanceof GenericCollect) {
                ((Drop) act1).setMachineUsed(((GenericCollect) b).getMachine());
            }
        } else if (act1 instanceof GenericDrop) {
            if (b instanceof Collect) {
                ((GenericDrop) act1).setMachine(((Collect) b).getMachine());
            }
            if (b instanceof GenericCollect) {
                ((GenericDrop) act1).setMachine(((GenericCollect) b).getMachine());
            }
        }
        t1.getActions().remove(index1);

        if (act2 instanceof Collect) {
            if (a instanceof Drop) {
                ((Drop) a).setMachineUsed(((Collect) act2).getMachine());
            }
            if (a instanceof GenericDrop) {
                ((GenericDrop) a).setMachine(((Collect) act2).getMachine());
            }
        } else if (act2 instanceof GenericCollect) {
            if (a instanceof Drop) {
                ((Drop) a).setMachineUsed(((GenericCollect) act2).getMachine());
            }
            if (a instanceof GenericDrop) {
                ((GenericDrop) a).setMachine(((GenericCollect) act2).getMachine());
            }
        } else if (act2 instanceof Drop) {
            if (a instanceof Collect) {
                ((Drop) act2).setMachineUsed(((Collect) a).getMachine());
            }
            if (a instanceof GenericCollect) {
                ((Drop) act2).setMachineUsed(((GenericCollect) a).getMachine());
            }
        } else if (act2 instanceof GenericDrop) {
            if (a instanceof Collect) {
                ((GenericDrop) act2).setMachine(((Collect) a).getMachine());
            }
            if (a instanceof GenericCollect) {
                ((GenericDrop) act2).setMachine(((GenericCollect) a).getMachine());
            }
        }
        t2.getActions().remove(index2);


        t1.getActions().add(index1, act2);
        if (act2 instanceof GenericDrop) {
            ((GenericDrop) act2).setLastDepot(((GenericDrop) act2).getDepotUsed());
            ((GenericDrop) act2).setDepotUsed(findDepotOnRoute(t1, index1, index1, act2));
        }
        if (act2 instanceof GenericCollect) {
            ((GenericCollect) act2).setLastDepot(((GenericCollect) act2).getDepotUsed());
            ((GenericCollect) act2).setDepotUsed(findDepotOnRoute(t1, index1, index1, act2));
        }
        t2.getActions().add(index2, act1);
        if (act1 instanceof GenericDrop) {
            ((GenericDrop) act1).setLastDepot(((GenericDrop) act1).getDepotUsed());
            ((GenericDrop) act1).setDepotUsed(findDepotOnRoute(t1, index1, index1, act1));
        }
        if (act1 instanceof GenericCollect) {
            ((GenericCollect) act1).setLastDepot(((GenericCollect) act1).getDepotUsed());
            ((GenericCollect) act1).setDepotUsed(findDepotOnRoute(t1, index1, index1, act1));
        }
        if (finalMove) {
            if (isGeneric(a) + isGeneric(act2) == 2) {
                t1.getActions().remove(a);

                t1.getActions().remove(act2);
            }
            if (isGeneric(b) + isGeneric(act1) == 2) {
                t2.getActions().remove(b);
                t2.getActions().remove(act1);
            }
        }

    }

    public int isGeneric(Activity a) {
        return ((a instanceof GenericDrop) || (a instanceof GenericCollect)) ? 1 : 0;
    }

    public void swap(int seed) {
        Random rand = new Random(seed);

        Truck t = trucks.get(rand.nextInt(trucks.size()));
        if (t.getActions().isEmpty()) {
            return;
        }
        int actionIndex = rand.nextInt(t.getActions().size());
        Activity a = t.getActions().get(actionIndex);
        int pairIndex = findPairedActivityIndex(a, t.getActions(), actionIndex);
        LinkedList<Activity> listt1 = t.getActions();

        Truck volunteerTruck = trucks.get(rand.nextInt(trucks.size()));

        if (t.getId() == volunteerTruck.getId())
            return;
        if (volunteerTruck.getActions().isEmpty())
            return;
        int oldDistance = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
        ArrayList<Integer> usedIndexes = new ArrayList<>();

        int bestOption = -1;
        int resultForBestOption = Integer.MAX_VALUE;

        for (int i = 0; i < volunteerTruck.getActions().size(); i++) {
            if (!usedIndexes.contains(i)) {
                int activityIndex2 = findPairedActivityIndex(volunteerTruck.getActions().get(i), volunteerTruck.getActions(), i);
                trySwap(pairIndex, actionIndex, i, activityIndex2, t, volunteerTruck);
                usedIndexes.add(i);
                usedIndexes.add(activityIndex2);
                if (t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions()) /*&&
                                t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled() - oldDistance < 0*/) {
                    if (resultForBestOption > t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()) {
                        bestOption = i * 1000 + activityIndex2;
                        resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                    }
                }
                trySwap(pairIndex, actionIndex, i, activityIndex2, t, volunteerTruck);
            }
        }

        if (resultForBestOption <= oldDistance) {
            improves++;

            trySwap(pairIndex, actionIndex, bestOption / 1000, bestOption % 1000, t, volunteerTruck);
            if (trucks.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum() <=
                    (bestSolution.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum() == 0 ? Integer.MAX_VALUE
                            : bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum())) {
                fullCopySolution(trucks);
                bestSolutionDistance = bestSolution.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum();
            }
        } else if (bestOption != -1 && Math.exp((oldDistance - resultForBestOption) / temperature) > rand.nextDouble()) {
            improves++;

            trySwap(pairIndex, actionIndex, bestOption / 1000, bestOption % 1000, t, volunteerTruck);
        }
        temperature *= factor;


    }


    private void trySwap(int pairIndex, int actionIndex, int i, int activityIndex2, Truck t1, Truck t2) {
        Activity drop = null;
        Activity collect = null;
        Activity drop2 = t2.getActions().remove(activityIndex2);
        Activity collect2 = t2.getActions().remove(i);
        if (pairIndex > actionIndex) {

            drop = t1.getActions().remove(pairIndex);
            collect = t1.getActions().remove(actionIndex);
            t1.getActions().add(actionIndex, collect2);
            t1.getActions().add(pairIndex, drop2);
        } else {
            drop = t1.getActions().remove(actionIndex);
            collect = t1.getActions().remove(pairIndex);
            t1.getActions().add(pairIndex, collect2);
            t1.getActions().add(actionIndex, drop2);
        }
        t2.getActions().add(i, collect);
        t2.getActions().add(activityIndex2, drop);

        if (drop instanceof GenericDrop) {

            GenericDrop d = (GenericDrop) drop;
            d.setLastDepot(d.getDepotUsed());
            d.setDepotUsed(findDepotOnRoute(t2, i, activityIndex2, drop));

        }
        if (collect instanceof GenericCollect) {
            GenericCollect c = (GenericCollect) collect;
            c.setLastDepot(c.getDepotUsed());
            c.setDepotUsed(findDepotOnRoute(t2, i, activityIndex2, collect));
        }

        if (drop2 instanceof GenericDrop) {

            GenericDrop d = (GenericDrop) drop2;
            d.setLastDepot(d.getDepotUsed());
            d.setDepotUsed(findDepotOnRoute(t1, i, activityIndex2, drop2));

        }
        if (collect2 instanceof GenericCollect) {
            GenericCollect c = (GenericCollect) collect2;
            c.setLastDepot(c.getDepotUsed());
            c.setDepotUsed(findDepotOnRoute(t1, i, activityIndex2, collect2));
        }


    }

    @SuppressWarnings("Duplicates")
    public void insert(int seed) {
        Random rand = new Random(seed);

        Truck t = trucks.get(rand.nextInt(trucks.size()));
        if (!t.getActions().isEmpty()) {
            int actionIndex = rand.nextInt(t.getActions().size());
            Activity a = t.getActions().get(actionIndex);
            int pairIndex = findPairedActivityIndex(a, t.getActions(), actionIndex);
            LinkedList<Activity> listt1 = t.getActions();

            Truck volunteerTruck = trucks.get(rand.nextInt(trucks.size()));

            if (t.getId() == volunteerTruck.getId())
                return;
            int oldDistance = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();


            LinkedList<Activity> listt2 = volunteerTruck.getActions();


            int bestOption = -1;
            int resultForBestOption = Integer.MAX_VALUE;

            int startingIndex = listt2.isEmpty() ? 0 : rand.nextInt(listt2.size());
            int endIndex = listt2.isEmpty() ? 0 : rand.nextInt(listt2.size());
            endIndex=endIndex<=startingIndex?startingIndex+10:endIndex;
            for (int insertIndex1 = startingIndex; insertIndex1 < startingIndex + 10; insertIndex1++) {
                for (int insertIndex2 = insertIndex1; insertIndex2 < endIndex; insertIndex2++) {
                    int usedIndex1 = volunteerTruck.getActions().isEmpty() ? 0 : insertIndex1 % volunteerTruck.getActions().size();
                    int usedIndex2 = volunteerTruck.getActions().isEmpty() ? 0 : insertIndex2 % volunteerTruck.getActions().size();
                    if (usedIndex1 <= usedIndex2) {
                        doInsertMove(pairIndex, actionIndex, usedIndex1, usedIndex2, t, volunteerTruck);
                        if (t.isValidList(t.getActions()) && volunteerTruck.isValidList(volunteerTruck.getActions()) /*&&
                                t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled() - oldDistance < 0*/) {
                            if (resultForBestOption > t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled()) {
                                bestOption = usedIndex1 * 1000 + usedIndex2;
                                resultForBestOption = t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled();
                            }
                            /*result.put(usedIndex1 * 1000 + usedIndex2,
                                    t.calculateDistanceTraveled() + volunteerTruck.calculateDistanceTraveled() - oldDistance);*/
                        }
                        undoInsertMove(pairIndex, actionIndex, usedIndex1, usedIndex2, t, volunteerTruck);
                    }
                }
            }

            if (resultForBestOption <= oldDistance) {
                improves++;

                doInsertMove(pairIndex, actionIndex, bestOption / 1000, bestOption % 1000, t, volunteerTruck);
                if (trucks.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum() <=
                        (bestSolution.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum() == 0 ? Integer.MAX_VALUE
                                : bestSolution.stream().mapToInt(Truck::calculateDistanceTraveled).sum())) {
                    fullCopySolution(trucks);
                    bestSolutionDistance = bestSolution.stream().mapToInt(i -> i.calculateDistanceTraveled()).sum();
                }
            } else if (bestOption != -1 && Math.exp((oldDistance - resultForBestOption) / temperature) > rand.nextDouble()) {
                doInsertMove(pairIndex, actionIndex, bestOption / 1000, bestOption % 1000, t, volunteerTruck);
                improves++;

            }
            temperature *= factor;

            //region OldSelectImprovement
		   /* for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
                for (int i = 0; i <Math.abs( entry.getValue()); i++) {
                    list.add(entry.getKey());
                }
            }
            if (!list.isEmpty()) {
                int finalDecision = list.get(rand.nextInt(list.size()));
                doInsertMove(pairIndex, actionIndex, finalDecision / 1000, finalDecision % 1000, t, volunteerTruck);
            }*/
            //endregion
            //region I don't even wanna know why
            /*if (pairIndex > actionIndex) {
                listt2.add(insertIndex2, t.getActions().get(pairIndex));
                listt2.add(insertIndex1, t.getActions().get(actionIndex));

                listt1.remove(pairIndex);
                listt1.remove(actionIndex);
            } else {
                listt2.add(insertIndex2, t.getActions().get(actionIndex));
                listt2.add(insertIndex1, t.getActions().get(pairIndex));
                listt1.remove(actionIndex);
                listt1.remove(pairIndex);
            }
            System.out.println(t.isValidList(listt1));
            System.out.println(volunteerTruck.isValidList(listt2));
            if (!(t.isValidList(listt1) && volunteerTruck.isValidList(listt2))) {
                return;
            }
            System.out.println(oldDistance);
            int newDistance = t.calculateDistanceTraveledWithList(listt1) + volunteerTruck.calculateDistanceTraveledWithList(listt2);
            if (newDistance < oldDistance) {
                t.setActions(listt1);
                volunteerTruck.setActions(listt2);
            }*/
            //endregion

        }

    }

    private void fullCopySolution(ArrayList<Truck> trucks) {
        this.bestSolution = new ArrayList<>();
        for (Truck t : trucks) {
            this.bestSolution.add(new Truck(t));
        }

    }

    private void undoInsertMove(int pairIndex, int actionIndex, int insertIndex1, int insertIndex2,
                                Truck t1, Truck t2) {
        Activity collect = t2.getActions().remove(insertIndex1);
        Activity drop = t2.getActions().remove(insertIndex2);
        if (drop instanceof GenericDrop) {
            ((GenericDrop) drop).setDepotUsed(((GenericDrop) drop).getLastDepot());
        }
        if (collect instanceof GenericCollect) {
            ((GenericCollect) collect).setDepotUsed(((GenericCollect) collect).getLastDepot());
        }
        if (pairIndex > actionIndex) {


            t1.getActions().add(actionIndex, collect);
            t1.getActions().add(pairIndex > t1.getActions().size() ? t1.getActions().size() : pairIndex, drop);

        } else {
            t1.getActions().add(pairIndex, collect);
            t1.getActions().add(actionIndex > t1.getActions().size() ? t1.getActions().size() : actionIndex, drop);
        }
    }

    private void doInsertMove(int pairIndex, int actionIndex, int insertIndex1, int insertIndex2,
                              Truck t1, Truck t2) {
        Activity drop = null;
        Activity collect = null;
        if (pairIndex > actionIndex) {

            drop = t1.getActions().remove(pairIndex);
            collect = t1.getActions().remove(actionIndex);

        } else {
            drop = t1.getActions().remove(actionIndex);
            collect = t1.getActions().remove(pairIndex);
        }
        t2.getActions().add(insertIndex2, drop);
        t2.getActions().add(insertIndex1, collect);
        if (drop instanceof GenericDrop) {

            GenericDrop d = (GenericDrop) drop;
            d.setLastDepot(d.getDepotUsed());
            d.setDepotUsed(findDepotOnRoute(t2, insertIndex1, insertIndex2, drop));

        }
        if (collect instanceof GenericCollect) {
            GenericCollect c = (GenericCollect) collect;
            c.setLastDepot(c.getDepotUsed());
            c.setDepotUsed(findDepotOnRoute(t2, insertIndex1, insertIndex2, collect));
        }
    }


    public Depot findDepotOnRoute(Truck t, int insertIndex1, int insertIndex2, Activity act) {
        Location lastLocation = t.getLocationFromActivity(act);
        Location nextLocation = t.getLocationFromActivity(act);
        int index = insertIndex2 + 1;
        boolean bool = false;
        while (!(nextLocation.getId() == t.getLocationFromActivity(act).getId() && !bool)) {
            if (index >= t.getActions().size()) {
                index = Integer.MAX_VALUE;
                bool = true;
                nextLocation = t.getEndLocation();
            } else if (!t.getActions().get(index).equals(act)) {
                nextLocation = t.getLocationFromActivity(t.getActions().get(index));
                bool = true;
            }
            index++;

        }
        bool = false;
        index = insertIndex2 + 1;

        while (!(lastLocation.getId() == t.getLocationFromActivity(act).getId() && !bool)) {
            if (index < 0) {
                bool = true;
                lastLocation = t.getStartingLocation();
            } else if (!t.getActions().get(index).equals(act)) {
                lastLocation = t.getLocationFromActivity(t.getActions().get(index));
                bool = true;
            }
            index--;

        }
        int shortestDistance = Integer.MAX_VALUE;
        Depot depot = null;
        for (Depot d : depots) {
            if (shortestDistance >= (distanceToLocation(lastLocation, d.getLocation()) + distanceToLocation(d.getLocation(), nextLocation))) {
                shortestDistance = distanceToLocation(lastLocation, d.getLocation()) + distanceToLocation(d.getLocation(), nextLocation);
                depot = d;
            }

        }
        return depot;

    }


    @SuppressWarnings("Duplicates")
    public int findPairedActivityIndex(Activity a, LinkedList<Activity> actions, int index) {
     /*   System.out.println(index);
        System.out.println(actions.toString());
        System.out.println(a.toString());*/
        //     System.out.println(temperature);
        if (index != -1) {
            if (a instanceof Drop) {
                Drop d = (Drop) a;
                for (int i = 0; i < actions.size(); i++) {
                    if (actions.get(i) instanceof Collect) {
                        if (((Collect) actions.get(i)).getMachine().getId() == d.getMachineUsed().getId()) {
                            return i;
                        }
                    }
                    if (actions.get(i) instanceof GenericCollect) {
                        if (((GenericCollect) actions.get(i)).getMachine().getId() == d.getMachineUsed().getId()) {
                            return i;
                        }
                    }

                }
            }
            if (a instanceof Collect) {
                Collect c = (Collect) a;
                for (int i = 0; i < actions.size(); i++) {
                    if (actions.get(i) instanceof Drop) {
                        if (((Drop) actions.get(i)).getMachineUsed().getId() == c.getMachine().getId()) {
                            return i;
                        }
                    }
                    if (actions.get(i) instanceof GenericDrop) {
                        if (((GenericDrop) actions.get(i)).getMachine().getId() == c.getMachine().getId()) {
                            return i;
                        }
                    }
                }
            }
            if (a instanceof GenericCollect) {
                GenericCollect c = (GenericCollect) a;

                for (int i = 0; i < actions.size(); i++) {
                    if (actions.get(i) instanceof Drop) {
                        if (((Drop) actions.get(i)).getMachineUsed().getId() == c.getMachine().getId()) {
                            return i;
                        }
                    }
                    if (actions.get(i) instanceof GenericDrop) {
                        if (((GenericDrop) actions.get(i)).getMachine().getId() == c.getMachine().getId()) {
                            return i;
                        }
                    }

                }
            }

            if (a instanceof GenericDrop) {
                GenericDrop d = (GenericDrop) a;
                for (int i = 0; i < actions.size(); i++) {
                    if (actions.get(i) instanceof Collect) {
                        if (((Collect) actions.get(i)).getMachine().getId() == d.getMachine().getId()) {
                            return i;
                        }
                    }
                    if (actions.get(i) instanceof GenericCollect) {
                        if (((GenericCollect) actions.get(i)).getMachine().getId() == d.getMachine().getId()) {
                            return i;
                        }
                    }

                }
            }
        }
        throw new RuntimeException("Pair not found");
    }

    public void switchLists(Truck t1, Truck t2) {
        int a = t1.getId();
        int b = t2.getId();
        // System.out.println(t1.checkIfActionListIsPossible(t2.getActions()) && t2.checkIfActionListIsPossible(t1.getActions()));
        if (t1.checkIfActionListIsPossible(t2.getActions()) && t2.checkIfActionListIsPossible(t1.getActions())) {
            int oldDistance = t1.calculateDistanceTraveled() + t2.calculateDistanceTraveled();
            int newDistance = t1.calculateDistanceTraveledWithList(t2.getActions()) + t2.calculateDistanceTraveledWithList(t1.getActions());
            if (oldDistance > newDistance) {
                LinkedList<Activity> temp = t1.getActions();
                t1.setActions(t2.getActions());
                t2.setActions(temp);
            }

        }

    }


    public void initialSolution() throws IOException {
        Random rand = new Random();
        ArrayList<Drop> PriorityDropList = new ArrayList<>();
        HashMap<MachineType, Integer> quantity = new HashMap<>();
        for (Drop d : drops) {
            if (!quantity.containsKey(d.getMachineType())) {
                quantity.put(d.getMachineType(), 0);
            }
            quantity.put(d.getMachineType(), quantity.get(d.getMachineType()) + 1);


        }
        for (Map.Entry<MachineType, Integer> entry : quantity.entrySet()) {
            if (entry.getKey().getQuantity() == entry.getValue() || entry.getKey().getQuantity() == entry.getValue() + 1) {
                entry.getKey().setLimited(true);
                drops.stream().filter(x -> x.getMachineType().getId() == entry.getKey().getId()).forEach(x -> x.setLimitedStock(true));
                for (Collect c : collects) {
                    if (c.getMachine().getMachineType().getId() == entry.getKey().getId()) {
                        //noinspection unchecked
                        c.setLinkedDrops((ArrayList) drops.stream().filter(x -> entry.getKey().getId() == x.getMachineType().getId()).collect(Collectors.toList()));
                    }
                }
            }


        }


        while (drops.stream().anyMatch(x -> !x.isFinished()) ||
                collects.stream().anyMatch(x -> !x.isFinished())) {

            Truck t = null;
            while (t == null ? true : t.isFinished()) {
                t = trucks.stream().filter(x -> !x.isFinished()).findFirst().orElseThrow(NullPointerException::new);
                //t = trucks.get(rand.nextInt(trucks.size()));
            }
            while (!t.isFinished()) {

                getClosestJob(t);
            }
            /*drops.stream().forEach(x -> System.out.println(x.isFinished()));
            collects.stream().forEach(x -> System.out.println(x.isFinished()));*/
            for (Truck truck : trucks) {
                if (!truck.isFinished()) {
                }
            }
            ArrayList<Drop> nonFinishedDrops = getNonFinishedActions(drops);
            ArrayList<Collect> nonFinishedCollects = getNonFinishedActions(collects);
            if (nonFinishedCollects.size() == 0) {
            }
        }
        int distance = trucks.stream().mapToInt(Truck::calculateDistanceTraveled).sum();
        System.out.println(distance);
        fullCopySolution(trucks);
    }

    private void getClosestJob(Truck t) {
        ArrayList possibleActions = new ArrayList();
        ArrayList<Drop> nonFinishedDrops = getNonFinishedActions(drops);
        ArrayList<Collect> nonFinishedCollects = getNonFinishedActions(collects);
        ArrayList<Drop> doableDrops = new ArrayList<>();
        ArrayList<Collect> doableCollects = new ArrayList<>();
        for (Drop d : nonFinishedDrops) {
            if (checkIfTruckCanDoDrop(t, d)) {
                doableDrops.add(d);
            }
        }
        for (Collect d : nonFinishedCollects) {
            if (checkIfTruckCanDoCollect(t, d)) {
                doableCollects.add(d);
            }
        }

        Drop d = getClosestDrop(t, doableDrops);
        Collect c = getClosestCollect(t, doableCollects);
        if (d == null && c == null) {
            if (t.getCapacity() == 0) {
                t.finishAtDepots(findNearestDepot(t.getEndLocation()));
                t.setFinished(true);
            } else {
                t.emptyAtDepots(findOptimalDepot(t));
            }

        } else if (d == null) {
            t.fetch(c);
            c.setFinished(true);

        } else if (c == null) {
            doDrop(t, doableDrops, d, c);

        } else if (getDistanceNeededForDrop(t, d) <= 1.55 * distanceToLocation(t.getCurrentLocation(), c.getMachine().getLocation())) {
            doDrop(t, doableDrops, d, c);
        } else {
            t.fetch(c);
            c.setFinished(true);
        }
    }

    public static Location findOptimalDepot(Truck t) {
        if (t.getWorkingtime()
                + timeToLocation(findNearestDepot(t.getCurrentLocation()), t.getCurrentLocation())
                + t.calculateEmptyTime()
                + timeToLocation(findNearestDepot(t.getCurrentLocation()), t.getEndLocation())
                <= Truck.TRUCK_WORKING_TIME) {
            return findNearestDepot(t.getCurrentLocation());
        }
        return findNearestDepot(t.getEndLocation());

    }

    private void doDrop(Truck t, ArrayList<Drop> doableDrops, Drop d, Collect c) {
        ArrayList<Drop> dropsSorted = getDropsInOrderOfCloseness(t, doableDrops);
        if (dropsSorted.size() >= 2) {
            for (int i = 1; i < dropsSorted.size(); i++) {
                if (getDistanceNeededForDrop(t, dropsSorted.get(i)) < (c == null ? 9999999 : distanceToLocation(t.getCurrentLocation(), c.getMachine().getLocation()))
                        && distanceToLocation(d.getLocation(), dropsSorted.get(i).getLocation()) < (c == null ? 9999999 : distanceToLocation(t.getCurrentLocation(), c.getMachine().getLocation()))) {
                    dropsSorted.get(i).setPriority(true);
                }
            }

        }
        t.deliver(d, nearestDepotWithMachine(t, d), dropsSorted);
        d.setFinished(true);
    }


    private Collect getClosestCollect(Truck t, ArrayList<Collect> doableCollects) {
        Collect finalCollect = null;
        int distance = Integer.MAX_VALUE;
        for (Collect c : doableCollects) {
            if (!c.validLinkedDrops()) {
                int i = distanceToLocation(t.getCurrentLocation(), c.getMachine().getLocation());
                if (i < distance) {
                    distance = i;
                    finalCollect = c;
                }
            } else {
                Drop smalestDrop = null;
                int distanceDrop = Integer.MAX_VALUE;
                for (Drop drops : c.getLinkedDrops()) {
                    int i = distanceToLocation(drops.getLocation(), c.getMachine().getLocation());
                    if (i < distanceDrop) {
                        distanceDrop = i;
                        smalestDrop = drops;
                    }
                }

                int i = distanceToLocation(t.getCurrentLocation(), c.getMachine().getLocation()) + distanceToLocation(c.getMachine().getLocation(), smalestDrop.getLocation());
                if (i < distance) {
                    distance = i;
                    finalCollect = c;
                }

            }
        }
        //   System.out.println(distance);
        return finalCollect;

    }

    private Drop getClosestDrop(Truck t, ArrayList<Drop> doableDrops) {
        Drop finalDrop = null;
        int distance = Integer.MAX_VALUE;
        for (Drop d : doableDrops) {
            int i = getDistanceNeededForDrop(t, d);
            if (i < distance) {
                distance = i;
                finalDrop = d;
            }
        }
        //  System.out.println(distance);
        return finalDrop;
    }

    private ArrayList<Drop> getDropsInOrderOfCloseness(Truck t, ArrayList<Drop> doableDrops) {
        doableDrops.sort(Comparator.comparing(x -> distanceToLocation(t.getCurrentLocation(), x.getLocation())));
        return doableDrops;
    }

    private ArrayList<Collect> getCollectsInOrderOfCloseness(Location l, ArrayList<Collect> doableCollects) {
        doableCollects.sort(Comparator.comparing(x -> distanceToLocation(l, x.getMachine().getLocation())));
        return doableCollects;
    }

    private int getDistanceNeededForDrop(Truck t, Drop d) {
        if (t.getMachinesLoaded().stream().anyMatch(m -> d.getMachineType().getId() == m.getMachineType().getId())) {
            if (d.isLimitedStock()) {
                return 0;
            }
            return (int) (0.1 * distanceToLocation(t.getCurrentLocation(), d.getLocation()));
        } else {
            return distanceToLocation(t.getCurrentLocation(), nearestDepotWithMachine(t, d).getLocation())
                    + distanceToLocation(nearestDepotWithMachine(t, d).getLocation(), d.getLocation());
        }
    }

    private ArrayList getNonFinishedActions(ArrayList actions) {
        if (actions.get(0) instanceof Drop) {
            ArrayList<Drop> dropList = actions;
            return (ArrayList) dropList.stream().filter(x -> !x.isFinished()).collect(Collectors.toList());
        } else if (actions.get(0) instanceof Collect) {
            ArrayList<Collect> collectList = actions;
            return (ArrayList) collectList.stream().filter(x -> !x.isFinished()).collect(Collectors.toList());
        }

        throw new RuntimeException("Wrong list");

    }


    private Boolean checkIfTruckCanDoDrop(Truck t, Drop d) {

        boolean availableAtDepot = false;
        boolean canDriveToDepot = false;
        if (t.getCapacity() + d.getMachineType().

                getVolume() <= Truck.TRUCK_CAPACITY) {
            Depot nearestDepot = nearestDepotWithMachine(t, d);

            if (nearestDepot != null) {
                if (timeToLocation(t.getCurrentLocation(), nearestDepot.getLocation()) + d.getMachineType().getServicetime()
                        + timeToLocation(nearestDepot.getLocation(), d.getLocation()) +
                        d.getMachineType().getServicetime() + timeToLocation(d.getLocation(), findNearestDepot(t.getEndLocation())) + timeToLocation(findNearestDepot(t.getEndLocation()), t.getEndLocation()) + t.calculateEmptyTime() + t.getWorkingtime() <= Truck.TRUCK_WORKING_TIME) {
                    canDriveToDepot = true;
                }
            }
        }
        return canDriveToDepot || availableAtDepot || t.getMachinesLoaded().stream()
                .anyMatch(m -> d.getMachineType().getId() == m.getMachineType().getId()) && (t.getWorkingtime()
                + timeToLocation(t.getCurrentLocation(), d.getLocation())
                + timeToLocation(d.getLocation(), findNearestDepot(t.getEndLocation()))
                + timeToLocation(findNearestDepot(t.getEndLocation()), t.getEndLocation())
                + t.calculateEmptyTime() + d.getMachineType().getServicetime() <= Truck.TRUCK_WORKING_TIME
        );
    }

    private Depot nearestDepotWithMachine(Truck t, Drop d) {
        ArrayList<Depot> depotWithMachine = new ArrayList<>();
        for (int i = 0; i < depots.size(); i++) {
            for (Machine m : machines) {
                if (m.getMachineType().getId() == d.getMachineType().getId() &&
                        m.getLocation().getId() == depots.get(i).getLocation().getId()
                        && !m.isUsed()) {
                    depotWithMachine.add(depots.get(i));
                }

            }
        }
        Depot nearestDepot = null;
        int disctanceToDepot = Integer.MAX_VALUE;
        if (!depotWithMachine.isEmpty()) {
            for (Depot depot : depotWithMachine) {
                if (distanceToLocation(t.getCurrentLocation(), depot.getLocation()) < disctanceToDepot) {
                    nearestDepot = depot;
                    disctanceToDepot = distanceToLocation(t.getCurrentLocation(), depot.getLocation());
                }
            }
        }
        return nearestDepot;
    }

    private static Location findNearestDepot(Location t) {
        int lowestDistanceToDepot = Integer.MAX_VALUE;
        Depot nearest = null;
        for (int i = 0; i < depots.size(); i++) {
            if (lowestDistanceToDepot > (DISTANCEMATRIX[t.getId()][depots.get(i).getLocation().getId()])) {
                nearest = depots.get(i);
                lowestDistanceToDepot = (DISTANCEMATRIX[t.getId()][depots.get(i).getLocation().getId()]);
            }
        }
        return nearest.getLocation();
    }


    public int distanceToNearestDepot(Truck t) {
        return distanceToLocation(t.getCurrentLocation(), findNearestDepot(t.getCurrentLocation()));
    }

    public static int timeToLocation(Location start, Location end) {
        return TIMEMATRIX[start.getId()][end.getId()];

    }

    public static int distanceToLocation(Location start, Location end) {
        return DISTANCEMATRIX[start.getId()][end.getId()];

    }

    private Truck getFirstAvailableTruck(Collect c) {
        for (Truck t : trucks) {
            if (checkIfTruckCanDoCollect(t, c)) {
                return t;
            }
        }
        return null;
    }

    private Boolean checkIfTruckCanDoCollect(Truck t, Collect c) {
        if (c.validLinkedDrops()) {
            Drop smalestDrop = null;
            int distanceDrop = Integer.MAX_VALUE;
            for (Drop drops : c.getLinkedDrops()) {
                int i = distanceToLocation(drops.getLocation(), c.getMachine().getLocation());
                if (i < distanceDrop) {
                    distanceDrop = i;
                    smalestDrop = drops;
                }
            }

            return ((t.getCapacity() + c.getMachine().getMachineType().getVolume() <= Truck.TRUCK_CAPACITY)
                    &&

                    (t.getWorkingtime() + c.getMachine().getMachineType().getServicetime()
                            + timeToLocation(smalestDrop.getLocation(), findNearestDepot(t.getEndLocation()))
                            + timeToLocation(findNearestDepot(t.getEndLocation()), t.getEndLocation())
                            + timeToLocation(t.getCurrentLocation(), c.getMachine().getLocation()) + t.calculateEmptyTime()
                            + timeToLocation(c.getMachine().getLocation(), smalestDrop.getLocation()) <= Truck.TRUCK_WORKING_TIME - 30));
        }
        return ((t.getCapacity() + c.getMachine().getMachineType().getVolume() <= Truck.TRUCK_CAPACITY)
                && (t.getWorkingtime() + c.getMachine().getMachineType().getServicetime() * 2
                + timeToLocation(c.getMachine().getLocation(), findNearestDepot(t.getEndLocation()))
                + timeToLocation(findNearestDepot(t.getEndLocation()), t.getEndLocation())
                + timeToLocation(t.getCurrentLocation(), c.getMachine().getLocation()) + t.calculateEmptyTime()
                + c.getMachine().getMachineType().getServicetime() <= Truck.TRUCK_WORKING_TIME));

    }

    public void calculateTime(int[] ints) {
        int i = 0;
        for (int j = 0; j < ints.length - 1; j++) {
            i += TIMEMATRIX[ints[j]][ints[j + 1]];
        }
    }


    public void calculateDistance(int[] ints) {
        int i = 0;
        for (int j = 0; j < ints.length - 1; j++) {
            i += DISTANCEMATRIX[ints[j]][ints[j + 1]];
        }
    }
}

//region oldGetJob (Random)
 /*   private void getJob(Truck t) {
        Random rand = new Random();
        t.increaseJobsGiven();
        int order = -1;
        int index = -1;
        if (t.isAtDepot(depots)) {
            index = 1 + rand.nextInt(options.size() - 1);
        } else {
            index = rand.nextInt(options.size());

        }
        order = options.get(index);
        if (order == 0) {
            t.emptyAtDepots(findNearestDepot(t.getCurrentLocation()));
            t.isFinished();
        }
        if (order < 2000 && order >= 1000) {
            if (!drops.get(order - 1000).isFinished() && checkIfTruckCanDoDrop(t, drops.get(order - 1000))) {
                t.deliver(drops.get(order - 1000), null);
                drops.get(order - 1000).setFinished(true);
                options.remove(index);
            }
        }
        if (order >= 2000) {
            if (!collects.get(order - 2000).isFinished() && checkIfTruckCanDoCollect(t, collects.get(order - 2000))) {
                t.fetch(collects.get(order - 2000));
                collects.get(order - 2000).setFinished(true);
                options.remove(index);

            }
        }
        if (t.getWorkingtime() > 0.8 * Truck.TRUCK_WORKING_TIME || t.getJobsGiven() > 20) {
            t.emptyAtDepots(findNearestDepot(t.getCurrentLocation()));
            t.setFinished(true);
        }

    }*/
//endregion