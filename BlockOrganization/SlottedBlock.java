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
    //Not used: private int recordsChecked;//records that we have been through for the function nextRID()

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
        // System.out.println(intBuffer.get(2));
        //System.out.println("test " +intBuffer.get(2));
        setEndOfBlock(data.length-SIZE_OF_INT);
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
        intBuffer.put(0, blockId);
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
            //System.out.println("intbufferget: " + intBuffer.get(i));
            //System.out.println("slotnum: " + rid.slotNum);

            if (intBuffer.get(rid.slotNum) == intBuffer.get(i)){
                //System.out.println("RECORD FOUND");
                //System.out.println("Records checked: " + recordsChecked);
                //System.out.println("");
                recordsChecked++;
                recordExists = true;
                break;
            }
            else{
                //System.out.println("Record not found");
                //System.out.println("Records checked: " + recordsChecked);
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
        //System.out.println("Record exists? " +recordExists);
        return recordToDelete;
    }    

    //Didnt need these
    // public void setTempRecord(RID tempRecord){
    //     this.tempRecord = tempRecord;
    // }
    // public RID getTempRecord(){
    //     return tempRecord;
    // }
    // private void setRecordsChecked(int recordsChecked){
    //     this.recordsChecked = recordsChecked;
    // }
    // public int getRecodsChecked(){
    //     return recordsChecked;
    // }

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
        //Como hago para llegar al final? Porque el ultimo no lo puedo leer si lo hago con un <=
        for (int i = 0; i < intBufferLength; i++) {
            //este if funciona si lo checkeo que este vacio primero pero es mejor asi porque hay mas espacios libres por ahora
            if (intBuffer.get(i) != 0) {
                hasValue += 1;
            }
        }
        freeSpace = (intBufferLength - hasValue) * SIZE_OF_INT;
        // if (intBuffer.get() != 0) {
        //         System.out.println("test " + intBuffer.get(i));
        //         hasValue += 1;
        //     }
        // for (int i = 0; i < data.length; i++){
        //     if (intBuffer.get() != 0) {
        //         System.out.println("test " + intBuffer.get(i));
        //         hasValue += 1;
        //     }
        // }
        //freeSpace = Byte.parseByte(String.valueOf(espacioLibre));
        //System.out.println("Los espacios ocupados son: " + hayValor);
        //System.out.println("Los espacios vacios son: " + estaVacio);
        //System.out.println("El espacio disponible es: " + espacioLibre);
        //System.out.println("En bytes el espacio libre es: " + freeSpace);
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
        //the record into the intbuffer or into data? SOLVED
        
        //Hay que hacer un for que revise de atras para adelante

        //This puts the amount of entries in the header
        setNEntriesToWrite(record.length);
        int entriesToWrite = getNEntriesToWrite();
        intBuffer.put(3, entriesToWrite);
        int rInBlock = getRecordsInBlock();
        int endOfB = getEndOfBlock();
        int lastRecordInBlock;
        //System.out.println("entries to write: " + entriesToWrite);
        //Test to see if it actually puts it in: System.out.println("Entries " + intBuffer.get(0));
        try {
            if (entriesToWrite < ((data.length/SIZE_OF_INT)-4)){
                if (rInBlock == 0) {
                    System.arraycopy(record, 0, data, endOfB, 1);
                    setEndOfBlock(endOfB - SIZE_OF_INT);
                    //El largo del record no importa porque el header de donde esta tiene el lugar de donde esta guardado la info del siguiente 
                    //Los records se van guardando de manera sequencial desde el fin del bloque hacia el inicio
                    intBuffer.put((4), endOfB);
                    //System.out.println("Test of iB: " + intBuffer.get(1+i)); 
                    setRecordsInBlock(rInBlock+1);
                    rInBlock = getRecordsInBlock();
                    //RID rid = new RID(intBuffer.get(0), intBuffer.get(4));
                    RID rid = new RID(intBuffer.get(0), 4);
                    if (rInBlock == entriesToWrite){
                        //System.out.println("Se mete al if del primero" + rInBlock);
                        //setLastRecordInBlock(intBuffer.get(4));
                        setLastRecordInBlock(4);
                    }
                    return rid;
                }
                else {
                    System.arraycopy(record, 0, data, endOfB, 1);
                    setEndOfBlock(endOfB - SIZE_OF_INT);
                    //El largo del record no importa porque el header de donde esta tiene el lugar de donde esta guardado la info del siguiente 
                    //Los records se van guardando de manera sequencial desde el fin del bloque hacia el inicio
                    intBuffer.put((4+rInBlock), endOfB);
                    //System.out.println("Test of iB: " + intBuffer.get(1+i)); 
                    
                    setRecordsInBlock(rInBlock+1);
                    lastRecordInBlock =  getRecordsInBlock();

                    if (lastRecordInBlock == entriesToWrite){
                        // setLastRecordInBlock(intBuffer.get(4+rInBlock));
                        setLastRecordInBlock(4+rInBlock);
                        //System.out.println("Last record is: " + getLastRecordInBlock());
                    }
                    //RID rid = new RID(intBuffer.get(0), intBuffer.get(4+rInBlock));
                    RID rid = new RID(intBuffer.get(0), (4+rInBlock));
                    //System.out.println("Espacio libre: " + getAvailableSpace() + " Intbufferlength: " + intBufferLength);
                    //System.out.println("Records in block: " + rInBlock);
                    return rid;
                    
                }
            }
            else{
                //System.out.println("Sorry the block is full");
                //throw BlockFullException e;
                throw new BlockFullException();
            }
            
        } 
        catch (BlockFullException bFE) {
            //System.out.println("Espacio libre: " + getAvailableSpace() + " Intbufferlength: " + intBufferLength);
            System.err.println("The block is full");
            return null;
        }
        /*Here are the many tests I had to do in order to get the code to work
        /aca el problema es que el arraycopy no esta metiendo nada al array de data
        //System.out.println("Esto es un test de poner el primer record en el array, al ya saber que vamos a empezar en el byte 1023");
        //System.arraycopy(record, 0, data, 1020, 4);
        //algo en el array copy no copia como tiene que copiar porque lo siguiente anda como tendria que hacerlo
        // data[1020] = 20;
        // System.out.println(data[1020]);
        // blockId = getBlockId();
        // nextB = getNextBlockId();
        // prevB = getPrevBlockId();
        // intBuffer.put(0, blockId);
        // intBuffer.put(1, prevB);
        // intBuffer.put(2, nextB);
        // intBuffer.get(0);
        // intBuffer.get(1);
        // intBuffer.get(2);
        
        //System.out.println("Data in first position: " + data[1020] + " Data in second position: " + data[1021] + " third "+data[1022] + " fourth: " +data[1023]);
        //la idea de esto es medio obvia, pero si no hay data entonces se puede usar como lugar para poner datos
        //creo que con un while es mejor porque si canBeLocation es false entonces entra al loop si pasa a true
        //sale del loop
        //RID rid = 1;

            // int p = 0;
        // int l = 1;
        // //i++ has to always be here otherwise it dont iterate
        // for (int i = 0; i <= (intBuffer.get(0)); i++){
        //     int endOfB = getEndOfBlock();
            
        //     System.arraycopy(record, i, data, endOfB, 1);
        //     setEndOfBlock(endOfB - SIZE_OF_INT);
        //     //No se bien que tengo que meter pero eso es lo que hay que hacer
        //     intBuffer.put((p+i), endOfB);
        
        //     System.err.println(p+1);
        //     p++;
        //     intBuffer.put((1+l), SIZE_OF_INT);
        //     System.err.println(1+l);
        //     l++;
            
        //     //test
        //     System.out.println("Test of iB: " + intBuffer.get(i));
        // }
      

        TEST DE FOR LOOP PARA RECORDS:::
        for (int i = 0; i < (intBuffer.get(0)); i++){
            int endOfB = getEndOfBlock();
            
            System.arraycopy(record, i, data, endOfB, 1);
            setEndOfBlock(endOfB - SIZE_OF_INT);
            //El largo del record no importa porque el header de donde esta tiene el lugar de donde esta guardado la info del siguiente 
            //Los records se van guardando de manera sequencial desde el fin del bloque hacia el inicio
            intBuffer.put((1+i), endOfB);
            //System.out.println("Test of iB: " + intBuffer.get(1+i)); 
        }









        // int Irid = 1;
        // intBuffer.put(255, Irid);
        // System.out.println(intBuffer.get(255));
        
        //System.arraycopy(record, Irid, record, Irid, Irid);
        boolean canBeHeaderLocation = false;
        int hLocationInIB;
        while (canBeHeaderLocation == false){
             
             for (int i = 0; i < data.length; i++){
                if (data[i] == 0){
                    canBeHeaderLocation = true;
                    hLocationInIB = data[i];
                    break;
                }
            }
            // if (canBeHeaderLocation == true) {
            //         intBuffer.put(hLocationInIB, data[i]);
            //         System.arraycopy(record, Irid, record, hLocationInIB, i);
            //     }

            
              //Aca necesito otro que apunte a donde esta guardado el registro, entonces primero tengo 
        //que guardar el registro en el array
        //intBuffer.put(hLocationInIB, locationInI);

        //System.arraycopy(record, 0, data, 0, SIZE_OF_INT);
        //RID is record ID
        //RID rid;
        //return rid;
        }*/
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
        // for (RID recordID : intBuffer.get(rid)){
        //     if (recordID == rid){
        //         return true;
        //     }
        //     else {
        //         return false;
        //     }
        // }
        //recordsInBlock = getRecordsInBlock();
        //SlottedBlock sp = new SlottedBlock(new Block());
        //
        //
        //Esto borra todo del bloque, o sea que tiene que ser sin un for 
        // for (int i = 0; i < data.length/SIZE_OF_INT && intBuffer.get(i) != data.length/SIZE_OF_INT; i++){
        //     //System.out.println("Se mete al for!");
        //     RID firstRecord = firstRecord();
        //     if (firstRecord.blockId == rid.blockId && firstRecord.slotNum == rid.slotNum){
        //         //System.out.println("Se mete al if!");
        //         intBuffer.put(firstRecord.slotNum, 0);
        //         //I dont think I need to change anything in the array itself 
        //         setRecordsInBlock(recordsInBlock-1);
        //         System.out.println("Records left in block "+recordsInBlock);
        //         return true;
        //     }
        //     //Puede ser que no necesite esto --SI LO NECESITAMOS PERO QUE ANDE BIEN NECESITAMOS
        //     else if (firstRecord.blockId != rid.blockId && firstRecord.slotNum != rid.slotNum){ 
        //         System.out.println("Se mete en el else if: ");
        //         System.out.println("");
        //         firstRecord = sp.nextRecord(rid);
        //         if (firstRecord.slotNum == rid.slotNum){
        //             System.out.println("Se mete en el if del else if:");
        //             //System.out.println("Se mete al if!");
        //             intBuffer.put(firstRecord.slotNum, 0);
        //             //I dont think I need to change anything in the array itself 
        //             setRecordsInBlock(recordsInBlock-1);
        //             System.out.println("Records left in block "+recordsInBlock);
        //             return true;
        //         }
        //     }
        //     else{
        //         System.out.println("Records left in block (else version): " + recordsInBlock);
        //         return false;
        //     }
        // }
        //}
        //
        //
        //
        //El if tiene que ser con intBuffer.get(slotId) donde se supone que eso tiene que devolver en donde esta guardado el record
        //entonces si eso es igual a getLastRecordInBlock() entonces ahi hago lo del pedo psicologico de mover todo
        // RID firstRecord = firstRecord();
        //     if (firstRecord.blockId == rid.blockId && firstRecord.slotNum == rid.slotNum){
        //         System.out.println("Primer record: " + firstRecord.slotNum);
        //         System.out.println("");
        //         //System.out.println("Se mete al if!");
        //         intBuffer.put(firstRecord.slotNum, 0);
        //         //I dont think I need to change anything in the array itself 
        //         setRecordsInBlock(recordsInBlock-1);
        //         //System.out.println("Se metio al primero!");
        //         System.out.println("Records left in block "+recordsInBlock);
        //         return true;
        //     }
        //     //Puede ser que no necesite esto --SI LO NECESITAMOS PERO QUE ANDE BIEN NECESITAMOS
        //     else if (firstRecord.slotNum != rid.slotNum){ 
        //         System.out.println("Se mete en el else if: ");        
        //         firstRecord = sp.nextRecord(rid);
        //         System.out.println("Next record to kill: " + firstRecord.slotNum);
        //         System.out.println("");
        //         //Lo de abajo no interesa
        //         // if (firstRecord.slotNum == rid.slotNum){
        //         //     System.out.println("Se mete en el if del else if:");
        //         //     //System.out.println("Se mete al if!");
        //         //     intBuffer.put(firstRecord.slotNum, 0);
        //         //     //I dont think I need to change anything in the array itself 
        //         //     setRecordsInBlock(recordsInBlock-1);
        //         //     System.out.println("Records left in block "+recordsInBlock);
        //         //     return true;
        //         // }
        //     }
        //     else{
        //         System.out.println("Records left in block (else version): " + recordsInBlock);
        //         return false;
        //     }
        //
        //
        // recordsInBlock = getRecordsInBlock();
        // RID firstRecord = firstRecord();
        // try {
        //     setTempRecord(nextRecord(rid));
        // } catch (Exception e) {
        //     setTempRecord(nextRecord(0));
        //     System.err.println();
        // }
        // tempRecord = getTempRecord();
        // if (firstRecord.blockId == rid.blockId && firstRecord.slotNum == rid.slotNum){
        //     //System.out.println("Primer record: " + firstRecord.slotNum);
        //     //System.out.println("");
        //     //System.out.println("Se mete al if!");
        //     intBuffer.put(firstRecord.slotNum, 0);
        //     //I dont think I need to change anything in the array itself 
        //     setRecordsInBlock(recordsInBlock-1);
        //     //System.out.println("Se metio al primero!");
        //     //System.out.println("Records left in block "+ getRecordsInBlock());
        //     //setTempRecord(nextRecord(rid));
        //     return true;
        // }
        // else if (tempRecord.slotNum != rid.slotNum){ 
        //     //System.out.println("Se mete en el else if: ");   
        //     setTempRecord(nextRecord(tempRecord));
        //     setRecordsInBlock(recordsInBlock-1);
        //     // System.out.println("Next record to kill: " + tempRecord.slotNum);
        //     // System.out.println("Records left: " + getRecordsInBlock());            
        // }
        // // else if (tempRecord.slotNum == rid.slotNum){ 
        // //     //System.out.println("Se mete en el else if: ");   
        // //     setTempRecord(nextRecord(tempRecord));
        // //     setRecordsInBlock(recordsInBlock-1);
        // //     intBuffer.put(tempRecord.slotNum, 0);
        // //     return true;
        // //     // System.out.println("Next record to kill: " + tempRecord.slotNum);
        // //     // System.out.println("Records left: " + getRecordsInBlock());            
        // // }
        // else{
        //     //System.out.println("Records left in block (else version): " + getRecordsInBlock());
        //     return false;
        // } 
        //recordsInBlock = getRecordsInBlock();
        boolean recordDeleted = false;
        //findRecord(rid) == intBuffer.get(i)
        //&& recordDeleted != true
        //rid.slotNum != 0
        //lastRecordInBlock = getLastRecordInBlock();
        if (rid.slotNum <= 3){
                //System.out.println("Cant delete the amount of records in block");
                recordDeleted = false;
                //break;
        }
        else{
            //System.out.println(data.length/SIZE_OF_INT);
            //int i = 0; i < data.length/SIZE_OF_INT;i++
            int count = 0;
            for (int i = 4; count < recordsInBlock;i++){
            //This is a check so that we dont accidently delete the amount of records in the block
            // if (rid.slotNum == 3){
            //     //System.out.println("Cant delete the amount of records in block");
            //     recordDeleted = false;
            //     //break;
            // }
                count++;
                if (rid.slotNum == i){
                    //System.out.println("SlotNum " + rid.slotNum);
                    // System.out.println("i " + intBuffer.get(i));
                    //System.out.println("getslotnum " + intBuffer.get(rid.slotNum));
                    //System.out.println("Se mete en " + intBuffer.get(i));
                    //intBuffer.put(intBuffer.get(rid.slotNum), 0);
                    //System.out.println(intBuffer.get(rid.slotNum));
                    intBuffer.put(i, -1);
                    //System.out.println("ibp " + intBuffer.get(rid.slotNum));
                    //recordsInBlock = getRecordsInBlock();
                    //System.out.println("get de 4 " + intBuffer.get(4));
                    recordDeleted = true;
                    break;
                }   
                // else {
                //     //System.out.println("Else");
                //     recordDeleted = false;
                //     //break;
                // }
            }
        }
        //System.out.println("lastRec " + lastRecordInBlock);
        // for (int i = 0; i < data.length/SIZE_OF_INT;i++){
        //     //This is a check so that we dont accidently delete the amount of records in the block
        //     // if (rid.slotNum == 3){
        //     //     //System.out.println("Cant delete the amount of records in block");
        //     //     recordDeleted = false;
        //     //     //break;
        //     // }
        //     if (findRecord(rid) != intBuffer.get(i)){
        //         // System.out.println("SlotNum " + rid.slotNum);
        //         // System.out.println("i " + intBuffer.get(i));
        //         //System.out.println("getslotnum " + intBuffer.get(rid.slotNum));
        //         //System.out.println("Se mete en " + intBuffer.get(i));
        //         //intBuffer.put(intBuffer.get(rid.slotNum), 0);
        //         //System.out.println(intBuffer.get(rid.slotNum));
        //         intBuffer.put(i);
        //         //System.out.println("ibp " + intBuffer.get(rid.slotNum));
        //         //recordsInBlock = getRecordsInBlock();
        //         //System.out.println("get de 4 " + intBuffer.get(4));
        //         recordDeleted = true;
        //         break;
        //     }   
        //     else {
        //         //System.out.println("Else");
        //         recordDeleted = false;
        //         break;
        //     }
        // }
        // int i = 0;
        // while (i < data.length/SIZE_OF_INT){
        //     if (findRecord(rid) == intBuffer.get(i)){
        //         System.out.println("Find records " + findRecord(rid));
        //         System.out.println("SlotNum " + rid.slotNum);
        //         System.out.println("i " + i);
        //         //System.out.println("getslotnum " + intBuffer.get(rid.slotNum));
        //         //System.out.println("Se mete en " + intBuffer.get(i));
        //         //intBuffer.put(intBuffer.get(rid.slotNum), 0);
        //         //System.out.println(intBuffer.get(rid.slotNum));
        //         intBuffer.put(rid.slotNum, 0);
        //         //System.out.println("ibp " + intBuffer.get(rid.slotNum));        
        //         //recordsInBlock = getRecordsInBlock();        
        //         //System.out.println("get de 4 " + intBuffer.get(4));
        //         recordDeleted = true;
        //         break;
        //     }
        //     i++;
        // }
        
        if (recordDeleted == true){
                setRecordsInBlock(recordsInBlock-1);
                intBuffer.put(3, recordsInBlock);
                //System.out.println("records in block: " + intBuffer.get(3));
        }
        if (recordDeleted == true && rid.slotNum == lastRecordInBlock){
            int recordsChecked = 0;
            setLastRecordInBlock(lastRecordInBlock-1);
            RID record = firstRecord();
            //int count = 0;
            for (int i = 4; recordsChecked < recordsInBlock; i++){
                // System.out.println("ibg: " + intBuffer.get(i));
                // System.out.println("");
                intBuffer.put(i, intBuffer.get(record.slotNum));
                record = nextRecord(record);
                recordsChecked++;
                // System.out.println("slotnum: " + i);
                // System.out.println("recordsChecked: " + recordsChecked);
                // System.out.println("ibg2: " + intBuffer.get(i));
                // System.out.println("");
                //count++;
                //System.out.println("Test of how many times it " + count);
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
                //System.out.println("IntBuffer.get(i): " + intBuffer.get(i));
                //System.out.println("Se metio en el loop");
                //--SOLUCIONADO: El if esta mal tengo que ver bien donde estoy igualando las cosas --SOLUCIONADO
                //Antes habia un - aca en vez de un /      
                //System.out.println(data.length/SIZE_OF_INT);
                if (intBuffer.get(i) == data.length-SIZE_OF_INT){
                    //System.out.println("Se metio en el if");
                    firstRecordPosition = i;
                    //System.out.println("Primera posicion: " + firstRecordPosition);
                    break;
                }
            }
            //fRID is short for first RID
            //RID fRID = new RID(intBuffer.get(0), intBuffer.get(firstRecordPosition));
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
            //System.out.println("dataLength: " + data.length);
            //System.out.println("SlotNum: " +curRid.slotNum);
            //System.out.println("NextSlotNum: " +intBuffer.get(curRid.slotNum));
            //int cSlotNum = intBuffer.get(curRid.slotNum);

            int cSlotNum = curRid.slotNum;
            //int nextSlotNum = cSlotNum+1;
            //int recordsChecked = getRecodsChecked();
//
            // if (intBuffer.get(cSlotNum) <= data.length-SIZE_OF_INT){
            //     RID nextRID = new RID(intBuffer.get(0), intBuffer.get(cSlotNum));
            //     return nextRID;
            // }

            int rInBlock = getRecordsInBlock();
            
            // if (cSlotNum+1 <= data.length/SIZE_OF_INT && cSlotNum <= rInBlock){
            //     RID nextRID = new RID(intBuffer.get(0), cSlotNum+1);
            //     // System.out.println("");
            //     // System.out.println("Next id: " + nextRID.slotNum);
            //
            //     return nextRID;
            // }
            //
            //&& recordsChecked <= rInBlock
            // if (cSlotNum+1 <= data.length/SIZE_OF_INT && cSlotNum+1 <= rInBlock){
            //     RID nextRID = new RID(intBuffer.get(0), cSlotNum+1);   
            //     System.out.println("recordsChecked: " + recordsChecked);
            //     System.out.println("cSLotNum: " + nextSlotNum);
            //     // System.out.println("Records checked: " + recordsChecked);
            //     // setRecordsChecked(recordsChecked+1);
            //     // System.out.println("");
            //     // System.out.println("Next id: " + nextRID.slotNum); 
            //     return nextRID;
            // }
            if (cSlotNum+1 <= data.length/SIZE_OF_INT){
                //Este if no me gusta mucho porque me parece que me trae problemas con otras funciones pero bueno
                //Lo voy a descubrir en la parte 3 del ejercicio, por ahora sirve
                //I think I accidently did the null part --I did
                if (intBuffer.get(cSlotNum+1) < rInBlock){
                    return null;
                    //return nextRID;
                }
                // else if (cSlotNum+1 > rInBlock){
                //     return null;
                // }
                // System.out.println("recordsChecked: " + recordsChecked);
                // System.out.println("cSLotNum: " + nextSlotNum);
                // System.out.println("Records checked: " + recordsChecked);
                // setRecordsChecked(recordsChecked+1);
                // System.out.println("");
                // System.out.println("Next id: " + nextRID.slotNum);
                RID nextRID = new RID(intBuffer.get(0), cSlotNum+1);
                return nextRID;
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
            findRecord(rid);
            byte[] record = {-1, -1, -1, -1};
            //tengo que crear un array que sea de largo size of int, que tenga los datos que se guardaron en rid.slotnum
            //data.clone();
            System.arraycopy(data, intBuffer.get(rid.slotNum), record, 0, SIZE_OF_INT);
            //System.out.println(record.toString());
            return record;
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
        //The basic idea to what I should do 
        if (getAvailableSpace() == data.length) {
          return true;
        }
        else{
           return false;
        }

    }
}
