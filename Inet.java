import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Inet {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try{
			System.out.println(InetAddress.getLocalHost().getHostAddress());
		}catch(Exception e){
			System.out.println(e);
		}
		
		 try
	      {
	         final Registry registry = LocateRegistry.getRegistry();
	         final String[] boundNames = registry.list();
	        
	         for (final String name : boundNames)
	         {
	            System.out.println(name);
	         }
	      }
	      catch (ConnectException connectEx)
	      {
	         
	      }
	      catch (RemoteException remoteEx)
	      {
	         System.err.println("RemoteException encountered: " + remoteEx.toString());
	      }
		
	}

}
