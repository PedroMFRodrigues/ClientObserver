package ClenteObserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String command;
        MyObserver observer = new MyObserver();
        boolean cicle = true;

        String folderToReceive = scanner.nextLine();
        String folderToSend = scanner.nextLine();

        File localDirToReceive = new File(folderToReceive);
        File localDirToSend = new File(folderToSend);

        if (!localDirToReceive.exists()) {
            System.err.println("A directoria " + localDirToReceive + " nao existe!");
            return;
        }

        if (!localDirToReceive.isDirectory()) {
            System.err.println("O caminho " + localDirToReceive + " nao se refere a uma directoria!");
            return;
        }

        if (!localDirToReceive.canWrite()) {
            System.err.println("Sem permissoes de escrita na directoria " + localDirToReceive);
            return;
        }
        if (!localDirToSend.exists()) {
            System.err.println("A directoria " + localDirToReceive + " nao existe!");
            return;
        }

        if (!localDirToSend.isDirectory()) {
            System.err.println("O caminho " + localDirToReceive + " nao se refere a uma directoria!");
            return;
        }

        if (!localDirToSend.canRead()) {
            System.err.println("Sem permissoes de leitura na directoria " + localDirToReceive);
            return;
        }
        
        observer.startFileWatch(folderToSend);
        observer.initMsgReception();
        observer.initPindReply();

        while (cicle) {
            System.out.println("Command:");
            command = (scanner.nextLine());
            String[] parts = command.split(" ");

            System.out.println(parts[0]);

            try {
                switch (parts[0]) {
                    case "download":
                        observer.startDownload(parts[1], localDirToReceive, parts[2], parts[3]);
                        break;
                    case "upload":
                        int port = observer.ProvideUpload(parts[1], localDirToSend);
                        break;
                    case "exit":
                        cicle = false;
                        break;
                    default:
                }
            } catch (Exception e) {
            }
        }

    }

    public static ArrayList<String> getListOfFiles(File folder) {
        ArrayList<String> fileList = new ArrayList<String>();
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileList.add(listOfFiles[i].getName());
            }
        }

        return fileList;
    }
}



class Messages {

    String message;
    String from;
    String subject;

    public Messages(String message, String from, String subject) {
        this.message = message;
        this.from = from;
        this.subject = subject;
    }

    Messages(String from) {
        this.message = "";
        this.from = from;
        this.subject = "";
    }

    @Override
    public String toString() {
        String aux;
        aux = "Subject: " + this.subject + " From: " + this.from + " Message: " + this.message;

        return aux;
    }

}
