package AutoDriveEditor.Managers;

import java.util.LinkedList;

import AutoDriveEditor.MapPanel.LinearLine;
import AutoDriveEditor.MapPanel.MapPanel;
import AutoDriveEditor.RoadNetwork.MapMarker;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import static AutoDriveEditor.GUI.MenuBuilder.*;
import static AutoDriveEditor.Managers.ScanManager.*;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.Utils.DebugUtils.*;
import static AutoDriveEditor.Utils.LoggerUtils.*;

/**
 * Manages a Queue of Changables to perform undo and/or redo operations. Clients can add implementations of the Changeable
 * class to this class, and it will manage undo/redo as a Queue.
 *
 * @author Greg Cope
 *
 */



public class ChangeManager {

    // Interface to implement a Changeable type of action - either undo or redo.
    // @author Greg COpe

    public interface Changeable {
        // Undoes an action
        void undo();

        // Redoes an action
        void redo();
    }

    //the current index node
    private Node currentIndex;
    //the parent node far left node.
    private final Node parentNode = new Node();
    /**
     * Creates a new ChangeManager object which is initially empty.
     */
    public ChangeManager(){
        currentIndex = parentNode;
    }

     // Creates a new ChangeManager which is a duplicate of the parameter in both contents and current index.

    public ChangeManager(ChangeManager manager){
        this();
        currentIndex = manager.currentIndex;
    }

     // Clears all Changables contained in this manager.

    public void clear(){
        currentIndex = parentNode;
    }

     // Add a Changeable to manage.

    public void addChangeable(Changeable changeable){
        Node node = new Node(changeable);
        currentIndex.right = node;
        node.left = currentIndex;
        currentIndex = node;
    }

     // Return if undo can be performed.

    public boolean canUndo() {
        return currentIndex != parentNode;
    }

     // Return if redo can be performed.

    public boolean canRedo(){
        return currentIndex.right != null;
    }

     // Undoes the Changeable at the current index.

    public void undo(){
        //validate
        canAutoSave = false;
        if ( !canUndo() ){
            LOG.info("Reached Beginning of Undo History.");
            canAutoSave = true;
            return;
            //throw new IllegalStateException("Cannot undo. Index is out of range.");
        }
        //undo
        if (currentIndex.changeable != null) {
            currentIndex.changeable.undo();
        } else {
            LOG.info("Unable to Undo");
        }
        //set index
        moveLeft();
        canAutoSave = true;
    }

    /**
     * Moves the internal pointer of the backed linked list to the left.
     * @throws IllegalStateException If the left index is null.
     */

    private void moveLeft(){
        if ( currentIndex.left == null ){
            throw new IllegalStateException("Internal index set to null.");
        }
        currentIndex = currentIndex.left;
    }

    /**
     * Moves the internal pointer of the backed linked list to the right.
     * @throws IllegalStateException If the right index is null.
     */

    private void moveRight(){
        if ( currentIndex.right == null ){
            throw new IllegalStateException("Internal index set to null.");
        }
        currentIndex = currentIndex.right;
    }

    /**
     * Redoes the Changable at the current index.
     * @throws IllegalStateException if canRedo returns false.
     */

    public void redo(){
        //validate
        canAutoSave = false;
        if ( !canRedo() ){
            LOG.info("Reached End of Undo History.");
            canAutoSave = true;
            return;
        }
        //reset index
        moveRight();
        //redo
        if (currentIndex.changeable != null) {
            currentIndex.changeable.redo();
        } else {
            LOG.info("Unable to Redo");
        }
        canAutoSave = true;
    }

    /**
     * Inner class to implement a doubly linked list for our queue of Changeables.
     * @author Greg Cope
     *
     */

    private static class Node {
        private Node left = null;
        private Node right = null;
        private final Changeable changeable;

        public Node(Changeable c){
            changeable = c;
        }

        public Node(){
            changeable = null;
        }
    }

    //
    //  Move Nodes
    //

    public static class MoveNodeChanger implements Changeable{
        private final LinkedList<MapNode> moveNodes;
        private final int diffX;
        private final int diffY;
        private final boolean wasSnapMove;
        private final boolean isStale;

        public MoveNodeChanger(LinkedList<MapNode> mapNodesMoved, int movedX, int movedY, boolean snapMove){
            super();
            this.moveNodes = new LinkedList<>();
            if (bDebugUndoRedo) LOG.info("node moved = {} , {}", movedX, movedY);
            this.diffX = movedX;
            this.diffY = movedY;
            this.wasSnapMove = snapMove;
            this.moveNodes.addAll(mapNodesMoved);
            this.isStale = getMapPanel().isStale();
        }

