package ClenteObserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Observable;

public class UpLoadFiles extends Observable implements Runnable {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 5*1000; //segundos

    protected ServerSocket serverSocket;
    protected File localDirectory;
    
    Messages messageToSend;

    public UpLoadFiles(ServerSocket socket, File localDirectory) throws SocketException, IOException {
        serverSocket = null;
        serverSocket = socket;
        this.localDirectory = localDirectory;
        messageToSend = new Messages("UPLOADFILE");
    }

    public void SetMessage(String subject, String message) {
        this.messageToSend.message = message;
        this.messageToSend.subject = subject;
        setChanged();
        notifyObservers(messageToSend);
    }

    public void processRequests() {
        BufferedReader in;
        OutputStream out;
        Socket socketToClient;
        byte[] fileChunk = new byte[MAX_SIZE];
        int nbytes;
        String requestedFileName, requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;

        if (serverSocket == null) {
            return;
        }

        this.SetMessage("Inicio","Servidor de carregamento de ficheiros iniciado...");

        try {

            while (true) {

                try {

                    socketToClient = serverSocket.accept();

                } catch (IOException e) {
                    this.SetMessage("Erro","Ocorreu uma excepcao no socket enquanto aguardava por um pedido de ligacao: \n\t" + e);                    
                    return;
                }

                try {

                    socketToClient.setSoTimeout( TIMEOUT);

                    in = new BufferedReader(new InputStreamReader(socketToClient.getInputStream()));
                    out = socketToClient.getOutputStream();

                    requestedFileName = in.readLine();

                    notifyObservers("Recebido pedido para: " + requestedFileName);

                    requestedCanonicalFilePath = new File(localDirectory + File.separator + requestedFileName).getCanonicalPath();

                    if (!requestedCanonicalFilePath.startsWith(localDirectory.getCanonicalPath() + File.separator)) {
                        this.SetMessage("Erro","Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                        continue;
                    }

                    requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
                    System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");

                    while ((nbytes = requestedFileInputStream.read(fileChunk)) > 0) {
                        out.write(fileChunk, 0, nbytes);
                        out.flush();
                    }

                    this.SetMessage("Fim","Transferencia concluida");

                } catch (FileNotFoundException e) {   //Subclasse de IOException                 
                    this.SetMessage("Erro","Ocorreu a excepcao {" + e + "} ao tentar abrir o ficheiro " + requestedCanonicalFilePath + "!");
                } catch (IOException e) {
                    this.SetMessage("Erro","Ocorreu a excepcao de E/S: \n\t" + e);
                }

                if (requestedFileInputStream != null) {
                    try {
                        requestedFileInputStream.close();
                    } catch (IOException ex) {
                    }
                }

                try {
                    socketToClient.close();
                } catch (IOException e) {
                }

            } //while(true)

        } finally {
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
        this.SetMessage("Inicio", "A iniciar envio de ficheiro");

        if (!localDirectory.exists()) {
            this.SetMessage("Erro", "A directoria " + localDirectory + " nao existe!");
            return;
        }

        if (!localDirectory.isDirectory()) {
            this.SetMessage("Erro", "O caminho " + localDirectory + " nao se refere a uma directoria!");
            return;
        }

        if (!localDirectory.canRead()) {
            this.SetMessage("Erro", "Sem permissoes de leitura na directoria " + localDirectory + "!");
            return;
        }

        try {

            this.processRequests();

        } catch (NumberFormatException e) {
            this.SetMessage("Erro", "O porto de escuta deve ser um inteiro positivo.");
        } catch (Exception e) {
            this.SetMessage("Erro", "Ocorreu uma excepcao ao nivel do socket TCP:\n\t" + e);
        }
    }
}
