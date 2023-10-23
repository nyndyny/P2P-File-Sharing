import java.io.*;

public class PeerProcess {

    private static final String path = new File(System.getProperty("user.dir")).getParent();
    private Common configs;
    private PeerInfo peerInfo;

    public static void main(String[] args) throws Exception {
        PeerProcess peerProcess = new PeerProcess();

        peerProcess.configs = new Common(path);
        peerProcess.peerInfo = new PeerInfo(path, args[0]);

        Server server = new Server(peerProcess.peerInfo.peerID, peerProcess.peerInfo.peerPort);
        server.start();

        if (!peerProcess.peerInfo.hasFile) {
            Client client = new Client(peerProcess.peerInfo.peerID, peerProcess.peerInfo.remotePeers);
            client.start();
        }

    }
}
