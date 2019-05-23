public class Drop extends Activity {

    private int id;
    private MachineType machineType;
    private Location location;
    private boolean finished;
    private boolean priority;
    private boolean limitedStock;

    private Machine machineUsed;

    public Drop(int id, MachineType machineType, Location location) {
        if (machineType == null || location == null) {
            throw new NullPointerException();
        }
        this.id = id;
        this.machineType = machineType;
        this.location = location;
        finished = false;
        priority = false;
        limitedStock = false;
    }

    public Drop(Drop a) {
        this.id = a.id;
        this.machineType = a.machineType;
        this.location = a.location;
        this.finished = a.finished;
        this.priority = a.priority;
        this.limitedStock = a.limitedStock;
        this.machineUsed = a.machineUsed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public void setMachineType(MachineType machineType) {
        this.machineType = machineType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean hasPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public boolean isLimitedStock() {
        return limitedStock;
    }

    public void setLimitedStock(boolean limitedStock) {
        this.limitedStock = limitedStock;
    }

    public Machine getMachineUsed() {
        return machineUsed;
    }

    public void setMachineUsed(Machine machineUsed) {
        this.machineUsed = machineUsed;
    }

    @Override
    public String toString() {
        return "Drop{" +
                "id=" + id +
                ", machineType=" + machineType +
                ", location=" + location +
                ", finished=" + finished +
                '}';
    }
}
