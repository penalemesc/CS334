import java.nio.*;

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
    /*At the time im starting this (17:00 02/05/2024) im treating it
    like a tree because that is the data structure that was the most fresh on my mind
    though I think I could try and implement the blocks like a linked list instead of tree
    
    Update (21:35 same day): im thinking now this is probably too simple of a solution and probably
    will not work, however the make file that we got to make it run doesnt work on this machine so idk what to do now!
    
    Yo creo que como arbol va a ser un poco más facíl porque ya de por si tengo una derecha que
    puedo tratar como adelante y una izquierda que puedo tratar como atras
    entonces en esta idea de implementacion de arbol, la derecha seria el parametro nextB
    y la izquierda seria prevB*/
    private int blockId;
    private int nextB;
    private int prevB;
    //static Block root;

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
      int blockId = getBlockId();
      int nextBlock = getNextBlockId();
      int prevBlock = getPrevBlockId();
      intBuffer.put(0, blockId);
      intBuffer.put(1, prevBlock);
      intBuffer.put(2, nextBlock);
    }


    /**
     * Sets the block id.
     * @param blockId the new block id.
     */
    public void setBlockId(int blockId)
    {
        //Tried doing it by creating a block called initialBlock, but that didnt work
        //static SlottedBlock initialBlock;
        //initialBlock = blockId;
        this.blockId = blockId;  
    }

    /**
     * Gets the block id.
     * @return the block id.
     */
    public int getBlockId()
    {
        //I need to return blockId that much im certain off, however I need to first figure out how to set 
        //said block id
        //blockId = initialBlock.blockId();
        return blockId;
    }

    /**
     * Sets the next block id.
     * @param blockId the next block id.
     */
    public void setNextBlockId(int blockId)
    {
        nextB = blockId;
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
    }

    /**
     * Gets the previous block id.
     * @return the previous block id.
     */
    public int getPrevBlockId()
    {
        return prevB;
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
        //Do we need to create a new block if there is no space in the slot?
        int hasValue = 0;
        int freeSpace = 0;
        for (int i = 0; i < intBufferLength; i++) {
            //este if funciona si lo checkeo que este vacio primero pero es mejor asi porque hay mas espacios libres por ahora
            if (intBuffer.get(i) != 0) {
                hasValue += 1;
            }
        }
        freeSpace = (intBufferLength - hasValue) * SIZE_OF_INT;
        // for (int i = 0; i < espacioLibre; i++){
        //     freeSpace[i] = espacioLibre;
        // }
        //freeSpace = Byte.parseByte(String.valueOf(espacioLibre));
        //System.out.println("Los espacios ocupados son: " + hayValor);
        //System.out.println("Los espacios vacios son: " + estaVacio);
        //System.out.println("El espacio disponible es: " + espacioLibre);
        System.out.println("En bytes el espacio libre es: " + freeSpace);
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
        //Idea de lo que hay que hacer es un intbuffer.put() como 3 veces, donde en el primero va la cantidad de records
        //en la segunda va donde esta el record que pusimos y el tercero va el largo del record 
        //Hay que repetir el segundo y el primero por cada record que hacemos 
        //y para poner el record en si en el array, hay que empezar desde el final para atras o sea 
        //el primer record va a ir del byte 1020 al 1023 (o el slot 255 si lo tratamos a cada slot como 4 bytes)

        //The main question that I have is about the extra space how would we define that in here
        //More importantly is what is needed to insert the record into the array and in that case are we inserting 
        //the record into the intbuffer or into data?
        
        //System.arraycopy(record, 0, data, 0, SIZE_OF_INT);
        //RID is record ID
        //RID rid;
        //return rid;
        return null;
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
        //I dont understand what the method is supposed to do then, is this how we create the free space in the 
        //block then? By deleting records?
        //No sirve por todo lo que dice arriba
        // idea basica de lo que hay que hacer intBuffer[rid] = 0
        // if (intBuffer.get(rid) == rid) {
        //     intBuffer.put(0);
        //     return true;
        // }
        // else {
        //     return false;
        // }
        return false;
    }

    /**
     * Returns RID of first record in block. Remember that some slots may be
     * empty, so you should skip over these.
     * @return the RID of the first record in the block. Returns null
     * if the block is empty.
     */
    public RID firstRecord()
    {
        return null;
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
        return null;
    }

    /**
     * Whether or not the block is empty.
     * @return true if the block is empty, false otherwise.
     */
    public boolean empty()
    {
        //The basic idea to what I should do 
        if (getAvailableSpace() == 1024) {
          return true;
        }
        else{
           return false;
        }

    }
}
