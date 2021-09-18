package gitlet;

import java.io.Serializable;
import java.util.*;
import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

public class BlobStage implements Serializable {

    public static final File BLOBS_DIR = join(Repository.GITLET_DIR, "Blobs");
    public static final File VERSIONSTORAGE = join(BLOBS_DIR, "File_Storage");
    /**
     * Map<File Hash, File Values>
     * Stores the Hash values of the different versions of the files
     * */
    private Map<String, File> blobDatabase = new TreeMap<>();
    /** Map<File Name, File Hash>*/
    private Map<String, String> trackFiles = new TreeMap<>();
    /** Map<File Name, File Hash>*/
    private Collection<String> rmFiles = new ArrayList<>();

    private Map<String, String> untrackedFiles = new HashMap<>();

    private Map<String, String> modifiedFiles = new TreeMap<>();

    public static void setUpstageBlob() {
        try {
            VERSIONSTORAGE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUpstorage() {
        Utils.writeObject(VERSIONSTORAGE, this);
    }

    public void add(String name) {
        File fName = join(Repository.CWD, name);
        Commit working = CommitTree.getWorking();
        if (!fName.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String fSha1 = Utils.sha1(Utils.readContents(fName));
        File addFile = join(BLOBS_DIR, fSha1);
        if (!addFile.exists()) {
            try {
                addFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] fByte = readContents(fName);
            writeContents(addFile, fByte);
        }
        if (rmFiles.contains(name)) {
            rmFiles.remove(name);
            working.add(name, fSha1);
            blobDatabase.put(fSha1, addFile);
        } else if (!(fSha1.equals(working.get(name)))) {
            trackFiles.put(name, fSha1);
            working.add(name, fSha1);
            blobDatabase.put(fSha1, addFile);
        }
        working.saveWorking();
        saveBlob();
    }

    public static void clearStage() {
        BlobStage stage = getInstance();
        stage.trackFiles.clear();
        stage.rmFiles.clear();
        stage.saveBlob();
    }

    /** True means there are untracked files*/
    public boolean untrackedFileExists() {
        return !(trackFiles.isEmpty() && rmFiles.isEmpty());
    }

    public void tracker() {
        untrackedFiles.clear();
        modifiedFiles.clear();
        HashMap<String, String> cwdFiles = new HashMap<>();
        Commit working = CommitTree.getWorking();
        Commit currentHead = CommitTree.currentCommit();
        for (String fName : plainFilenamesIn(Repository.CWD)) {
            File cwdFile = join(Repository.CWD, fName);
            String fSha1 = Utils.sha1(Utils.readContents(cwdFile));
            cwdFiles.put(fName, fSha1);
            if (!working.containsKey(fName)) {
                untrackedFiles.put(fName, null);
                continue;
            }
            if (!working.get(fName).equals(fSha1)) {
                modifiedFiles.put(fName, "(modified)");
            }
        }
        for (String fName : working.getFiles().keySet()) {
            if (!cwdFiles.containsKey(fName)) {
                modifiedFiles.put(fName, "(deleted)");
            }
        }
        saveBlob();
    }

    public void remove(String name) {
        Commit headCommit = CommitTree.currentCommit();
        Commit work = CommitTree.getWorking();
        File rmFile = join(Repository.CWD, name);
        if (headCommit.containsKey(name)) {
            work.remove(name);
            rmFiles.add(name);
            rmFile.delete();
            trackFiles.remove(name);
            work.saveWorking();
            saveBlob();
            return;
        } else if (trackFiles.containsKey(name)) {
            trackFiles.remove(name);
            work.remove(name);
            work.saveWorking();
            saveBlob();
            return;
        }
        System.out.println("No reason to remove the file.");
    }

    public Map<String, String> putCommit(Map<String, String> commitFiles) {
        rmFiles.clear();
        trackFiles.clear();
        saveBlob();
        return commitFiles;
    }

    public static Set<String> getStaged() {
        return BlobStage.getInstance().trackFiles.keySet();
    }

    public static Collection<String> getRemoved() {
        return BlobStage.getInstance().rmFiles;
    }

    public static Map<String, String> getUntracked() {
        return BlobStage.getInstance().untrackedFiles;
    }

    public static Map<String, String> getModified() {
        return BlobStage.getInstance().modifiedFiles;
    }


    public void saveBlob() {
        Utils.writeObject(VERSIONSTORAGE, this);
    }

    public File get(String name) {
        return blobDatabase.get(name);
    }

    public static BlobStage getInstance() {
        return readObject(BlobStage.VERSIONSTORAGE, BlobStage.class);
    }
}
