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

        Commit initialCommit = new Commit("initial commit", null, null, new HashMap<>());
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

    public static void commit(String message, String parent2) {
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

        Commit currentCommit = new Commit(message, parentId, parent2, newBlobs);
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
        if (commitId == null) {
            return null;
        }
        if (commitId.length() == 40) {
            File commitFile = join(OBJECTS_DIR, commitId);
            if (commitFile.exists()) {
                try {
                    return readObject(commitFile, Commit.class);
                } catch (IllegalArgumentException e) {
                    System.out.println("No commit with that id exists.");
                    System.exit(0);
                    return null;
                }
            }
            System.out.println("No commit with that id exists.");
            System.exit(0);
            return null;
        }
        List<String> allObjects = plainFilenamesIn(OBJECTS_DIR);
        if (allObjects == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
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
        System.out.println("No commit with that id exists.");
        System.exit(0);
        return null;
    }

    public static void checkout2(String commitId, String fileName) {
        Commit commit = getCommit(commitId);

        if (!commit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        File blobFile = getFile(commit, fileName);

        Blob b = readObject(blobFile, Blob.class);

        File target = join(CWD, fileName);
        writeContents(target, (Object) b.getContent());
    }

    private static void hasUntracked(Commit targetCommit) {
        Commit currentCommit = getHeadCommit();
        StagingArea index = StagingArea.load();
        List<String> workingFiles = plainFilenamesIn(CWD);
        Map<String, String> currentBlobs =
                (currentCommit != null) ? currentCommit.getBlobs() : new HashMap<>();

        if (workingFiles == null) {
            return;
        }

        for (String fileName : workingFiles) {
            boolean isTracked = currentBlobs.containsKey(fileName)
                    || index.getAdded().containsKey(fileName);
            if (!isTracked) {
                if (targetCommit.getBlobs().containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
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

        hasUntracked(targetCommit);


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

    public static void branch(String branchName) {
        File newBranchFile = join(HEADS_DIR, branchName);
        if (newBranchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit headCommit = getHeadCommit();
        if (headCommit != null) {
            writeContents(newBranchFile, headCommit.getId());
        }
    }

    public static void rmBranch(String branchName) {
        String currentBranch = readContentsAsString(HEAD_FILE);
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        File targetBranchFile = join(HEADS_DIR, branchName);
        if (!targetBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        restrictedDelete(targetBranchFile);
    }

    public static void reset(String commitId) {
        Commit targetCommit = getCommit(commitId);
        hasUntracked(targetCommit);

        Map<String, String> targetBlobs = targetCommit.getBlobs();
        for (Map.Entry<String, String> entry : targetBlobs.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            File blobFile = join(OBJECTS_DIR, blobId);
            Blob b = readObject(blobFile, Blob.class);
            writeContents(join(CWD, fileName), (Object) b.getContent());
        }

        Commit currentCommit = getHeadCommit();
        if (currentCommit != null) {
            for (String fileName : currentCommit.getBlobs().keySet()) {
                if (!targetBlobs.containsKey(fileName)) {
                    restrictedDelete(join(CWD, fileName));
                }
            }
        }

        StagingArea index = StagingArea.load();
        index.clear();
        index.save();

        String currentBranchName = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranchName);
        writeContents(branchFile, targetCommit.getId());
    }

    private static Set<Commit> getBranchCommits(Commit headCommit) {
        Set<Commit> commits = new HashSet<>();
        Commit currentCommit = headCommit;
        while (currentCommit != null) {
            commits.add(currentCommit);

            if (currentCommit.getParent() != null) {
                String parentId = currentCommit.getParent();
                currentCommit = readObject(join(OBJECTS_DIR, parentId), Commit.class);
            } else {
                currentCommit = null;
            }
        }
        return commits;
    }


    private static Set<String> getAncestorIds(Commit commit) {
        Set<String> ancestors = new HashSet<>();
        Queue<Commit> queue = new LinkedList<>();
        queue.add(commit);

        while (!queue.isEmpty()) {
            Commit c = queue.poll();
            if (c != null && !ancestors.contains(c.getId())) {
                ancestors.add(c.getId());
                if (c.getParent() != null) {
                    queue.add(getCommit(c.getParent()));
                }
                if (c.getParent2() != null) {
                    queue.add(getCommit(c.getParent2()));
                }
            }
        }
        return ancestors;
    }

    public static void merge(String branchName) {
        Result mergeInit = getResult(branchName);
        if (mergeInit == null) {
            return;
        }

        Set<String> allFileNames = new HashSet<>();
        allFileNames.addAll(mergeInit.currentCommit.getBlobs().keySet());
        allFileNames.addAll(mergeInit.targetCommit.getBlobs().keySet());
        allFileNames.addAll(mergeInit.splitCommit.getBlobs().keySet());

        boolean encounteredConflict = false;

        for (String fileName : allFileNames) {
            String spHash = mergeInit.splitCommit.getBlobs().get(fileName);
            String currHash = mergeInit.currentCommit.getBlobs().get(fileName);
            String givenHash = mergeInit.targetCommit.getBlobs().get(fileName);

            boolean inSplit = spHash != null;
            boolean inCurr = currHash != null;
            boolean inGiven = givenHash != null;

            if (inSplit && inCurr && inGiven && safeEquals(spHash, currHash)
                    && !safeEquals(spHash, givenHash)) {
                checkout2(mergeInit.targetCommitId, fileName);
                mergeInit.index.add(fileName, givenHash);
            } else if (inSplit && inCurr && inGiven && !safeEquals(spHash, currHash)
                    && safeEquals(spHash, givenHash)) {
                continue;
            } else if (safeEquals(currHash, givenHash)) {
                continue;
            } else if (!inSplit && !inCurr) {
                checkout2(mergeInit.targetCommitId, fileName);
                mergeInit.index.add(fileName, givenHash);
            } else if (!inSplit && !inGiven) {
                continue;
            } else if (inSplit && inCurr && !inGiven && safeEquals(spHash, currHash)) {
                rm(fileName);
            } else if (inSplit && !inCurr && safeEquals(spHash, givenHash)) {
                continue;
            } else {
                encounteredConflict = true;
                handleConflict(fileName, currHash, givenHash, mergeInit.index);
            }
        }

        mergeInit.index.save();

        String msg = "Merged " + branchName + " into " + mergeInit.currentBranchName + ".";

        commit(msg, mergeInit.targetCommitId);

        if (encounteredConflict) {
            System.out.println("Encountered a merge conflict.");
        }

    }

    private static Result getResult(String branchName) {
        StagingArea index = StagingArea.load();
        if (!index.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        Commit currentCommit = getHeadCommit();

        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String targetCommitId = readContentsAsString(branchFile);

        Commit targetCommit = getCommit(targetCommitId);
        String currentBranchName = readContentsAsString(HEAD_FILE);
        if (branchName.equals(currentBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        hasUntracked(targetCommit);

        Set<String> currentAncestorIds = getAncestorIds(currentCommit);

        Commit splitCommit = getSpCommit(targetCommit, currentAncestorIds);

        String splitCommitId = splitCommit.getId();
        String currentCommitId = null;
        if (currentCommit != null) {
            currentCommitId = currentCommit.getId();
        }

        if (splitCommitId.equals(targetCommitId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return null;
        }
        if (splitCommitId.equals(currentCommitId)) {
            checkout3(branchName);
            System.out.println("Current branch fast-forwarded.");
            return null;
        }
        return new Result(index, currentCommit, targetCommitId,
                targetCommit, currentBranchName, splitCommit);
    }

    private static class Result {
        final StagingArea index;
        final Commit currentCommit;
        final String targetCommitId;
        final Commit targetCommit;
        final String currentBranchName;
        final Commit splitCommit;

        Result(StagingArea index, Commit currentCommit, String targetCommitId,
               Commit targetCommit, String currentBranchName, Commit splitCommit) {
            this.index = index;
            this.currentCommit = currentCommit;
            this.targetCommitId = targetCommitId;
            this.targetCommit = targetCommit;
            this.currentBranchName = currentBranchName;
            this.splitCommit = splitCommit;
        }
    }

    private static Commit getSpCommit(Commit targetCommit, Set<String> currentAncestorIds) {
        Commit splitCommit = null;
        Queue<Commit> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(targetCommit);
        visited.add(targetCommit.getId());

        while (!queue.isEmpty()) {
            Commit commit = queue.poll();
            if (currentAncestorIds.contains(commit.getId())) {
                splitCommit = commit;
                break;
            }
            List<String> parents = new ArrayList<>();
            if (commit.getParent() != null) {
                parents.add(commit.getParent());
            }
            if (commit.getParent2() != null) {
                parents.add(commit.getParent2());
            }

            for (String pid : parents) {
                if (!visited.contains(pid)) {
                    visited.add(pid);
                    queue.add(getCommit(pid));
                }
            }
        }
        return splitCommit;
    }


    private static boolean safeEquals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private static void handleConflict(String fileName, String currHash,
                                       String givenHash, StagingArea index) {
        String currContent = "";
        String givenContent = "";

        if (currHash != null) {
            File f = join(OBJECTS_DIR, currHash);
            Blob b = readObject(f, Blob.class);
            currContent = new String(b.getContent());
        }

        if (givenHash != null) {
            File f = join(OBJECTS_DIR, givenHash);
            Blob b = readObject(f, Blob.class);
            givenContent = new String(b.getContent());
        }

        String content = "<<<<<<< HEAD\n" + currContent + "=======\n" + givenContent + ">>>>>>>\n";

        File targetFile = join(CWD, fileName);
        writeContents(targetFile, content);

        Blob conflictBlob = new Blob(fileName, targetFile);
        conflictBlob.save();
        index.add(fileName, conflictBlob.getId());
    }


}
