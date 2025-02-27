package AutoDriveEditor.RoadNetwork;

public class MarkerGroup implements Comparable{

    public int groupIndex;
    public String groupName;

    public MarkerGroup(int index, String name) {
        this.groupIndex = index;
        this.groupName = name;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof MarkerGroup) {
            MarkerGroup other = (MarkerGroup) o;
            if (other.groupName.equals(groupName) && other.groupIndex == groupIndex) {
                return 0;
            }
        }
        return 1;
    }
}
