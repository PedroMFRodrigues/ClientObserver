package ClenteObserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Observable;


public class GetFiles extends Observable implements Runnable {

    public static final int MAX_SIZE = 4000;
    public static final int TIMEOUT = 5 * 1000; //segundos
    public String fileName;
    public File localDirectory;
    int serverPort;
    Messages messageToSend;
    String ip;
    
    public GetFiles(String file, File directory,String port,String ip) {
        fileName = file;
        localDirectory = directory;
        serverPort = Integer.parseInt(port);
        this.ip=ip;
        messageToSend=new Messages("DOWNLOAD");
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
        String localFilePath = null;
        FileOutputStream localFileOutputStream = null;
  
        Socket socketToServer = null;
        PrintWriter pout;
        InputStream in;
        byte[] fileChunk = new byte[MAX_SIZE];
        int nbytes;
        int contador = 0;        

        try {

            try {
                this.SetMessage("A criar Ficheiro","Ficheiro " + localFilePath + " criado.");
                
                localFilePath = localDirectory.getCanonicalPath() + File.separator + fileName;
                localFileOutputStream = new FileOutputStream(localFilePath);
                

            } catch (IOException e) {

                if (localFilePath == null) {
                     this.SetMessage("Erro","Ocorreu a excepcao {" + e + "} ao obter o caminho canonico para o ficheiro local!");
                } else {
                    this.SetMessage("Erro","Ocorreu a excepcao {" + e + "} ao tentar criar o ficheiro " + localFilePath + "!");
                }

                return;
            }

            try {
                socketToServer = new Socket(ip, serverPort);

                socketToServer.setSoTimeout(TIMEOUT );

                in = socketToServer.getInputStream();
                pout = new PrintWriter(socketToServer.getOutputStream(), true);

                pout.println(fileName);
                pout.flush();

                while ((nbytes = in.read(fileChunk)) > 0) {
                    localFileOutputStream.write(fileChunk, 0, nbytes);
                }

                 this.SetMessage("Fim","Transferencia concluida.");

            } catch (UnknownHostException e) {
                 this.SetMessage("Erro","Destino desconhecido:\n\t" + e);
            } catch (NumberFormatException e) {
                 this.SetMessage("Erro","O porto do servidor deve ser um inteiro positivo:\n\t" + e);
            } catch (SocketTimeoutException e) {
                 this.SetMessage("Erro","Não foi recebida qualquer bloco adicional, podendo a transferencia estar incompleta:\n\t" + e);
            } catch (SocketException e) {
                 this.SetMessage("Erro","Ocorreu um erro ao nível do socket TCP:\n\t" + e);
            } catch (IOException e) {
                 this.SetMessage("Erro","Ocorreu um erro no acesso ao socket ou ao ficheiro local " + localFilePath + ":\n\t" + e);
            }

        } finally {

            if (socketToServer != null) {
                try {
                    socketToServer.close();
                } catch (IOException ex) {
                }
            }

            if (localFileOutputStream != null) {
                try {
                    localFileOutputStream.close();
                } catch (IOException e) {
                }
            }

        }

    }
}
