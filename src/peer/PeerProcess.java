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
    public static String fName;
    private boolean completeFile;
    private int port;
    private int peerID;
    private int nPieces;
    private long fSize;
    private long pSize;
    public static HashMap<Integer, Piece> enumPieces;
    public static ArrayList<PeerInfo> peersList = new ArrayList<PeerInfo>();
    public static ArrayList<Integer> peerIDList;
    // private static ArrayList<Integer> prefPeerIDList;
    public static LinkedList<Message> msgPool = new LinkedList<Message>();
    public static ArrayList<PeerConnection> hasFullFile = new ArrayList<PeerConnection>();

    public static void main(String[] args) throws Exception {
        PeerProcess peerProcess = new PeerProcess();

        // peerProcess.configs = new Common(path);
        // peerProcess.peerInfo = new PeerInfo(path, args[0]);

        // Read the Common Configuration File
        Common commonCfParser = new Common(path);
        HashMap<String, Object> commonFileData = commonCfParser.readCommonFile(path);

        // Read the Peer Info File
        PeerInfoParser peerInfoParser = new PeerInfoParser(Integer.parseInt(args[0]));
        peerInfoParser.readPeerInfoFile();
        peerIDList = peerInfoParser.getPeerIDs();

        // Populate current peer data
        peerProcess.fName = (String) commonFileData.get("FileName");
        peerProcess.fSize = Long.parseLong((String) commonFileData.get("FileSize"));
        peerProcess.pSize = Long.parseLong((String) commonFileData.get("PieceSize"));
        peerProcess.nPieces = (int) Math.ceil((double) peerProcess.fSize / peerProcess.pSize);
        peerProcess.peerID = peerInfoParser.getPeerID();
        peerProcess.port = peerInfoParser.getPeerPort();
        peerProcess.completeFile = peerInfoParser.hasCompleteFile();
        Bitfield.setBitfield(peerProcess.completeFile, peerProcess.nPieces);

        // Start the logging per peer
        Logs.startLogging(Integer.toString(peerProcess.peerID));

        if (peerProcess.completeFile == false) {
            enumPieces = new HashMap<Integer, Piece>();

            Server server = new Server(peerProcess.port, peerProcess.peerID, peerProcess.completeFile,
                    peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
            server.start();

            Client client = new Client(peerProcess.peerID, peerProcess.completeFile, peerProcess.nPieces,
                    peerProcess.fSize, peerProcess.pSize);
            client.start();
        } else if (peerProcess.completeFile == true) {
            Parser fileParser = new Parser(peerProcess.peerID, peerProcess.fName, peerProcess.pSize);
            enumPieces = fileParser.readPiecesFromFile();
            if (checkFileDetails(enumPieces, peerProcess.fSize, peerProcess.pSize)) {
                System.out.println("Read File " + peerProcess.fName);

                Server server = new Server(peerProcess.port, peerProcess.peerID, peerProcess.completeFile,
                        peerProcess.nPieces, peerProcess.fSize, peerProcess.pSize);
                server.start();
            } else {
                System.out.println("File corrupted.");
                System.exit(0);
            }
        }
    }

    private static boolean checkFileDetails(HashMap<Integer, Piece> enumPieces2, long fSize2, long pSize2) {

        int numberOfPieces = enumPieces2.size();
        int expectedNPieces = (int) Math.ceil((double) fSize2 / pSize2);
        return numberOfPieces == expectedNPieces;
    }
}
