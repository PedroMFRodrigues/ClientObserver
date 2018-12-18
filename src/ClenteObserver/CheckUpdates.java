package ClenteObserver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Observable;

public class CheckUpdates extends Observable implements Runnable {
    public String localDirToWatch;
    Messages messageToSend;

    public CheckUpdates(String directory) {
        localDirToWatch = directory;
        messageToSend = new Messages("DIRUPDATED");
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
        Path myDir = Paths.get(localDirToWatch);

        try {
            WatchService watcher = myDir.getFileSystem().newWatchService();
            myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey watckKey = watcher.take();

            List<WatchEvent<?>> events = watckKey.pollEvents();
            for (WatchEvent event : events) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    this.SetMessage("Change","Created: " + event.context().toString());
                }
                if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    this.SetMessage("Change","Delete: " + event.context().toString());
                }
                if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    this.SetMessage("Change","Modify: " + event.context().toString());
                }
            }

        } catch (Exception e) {
            this.SetMessage("Error","Error: " + e.toString());
        }
    }
}
