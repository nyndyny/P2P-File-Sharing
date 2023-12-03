package configure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import peer.PeerInfo;

public class Common {
    public static int numPreferredNeighbors;
    public static int unchokingInterval;
    public static int optimalUnchokingInterval;
    public static String fileName;
    public static long fileSize;
    public static long pieceSize;
    public PeerInfo peerInfo;

    public Common(String path) {
        readCommonFile(path);
    }

    /**
     * Reads the common configuration file
     * 
     * @param commonFilePath Path to the common configuration file
     */
    public static HashMap<String, Object> readCommonFile(String commonFilePath) {
        HashMap<String, Object> res = new HashMap<String, Object>();
        try {
            File cfp = new File(commonFilePath + "/Common.cfg");
            BufferedReader sc = new BufferedReader(new FileReader(cfp));

            while (sc.ready()) {
                String data = sc.readLine();
                String[] arr = data.split("\\s+");
                res.put(arr[0], arr[1]);
            }
            sc.close();

            numPreferredNeighbors = Integer.parseInt((String) res.get("NumberOfPreferredNeighbors"));
            unchokingInterval = Integer.parseInt((String) res.get("UnchokingInterval"));
            optimalUnchokingInterval = Integer.parseInt((String) res.get("OptimisticUnchokingInterval"));
            fileName = (String) res.get("FileName");
            fileSize = Long.parseLong((String) res.get("FileSize"));
            pieceSize = Long.parseLong((String) res.get("PieceSize"));
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
        return res;
    }
}
