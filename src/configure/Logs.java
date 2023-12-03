package configure;
// This file writes the peer log into the log file ‘log_peer_[peerID].log'

import java.time.LocalDateTime;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;

//import peer.PeerInfo;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;

public class Logs {
    public static String peerID;
    public static String peerAddress;
    public static String peerPort;
    public static String fileName;
    public static LocalDateTime localDT = LocalDateTime.now();
    public static File file;
    public static DateTimeFormatter formatDT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static BufferedWriter buffWriter;
    public static boolean Set = false;
    public static boolean Check = false;
    // PeerInfo peerInfo = new PeerInfo(peerID, peerAddress, peerPort, fileName);

    public static void startLogging(String pID) {
        peerID = pID;
        String name = (new File(System.getProperty("user.dir")).getParent() + "/log_peer_" + peerID + ".log");
        file = new File(name);
        try {
            buffWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            System.err.println(e);
        }
    }

    public static void choking(int pID) {
        try {
            String temp = formatDT.format(localDT) + peerID + " is choked by " + pID + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void unchoking(int pID) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " is unchoked by " + pID + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void makingTCPConnection(int peerID) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " makes a connection to Peer " + peerID
                    + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void madeTCPConnection(int peerID) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " is connected from Peer " + peerID
                    + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void changeInPreferredNeighbors(String neighbors) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " has the preferred neighbors "
                    + neighbors + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void changeofOptimisticallyUnchockedN(int pId) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " has the optimistically unchoked neighbor "
                    + pId + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void receivingInterested(int pId) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " received the 'interested' message from "
                    + pId + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void receivingNotInterested(int pId) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID
                    + " received the 'not interested' message from " + pId + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void receivingHave(int pId, int pIdx) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " received the 'have' message from Peer "
                    + pId + " for the piece " + pIdx + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void downloadingPiece(int pId, int pIdx) {
        try {
            String temp = formatDT.format(localDT) + " : Peer " + peerID + " has downloaded the piece " + pIdx
                    + " from Peer " + pId + ".";
            buffWriter.append(temp);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void downloadCompletion() {
        if (Set == true) {
            try {
                String temp = formatDT.format(localDT) + " : Peer " + peerID + " has downloaded the complete file.";
                buffWriter.append(temp);
                buffWriter.newLine();
                buffWriter.flush();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    public static void endLogging() {
        try {
            buffWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
