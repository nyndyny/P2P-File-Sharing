package process;

import java.net.Socket;
import java.util.Iterator;
import java.util.Random;

import configure.Merger;
import configure.Logs;
import peer.PeerConnection;
import peer.Message;
import peer.PeerInfo;
import peer.PeerProcess;
import messageTypes.Bitfield;
import messageTypes.Interested;
import messageTypes.NotInterested;
import messageTypes.Request;

public class RequestPiece extends Thread {

    private int requestingPeerID;
    private int numberOfPieces;
    private long fileSize;
    private long pieceSize;
    private int peerID;
    private boolean completeFile;
    private boolean flag = false;
    private Socket socket;

    public RequestPiece(int peerID, int numberOfPieces, boolean completeFile, long fileSize, long pieceSize) {
        this.peerID = peerID;
        this.numberOfPieces = numberOfPieces;
        this.completeFile = completeFile;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
    }

    @Override
    public void run() {
        if (!completeFile) {
            PeerInfo peer = null;
            byte[] bitField;
            int requestedPiece;

            synchronized (PeerProcess.peersList) {
                Iterator<PeerInfo> iterator = PeerProcess.peersList.iterator();
                while (iterator.hasNext()) {
                    peer = (PeerInfo) iterator.next();
                    System.out.println(peer.getPersPeerID());
                    if (peer.getPeerID() == peerID) {
                        requestingPeerID = peer.getPersPeerID();
                        socket = peer.getSock();
                        break;
                    }
                }
            }

            while (true) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                boolean fullFile = hasFullFile();

                if (fullFile) {
                    if (!Logs.Set) {
                        Logs.Set = true;
                        System.out.println("Finished Downloading.");
                        Logs.downloadCompletion();

                        Merger fileMerger = new Merger();
                        fileMerger.reassemble(requestingPeerID, fileSize, pieceSize, numberOfPieces);

                        for (PeerInfo p : PeerProcess.peersList) {
                            if (p.getPeerID() == requestingPeerID) {
                                System.out.println("Peer " + requestingPeerID + " is done. It is now a server.");
                                p.setChecker(true);
                            }
                        }

                        boolean checker = PeerProcess.peersList.stream().allMatch(p -> p.Checker());

                        if (checker) {
                            if (!Logs.Check) {
                                Logs.Check = true;

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                Logs.endLogging();
                            }
                            System.exit(0);
                        }

                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                } else {
                    if (peer.isInterested()) {
                        bitField = peer.getBitfield();
                        requestedPiece = getRequestedPiece(bitField, Bitfield.bitFieldBytes);

                        if (requestedPiece == 0) {
                            peer.setInterested(false);
                            NotInterested notInterestedMsg = new NotInterested();

                            synchronized (PeerProcess.msgPool) {
                                Message msg = new Message();
                                msg.setSock(socket);
                                msg.setMessage(notInterestedMsg.notInterestedMessageBytes);
                                PeerProcess.msgPool.add(msg);
                            }
                            flag = true;
                        } else {
                            Request request = new Request(requestedPiece);

                            synchronized (PeerProcess.msgPool) {
                                Message msg = new Message();
                                msg.setSock(socket);
                                msg.setMessage(request.requestMessage);
                                PeerProcess.msgPool.add(msg);
                            }
                        }
                    } else {
                        bitField = peer.getBitfield();
                        requestedPiece = getRequestedPiece(bitField, Bitfield.bitFieldBytes);

                        if (requestedPiece == 0) {
                            if (!flag) {
                                NotInterested notInterestedMsg = new NotInterested();

                                synchronized (PeerProcess.msgPool) {
                                    Message msg = new Message();
                                    msg.setSock(socket);
                                    msg.setMessage(notInterestedMsg.notInterestedMessageBytes);
                                    PeerProcess.msgPool.add(msg);
                                }
                            }
                        } else {
                            peer.setInterested(true);
                            flag = false;

                            Interested interestedMsg = new Interested();

                            synchronized (PeerProcess.msgPool) {
                                Message msgBody = new Message();
                                msgBody.setSock(socket);
                                msgBody.setMessage(interestedMsg.interestedMessageBytes);
                                PeerProcess.msgPool.add(msgBody);
                            }

                            Request request = new Request(requestedPiece);

                            synchronized (PeerProcess.msgPool) {
                                Message msgBody = new Message();
                                msgBody.setSock(socket);
                                msgBody.setMessage(request.requestMessage);
                                PeerProcess.msgPool.add(msgBody);
                            }
                        }
                    }
                }
            }

        }

        byte[] contentFull = new byte[5];

        for (int j = 0; j < contentFull.length - 1; j++) {
            contentFull[j] = 0;
        }

        contentFull[4] = 8;

        sendContentFull(contentFull);
    }

