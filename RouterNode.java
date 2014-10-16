import javax.swing.*;        
import java.util.Arrays;
import java.util.*;

public class RouterNode {
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private int[] costs  = new int[RouterSimulator.NUM_NODES];
  private int[] routes = new int[RouterSimulator.NUM_NODES];
  
  //Distance table stored in node/router
  private int[][] distance_table = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];
  private Set<Integer> neighbors = new HashSet<Integer>(5);  

  // private boolean[] neighbors = new boolean[RouterSimulator.NUM_NODES];
  // private int[] route = new int[RouterSimulator.NUM_NODES];

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    myID = ID;
    this.sim = sim;
    myGUI = new GuiTextArea("  Output window for Router #"+ ID + "  ");
    System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
    Iterator<Integer> it;  
    
    //Initialize the distance_table array with infinity values
    for ( int i=0; i<RouterSimulator.NUM_NODES; i++ ) {
      for ( int j=0; j<RouterSimulator.NUM_NODES; j++) {
        distance_table[i][j] = RouterSimulator.INFINITY;
        routes[j] = RouterSimulator.INFINITY;
      }
    }

    //Fills the line in distance table that belongs to this node, creates set of neighbors of this node
    for ( int i=0; i<RouterSimulator.NUM_NODES; i++ ) {
      distance_table[myID][i] = costs[i];
      if ( costs[i] != RouterSimulator.INFINITY && costs[i] != 0  ) {
        neighbors.add(i);
        routes[i] = i;
      }
    }

    it = neighbors.iterator();

    //Sends line belonging to this node as update to all neighbors
    while ( it.hasNext() ) {
      sendUpdate ( new RouterPacket ( myID, it.next(), distance_table[myID] ) ); 
    }

    printDistanceTable();
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt) {
    boolean changed = false;
    System.arraycopy ( pkt.mincost, 0, distance_table[pkt.sourceid], 0, RouterSimulator.NUM_NODES );
    Iterator<Integer> it = neighbors.iterator();
    int min = RouterSimulator.INFINITY;

    //Iterate through all nodes and if distance to some node is smaller using the newly updated date than the one in distance_table, replace value in distance_table
    for ( int i=0; i<RouterSimulator.NUM_NODES; i++ ) {
      if ( i == myID )
        continue;
      it = neighbors.iterator();
      min = RouterSimulator.INFINITY;
      while ( it.hasNext() ) {
        int tmp = it.next();
        if ( min > costs[tmp] + distance_table[tmp][i] ) {
          min = costs[tmp] + distance_table[tmp][i];
          routes[i] = tmp;  
        }
     }
     if ( min != distance_table[myID][i] )
        changed = true;
      distance_table[myID][i] = min;
    }
    //If some change in distance table occured, send updae to all neighbors
    if ( changed ) {
      it = neighbors.iterator();
      while ( it.hasNext() ) {
        sendUpdate( new RouterPacket ( myID, it.next(), distance_table[myID] ) );
      }
    }

    printDistanceTable();
  }

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    if ( RouterSimulator.POISONED_REVERSE ) {
      for ( int i=0; i<RouterSimulator.NUM_NODES; i++ ) {
        if ( routes[i] == pkt.destid )
          pkt.mincost[i] = RouterSimulator.INFINITY;
      }
    }
  
  sim.toLayer2(pkt);

  }
  

  //--------------------------------------------------
    public void printDistanceTable() {
        
        myGUI.println(" Table for " + myID +
                      " at t=" + sim.getClocktime());
        
        /*myGUI.println("nbr " + myID + Arrays.toString(distance_table[myID]));
         Iterator<Integer> it = neighbors.iterator();
         while ( it.hasNext() ) {
         int next = it.next();
         myGUI.println("nbr " + next + Arrays.toString(distance_table[next]));
         }
         myGUI.println ( "real cost " + Arrays.toString(costs));
         myGUI.println ();*/
        
        // print all of the distances
        for (int i=0; i<RouterSimulator.NUM_NODES; i++) {
            if (i == myID)
                myGUI.println(" Node " + i + " " + Arrays.toString(distance_table[i])
                              + " *");
            else
                myGUI.println(" Node " + i + " " + Arrays.toString(distance_table[i]));
        }
        myGUI.println (" Real cost " + Arrays.toString(costs));
        myGUI.println (" Route " + Arrays.toString(routes));
        myGUI.println ();
    }

  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {
    myGUI.println ( "LINK COST FROM " + myID + " TO " + dest + " HAS CHANGED FROM " + distance_table[myID][dest] 
      + " TO " + newcost + ". PREPARE TO BE ASSIMILATED. RESISTANCE IS FUTILE." );
    costs[dest] = newcost;
    int min = RouterSimulator.INFINITY;
    boolean changed = false; 

    Iterator<Integer> it = neighbors.iterator();

    //Iterate through all nodes and if distance to some node is smaller using the newly updated date than the one in distance_table, replace value in distance_table
    for ( int i=0; i<RouterSimulator.NUM_NODES; i++ ) {
      if ( i == myID )
        continue;
      it = neighbors.iterator();
      min = RouterSimulator.INFINITY;
      while ( it.hasNext() ) {
        int tmp = it.next();
        if ( min > costs[tmp] + distance_table[tmp][i] ) {
          min = costs[tmp] + distance_table[tmp][i];
          routes[i] = tmp;
        }
      }
      if ( distance_table[myID][i] != min )
        changed = true;
      distance_table[myID][i] = min;
    }

    it = neighbors.iterator();

    //If something changed, send updated distance vector to all neighbors{
    if ( changed ) {
      while ( it.hasNext() ) {
        sendUpdate( new RouterPacket ( myID, it.next(), distance_table[myID] ) );        
      }
    }
    printDistanceTable();

  }
    
}
