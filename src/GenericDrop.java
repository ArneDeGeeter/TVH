public class GenericDrop extends Activity {
    private int id;
    private Machine machine;

    private Depot depotUsed;
    private Depot lastDepot;

    public GenericDrop(Machine machine,Depot depot) {
        this.machine = machine;
        this.depotUsed=depot;
    }

    public GenericDrop(GenericDrop a) {
        this.id=a.id;
        this.machine=a.machine;
        this.depotUsed=a.depotUsed;
        this.lastDepot=a.lastDepot;
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

    public Depot getDepotUsed() {
        return depotUsed;
    }

    public void setDepotUsed(Depot depotUsed) {
        this.depotUsed = depotUsed;
    }

    public Depot getLastDepot() {
        return lastDepot;
    }

    public void setLastDepot(Depot lastDepot) {
        this.lastDepot = lastDepot;
    }

    @Override
    public String toString() {
        return "GenericDrop{" +
                "id=" + id +
                ", machine=" + machine +
                '}';
    }
}
