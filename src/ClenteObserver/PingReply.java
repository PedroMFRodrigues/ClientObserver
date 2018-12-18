package ClenteObserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;

public class PingReply extends Observable implements Runnable {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 5 * 1000; //segundos

    InetAddress group;
    MulticastSocket skt;

    Messages messageToSend;

    public PingReply(MulticastSocket s,InetAddress g) throws IOException {
        skt = s;        
        skt.joinGroup(g);
        messageToSend = new Messages("UPLOADFILE");
    }

    public void SetMessage(String subject, String message) {
        this.messageToSend.message = message;
        this.messageToSend.subject = subject;
        setChanged();
        notifyObservers(messageToSend);
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        DatagramPacket pkt;
        String msg;
        String ping = "ping";

        if (skt == null) {
            return;
        }

        try {

            while (true) {
                pkt = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                skt.receive(pkt);

                msg = new String(pkt.getData(), 0, pkt.getLength());

                if (msg.toUpperCase().contains("ping")) {
                    pkt.setData(ping.getBytes());
                    pkt.setLength(ping.length());
                    skt.send(pkt);
                    continue;
                }
            }

        } catch (IOException e) {

            if (!skt.isClosed()) {
                skt.close();
            }

        }
    }
}
