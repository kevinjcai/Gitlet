package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Kevin Cai
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current WORKING directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet/");

    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        Commit.COMMITS_DIR.mkdir();
        Commit.COMMIT_STORAGE.mkdir();
        BlobStage.BLOBS_DIR.mkdir();
        CommitTree.BRANCH_DIR.mkdir();
        Remote.REMOTE_DIR.mkdir();
        try {
            Commit.CURRENT.createNewFile();
            Commit.WORKING.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //@source: JavatPoint
    private static String time() {
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        formatter.setTimeZone((TimeZone.getTimeZone("PT")));
        String newDate = formatter.format(new Date());
        return newDate;
    }

    public static void createInit() {
        final File master = join(CommitTree.BRANCH_DIR, "master");
        try {
            master.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BlobStage.setUpstageBlob();
        BlobStage setUp = new BlobStage();
        setUp.setUpstorage();
        writeContents(Commit.CURRENT, "master");
        Commit intial = new Commit();
        intial.createInit();
    }

    public static void createCommit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        } else if (!BlobStage.getInstance().untrackedFileExists())  {
            System.out.println("No changes added to the commit.");
            return;
        }
        Commit create = CommitTree.getWorking();
        create.addFiles();
        create.saveCommit(message, time());
    }

    public static void add(String name) {
        BlobStage blobStage = BlobStage.getInstance();
        blobStage.add(name);
        blobStage.saveBlob();
    }

    public static void remove(String name) {
        BlobStage blobStage = BlobStage.getInstance();
        blobStage.remove(name);
        blobStage.saveBlob();
    }

    public static void fileCheckout(String name) {
        CommitTree.fileCheckout(name);
    }

    public static void fileCheckout(String id, String name) {
        CommitTree.fileCheckout(id, name);
    }

    public static void branchCheckout(String name) {
        CommitTree.branchCheckout(name);
    }

    public static void reset(String name) {
        CommitTree.reset(name);
    }


    public static void log() {
        CommitTree.log();
    }

    public static void globalLog() {
        CommitTree.globalLog();
    }

    public static void find(String message) {
        CommitTree.find(message);
    }

    public static void branch(String name) {
        CommitTree.branch(name);
    }

    public static void rmBranch(String name) {
        CommitTree.rmBranch(name);
    }

    public static void status() {
        CommitTree.status();
    }

    public static void merge(String name) {
        CommitTree.merge(name);
    }

    public static void addRemote(String name, String pName) {
        Remote.addRemote(name, pName);
    }

    public static void rmRemote(String name) {
        Remote.rmRemote(name);
    }
}

