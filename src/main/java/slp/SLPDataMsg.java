// This class is not distributed to students

package slp;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

import core.Msg;
import exceptions.BadChecksumException;
import exceptions.IWProtocolException;
import exceptions.IllegalAddrException;
import exceptions.IllegalMsgException;

import javax.xml.crypto.Data;

/* 
 * Simple link protocol data message fields:
 *  -> DataHeader
 * 	-> dst addr
 *  -> src addr
 *  -> data length
 *  -> data
 *  -> checksum
*/


public class SLPDataMsg extends SLPMsg {
    private String DATA_HEADER = "slp";
    private int dstAddr, srcAddr;
    private int dataLength;
    private CRC32 crc32;

    private String message;

    protected SLPDataMsg() {
        super();
        this.crc32 = new CRC32();
    }

    protected void create(String data, int dstAddr, int srcAddr){
        this.dstAddr = dstAddr;
        this.srcAddr = srcAddr;
        this.data = data;

        //2.3 a - Use the length field to extract the right amount of characters from the message
        dataBytes = this.data.getBytes();
        dataLength = dataBytes.length;

        //2.3 b - To calculate the crc-checksum use the CRC32 class of Java
        crc32.update(dataBytes);
        String crc = String.valueOf(crc32.getValue());

        /*
        format according to 3.3. of the specs:
        slp⟨WS⟩⟨dest_addr⟩⟨WS⟩⟨src_addr⟩⟨WS⟩⟨data_len⟩⟨WS⟩⟨data⟩⟨WS⟩⟨crc⟩
         */
        message = DATA_HEADER + " " + dstAddr + " " + srcAddr + " " + dataLength + " " + data + " " + crc;

        System.out.println(message);
    }

    @Override
    public SLPDataMsg parse(String sentence) throws IWProtocolException {
        SLPDataMsg slpDataMsg = new SLPDataMsg();

        String destAddr, srcAddr, dataLen, data;
        Long crc, calculatedCrc;
        CRC32 crc32 = new CRC32();

        // PARTS = ⟨dest_addr⟩⟨src_addr⟩⟨data_len⟩⟨data⟩⟨crc⟩
        String [] parts = sentence.split("\\s+");

        destAddr = parts[0];
        srcAddr = parts[1];
        dataLen = parts[2];

        // get the last entry of parts array -> crc checksum
        crc = Long.valueOf(parts[parts.length - 1]);

        // if parts array has < 5 entries, the crc/data is missing
        if (parts.length < 5) {
            calculatedCrc = (long) 0;
            data="";
        }
        else {
            // Extracting the data
            // last part of this array is ⟨data⟩⟨WS⟩⟨data⟩⟨WS⟩⟨data⟩⟨WS⟩⟨crc⟩
            String[] temp = sentence.split("\\s+", 4);
            data = temp[3].replace(" " + crc, "");

            //calculating crc to check for mismatches
            crc32.update(data.getBytes());
            calculatedCrc = crc32.getValue();
        }

        // 4.1. of the specs - messages that do not adhere to this specification have to be discarded silently
        if (Integer.parseInt(dataLen) != data.length()) {
            throw new IllegalMsgException();
        }

        // 3.3. of the specs - in case the two checksums do not match, the data message has to be silently dropped
        if( !crc.equals(calculatedCrc) ) {
            throw new BadChecksumException();
        }

        slpDataMsg.create(data, Integer.parseInt(destAddr), Integer.parseInt(srcAddr));
        crc32.reset();

        return slpDataMsg;
    }


    //2.3 c - Add getter/setter methods as needed by further tasks
    public int getSrc(){
        return this.srcAddr;
    }

    public int getDst(){
        return this.dstAddr;
    }

    public String getMessage(){
        return this.message;
    }

    public void setMessage(String message){
        this.message = message;
    }
}
