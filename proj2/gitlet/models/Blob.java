package gitlet.models;

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
        this.id = sha1(content);
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

    public void save(File objectsDir) {
        File blobFile = join(objectsDir, id);
        writeObject(blobFile, this);
    }
}