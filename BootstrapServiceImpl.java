import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;



/**
 * Actual Bootstrap Server
 * Used to join nodes in CAN
 */

public class BootstrapServiceImpl extends UnicastRemoteObject implements BootsrapService, Serializable{

	private static final long serialVersionUID = 1L; 
	private boolean hasEntryNode = false;  // To check whether CAN has at least one node
	private String entryNodeIP;  // To store the IP address of entry node of CAN
	private boolean hasOddShape = false;
	private CANData oddShapedCANData = new CANData(); 
	//private CANData newNodeCANData = new CANData();
	private String oddShapedZoneIP;
	private String nodeToJoinIP = "";

	public BootstrapServiceImpl() throws RemoteException{
		
	}

	@Override
	public String joinCAN(String ipAddress, int x_coord, int y_coord) {
		
		String ipAddressOfNodeToJoin = "";
		
		if(hasEntryNode){
			
			if(hasOddShape){
				return oddShapedZoneIP;
			}else{
				
				MessageDigest md;
				try {
					
					//System.out.println("Iside Join CAN Bootstrap");

					md = MessageDigest.getInstance("md5");
					byte[] hashedXCoord = md.digest(ipAddress.getBytes()); 
					
					int rnd_x = 0;
					int rnd_y = 0;
					
					rnd_x = Math.abs((int)hashedXCoord[0]);
					x_coord = rnd_x % 100;

					rnd_y = Math.abs((int)hashedXCoord[1]);
					y_coord = rnd_y % 100;
					
					/*
					StringBuffer stringBuffer = new StringBuffer();
				    for (int i = 0; i < hashedXCoord.length; i++) {
				        stringBuffer.append(Integer.toString((hashedXCoord[i] & 0xff) + 0x100, 16)
				                .substring(1));
				    }
				    
				    String encryptedFile = stringBuffer.toString();
				    
				    StringBuffer stringBuffer1 = new StringBuffer();
		     
					for(int i=0;i<encryptedFile.length();i++){
						if(Character.isDigit(encryptedFile.charAt(i)))
							stringBuffer1.append(encryptedFile.charAt(i));
					}
		     
					encryptedFile = stringBuffer1.toString();
		     
					//System.out.println(encryptedFile);
		      
					x_coord = (int)Double.parseDouble(encryptedFile) % 100;
					y_coord = (int)Double.parseDouble(encryptedFile) % 100;
					*/
	
					System.out.println("x Coord " + x_coord + " Y Coord " + y_coord);


				try {
					System.out.println(entryNodeIP);
					CANNodeService neighbor = (CANNodeService) Naming.lookup("rmi://"+entryNodeIP+"/"+entryNodeIP);
					System.out.println(neighbor);
					nodeToJoinIP = neighbor.joinNode(ipAddress, x_coord, y_coord);
					System.out.println(nodeToJoinIP);
					return nodeToJoinIP;
				} catch (MalformedURLException | RemoteException
						| NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}catch(Exception e){
				e.printStackTrace();
				System.err.println("Error while joining Node Bootstrap");
			}
			
		  }
			
	    }else{
			entryNodeIP = ipAddress;
			hasEntryNode = true;
			return null;
		}
		
		return null;
	}
	
	@Override
	public void setHasOddShape(boolean hasOddShape) {
		
		this.hasOddShape = hasOddShape;
		
	}

	@Override
	public void storeOddShapedZonesCANData(CANData oddShapedZoneCANData) {
		// TODO Auto-generated method stub
		
		this.oddShapedCANData = oddShapedZoneCANData;
		
	}
	
	public static void main(String[] args){
		try{
			//if (System.getSecurityManager() == null) {
            //	System.setSecurityManager(new SecurityManager());
        	//}
			BootsrapService service = new BootstrapServiceImpl();
			Naming.rebind("BootstrapServer", service);
		}catch(Exception ex){
			System.err.println(ex);
		}
		
	}

	@Override
	public void storeIPOfOddShapedZone(String ipAddressZone) {
		// TODO Auto-generated method stub
		this.oddShapedZoneIP = ipAddressZone;
	}

	@Override
	public void updateNeighbor(String ipAddress) throws RemoteException {		
		entryNodeIP = ipAddress;		
	}

	@Override
	public void updateBootstrapOnAllLeave() throws RemoteException {		
		hasEntryNode = false;
	}
	
	
}
