public class MachineType {

    private final int id;
    private final int volume;
    private final int servicetime;
    private final String name;
    private int quantity;
    private boolean limited;

    public MachineType(int id, int volume, int servicetime, String name) {
        this.id = id;
        this.volume = volume;
        this.servicetime = servicetime;
        this.name = name;
        quantity = 0;
        limited = false;
    }

    public int getId() {
        return id;
    }

    public int getVolume() {
        return volume;
    }

    public int getServicetime() {
        return servicetime;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public void increaseQuantity() {
        quantity++;
    }

    public boolean isLimited() {
        return limited;
    }

    public void setLimited(boolean limited) {
        this.limited = limited;
    }

    @Override
    public String toString() {
        return "MachineType{" +
                "id=" + id +
                ", volume=" + volume +
                ", servicetime=" + servicetime +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", limited=" + limited +
                '}';
    }
}
