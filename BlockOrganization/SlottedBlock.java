import java.lang.reflect.Array;
import java.nio.*;

import javax.xml.crypto.dsig.SignedInfo;

/**
 * Slotted file block. This is a wrapper around a traditional Block that
 * adds the appropriate struture to it.
 *
 * @author Dave Musicant, with considerable inspiration from the UW-Madison
 * @author Conrado Peña Lemes modified this code starting on 02/05/2024 (D/M/Y)
 * Minibase project
 */

public class SlottedBlock
{
    public static class BlockFullException extends RuntimeException {};
    public static class BadSlotIdException extends RuntimeException {};
    public static class BadBlockIdException extends RuntimeException {};

    private static class SlotArrayOutOfBoundsException
        extends RuntimeException {};

    /**
     * Value to use for an invalid block id.
     */
    public static final int INVALID_BLOCK = -1;
    public static final int SIZE_OF_INT = 4;

    private byte[] data;
    private IntBuffer intBuffer;
    private int intBufferLength;

    //This is to set up the block
    private int blockId;
    private int nextB;
    private int prevB;

    //And this is to set up the records in the block
    private int nEntriesToWrite;//Amount of things we are going to write to the block
    private int endOfBlock;//end 
    private int recordsInBlock;//Amount of things that are already in the block
    private int lastRecordInBlock;
    private RID tempRecord;//meant to be used in deleting a record as a temporary record for the cases where the record we want to delete is not the first one 

    /**
     * Constructs a slotted block by wrapping around a block object already
     * provided.
     * @param block the block to be wrapped.
     */
    public SlottedBlock(Block block)
    {
        data = block.data;
        intBuffer = (ByteBuffer.wrap(data)).asIntBuffer();
        intBufferLength = data.length / SIZE_OF_INT;
    }

    /**
     * Initializes values in the block as necessary. This is separated out from
     * the constructor since it actually modifies the block at hand, where as
     * the constructor simply sets up the mechanism.
     */
    public void init()
    {
        //What this does is set the intBuffer so that the first 3 slots have the data of the block
        //In other words, the first 12 bytes of @param data are for referencing the block
        blockId = getBlockId();
        nextB = getNextBlockId();
        prevB = getPrevBlockId();
        intBuffer.put(0, blockId);
        intBuffer.put(1, prevB);
        intBuffer.put(2, nextB);
        setEndOfBlock(data.length-SIZE_OF_INT);
    }


    /**
     * Sets the block id.
     * @param blockId the new block id.
     */
    public void setBlockId(int blockId)
    {
        this.blockId = blockId;  
        intBuffer.put(0, blockId);
    }

    /**
     * Gets the block id.
     * @return the block id.
     */
    public int getBlockId()
    {
        return blockId;
    }

    /**
     * Sets the next block id.
     * @param blockId the next block id.
     */
    public void setNextBlockId(int blockId)
    {
        nextB = blockId;
        intBuffer.put(1, prevB);
    }

    /**
     * Gets the next block id.
     * @return the next block id.
     */
    public int getNextBlockId()
    {
        return nextB;
    }

    /**
     * Sets the previous block id.
     * @param blockId the previous block id.
     */
    public void setPrevBlockId(int blockId)
    {
        prevB = blockId;
        intBuffer.put(2, nextB);
    }

    /**
     * Gets the previous block id.
     * @return the previous block id.
     */
    public int getPrevBlockId()
    {
        return prevB;
    }

    public void setNEntriesToWrite(int nEntriesToWrite){
        this.nEntriesToWrite = nEntriesToWrite;
    }

    public int getNEntriesToWrite(){
        return nEntriesToWrite;
    }

    public void setEndOfBlock(int endOfBlock){
        this.endOfBlock = endOfBlock;
    }

    public int getEndOfBlock(){
        return endOfBlock;
    }

    public void setRecordsInBlock(int recordsInBlock){
        this.recordsInBlock = recordsInBlock;
    }

    public int getRecordsInBlock(){
        return recordsInBlock;
    }

    public void setLastRecordInBlock(int lastRecordInBlock){
        this.lastRecordInBlock = lastRecordInBlock;
    }

    public int getLastRecordInBlock(){
        return lastRecordInBlock;
    }

    public void setTempRecord(RID tempRecord){
        this.tempRecord = tempRecord;
    }
    public RID getTempRecord(){
        return tempRecord;
    }

