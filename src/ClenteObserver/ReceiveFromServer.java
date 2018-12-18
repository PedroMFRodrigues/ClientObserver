package ClenteObserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;

public class ReceiveFromServer extends Observable implements Runnable {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 5 * 1000; //segundos
    protected Socket serverSocket;

    MessageToServer msg;
    Messages messageToSend;

    public ReceiveFromServer(Socket srvSocket) {
        messageToSend = new Messages("RECEIVEFROMSERVER");
        serverSocket= srvSocket;
    }

    public void SetMessage(String subject, String message) {
        this.messageToSend.message = message;
        this.messageToSend.subject = subject;
        setChanged();
        notifyObservers(messageToSend);
    }

    public void SetMessage(MessageToServer msg) {        
        this.msg=msg;
        setChanged();
        notifyObservers(msg);
    }

    public void processMessage() {
        ObjectInputStream in;

        if (serverSocket == null) {
            this.SetMessage("Erro", "Erro na Criação do Socket");
            return;
        }

        this.SetMessage("Inicio", "Iniciar Ciclo de Ler de Socket");

        try {
            while (true) {
                in = new ObjectInputStream(serverSocket.getInputStream());

                msg = (MessageToServer) in.readObject();

                this.SetMessage(msg);
            }
        } catch (UnknownHostException ex) {
            this.SetMessage("Erro", "UnknownHostException Erro a Receber Mensagem" + ex);
        } catch (ClassNotFoundException ex) {
            this.SetMessage("Erro", "ClassNotFoundException Erro a Receber Mensagem" + ex);
        } catch (IOException ex) {
            this.SetMessage("Erro", "IOException Erro a Receber Mensagem" + ex);
        } finally {
            this.SetMessage("Fim", "A Encerrar Recepcao de Mensagens");
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
            this.processMessage();

        } catch (NumberFormatException e) {
            this.SetMessage("Erro", "O porto de escuta deve ser um inteiro positivo.");
        } catch (Exception e) {
            this.SetMessage("Erro", "Ocorreu uma excepcao ao nivel do socket TCP:\n\t" + e);
        }
    }
}
