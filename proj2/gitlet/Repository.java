package gitlet;

import gitlet.models.*;

import java.io.File;
import java.util.HashMap;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author camvan
 */
public class Repository {
    /** The working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    static String branch;

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

        branch = "master";

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
            return null; // 或者抛出异常，视情况而定
        }
        String commitHash = readContentsAsString(headCommitFile);
        return readObject(join(OBJECTS_DIR, commitHash), Commit.class);
    }

    public static void add(String fileName) {
        File file = join(Repository.CWD, fileName);

        Blob blob = new Blob(fileName, file);
        blob.save();

        Commit commit = getHeadCommit();
        StagingArea index = StagingArea.load();
        String currentHash = blob.getId();


        if (commit != null && commit.isAdded(currentHash, fileName)) {
            index.revert(fileName);

        }
        index.save();
    }
}
