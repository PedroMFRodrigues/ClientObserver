/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClenteObserver;

import java.io.Serializable;
import static java.nio.file.Files.list;
import static java.rmi.Naming.list;
import java.util.ArrayList;
import static java.util.Collections.list;

/**
 *
 * @author PedroRodrigues
 */
public class MessageToServer implements Serializable{
    String from;
    String subject;
    String message;
    ArrayList<String> files;
    
    public MessageToServer (String from, String subject, String message,ArrayList<String> files){
        this.from=from;
        this.subject=subject;
        this.message=message;
        this.files=files;
    }
}
