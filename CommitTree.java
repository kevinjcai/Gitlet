package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Commit.*;
import  static gitlet.Repository.*;

public class CommitTree {

    public static final File BRANCH_DIR = join(COMMITS_DIR, "BRANCH_DIR");

    public static File getBranchfile(String name) {
        return join(BRANCH_DIR, name);
    }
    
    public static Commit branchCommit(String name) {
        return readObject(getBranchfile(name), Commit.class);
    }

    public static File currentFile() {
        return getBranchfile(currentPointer());
    }

    public static String currentPointer() {
        return readContentsAsString(CURRENT);
    }

    public static Commit currentCommit() {
        return readObject(currentFile(), Commit.class);
    }

    public static String getCurrenthash() {
        return currentCommit().getSHA();
    }

    public static Commit getWorking() {
        return readObject(WORKING, Commit.class);
    }

    public static Commit getCommit(String shaID) {
        if (shaID == null) {
            return null;
        }
        if (shaID.length() < 40) {
            return abbreviatedCommit(shaID);
        }
        File commit = Utils.join(COMMIT_STORAGE, shaID);
        if (commit.exists()) {
            return readObject(commit, Commit.class);
        }
        return null;
    }

    public static Commit abbreviatedCommit(String shaID) {
        for (String x : plainFilenamesIn(COMMIT_STORAGE)) {
            if (x.startsWith(shaID)) {
                File commit = Utils.join(COMMIT_STORAGE, x);
                return readObject(commit, Commit.class);
            }
        }
        return null;
    }

//    public static String abbreviatedID(String shaID) {
//        for (String x : plainFilenamesIn(COMMIT_STORAGE)) {
//            if (x.substring(0, shaID.length()).equals(shaID)) {
//                return x;
//            }
//        }
//        return null;
//    }

    public static void fileCheckout(String name) {
        fileCheckout(CommitTree.getCurrenthash(), name);
    }

