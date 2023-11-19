package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* staging area
* all stage data in file "index"
* @author: LMS
* */
public class Stage implements Serializable {
    /* <K, V> --> <fileName, blobId> */
    private Map<String, String> addedFiles;

    private List<String> removedFiles;

    public Stage() {
        addedFiles = new HashMap<>();
        removedFiles = new ArrayList<>();
    }

    public Map<String, String> getAddedFiles() {
        return this.addedFiles;
    }

    public List<String> getRemovedFiles() {
        return this.removedFiles;
    }

    public void clear() {
        this.addedFiles = new HashMap<>();
        this.removedFiles = new ArrayList<>();
    }

    public void addFileToStage(String fileName, String blobId) {
        addedFiles.put(fileName, blobId);
    }

    public void removeFileOutOfStage(String fileName) {
        addedFiles.remove(fileName);
    }

    public void removeFileForRemoval(String filename) {
        removedFiles.add(filename);
    }

    public void removeFileOutOfRemoval(String fileName) {
        removedFiles.remove(fileName);
    }
}
