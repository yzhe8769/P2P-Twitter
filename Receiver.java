import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Receiver implements Runnable {
	private static final int ACTIVE_WITHIN_10 = 2;
	private Status status;
	private DatagramSocket socket;

	public static boolean isNumber(String str) {
		try {
			@SuppressWarnings("unused")
			int i = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public Receiver(Status s, String unikey, DatagramSocket socket) {
		this.status = s;
		this.socket = socket;
	}

	// listen on the socket, verify the unikey, if not do nothing, if yes,
	// update the time active
	public void listen() {
		byte[] buff = new byte[300];

		DatagramPacket d;
		Charset iso88591charset = Charset.forName("ISO-8859-1");
		int index;
		d = new DatagramPacket(buff, buff.length);
		try {
			socket.receive(d);
		} catch (IOException e) {
			return;
		}
		String input = new String(d.getData(), 0, d.getLength());
		//int trueLength = d.getLength();
		//byte[] input1 = Arrays.copyOf(buff, trueLength);
		//String input = null;
		//input = new String(input1, iso88591charset);
		String strToProcess = new String(input);
		strToProcess = strToProcess.replaceAll("\\\\:", "" + (char)256);
		String[] components = strToProcess.split(":");
		String state = null;
		String seq = null;
		String unikey = components[0];
		index = status.getIndexByUnikey(unikey);
		if (index == -1) {
			return;
		}
		if(components.length == 2){
			//no sequence number for sure
			 state = input.substring(input.indexOf(':') + 1);
			 status.changeSequenceNumber(index, 0);
		}else if(components.length == 3){
			seq = components[2];
			if(isNumber(seq)){
				int sequenceNumber = Integer.parseInt(seq);
				int oldSeq = status.getSequenceNumber(index);
				if(sequenceNumber == 0){
					status.changeSequenceNumber(index, sequenceNumber);
					state = input.substring(input.indexOf(':') + 1, input.lastIndexOf(':'));
				}else if(oldSeq > sequenceNumber){
					//ignore message
					return;
				}else{
					status.changeSequenceNumber(index, sequenceNumber);
					state = input.substring(input.indexOf(':') + 1, input.lastIndexOf(':'));
				}	
			}else{
				//sequence number invalid
				state = input.substring(input.indexOf(':') + 1);
				status.changeSequenceNumber(index, 0);
			}
		}else{
			return;
			//illegal message, has more then two : not escaped
		}	
		state = state.replaceAll("\\\\:", ":");
		status.changeStatus(index, state);
		status.changeInitializeStatus(index, true);
		status.changeActiveStatus(index, ACTIVE_WITHIN_10);
		
		status.changeLastTimeActive(index, (long) (System.currentTimeMillis() / 1000));
	}

	@Override
	public void run() {
		while(true){
			listen();
		}

	}

}
