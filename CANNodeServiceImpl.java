import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;


public class CANNodeServiceImpl extends UnicastRemoteObject implements CANNodeService, Serializable, Runnable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean isJoined = false;
	NodeCoord nodeOwnCoord = new NodeCoord();
	CANData nodeCANData = new CANData();
	FileCoord nodeFileCoord = new FileCoord();
	private HashMap<String, FileCoord> newNodefileCoordInfo;
	private HashMap<String, NodeCoord> newNodeneighborBoundaryInfo;
	private HashMap<String, String> newNodeneighborsIPInfo;
	private ArrayList<String> filesToSend;  // Files to send to new node
	private String ipOfNodeToSendFile;
	CANData newNodeCANData = new CANData();
	boolean sendInfoToBootstrap = false;
	CANData controlledZoneCANDATA = new CANData();
	ArrayList<String> tempNeighbors = new ArrayList<String>();
	ArrayList<String> tempFiles = new ArrayList<String>();
	HashMap<String, NodeCoord> tempNeighborMap = new HashMap<String, NodeCoord>(); 
	static int choice = 0;
	boolean isBootstrapNeighbor = false;
	private String bootstrapIP;
	
	public CANNodeServiceImpl() throws RemoteException{
		
		newNodefileCoordInfo = new HashMap<String, FileCoord>();
		newNodeneighborBoundaryInfo = new HashMap<String, NodeCoord>();
		newNodeneighborsIPInfo = new HashMap<String, String>();
		
	}
	
	/**
	 * Add a new neighbor to CURRENT node
	 * 
	 * @param neighbor_name
	 * @param neighbour_IP
	 * @param neighbor_NodeCoord
	 */
	public void addNeighbor(String neighbor_name, String neighbour_IP, NodeCoord neighbor_NodeCoord){
		
		nodeCANData.getNeighborsIPInfo().put(neighbor_name, neighbour_IP);
		nodeCANData.getNeighborBoundaryInfo().put(neighbour_IP,neighbor_NodeCoord);
		
	}
	
	/**
	 * Update Neighbors Boundary 
	 * 
	 * @param ipAddressNei
	 * @param neiCoord
	 */
	public void updateNeighborBoundary(String ipAddressNei, NodeCoord neiCoord) throws RemoteException{
		nodeCANData.getNeighborBoundaryInfo().put(ipAddressNei, neiCoord);
	}
	
	/**
	 * Replace keys in node's table
	 */
	@Override
	public void replaceKeysinCANData(String oldKey, String newKey) {
		
		NodeCoord temp = null;
		//System.out.println("Called Replace keys");
		if(!nodeCANData.getNeighborBoundaryInfo().isEmpty() && !nodeCANData.getNeighborsIPInfo().isEmpty()){
			temp = nodeCANData.getNeighborBoundaryInfo().get(oldKey);
			if(!nodeCANData.getNeighborBoundaryInfo().containsKey(newKey) && !newKey.equals(nodeCANData.getName())){				
				//System.out.println(" Inside Replace Keys Somehow NEW Key " + newKey + " Name of Node " + nodeCANData.getName());
				nodeCANData.getNeighborBoundaryInfo().put(newKey, temp);
				nodeCANData.getNeighborsIPInfo().put(newKey, newKey);
			}
			nodeCANData.getNeighborBoundaryInfo().remove(oldKey);
			nodeCANData.getNeighborsIPInfo().remove(oldKey);
		}
		
	}
	
	/**
	 * Remove neighbor from CURRENT Node
	 * 
	 */
	public void removeNeighbor(String neighbor_IP) throws RemoteException{
		
		if(nodeCANData.getNeighborsIPInfo() != null && nodeCANData.getNeighborBoundaryInfo() != null){
			nodeCANData.getNeighborsIPInfo().remove(neighbor_IP);
			nodeCANData.getNeighborBoundaryInfo().remove(neighbor_IP);
		}
		
	}
	
	
	
	/**
	 * Populate new node's file Coord
	 * This functions removes files from current node
	 * if they dont fall in that zone anymore and
	 * add them to new node's file coord info
	 */
	
	public void addFilesToNewNode() throws RemoteException{
		
		
		nodeOwnCoord = nodeCANData.getOwnCoord();
		
		int file_x_coord = 0;
		int file_y_coord = 0;	
		int x1_coord = nodeOwnCoord.getX1_coord();
		int x2_coord = nodeOwnCoord.getX2_coord();
		int y1_coord = nodeOwnCoord.getY1_coord();
		int y2_coord = nodeOwnCoord.getY2_coord();
		ArrayList<String> filesToBeRemovedFromCurrent = new ArrayList<String>();
		
		if(nodeCANData.getFileCoordInfo() != null){
			
			for(Entry<String, FileCoord> fc : nodeCANData.getFileCoordInfo().entrySet()){
				
				file_x_coord = fc.getValue().getFile_x_coord();
				file_y_coord = fc.getValue().getFile_y_coord();
				
				if(!(file_x_coord >= x1_coord && file_x_coord <= x2_coord 
				   && file_y_coord >= y1_coord && file_y_coord <= y2_coord)){
					
					//System.out.println("Inside add files " + file_x_coord + " " +  file_y_coord + " " + x1_coord + " " + x2_coord + " " + y1_coord + " " + y2_coord);
					// Put file entries in new node's file coord info
					newNodefileCoordInfo.put(fc.getKey(), fc.getValue());
					
					// Remove Such files from current node's file coord info					
					filesToBeRemovedFromCurrent.add(fc.getKey());
					
				}
				
			}
			
			removeFilesFromCurrentNode(filesToBeRemovedFromCurrent);
			
		}
		
		
	}
	
	/**
	 * Remove files from current node
	 */
	
	public void removeFilesFromCurrentNode(ArrayList<String> filesToBeRemovedFromCurrent){
		
		if(filesToBeRemovedFromCurrent.size() > 0){
			
			Iterator<Map.Entry<String, FileCoord>> fileRemIter = nodeCANData.getFileCoordInfo().entrySet().iterator();
			
			while(fileRemIter.hasNext()){
				Entry<String, FileCoord> fc = fileRemIter.next();
				if(filesToBeRemovedFromCurrent.contains(fc.getKey())){
					fileRemIter.remove();
				}
			}
			
		}
		
	}
	
	/**
	 * Add neighbors to new Node and remove neighbors from current node
	 */
	
	public void addToNewNodeRemoveFromCurrent(NodeCoord currentNodeCoord, NodeCoord newNodeCoord, String ipAddress){
		
	
		
		/*
		 * Coords of new node
		 */
		
		int new_x1_coord = newNodeCoord.getX1_coord();
		int new_x2_coord = newNodeCoord.getX2_coord();
		int new_y1_coord = newNodeCoord.getY1_coord();
		int new_y2_coord = newNodeCoord.getY2_coord();
		
		
		//System.out.println("\nNew node x1 " + new_x1_coord);
		//System.out.println("New node x2 " + new_x2_coord);
		//System.out.println("New node y1 " + new_y1_coord);
		//System.out.println("New node y2 " + new_y2_coord);
		
		
		if(!nodeCANData.getNeighborBoundaryInfo().isEmpty() && !nodeCANData.getNeighborsIPInfo().isEmpty()){
			
			for(Entry<String, NodeCoord> nei_node : nodeCANData.getNeighborBoundaryInfo().entrySet()){
				
				System.out.println("\nMy neighbor " + nei_node.getKey());
				
				//System.out.println("Neighbor node x1 " + nei_node.getValue().getX1_coord());
				//System.out.println("Neighbor node x2 " + nei_node.getValue().getX2_coord());
				//System.out.println("Neighbor node y1 " + nei_node.getValue().getY1_coord());
				//System.out.println("Neighbor node y2 " + nei_node.getValue().getY2_coord());
		
				
				// Add neighbors to new node
				if(
					(
						(nei_node.getValue().getX1_coord() < new_x2_coord && nei_node.getValue().getX1_coord() >= new_x1_coord)
						||
						(nei_node.getValue().getX2_coord() <= new_x2_coord && nei_node.getValue().getX2_coord() > new_x1_coord)
					    ||
					    (nei_node.getValue().getX1_coord() == new_x1_coord && nei_node.getValue().getX2_coord() == new_x2_coord)			
					)	
					||
					(
						(nei_node.getValue().getY1_coord() >= new_y1_coord && nei_node.getValue().getY1_coord() < new_y2_coord)
						||
						(nei_node.getValue().getY2_coord() <= new_y2_coord && nei_node.getValue().getY2_coord() > new_y1_coord)
						||
						(nei_node.getValue().getY1_coord() == new_y1_coord && nei_node.getValue().getY2_coord() == new_y2_coord)
					)
				  ){
					
				//System.out.println("OK inside addToNewNodeRemoveFromCurrent");
					
					newNodeneighborBoundaryInfo.put(nei_node.getKey(), nei_node.getValue());
					newNodeneighborsIPInfo.put(nei_node.getKey(), nei_node.getKey()); // As we have ipAddress from nei_node.getKey() 
										

					try {
						CANNodeService neighborNode = (CANNodeService) Naming.lookup("rmi://" + nei_node.getKey() +"/" + nei_node.getKey());
						neighborNode.addNeighbor(ipAddress, ipAddress, newNodeCoord);
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}	
				
			}
			
			removeNeighborsFromCurrentNode(currentNodeCoord);
			
		}
		
		
	}
	
	/**
	 * Remove neighbors from current node and VICE VERSA
	 */
	public void removeNeighborsFromCurrentNode(NodeCoord currentNodeCoord){
		
		/*
		 * Coords of current node
		 */
		int current_x1_coord = currentNodeCoord.getX1_coord();
		int current_x2_coord = currentNodeCoord.getX2_coord();
		int current_y1_coord = currentNodeCoord.getY1_coord();
		int current_y2_coord = currentNodeCoord.getY2_coord();
		
		ArrayList<String> neighborsRemoveList = new ArrayList<String>();
		
		if(!nodeCANData.getNeighborBoundaryInfo().isEmpty() && !nodeCANData.getNeighborsIPInfo().isEmpty()){
			
			for(Entry<String, NodeCoord> nei_node : nodeCANData.getNeighborBoundaryInfo().entrySet()){
				
				// Add neighbors to new node
				if(!
					((
						(nei_node.getValue().getX1_coord() < current_x2_coord && nei_node.getValue().getX1_coord() >= current_x1_coord)
						||
						(nei_node.getValue().getX2_coord() <= current_x2_coord && nei_node.getValue().getX2_coord() > current_x1_coord)
					    ||
					    (nei_node.getValue().getX1_coord() == current_x1_coord && nei_node.getValue().getX2_coord() == current_x2_coord)			
					)	
					||
					(
						(nei_node.getValue().getY1_coord() >= current_y1_coord && nei_node.getValue().getY1_coord() < current_y2_coord)
						||
						(nei_node.getValue().getY2_coord() <= current_y2_coord && nei_node.getValue().getY2_coord() > current_y1_coord)
						||
						(nei_node.getValue().getY1_coord() == current_y1_coord && nei_node.getValue().getY2_coord() == current_y2_coord)
					))
				  ){
						//System.out.println("Inside removeNeighborsFromCurrentNode");
						neighborsRemoveList.add(nei_node.getKey());
					
				   }
				
			}
			
		}
		
		if(neighborsRemoveList.size() > 0){
			
			Iterator<Map.Entry<String, String>> neiIPRem = nodeCANData.getNeighborsIPInfo().entrySet().iterator();
			
			while(neiIPRem.hasNext()){
				Entry<String, String> neiIP = neiIPRem.next();
				if(neighborsRemoveList.contains(neiIP.getKey())){
					neiIPRem.remove();
				}
			}
			
		   Iterator<Map.Entry<String, NodeCoord>> neiCoordRem = nodeCANData.getNeighborBoundaryInfo().entrySet().iterator();
			
		   while(neiCoordRem.hasNext()){
			   Entry<String, NodeCoord> neiCoord = neiCoordRem.next();
			   if(neighborsRemoveList.contains(neiCoord.getKey())){
				   
				   /*
				    * To remove current node from its neighbors
				    */
					try {
						CANNodeService neighborNode = (CANNodeService) Naming.lookup("rmi://" + neiCoord.getKey() +"/" + neiCoord.getKey());
						neighborNode.removeNeighbor(nodeCANData.getName());;
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					/*
					 * Remove neighbor from current Node
					 */
				   neiCoordRem.remove();
			   }
		   }
		   
		}
		
		
	}
	
		
	/**
	 * 
	 * Update Current Nodes Zone(Boundary)
	 * 
	 * @param x1_coord
	 * @param x2_coord
	 * @param y1_coord
	 * @param y2_coord
	 */
	public void updateNodeBoundary(int x1_coord, int x2_coord, int y1_coord, int y2_coord){

		 nodeOwnCoord = nodeCANData.getOwnCoord();

		 nodeOwnCoord.setX1_coord(x1_coord);
		 nodeOwnCoord.setX2_coord(x2_coord);
		 nodeOwnCoord.setY1_coord(y1_coord);
		 nodeOwnCoord.setY2_coord(y2_coord); 
		 
		 nodeCANData.setOwnCoord(nodeOwnCoord);
		 
	}
	
	
	/**
	 * Assigns Zone to node
	 * @param file_name
	 */
	
	public void assignZoneToNewNode(int x1_coord, int x2_coord, int y1_coord, int y2_coord, NodeCoord nodeCoord){
		
		nodeCoord.setX1_coord(x1_coord);
		nodeCoord.setX2_coord(x2_coord);
		nodeCoord.setY1_coord(y1_coord);
		nodeCoord.setY2_coord(y2_coord);
		
	}
	
	
	/**
	 * To add new file to file map
	 * @param filename
	 * @param fileCoord
	 */
	
	public void updateFileMap(String filename, FileCoord fileCoord){
		
		if(!nodeCANData.getFileCoordInfo().containsKey(filename)){
			nodeCANData.getFileCoordInfo().put(filename, fileCoord);
		}
		
	}
	
	/**
	 * 
	 */
	@Override
	public void searchForFile(String ipAddress, String filename, int file_x_coord, int file_y_coord) throws RemoteException {
		
		int x1_coord = 0;
		int x2_coord = 0;
		int y1_coord = 0;
		int y2_coord = 0;
		String encryptedFile ="";
		boolean foundNeighbor = false;
		
		nodeOwnCoord = nodeCANData.getOwnCoord();
		
		
		if(file_x_coord >= nodeOwnCoord.getX1_coord() && file_x_coord <= nodeOwnCoord.getX2_coord() 
				&& file_y_coord >= nodeOwnCoord.getY1_coord() && file_y_coord <= nodeOwnCoord.getY2_coord()
		   ){
			
			System.out.println("Node IP to which File(Who searched for it) to be sent " + ipAddress);
			
			// TODO : Transfer File
			FileTransferInterface fileTraInterface = new FileTransferImpl();
			fileTraInterface.transferFile(ipAddress, filename);
			
		}else{

			for(Entry<String, NodeCoord> fileEntry :  nodeCANData.getNeighborBoundaryInfo().entrySet()){
	    	 
				x1_coord = fileEntry.getValue().getX1_coord();
				x2_coord = fileEntry.getValue().getX2_coord();
				y1_coord = fileEntry.getValue().getY1_coord();
				y2_coord = fileEntry.getValue().getY2_coord();		    	 
	    	 
				if(file_x_coord >= x1_coord && file_x_coord <= x2_coord && file_y_coord >= y1_coord && file_y_coord <= y2_coord){
	    		 
					try {
						CANNodeService neighborForFile = (CANNodeService) Naming.lookup("rmi://" + fileEntry.getKey() + "/" + fileEntry.getKey());
						neighborForFile.searchForFile(ipAddress,filename, file_x_coord, file_y_coord);						
						foundNeighbor = true;
						break;
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    	   }
	    	 
	       }
	     
	       if(!foundNeighbor){
	    	 
	    	 String fileRouteNode = routingAlgorithm(nodeCANData.getNeighborBoundaryInfo(), file_x_coord, file_y_coord);
	    	 try {
					CANNodeService neighborForFile = (CANNodeService) Naming.lookup("rmi://" + fileRouteNode + "/" + fileRouteNode);
					neighborForFile.searchForFile(ipAddress, filename, file_x_coord, file_y_coord);
				} catch (MalformedURLException | RemoteException
						| NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	 
	       }	     
	   
		}	
		
		
	}



	public void searchFileInCANRequest(String ipAddress, String file_name) throws RemoteException{
		
		String encryptedFile ="";
		boolean gotFile = false;
		FileCoord fc = null;
		/** Calculate Coordinates **/
		MessageDigest md;
		try {
			
			md = MessageDigest.getInstance("md5");
			byte[] hashedXCoord = md.digest(file_name.getBytes()); 
			
			StringBuffer stringBuffer = new StringBuffer();
		    for (int i = 0; i < hashedXCoord.length; i++) {
		        stringBuffer.append(Integer.toString((hashedXCoord[i] & 0xff) + 0x100, 16)
		                .substring(1));
		    }
		    
		     encryptedFile = stringBuffer.toString();
		     
		     //System.out.println(encryptedFile);
		     
		     StringBuffer stringBuffer1 = new StringBuffer();
		     
		     for(int i=0;i<encryptedFile.length();i++)
		     {
		        if(Character.isDigit(encryptedFile.charAt(i)))
		        stringBuffer1.append(encryptedFile.charAt(i));
		     }
		     
		      encryptedFile = stringBuffer1.toString();
		     
		     int file_x_coord = Integer.parseInt((encryptedFile.substring(0, 6)), 16) % 100;
		     int file_y_coord = Integer.parseInt((encryptedFile.substring(0, 6)), 16) % 100;
		    
		     
		    searchForFile(ipAddress, file_name, file_x_coord, file_y_coord);
		     
		     
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * Store file in CAN
	 * 
	 * @param filename
	 */
	
	public void storeFileinCANRequest(String filename) throws RemoteException{
		
		int file_x_coord = 0;
		int file_y_coord = 0;
		
		String encryptedFile ="";
		
		/** Calculate Coordinates **/
		MessageDigest md;
		try {
			
			md = MessageDigest.getInstance("md5");
			byte[] hashedXCoord = md.digest(filename.getBytes()); 
			
			StringBuffer stringBuffer = new StringBuffer();
		    for (int i = 0; i < hashedXCoord.length; i++) {
		        stringBuffer.append(Integer.toString((hashedXCoord[i] & 0xff) + 0x100, 16)
		                .substring(1));
		    }
		    
		     encryptedFile = stringBuffer.toString();
		     
		     //System.out.println(encryptedFile);
		     
		     StringBuffer stringBuffer1 = new StringBuffer();
		     
		     for(int i=0;i<encryptedFile.length();i++)
		     {
		        if(Character.isDigit(encryptedFile.charAt(i)))
		        stringBuffer1.append(encryptedFile.charAt(i));
		     }
		     
		      encryptedFile = stringBuffer1.toString();
		     
		     file_x_coord = Integer.parseInt((encryptedFile.substring(0, 6)), 16) % 100;
		     file_y_coord = Integer.parseInt((encryptedFile.substring(0, 6)), 16) % 100;
		    
		     
		     System.out.println("\n File X Coord " + file_x_coord + " File Y Coord " + file_y_coord + "\n");
		     
		     ipOfNodeToSendFile = storeFileUsingRoutingInCAN(filename, file_x_coord, file_y_coord);
		     
		     System.out.println(" \n Node to send file to store is " + ipOfNodeToSendFile);
		     
		     // Send the file
		     FileTransferInterface ftp = new FileTransferImpl();
		     
		     try{
		    	 ftp.transferFile(ipOfNodeToSendFile, filename);
		     }catch(Exception e){
		    	 System.err.println(" Unable to transfer File");
		     }
		     
		     
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	@Override
	public String storeFileUsingRoutingInCAN(String filename, int file_x_coord, int file_y_coord) throws RemoteException{
		
		int x1_coord = 0;
		int x2_coord = 0;
		int y1_coord = 0;
		int y2_coord = 0;
		boolean foundNeighbor = false;
		String ipToSend = "";
		FileCoord fc;
		
		
		nodeOwnCoord = nodeCANData.getOwnCoord();
		
		if(file_x_coord >= nodeOwnCoord.getX1_coord() && file_x_coord <= nodeOwnCoord.getX2_coord() 
				&& file_y_coord >= nodeOwnCoord.getY1_coord() && file_y_coord <= nodeOwnCoord.getY2_coord()
		   ){
			
			// TODO : Save file
			
			// If already has file
			
			if(!nodeCANData.getFileCoordInfo().containsKey(filename)){
				// Store
				fc = new FileCoord();
				fc.setFile_x_coord(file_x_coord);
				fc.setFile_y_coord(file_y_coord);
				nodeCANData.getFileCoordInfo().put(filename, fc);
				return nodeCANData.getName();
			}
			
			
		}else{
			
			for(Entry<String, NodeCoord> fileEntry :  nodeCANData.getNeighborBoundaryInfo().entrySet()){
		    	 
				x1_coord = fileEntry.getValue().getX1_coord();
				x2_coord = fileEntry.getValue().getX2_coord();
				y1_coord = fileEntry.getValue().getY1_coord();
				y2_coord = fileEntry.getValue().getY2_coord();		    	 
	    	 
				if(file_x_coord >= x1_coord && file_x_coord <= x2_coord && file_y_coord >= y1_coord && file_y_coord <= y2_coord){
	    		 
					try {
						CANNodeService neighborForFile = (CANNodeService) Naming.lookup("rmi://" + fileEntry.getKey() + "/" + fileEntry.getKey());
						ipToSend = neighborForFile.storeFileUsingRoutingInCAN(filename, file_x_coord, file_y_coord);						
						foundNeighbor = true;
						break;
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    	   }
	    	 
	       }
			
			if(!foundNeighbor){
		    	 
		    	 String fileRouteNode = routingAlgorithm(nodeCANData.getNeighborBoundaryInfo(), file_x_coord, file_y_coord);
		    	 try {
						CANNodeService neighborForFile = (CANNodeService) Naming.lookup("rmi://" + fileRouteNode + "/" + fileRouteNode);
						ipToSend = neighborForFile.storeFileUsingRoutingInCAN(filename, file_x_coord, file_y_coord);
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	 
		       }
			
		}
		
		return ipToSend;
		
	}
	
	
	/**
	 * To check whether neighbor condition still holds
	 */
	 public boolean stillANeighbor(){
		 
		 return false;
	 }
	
	
	public void cleanUpTempControlledZone(){
		
		
		
		
		
	}
	
	
	/**
	 * Give control of temp zone to new joining node
	 * 
	 * @param ipAddress
	 * @return
	 */
	public CANData giveControlOfTempZoneToNewNode(String ipAddress){
		
		sendInfoToBootstrap = false;
		CANData tempData = controlledZoneCANDATA;
		tempData.setName(ipAddress);
		controlledZoneCANDATA = null;
		return tempData;
		
	}
	
	
/**
	 * Take temporary or complete control of left node's zone
	 */
	public void takeControlOfLeftNode(CANData leftNeighborCANData, boolean sendInfoBootstrap){
		
		int file_x_coord = 0;
		int file_y_coord = 0;
		int x1_coord = 0;
		int x2_coord = 0;
		int y1_coord = 0;
		int y2_coord = 0;
		
		//System.out.println(" Inside Take control " + nodeCANData.getName());
		
		
		nodeOwnCoord = nodeCANData.getOwnCoord();
		
				
				x1_coord = leftNeighborCANData.getOwnCoord().getX1_coord();
				x2_coord = leftNeighborCANData.getOwnCoord().getX2_coord();
				y1_coord = leftNeighborCANData.getOwnCoord().getY1_coord();
				y2_coord = leftNeighborCANData.getOwnCoord().getY2_coord();
				
				
				
				if(nodeOwnCoord.getX1_coord() != x1_coord){
					nodeOwnCoord.setX1_coord((nodeOwnCoord.getX1_coord() < x1_coord) ? nodeOwnCoord.getX1_coord() : x1_coord);
				}
				
				if(nodeOwnCoord.getX2_coord() != x2_coord){
					nodeOwnCoord.setX2_coord((nodeOwnCoord.getX2_coord() < x2_coord) ?  x2_coord : nodeOwnCoord.getX2_coord());
				}
				
				if(nodeOwnCoord.getY1_coord() != y1_coord){
					nodeOwnCoord.setY1_coord((nodeOwnCoord.getY1_coord() < y1_coord) ? nodeOwnCoord.getY1_coord() : y1_coord);
				}
				
				if(nodeOwnCoord.getY2_coord() != y2_coord){
					nodeOwnCoord.setY2_coord((nodeOwnCoord.getY2_coord() < y2_coord) ? y2_coord : nodeOwnCoord.getY2_coord());
				}
				
		//		System.out.println("New x1 -coord " + nodeOwnCoord.getX1_coord());
		//		System.out.println("New x2 -coord " + nodeOwnCoord.getX2_coord());
		//		System.out.println("New y1 -coord " + nodeOwnCoord.getY1_coord());
		//		System.out.println("New y2 -coord " + nodeOwnCoord.getY2_coord());
				
				
			if(!(nodeCANData.getNeighborBoundaryInfo().isEmpty() && nodeCANData.getNeighborsIPInfo().isEmpty())){
						
				// Tell neighbors to update this node's boundary in their table
				for(Entry<String, String> neiIp : nodeCANData.getNeighborsIPInfo().entrySet()){
					
					try {
						CANNodeService neiNode = (CANNodeService) Naming.lookup("rmi://"+neiIp.getKey()+"/"+neiIp.getKey());
						neiNode.updateNeighborBoundary(nodeCANData.getName(), nodeOwnCoord);
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			
			}else{
				try {
					BootsrapService bootstrap = (BootsrapService) Naming.lookup("rmi://"+getBootstrapIP()+"/BootstrapServer");
					bootstrap.updateNeighbor(nodeCANData.getName());
					System.out.println("Bootstrap neighbor Updated");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
			
			if(!(leftNeighborCANData.getNeighborBoundaryInfo().isEmpty() && leftNeighborCANData.getNeighborsIPInfo().isEmpty())){
			
			
				for(Entry<String, NodeCoord> neighborsCoord : leftNeighborCANData.getNeighborBoundaryInfo().entrySet()){
				
					if(!nodeCANData.getNeighborBoundaryInfo().containsKey(neighborsCoord.getKey()) && !nodeCANData.getName().equals(neighborsCoord.getKey())){
						nodeCANData.getNeighborBoundaryInfo().put(neighborsCoord.getKey(), neighborsCoord.getValue());
						nodeCANData.getNeighborsIPInfo().put(neighborsCoord.getKey(), neighborsCoord.getKey());
						tempNeighbors.add(neighborsCoord.getKey());
					}					
			
				}
			
			}
			
			if(!leftNeighborCANData.getFileCoordInfo().isEmpty()){
			
				for(Entry<String, FileCoord> fileEn : leftNeighborCANData.getFileCoordInfo().entrySet()){
				
					nodeCANData.getFileCoordInfo().put(fileEn.getKey(), fileEn.getValue());
					tempFiles.add(fileEn.getKey());
				
				}
			
			}
			
			
		
		
	}
	




	/**
	 * Leave CAN 
	 */
	
	@Override
	public void leaveCAN() throws RemoteException{
		
		if(!nodeCANData.getNeighborBoundaryInfo().isEmpty()){
			
			int minArea = Integer.MAX_VALUE;
			int x_length = 0;
			int y_length = 0;
			int neighborArea = 0;
			int x1_coord = nodeCANData.getOwnCoord().getX1_coord();
			int x2_coord = nodeCANData.getOwnCoord().getX2_coord();
			int y1_coord = nodeCANData.getOwnCoord().getY1_coord();
			int y2_coord = nodeCANData.getOwnCoord().getY2_coord();
			int nei_x1 = 0;
			int nei_x2 = 0;
			int nei_y1 = 0;
			int nei_y2 = 0;
			String neighborIP = "";
			
			for(Entry<String, NodeCoord> neighbor : nodeCANData.getNeighborBoundaryInfo().entrySet()){
				
				x_length = (int) Math.abs( neighbor.getValue().getX2_coord() - neighbor.getValue().getX1_coord());
				y_length = (int) Math.abs( neighbor.getValue().getY2_coord() - neighbor.getValue().getY1_coord());
				
				neighborArea = x_length * y_length;
				
				if(minArea > neighborArea){
					minArea = neighborArea;
					neighborIP = neighbor.getKey();
					nei_x1 = neighbor.getValue().getX1_coord();
					nei_x2 = neighbor.getValue().getX2_coord();
					nei_y1 = neighbor.getValue().getY1_coord();
					nei_y2 = neighbor.getValue().getY2_coord();
				}
				
			}
						 
			try {
				System.out.println("IP of min neighbor " + neighborIP);
				CANNodeService controllingNeighbor = (CANNodeService) Naming.lookup("rmi://"+neighborIP+"/"+neighborIP);
				controllingNeighbor.removeNeighbor(nodeCANData.getName());
				FileTransferInterface filesToSend = new FileTransferImpl();
				Path path = null;
				if(!nodeCANData.getFileCoordInfo().isEmpty()){
					
					for(Entry<String, FileCoord> fileCo : nodeCANData.getFileCoordInfo().entrySet()){
						
						filesToSend.transferFile(neighborIP, fileCo.getKey());
						path = Paths.get(fileCo.getKey());
						//Files.deleteIfExists(path);
						
					}
					
				}
				
				for(Entry<String, NodeCoord> neighborsCoord : nodeCANData.getNeighborBoundaryInfo().entrySet()){
					
					// update each neighbor about change
					try {
						CANNodeService neighborNode = (CANNodeService) Naming.lookup("rmi://"+neighborsCoord.getKey()+"/"+neighborsCoord.getKey());
						neighborNode.replaceKeysinCANData(nodeCANData.getName(), neighborIP);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
				
				}
				
				if(getIsBootstrapNeighbor()){
					
					try {
						BootsrapService bootstrap = (BootsrapService) Naming.lookup("rmi://"+getBootstrapIP()+"/BootstrapServer");
						bootstrap.updateNeighbor(neighborIP);
						controllingNeighbor.setIsBootstrapNeighbor(true);
						System.out.println("Bootstrap neighbor Updated");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				controllingNeighbor.takeControlOfLeftNode(nodeCANData, sendInfoToBootstrap);
				
				nodeCANData = null;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}else{
			// TODO : Tell bootstrap to remove this node as neighbor
				try {
					BootsrapService bootstrap = (BootsrapService) Naming.lookup("rmi://"+getBootstrapIP()+"/BootstrapServer");
					bootstrap.updateBootstrapOnAllLeave();
					System.out.println("All left");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			nodeCANData = null;
		}
		
	}
	
	/**
	 * Routing Algorithm
	 * @param file_name
	 * Return Ip Address of minimum distance Node
	 */
	
	public String routingAlgorithm(HashMap<String, NodeCoord> neighborMap, int x_coord, int y_coord) {
		
		int min_within_node = Integer.MAX_VALUE;
		int distance = Integer.MAX_VALUE;
		HashMap<String, Integer>neighborNodeAndItsMin = new HashMap<String,Integer>();
		
		int nei_x1_coord = 0;
		int nei_x2_coord = 0;
		int nei_y1_coord = 0;
		int nei_y2_coord = 0;
		
		for(Entry<String, NodeCoord> neighborNodeForMin : neighborMap.entrySet()){
			
			min_within_node = Integer.MAX_VALUE;
								
			nei_x1_coord = neighborNodeForMin.getValue().getX1_coord();
			nei_x2_coord = neighborNodeForMin.getValue().getX2_coord();
			nei_y1_coord = neighborNodeForMin.getValue().getY1_coord();
			nei_y2_coord = neighborNodeForMin.getValue().getY2_coord();
			
			distance = (int)Math.sqrt(
					(Math.pow(nei_x1_coord,2) - Math.pow(x_coord,2))
					+
					(Math.pow(nei_y1_coord,2) - Math.pow(y_coord,2)));
			
			if(distance < min_within_node){
				min_within_node = distance;
			}
			
			distance = (int)Math.sqrt(
					(Math.pow(nei_x2_coord,2) - Math.pow(x_coord,2))
					+
					(Math.pow(nei_y1_coord,2) - Math.pow(y_coord,2)));
			
			if(distance < min_within_node){
				min_within_node = distance;
			}
			
			distance = (int)Math.sqrt(
					(Math.pow(nei_x1_coord,2) - Math.pow(x_coord,2))
					+
					(Math.pow(nei_y2_coord,2) - Math.pow(y_coord,2)));
			
			if(distance < min_within_node){
				min_within_node = distance;
			}
			
			distance = (int)Math.sqrt(
					(Math.pow(nei_x2_coord,2) - Math.pow(x_coord,2))
					+
					(Math.pow(nei_y2_coord,2) - Math.pow(y_coord,2)));
			
			if(distance < min_within_node){
				min_within_node = distance;
			}
			
			neighborNodeAndItsMin.put(neighborNodeForMin.getKey(), min_within_node);
			
		}
		
		int min_amongst_nodes = Integer.MAX_VALUE;
		String ipOfMinDistNeighbor = null;
		
		for(Entry<String, Integer> minNeighbor : neighborNodeAndItsMin.entrySet()){
			
			if(min_amongst_nodes < minNeighbor.getValue()){
				ipOfMinDistNeighbor = minNeighbor.getKey();
			}
			
		}
		
		return ipOfMinDistNeighbor;
		
	}
	
	
	/**
	 * Return can data for new joining node
	 */
	public CANData returnCANdata(){
		return newNodeCANData;
	}
	

	@Override
	public String joinNode(String ipAddress, int x_coord, int y_coord) throws RemoteException {
		
		
		boolean divideHorizontally = false;
		boolean divideVertically = false;
		String IPAddress="";
		try{
			IPAddress = InetAddress.getLocalHost().getHostAddress();
		}catch(Exception e){
			System.out.println("Problem in join CAN ");
		}
		/*
		 * First Check in Node's own coordinates boundary 
		 */
		
		nodeOwnCoord = nodeCANData.getOwnCoord(); // Getting node's corrdinate
		
		
		int x1_coord = nodeOwnCoord.getX1_coord();
		int x2_coord = nodeOwnCoord.getX2_coord();
		int y1_coord = nodeOwnCoord.getY1_coord();
		int y2_coord = nodeOwnCoord.getY2_coord();
		
		
		//System.out.println("Inside CAN join");

		
		if(x_coord >= x1_coord && x_coord <= x2_coord && y_coord >= y1_coord && y_coord <= y2_coord){
			
			NodeCoord newNodeCoord = new NodeCoord(); // Set the node coordinates(Boundary) for new node
			
			// FileCoord newFileCoord; To store file coord from current node to new Node. No need right now  
						
			/*
			 *  Check how to divide the Zone
			 */
			
			int node_zone_x_length = x2_coord - x1_coord;
			int node_zone_y_length = y2_coord - y1_coord;
			
			if(node_zone_x_length >= node_zone_y_length){
				divideVertically = true;
			}else{
				divideHorizontally = true;
			}
			
			
			
			/*
			 * Depending on zone split and coordinate where new node wants to join
			 * handle node join
			 * If coordinates dont fall on boundary , always give current zone the boundary coords
			 */
			
			if(divideVertically){
				
				if(x_coord < (x2_coord / 2)){
					assignZoneToNewNode(x1_coord, (x2_coord / 2) - 1, y1_coord, y2_coord, newNodeCoord); // Assign new zone to new node
					updateNodeBoundary((x2_coord / 2), x2_coord, y1_coord, y2_coord);
				}else if(x_coord > (x2_coord / 2)){
					assignZoneToNewNode((x2_coord / 2) + 1, x2_coord, y1_coord, y2_coord, newNodeCoord);
					updateNodeBoundary(x1_coord, (x2_coord / 2), y1_coord, y2_coord);
				}else{
					// If coordinates fall on boundary
					// Always give new node right zone
					assignZoneToNewNode(x2_coord / 2, x2_coord, y1_coord, y2_coord, newNodeCoord);
					updateNodeBoundary(x1_coord, (x2_coord / 2) - 1, y1_coord, y2_coord);
				}
				
			}else{ // Divide Horizontally
				
				if(y_coord < (y2_coord / 2)){
					assignZoneToNewNode(x1_coord, x2_coord, y1_coord, (y2_coord / 2) - 1, newNodeCoord);
					updateNodeBoundary(x1_coord, x2_coord, y2_coord / 2, y2_coord);
				}else if(y_coord > (y2_coord)){
					assignZoneToNewNode(x1_coord, x2_coord, (y2_coord) + 1, y2_coord, newNodeCoord);
					updateNodeBoundary(x1_coord, x2_coord, y1_coord, (y2_coord / 2));
				}else{
					// If coordinates fall on boundary
					// Always give new node lower zone
					assignZoneToNewNode(x1_coord, x2_coord, y1_coord, (y2_coord / 2), newNodeCoord);
					updateNodeBoundary(x1_coord, x2_coord, (y2_coord / 2) + 1, y2_coord);
				}
				
			}
			
			
			
			/*
			 * Add new node as neighbor to current node and vice versa 
			 */
			
			//Add the current node as neighbor of new node
			newNodeneighborBoundaryInfo.put(nodeCANData.getName(),nodeOwnCoord);
			newNodeneighborsIPInfo.put(nodeCANData.getName(), nodeCANData.getName());
			
			
			/*
			 * Add new neighbors to new node considering change in boundary
			 * 
			 */
			
			 addToNewNodeRemoveFromCurrent(nodeOwnCoord, newNodeCoord, ipAddress);
			 
			// Add new node neighbor to current node
			addNeighbor(ipAddress, ipAddress, newNodeCoord);
			
			
			/*
			 * Transfer files from current node to new node if they
			 * dont fall under current node's new zone anymore
			 */
			
			addFilesToNewNode();
			
			
			
			/*
			 * Tell neighbors to update me
			 */
			 if(!(nodeCANData.getNeighborBoundaryInfo().isEmpty() && nodeCANData.getNeighborsIPInfo().isEmpty())){
						
				// Tell neighbors to update this node's boundary in their table
				for(Entry<String, String> neiIp : nodeCANData.getNeighborsIPInfo().entrySet()){
					
					try {
						CANNodeService neiNode = (CANNodeService) Naming.lookup("rmi://"+neiIp.getKey()+"/"+neiIp.getKey());
						neiNode.updateNeighborBoundary(nodeCANData.getName(), nodeOwnCoord);
					} catch (MalformedURLException | RemoteException
							| NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
			
			}
			
			 /*
			  * Create new Node's Data Structure
			  */
			
			 newNodeCANData.setName(ipAddress);
			 newNodeCANData.setFileCoordInfo(newNodefileCoordInfo);
			 newNodeCANData.setNeighborBoundaryInfo(newNodeneighborBoundaryInfo);
			 newNodeCANData.setNeighborsIPInfo(newNodeneighborsIPInfo);
			 newNodeCANData.setOwnCoord(newNodeCoord);
			
		}else{
			
			
			HashMap<String, NodeCoord> neighborMap = nodeCANData.getNeighborBoundaryInfo();
			
			//HashMap<String,Integer> neighborNodeAndItsMin = null;
			
			/*
			 * Find if coordinate of new node is contained within the neighbors zone
			 */			
			
			Iterator<Map.Entry<String, NodeCoord>> neighborIterator = neighborMap.entrySet().iterator();
			Entry<String, NodeCoord> neighborEntry = null;
			int nei_x1_coord = 0;
			int nei_x2_coord = 0;
			int nei_y1_coord = 0;
			int nei_y2_coord = 0;
			boolean gotNeighbor = false;
						
			while(neighborIterator.hasNext()){
				neighborEntry = neighborIterator.next();
				
				nei_x1_coord = neighborEntry.getValue().getX1_coord();
				nei_x2_coord = neighborEntry.getValue().getX2_coord();
				nei_y1_coord = neighborEntry.getValue().getY1_coord();
				nei_y2_coord = neighborEntry.getValue().getY2_coord();				
				
				if(
					x_coord >= nei_x1_coord && x_coord <= nei_x2_coord &&
					y_coord >= nei_y1_coord && y_coord <= nei_y2_coord
				  ){
					gotNeighbor = true;
					break;
				}	
				
			}
			
			if(gotNeighbor){ // Got neighbor with correct boundary
				
				try {
					CANNodeService neighborNode = (CANNodeService) Naming.lookup("rmi://" + neighborEntry.getKey() +"/" + neighborEntry.getKey());
					IPAddress = neighborNode.joinNode(ipAddress, x_coord, y_coord);
				} catch (MalformedURLException | RemoteException
						| NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}else{ // No such neighbor with boundary
				
				String ipNeighborMinimumDist = routingAlgorithm(neighborMap, x_coord, y_coord); 
				
				try {
					CANNodeService neighborNodeMinDist = (CANNodeService) Naming.lookup("rmi://" + ipNeighborMinimumDist +"/" + ipNeighborMinimumDist);
					IPAddress = neighborNodeMinDist.joinNode(ipAddress, x_coord, y_coord);
				} catch (MalformedURLException | RemoteException
						| NotBoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
			
		}
		
		return IPAddress;
	}
	
	
	
	public void printInfoAboutNode(){
		
		System.out.println("\nWelcome to Node " + nodeCANData.getName());
		
		nodeOwnCoord = nodeCANData.getOwnCoord();
		
		System.out.println("Node's Coordinate : ");
		System.out.println("X1 Coord = " + nodeOwnCoord.getX1_coord());
		System.out.println("X2 Coord = " + nodeOwnCoord.getX2_coord());
		System.out.println("Y1 Coord = " + nodeOwnCoord.getY1_coord());
		System.out.println("Y2 Coord = " + nodeOwnCoord.getY2_coord());
		
		
		System.out.println("\nFile Info");
		  if(!nodeCANData.getFileCoordInfo().isEmpty()){
			  for(Entry<String, FileCoord> neiFile : nodeCANData.getFileCoordInfo().entrySet()){
				  
				  System.out.println("File Name " + neiFile.getKey());
				  System.out.println("File X Coord " + neiFile.getValue().getFile_x_coord() + 
						             " File Y Coord " + neiFile.getValue().getFile_y_coord()); 
				  
			  }
		  }else{
			  System.out.println("No file Yet");
		  }
		
		System.out.println("\nNeighbor Info");
		if(!nodeCANData.getNeighborBoundaryInfo().isEmpty()){
			for(Entry<String, NodeCoord> neiCoord : nodeCANData.getNeighborBoundaryInfo().entrySet()){
				System.out.println("Neighbor " + neiCoord.getKey());
				System.out.println("Neighbor's Coordinate : ");
				System.out.println("X1 Coord = " + neiCoord.getValue().getX1_coord());
				System.out.println("X2 Coord = " + neiCoord.getValue().getX2_coord());
				System.out.println("Y1 Coord = " + neiCoord.getValue().getY1_coord());
				System.out.println("Y2 Coord = " + neiCoord.getValue().getY2_coord());
				
				
			}
		}else{
			System.out.println("No Neighbor Yet");
		}
		
	  
		
	}
	
	
	@Override
	public void run() {
		
		
	}
	
	
	public void setIsBootstrapNeighbor(boolean isBN) throws RemoteException{
		isBootstrapNeighbor = isBN;
	}
	
	public boolean getIsBootstrapNeighbor(){
		return isBootstrapNeighbor;
	}
	
	public CANData getCANData(){
		return this.nodeCANData;
	}
	
	public void setCANData(CANData canData){
		this.nodeCANData = canData;
	}
	
	
	public String getBootstrapIP() {
		return bootstrapIP;
	}

	public void setBootstrapIP(String bootstrapIP) {
		this.bootstrapIP = bootstrapIP;
	}

	public static void main(String[] args) {
		
		CANNodeServiceImpl canService = null;
		String nameObj = "";
		String fileToSend = "";
		String bootIP = "";
		try {
			
			canService = new CANNodeServiceImpl();
			nameObj = InetAddress.getLocalHost().getHostAddress();
			System.out.println("Node Name -> " + nameObj);
			Naming.rebind(nameObj, canService);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        Scanner sc = new Scanner(System.in);
		
		System.out.println("**** WELCOME TO CAN ****");
		
		boolean leaveSys = false;
				
		System.out.println("Enter Bootstrap's IP : ");
		
		bootIP = sc.nextLine();
		
		canService.setBootstrapIP(bootIP);
		
		String ipToJoin =  "";	
		
		
			
				try {
					canService.getCANData().setName(InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				try {
					BootsrapService bootstrap = (BootsrapService) Naming.lookup("rmi://"+canService.getBootstrapIP()+"/BootstrapServer");
					ipToJoin = bootstrap.joinCAN(InetAddress.getLocalHost().getHostAddress().toString(), 0, 0);
					System.out.println("Called");
				} catch (MalformedURLException | RemoteException
						| NotBoundException | UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
				System.out.println("IP To Join " + ipToJoin);

				if(ipToJoin == null){
					System.out.println();
					//System.out.println(nodeOwnCoord);
					canService.updateNodeBoundary(0, 100, 0, 100);
					try{
						canService.setIsBootstrapNeighbor(true);
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					try {
					CANNodeService nodeToJoin = (CANNodeService) Naming.lookup("rmi://"+ipToJoin+"/"+ipToJoin);
					canService.setCANData(nodeToJoin.returnCANdata());
					System.out.println("Called");
					} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
				}
			
				canService.printInfoAboutNode();
				
				
		
		
		
       while(true){
			
		
		System.out.println("1. File Store");
		System.out.println("2. File Search");
		System.out.println("3. Leave CAN");
		System.out.println("4. Print Node Information ");
		
		System.out.println("\nEnter Your Choice : ");
		
			if(leaveSys){
				break;
			}
			
			choice = sc.nextInt();		
			
			
			
			switch(choice){
				
				
			case 2:
				System.out.println("**** Search & Retreive File in CAN ***");
				System.out.println("Enter the name of File -> ");
				String dum = sc.nextLine();
				String fileToSearch = sc.nextLine();
				System.out.println("File name is " + fileToSearch);
				
				try{
					canService.searchFileInCANRequest(canService.getCANData().getName(), fileToSearch);
				}catch(RemoteException re){
					re.printStackTrace();
				}
				
				
				
				break;
				
				
			case 1:
				System.out.println("**** Store File in CAN ***");
				System.out.println("Enter the name of File -> ");
				dum = sc.nextLine();
				fileToSend = sc.nextLine();
				System.out.println("File name is " + fileToSend);
				
				
				try{
					canService.storeFileinCANRequest(fileToSend);
				}catch(RemoteException re){
					re.printStackTrace();
				}
				break;
			case 3:
				leaveSys = true;
				try{
					canService.leaveCAN();
				}catch(Exception e){
					e.printStackTrace();
				}
				System.out.println(" Nice CANNING with you ! Bye !! ");
				System.exit(0);
				break;
			case 4:			
				canService.printInfoAboutNode();
				break;
			}
			
		}
		
	}

	

	

	

	
}
