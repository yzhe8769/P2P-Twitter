import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Receiver implements Runnable {
	private static final int NOT_YET_INITIALISED = 0;
	private static final int NOT_ACTIVE_BETWEEN_10_20 = 1;
	private static final int ACTIVE_WITHIN_10 = 2;
	private static final int NOT_ACTIVE_MORE_THAN_20 = 3;
	private Status status;
	private String myUnikey;
	private int myIndex;
	private DatagramSocket socket;
	private InetAddress ipaddress;
	private int port;

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
		this.myUnikey = unikey;
		this.status = s;
		this.myIndex = this.status.unikeys.indexOf(myUnikey);
		this.socket = socket;
		try {
			this.ipaddress = InetAddress.getByName(status.getIpAddress(myIndex));
		} catch (UnknownHostException e) {
		}
		this.port = status.getPort(myIndex);
	}

	public static String removeSlashes(String str) {
		String output = "";
		for (int i = 0; i < str.length();) {
			if (str.charAt(i) == '\\' && i != str.length() - 1 && str.charAt(i + 1) == ':') {
				output += str.charAt(i + 1);
				i += 2;
			} else {
				output += str.charAt(i);
				i++;
			}
		}
		return output;
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
		
		int trueLength = d.getLength();
		byte[] input1 = Arrays.copyOf(buff, trueLength);
		String input = null;
		input = new String(input1, iso88591charset);
		String strToProcess = new String(input);
		strToProcess = strToProcess.replaceAll("\\\\:", "");
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
		}else if(components.length == 3){
			seq = components[2];
			if(isNumber(seq)){
				status.changeSequenceNumber(index, Integer.parseInt(seq));
				state = input.substring(input.indexOf(':') + 1, input.lastIndexOf(':'));
			}else{
				state = input.substring(input.indexOf(':') + 1);
			}
		}else{
			return;
			//illegal message, has more then two : not escaped
		}	
		state = removeSlashes(state);
		status.changeStatus(index, state);
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