    /**
     * Determines whether the record we are looking for is in the block
     * @param rid an RID
     * @return -1 if the record is not in the block, the slot number of the 
     * record if it exists in the block
     */
    public int findRecord(RID rid){
        recordsInBlock = getRecordsInBlock();
        boolean recordExists = false;
        int recordsChecked = 0;
        int recordToDelete;
        for (int i = 4; i < data.length/SIZE_OF_INT && recordsChecked <= recordsInBlock; i++){

            if (intBuffer.get(rid.slotNum) == intBuffer.get(i)){

                recordsChecked++;
                recordExists = true;
                break;
            }
            else{
                recordsChecked++;
                recordExists = false;
            }
        }
        if (recordExists == true){
            recordToDelete = intBuffer.get(rid.slotNum);
        }
        else {
            recordToDelete = -1;
        }
        return recordToDelete;
    }    

    /**
     * Determines how much space, in bytes, is actually available in the block,
     * which depends on whether or not a new slot in the slot array is
     * needed. If a new spot in the slot array is needed, then the amount of
     * available space has to take this into consideration. In other words, the
     * space you need for the addition to the slot array shouldn't be included
     * as part of the available space, because from the user's perspective, it
     * isn't available for adding data.
     * @return the amount of available space in bytes
     */
    public int getAvailableSpace()
    {
        int hasValue = 0;
        int freeSpace = 0;
        for (int i = 0; i < intBufferLength; i++) {
            if (intBuffer.get(i) != 0) {
                hasValue += 1;
            }
        }
        freeSpace = (intBufferLength - hasValue) * SIZE_OF_INT;
        return freeSpace;
    }
        
    /**
     * Dumps out to the screen the # of entries in the block, the location where
     * the free space starts, the slot array in a readable fashion, and the
     * actual contents of each record. (This method merely exists for debugging
     * and testing purposes.)
    */ 
    public void dumpBlock()
    {
        System.out.println("Availble space: " + getAvailableSpace());
        System.out.println("Free space starts at: " + getLastRecordInBlock());

        RID rid = firstRecord();
        while (rid != null)
        {
            byte[] dumpRecord = getRecord(rid); 
            StringBuilder sBuilder = new StringBuilder();
            for (int i =0; i < SIZE_OF_INT; i ++){
                sBuilder.append(Array.get(dumpRecord, i) + ", ");
            }
            System.out.println("");
            System.out.println("Contents of record: " + sBuilder);
            System.out.println("Retrieved record, RID " + rid.blockId + ", " + rid.slotNum);
            rid = nextRecord(rid);
        }
    }

    /**
     * Inserts a new record into the block.
     * @param record the record to be inserted. A copy of the data is
     * placed in the block.
     * @return the RID of the new record 
     * @throws BlockFullException if there is not enough room for the
     * record in the block.
    */
    public RID insertRecord(byte[] record)
    {
        //This puts the amount of entries in the header
        setNEntriesToWrite(record.length);
        int entriesToWrite = getNEntriesToWrite();
        intBuffer.put(3, entriesToWrite);
        int rInBlock = getRecordsInBlock();
        int endOfB = getEndOfBlock();
        int lastRecordInBlock;
        try {
            if (entriesToWrite < ((data.length/SIZE_OF_INT)-4)){
                if (rInBlock == 0) {
                    System.arraycopy(record, 0, data, endOfB, 1);
                    setEndOfBlock(endOfB - SIZE_OF_INT);
                    intBuffer.put((4), endOfB);
                    setRecordsInBlock(rInBlock+1);
                    rInBlock = getRecordsInBlock();
                    RID rid = new RID(intBuffer.get(0), 4);
                    if (rInBlock == entriesToWrite){
                        setLastRecordInBlock(4);
                    }
                    return rid;
                }
                else {
                    System.arraycopy(record, 0, data, endOfB, 1);
                    setEndOfBlock(endOfB - SIZE_OF_INT);
                    intBuffer.put((4+rInBlock), endOfB);
                    setRecordsInBlock(rInBlock+1);
                    lastRecordInBlock =  getRecordsInBlock();
                    if (lastRecordInBlock == entriesToWrite){
                        setLastRecordInBlock(4+rInBlock);
                    }
                    RID rid = new RID(intBuffer.get(0), (4+rInBlock));
                    return rid;
                    
                }
            }
            else{
                throw new BlockFullException();
            }
            
        } 
        catch (BlockFullException bFE) {
            System.err.println("The block is full");
            return null;
        }
    }

