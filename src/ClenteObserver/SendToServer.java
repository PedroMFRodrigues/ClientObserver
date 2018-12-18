package ClenteObserver;

import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendToServer extends Observable implements Runnable {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 5 * 1000; //segundos
    protected Socket serverSocket;
    String serverAddr;
    int serverPort;

    MessageToServer msg;
    Messages messageToSend;

    public SendToServer(String ip, int port, MessageToServer message) {
        serverAddr = ip;
        serverPort = port;
        messageToSend = new Messages("SendToServer");
        msg = message;
    }

    public void SetMessage(String subject, String message) {
        this.messageToSend.message = message;
        this.messageToSend.subject = subject;
        setChanged();
        notifyObservers(messageToSend);
    }

    public void processRequests(){
        ObjectOutputStream out;

        if (serverSocket == null) {
            this.SetMessage("Erro", "Erro na Criação do Socket");
            return;
        }

        this.SetMessage("Inicio", "Servidor de carregamento de ficheiros iniciado...");

        try {
            InetAddress serverAddress;
        
            serverAddress = InetAddress.getByName(serverAddr);
        
            serverSocket = new Socket(serverAddress, serverPort);

            serverSocket.setSoTimeout(TIMEOUT);
            out = new ObjectOutputStream(serverSocket.getOutputStream());

            out.writeObject(out);

            out.writeObject(msg);
            out.flush();

        } catch (UnknownHostException ex) {
            this.SetMessage("Erro", "Enviada Mensagem ao Servidor");
        } catch (IOException ex) {
            Logger.getLogger(SendToServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.SetMessage("Fim", "Enviada Mensagem ao Servidor");
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        this.SetMessage("Inicio", "A iniciar Comunicação");

        try {
            this.processRequests();

        } catch (NumberFormatException e) {
            this.SetMessage("Erro", "O porto de escuta deve ser um inteiro positivo.");
        } catch (Exception e) {
            this.SetMessage("Erro", "Ocorreu uma excepcao ao nivel do socket TCP:\n\t" + e);
        }
    }
}
