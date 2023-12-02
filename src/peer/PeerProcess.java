package peer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import configure.Common;
import configure.Parser;
import configure.Logs;
import configure.PeerInfoParser;
import messageTypes.Bitfield;
import messageTypes.Piece;
import process.Client;
import process.Server;

public class PeerProcess {

    private static final String path = new File(System.getProperty("user.dir")).getParent();
    private static Common configs;
    private PeerInfo peerInfo;

    // Existing members from the first code
    public static String fName;
    private boolean completeFile;
    private int PORT;
    private int peerID;
    private int nPieces;
    private long fSize;
    private long pSize;
    public static HashMap<Integer, Piece> enumPieces;
    public static ArrayList<PeerInfo> peersList = new ArrayList<PeerInfo>();
    private static ArrayList<Integer> peerIDList;
    private static ArrayList<Integer> prefPeerIDList;
    public static LinkedList<Message> msgPool = new LinkedList<Message>();
    public static ArrayList<PeerConnection> hasFullFile = new ArrayList<PeerConnection>();

    public static void main(String[] args) throws Exception {
        PeerProcess peerProcess = new PeerProcess();

        peerProcess.configs = new Common(path);
        peerProcess.peerInfo = new PeerInfo(path, args[0]);

        // Read the Common Configuration File
        Common commonCfParser = new Common(path);
        HashMap<String, Object> commonFileData = commonCfParser.readCommonFile(path);

        // Read the Peer Info File
        PeerInfoParser peerInfoParser = new PeerInfoParser(peerProcess.peerInfo.peerID);
        peerInfoParser.readPeerInfoFile();
        peerIDList = peerInfoParser.getPeerIDs();

        // Populate current peer data
        peerProcess.fName = (String) commonFileData.get("FileName");
        peerProcess.fSize = Long.parseLong((String) commonFileData.get("FileSize"));
        peerProcess.pSize = Long.parseLong((String) commonFileData.get("PieceSize"));
        peerProcess.nPieces = (int) Math.ceil((double) peerProcess.fSize / peerProcess.pSize);
        peerProcess.peerID = peerInfoParser.getPeerID();
        peerProcess.PORT = peerInfoParser.getPeerPort();
        peerProcess.completeFile = peerInfoParser.hasCompleteFile();
        Bitfield.setBitfield(peerProcess.completeFile, peerProcess.nPieces);

        // Start the logging per peer
        Logs.startLogging(Integer.toString(peerProcess.peerID));

        // If peer does not have the complete file the peer will be started up as both,
        // a client and a server.
        if (peerProcess.completeFile == false) {
            // Since no file exists a hashmap of enumPieces needs to be made from scratch
            enumPieces = new HashMap<Integer, Piece>();

            Server server = new Server(peerProcess.peerInfo.peerID, peerProcess.peerInfo.peerPort);
            server.start();

            Client client = new Client(peerProcess.peerInfo.peerID, peerProcess.peerInfo.remotePeers);
            client.start();
        } else if (peerProcess.completeFile == true) {
            // Since server has a full file the enumPieces HashMap will be populated with
            // the
            // data from the file present on the server itself.
            Parser fileParser = new Parser(peerProcess.peerID, peerProcess.fName, peerProcess.pSize);
            enumPieces = fileParser.readPiecesFromFile();
            if (checkFileDetails(enumPieces, peerProcess.fSize, peerProcess.pSize)) {
                System.out.println("Read File " + peerProcess.fName);

                Server server = new Server(peerProcess.peerInfo.peerID, peerProcess.peerInfo.peerPort);
                server.start();
            } else {
                System.out.println("File corrupted.");
                System.exit(0);
            }
        }
    }

    /**
     * Checks if the expected file piece count and the recently received file piece
     * count match
     * 
     * @param enumPieces2 Enumerated Piece List of all the pieces
     * @param fSize2      Expected file size
     * @param pSize2      size per piece
     * @return
     */
    private static boolean checkFileDetails(HashMap<Integer, Piece> enumPieces2, long fSize2, long pSize2) {

        int numberOfPieces = enumPieces2.size();
        int expectedNPieces = (int) Math.ceil((double) fSize2 / pSize2);
        return numberOfPieces == expectedNPieces;
    }
}
