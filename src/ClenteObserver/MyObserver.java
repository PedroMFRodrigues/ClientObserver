package ClenteObserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

class MyObserver implements Observer {

    ArrayList<String> history;
    
    //////////My Server Connection//////////
    String serverIp;
    int serverPort;
    
    //////////My Server Receive Ping//////////
    String receiveIp;
    int receivePort;

    //////////////Message Receive///////////////
    MulticastSocket mskt;
    //////////////Ping Replay//////////////
    Socket socket;

    List<String> messages;
    int showMessages = 0;

    public MyObserver() throws UnknownHostException {
        this.receiveIp = (InetAddress.getLocalHost()).toString();
        this.receivePort = 5000;
    }

    public boolean initConnection(String ip, int port) {
        this.serverIp = "192.168.1.7";
        this.serverPort = 5000;
        return true;
    }

    public void StartConnection(String localDirToSend) {
        File folder = new File(localDirToSend);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> files = new ArrayList<String>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                files.add(listOfFiles[i].getName());
            }
        }

        MessageToServer msg = new MessageToServer(receiveIp, serverIp, "List Of Files", files);

        SendToServer(msg);
    }

    public void initPindReply() {
        try {
            mskt = new MulticastSocket(5555);
            InetAddress group = InetAddress.getByName(receiveIp);
            PingReply pingReply = new PingReply(mskt, group);
            pingReply.addObserver(this);
            pingReply.start();
        } catch (IOException ex) {
            Logger.getLogger(MyObserver.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void initMsgReception() {
        socket = new Socket();
        ReceiveFromServer recFromServ = new ReceiveFromServer(socket);
        recFromServ.addObserver(this);
        recFromServ.start();
    }

    public void SendToServer(MessageToServer msg) {
        SendToServer sendToServer = new SendToServer(serverIp, serverPort, msg);
        sendToServer.addObserver(this);
        sendToServer.start();
    }

    public void startFileWatch(String localDerectory) {
        CheckUpdates checkUpdates = new CheckUpdates(localDerectory);
        checkUpdates.addObserver(this);
        checkUpdates.start();
    }

    public void startDownload(String file, File localDerectory, String port, String ip) {
        System.err.println(file + " " + port + " " + ip);

        GetFiles dowloadfile = new GetFiles(file, localDerectory, port, ip);
        dowloadfile.addObserver(this);
        dowloadfile.start();
    }

    public int ProvideUpload(String port, File localDerectory) {
        try {
            ServerSocket servSocket = new ServerSocket(5000);
            UpLoadFiles upLoadFile = new UpLoadFiles(servSocket, localDerectory);
            upLoadFile.addObserver(this);
            upLoadFile.start();

            return servSocket.getLocalPort();
        } catch (IOException e) {
            System.out.println("Erro ao iniciar Download");
            return 0;
        }
    }

    public void CloseConnections() {
        try {
            if (!mskt.isClosed()) {
                mskt.close();
            }
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        Messages m = (Messages) arg;
        System.out.println(m.toString());
    }

}
