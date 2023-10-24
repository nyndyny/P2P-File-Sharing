// This file writes the peer log  into the log file ‘log_peer_[peerID].log'

import PeerInfo;
import java.time.LocalDateTime;
import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;

public class Logs {
  public static int peerID;
  public static LocalDateTime localDT = LocalDateTime.now();
  public static File file;
  public static DateTimeFormatter formatDT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
  private static BufferedWriter buffWriter;
  PeerInfo peerInfo = new PeerInfo();

  public static void startLogging(int pID)
  {
    peerID = pID;
    String name = (new File(System.getProperty("user.dir")).getParent() + "/log_peer_" + thisPeerID + ".log");
    file = new File(name);
    try 
    {
			buffWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		} 
    catch (FileNotFoundException e) 
    {
      System.err.println(e);
		}
  }

  public static void choking(int pID)
  {
    try 
    {
      string temp = formatDT.format(localDT) + thisPeerID + " is choked by " + pID + ".";
      buffWriter.append(temp);
      buffWriter.newLine();
      buffWriter.flush();
		} 
    catch(IOException e) 
    {
			System.err.println(e);
		}
  }

  public static void unchoking(int pID)
  {
		try
    {
  			String temp = formatDT.format(localDT) + " : Peer " + thisPeerID + " is unchoked by " + pID + ".";
  			buffWriter.append(temp);
  			buffWriter.newLine();
  			buffWriter.flush();
		}
    catch(IOException e)
    {
			System.err.println(e);
		}
	}

  //TODO: add logs for other scenarios like TCP connection, downloading a piece, receiving messages, etc.
  
  public static void endLogging()
  {
    try 
    {
			buffWriter.close();
		} 
    catch (IOException e) 
    {
			e.printStackTrace();
		}
  }
  
}

