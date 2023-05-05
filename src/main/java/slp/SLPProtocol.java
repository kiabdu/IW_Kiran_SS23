package slp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import core.Configuration;
import core.Msg;
import core.Protocol;
import phy.PhyConfiguration;
import phy.PhyMsg;
import phy.PhyProtocol;
import exceptions.*;


public class SLPProtocol extends Protocol {
	private static final int SLPTIMEOUT = 2000;
	private int myID;
	private final PhyProtocol phy;
	private final boolean isSwitch;
	private boolean isRegistered;
	private boolean useTimeout;
	// Switches map slp id to virtual link
	private Map<Integer, PhyConfiguration> systems;
	PhyConfiguration phyConfig;

	// Constructor
	public SLPProtocol(int id, boolean isSwitch, PhyProtocol proto) throws IWProtocolException {
		if(isSwitch == true && id < 5000) {
			systems = new HashMap<>();
		} else {
			if(SLPRegMsg.validateAddress(id)  == false )
				throw new IllegalAddrException();
			this.myID = id;
			this.isRegistered = false;
		}
		this.isSwitch = isSwitch;
		this.phy = proto;
	}
	
	/*
	 * Enable/disable the use of timeout when reading from socket
	 */
	private void enableTimeout() {
		this.useTimeout = true;
	}
	private void disableTimeout() {
		this.useTimeout = false;
	}
	
	// Register an end systems 
	public void register(InetAddress rname, int rp) throws IWProtocolException, IOException {
		// Create registration message object 
		SLPRegMsg reg = new SLPRegMsg();
		// Fill registration message fields
		reg.create(Integer.toString(this.myID));
		// Create configuration object
		this.phyConfig = new PhyConfiguration(rname, rp);

		/* -- Remove for task 4 -- */
		// Send registration request via phy layer
		phy.send(new String(reg.getDataBytes()), this.phyConfig);
		SLPMsg in = new SLPMsg();
		;
		Msg inBasic;
		try {
			// Receive response from phy layer - timeout 2s
			inBasic = this.phy.receive(2000);
			// Get data from msg object
			String sentence = inBasic.getData();
			// Parse response
			in = (SLPMsg) in.parse(sentence);
		} catch (SocketTimeoutException e) {
			System.out.println("No response received");
			throw new RegistrationFailedException();
		}catch (IllegalMsgException e) {
			System.out.println("Cannot parse response");
			throw new RegistrationFailedException();
		}
		// If any other message than a registration response message was received or
		// the registration response indicates failure throw exception
		if (!(in instanceof SLPRegResponseMsg) || (((SLPRegResponseMsg)in).getRegResponse() == false)) {
			// A registration non-acknowledgement is signaled by throwing an exception
			throw new RegistrationFailedException();
		}
		/* -- End remove for task 4 -- */

		this.isRegistered = true;
	}
	
	// Create SLPDataMsg object (subtask 3.3) and send
	// Subtask 2.1
	@Override
	public void send(String s, Configuration config) throws IOException, IWProtocolException {

		//2.1 a - The client must not send any message if not registered successfully
		if(!this.isRegistered){
			throw new RegistrationFailedException();
		}

		//2.1 b - Create a SLPDataMsg object
		SLPDataMsg msg = new SLPDataMsg();
		int destinationID = ((SLPConfiguration)config).getRemoteID();
		msg.create(s, destinationID, this.myID);

		//2.1 c - Send to switch. The PHY config is stored as an attribute in SLPProtocol
		this.phy.send(msg.getData(), this.phyConfig);
	}

	// Receive message from underlying protocol, parse and process
	// Subtask 2.2
	@Override
	public Msg receive() throws IOException, IWProtocolException {
		SLPMsg in = new SLPMsg();

		//2.2 b - Call PHY.receive method
		in = (SLPMsg) this.phy.receive();

		//storing and splitting data for further usage
		String data = in.getData();
		String[] parts = data.split("\\s+", 0);

		//2.2 a - A client can only receive data messages if registered
		if(!this.isRegistered){
			throw new RegistrationFailedException();
		}

		//2.2 d - After successful registration any incoming SLPRegMsg must be discarded
		//3.1. of the specs - format of a reg. message: slp⟨WS⟩reg<WS>ACK
		if(parts[1].equals("reg")){
			in = null;
			return this.receive();
		}

		//3.3. of the specs - An end system receiving a data message has to validate that it owns the destination identifier
		//format of a data message: slp⟨WS⟩⟨dest_addr⟩⟨WS⟩⟨src_addr⟩⟨WS⟩⟨data_len⟩⟨WS⟩⟨data⟩⟨WS⟩⟨crc⟩
		if(Integer.parseInt(parts[1]) != this.myID){
			return this.receive();
		}

		//2.2 c - Call the message parser to create a SLP message object from the String object
		//received according to the protocol specification
		try{
			in = new SLPDataMsg().parse(data);
		} catch(Exception e){
			//if the parser throws an exception, the message shall be discarded
			e.printStackTrace();
			in = null;
			return this.receive();
		}

		return in;
	}


	public void storeAndForward() throws IOException {
		while (true) {
			forward();
		}
	}

	public void forward() throws IOException {
		// Subtask 3.2

	}
}
