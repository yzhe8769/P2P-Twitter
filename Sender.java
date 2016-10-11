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

	public void broadcast() {
		String newStatus;
		newStatus = status.getStatus(myIndex);
		Charset iso88591charset = Charset.forName("ISO-8859-1");
		newStatus = newStatus.replaceAll(":", "\\\\:");
		String output = myUnikey + ":" + newStatus + ":" + logicalClock;
		byte[] toSend = null;
		toSend = output.getBytes();
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
