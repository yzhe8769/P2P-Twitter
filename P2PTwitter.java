import java.util.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.SocketException;

public class P2PTwitter implements Runnable {
	//public static final int NOT_YET_INITIALISED = 0;
	public static final int NOT_ACTIVE_BETWEEN_10_20 = 1;
	public static final int ACTIVE_WITHIN_10 = 2;
	public static final int NOT_ACTIVE_MORE_THAN_20 = 3;
	private String unikey;
	private Status s;
	public P2PTwitter(){
		
	}
	public P2PTwitter(String unikey, Status s) {
		this.unikey = unikey;
		this.s = s;
	}

	public static void main(String[] args) {
		Status s = new Status();
		Scanner fileInput = null;
		try {
			fileInput = new Scanner(new FileReader("participants.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (fileInput.hasNextLine()) {
			String input = fileInput.nextLine();
			if (input.length() >= 12 && input.substring(0, 13).equals("participants=")) {
				s.participants = Arrays.asList(input.substring(13).split(","));
				break;
			} else {
				continue;
			}
		}
		for (int i = 0; i < s.participants.size(); i++) {
			String p = s.participants.get(i);
			while (fileInput.hasNextLine()) {
				String input = fileInput.nextLine();
				if (input.startsWith(p)) {
					String information = input.substring(p.length() + 1);
					if (information.startsWith("ip")) {
						s.ipAddresses.add(information.substring(3));
					} else if (information.startsWith("pseudo")) {
						s.pseudoes.add(information.substring(7));
					} else if (information.startsWith("unikey")) {
						s.unikeys.add(information.substring(7));
						if (information.substring(7).equals(args[0])) {
							s.setIndex(i);
						}
					} else if (information.startsWith("port")) {
						s.ports.add(Integer.parseInt(information.substring(5)));
						break;
					}
				} else {
					continue;
				}
			}
			s.lineOfStatus.add("");
			s.activeStatus.add(ACTIVE_WITHIN_10);
			s.initialized.add(false);
			s.lastTimeActive.add(System.currentTimeMillis() / 1000);
			s.sequenceNumbers.add(0);
		}
		fileInput.close();
		Thread p2p = new Thread(new P2PTwitter(args[0], s));
		p2p.start();

	}

	@Override
	public void run() {
		// Scanner for standard input
		Scanner in = new Scanner(System.in);
		
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(s.ports.get(s.unikeys.indexOf(unikey)));
		} catch (SocketException e) {
			System.out.println("SocketException");
		}

		Sender send = new Sender(s, unikey, socket);
		Thread recv = new Thread(new Receiver(s, unikey, socket));
		Thread sender = new Thread(send);
		int myIndex = s.getIndexByUnikey(unikey);
		boolean serverStarted = false;
		recv.start();
		while (true) {
			System.out.print("Status: ");
			String myStatus = in.nextLine();
			if (myStatus.length() > 140) {
				System.out.println("Status is too long, 140 characters max. Retry.");
				continue;
			}
			if (myStatus.length() == 0) {
				System.out.println("Status is empty. Retry.");
				continue;
			}
			System.out.println("### P2P tweets ###");
			s.changeStatus(myIndex, myStatus);
			s.changeInitializeStatus(myIndex, true);
			s.changeLastTimeActive(myIndex, (long) (System.currentTimeMillis() / 1000));
			if (!serverStarted) {
				sender.start();
				serverStarted = true;
			}
			send.broadcast();
			for (int i = 0; i < s.getNumberOfParticipants(); i++) {
					long currentTime = System.currentTimeMillis() / 1000;
					long lastTimeActive = s.getLastTimeActive(i);
					if (currentTime - lastTimeActive < 10) {
						s.changeActiveStatus(i, ACTIVE_WITHIN_10);
					} else if (currentTime - lastTimeActive < 20) {
						s.changeActiveStatus(i, NOT_ACTIVE_BETWEEN_10_20);
					} else {
						s.changeActiveStatus(i, NOT_ACTIVE_MORE_THAN_20);
					}
				
			}
			System.out.print(s);
			System.out.println("### End tweets ###\n");
		}
	}
}