    /**
     * Deletes the record with the given RID from the block, compacting
     * the hole created. Compacting the hole, in turn, requires that
     * all the offsets (in the slot array) of all records after the
     * hole be adjusted by the size of the hole, because you are
     * moving these records to "fill" the hole. You should leave a
     * "hole" in the slot array for the slot which pointed to the
     * deleted record, if necessary, to make sure that the rids of the
     * remaining records do not change. The slot array should be
     * compacted only if the record corresponding to the last slot is
     * being deleted.
     * @param rid the RID to be deleted.
     * @return true if successful, false if the rid is actually not
     * found in the block.
    */
    public boolean deleteRecord(RID rid)
    {
        boolean recordDeleted = false;
        if (rid.slotNum <= 3){
            recordDeleted = false;
        }
        else{
            int count = 0;
            for (int i = 4; count < recordsInBlock;i++){
           
                count++;
                if (rid.slotNum == i){
                    //-1 is meant to signify an invalid slot number
                    intBuffer.put(i, -1);
                    recordDeleted = true;
                    break;
                }   
                
            }
        }
        if (recordDeleted == true){
            setRecordsInBlock(recordsInBlock-1);
            intBuffer.put(3, recordsInBlock);        
        }
        if (recordDeleted == true && rid.slotNum == lastRecordInBlock){
            int recordsChecked = 0;
            setLastRecordInBlock(lastRecordInBlock-1);
            RID record = firstRecord();
            for (int i = 4; recordsChecked < recordsInBlock; i++){
                intBuffer.put(i, intBuffer.get(record.slotNum));
                record = nextRecord(record);
                recordsChecked++;
            }
        }
        return recordDeleted;
    }

    /**
     * Returns RID of first record in block. Remember that some slots may be
     * empty, so you should skip over these.
     * @return the RID of the first record in the block. Returns null
     * if the block is empty.
     */
    public RID firstRecord()
    {
        if (empty() == true){
            return null;
        }
        else {
            int firstRecordPosition = 0;
            for (int i = 0; i < data.length/SIZE_OF_INT && intBuffer.get(i) != data.length/SIZE_OF_INT; i++){
                if (intBuffer.get(i) == data.length-SIZE_OF_INT){
                    
                    firstRecordPosition = i;
                   
                    break;
                }
            }
            //fRID is short for first RID
            RID fRID = new RID(intBuffer.get(0), firstRecordPosition);
            return fRID;
        }
    }

    /**
     * Returns RID of next record in the block, where "next in the block" means
     * "next in the slot array after the rid passed in." Remember that some
     * slots may be empty, so you should skip over these.
     * @param curRid an RID
     * @return the RID immediately following curRid. Returns null if
     * curRID is the last record in the block.
     * @throws BadBlockIdException if the block id within curRid is
     * invalid
     * @throws BadSlotIdException if the slot id within curRid is invalid
    */
    public RID nextRecord(RID curRid)
    {
        try {
            if (curRid.blockId != blockId){
                throw new BadBlockIdException();
            }
            else if (curRid.slotNum < 4 || curRid.slotNum > (data.length/SIZE_OF_INT)){
                throw new BadSlotIdException();
            }
            else {
                int cSlotNum = curRid.slotNum;
                int rInBlock = getRecordsInBlock();
                if (cSlotNum+1 <= data.length/SIZE_OF_INT){
                    if (intBuffer.get(cSlotNum+1) < rInBlock){
                        return null;
                    }
                    RID nextRID = new RID(intBuffer.get(0), cSlotNum+1);
                    return nextRID;
                }
            }
        } 
        catch (BadBlockIdException bBIE) {
            System.err.println("The block ID provided is not valid");
            return null;
        } catch (BadSlotIdException bSIE) {
            System.err.println("The slot ID provided is not valid");
            return null;
        }
        return null;
    }

    /**
     * Returns the record associated with an RID.
     * @param rid the rid of interest
     * @return a byte array containing a copy of the record. The array
     * has precisely the length of the record (there is no padded space).
     * @throws BadBlockIdException if the block id within curRid is
     * invalid
     * @throws BadSlotIdException if the slot id within curRid is invalid
    */
    public byte[] getRecord(RID rid)
    {
        try {
            if (rid.blockId != blockId){
                throw new BadBlockIdException();
            }
            else if (rid.slotNum < 4 || rid.slotNum > (data.length/SIZE_OF_INT)){
                throw new BadSlotIdException();
            }
            else {
                findRecord(rid);
                byte[] record = {-1, -1, -1, -1};
                System.arraycopy(data, intBuffer.get(rid.slotNum), record, 0, SIZE_OF_INT);
                return record;
            }
        } 
        
        catch (BadBlockIdException bBIE) {
            System.err.println("Invalid Block ID");
            return null;
        }
        catch (BadSlotIdException bSIE){
            System.err.println("Invalid Slot ID");
            return null;
        }
    }

    /**
     * Whether or not the block is empty.
     * @return true if the block is empty, false otherwise.
     */
    public boolean empty()
    {
        if (getAvailableSpace() == data.length) {
          return true;
        }
        else{
           return false;
        }
    }
}