        public void undo(){
            getMapPanel().moveNodeBy(this.moveNodes, -this.diffX, -this.diffY, true);
            for (MapNode node : this.moveNodes) {
                checkAreaForNodeOverlap(node);
            }
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){
            getMapPanel().moveNodeBy(this.moveNodes, this.diffX, this.diffY, true);
            for (MapNode node : this.moveNodes) {
                checkAreaForNodeOverlap(node);
            }
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    //
    // Add node
    //

    public static class AddNodeChanger implements Changeable{
        private final MapNode storeNode;
        private final boolean isStale;

        public AddNodeChanger(MapNode node){
            super();
            this.storeNode = node;
            this.isStale = getMapPanel().isStale();
        }

        public void undo(){
            RoadMap.removeMapNode(storeNode);
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){
            roadMap.insertMapNode(storeNode, null, null);
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    //
    // Add node from LinkedList
    //

    public static class  PasteSelectionChanger implements Changeable{
        private final LinkedList<MapNode> storeNodes;
        private final boolean isStale;

        public PasteSelectionChanger(LinkedList<MapNode> nodes){
            super();
            this.storeNodes = (LinkedList<MapNode>) nodes.clone();
            this.isStale = getMapPanel().isStale();
        }

        public void undo(){
            clearMultiSelection();
            RoadMap.mapNodes.removeAll(this.storeNodes);
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){
            RoadMap.mapNodes.addAll(this.storeNodes);
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    //
    // Delete Node
    //

    public static class DeleteNodeChanger implements Changeable{

        private LinkedList<NodeLinks> nodeListToDelete = new LinkedList<>();
        private final boolean isStale;

        public DeleteNodeChanger(LinkedList<NodeLinks> nodeLinks){
            super();
            this.nodeListToDelete =  (LinkedList<NodeLinks>) nodeLinks.clone();
            this.isStale = getMapPanel().isStale();
        }

        public void undo(){
            if (bDebugTest) startTimer();
            for (NodeLinks insertNode : this.nodeListToDelete) {
                if (bDebugUndoRedo) LOG.info("Insert {} ({})",insertNode.node.id,insertNode.nodeIDBackup);
                if (insertNode.node.id != insertNode.nodeIDBackup) {
                    if (bDebugUndoRedo) LOG.info("## RemoveNode Undo ## ID mismatch.. correcting ID {} -> ID {}", insertNode.node.id, insertNode.nodeIDBackup);
                    insertNode.node.id = insertNode.nodeIDBackup;
                }
                roadMap.insertMapNode(insertNode.node, insertNode.otherIncoming, insertNode.otherOutgoing);
                if (insertNode.linkedMarker != null) {
                    if (bDebugUndoRedo) LOG.info("## RemoveNode Undo ## MapNode ID {} has a linked MapMarker.. restoring {} ( {} )", insertNode.node.id, insertNode.linkedMarker.name, insertNode.linkedMarker.group);
                    roadMap.addMapMarker(insertNode.linkedMarker);
                }
            }

            /*if (!bDebugTest) {
                LinkedList<MapNode> nodes = getMapPanel().getRoadMap().mapNodes;
                for (int i = 0; i <= nodes.size() - 1 ; i++) {
                    MapNode mapNode = nodes.get(i);
                    mapNode.id = i+1;
                }
            }*/

            if (bDebugTest) LOG.info("result = {}", stopTimer());
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){
            for (NodeLinks nodeLinks : this.nodeListToDelete) {
                MapNode toDelete = nodeLinks.node;
                roadMap.removeMapNode(toDelete);
            }
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    //
    // Linear Line Changer
    //

    public static class LinearLineChanger implements Changeable{

        private final MapNodeStore fromNode;
        private final MapNodeStore toNode;
        private final LinkedList<MapNodeStore> autoGeneratedNodes;
        private final int connectionType;
        private final boolean isStale;

        //TODO: save/restore isStale() status

        public LinearLineChanger(MapNode from, MapNode to, LinkedList<MapNode> inbetweenNodes, int type){
            super();
            this.fromNode = new MapNodeStore(from);
            this.toNode = new MapNodeStore(to);
            this.autoGeneratedNodes = new LinkedList<>();
            this.connectionType=type;
            this.isStale = getMapPanel().isStale();

            for (MapNode toStore : inbetweenNodes) {
                if (bDebugUndoRedo) LOG.info("## LinearLineChanger ## Adding ID {} to autoGeneratedNodes", toStore.id);
                autoGeneratedNodes.add(new MapNodeStore(toStore));
            }
        }

        public void undo(){

            if (this.autoGeneratedNodes.size() <= 2 ) {
                if (bDebugUndoRedo) LOG.info("## LinearLineChanger.undo ## No autoGeneratedNodes - restoring to/from connections");
                this.fromNode.restoreConnections();
                this.toNode.restoreConnections();
            } else {
                for (int j = 1; j < this.autoGeneratedNodes.size() - 1 ; j++) {
                    MapNodeStore storedNode = this.autoGeneratedNodes.get(j);
                    MapNode toDelete = storedNode.getMapNode();
                    if (bDebugUndoRedo) LOG.info("## LinearLineChanger.undo ## undo is removing ID {} from MapNodes", toDelete.id);
                    MapPanel.roadMap.removeMapNode(toDelete);
                    if (storedNode.hasChangedID()) {
                        if (bDebugUndoRedo) LOG.info("## LinearLineChanger.undo ## Removed node changed ID {}", storedNode.mapNode.id);
                        storedNode.resetID();
                        if (bDebugUndoRedo) LOG.info("## LinearLineChanger.undo ## Reset ID to {}", storedNode.mapNode.id);
                    }
                    if (MapPanel.hoveredNode == toDelete) {
                        MapPanel.hoveredNode = null;
                    }
                }
            }
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){

            if (this.autoGeneratedNodes.size() > 2 ) {
                for (int i = 1; i < this.autoGeneratedNodes.size() - 1 ; i++) {
                    MapNodeStore storedNode = this.autoGeneratedNodes.get(i);
                    storedNode.clearConnections();
                    // during the undo process, removeMapNode deletes all the connections coming
                    // to/from this node but adjusts the node id's,  so we have to manually restore
                    // the id, .getMapNode() will check this for us and correct if necessary before
                    // passing us the node info.
                    MapNode newNode = storedNode.getMapNode();
                    if (bDebugUndoRedo) LOG.info("## LinearLineChanger.redo ## Inserting ID {} in MapNodes", newNode.id);
                    roadMap.insertMapNode(newNode, null,null);
                }
            }
            LinearLine.connectNodes(getLineLinkedList(), this.connectionType);
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }

        public LinkedList<MapNode> getLineLinkedList() {
            LinkedList<MapNode> list = new LinkedList<>();
            for (int i = 0; i <= this.autoGeneratedNodes.size() - 1 ; i++) {
                MapNodeStore nodeBackup = this.autoGeneratedNodes.get(i);
                list.add(nodeBackup.mapNode);
            }
            return list;
        }
    }

    //
    // Quadratic Curve Changer
    //

    public static class CurveChanger implements Changeable{

        private final LinkedList<MapNodeStore> storedCurveNodeList;
        private final boolean isReversePath;
        private final boolean isDualPath;
        private final boolean isStale;

        public CurveChanger(LinkedList<MapNode> curveNodes, boolean isReverse, boolean isDual){
            super();

            this.storedCurveNodeList = new LinkedList<>();
            this.isReversePath = isReverse;
            this.isDualPath = isDual;
            this.isStale = getMapPanel().isStale();

            for (int i = 0; i <= curveNodes.size() -1 ; i++) {
                MapNode mapNode = curveNodes.get(i);
                if (bDebugUndoRedo) LOG.info("## QuadCurveChanger ## Adding ID {} to storedCurveNodeList", mapNode.id);
                this.storedCurveNodeList.add(new MapNodeStore(mapNode));
            }
        }

        public void undo(){
            for (int i = 1; i <= this.storedCurveNodeList.size() - 2 ; i++) {
                MapNodeStore curveNode = this.storedCurveNodeList.get(i);
                if (bDebugUndoRedo) LOG.info("## QuadCurveChanger.undo ## Removing node ID {}", curveNode.mapNode.id);
                roadMap.removeMapNode(curveNode.mapNode);
                if (curveNode.hasChangedID()) {
                    if (bDebugUndoRedo) LOG.info("## QuadCurveChanger.undo ## ID {} changed", curveNode.mapNode.id);
                    curveNode.resetID();
                    if (bDebugUndoRedo) LOG.info("## QuadCurveChanger.undo ## Reset ID to {}", curveNode.mapNode.id);
                }
            }
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){

            for (int i = 1; i <= this.storedCurveNodeList.size() - 2 ; i++) {
                MapNodeStore curveNode = this.storedCurveNodeList.get(i);
                curveNode.clearConnections();
                if (bDebugUndoRedo) LOG.info("## QuadCurveChanger ## Inserting mapNode ID {}", curveNode.mapNode.id);
                roadMap.insertMapNode(curveNode.getMapNode(), null,null);
                if (curveNode.hasChangedID()) curveNode.resetID();
            }
            connectNodes(getCurveLinkedList(), this.isReversePath, this.isDualPath);
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }

        public static void connectNodes(LinkedList<MapNode> mergeNodesList, boolean reversePath, boolean dualPath)  {
            for (int j = 0; j < mergeNodesList.size() - 1; j++) {
                MapNode startNode = mergeNodesList.get(j);
                MapNode endNode = mergeNodesList.get(j+1);
                if (reversePath) {
                    MapPanel.createConnectionBetween(startNode,endNode,CONNECTION_REVERSE);
                } else if (dualPath) {
                    MapPanel.createConnectionBetween(startNode,endNode,CONNECTION_DUAL);
                } else {
                    MapPanel.createConnectionBetween(startNode,endNode,CONNECTION_STANDARD);
                }
            }
        }

        public LinkedList<MapNode> getCurveLinkedList() {
            LinkedList<MapNode> list = new LinkedList<>();
            for (int i = 0; i <= this.storedCurveNodeList.size() - 1 ; i++) {
                MapNodeStore nodeBackup = this.storedCurveNodeList.get(i);
                list.add(nodeBackup.mapNode);
            }
            return list;
        }
    }

    //
    // Node Priority Changer
    //

    public static class NodePriorityChanger implements Changeable{
        private final LinkedList<MapNode> nodesPriorityChanged;
        private final boolean isStale;

        public NodePriorityChanger(LinkedList<MapNode> mapNodesChanged){
            super();
            this.nodesPriorityChanged = new LinkedList<>();
            for (int i = 0; i <= mapNodesChanged.size() - 1 ; i++) {
                MapNode mapNode = mapNodesChanged.get(i);
                this.nodesPriorityChanged.add(mapNode);
            }
            this.isStale = getMapPanel().isStale();
        }

        public NodePriorityChanger(MapNode nodeToChange){
            super();
            this.nodesPriorityChanged = new LinkedList<>();
            this.nodesPriorityChanged.add(nodeToChange);
            this.isStale = getMapPanel().isStale();
        }

        public void undo(){
            for (int i = 0; i <= this.nodesPriorityChanged.size() - 1 ; i++) {
                MapNode mapNode = this.nodesPriorityChanged.get(i);
                mapNode.flag = 1 - mapNode.flag;
            }
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){
            for (int i = 0; i <= this.nodesPriorityChanged.size() - 1 ; i++) {
                MapNode mapNode = this.nodesPriorityChanged.get(i);
                mapNode.flag = 1 - mapNode.flag;
            }
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    public static class MarkerAddChanger implements Changeable{
        private final MapMarker markerToChange;
        private final Boolean isStale;

        public MarkerAddChanger(MapMarker marker){
            super();
            this.markerToChange = marker;
            this.isStale = getMapPanel().isStale();
        }

        public void undo(){
            RoadMap.removeMapMarker(this.markerToChange);
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){
            roadMap.addMapMarker(this.markerToChange);
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    public static class MarkerRemoveChanger implements Changeable{
        private final Boolean isStale;
        private final MapMarker markerStore;

        public MarkerRemoveChanger(MapMarker marker){
            super();
            this.markerStore = marker;
            this.isStale = getMapPanel().isStale();
        }

        public void undo(){
            roadMap.addMapMarker(this.markerStore);
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo(){
            roadMap.removeMapMarker(this.markerStore);
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    public static class MarkerEditChanger implements Changeable{
        private final Boolean isStale;
        private final MapNode mapNode;
        private final int mapNodeID;
        private final String oldName;
        private final String newName;
        private final String oldGroup;
        private final String newGroup;

        public MarkerEditChanger(MapNode mapNode, int id, String prevName, String newName, String prevGroup, String newGroup){
            super();
            this.isStale = getMapPanel().isStale();
            this.mapNode = mapNode;
            this.mapNodeID = id;
            this.oldName = prevName;
            this.newName = newName;
            this.oldGroup = prevGroup;
            this.newGroup = newGroup;
        }

        public void undo() {
            for (int i = 0; i < roadMap.mapMarkers.size(); i++) {
                MapMarker mapMarker = roadMap.mapMarkers.get(i);
                if (mapMarker.mapNode == this.mapNode) {
                    mapMarker.name = this.oldName;
                    mapMarker.group = this.oldGroup;
                }
            }
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo() {
            for (int i = 0; i < roadMap.mapMarkers.size(); i++) {
                MapMarker mapMarker = roadMap.mapMarkers.get(i);
                if (mapMarker.mapNode == this.mapNode) {
                    mapMarker.name = this.newName;
                    mapMarker.group = this.newGroup;
                }
            }
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }
    }

    public static class AlignmentChanger implements Changeable{
        private final Boolean isStale;
        private final LinkedList<ZStore> nodeList;

        public AlignmentChanger(LinkedList<MapNode> multiSelectList, double x, double y, double z){
            super();
            this.isStale = getMapPanel().isStale();
            this.nodeList = new LinkedList<>();

            for (MapNode node : multiSelectList) {
                nodeList.add(new ZStore(node, x, y, z));
            }
        }

        public void undo() {
            for (ZStore storedNode : nodeList) {
                storedNode.mapNode.x += storedNode.diffX;
                storedNode.mapNode.y += storedNode.diffY;
                storedNode.mapNode.z += storedNode.diffZ;
            }
            getMapPanel().repaint();
            getMapPanel().setStale(this.isStale);
        }

        public void redo() {
            for (ZStore storedNode : nodeList) {
                storedNode.mapNode.x += -storedNode.diffX;
                storedNode.mapNode.y += -storedNode.diffY;
                storedNode.mapNode.z += -storedNode.diffZ;
            }
            getMapPanel().repaint();
            getMapPanel().setStale(true);
        }

        private static class ZStore {
            private final MapNode mapNode;
            private final double diffX;
            private final double diffY;
            private final double diffZ;

            public ZStore(MapNode node, double dX, double dY, double dZ) {
                this.mapNode = node;
                if (dX == 0) {
                    this.diffX = 0;
                } else {
                    this.diffX = node.x - dX;
                }
                if (dY == 0) {
                    this.diffY = 0;
                } else {
                    this.diffY = node.y - dY;
                }
                if (dZ == 0) {
                    this.diffZ = 0;
                } else {
                    this.diffZ = node.z - dZ;
                }
            }
        }
    }

    private static class MapNodeStore {
        private final MapNode mapNode;
        private final int mapNodeIDBackup;
        private final LinkedList<MapNode> incomingBackup;
        private final LinkedList<MapNode> outgoingBackup;

        public MapNodeStore(MapNode node) {
            this.mapNode = node;
            this.mapNodeIDBackup = node.id;
            this.incomingBackup = new LinkedList<>();
            this.outgoingBackup = new LinkedList<>();
            backupConnections();
        }

        public MapNode getMapNode() {
            if (this.hasChangedID()) this.resetID();
            return this.mapNode;
        }

        public void resetID() { this.mapNode.id = this.mapNodeIDBackup; }

        public boolean hasChangedID() { return this.mapNode.id != this.mapNodeIDBackup; }

        public void clearConnections() {
            clearIncoming();
            clearOutgoing();
        }

        public void clearIncoming() { this.mapNode.incoming.clear(); }

        public void clearOutgoing() { this.mapNode.outgoing.clear(); }

        public void backupConnections() {
            copyList(this.mapNode.incoming, this.incomingBackup);
            copyList(this.mapNode.outgoing, this.outgoingBackup);
        }

        public void restoreConnections() {
            copyList(this.incomingBackup, this.mapNode.incoming);
            copyList(this.outgoingBackup, this.mapNode.outgoing);
        }

        public void backupIncoming() { copyList(this.mapNode.incoming, this.incomingBackup); }

        public void restoreIncoming() { copyList(this.incomingBackup, this.mapNode.incoming); }

        public void backupOutgoing() { copyList(this.mapNode.outgoing, this.outgoingBackup); }

        public void restoreOutgoing() { copyList(this.outgoingBackup, this.mapNode.outgoing); }

        private void copyList(LinkedList<MapNode> from, LinkedList<MapNode> to) {
            to.clear();
            // use .clone() ??
            for (int i = 0; i <= from.size() - 1 ; i++) {
                MapNode mapNode = from.get(i);
                to.add(mapNode);
            }
        }
    }


}