import java.io.CharArrayReader;
import java.util.ArrayList;

public class Collect extends Activity {

    private int id;
    private Machine machine;


    private boolean finished;

    private ArrayList<Drop> linkedDrops;

    public Collect(int id, Machine machine) {
        if (machine == null) {
            throw new NullPointerException();
        }
        this.id = id;
        this.machine = machine;
        finished = false;
        linkedDrops = new ArrayList<>();
    }

    public Collect(Collect a) {
        this.id = a.id;
        this.machine = a.machine;
        this.finished = a.finished;
        this.linkedDrops = a.linkedDrops;
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

    public ArrayList<Drop> getLinkedDrops() {
        return linkedDrops;
    }

    public boolean validLinkedDrops() {
        return !linkedDrops.isEmpty();
    }

    public void setLinkedDrops(ArrayList<Drop> linkedDrops) {
        this.linkedDrops = linkedDrops;
    }

    @Override
    public String toString() {
        return "Collect{" +
                "id=" + id +
                ", machine=" + machine +
                ", finished=" + finished +
                '}';
    }
}
