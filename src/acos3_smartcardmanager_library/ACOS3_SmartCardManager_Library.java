/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acos3_smartcardmanager_library;

import javax.smartcardio.*;
import acos3_smartcardmanager_library.CardManager;
import java.util.List;

/**
 *
 * @author AIT SAID
 */
public class ACOS3_SmartCardManager_Library {

    private static final byte[] PIN_CODE = {(byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D,
        (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D};
    private static final byte[] IC_CODE = {(byte) 0x41, (byte) 0x43, (byte) 0x4F, (byte) 0x53, (byte) 0x54,
        (byte) 0x45, (byte) 0x53, (byte) 0x54};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CardException {
        // TODO code application logic here

        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminals readers = factory.terminals();

        // try {
        List<CardTerminal> readersList = readers.list(); //  factory.terminals.list();
        System.out.println("Detected cards:");

        readersList.stream().forEach((ct) -> {
            System.out.println(readersList.indexOf(ct) + "  -->> " + ct.getName());
        });

        CardTerminal term = readersList.get(0);
        System.out.println("Connecting to Terminal " + term.getName());
        if (term.isCardPresent()) {
            System.out.println(term.getName() + " -->> CARD IS PRESENT");
            
            // EXAMPLE OBJECTIVE: Create a file named 0A0A
            //                    Containing 2 records
            //                    With 8 bytes of record length
            //                    Read Access using PIN code
            //                    Write Access using IC code
            
            CardManager manager = new CardManager(term, IC_CODE, PIN_CODE); // Automatic connection
            System.out.println(manager.GetATR());
            manager.ClearCard();
            manager.ResetConnection();
            manager.SetPINCode(PIN_CODE);
            //manager.GetPINCOde()
            manager.ResetConnection();

            // Here we start creation procedure
            byte[] newfile_id_0A0A = {(byte) 0x0A, (byte) 0x0A};
            manager.CreateNewFile(newfile_id_0A0A, (byte) 2, (byte) 0x08, CardManager.SecAttrib.PIN_sec, CardManager.SecAttrib.IC_sec);
            manager.ResetConnection();
            manager.SubmitIC();
            manager.SelectFile(newfile_id_0A0A);
            manager.WriteRecord((byte) 0, (byte) 8, PIN_CODE);
            manager.WriteRecord((byte) 1, (byte) 8, PIN_CODE);

            manager.Disconnect();
        }

    }

}
