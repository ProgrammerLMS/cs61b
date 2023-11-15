package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
* staging area
* all stage data in file "index"
* @author: LMS
* */
public class Stage implements Serializable {
    /* <K, V> --> <fileName, blobId> */
    private Map<String, String> addedFiles;

    public Stage() {
        addedFiles = new HashMap<>();
    }

    public Map<String, String> getAddedFiles() {
        return this.addedFiles;
    }

    public void clearAddedFiles() {
        this.addedFiles = new HashMap<>();
    }

    public void addFileToStage(String fileName, String blobId) {
        addedFiles.put(fileName, blobId);
    }

    public void removeFileByFileName(String fileName) {
        addedFiles.remove(fileName);
    }
}
