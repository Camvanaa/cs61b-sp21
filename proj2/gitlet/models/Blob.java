package gitlet.models;

import gitlet.Repository;

import java.io.File;
import java.io.Serializable;
import static gitlet.Utils.*;

public class Blob implements Serializable {

    private final String fileName;
    private final byte[] content;
    private final String id;

    public Blob(String fileName, File file) {
        this.fileName = fileName;
        this.content = readContents(file);
        this.id = sha1((Object) content);
    }

    public String getId() {
        return id;
    }

    public byte[] getContent() {
        return content;
    }

    public String getFileName() {
        return fileName;
    }

    public void save() {
        File blobFile = join(Repository.OBJECTS_DIR, id);
        writeObject(blobFile, this);
    }
}
