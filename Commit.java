package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Kevin Cai
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    public static final File COMMITS_DIR = join(Repository.GITLET_DIR, "Commits");
    /** Current is the pointer to the current head commit*/
    public static final File CURRENT = join(COMMITS_DIR, "CURRENT");
    /** Working is the commit that is currently being staged for addition or removal*/
    public static final File WORKING = join(COMMITS_DIR, "working");


    public static final File COMMIT_STORAGE = join(COMMITS_DIR, "COMMIT_STORAGE");
    /** The time of this Commit. */
    private String timestamp;
    /** The message of this Commit. */
    private String message;
    /** The previous commit of this Commit. */
    private ArrayList<String> parentSha;
    /** The Sha-1 Hash of this Commit. */
    private String sha1;
    /** Map<File Name, Sha-1 Hash of the File*/
    private Map<String, String> files = new TreeMap<>();

    private ArrayList<Commit> parent;
    /**
     * Intital Commit Constructor
     */
    public Commit() {
        parentSha = new ArrayList<>();
        parent = new ArrayList<>();
        parent.add(null);
        parentSha.add(null);
    }

    public Commit(String parentId, Commit parentCommit) {
        parentSha = new ArrayList<>();
        parent = new ArrayList<>();
        parentSha.add(parentId);
        parent.add(parentCommit);
    }

    public void addFiles() {
        BlobStage blobStage = BlobStage.getInstance();
        files = blobStage.putCommit(files);
    }

    public Map<String, String> getFiles() {
        return this.files;
    }

    public void add(String name, String hash) {
        this.files.put(name, hash);
    }

    public void remove(String name) {
        this.files.remove(name);
    }

    public String calcSHA() {
        return Utils.sha1(Utils.serialize(this));
    }

    public String getSHA() {
        return this.sha1;
    }

    public String getFilesha(String name) {
        return files.get(name);
    }

    public File nameFile(String name) {
        BlobStage stage = BlobStage.getInstance();
        return stage.get(getFilesha(name));
    }

    public byte[] fileBytes(String name) {
        return readContents(nameFile(name));
    }

    public Boolean containsKey(String name) {
        return files.containsKey(name);
    }

    public Boolean containsValue(String name) {
        return files.containsValue(name);
    }

    public String get(String key) {
        return files.get(key);
    }

    public String parentHash() {
        return parentSha.get(0);
    }

    public Commit getParent() {
        return parent.get(0);
//        File parent = join(COMMIT_STORAGE, parentHash());
//        Commit parentCommit = Utils.readObject(parent, Commit.class);
//        return parentCommit;
    }

    public ArrayList<Commit> parentList() {
        return this.parent;
    }

    public String getTime() {
        return this.timestamp;
    }

    public String getMessage() {
        return this.message;
    }

    public void createInit() {
        message = "initial commit";
        timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        sha1 = calcSHA();
        File commitName = Utils.join(COMMIT_STORAGE, this.sha1);
        try {
            commitName.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(commitName, this);
        writeObject(CommitTree.currentFile(), this);
        Commit work = new Commit(sha1, this);
        Utils.writeObject(WORKING, work);
    }

    public void saveCommit(String msg, String time) {
        this.timestamp = time;
        this.message = msg;
        this.sha1 = this.calcSHA();
        File commitName = Utils.join(COMMIT_STORAGE, this.sha1);
        try {
            commitName.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(commitName, this);
        File currentHead = CommitTree.currentFile();
        Utils.writeObject(currentHead, this);
        Commit work = new Commit(this.sha1, this);
        work.files.putAll(this.files);
        Utils.writeObject(WORKING, work);
    }

    public void mergeHelper(Commit secondParent) {
        parent.add(secondParent);
        parentSha.add(secondParent.getSHA());
        saveWorking();
    }

    public void saveWorking() {
        Utils.writeObject(WORKING, this);
    }

    public String toString() {
        StringBuffer logMessage = new StringBuffer();
        logMessage.append("===" + "\n");
        logMessage.append("commit " + this.getSHA() + "\n");
        if (parent.size() > 1) {
            logMessage.append("Merge: " + parent.get(0).getSHA().substring(0, 7)
                    + " " + parent.get(1).getSHA().substring(0, 7) + "\n");
        }
        logMessage.append("Date: " + this.getTime() + "\n");
        logMessage.append(this.getMessage() + "\n");
        return logMessage.toString();
    }
}
