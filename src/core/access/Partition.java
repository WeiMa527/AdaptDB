package core.access;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import core.utils.IOUtils;


public class Partition implements Cloneable{
	
	public static enum State {ORIG,NEW,MODIFIED};
	State state;
	
	protected String path;
	protected byte[] bytes;
	protected int offset;	
	protected int recordCount;
	
	protected int partitionId;
	
	//public int[] lineage;
	
	/**
	 * Create an existing  partition object.
	 * @param pathAndPartitionId
	 */
	public Partition(String pathAndPartitionId){
		this(FilenameUtils.getPath(pathAndPartitionId), 
				Integer.parseInt(FilenameUtils.getBaseName(pathAndPartitionId)));		
	}
	
	public Partition(String path, int partitionId){
		this.path = path;
		this.partitionId = partitionId;
		this.state = State.ORIG;
		this.offset = 0;
		this.recordCount = 0;
	}
	
	public Partition clone() {
		Partition p = new Partition(path, partitionId);
		p.bytes = new byte[bytes.length];
		p.state = State.NEW;
        return p;
    }
	
	
	/**
	 * Create a new partition
	 * 
	 * @param childId
	 * @return
	 */
//	public Partition createChild(int childId){
//		Partition p = new Partition(path+"_"+childId);
//		p.bytes = new byte[bytes.length];	// child cannot have more bytes than parent
//		//p.bytes = new byte[8*1024*1024];	// child cannot have more than 8m bytes
//		p.state = State.NEW;
//		return p;
//	}
//	
//	public void setAsParent(Partition p){
//		for(int i=0;i<p.lineage.length;i++)
//			lineage[i] = p.lineage[i];
//		this.offset = 0;
//	}
	
	public void setPathAndPartitionId(String pathAndPartitionId){
		this.path = FilenameUtils.getPath(pathAndPartitionId); 
		this.partitionId = Integer.parseInt(FilenameUtils.getBaseName(pathAndPartitionId));
//		this.path = pathAndPartitionId;
//		this.lineage = ArrayUtils.splitWithException("_", FilenameUtils.getBaseName(pathAndPartitionId));
		this.offset = 0;
	}
	
	public void setPartitionId(int partitionId){
		this.partitionId = partitionId;
	}
	
	public int getPartitionId(){
		return this.partitionId;
	}
	
	
	
	
	public boolean load(){
		if(path==null || path.equals(""))
			return false;
		bytes = IOUtils.readByteArray(path);
		offset = bytes.length;
		return true;	// load the physical block for this partition 
	}
	
	public void write(byte[] source, int offset, int length){
		if(offset+length > source.length || this.offset+length>bytes.length)
			System.out.println("somthing is wrong!");
		System.arraycopy(source, offset, bytes, this.offset, length);
		this.offset += length;
		bytes[this.offset] = '\n';
		this.offset++;
		this.recordCount++;
	}
	
	public void store(boolean append){
		//String storePath = FilenameUtils.getFullPath(path) + ArrayUtils.join("_", lineage);
		String storePath = path + "/" + partitionId;
		IOUtils.writeByteArray(storePath, bytes, 0, offset, append);
	}
	
	public void drop(){
		FileUtils.deleteQuietly(new File(path));
	}
	
	public byte[] getBytes(){
		if(bytes==null)
			load();
		return bytes;
	}
	
	public int getSize(){
		return offset;
	}
	
	public boolean equals(Object p){
		return ((Partition)p).path.equals(path) && ((Partition)p).partitionId==partitionId;
	}
	
	public int hashCode(){
		return (path+partitionId).hashCode();
	}
	
	public int getRecordCount(){
		return this.recordCount;	// returns the number of records written to this partition
	}
}
