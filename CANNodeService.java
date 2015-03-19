import java.rmi.Remote;
import java.rmi.RemoteException;


public interface CANNodeService extends Remote {

	// Join Node in CAN in given Target Node's Area
	public String joinNode(String ipAddress, int x_coord, int y_coord) throws RemoteException;
	
	// Return new nodes CAN Data
	public CANData returnCANdata() throws RemoteException;
	
	// Replace keys in tables in CAN data
	public void replaceKeysinCANData(String oldKey, String newKey) throws RemoteException;
	
	// To add neighbors
	public void addNeighbor(String neighbor_name, String neighbour_IP, NodeCoord neighbor_NodeCoord) throws RemoteException;
	
	// To remove neighbors
	public void removeNeighbor(String neighbor_IP) throws RemoteException;
	
	// Search For file in CAN
	public void searchForFile(String ipAddress, String filename, int file_x_coord, int file_y_coord) throws RemoteException;
	
	// Store file in CAN
	public String storeFileUsingRoutingInCAN(String filename, int file_x_coord, int file_y_coord) throws RemoteException;
	
	// Leave CAN
	public void leaveCAN() throws RemoteException;
	
	// Take control of leaving node
	public void takeControlOfLeftNode(CANData leftNeighborCANData, boolean sendInfoBootstrap) throws RemoteException;
	
	// Set node as neighbor of bootstrap
	public void setIsBootstrapNeighbor(boolean isBN) throws RemoteException;
	
	/**
	 * Update Neighbors Boundary 
	 * 
	 * @param ipAddressNei
	 * @param neiCoord
	 */
	public void updateNeighborBoundary(String ipAddressNei, NodeCoord neiCoord) throws RemoteException;
}
