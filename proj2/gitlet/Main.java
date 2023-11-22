package gitlet;

import java.util.Date;

/* Driver class for Gitlet, a subset of the Git version-control system.
 * Some of the commands have their differences from real Git listed.
 * README does list some of the bigger or potentially confusing and misleading ones.
 * @author LMS
 */
public class Main {

    /* java gitlet.Main ARGS, where ARGS contains <COMMAND> <OPERAND1> <OPERAND2> ... */
    public static void main(String[] args) {
        if(args.length == 0) {
            Repository.exitRepository("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                if(Repository.checkRepositoryExist()){
                    Repository.exitRepository("A Gitlet version-control system " +
                            "already exists in the current directory.");
                } else {
                    Repository.initRepository();
                }
                break;
            case "add":
                validateNumArgs(args, 2);
                if(Repository.checkRepositoryExist()) {
                    /* add the file into stage area(called ZanCunQu) when file changed or added first time
                       add the file content(at the moment when you call "add") to the blob
                       notice that file which not change will not be added to stage */
                    Repository.initBranch();
                    String filename = args[1];
                    Repository.addFileToStage(filename);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "commit":
                validateNumArgs(args, 2); // commit [message]
                if(Repository.checkRepositoryExist()) {
                    /* creating a new commit saves a snapshot of tracked files in the current commit and staging area
                      so they can be restored at a later time.
                      The commit is said to be tracking the saved files.
                      By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot
                      of files;  it will keep versions of files exactly as they are, and not update them.
                      A commit will only update the contents of files it is tracking that have been staged for addition
                      at the time of commit, in which case the commit will now include the version of the file that was staged
                      instead of the version it got from its parent.
                      A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent.
                      Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal
                      by the rm command . */
                    Repository.initBranch();
                    Date date = new Date();
                    String message = args[1];
                    if(message.length() == 0) {
                        Repository.exitRepository("Please enter a commit message.");
                    }
                    Repository.clearStageAndCommit(message, date);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "rm":
                validateNumArgs(args, 2); // rm [file-name]
                if(Repository.checkRepositoryExist()) {
                    /* Unstage the file if it is currently staged for addition.
                       If the file is tracked in the current commit,
                       stage it for removal and remove the file from the working directory
                       if the user has not already done so
                       remember do not remove it unless it is tracked in the current commit */
                    Repository.initBranch();
                    String fileName = args[1];
                    // TODO right?
                    Repository.removeFileFromStageAndCWD(fileName);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "log":
                validateNumArgs(args, 1);
                if(Repository.checkRepositoryExist()) {
                    /* Starting at the current head commit, display information about each commit
                       backwards along the commit tree until the initial commit,
                       following the first parent commit links,
                       ignoring any second parents found in merge commits.
                       In regular Git, this is what you get with git log --first-parent.
                       This set of commit nodes is called the commit’s history. */
                    Repository.initBranch();
                    Repository.showLogInfo();
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "global-log":
                validateNumArgs(args, 1);
                if(Repository.checkRepositoryExist()) {
                    Repository.initBranch();
                    Repository.showGlobalLogInfo();
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "find":
                validateNumArgs(args, 2);
                if(Repository.checkRepositoryExist()) {
                    String message = args[1];
                    Repository.initBranch();
                    Repository.findAllCommitByMessage(message);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "status":
                validateNumArgs(args, 1);
                if(Repository.checkRepositoryExist()) {
                    Repository.initBranch();
                    Repository.showStatusInfo();
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "checkout":
                if(Repository.checkRepositoryExist()) {
                    Repository.initBranch();
                    if(args.length == 2) { // checkout [branch name]
                        /* In our proj, only checkout of a full branch modifies the staging area
                           otherwise files scheduled for addition or removal remain so. */
                        String branchName = args[1];
                        Repository.checkoutToGivenBranch(branchName);
                    } else if(args.length == 3) { // checkout -- [file name]
                        /* Takes the version of the file as it exists in the head commit
                           and puts it in the working directory,
                           overwriting the version of the file that’s already there if there is one.
                           The new version of the file is not staged. */
                        if(!args[1].equals("--")) {
                            Repository.exitRepository("Incorrect operands.");
                        }
                        String fileName = args[2];
                        Repository.checkoutFileToCurrentCommit(fileName);
                    } else if(args.length == 4) { // checkout [commit id] -- [file name]
                        /* Takes the version of the file as it exists in the commit with the given id,
                           and puts it in the working directory,
                           overwriting the version of the file that’s already there if there is one.
                           The new version of the file is not staged. */
                        if(!args[2].equals("--")) {
                            Repository.exitRepository("Incorrect operands.");
                        }
                        String commitId = args[1];
                        String fileName = args[3];
                        Repository.checkoutFileToGivenCommit(fileName, commitId);
                    } else Repository.exitRepository("Incorrect operands.");
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "branch":
                validateNumArgs(args, 2);
                String newBranchName = args[1];
                if(Repository.checkRepositoryExist()) {
                    Repository.initBranch();
                    Repository.createNewBranch(newBranchName);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                String branchName = args[1];
                if(Repository.checkRepositoryExist()) {
                    /* Deletes the branch with the given name.
                   This only means to delete the pointer associated with the branch;
                   it does not mean to delete all commits that were created under the branch. */
                    Repository.initBranch();
                    Repository.deleteGivenBranch(branchName);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "reset":
                validateNumArgs(args, 2);  // reset [commit id]
                /* Checks out all the files tracked by the given commit.
                   Removes tracked files that are not present in that commit.
                   Also moves the current branch’s head to that commit node.
                   The staging area is cleared.
                   The command is essentially checkout of an arbitrary commit that also changes the current branch head. */
                if(Repository.checkRepositoryExist()) {
                    Repository.initBranch();
                    String commitId = args[1];
                    Repository.resetHard(commitId);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "merge": // in gitlet, we only support two branches merge
                validateNumArgs(args, 2); // merge [file name]
                /* Merge files from the given branch into the current branch. */
                String givenBranchName = args[1];
                if(Repository.checkRepositoryExist()) {
                    Repository.initBranch();
                    Repository.mergeGivenBranchToCurrent(givenBranchName);
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            default:
                Repository.exitRepository("No command with that name exists.");
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            Repository.exitRepository("Incorrect operands.");
        }
    }
}
