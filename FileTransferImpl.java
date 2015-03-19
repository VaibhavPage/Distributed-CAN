import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;

public class FileTransferImpl extends UnicastRemoteObject implements FileTransferInterface, Serializable{


	static String filePath;
	
	
	public FileTransferImpl() throws RemoteException{
		
	}

	@Override
	public void acceptFile(String filename, byte[] fileData) throws RemoteException{
		
		BufferedOutputStream br = null;
		try {
		//URL res = getClass().getClassLoader().getResource(filename);
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir+"/"+filename);
		byte[] buffer = fileData;
		//System.out.println("File to accept is " + filePath+filename);
		
		
			br = new BufferedOutputStream(new FileOutputStream(file));
			br.write(buffer, 0, buffer.length);
			br.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try{
				br.close();
			}catch(IOException io){
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void transferFile(String ipAddress, String filename) throws RemoteException{
		
		BufferedInputStream br = null;
		try {
		//System.out.println("File to transfer is " + filePath+filename);
		//URL res = getClass().getClassLoader().getResource(filename);
		String workingDir = System.getProperty("user.dir");
		File file = new File(workingDir+"/"+filename);
		byte[] buffer = new byte[(int) file.length()];
		
		
			br = new BufferedInputStream(new FileInputStream(file));
			br.read(buffer, 0, buffer.length);
			FileTransferInterface fileTransObj = (FileTransferInterface) Naming.lookup("rmi://"+ipAddress+"/"+ipAddress+"FileTransferObject");
			fileTransObj.acceptFile(filename, buffer);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try{
				br.close();
			}catch(IOException io){
				e.printStackTrace();
			}
			
		}
		
		
	}
	
	
	public static void main(String[] args){
		
		
		try{
			
			//Scanner sc = new Scanner(System.in);
			//System.out.println("Enter file path. PLEASE Provide absolute path. Thanks!!  : ");
			//filePath = sc.nextLine();
			
			FileTransferImpl fileTrans = new FileTransferImpl();
			
			
			//System.out.println("File path you set is " + filePath);
			String fileTransName = InetAddress.getLocalHost().getHostAddress();
			Naming.rebind(fileTransName+"FileTransferObject", fileTrans);
			
		}catch(Exception e){
			
			e.printStackTrace();
			
		}
		
	}

}
