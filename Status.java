import java.util.*;

public class Status {
	//public static final int NOT_YET_INITIALISED = 0;
	public static final int NOT_ACTIVE_BETWEEN_10_20 = 1;
	public static final int ACTIVE_WITHIN_10 = 2;
	public static final int NOT_ACTIVE_MORE_THAN_20 = 3;
	public List<String> participants;
	public ArrayList<String> ipAddresses;
	public ArrayList<String> pseudoes;
	public ArrayList<String> unikeys;
	public ArrayList<Integer> ports;
	public ArrayList<String> lineOfStatus;
	public ArrayList<Integer> activeStatus;
	public ArrayList<Long> lastTimeActive;
	public ArrayList<Integer> sequenceNumbers;
	public ArrayList<Boolean> initialized;
	public int myIndex;

	public Status() {
		ipAddresses = new ArrayList<String>();
		pseudoes = new ArrayList<String>();
		unikeys = new ArrayList<String>();
		ports = new ArrayList<Integer>();
		lineOfStatus = new ArrayList<String>();
		activeStatus = new ArrayList<Integer>();
		lastTimeActive = new ArrayList<Long>();
		sequenceNumbers = new ArrayList<Integer>();
		initialized = new ArrayList<Boolean>();
	}

	public synchronized boolean isInitialized(int index) {
		return initialized.get(index);
	}

	public synchronized void changeInitializeStatus(int index, boolean newStatus) {
		initialized.set(index, newStatus);
	}

	public synchronized int getNumberOfParticipants() {
		return participants.size();
	}

	public void setIndex(int myIndex) {
		this.myIndex = myIndex;
	}

	public synchronized String getIpAddress(int index) {
		return ipAddresses.get(index);
	}

	public synchronized int getIndexByUnikey(String unikey) {
		return unikeys.indexOf(unikey);
	}

	public synchronized int getPort(int index) {
		return ports.get(index);
	}

	public synchronized int getSequenceNumber(int index) {
		return sequenceNumbers.get(index);
	}

	public synchronized String getStatus(int index) {
		return lineOfStatus.get(index);
	}

	public synchronized int getActiveStatus(int index) {
		return activeStatus.get(index);
	}

	public synchronized long getLastTimeActive(int index) {
		return lastTimeActive.get(index);
	}

	public synchronized void changeStatus(int index, String newStatus) {
		this.lineOfStatus.set(index, newStatus);
	}

	public synchronized void changeActiveStatus(int index, int newActiveStatus) {
		this.activeStatus.set(index, newActiveStatus);
	}

	public synchronized void changeLastTimeActive(int index, long newTime) {
		this.lastTimeActive.set(index, newTime);
	}

	public synchronized void changeSequenceNumber(int index, int newSequenceNumber) {
		this.sequenceNumbers.set(index, newSequenceNumber);
	}

	public synchronized String toString() {
		String output = "";
		for (int i = 0; i < participants.size(); i++) {
			if (i == myIndex) {
				output += "# " + pseudoes.get(i) + " (myself): " + lineOfStatus.get(i);
			} else {
				if (activeStatus.get(i) == ACTIVE_WITHIN_10) {
					if(initialized.get(i) == true){
						output += "# " + pseudoes.get(i) + " (" + unikeys.get(i) + "): " + lineOfStatus.get(i);
					}else{
						output += "# [" + pseudoes.get(i) + " (" + unikeys.get(i) + "): not yet initialized]";
					}	
				} else if (activeStatus.get(i) == NOT_ACTIVE_BETWEEN_10_20) {
					output += "# [" + pseudoes.get(i) + " (" + unikeys.get(i) + "): " + "idle]";
				} else if (activeStatus.get(i) == NOT_ACTIVE_MORE_THAN_20) {
					continue;
				}

			}
			output += "\n";

		}

		return output;
	}
}
