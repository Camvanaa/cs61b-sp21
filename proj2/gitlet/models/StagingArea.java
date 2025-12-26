package gitlet.models;

import gitlet.Repository;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

public class StagingArea implements Serializable {
    /** fileName -> Blob SHA-1 */
    private final Map<String, String> added = new HashMap<>();
    /** rm file */
    private final Set<String> removed = new HashSet<>();
    public static final File STAGE_FILE = join(Repository.GITLET_DIR, "index");

    public StagingArea() {
    }

    public void add(String fileName, String sha1) {
        removed.remove(fileName);
        added.put(fileName, sha1);
    }

    public boolean isAdded(String fileName) {
        return added.containsKey(fileName);
    }

    public boolean isRemoved(String fileName) {
        return removed.contains(fileName);
    }

    public void revert(String fileName) {
        added.remove(fileName);
        removed.remove(fileName);
    }



    public void stageForRemoval(String fileName) {
        removed.add(fileName);
    }

    public void clear() {
        added.clear();
        removed.clear();
    }

    public boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty();
    }

    public Map<String, String> getAdded() {
        return added;
    }

    public Set<String> getRemoved() {
        return removed;
    }

    public void save() {
        writeObject(STAGE_FILE, this);
    }

    public static StagingArea load() {
        if (STAGE_FILE.exists()) {
            return readObject(STAGE_FILE, StagingArea.class);
        }
        return new StagingArea();
    }
}
