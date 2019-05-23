
public class GenericCollect extends Activity {
    private int id;
    private Machine machine;

    private Depot depotUsed;
    private boolean finished;
    private Depot lastDepot;


    public GenericCollect(Machine machine, Depot depot) {
        this.machine = machine;
        this.depotUsed = depot;
    }

    public GenericCollect(GenericCollect a) {
        this.id = a.id;
        this.machine = a.machine;
        this.depotUsed = a.depotUsed;
        this.finished = a.finished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Depot getDepotUsed() {
        return depotUsed;
    }

    public void setDepotUsed(Depot depotUsed) {
        this.depotUsed = depotUsed;
    }

    @Override
    public String toString() {
        return "GenericCollect{" +
                "id=" + id +
                ", machine=" + machine +
                ", finished=" + finished +
                '}';
    }

    public void setLastDepot(Depot lastDepot) {
        this.lastDepot = lastDepot;
    }

    public Depot getLastDepot() {
        return lastDepot;
    }
}
