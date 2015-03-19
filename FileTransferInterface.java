
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FileTransferInterface extends Remote {

	// To accept file
	public void acceptFile(String filename, byte[] fileData) throws RemoteException;
	
	// To transfer file
	public void transferFile(String ipAddress, String filename) throws RemoteException;
	
}
