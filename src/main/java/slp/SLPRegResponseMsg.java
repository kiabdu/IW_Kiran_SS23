// This class is not distributed to students

package slp;

import core.Msg;

/* 
 * Simple link protocol registration response message fields:
 *  -> slp registration success indicator
 *  -> [error message]
*/

public class SLPRegResponseMsg extends SLPRegMsg {
	protected static final String SLP_REG_SUCCESS = "ACK";
	protected static final String SLP_REG_FAILED = "NAK";

	private boolean regResponse;
	String regResponseMsg;

	protected boolean getRegResponse() {
		return this.regResponse;
	}
	protected void setRegResponse(boolean b) {
		this.regResponse = b;
	}

	@Override
	protected void create(String sentence) {
		// Subtask 3.2


	}

	@Override
	protected Msg parse(String sentence) {
		if (sentence.startsWith(SLP_REG_SUCCESS)) {
			this.regResponse = true;
		} else {
			this.regResponse = false;
			String[] parts = sentence.split("\\s+", 2);
			if (parts.length > 1) {
				regResponseMsg = parts[1];
			}
		}
		return this;
	}
}
