import java.io.Serializable;
import java.util.HashMap;

import javax.xml.soap.Node;


class NodeCoord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int x1_coord;
	private int x2_coord;
	private int y1_coord;
	private int y2_coord;
	
	
	public int getX1_coord() {
		return x1_coord;
	}
	public void setX1_coord(int x1_coord) {
		this.x1_coord = x1_coord;
	}
	public int getX2_coord() {
		return x2_coord;
	}
	public void setX2_coord(int x2_coord) {
		this.x2_coord = x2_coord;
	}
	public int getY1_coord() {
		return y1_coord;
	}
	public void setY1_coord(int y1_coord) {
		this.y1_coord = y1_coord;
	}
	public int getY2_coord() {
		return y2_coord;
	}
	public void setY2_coord(int y2_coord) {
		this.y2_coord = y2_coord;
	}
	
	
}


class FileCoord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private int file_x_coord;
	private int file_y_coord;
	
	
	public int getFile_x_coord() {
		return file_x_coord;
	}
	public void setFile_x_coord(int file_x_coord) {
		this.file_x_coord = file_x_coord;
	}
	public int getFile_y_coord() {
		return file_y_coord;
	}
	public void setFile_y_coord(int file_y_coord) {
		this.file_y_coord = file_y_coord;
	}
	
	
	
	
}

public class CANData implements Serializable{

	/**
	 *  Implements Serializable
	 */
	private static final long serialVersionUID = 1L;
	
	
	// Nodes IP
	
	private String name;

	// Info about neighboring node IPs
	private HashMap<String, String> neighborsIPInfo = new HashMap<String, String>();
	
	// Info about boundaries of neighbors
	private HashMap<String, NodeCoord> neighborBoundaryInfo = new HashMap<String, NodeCoord>();
	
	// Info about coordination where file is located
	private HashMap<String, FileCoord> fileCoordInfo = new HashMap<String, FileCoord>();
	
	private NodeCoord ownCoord = new NodeCoord();
	
	private FileCoord ownFileCoord = new FileCoord();

	public HashMap<String, String> getNeighborsIPInfo() {
		return neighborsIPInfo;
	}

	public void setNeighborsIPInfo(HashMap<String, String> neighborsIPInfo) {
		this.neighborsIPInfo = neighborsIPInfo;
	}

	public HashMap<String, NodeCoord> getNeighborBoundaryInfo() {
		return neighborBoundaryInfo;
	}

	public void setNeighborBoundaryInfo(
			HashMap<String, NodeCoord> neighborBoundaryInfo) {
		this.neighborBoundaryInfo = neighborBoundaryInfo;
	}

	public HashMap<String, FileCoord> getFileCoordInfo() {
		return fileCoordInfo;
	}

	public void setFileCoordInfo(HashMap<String, FileCoord> fileCoordInfo) {
		this.fileCoordInfo = fileCoordInfo;
	}

	public NodeCoord getOwnCoord() {
		return ownCoord;
	}

	public void setOwnCoord(NodeCoord ownCoord) {
		this.ownCoord = ownCoord;
	}
	
	public FileCoord getOwnFileCoord() {
		return ownFileCoord;
	}

	public void setOwnFileCoord(FileCoord fileCoord) {
		this.ownFileCoord = fileCoord;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
