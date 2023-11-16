package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

/*  Represents a gitlet repository.
 *  The repository also maintains a mapping from branch heads to reference of commits
 *  so that certain important commits have symbolic names.
 *
 *  About sha1
 *  An interesting feature of Git is that these ids are universal:
 *  unlike a typical Java implementation, two objects with exactly the same content will have the same id on all systems
 *  my computer, your computer, and anyone else’s computer will compute this same exact id
 *  In the case of blobs, same content means the same file contents.
 *  In the case of commits, it means the same metadata, the same mapping of names of references,
 *  and the same parent reference. The objects in a repository are thus said to be content addressable.
 *  @author LMS
 */
public class Repository {
    /* The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /* The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* Blob and Commit data directory */
    public static final File OBJECT_DIR = Utils.join(GITLET_DIR, "objects");

    public static final File COMMIT_DIR = Utils.join(OBJECT_DIR, "commits");

    public static final File BLOB_DIR = Utils.join(OBJECT_DIR, "blobs");

    /* Stage data file */
    public static final File STAGE_FILE = Utils.join(GITLET_DIR, "index");

    /* Branch file directory */
    public static final File BRANCH_DIR = Utils.join(GITLET_DIR, "refs");

    /* local branch directory*/
    public static final File LOCAL_BRANCH_DIR = Utils.join(BRANCH_DIR, "heads");

    public static String currentBranchName;

    /* HEAD file */
    /* Note that in Gitlet, there is no way to be in a detached head state
       since there is no [checkout] command that will move the HEAD pointer to a specific commit.
       The [reset] command will do that, though it also moves the branch pointer.
       Thus, in Gitlet, you will never be in a detached HEAD state. */
    public static final File HEAD_FILE = Utils.join(GITLET_DIR, "HEAD");

    /* init the repository before any operation */
    public static void initRepository() {
        if(!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }
        if(!OBJECT_DIR.exists()) {
            OBJECT_DIR.mkdir();
        }
        if(!BRANCH_DIR.exists()) {
            BRANCH_DIR.mkdir();
        }
        if(!LOCAL_BRANCH_DIR.exists()) {
            LOCAL_BRANCH_DIR.mkdir();
        }
        if(!COMMIT_DIR.exists()) {
            COMMIT_DIR.mkdir();
        }
        if(!BLOB_DIR.exists()) {
            BLOB_DIR.mkdir();
        }
        initBranch();
        // do not forget every time you init, there will be a new Commit which point nothing;
        Date initDate = new Date(0);
        String initMessage = "initial commit";
        /* Since the initial commit in all repositories created by Gitlet will have exactly the same content,
           it follows that all repositories will automatically share this commit
           they will all have the same UID and all commits in all repositories will trace back to it. */
        String obj = initMessage + initDate;
        String commitId = Utils.sha1(obj);
        // init commit
        Commit commit = new Commit(initMessage, initDate, "");
        writeCommitIntoObjects(commitId, commit);
        // local"master" branch head and HEAD file both point at the init commit
        writeCurrentCommitIdIntoLocalBranch(commitId);
        // write branchInfo into HEAD
        writeCurrentLocalBranchIntoHead();
    }

    public static void initBranch() {
        if(HEAD_FILE.exists()) {
            String currentLocalBranchInfo = Utils.readContentsAsString(HEAD_FILE);
            currentBranchName = currentLocalBranchInfo.split(" ")[1].split("/")[2];
        } else currentBranchName = "master";
    }

    public static void switchToNewBranch(String newBranchName) {
        currentBranchName = newBranchName;
        writeCurrentLocalBranchIntoHead();
    }

    /* check directory exsit */
    public static boolean checkRepositoryExist() {
        return GITLET_DIR.exists() && OBJECT_DIR.exists()
                && BRANCH_DIR.exists() && LOCAL_BRANCH_DIR.exists()
                && COMMIT_DIR.exists() && BLOB_DIR.exists()
                && GITLET_DIR.isDirectory() && OBJECT_DIR.isDirectory()
                && BRANCH_DIR.isDirectory() && LOCAL_BRANCH_DIR.isDirectory()
                && COMMIT_DIR.isDirectory() && BLOB_DIR.isDirectory();
    }

