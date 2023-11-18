package gitlet;

import java.io.Serializable;
import java.util.*;

/*  Represents a gitlet commit object, stored in directory "objects"
 *  In real git, we have three objects, blob, commit and tree
 *  but here, we incorporate trees into commits and not dealing with subdirectories
 *  so there will be one flat directory of plain files for each repository
 *
 *  About Merge:
 *  A commit, therefore, will consist of a log message, timestamp, a mapping of file names to blob references,
 *  a parent reference, and (for merges) a second parent reference.
 *
 *  About hash
 *  In particular, this involves Including all metadata and references when hashing a commit.
 *  Distinguishing somehow between hashes for commits and hashes for blobs.
 *  A good way of doing this involves a well-thought out directory structure within the .gitlet directory.
 *  Another way to do so is to hash in an extra word for each object that has one value for blobs and another for commits.
 *  @author LMS
 */
public class Commit implements Serializable {
    /* The message of this Commit. */
    private String message;

    /* the timestamp of this commit */
    private Date timestamp;

    /* point to the last commit node */
    private String parentCommitId;

    /* second parent commitId, for merge */
    private String secondParentCommitId;

    /* <fileName, blobId> */
    private Map<String, String> commitFiles;

    public Commit(String message, Date timestamp, String parentCommitId) {
        this.message = message;
        this.timestamp = timestamp;
        this.parentCommitId = parentCommitId;
        this.secondParentCommitId = "";
        commitFiles = new HashMap<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timeStamp) {
        this.timestamp = timeStamp;
    }

    public String getParentCommitId() {
        return parentCommitId;
    }

    public void setParentCommitId(String parentCommitId) {
        this.parentCommitId = parentCommitId;
    }

    public String getSecondParentCommitId() {
        return secondParentCommitId;
    }

    public void setSecondParentCommitId(String secondParentCommitId) {
        this.secondParentCommitId = secondParentCommitId;
    }

    public Map<String, String> getCommitFiles() {
        return commitFiles;
    }

    public void setCommitFiles(Map<String, String> commitFiles) {
        this.commitFiles = commitFiles;
    }

    public void addCommitFile(String fileName, String blobId) {
        this.commitFiles.put(fileName, blobId);
    }

    public void removeCommitFiles(String fileName) {
        this.commitFiles.remove(fileName);
    }
}
