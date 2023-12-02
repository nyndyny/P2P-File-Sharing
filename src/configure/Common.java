package process;

import java.io.*;
import java.util.*;

import peer.PeerInfo;

public class Common {
    public int numPreferredNeighbors;
    public int unchokingInterval;
    public int optimalUnchokingInterval;
    public String fileName;
    public long fileSize;
    public long pieceSize;
    public PeerInfo peerInfo;

    public Common(String path) {
        String str;

        try {
            BufferedReader in = new BufferedReader(new FileReader(path + "/Common.cfg"));
            Vector<String> configs = new Vector<String>();

            while ((str = in.readLine()) != null) {
                String[] tokens = str.split("\\s+");
                configs.addElement(tokens[1]);
            }

            numPreferredNeighbors = Integer.parseInt(configs.elementAt(0));
            unchokingInterval = Integer.parseInt(configs.elementAt(1));
            optimalUnchokingInterval = Integer.parseInt(configs.elementAt(2));
            fileName = configs.elementAt(3);
            fileSize = Long.parseLong(configs.elementAt(4));
            pieceSize = Long.parseLong(configs.elementAt(5));

            in.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