    private void sendContentFull(byte[] contentFull) {
        Iterator<PeerConnection> iterator = PeerProcess.hasFullFile.iterator();

        while (iterator.hasNext()) {
            PeerConnection peer = (PeerConnection) iterator.next();

            synchronized (PeerProcess.msgPool) {
                Message msg = new Message();
                msg.setSock(peer.getSocket());
                msg.setMessage(contentFull);
                PeerProcess.msgPool.add(msg);
            }
        }
    }

    private int getRequestedPiece(byte[] bitField, byte[] fileBitField) {
        int[] temporaryPieceList = new int[numberOfPieces];
        int k = 0;
        int missingPiecesCount = 0;
        int overflow = numberOfPieces % 8;

        for (int j = 5; j < fileBitField.length; j++) {
            int currFileBFBit = fileBitField[j];
            int currBitFieldBit = bitField[j];

            String binaryString1 = Integer.toBinaryString(currFileBFBit & 255 | 256).substring(1);
            char[] binaryCharArray1 = binaryString1.toCharArray();
            int[] bits1 = new int[8];

            for (int i = 0; i < binaryCharArray1.length; i++) {
                bits1[i] = binaryCharArray1[i] - '0';
            }

            String binaryString2 = Integer.toBinaryString(currBitFieldBit & 255 | 256).substring(1);
            char[] binaryCharArray2 = binaryString2.toCharArray();
            int[] bits2 = new int[8];

            for (int i = 0; i < binaryCharArray2.length; i++) {
                bits2[i] = binaryCharArray2[i] - '0';
            }

            if (j < (fileBitField.length - 1)) {
                for (int i = 0; i < bits2.length; i++) {
                    if (bits2[i] == 0 && bits1[i] == 1) {
                        temporaryPieceList[k] = 0;
                        k++;
                        missingPiecesCount++;
                    }

                    if (bits2[i] == 0 && bits1[i] == 0) {
                        temporaryPieceList[k] = 1;
                        k++;
                    }

                    if (bits2[i] == 1) {
                        temporaryPieceList[k] = 1;
                        k++;
                    }
                }
            } else {
                for (int i = 0; i < overflow; i++) {
                    if (bits2[i] == 0 && bits1[i] == 1) {
                        temporaryPieceList[k] = 0;
                        k++;
                        missingPiecesCount++;
                    }

                    if (bits2[i] == 0 && bits1[i] == 0) {
                        temporaryPieceList[k] = 1;
                        k++;
                    }

                    if (bits2[i] == 1) {
                        temporaryPieceList[k] = 1;
                        k++;
                    }
                }
            }
        }

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (missingPiecesCount == 0) {
            return 0;
        }

        int[] choices = new int[missingPiecesCount];

        int index = 0;
        for (int i = 0; i < temporaryPieceList.length; i++) {
            if (temporaryPieceList[i] == 0) {
                choices[index] = i;
                index++;
            }
        }

        Random rand = new Random();
        int randomChoice = rand.nextInt(missingPiecesCount);
        int requestedPiece = choices[randomChoice] + 1;
        System.out.println("Requested Piece " + requestedPiece);

        return requestedPiece;
    }

    private boolean hasFullFile() {
        boolean completeFlag = true;
        byte[] bitField = Bitfield.bitFieldBytes;

        for (int j = 5; j < bitField.length - 1; j++) {
            if (bitField[j] != -1) {
                completeFlag = false;
                break;
            }
        }

        if (completeFlag) {
            int overflowBits = numberOfPieces % 8;
            int lastByte = bitField[bitField.length - 1];
            String binaryString = Integer.toBinaryString(lastByte & 255 | 256).substring(1);
            char[] binaryCharArray = binaryString.toCharArray();
            int[] bits = new int[8];

            for (int j = 0; j < binaryCharArray.length; j++) {
                bits[j] = binaryCharArray[j] - '0';
            }

            for (int j = 0; j < overflowBits; j++) {
                if (bits[j] == 0) {
                    completeFlag = false;
                    break;
                }
            }
        }

        return completeFlag;
    }
}
