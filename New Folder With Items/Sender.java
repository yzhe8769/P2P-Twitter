import java.io.*;
import java.net.*;
import java.nio.charset.Charset;

public class Sender implements Runnable {
	private Status status;
	private String myUnikey;
	private int myIndex;
	private DatagramSocket socket;
	private int logicalClock;

	public Sender(Status s, String unikey, DatagramSocket socket) {
		myUnikey = unikey;
		this.status = s;
		this.myIndex = this.status.unikeys.indexOf(unikey);
		this.socket = socket;
		logicalClock = 0;
	}

	public static String addSlashes(String str) {
		String result = "";
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != ':') {
				result += str.charAt(i);
			} else {
				result += "\\" + str.charAt(i);
			}
		}
		return result;
	}

	public void broadcast() {
		String newStatus;
		newStatus = status.getStatus(myIndex);
		Charset iso88591charset = Charset.forName("ISO-8859-1");
		newStatus = addSlashes(newStatus);
		String output = myUnikey + ":" + newStatus + ":" + logicalClock;
		byte[] toSend = null;
		toSend = output.getBytes(iso88591charset);
		for (int i = 0; i < status.getNumberOfParticipants(); i++) {
			// send the message using the datagram socket to all other peers
			if (i != myIndex) {
				DatagramPacket d = null;
				try {
					d = new DatagramPacket(toSend, toSend.length, InetAddress.getByName(status.getIpAddress(i)),
							status.getPort(i));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				try {
					socket.send(d);
				} catch (IOException e) {
					continue;
				}
			}
		}
		logicalClock++;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep((long) (Math.random() * 2000 + 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			broadcast();
		}

	}
}