    public static void fileCheckout(String id, String name) {
        Commit head = getCommit(id);
        if (head == null) {
            System.out.println("No commit with that id exists.");
        } else if (head.containsKey(name)) {
            File working = join(Repository.CWD, name);
            File nameSha = head.nameFile(name);
            writeContents(working, readContents(nameSha));
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public static void branchCheckout(String name) {
        File branch = join(BRANCH_DIR, name);
        if (!branch.exists()) {
            System.out.println("No such branch exists.");
            return;
        } else if (readContentsAsString(CURRENT).equals(name)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit bCommit = branchCommit(name);
        Commit currentCommit = currentCommit();
        for (String fName : plainFilenamesIn(Repository.CWD)) {
            if (bCommit.containsKey(fName) && !currentCommit.containsKey(fName)) {
                System.out.println("There is an untracked file in the way; delete it"
                        + ", or add and commit it first.");
                return;
            }
        }
        for (String fName : currentCommit.getFiles().keySet()) {
            if (!bCommit.containsKey(fName)) {
                File cwdFile = join(CWD, fName);
                cwdFile.delete();
            }
        }
        for (String fName : bCommit.getFiles().keySet()) {
            File cwdFile = join(CWD, fName);
            File branchFile = bCommit.nameFile(fName);
            if (!join(CWD, fName).exists()) {
                try {
                    cwdFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeContents(cwdFile, readContents(branchFile));
        }
        writeContents(CURRENT, name);
        Commit newWork = new Commit(getCurrenthash(), bCommit);
        newWork.getFiles().putAll(currentCommit().getFiles());
        Utils.writeObject(WORKING, newWork);
        BlobStage.clearStage();
    }

    public static void reset(String id) {
        Commit resetPoint = getCommit(id);
        if (resetPoint == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit prevHead = currentCommit();
        File head = currentFile();
        writeObject(head, resetPoint);
        for (String fName : plainFilenamesIn(Repository.CWD)) {
            if (resetPoint.containsKey(fName) && !prevHead.containsKey(fName)) {
                System.out.println("There is an untracked file in the way; delete it"
                            + ", or add and commit it first.");
                return;
            }
        }
        for (String fName : prevHead.getFiles().keySet()) {
            if (!resetPoint.containsKey(fName)) {
                File cwdFile = join(CWD, fName);
                cwdFile.delete();
            }
        }
        for (String fName : resetPoint.getFiles().keySet()) {
            File cwdFile = join(CWD, fName);
            File branchFile = resetPoint.nameFile(fName);
            if (!join(CWD, fName).exists()) {
                try {
                    cwdFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeContents(cwdFile, readContents(branchFile));
        }
        Commit newWork = new Commit(getCurrenthash(), resetPoint);
        newWork.getFiles().putAll(currentCommit().getFiles());
        Utils.writeObject(WORKING, newWork);
        BlobStage.clearStage();
    }

    public static void log() {
        Commit head = CommitTree.currentCommit();
        while (head != null) {
            System.out.print(head.toString() + "\n");
            head = head.getParent();
        }
    }

    public static void globalLog() {
        List<String> commitList = plainFilenamesIn(COMMIT_STORAGE);
        for (String name : commitList) {
            Commit head = getCommit(name);
            System.out.println(head.toString());
        }
    }

    public static void find(String message) {
        List<String> commitList = plainFilenamesIn(COMMIT_STORAGE);
        Boolean found = false;
        for (String name : commitList) {
            Commit head = getCommit(name);
            if (head.getMessage().equals(message)) {
                found = true;
                System.out.println(head.getSHA());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void statusHeader(String message) {
        System.out.println("=== " + message + " ===");
    }

    public static void status() {
        statusHeader("Branches");
        BlobStage stage = BlobStage.getInstance();
        stage.tracker();
        List files = plainFilenamesIn(BRANCH_DIR);
        for (String branch : plainFilenamesIn(BRANCH_DIR)) {
            if ((readContentsAsString(CURRENT).equals(branch))) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.print("\n");
        statusHeader("Staged Files");
        for (String fName : BlobStage.getStaged()) {
            System.out.println(fName);
        }
        System.out.print("\n");
        statusHeader("Removed Files");
        for (String fName : BlobStage.getRemoved()) {
            System.out.println(fName);
        }
        System.out.print("\n");
        statusHeader("Modifications Not Staged For Commit");
        for (String fName : BlobStage.getModified().keySet()) {
            System.out.println(fName + " " + BlobStage.getModified().get(fName));
        }
        System.out.print("\n");
        statusHeader("Untracked Files");
        for (String fName : BlobStage.getUntracked().keySet()) {
            System.out.println(fName);
        }
        System.out.print("\n");
    }

    public static void branch(String name) {
        File branch = join(BRANCH_DIR, name);
        if (branch.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        try {
            branch.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeObject(branch, currentCommit());
    }

    public static void rmBranch(String name) {
        File branch = join(BRANCH_DIR, name);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (name.equals(currentPointer())) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branch.delete();
    }
    
    public static boolean mergeError(String name) {
        if (BlobStage.getInstance().untrackedFileExists()) {
            System.out.println("You have uncommitted changes.");
            return true;
        } else if (!join(BRANCH_DIR, name).exists()) {
            System.out.println("A branch with that name does not exist.");
            return true;
        } else if (name.equals(currentPointer())) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        return false;
    }

    public static boolean mergeError2(Commit head, Commit branch, Commit split, String bName) {
        if (head.getSHA().equals(branch.getSHA()) || split.getSHA().equals(branch.getSHA())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return true;
        }
        if (split.getSHA().equals(head.getSHA())) {
            System.out.print("Current branch fast-forwarded.");
            branchCheckout(bName);
            return true;
        }
        for (String fName : plainFilenamesIn(Repository.CWD)) {
            if (!split.containsKey(fName) && !head.containsKey(fName)
                    && branch.containsKey(fName)) {
                System.out.println("There is an untracked file in the way; delete it"
                        + ", or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    public static String fileMerger(Commit head, Commit branch, String fName) {
        StringBuffer mergedFiles = new StringBuffer();
        mergedFiles.append("<<<<<<< HEAD" + "\n");
        if (head.containsKey(fName)) {
            String headFile = readContentsAsString(head.nameFile(fName));
            mergedFiles.append(headFile);
        } else {
            mergedFiles.append("");
        }
        mergedFiles.append("=======" + "\n");
        if (branch.containsKey(fName)) {
            String branchFile = readContentsAsString(branch.nameFile(fName));
            mergedFiles.append(branchFile);
        } else {
            mergedFiles.append("");
        }
        mergedFiles.append(">>>>>>>" + "\n");
        return mergedFiles.toString();
    }

    public static HashMap<String, String> parentHashMap(Commit head) {
        HashMap<String, String> holder = new HashMap<>();
        if (head == null) {
            return holder;
        }
        holder.put(head.getSHA(), null);
        for (Commit parent : head.parentList()) {
            holder.putAll(parentHashMap(parent));
        }
        return holder;
    }

    public static Commit splitHelper(HashMap<String, String> headCommits, Commit branch) {
        HashMap<String, Boolean> tracked = new HashMap<>();
        LinkedList<Commit> queue = new LinkedList<>();
        tracked.put(branch.getSHA(), true);
        queue.add(branch);
        while (queue.size() > 0) {
            Commit checker = queue.poll();
            if (headCommits.containsKey(checker.getSHA())) {
                return checker;
            }
            for (Commit parent : checker.parentList()) {
                if (!tracked.containsKey(parent.getSHA())) {
                    queue.add(parent);
                }
            }
        }
        return null;
    }

    public static Commit splitFinder(Commit head, Commit branch) {
        return splitHelper(parentHashMap(head), branch);
    }

    public static void conflicManager(Commit head, Commit branch, String fName) {
        System.out.println("Encountered a merge conflict.");
        String contents = fileMerger(head, branch, fName);
        writeContents(join(CWD, fName), contents);
        Repository.add(fName);
    }

    public static void merge(String name) {
        if (mergeError(name)) {
            return;
        }
        Commit currentHead = currentCommit();
        Commit branchHead = branchCommit(name);
        Commit splitPoint = splitFinder(currentHead, branchHead);
        if (mergeError2(currentHead, branchHead, splitPoint, name)) {
            return;
        }
        Commit workCommit = getWorking();
        workCommit.mergeHelper(branchHead);
        workCommit.saveWorking();
        Map<String, String> mergeTracker = new HashMap<>();
        for (String fName : splitPoint.getFiles().keySet()) {
            mergeTracker.put(fName, null);
            String spHash = splitPoint.get(fName);
            String cfHash = currentHead.get(fName);
            String brHash = branchHead.get(fName);
            if (currentHead.containsKey(fName) && branchHead.containsKey(fName)) {
                if (!brHash.equals(spHash) && cfHash.equals(spHash)) {
                    File adde = join(CWD, fName);
                    writeContents(adde, branchHead.fileBytes(fName));
                    Repository.add(fName); //modified in branch but not in current
                } else if (!brHash.equals(spHash) && !cfHash.equals(spHash)
                        && !cfHash.equals(brHash)) {
                    conflicManager(currentHead, branchHead, fName);
                }
            } else if (currentHead.containsKey(fName) && !branchHead.containsKey(fName)) {
                if (cfHash.equals(spHash)) {
                    Repository.remove(fName); //unomodified in current but no present in branch
                }  else {
                    conflicManager(currentHead, branchHead, fName);
                }
            } else if (!currentHead.containsKey(fName) && branchHead.containsKey(fName)) {
                if (!brHash.equals(spHash)) {
                    File adde = join(CWD, fName);
                    try {
                        adde.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    conflicManager(currentHead, branchHead, fName);
                }
            }
        }
        for (String fName : branchHead.getFiles().keySet()) {
            if (!mergeTracker.containsKey(fName)) {
                String cfHash = currentHead.get(fName);
                String brHash = branchHead.get(fName);
                if (currentHead.containsKey(fName) && !cfHash.equals(brHash)) {
                    conflicManager(currentHead, branchHead, fName);
                } else if (!currentHead.containsKey(fName)) {
                    File adde = join(CWD, fName);
                    try {
                        adde.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    writeContents(adde, branchHead.fileBytes(fName));
                    Repository.add(fName); //not in split not in current but in branch
                }
            }
        }
        Repository.createCommit("Merged " + name + " into " + currentPointer() + ".");
    }
}
