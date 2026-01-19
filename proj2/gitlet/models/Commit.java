package gitlet.models;

import gitlet.Repository;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static gitlet.Utils.*;

public class Commit implements Serializable {
    /** The message of this Commit. */
    private final String message;
    /** The submit date of this Commit. */
    private Date timestamp;
    /** The parent of this Commit. */
    private final String parent;
    private final String parent2 = null;
    /** fileName -> blob's SHA-1 */
    Map<String, String> blobs;


    private  String id;


    public Commit(String message, String parent, Map<String, String> blobs) {
        this.message = message;
        this.parent = parent;
        this.blobs = blobs;
        this.timestamp = new Date();
        this.id = generateId();
    }

    public void setTimestamp(Date date) {
        this.timestamp = date;
        this.id = generateId();
    }

    public void save() {
        File commitFile = join(Repository.OBJECTS_DIR, id);
        writeObject(commitFile, this);
    }

    public String getId() {
        return id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getParent() {
        return parent;
    }

    public String getParent2() {
        return parent2;
    }

    public String getMessage() {
        return message;
    }

    private String generateId() {
        return sha1(message, timestamp.toString(),
                (parent == null ? "" : parent),
                blobs.toString());
    }

    public boolean isAdded(String sha1, String fileName) {
        return blobs.containsKey(fileName) && blobs.get(fileName).equals(sha1);
    }

    public Map<String, String> getBlobs() {
        return blobs;
    }
}
