package configure;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class PeerInfoParser {
    private int peerID;
    private int currentPeerID;
    private String peerIPAddress;
    private int peerPort;
    private boolean hasCompleteFile;

    private final String peerInfoFilePath = new File(System.getProperty("user.dir")).getParent() + "/PeerInfo.cfg";

    public PeerInfoParser(int currentPeerID) {
        this.currentPeerID = currentPeerID;
    }

    public void readPeerInfoFile() {
        File peerInfoFile = new File(peerInfoFilePath);
        try {
            Scanner scanner = new Scanner(peerInfoFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] infoArray = line.split(" ");

                if (currentPeerID == Integer.parseInt(infoArray[0])) {
                    peerID = Integer.parseInt(infoArray[0]);
                    peerIPAddress = infoArray[1];
                    peerPort = Integer.parseInt(infoArray[2]);
                    hasCompleteFile = Integer.parseInt(infoArray[3]) == 1;
                    System.out.println(hasCompleteFile);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: PeerInfo file not found. Expected Path: " + peerInfoFilePath);
        }
    }

    public ArrayList<Integer> getPeerIDs() {
        ArrayList<Integer> peerIDs = new ArrayList<>();
        File peerInfoFile = new File(peerInfoFilePath);
        try {
            Scanner scanner = new Scanner(peerInfoFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] infoArray = line.split(" ");
                peerIDs.add(Integer.parseInt(infoArray[0]));
            }
            scanner.close();
            return peerIDs;
        } catch (FileNotFoundException e) {
            System.out.println("Error: PeerInfo file not found. Expected Path: " + peerInfoFilePath);
        }
        return null;
    }

    public ArrayList<String[]> getAllPeerInfo() {
        ArrayList<String[]> allPeerInfo = new ArrayList<>();
        File peerInfoFile = new File(peerInfoFilePath);
        try {
            Scanner scanner = new Scanner(peerInfoFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] infoArray = line.split(" ");
                if (currentPeerID != Integer.parseInt(infoArray[0])) {
                    allPeerInfo.add(infoArray);
                } else {
                    break;
                }
            }
            scanner.close();
            return allPeerInfo;
        } catch (FileNotFoundException e) {
            System.out.println("Error: PeerInfo file not found. Expected Path: " + peerInfoFilePath);
        }
        return null;
    }

    public ArrayList<String> getPeerIPs() {
        ArrayList<String> peerIPs = new ArrayList<>();
        File peerInfoFile = new File(peerInfoFilePath);
        try {
            Scanner scanner = new Scanner(peerInfoFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] infoArray = line.split(" ");
                peerIPs.add(infoArray[1]);
            }
            scanner.close();
            return peerIPs;
        } catch (FileNotFoundException e) {
            System.out.println("Error: PeerInfo file not found. Expected Path: " + peerInfoFilePath);
        }
        return null;
    }

    public int getPeerID() {
        return peerID;
    }

    public void setPeerID(int peerID) {
        this.peerID = peerID;
    }

    public String getPeerIPAddress() {
        return peerIPAddress;
    }

    public void setPeerIPAddress(String peerIPAddress) {
        this.peerIPAddress = peerIPAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }

    public boolean hasCompleteFile() {
        return hasCompleteFile;
    }

    public void setHasCompleteFile(boolean hasCompleteFile) {
        this.hasCompleteFile = hasCompleteFile;
    }
}
