package process;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import peer.Message;
import peer.PeerProcess;

public class Sender extends Thread {

    private static final int SLEEP_TIME_MS = 10;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            processMessageQueue();
        }
    }

    private void processMessageQueue() {
        if (!PeerProcess.msgPool.isEmpty()) {
            sendMessageFromQueue();
        }
    }

    private void sendMessageFromQueue() {
        synchronized (PeerProcess.msgPool) {
            Message message = PeerProcess.msgPool.poll();
            if (message != null) {
                sendMessage(message);
            }
        }
    }

    private void sendMessage(Message message) {
        if (isValidMessage(message.getMessage())) {
            sendValidMessage(message);
        } else {
            handleInvalidMessage();
        }
    }

    private void sendValidMessage(Message message) {
        try {
            Socket socket = message.getSock();
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            synchronized (socket) {
                outputStream.writeObject(message.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleInvalidMessage() {
        System.out.println("Invalid message entered in pool");
    }

    private boolean isValidMessage(byte[] message) {
        int messageType = message[4];
        return messageType >= 0 && messageType < 9;
    }
}
