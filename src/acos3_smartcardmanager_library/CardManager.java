/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acos3_smartcardmanager_library;

import javax.smartcardio.*;
import java.util.Arrays;
import java.nio.ByteBuffer;

/**
 *
 * @author AIT SAID
 */
public class CardManager {

    public enum SecAttrib {
        IC_sec, PIN_sec, AC5_sec, AC4_sec, AC3_sec, AC2_sec, AC1_sec, AC0_sec, NONE_sec
    }

    private Card myCard;

    // Card communication
    private CardChannel ch;
    private CardTerminal ct;
    private ResponseAPDU resp;
    public Integer LAST_STATUS_CODE;

    // Card attributes
    private byte[] IC_CODE;  // = {(byte) 0x41, 0x43, 0x4F, 0x53, 0x54, 0x45, 0x53,  0x54};
    private byte[] PIN_CODE;

    // Final properties
    private static final byte[] IC_APDU_PREFIX = {(byte) 0x80, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x08};
    private static final byte[] PIN_APDU_PREFIX = {(byte) 0x80, (byte) 0x20, (byte) 0x06, (byte) 0x00, (byte) 0x08};
    private static final byte[] CLEAR_CODE = {(byte) 0x80, (byte) 0x30, (byte) 00, (byte) 0x00, (byte) 0x00};
    private static final byte[] SELECT_FILE_PREFIX = {(byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02};
    private static final byte[] SELECT_FF02 = {(byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xFF, (byte) 0x02};
    private static final byte[] SELECT_FF03 = {(byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xFF, (byte) 0x03};
    private static final byte[] SELECT_FF04 = {(byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xFF, (byte) 0x04};

    // (byte) 0x41, (byte) 0x43, (byte) 0x4F, (byte) 0x53, (byte) 0x54,
    // (byte) 0x45, (byte) 0x53, (byte) 0x54};
    private byte N_OF_FILE;
    private boolean IC_ALREADY_SUBMIT;
    private boolean PIN_ALREADY_SUBMIT;

    // APDUs
    private byte[] IC_SUBMIT_APDU;
    private byte[] PIN_SUBMIT_APDU;


    /*Card myCard,*/
    public CardManager(CardTerminal ct, byte[] IC_CODE, byte[] PIN_CODE) throws CardException {
        this.myCard = ct.connect("*"); //  myCard;

        this.LAST_STATUS_CODE = null;
        this.IC_ALREADY_SUBMIT = false;
        this.PIN_ALREADY_SUBMIT = false;

        // Creating channel
        this.ch = this.myCard.getBasicChannel();
        this.ct = ct;
        // Creating my IC_APDU
        this.IC_CODE = new byte[8];
        System.arraycopy(IC_CODE, 0, this.IC_CODE, 0, IC_CODE.length);

        this.PIN_CODE = new byte[8];
        System.arraycopy(PIN_CODE, 0, this.PIN_CODE, 0, PIN_CODE.length);

        this.IC_SUBMIT_APDU = new byte[13];
        System.arraycopy(IC_APDU_PREFIX, 0, this.IC_SUBMIT_APDU, 0, IC_APDU_PREFIX.length);
        System.arraycopy(this.IC_CODE, 0, this.IC_SUBMIT_APDU, IC_APDU_PREFIX.length, this.IC_CODE.length);

        this.PIN_SUBMIT_APDU = new byte[13];
        System.arraycopy(PIN_APDU_PREFIX, 0, this.PIN_SUBMIT_APDU, 0, PIN_APDU_PREFIX.length);
        System.arraycopy(this.PIN_CODE, 0, this.PIN_SUBMIT_APDU, PIN_APDU_PREFIX.length, this.PIN_CODE.length);

        //this.IC_SUBMIT_APDU = concatBytes(ic_submit_apdu_prefix, this.IC_CODE);
        //for(int i=0; i<this.IC_SUBMIT_APDU.length ; i++)
        //    System.out.println("" + Integer.toHexString(this.IC_SUBMIT_APDU[i]));
    }

    /*
    Return true  and modifies LAST_STATUS_CODE if succeded
    Return false and modifies LAST_STATUS_CODE if not
     */
    public boolean SubmitIC() throws CardException {
        CommandAPDU Submit_IC_APDU = new CommandAPDU(this.IC_SUBMIT_APDU);
        this.resp = this.ch.transmit(Submit_IC_APDU);

        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            this.IC_ALREADY_SUBMIT = true;
            System.out.println("IC Code Submit -->> OK");
            return true;
        } else {
            this.IC_ALREADY_SUBMIT = false;
            System.out.println("IC Code Submit -->> ERROR");
            return false;
        }
    }

    public boolean SubmitPIN() throws CardException {
        CommandAPDU Submit_PIN_APDU = new CommandAPDU(this.PIN_SUBMIT_APDU);
        this.resp = this.ch.transmit(Submit_PIN_APDU);

        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            this.PIN_ALREADY_SUBMIT = true;
            System.out.println("PIN Code Submit -->> OK");
            return true;
        } else {
            this.PIN_ALREADY_SUBMIT = false;
            System.out.println("PIN Code Submit -->> ERROR");
            return false;
        }
    }

    public boolean ClearCard() throws CardException {
        if(!this.IC_ALREADY_SUBMIT)
            this.SubmitIC();
        
        this.resp = this.ch.transmit(new CommandAPDU(CLEAR_CODE));

        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            this.IC_ALREADY_SUBMIT = false;
            System.out.println("Clearing Card -->> OK");
            return true;
        } else {
            System.out.println("Clearing Card -->> ERROR");
            return false;
        }
    }

    public void ResetConnection() throws CardException {
        this.Disconnect();
        this.Connect();

        //System.out.println("Reset Connection and channel  -->> OK");
    }

    public boolean SelectFF02() throws CardException {
        if(!this.IC_ALREADY_SUBMIT)
            this.SubmitIC();
        
        this.resp = this.ch.transmit(new CommandAPDU(SELECT_FF02));
        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            //System.out.println("FF02 selection -->> OK");
            return true;
        } else {
            //System.out.println("FF02 selection -->> ERROR");
            return false;
        }
    }

