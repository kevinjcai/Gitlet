package gitlet;

import java.io.File;
import java.io.IOException;


import static gitlet.Utils.*;

public class Remote {
    public static final File REMOTE_DIR = join(Repository.CWD, "remote.dir");

    public static void addRemote(String name, String pName) {
        String pathName = pName.replace('/', File.separatorChar);
        File remoteFile = join(REMOTE_DIR, name);
        if (remoteFile.exists()) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        try {
            remoteFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeContents(remoteFile, pathName);
    }

    public static void rmRemote(String name) {
        File remoteFile = join(REMOTE_DIR, name);
        if (!remoteFile.exists()) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
        remoteFile.delete();
    }
}