    /* write commit into objects */
    public static void writeCommitIntoObjects(String commitId, Commit commit) {
        File file = Utils.join(COMMIT_DIR, commitId);
        Utils.writeObject(file, commit);
    }

    /* write current commitId into refs/heads/branchName */
    public static void writeCurrentCommitIdIntoLocalBranch(String commitId) {
        File file = Utils.join(LOCAL_BRANCH_DIR, currentBranchName);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeContents(file, commitId);
    }

    /* write current branchInfo into HEAD */
    public static void writeCurrentLocalBranchIntoHead() {
        if(!HEAD_FILE.exists()) {
            try {
                HEAD_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String content = "ref: " + BRANCH_DIR.getName() + "/" + LOCAL_BRANCH_DIR.getName() + "/" + currentBranchName;
        Utils.writeContents(HEAD_FILE, content);
    }

    /* using filename+filecontent as key */
    public static String checkBlobExist(String fileName, String content) {
        String obj = fileName + content;
        String blobId = Utils.sha1(obj);
        File blobFile = Utils.join(BLOB_DIR, blobId);
        if(blobFile.exists()) {
            return blobId;
        } else return "";
    }

    public static String writeBlobIntoObjects(String fileName, String content) {
        Blob blob = new Blob(content);
        String obj = fileName + content;
        String blobId = Utils.sha1(obj);
        File file = Utils.join(BLOB_DIR, blobId);
        Utils.writeObject(file, blob);
        return blobId;
    }

    public static String getCurrentLocalBranchHeadId() {
        File file = Utils.join(LOCAL_BRANCH_DIR, currentBranchName);
        if(file.exists()) {
            return Utils.readContentsAsString(file);
        } else return "";
    }

    public static Commit getCurrentLocalBranchHeadFromHEAD() {
        String commitId = getCurrentLocalBranchHeadId();
        File commitFile = Utils.join(COMMIT_DIR, commitId);
        if(commitFile.exists()) {
            return Utils.readObject(commitFile, Commit.class);
        } else return null;
    }

    /* Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
       The staging area should be somewhere in .gitlet.
       If the current working version of the file is identical to the version in the current commit,
       do not stage it to be added, and remove it from the staging area if it is already there.
       as can happen when a file is changed, added, and then changed back to it’s original version.
       The file will no longer be staged for removal, see git rm, if it was at the time of the command. */
    public static void addFileToStage(String fileName, String blobId) {
        Stage stage;
        if(STAGE_FILE.exists()) {
            stage = Utils.readObject(STAGE_FILE, Stage.class);
        } else stage = new Stage();
        stage.addFileToStage(fileName, blobId);
        // check this file version in current branch
        Commit currentCommit = getCurrentLocalBranchHeadFromHEAD();
        if(currentCommit != null) {
            Map<String, String> commitFiles = currentCommit.getCommitFiles();
            for(String filename : commitFiles.keySet()) {
                if(filename.equals(fileName) && commitFiles.get(filename).equals(blobId)) {
                    // if this unchanged file exist in stage, remove it
                    stage.removeFileByFileName(fileName);
                }
            }
        }
        Utils.writeObject(STAGE_FILE, stage);
    }

    public static void clearStageAndCommit(String message, Date date) {
        String obj = message + date.toString();
        String commitId = Utils.sha1(obj);
        // how we get the last commitId? -> current branch head point at it
        String currentLocalBranchHeadId = getCurrentLocalBranchHeadId();
        Commit commit = new Commit(message, date, currentLocalBranchHeadId);
        Stage stage = Utils.readObject(STAGE_FILE, Stage.class);
        Map<String, String> stagedFiles = stage.getAddedFiles();
        for(String fileName : stagedFiles.keySet()) {
            commit.addCommitFile(fileName, stagedFiles.get(fileName));
        }
        // 1.update index
        stage.clearAddedFiles();
        Utils.writeObject(STAGE_FILE, stage);
        // 2.update refs/heads
        writeCurrentCommitIdIntoLocalBranch(commitId);
        // 3.write commit into object
        writeCommitIntoObjects(commitId, commit);
    }

    public static String getFileContentFromBlob(String blobId) {
        File file = Utils.join(BLOB_DIR, blobId);
        Blob blob = Utils.readObject(file, Blob.class);
        return blob.getContent();
    }

    /* in real git, this is [git log --first-parent] */
    public static void showLogInfo() {
        String commitId = getCurrentLocalBranchHeadId();
        File file;
        Commit commit;
        do {
            file = Utils.join(COMMIT_DIR, commitId);
            commit = Utils.readObject(file, Commit.class);
            showSingleCommitLogInfo(commitId, commit);
            commitId = commit.getParentCommitId();
        } while(!commitId.equals(""));
    }

    private static void showSingleCommitLogInfo(String commitId, Commit commit) {
        StringBuilder sb = new StringBuilder();
        sb.append("===\n");
        // SHA1
        sb.append("commit ").append(commitId).append("\n");
        // Merge
        if (!commit.getSecondParentCommitId().equals("")) {
            sb.append("Merge: ").append(commit.getParentCommitId().substring(0, 7)).append(" ");
            sb.append(commit.getSecondParentCommitId().substring(0, 7)).append("\n");
        }
        // TimeStamp
        sb.append("Date: ");
        SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy Z");
        sb.append(format.format(commit.getTimestamp())).append("\n");
        // Message
        sb.append(commit.getMessage()).append("\n");
        System.out.println(sb.toString());
    }

    /* in real git, this is [git log] */
    /* in fact, get all commits equals traverse all nodes in a tree
       since we can use HEAD, which means a tree node */
    public static void showGlobalLogInfo() {
        List<String> commitFiles = Utils.plainFilenamesIn(COMMIT_DIR);
        if(commitFiles != null) {
            File file;
            Commit commit;
            for(String commitFileName : commitFiles) {
                file = Utils.join(COMMIT_DIR, commitFileName);
                commit = Utils.readObject(file, Commit.class);
                showSingleCommitLogInfo(commitFileName, commit);
            }
        }
    }

    /* Doesn’t exist in real git.
       Similar effects can be achieved by grepping the output of log. */
    public static void findAllCommitByMessage(String message) {
        List<String> commitFiles = Utils.plainFilenamesIn(COMMIT_DIR);
        if(commitFiles != null) {
            File file;
            Commit commit;
            for(String commitFileName : commitFiles) {
                file = Utils.join(COMMIT_DIR, commitFileName);
                commit = Utils.readObject(file, Commit.class);
                if(commit.getMessage().equals(message)) {
                    System.out.println(commitFileName);
                }
            }
        }
        System.out.println();
    }

    //TODO not finish yet
    public static void showStatusInfo() {
        /* branch */
        System.out.println("=== Branches ===");
        System.out.println();
        /* stage */
        System.out.println("=== Staged Files ===");
        System.out.println();
        /* remove */
        System.out.println("=== Removed Files ===");
        System.out.println();
        /* not stage */
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        /* untracked */
        System.out.println("=== Untracked Files ===");
    }


    /* Real git does not clear the staging area and stages the file that is checked out.
       Also, it won’t do a checkout that would overwrite or undo changes
       sucn as additions or removals that you have staged.*/
    public static void checkoutFileToCurrentCommit(String fileName) {
        /* but here, we immediately overwrite the file */
        File file = Utils.join(CWD, fileName);
        Commit latestCommit = getCurrentLocalBranchHeadFromHEAD();
        if(latestCommit != null) {
            Map<String, String> commitedFIles = latestCommit.getCommitFiles();
            for(String filename : commitedFIles.keySet()) {
                if(filename.equals(fileName)) {
                    String content = getFileContentFromBlob(commitedFIles.get(filename));
                    /* if this file exists, we overwrite. Otherwise, there will be new file */
                    Utils.writeContents(file, content);
                }
            }
            exitRepository("File does not exist in that commit.");
        }
    }

    /* in real git, it won’t do a checkout that would overwrite or undo changes
       sucn as additions or removals that you have staged.*/
    public static void checkoutFileToGivenCommit(String fileName, String commitId) {
        File file = Utils.join(CWD, fileName);
        File commitFile = Utils.join(COMMIT_DIR, commitId);
        if(!commitFile.exists()) {
            exitRepository("No commit with that id exists.");
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if(commit != null) {
            Map<String, String> commitedFiles = commit.getCommitFiles();
            for(String filename : commitedFiles.keySet()) {
                if(filename.equals(fileName)) {
                    String content = getFileContentFromBlob(commitedFiles.get(filename));
                    /* if this file exists, we overwrite. Otherwise, there will be new file */
                    Utils.writeContents(file, content);
                }
            }
            exitRepository("File does not exist in that commit.");
        }
    }

    /* Takes all files in the commit at the head of the given branch,
       and puts them in the working directory, overwriting the versions of the files that are already there if they exist.
       Also, at the end of this command, the given branch will now be considered the current branch.
       Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
       The staging area is cleared, unless the checked-out branch is the current branch*/
    public static void checkoutToGivenBranch(String givenBranchName) {
        if(givenBranchName.equals(currentBranchName)) {
            exitRepository("No need to checkout the current branch.");
        }
        File givenBranchFile = Utils.join(LOCAL_BRANCH_DIR, givenBranchName);
        if(!givenBranchFile.exists()) {
            exitRepository("No such branch exists.");
        }
        // actually this is cached character
        String currentBranch = currentBranchName;
        Commit currentCommit = getCurrentLocalBranchHeadFromHEAD();
        // check out to new branch
        switchToNewBranch(givenBranchName);
        Commit givenBranchCommit = getCurrentLocalBranchHeadFromHEAD();
        if(currentCommit != null && givenBranchCommit != null) {
            Map<String, String> currentCommitedFiles = currentCommit.getCommitFiles();
            Map<String, String> givenCommitedFiles = givenBranchCommit.getCommitFiles();
            for(String givenCommitFilename : givenCommitedFiles.keySet()) {
                /* If a working file is untracked in the current branch and would be overwritten by the checkout,
                   print the info below, and exit; */
                if(!currentCommitedFiles.containsKey(givenCommitFilename)) {
                    // remember to switch back
                    switchToNewBranch(currentBranch);
                    exitRepository("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
            File file;
            for(String givenCommitFilename : givenCommitedFiles.keySet()) {
                file = Utils.join(CWD, givenCommitFilename);
                String content = getFileContentFromBlob(givenCommitedFiles.get(givenCommitFilename));
                Utils.writeContents(file, content);
                currentCommitedFiles.remove(givenCommitFilename);
            }
            // delete all un-presented files
            for(String unPresentFile : currentCommitedFiles.keySet()) {
                file = Utils.join(CWD, unPresentFile);
                Utils.restrictedDelete(file);
            }
            // clear the stage
            Stage stage = new Stage();
            Utils.writeObject(STAGE_FILE, stage);
        }
    }

    /* Creates a new branch with the given name,
       and points it at the current head commit.
       A branch is nothing more than a name for a reference to a commit node.
       This command does NOT immediately switch to the newly created branch
       Before you ever call branch, your code should be running with a default branch called master */
    public static void createNewBranch(String newBranchName) {
        File file = Utils.join(BRANCH_DIR, newBranchName);
        if(file.exists()) {
            exitRepository("A branch with that name already exists.");
        }
        String commitId = getCurrentLocalBranchHeadId();
        Utils.writeContents(file, commitId);
    }

    public static void exitRepository(String message) {
        System.out.println(message);
        System.exit(0);
    }
}
