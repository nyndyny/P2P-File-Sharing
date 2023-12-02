package peer;

import java.net.Socket;

public class PeerConnection {
    private Socket socket;
    private boolean endFile;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setCompleteFileDownloaded(boolean completeFileDownloaded) {
        this.endFile = completeFileDownloaded;
    }

    public boolean hasCompleteFile() {
        return endFile;
    }

    public Socket getSocket() {
        return socket;
    }
}
