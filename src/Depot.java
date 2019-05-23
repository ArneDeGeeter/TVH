public class Depot {

    private int id;
    private Location location;

    public Depot(int id, Location location) {
        if(location==null){
            throw new NullPointerException();
        }
        this.id = id;
        this.location = location;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