    public boolean SelectFF03() throws CardException {
        if(!this.IC_ALREADY_SUBMIT)
            this.SubmitIC();
            
        this.resp = this.ch.transmit(new CommandAPDU(SELECT_FF03));
        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            //System.out.println("FF03 selection -->> OK");
            return true;
        } else {
            //System.out.println("FF03 selection -->> ERROR");
            return false;
        }
    }

    public boolean SelectFF04() throws CardException {
        if(!this.IC_ALREADY_SUBMIT)
            this.SubmitIC();
            
        this.resp = this.ch.transmit(new CommandAPDU(SELECT_FF04));
        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            //System.out.println("FF04 selection -->> OK");
            return true;
        } else {
            //System.out.println("FF04 selection -->> ERROR");
            return false;
        }
    }

    public boolean SelectFile(byte[] fileID) throws CardException {
        byte[] select_file_bytes = new byte[7];
        System.arraycopy(SELECT_FILE_PREFIX, 0, select_file_bytes, 0, SELECT_FILE_PREFIX.length);
        System.arraycopy(fileID, 0, select_file_bytes, SELECT_FILE_PREFIX.length, 2);

        this.resp = this.ch.transmit(new CommandAPDU(select_file_bytes));
        
        this.LAST_STATUS_CODE = this.resp.getSW();
        if(this.LAST_STATUS_CODE == 0x9000)
        {
            System.out.println("File   " + (Integer.toHexString(fileID[0] & 0xFF)) + " " + Integer.toHexString(fileID[1]) + "  selection -->> OK");
            System.out.println("\t -->> It's an internal Data File");
            return true;
        }
        else if ((this.LAST_STATUS_CODE & 0xFF00) == 0x9100) {
            System.out.println("File   " + (Integer.toHexString(fileID[0] & 0xFF)) + " " + Integer.toHexString(fileID[1]) + "  selection -->> OK");
            System.out.println("\t -->> It's a User Data File, located in record " + (this.LAST_STATUS_CODE & 0x00FF) + " in the FF04 file.");
                    
            return true;
        } else {
            System.out.println("File " + Integer.toHexString(fileID[1]) + " " + Integer.toHexString(fileID[1]) + "selection -->> ERROR");
            //System.out.println();
            return false;
        }
    }

    public boolean WriteRecord(byte recNo, byte Len, byte[] data) throws CardException {
        byte[] write_record_bytes = new byte[5 + data.length];
        write_record_bytes[0] = (byte) 0x80;
        write_record_bytes[1] = (byte) 0xD2;
        write_record_bytes[2] = recNo;
        write_record_bytes[3] = (byte) 0x00;
        write_record_bytes[4] = Len;
        System.arraycopy(data, 0, write_record_bytes, 5, data.length);

        this.resp = this.ch.transmit(new CommandAPDU(write_record_bytes));
        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            System.out.println("Record Writing -->> OK");
            return true;
        } else {
            System.out.println("Record Writing -->> ERROR");
            return false;
        }
    }

    /*
    Returns only data
    -> status is stored in LAST_STATUS_CODE
     */
    public byte[] ReadRecord(byte recNo, byte Len) throws CardException {
        byte[] read_record_bytes = new byte[5];

        read_record_bytes[0] = (byte) 0x80;
        read_record_bytes[1] = (byte) 0xB2;
        read_record_bytes[2] = recNo;
        read_record_bytes[3] = (byte) 0x00;
        read_record_bytes[4] = Len;

        this.resp = this.ch.transmit(new CommandAPDU(read_record_bytes));

        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            System.out.print("Record Reading: ");

            for (byte bb : this.resp.getData()) {
                System.out.print(" " + Integer.toHexString(bb));
            }
            System.out.println("  -->> OK");
            return this.resp.getData();
        } else {
            System.out.println("Record Reading -->> ERROR");
            return null;
        }
    }

    // same as ReadRecord but without ECHO
    public byte[] __ReadRecord(byte recNo, byte Len) throws CardException {
        byte[] read_record_bytes = new byte[5];

        read_record_bytes[0] = (byte) 0x80;
        read_record_bytes[1] = (byte) 0xB2;
        read_record_bytes[2] = recNo;
        read_record_bytes[3] = (byte) 0x00;
        read_record_bytes[4] = Len;

        this.resp = this.ch.transmit(new CommandAPDU(read_record_bytes));

        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            /*System.out.print("Record Reading: ");

            for (byte bb : this.resp.getData())
            {
              System.out.print(" "+ Integer.toHexString(bb));
            }
            System.out.println("  -->> OK");  */
            return this.resp.getData();
        } else {
            //System.out.println("Record Reading -->> ERROR");
            return null;
        }
    }

    public boolean CreateNewFile(byte[] fileID, byte nbRecords, byte recLength, SecAttrib readSecAttrib, SecAttrib writeSecAttrib) throws CardException {
        //if(!this.IC_ALREADY_SUBMIT)
        //    this.SubmitIC();

        byte lastN_OF_FILE = this.getN_OF_FILE();
        // FF02 is still selected
        //this.SelectFF02();
        byte[] data_ff02 = {(byte) 0x00, (byte) 0x00, (byte) (lastN_OF_FILE + 1), (byte) 0x00};
        if (!this.WriteRecord((byte) 0, (byte) 4, data_ff02))
            return false;

        this.ResetConnection();

        this.SelectFF04();
        byte[] data_ff04 = {recLength, nbRecords, secAttribToByte(readSecAttrib), secAttribToByte(writeSecAttrib), fileID[0], fileID[1]};
        if( !this.WriteRecord(lastN_OF_FILE, (byte)6, data_ff04))
            return false;
        
        return true;
    }
    
    public boolean SetPINCode(byte[] pin_code) throws CardException
    {
        this.SelectFF03();
        return (this.WriteRecord((byte)1, (byte)8, pin_code));
    }
    
    public byte[] GetPINCOde() throws CardException
    {
        this.SelectFF03();
        return (this.ReadRecord((byte)1, (byte)8));
    }

    public byte secAttribToByte(SecAttrib sa) {
        switch (sa) {
            case IC_sec:
                return (byte)0x80;
            case PIN_sec:
                return (byte)0x40;
            case AC5_sec:
                return (byte)0x20;
            case AC4_sec:
                return (byte)0x10;
            case AC3_sec:
                return (byte)0x08;
            case AC2_sec:
                return (byte)0x04;
            case AC1_sec:
                return (byte)0x02;
            case AC0_sec:
                return (byte)0x01;
            default:
                return (byte)0x00;
        }
    }

    /**
     * **
     * GARA ADD -> Create file // WriteFile // ReadFile BUT before try to write
     * them in ur testing java file
     *
     * @return
     * @throws CardException
     */
    public boolean Connect() throws CardException {
        this.myCard = this.ct.connect("*");

        if (this.myCard == null) {
            System.out.println("Connected to card  -->> ERROR");
            return false;

        }

        this.ch = this.myCard.getBasicChannel();
        System.out.println("Connected to card  -->> OK");
        return true;
    }

    public boolean isCardNUll() {
        return (this.myCard == null);
    }

    public void Disconnect() throws CardException {
        this.myCard.disconnect(true);
        this.myCard = null;
        this.IC_ALREADY_SUBMIT = false;

        System.out.println("Disconnected from card  -->> OK");
    }

    public String GetATR() {
        ATR atr = this.myCard.getATR();
        return atr.toString();
    }

    public int getLAST_STATUS_CODE() {
        return LAST_STATUS_CODE;
    }

    private void setLAST_STATUS_CODE(int LAST_STATUS_CODE) {
        this.LAST_STATUS_CODE = LAST_STATUS_CODE;
    }

    public byte getN_OF_FILE() throws CardException {
        //this.N_OF_FILE = ...
        //return N_OF_FILE;

        if (!this.IC_ALREADY_SUBMIT) {
            this.SubmitIC();
        }

        //byte[] id = {(byte)0xFF, (byte)0x02};
        this.SelectFF02();

        byte n_of_file = this.__ReadRecord((byte)0, (byte)4)[2];
        System.out.println("Card contains  " + n_of_file + "  file.");
        this.N_OF_FILE = n_of_file;
        return n_of_file;
    }

    public void setCard(Card myCard) {
        this.myCard = myCard;
    }

    public void setIC_CODE(byte[] IC_CODE) {
        this.IC_CODE = IC_CODE;
    }

    public void setPIN_CODE(byte[] PIN_CODE) {
        this.PIN_CODE = PIN_CODE;
    }

    public Card getActualCard() {
        return myCard;
    }

    public byte[] getActualIC_CODE() {
        return IC_CODE;
    }

    public byte[] getActualPIN_CODE() {
        return PIN_CODE;
    }

}
