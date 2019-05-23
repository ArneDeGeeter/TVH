public class Machine {

    private int id;
    private MachineType machineType;
    private Location location;
    private boolean used = false;
    private boolean fromCollect = false;
    private final Location originalLocation;

    public Machine(int id, MachineType machineType, Location location) {
        if (machineType == null || location == null) {
            throw new NullPointerException();
        }
        this.id = id;
        this.machineType = machineType;
        machineType.increaseQuantity();

        this.location = location;
        this.originalLocation = location;
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

    public Boolean isUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    public boolean isFromCollect() {
        return fromCollect;
    }

    public void setFromCollect(boolean fromCollect) {
        this.fromCollect = fromCollect;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "id=" + id +
                ", machineType=" + machineType +
                ", location=" + location +
                ", used=" + used +
                ", fromCollect=" + fromCollect +
                ", originalLocation=" + originalLocation +
                '}';
    }

    public Location getOriginalLocation() {
        return originalLocation;
    }
}
