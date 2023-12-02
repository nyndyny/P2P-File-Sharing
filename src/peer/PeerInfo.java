package peer;
/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class PeerInfo {
	public int peerID;
	public int peerPort;
	public String peerAddress;
	public Boolean hasFile;
	public Vector<PeerInfo> remotePeers;

	public PeerInfo(String path, String pID) {
		String str;

		try {
			BufferedReader in = new BufferedReader(new FileReader(path + "/PeerInfo.cfg"));
			remotePeers = new Vector<PeerInfo>();

			while ((str = in.readLine()) != null) {
				String[] tokens = str.split("\\s+");

				// Add all preceding peers to peers vector and stop when reaching itself
				if (!pID.equals(tokens[0])) {
					remotePeers.add(new PeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));
				} else {
					peerID = Integer.parseInt(tokens[0]);
					peerAddress = tokens[1];
					peerPort = Integer.parseInt(tokens[2]);
					hasFile = Integer.parseInt(tokens[3]) == 1;
					break;
				}
			}
			in.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	public PeerInfo(String pID, String pAddress, String pPort, String file) {
		peerID = Integer.parseInt(pID);
		peerAddress = pAddress;
		peerPort = Integer.parseInt(pPort);
		hasFile = Integer.parseInt(file) == 1;
	}
}