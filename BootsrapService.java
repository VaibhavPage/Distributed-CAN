import java.rmi.Remote;
import java.rmi.RemoteException;


public interface BootsrapService extends Remote{

	// Join new node in CAN
	public String joinCAN(String ipAddress, int x_coord, int y_coord) throws RemoteException;
	
	// Set odd shaped zone variable
	public void setHasOddShape(boolean hasOddShape) throws RemoteException;
	
	// store odd shaped zone CANDATA
	public void storeOddShapedZonesCANData(CANData oddShapedZoneCANData) throws RemoteException;
	
	// store IP of odd shaped CAN zone
	public void storeIPOfOddShapedZone(String ipAddressZone) throws RemoteException;
	
	// Update the neighbor in CAN
	public void updateNeighbor(String ipAddress) throws RemoteException;
	
	// Update bootstrap when all nodes exits
	public void updateBootstrapOnAllLeave() throws RemoteException;
}
