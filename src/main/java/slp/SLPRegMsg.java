package slp;

import core.Msg;
import exceptions.IWProtocolException;
import exceptions.IllegalAddrException;
import exceptions.IllegalMsgException;

/* 
 * Simple link protocol registration request message fields:
 * 	-> RegHeader
 *  -> slp id
*/


public class SLPRegMsg extends SLPMsg {
	protected static final String SLP_REG_HEADER = "reg ";

	private int slpid;
	
	protected int getSlpId() {
		return slpid;
	}

	/*
	* Create registration message. This message concatenates the reg header with the SLP ID.
	* The slp header is prepended in the super-class.
	*/
	@Override
	protected void create(String data) {
		// prepend reg header
		data =  SLP_REG_HEADER + data;
		// super class prepends slp header
		super.create(data);
	}
	
	/*
	 * This method should be called by SLPMsg.parse only.
	 * Tokenize the given string object.
	 * Test if the first token is 'reg'.
	 * Test if next token is ACK or NAK -> call registration response parser
	 */
	@Override
	protected Msg parse(String sentence) throws IWProtocolException {
		//Split String at whitespace
		String[] parts = sentence.split("\\s+", 2);
		
		//Check that the message contains exactly two fields
		if(parts.length < 2)
			throw new IllegalMsgException();

		// Test if it is a response msg
		if (parts[1].startsWith(SLPRegResponseMsg.SLP_REG_SUCCESS) || parts[1].startsWith(SLPRegResponseMsg.SLP_REG_FAILED)) {
			Msg pdu = new SLPRegResponseMsg();
			((SLPRegResponseMsg) pdu).parse(parts[1]);
			return pdu;
		}

		// Subtask 3.2


		return this;
	}
}
