package gitlet;

import gitlet.models.*;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;


/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author camvan
 */
public class Repository {
    /**
     * The working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    private static void mkdir(File dir) {
        if (!dir.mkdir() && !dir.isDirectory()) {
            throw new GitletException("Failed to create directory: " + dir.getPath());
        }
    }

    public static void init() {
        mkdir(GITLET_DIR);
        mkdir(OBJECTS_DIR);
        mkdir(REFS_DIR);
        mkdir(HEADS_DIR);

        StagingArea index = new StagingArea();
        index.save();

        String branch = "master";

        Commit initialCommit = new Commit("initial commit", null, new HashMap<>());
        initialCommit.setTimestamp(new java.util.Date(0));

        initialCommit.save();

        File masterBranch = join(HEADS_DIR, branch);
        writeContents(masterBranch, initialCommit.getId());

        writeContents(HEAD_FILE, branch);
    }

    private static Commit getHeadCommit() {
        String headName = readContentsAsString(HEAD_FILE);
        File headCommitFile = join(HEADS_DIR, headName);
        if (!headCommitFile.exists()) {
            return null;
        }
        String commitHash = readContentsAsString(headCommitFile);
        return readObject(join(OBJECTS_DIR, commitHash), Commit.class);
    }

    private static void updateHeadCommit(Commit commit) {
        String headName = readContentsAsString(HEAD_FILE);
        File headCommitFile = join(HEADS_DIR, headName);
        writeContents(headCommitFile, commit.getId());
    }

    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Blob blob = new Blob(fileName, file);
        String currentHash = blob.getId();
        Commit head = getHeadCommit();
        StagingArea index = StagingArea.load();

        if (head != null && currentHash.equals(head.getBlobs().get(fileName))) {
            index.revert(fileName);
        } else {
            blob.save();
            index.add(fileName, currentHash);
        }
        index.save();
    }

    public static void commit(String message) {
        StagingArea index = StagingArea.load();
        if (index.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getHeadCommit();
        String parentId = null;
        if (parentCommit != null) {
            parentId = parentCommit.getId();
        }

        Map<String, String> newBlobs = null;
        if (parentCommit != null) {
            newBlobs = new HashMap<>(parentCommit.getBlobs());
        }

        if (newBlobs != null) {
            newBlobs.putAll(index.getAdded());
        }
        for (String fileName : index.getRemoved()) {
            if (newBlobs != null) {
                newBlobs.remove(fileName);
            }
        }

        Commit currentCommit = new Commit(message, parentId, newBlobs);
        currentCommit.save();

        updateHeadCommit(currentCommit);

        index.clear();
        index.save();
    }

    public static void rm(String fileName) {
        Commit commit = getHeadCommit();
        StagingArea index = StagingArea.load();


        boolean isStaged = index.isAdded(fileName);
        boolean isTracked = (commit != null && commit.getBlobs().containsKey(fileName));


        if (!isStaged && !isTracked) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (isStaged) {
            index.revert(fileName);
        }

        if (isTracked) {
            index.stageForRemoval(fileName);

            File fileInCwd = join(CWD, fileName);
            if (fileInCwd.exists()) {
                restrictedDelete(fileInCwd);
            }
        }

        index.save();
    }

    private static void printCommit(Commit commit) {
        java.text.SimpleDateFormat dateFormat =
                new java.text.SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", java.util.Locale.US);

        System.out.println("===");
        System.out.println("commit " + commit.getId());

        if (commit.getParent2() != null) {
            String p1 = commit.getParent().substring(0, 7);
            String p2 = commit.getParent2().substring(0, 7);
            System.out.println("Merge: " + p1 + " " + p2);
        }

        System.out.println("Date: " + dateFormat.format(commit.getTimestamp()));
        System.out.println(commit.getMessage());
        System.out.println();
    }

    public static void log() {
        Commit commit = getHeadCommit();

        while (commit != null) {
            printCommit(commit);

            if (commit.getParent() != null) {
                String parentId = commit.getParent();
                commit = readObject(join(OBJECTS_DIR, parentId), Commit.class);
            } else {
                commit = null;
            }
        }
    }

    public static void globalLog() {
        List<String> filenames = plainFilenamesIn(OBJECTS_DIR);
        if (filenames == null) {
            return;
        }

        for (String fileName : filenames) {
            File file = join(OBJECTS_DIR, fileName);

            try {
                Commit commit = readObject(file, Commit.class);
                printCommit(commit);
            } catch (IllegalArgumentException | ClassCastException ignored) {
                //blob
            }
        }
    }


    public static void find(String message) {
        List<String> filenames = plainFilenamesIn(OBJECTS_DIR);
        if (filenames == null) {
            return;
        }

        boolean found = false;

        for (String fileName : filenames) {
            File file = join(OBJECTS_DIR, fileName);

            try {
                Commit commit = readObject(file, Commit.class);
                if (commit.getMessage().equals(message)) {
                    System.out.println(commit.getId());
                    found = true;
                }
            } catch (IllegalArgumentException | ClassCastException ignored) {
                //blob
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void status() {
        StagingArea index = StagingArea.load();
        Commit headCommit = getHeadCommit();
        Map<String, String> headBlobs =
                (headCommit != null) ? headCommit.getBlobs() : new HashMap<>();
        List<String> workingFiles = plainFilenamesIn(CWD);
        if (workingFiles == null) {
            workingFiles = new ArrayList<>();
        }
        System.out.println("=== Branches ===");
        String currentBranch = readContentsAsString(HEAD_FILE);
        List<String> branchNames = plainFilenamesIn(HEADS_DIR);
        if (branchNames != null) {
            Collections.sort(branchNames);
            for (String name : branchNames) {
                if (name.equals(currentBranch)) {
                    System.out.println("*" + name);
                } else {
                    System.out.println(name);
                }
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        TreeSet<String> sortedStaged = new TreeSet<>(index.getAdded().keySet());
        for (String fileName : sortedStaged) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        TreeSet<String> sortedRemoved = new TreeSet<>(index.getRemoved());
        for (String fileName : sortedRemoved) {
            System.out.println(fileName);
        }
        System.out.println();
        TreeSet<String> modifications = new TreeSet<>();
        for (String fileName : headBlobs.keySet()) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                String currentHash = new Blob(fileName, file).getId();
                if (!index.getAdded().containsKey(fileName)
                        && !currentHash.equals(headBlobs.get(fileName))) {
                    modifications.add(fileName + " (modified)");
                }
            } else if (!index.getRemoved().contains(fileName)) {
                modifications.add(fileName + " (deleted)");
            }
        }
        for (String fileName : index.getAdded().keySet()) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                String currentHash = new Blob(fileName, file).getId();
                if (!currentHash.equals(index.getAdded().get(fileName))) {
                    modifications.add(fileName + " (modified)");
                }
            } else {
                modifications.add(fileName + " (deleted)");
            }
        }

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String mod : modifications) {
            System.out.println(mod);
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        TreeSet<String> untracked = new TreeSet<>();
        for (String fileName : workingFiles) {
            if (!headBlobs.containsKey(fileName) && !index.getAdded().containsKey(fileName)) {
                untracked.add(fileName);
            }
        }
        for (String fileName : untracked) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    private static File getFile(Commit commit, String fileName) {
        Map<String, String> blobs = commit.getBlobs();
        if (blobs.containsKey(fileName)) {
            return join(OBJECTS_DIR, blobs.get(fileName));
        } else {
            return null;
        }
    }

    public static void checkout1(String fileName) {
        Commit commit = getHeadCommit();

        if (commit == null || !commit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File blobFile = getFile(commit, fileName);

        Blob b = readObject(blobFile, Blob.class);

        File target = join(CWD, fileName);
        writeContents(target, (Object) b.getContent());
    }

    private static Commit getCommit(String commitId) {
        if (commitId.length() == 40) {
            File commitFile = join(OBJECTS_DIR, commitId);
            if (commitFile.exists()) {
                try {
                    return readObject(commitFile, Commit.class);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
            return null;
        }
        List<String> allObjects = plainFilenamesIn(OBJECTS_DIR);
        if (allObjects == null) {
            return null;
        }

        for (String objId : allObjects) {
            if (objId.startsWith(commitId)) {
                File objFile = join(OBJECTS_DIR, objId);
                try {
                    return readObject(objFile, Commit.class);
                } catch (IllegalArgumentException | ClassCastException e) {
                    //blob
                }
            }
        }
        return null;
    }

    public static void checkout2(String commitId, String fileName) {
        Commit commit = getCommit(commitId);

        if (commit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        if (!commit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File blobFile = getFile(commit, fileName);

        Blob b = readObject(blobFile, Blob.class);

        File target = join(CWD, fileName);
        writeContents(target, (Object) b.getContent());
    }

    private static boolean hasUntracked(Commit targetCommit) {
        Commit currentCommit = getHeadCommit();
        StagingArea index = StagingArea.load();
        List<String> workingFiles = plainFilenamesIn(CWD);
        Map<String, String> currentBlobs =
                (currentCommit != null) ? currentCommit.getBlobs() : new HashMap<>();

        if (workingFiles == null) {
            return false;
        }

        for (String fileName : workingFiles) {
            boolean isTracked = currentBlobs.containsKey(fileName)
                    || index.getAdded().containsKey(fileName);
            if (!isTracked) {
                if (targetCommit.getBlobs().containsKey(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void checkout3(String branchName) {
        String currentBranch = readContentsAsString(HEAD_FILE);
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        File targetBranch = join(HEADS_DIR, branchName);
        if (!targetBranch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        String commitId = readContentsAsString(targetBranch);
        Commit currentCommit = getHeadCommit();
        Commit targetCommit = getCommit(commitId);

        if (hasUntracked(targetCommit)) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        Map<String, String> targetBlobs = targetCommit.getBlobs();
        for (Map.Entry<String, String> entry : targetBlobs.entrySet()) {
            File blobFile = join(OBJECTS_DIR, entry.getValue());
            Blob b = readObject(blobFile, Blob.class);
            writeContents(join(CWD, entry.getKey()), (Object) b.getContent());
        }

        for (String fileName : currentCommit.getBlobs().keySet()) {
            if (!targetBlobs.containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }

        StagingArea index = StagingArea.load();
        index.clear();
        index.save();

        writeContents(HEAD_FILE, branchName);

    }
}
