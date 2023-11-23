package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*  Represents a gitlet repository.
 *  The repository also maintains a mapping from branch heads to reference of commits
 *  so that certain important commits have symbolic names.
 *
 *  About sha1
 *  An interesting feature of Git is that these ids are universal:
 *  unlike a typical Java implementation
 *  two objects with exactly the same content will have the same id on all systems
 *  my computer, your computer, and anyone else’s computer will compute this same exact id
 *  In the case of blobs, same content means the same file contents.
 *  In the case of commits, it means the same metadata, the same mapping of names of references,
 *  and the same parent reference.
 *  The objects in a repository are thus said to be content addressable.
 *  @author LMS
 */
public class Repository {
    /* The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /* The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");

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

    private static String currentBranchName;

    /* HEAD file */
    /* Note that in Gitlet, there is no way to be in a detached head state
       since there is no [checkout] command that will move the HEAD pointer to a specific commit.
       The [reset] command will do that, though it also moves the branch pointer.
       Thus, in Gitlet, you will never be in a detached HEAD state. */
    public static final File HEAD_FILE = Utils.join(GITLET_DIR, "HEAD");

    /* init the repository before any operation */
    public static void initRepository() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }
        if (!OBJECT_DIR.exists()) {
            OBJECT_DIR.mkdir();
        }
        if (!BRANCH_DIR.exists()) {
            BRANCH_DIR.mkdir();
        }
        if (!LOCAL_BRANCH_DIR.exists()) {
            LOCAL_BRANCH_DIR.mkdir();
        }
        if (!COMMIT_DIR.exists()) {
            COMMIT_DIR.mkdir();
        }
        if (!BLOB_DIR.exists()) {
            BLOB_DIR.mkdir();
        }
        initBranch();
        // do not forget every time you init, there will be a new Commit which point nothing;
        Date initDate = new Date(0);
        String initMessage = "initial commit";
        /* Since the initial commit in all repositories
           created by Gitlet will have exactly the same content,
           it follows that all repositories will automatically share this commit
           they will all have the same UID and
           all commits in all repositories will trace back to it. */
        String obj = initMessage + initDate;
        String commitId = Utils.sha1(obj);
        // init commit
        Commit commit = new Commit(initMessage, initDate, "");
        writeCommitIntoObjects(commitId, commit);
        // local"master" branch head and HEAD file both point at the init commit
        writeCurrentCommitIdIntoCurrentLocalBranch(commitId);
        // write branchInfo into HEAD
        writeCurrentLocalBranchIntoHead();
    }

    public static void initBranch() {
        if (HEAD_FILE.exists()) {
            String currentLocalBranchInfo = Utils.readContentsAsString(HEAD_FILE);
            currentBranchName = currentLocalBranchInfo.split(" ")[1].split("/")[2];
        } else {
            currentBranchName = "master";
        }
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
    public static void writeCurrentCommitIdIntoCurrentLocalBranch(String commitId) {
        File file = Utils.join(LOCAL_BRANCH_DIR, currentBranchName);
        if (!file.exists()) {
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
        if (!HEAD_FILE.exists()) {
            try {
                HEAD_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String content = "ref: " + BRANCH_DIR.getName() + "/"
                + LOCAL_BRANCH_DIR.getName() + "/" + currentBranchName;
        Utils.writeContents(HEAD_FILE, content);
    }

    /* using filename+filecontent as key */
    public static String checkBlobExist(String fileName, String content) {
        String obj = fileName + content;
        String blobId = Utils.sha1(obj);
        File blobFile = Utils.join(BLOB_DIR, blobId);
        if (blobFile.exists()) {
            return blobId;
        } else {
            return "";
        }
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
        if (file.exists()) {
            return Utils.readContentsAsString(file);
        } else {
            return "";
        }
    }

    public static Commit getCurrentLocalBranchHead() {
        String commitId = getCurrentLocalBranchHeadId();
        File commitFile = Utils.join(COMMIT_DIR, commitId);
        if (commitFile.exists()) {
            return Utils.readObject(commitFile, Commit.class);
        } else {
            return null;
        }
    }

    /* Staging an already-staged file overwrites
       the previous entry in the staging area with the new contents.
       If the current working version of the file is identical to the version in the current commit,
       do not stage it to be added, and remove it from the staging area if it is already there.
       as can happen when a file is changed, added, and then changed back to it’s original version.
       The file will no longer be staged for removal,
       see git rm, if it was at the time of the command. */
    public static void addFileToStage(String fileName) {
        File file = Utils.join(CWD, fileName);
        String content, blobId;
        Stage stage;
        if (STAGE_FILE.exists()) {
            stage = Utils.readObject(STAGE_FILE, Stage.class);
        } else {
            stage = new Stage();
        }
        if (file.exists()) {
            content = Utils.readContentsAsString(file);
            blobId = Repository.checkBlobExist(file.getName(), content);
            /* read blob data and check file content;
             * if the content change, add it to the stage area */
            if (blobId.equals("")) {
                blobId = Repository.writeBlobIntoObjects(file.getName(), content);
            }
            if (stage.getRemovedFiles().contains(fileName)) {
                stage.removeFileOutOfRemoval(fileName);
            } else {
                stage.addFileToStage(fileName, blobId);
            }
            // check this file version in current branch
            Commit currentCommit = getCurrentLocalBranchHead();
            if (currentCommit != null) {
                Map<String, String> commitFiles = currentCommit.getCommitFiles();
                for (String filename : commitFiles.keySet()) {
                    if (filename.equals(fileName) && commitFiles.get(filename).equals(blobId)) {
                        // if this unchanged file exist in stage, remove it from stage
                        // not stage it for removal!
                        stage.removeFileOutOfStage(fileName);
                    }
                }
            }
            Utils.writeObject(STAGE_FILE, stage);
        } else {
            Repository.exitRepository("File does not exist.");
        }
    }

    /* By default a commit has the same file contents as its parent.
       Files staged for addition and removal are the updates to the commit.
       remember that the staging area is cleared after a commit. */
    /* Any changes made to files after staging
       for addition or removal are ignored by the commit command */
    /* ? Each commit is identified by its SHA-1 id,
       which must include the blob references of its files,
       parent reference, log message, and commit time. ? */
    public static void clearStageAndCommit(String message, Date date) {
        String obj = message + date.toString();
        String newCommitId = Utils.sha1(obj);
        // how we get the last commitId? -> current branch head point at it
        String currentCommitId = getCurrentLocalBranchHeadId();
        Commit currentCommit = getCurrentLocalBranchHead();
        Commit newCommit = new Commit(message, date, currentCommitId);
        /* default commit is same as it parent commit */
        if (currentCommit != null) {
            newCommit.setCommitFiles(currentCommit.getCommitFiles());
        }
        Stage stage;
        if (STAGE_FILE.exists()) {
            stage = Utils.readObject(STAGE_FILE, Stage.class);
        } else {
            stage = new Stage();
        }
        Map<String, String> addedFiles = stage.getAddedFiles();
        List<String> removedFiles = stage.getRemovedFiles();
        if (addedFiles.size() == 0 && removedFiles.size() == 0) {
            exitRepository("No changes added to the commit.");
        }
        for (String fileName : addedFiles.keySet()) {
            newCommit.addCommitFile(fileName, addedFiles.get(fileName));
        }
        /* files tracked in the current commit may be untracked in the new commit
           as a result being staged for removal */
        for (String removeFileName : removedFiles) {
            newCommit.removeCommitFiles(removeFileName);
        }
        // 1.update index
        stage.clear();
        Utils.writeObject(STAGE_FILE, stage);
        // 2.update refs/heads
        writeCurrentCommitIdIntoCurrentLocalBranch(newCommitId);
        // 3.write commit into object
        writeCommitIntoObjects(newCommitId, newCommit);
    }

    /* If the file is neither staged nor tracked by the head commit. do not remove */
    /* The rm command will remove such files, as well as staging them for removal
       so that they will be untracked after a commit. */
    public static void removeFileFromStageAndCWD(String fileName) {
        Stage stage;
        if (STAGE_FILE.exists()) {
            stage = Utils.readObject(STAGE_FILE, Stage.class);
        } else {
            stage = new Stage();
        }
        File file = Utils.join(CWD, fileName);
        Commit commit = getCurrentLocalBranchHead();
        if (commit != null) {
            Map<String, String> addedFiles = stage.getAddedFiles();
            Map<String, String> commitedFiles = commit.getCommitFiles();
            if (!addedFiles.containsKey(fileName) && !commitedFiles.containsKey(fileName)) {
                exitRepository("No reason to remove the file.");
            }
            if (addedFiles.containsKey(fileName)) {
                stage.removeFileOutOfStage(fileName);
            }
            /* do not remove it unless it is tracked in the current commit */
            if (commitedFiles.containsKey(fileName)) {
                stage.removeFileForRemoval(fileName);
                if (file.exists()) {
                    Utils.restrictedDelete(file);
                }
            }
            Utils.writeObject(STAGE_FILE, stage);
        }
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
        } while (!commitId.equals(""));
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
        if (commitFiles != null) {
            File file;
            Commit commit;
            for (String commitFileName : commitFiles) {
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
        List<String> namesList = new ArrayList<>();
        if (commitFiles != null) {
            File file;
            Commit commit;
            for (String commitFileName : commitFiles) {
                file = Utils.join(COMMIT_DIR, commitFileName);
                commit = Utils.readObject(file, Commit.class);
                if (commit.getMessage().equals(message)) {
                    namesList.add(commitFileName);
                }
            }
            if (namesList.size() == 0) {
                exitRepository("Found no commit with that message.");
            }
            for (String name : namesList) {
                System.out.println(name);
            }
        }
    }

    public static void showStatusInfo() {
        /* branch */
        System.out.println("=== Branches ===");
        List<String> branchNames = Utils.plainFilenamesIn(LOCAL_BRANCH_DIR);
        if (branchNames != null) {
            for (String branchName : branchNames) {
                if (branchName.equals(currentBranchName)) {
                    System.out.println("*" + branchName);
                } else {
                    System.out.println(branchName);
                }
            }
        }
        System.out.println();
        /* stage */
        System.out.println("=== Staged Files ===");
        Stage stage;
        if (STAGE_FILE.exists()) {
            stage = Utils.readObject(STAGE_FILE, Stage.class);
        } else {
            stage = new Stage();
        }
        for (String addedFile : stage.getAddedFiles().keySet()) {
            System.out.println(addedFile);
        }
        System.out.println();
        /* remove */
        System.out.println("=== Removed Files ===");
        for (String removedFile : stage.getRemovedFiles()) {
            System.out.println(removedFile);
        }
        System.out.println();
        /* not stage */
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        /* untracked */
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }


    /* Real git does not clear the staging area and stages the file that is checked out.
       Also, it won’t do a checkout that would overwrite or undo changes
       sucn as additions or removals that you have staged.*/
    public static void checkoutFileToCurrentCommit(String fileName) {
        /* but here, we immediately overwrite the file */
        File file = Utils.join(CWD, fileName);
        Commit latestCommit = getCurrentLocalBranchHead();
        if (latestCommit != null) {
            Map<String, String> commitedFiles = latestCommit.getCommitFiles();
            for (String filename : commitedFiles.keySet()) {
                if (filename.equals(fileName)) {
                    String content = getFileContentFromBlob(commitedFiles.get(filename));
                    /* if this file exists, we overwrite. Otherwise, there will be new file */
                    Utils.writeContents(file, content);
                    return;
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
        if (!commitFile.exists()) {
            exitRepository("No commit with that id exists.");
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        if (commit != null) {
            Map<String, String> commitedFiles = commit.getCommitFiles();
            for (String filename : commitedFiles.keySet()) {
                if (filename.equals(fileName)) {
                    String content = getFileContentFromBlob(commitedFiles.get(filename));
                    /* if this file exists, we overwrite. Otherwise, there will be new file */
                    Utils.writeContents(file, content);
                    return;
                }
            }
            exitRepository("File does not exist in that commit.");
        }
    }

    /* Takes all files in the commit at the head of the given branch,
       and puts them in the working directory, overwriting the
       versions of the files that are already there if they exist.
       Also, at the end of this command, the given branch will
       now be considered the current branch.
       Any files that are tracked in the current branch
       but are not present in the checked-out branch are deleted.
       The staging area is cleared, unless the checked-out branch is the current branch*/
    public static void checkoutToGivenBranch(String givenBranchName) {
        if (givenBranchName.equals(currentBranchName)) {
            exitRepository("No need to checkout the current branch.");
        }
        File givenBranchFile = Utils.join(LOCAL_BRANCH_DIR, givenBranchName);
        if (!givenBranchFile.exists()) {
            exitRepository("No such branch exists.");
        }
        // actually this is cached character
        String preBranch = currentBranchName;
        Commit currentCommit = getCurrentLocalBranchHead();
        // check out to new branch
        switchToNewBranch(givenBranchName);
        Commit givenBranchCommit = getCurrentLocalBranchHead();
        if (currentCommit != null && givenBranchCommit != null) {
            Map<String, String> currentCommitedFiles = currentCommit.getCommitFiles();
            Map<String, String> givenCommitedFiles = givenBranchCommit.getCommitFiles();
            for (String givenCommitFilename : givenCommitedFiles.keySet()) {
                /* If a working file is untracked in the current branch
                   and would be overwritten by the checkout,
                   print the info below, and exit; */
                if (!currentCommitedFiles.containsKey(givenCommitFilename)) {
                    // check the content
                    File file = Utils.join(CWD, givenCommitFilename);
                    if (file.exists()) {
                        String currentContent = Utils.readContentsAsString(file);
                        String oldContent =
                                getFileContentFromBlob(givenCommitedFiles.get(givenCommitFilename));
                        if (!currentContent.equals(oldContent)) {
                            // remember to switch back
                            switchToNewBranch(preBranch);
                            exitRepository("There is an untracked file in the way; "
                                    + "delete it, or add and commit it first.");
                        }
                    }
                }
            }
            File file;
            for (String givenCommitFilename : givenCommitedFiles.keySet()) {
                file = Utils.join(CWD, givenCommitFilename);
                String content =
                        getFileContentFromBlob(givenCommitedFiles.get(givenCommitFilename));
                Utils.writeContents(file, content);
                currentCommitedFiles.remove(givenCommitFilename);
            }
            // delete all un-presented files
            for (String unPresentFile : currentCommitedFiles.keySet()) {
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
       Before you ever call branch,
       your code should be running with a default branch called master*/
    public static void createNewBranch(String newBranchName) {
        File file = Utils.join(LOCAL_BRANCH_DIR, newBranchName);
        if (file.exists()) {
            exitRepository("A branch with that name already exists.");
        }
        String commitId = getCurrentLocalBranchHeadId();
        Utils.writeContents(file, commitId);
    }

    public static void deleteGivenBranch(String branchName) {
        if (branchName.equals(currentBranchName)) {
            exitRepository("Cannot remove the current branch.");
        }
        File file = Utils.join(LOCAL_BRANCH_DIR, branchName);
        if (!file.exists()) {
            exitRepository("A branch with that name does not exist.");
        }
        Utils.notRestrictedDelete(file);
    }

    /* in real git, this is reset [id] -- hard */
    public static void resetHard(String commitId) {
        File file = Utils.join(COMMIT_DIR, commitId);
        if (!file.exists()) {
            exitRepository("No commit with that id exists.");
        }
        Commit givenCommit = Utils.readObject(file, Commit.class);
        Commit currentCommit = getCurrentLocalBranchHead();
        if (currentCommit != null) {
            File unpresentFile, presentFile;
            Map<String, String> currentCommitFiles = currentCommit.getCommitFiles();
            Map<String, String> givenCommitFiles = givenCommit.getCommitFiles();
            checkOverwrite(givenCommit, currentCommit);
            for (String currentFile : currentCommitFiles.keySet()) {
                if (!givenCommitFiles.containsKey(currentFile)) {
                    unpresentFile = Utils.join(CWD, currentFile);
                    if (unpresentFile.exists()) {
                        Utils.restrictedDelete(unpresentFile);
                    }
                } else {
                    presentFile = Utils.join(CWD, currentFile);
                    String newContent = getFileContentFromBlob(givenCommitFiles.get(currentFile));
                    Utils.writeContents(presentFile, newContent);
                }
            }
            // move the current branch’s head to that commit node
            writeCurrentCommitIdIntoCurrentLocalBranch(commitId);
            // clear stage
            Stage stage;
            if (STAGE_FILE.exists()) {
                stage = Utils.readObject(STAGE_FILE, Stage.class);
            } else {
                stage = new Stage();
            }
            stage.clear();
            Utils.writeObject(STAGE_FILE, stage);
        }
    }

    private static void checkOverwrite(Commit givenCommit, Commit currentCommit) {
        File untrackedFile;
        Map<String, String> currentCommitFiles = currentCommit.getCommitFiles();
        Map<String, String> givenCommitFiles = givenCommit.getCommitFiles();
        for (String givenFile : givenCommitFiles.keySet()) {
            if (!currentCommitFiles.containsKey(givenFile)) {
                untrackedFile = Utils.join(CWD, givenFile);
                if (untrackedFile.exists()) {
                    String newContent = Utils.readContentsAsString(untrackedFile);
                    String oldContent = getFileContentFromBlob(givenCommitFiles.get(givenFile));
                    if (!newContent.equals(oldContent)) {
                        exitRepository("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                    }
                }
            }
        }
    }

    /* Differences from real git:
       Real Git does a more subtle job of merging files, displaying conflicts
       only in places where both files have changed since the split point.

       Real Git has a different way to decide which of multiple possible split points to use.

       Real Git will force the user to resolve the merge conflicts
       before committing to complete the merge.
       Gitlet just commits the merge, conflicts and all
       so that you must use a separate commit to resolve problems.

       Real Git will complain if there are unstaged changes to
       a file that would be changed by a merge.
       You may do so as well if you want, but we will not test that case.
     * */
    public static void mergeGivenBranchToCurrent(String givenBranchName) {
        Stage stage;
        if (STAGE_FILE.exists()) {
            stage = Utils.readObject(STAGE_FILE, Stage.class);
        } else {
            stage = new Stage();
        }
        if (stage.getAddedFiles().size() != 0 || stage.getRemovedFiles().size() != 0) {
            exitRepository("You have uncommitted changes.");
        }
        if (givenBranchName.equals(currentBranchName)) {
            exitRepository("Cannot merge a branch with itself.");
        }
        File branchFile = Utils.join(LOCAL_BRANCH_DIR, givenBranchName);
        if (!branchFile.exists()) {
            exitRepository("A branch with that name does not exist.");
        }
        /* check overwrite */
        String currentBranchHeadId = getCurrentLocalBranchHeadId();
        String givenBranchHeadId = Utils.readContentsAsString(branchFile);
        Commit currentCommit = getCurrentLocalBranchHead();
        File givenCommitFile = Utils.join(COMMIT_DIR, givenBranchHeadId);
        Commit givenCommit = Utils.readObject(givenCommitFile, Commit.class);
        if (currentCommit != null) {
            checkOverwrite(givenCommit, currentCommit);
        }
        /* find the split point */
        boolean hasConflict = false;
        String splitPointId = getSplitPoint(currentBranchHeadId, givenBranchHeadId);
        /* If the split point is the same commit as the given branch,
        then we do nothing and operation ends with the message */
        if (splitPointId.equals(givenBranchHeadId)) {
            exitRepository("Given branch is an ancestor of the current branch.");
        }
        /* If the split point is the current branch
           then the effect is to check out the given branch
           and operation ends after printing the message */
        if (splitPointId.equals(currentBranchHeadId)) {
            checkoutToGivenBranch(givenBranchName);
            exitRepository("Current branch fast-forwarded.");
        }
        File splitPointFile = Utils.join(COMMIT_DIR, splitPointId);
        Commit splitPoint = Utils.readObject(splitPointFile, Commit.class);
        if (currentCommit != null) {
            Map<String, String> currentCommitFiles = currentCommit.getCommitFiles();
            Map<String, String> givenCommitFiles = givenCommit.getCommitFiles();
            Map<String, String> splitPointFiles = splitPoint.getCommitFiles();
            for (String currentFileName : currentCommitFiles.keySet()) {
                if (currentCommitFiles.get(currentFileName).equals(
                        splitPointFiles.getOrDefault(currentFileName, ""))
                ) {
                    /* 1.Any files that have been modified in the given branch
                       since the split point, but not modified in the current branch
                       since the split point should be changed to their versions
                       in the given branch,which means
                       checked out from the commit at the front of the given branch.
                       These files should then all be automatically staged */
                    if (!givenCommitFiles.getOrDefault(currentFileName, "").equals(
                        splitPointFiles.getOrDefault(currentFileName, "")
                    )) {
                        checkoutFileToGivenCommit(currentFileName, givenBranchHeadId);
                        addFileToStage(currentFileName);
                    }
                    /* 6.Any files present at the split point,
                       unmodified in the current branch,
                       and absent in the given branch
                       should be removed and untracked. */
                    if (!givenCommitFiles.containsKey(currentFileName)) {
                        removeFileFromStageAndCWD(currentFileName);
                    }
                }
                /* 3.1 Any files that have been modified in both
                   the current and given branch in the same way,
                   both files now have the same content or were both removed
                   are left unchanged by the merge. */

                /* 3.2 If a file was removed from both the current and given branch
                   but a file of the same name is present in the working directory
                   it is left alone and continues to be absent,
                   not tracked nor staged in the merge. */

                /* 4.Any files that were not present at the split point
                  and are present only in the current branch
                  should remain as they are. */
                if (!splitPointFiles.containsKey(currentFileName)
                        && !givenCommitFiles.containsKey(currentFileName)) {
                    continue;
                }
                /* 8.Any files modified in different ways in the current
                   and given branches are in conflict. */
                if (!currentCommitFiles.get(currentFileName).equals(
                        splitPointFiles.getOrDefault(currentFileName, "")
                    )
                    && !givenCommitFiles.getOrDefault(currentFileName, "").equals(
                        splitPointFiles.getOrDefault(currentFileName, "")
                    )
                    && !currentCommitFiles.get(currentFileName).equals(
                         givenCommitFiles.getOrDefault(currentFileName, "")
                )) {
                    hasConflict = true;
                    handleConflict(currentCommitFiles, givenCommitFiles, currentFileName);
                }

            }
            for (String givenCommitFileName : givenCommitFiles.keySet()) {
                if (splitPointFiles.getOrDefault(givenCommitFileName, "").equals(
                        givenCommitFiles.get(givenCommitFileName)
                )) {
                    /* 7.Any files present at the split point
                       unmodified in the given branch
                       and absent in the current branch should remain absent. */
                    if (!currentCommitFiles.containsKey(givenCommitFileName)) {
                        continue;
                    }
                    /* 2.Any files that have been modified in the current branch
                       but not in the given branch since the split point
                       should stay as they are. */
                    if (!currentCommitFiles.getOrDefault(givenCommitFileName, "").equals(
                         givenCommitFiles.get(givenCommitFileName)
                    )) {
                        continue;
                    }
                }

                /* 5.Any files that were not present at the split point
                   and are present only in the given branch
                   should be checked out and staged. */
                if (!currentCommitFiles.containsKey(givenCommitFileName)
                        && !splitPointFiles.containsKey(givenCommitFileName)) {
                    checkoutFileToGivenCommit(givenCommitFileName, givenBranchHeadId);
                    addFileToStage(givenCommitFileName);
                }

                /* 8.Any files modified in different ways in the current
                   and given branches are in conflict. */
                if (!currentCommitFiles.get(givenCommitFileName).equals(
                        splitPointFiles.getOrDefault(givenCommitFileName, "")
                    )
                        && !givenCommitFiles.getOrDefault(givenCommitFileName, "").equals(
                        splitPointFiles.getOrDefault(givenCommitFileName, "")
                    )
                        && !currentCommitFiles.get(givenCommitFileName).equals(
                        givenCommitFiles.getOrDefault(givenCommitFileName, "")
                )) {
                    hasConflict = true;
                    handleConflict(currentCommitFiles, givenCommitFiles, givenCommitFileName);
                }
            }


            if (hasConflict) {
                System.out.println("Encountered a merge conflict.");
            }
            // new commit
            String message = "Merged " + givenBranchName + " into " + currentBranchName + ".";
            clearStageAndCommit(message, new Date());
        }
    }

    private static String getSplitPoint(String currentBranchHeadId, String givenBranchHeadId) {
        List<String> currenBranchNode = new ArrayList<>();
        while (!currentBranchHeadId.equals("")) {
            currenBranchNode.add(currentBranchHeadId);
            File file = Utils.join(COMMIT_DIR, currentBranchHeadId);
            Commit commit = Utils.readObject(file, Commit.class);
            currentBranchHeadId = commit.getParentCommitId();
        }
        while (!givenBranchHeadId.equals("")) {
            if (currenBranchNode.contains(givenBranchHeadId)) {
                return givenBranchHeadId;
            }
            File file = Utils.join(COMMIT_DIR, givenBranchHeadId);
            Commit commit = Utils.readObject(file, Commit.class);
            givenBranchHeadId = commit.getParentCommitId();
        }
        return "";
    }

    // TODO
    private static void handleConflict(Map<String,String> currentCommitFiles,
                                       Map<String,String> givenCommitFiles,
                                       String filename) {
        String currentContent;
        String givenContent;
        File blobFile;
        Blob blob;
        if (currentCommitFiles.containsKey(filename)) {
            blobFile = Utils.join(BLOB_DIR, currentCommitFiles.get(filename));
            blob = Utils.readObject(blobFile, Blob.class);
            currentContent = blob.getContent();
        } else {
            currentContent = "";
        }
        if (givenCommitFiles.containsKey(filename)) {
            blobFile = Utils.join(BLOB_DIR, givenCommitFiles.get(filename));
            blob = Utils.readObject(blobFile, Blob.class);
            givenContent = blob.getContent();
        } else {
            givenContent = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<<<<<<< HEAD\n");
        sb.append(currentContent);
        sb.append("=======\n");
        sb.append(givenContent);
        sb.append(">>>>>>>\n");
        File newConflictFile = Utils.join(CWD, filename);
        Utils.writeContents(newConflictFile, sb.toString());
        addFileToStage(filename);
    }

    public static void exitRepository(String message) {
        System.out.println(message);
        System.exit(0);
    }
}
