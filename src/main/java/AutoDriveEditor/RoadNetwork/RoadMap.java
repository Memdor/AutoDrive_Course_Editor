package AutoDriveEditor.RoadNetwork;

import java.util.LinkedList;

import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;

public class RoadMap {

    public String roadMapName;
    public static LinkedList<MapNode> mapNodes;
    public static LinkedList<MapMarker> mapMarkers;

    public RoadMap() {
        mapMarkers = new LinkedList<>();
        mapNodes = new LinkedList<>();
        this.roadMapName = null;

    }

    public void addMapMarker(MapMarker mapMarker) {
        mapMarkers.add(mapMarker);
    }

    public void insertMapNode(MapNode toAdd, LinkedList<MapNode> otherNodesInList, LinkedList<MapNode> otherNodesOutList) {

        // starting at the index of where we need to insert the node
        // increment the ID's of all nodes to the right of the mapNodes by +1
        // so when we insert the node all the id's match their index

        LinkedList<MapNode> nodes = mapNodes;
        if (bDebugUndoRedo) LOG.info("## insertMapNode() ## bumping all ID's of mapNodes index {} -> {} by +1", toAdd.id - 1, nodes.size() - 1);
        for (int i = toAdd.id - 1; i <= nodes.size() - 1; i++) {
            MapNode mapNode = nodes.get(i);
            mapNode.id++;
        }

        // insert the MapNode into the list

        if (bDebugUndoRedo) LOG.info("## insertMapNode() ## inserting index {} ( ID {} ) into mapNodes", toAdd.id - 1, toAdd.id );
        mapNodes.add(toAdd.id -1 , toAdd);

        //now we need to restore all the connections to/from it

        // restore all the outgoing connections

        if (otherNodesInList != null) {
            for (MapNode inNode : otherNodesInList) {
                if (!inNode.incoming.contains(toAdd)) inNode.incoming.add(toAdd);
            }
        }

        if (otherNodesOutList != null) {
            for (MapNode outNode : otherNodesOutList) {
                if (!outNode.outgoing.contains(toAdd)) outNode.outgoing.add(toAdd);
            }
        }
    }

    public static void removeMapNode(MapNode toDelete) {
        boolean deleted = false;
        /*if (mapNodes.contains(toDelete)) {*/
            mapNodes.remove(toDelete);
            //deleted = true;
        /*}*/

        LinkedList<MapNode> nodes = mapNodes;
        for (MapNode mapNode : nodes) {
            mapNode.outgoing.remove(toDelete);
            mapNode.incoming.remove(toDelete);
            if (mapNode.id > toDelete.id) {
                mapNode.id--;
            }
        }

        LinkedList<MapMarker> mapMarkersToDelete = new LinkedList<>();
        for (MapMarker mapMarker : mapMarkers) {
            if (mapMarker.mapNode == toDelete) {

                mapMarkersToDelete.add(mapMarker);
            }
        }
        for (MapMarker mapMarker : mapMarkersToDelete) {
            removeMapMarker(mapMarker);
            mapMarkers.remove(mapMarker);
        }
    }

    public static void removeMapMarker(MapMarker mapMarker) {
        LinkedList<MapMarker> mapMarkersToKeep = new LinkedList<>();
        for (MapMarker marker : mapMarkers) {
            if (marker.mapNode.id != mapMarker.mapNode.id) {
                mapMarkersToKeep.add(marker);
            }
        }
        mapMarkers = mapMarkersToKeep;
    }

    public static boolean isDual(MapNode start, MapNode target) {
        LinkedList<MapNode> nodes = start.outgoing;
        for (MapNode outgoing : nodes) {
            if (outgoing == target) {
                LinkedList<MapNode> mapNodeLinkedList = target.outgoing;
                for (MapNode outgoingTarget : mapNodeLinkedList) {
                    if (outgoingTarget == start) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean isReverse(MapNode start, MapNode target) {
        LinkedList<MapNode> startNodes = target.incoming;
        if (startNodes.size() >0) {
            for (MapNode incoming : startNodes) {
                if (incoming.id == start.id) {
                    return false;
                }
            }
        }
        LinkedList<MapNode> outNodes = start.outgoing;
        for (MapNode outgoing : outNodes) {
            if (outgoing.id == target.id) {
                return true;
            }
        }
        return false;
    }
}
