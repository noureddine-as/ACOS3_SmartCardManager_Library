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

    private Card myCard;

    // Card communication
    private CardChannel ch;
    private CardTerminal ct;
    private ResponseAPDU resp;
    private Integer LAST_STATUS_CODE;

    // Card attributes
    private byte[] IC_CODE;  // = {(byte) 0x41, 0x43, 0x4F, 0x53, 0x54, 0x45, 0x53,  0x54};
    private byte[] PIN_CODE;

    // Final properties
    private static final byte[] IC_APDU_PREFIX = {(byte) 0x80, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x08};
    private static final byte[] CLEAR_CODE = {(byte) 0x80, (byte) 0x30, (byte) 00, (byte) 0x00, (byte) 0x00};
    private static final byte[] SELECT_FF02 = {(byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xFF, (byte) 0x02};
    private static final byte[] SELECT_FF03 = {(byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xFF, (byte) 0x03};
    private static final byte[] SELECT_FF04 = {(byte) 0x80, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0xFF, (byte) 0x04};

    // (byte) 0x41, (byte) 0x43, (byte) 0x4F, (byte) 0x53, (byte) 0x54,
    // (byte) 0x45, (byte) 0x53, (byte) 0x54};
    private int N_OF_FILE;

    // APDUs
    private byte[] IC_SUBMIT_APDU;

    /*Card myCard,*/
    public CardManager(CardTerminal ct, byte[] IC_CODE, byte[] PIN_CODE) throws CardException {
        this.myCard = ct.connect("*"); //  myCard;
        this.LAST_STATUS_CODE = null;

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
            System.out.println("IC Code Submit -->> OK");
            return true;
        } else {
            System.out.println("IC Code Submit -->> ERROR");
            return false;
        }
    }

    public boolean ClearCard() throws CardException {
        this.resp = this.ch.transmit(new CommandAPDU(CLEAR_CODE));

        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
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
        this.resp = this.ch.transmit(new CommandAPDU(SELECT_FF02));
        if ((this.LAST_STATUS_CODE = this.resp.getSW()) == 0x9000) {
            System.out.println("FF02 selection -->> OK");
            return true;
        } else {
            System.out.println("FF02 selection -->> ERROR");
            return false;
        }
    }

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

    public boolean isCardConnected() {
        return !(this.myCard == null);
    }

    public void Disconnect() throws CardException {
        this.myCard.disconnect(true);
        this.myCard = null;
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

    public int getN_OF_FILE() {
        //this.N_OF_FILE = ...
        //return N_OF_FILE;

        System.out.println("No yet implemented");
        return -1;
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

    /*
    // PROBELMS :'(
    /*   this.myCard = myCard;
        this.PIN_CODE = PIN_CODE;
        this.LAST_STATUS_CODE = null;
     */
    // Creating channel
    //ch = this.myCard.getBasicChannel();
    // Creating my IC_APDU
    /*   this.IC_CODE = new byte[13];
        for(int j=0; j<IC_CODE.length; j++)
            this.IC_CODE[j] = IC_CODE[j];
        
        byte[] ic_apdu_prefix = {(byte) 0x80, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x08};
       // System.arraycopy(ic_apdu_prefix, 0, this.IC_APDU, 0, ic_apdu_prefix.length);
        //System.arraycopy(IC_CODE, 0, this.IC_APDU, ic_apdu_prefix.length, IC_CODE.length);
        
        ByteBuffer bb = ByteBuffer.allocate(13);
        bb.put(this.IC_CODE);
        bb.put(ic_apdu_prefix);

        byte[] cmd = bb.array();
        
        for(int i=0; i<this.IC_APDU.length ; i++)
            System.out.print(" " + Integer.toHexString(cmd[i]));
        // this.N_OF_FILE = 0;   // GET N_OF_FILE
        
        
        byte[] prefix = {(byte) 0x80, (byte) 0x20, (byte) 0x07, (byte) 0x00, (byte) 0x08};
        byte[] code = {(byte) 0x41, (byte) 0x43, (byte) 0x4F, (byte) 0x53, (byte) 0x54,
                            (byte) 0x45, (byte) 0x53, (byte) 0x54};
        
        ByteBuffer bb = ByteBuffer.allocate(prefix.length + code.length);
        bb.put(prefix);
        bb.put(code);

        byte[] cmd = bb.array();
        cmd[0] = (byte) ((byte)cmd[0] & (byte)0xFF); 
        
        for(int i=0; i< cmd.length ; i++)
           System.out.print(" " + Integer.toHexString(cmd[i]));
    
    
     */
}
