import java.io.*;
import java.util.*;

public class SPTester
{
    public static interface Testable
    {
        void test() throws Exception;
    }
    
    public static class TestFailedException extends RuntimeException
    {
        public TestFailedException(String explanation)
        {
            super(explanation);
        }
    }

    public static class Test1 implements Testable
    {
        public void test() throws Exception
        {
            SlottedBlock sp = new SlottedBlock(new Block());
            sp.init();
            
            System.out.println("--- Test 1: Block Initialization Checks ---");
            sp.setBlockId(7);
            sp.setNextBlockId(8);
            sp.setPrevBlockId(SlottedBlock.INVALID_BLOCK);
            
            System.out.println
                ("Current Block No.: " + sp.getBlockId() + ", " +
                 "Next Block Id: " + sp.getNextBlockId() + ", " +
                 "Prev Block Id: " + sp.getPrevBlockId() + ", " +
                 "Available Space: " + sp.getAvailableSpace());
        
            if (!sp.empty())
                throw new TestFailedException("Block should be empty.");

            System.out.println("Block Empty as expected.");
            sp.dumpBlock();
        }
    }


    public static class Test2 implements Testable
    {
        public void test() throws Exception
        {
            int buffSize = 20;
            int limit = 20;
            byte[] tmpBuf = new byte[buffSize];

            SlottedBlock sp = new SlottedBlock(new Block());
            sp.init();
            sp.setBlockId(7);
            sp.setNextBlockId(8);
            sp.setPrevBlockId(SlottedBlock.INVALID_BLOCK);

            System.out.println("--- Test 2: Insert and traversal of " +
                               "records ---");
            for (int i=0; i < limit; i++)
            {
                RID rid = sp.insertRecord(tmpBuf);
                System.out.println("Inserted record, RID " + rid.blockId +
                                   ", " + rid.slotNum);
                rid = sp.nextRecord(rid);
            }

            if (sp.empty())
                throw new TestFailedException("The block cannot be empty");
            
            RID rid = sp.firstRecord();
            while (rid != null)
            {
                tmpBuf = sp.getRecord(rid); 
                System.out.println("Retrieved record, RID " + rid.blockId +
                                   ", " + rid.slotNum);
                rid = sp.nextRecord(rid);
            }
        }
    }

     public static class Test3 implements Testable
    {
        public void test() throws Exception
        {
            int buffSize = 20;
            int limit = 20;
            byte[] tmpBuf = new byte[buffSize];

            SlottedBlock sp = new SlottedBlock(new Block());
            sp.init();
            sp.setBlockId(7);
            sp.setNextBlockId(8);
            sp.setPrevBlockId(SlottedBlock.INVALID_BLOCK);
            //Insert records
            for (int i=0; i < limit; i++)
            {
                RID rid = sp.insertRecord(tmpBuf);
                //System.out.println("Inserted record, RID " + rid.blockId +
                                   //", " + rid.slotNum);
                rid = sp.nextRecord(rid);
            }

            RID rid = sp.firstRecord();
            while (rid != null)
            {
                tmpBuf = sp.getRecord(rid); 
                //System.out.println("Retrieved record, RID " + rid.blockId +
                                   //", " + rid.slotNum);
                rid = sp.nextRecord(rid);
            }

            //rid = sp.firstRecord();
            //sp.deleteRecord(rid);
            //System.out.println("Deleted first record! " + rid.slotNum);
            RID testRid = new RID(0, 3);
            //sp.findRecord(testRid);
            //sp.deleteRecord(testRid);
            //System.out.println(sp.findRecord(testRid));
            System.out.println(sp.deleteRecord(testRid));
            //sp.deleteRecord(testRid);
            // while (rid != null)
            // {
            //     sp.deleteRecord(rid); 
            //     System.out.println("Deleted record! " + rid.blockId +
            //                        ", " + rid.slotNum);
            //     rid = sp.nextRecord(rid);
            // }
            
        }
    }

    public static boolean runTest(Testable testObj)
    {
        boolean success = true;
        try
        {
            testObj.test();
        }
        catch (Exception e)
        {
            success = false;
            e.printStackTrace();
        }

        return success;
    }


    public static void main(String[] args)
    {
        System.out.println("Running block tests.");

         //runTest(new Test1());
         //runTest(new Test2());
         runTest(new Test3());
    }
}
