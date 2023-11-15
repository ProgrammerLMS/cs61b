package gitlet;

import java.io.File;
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
                    // working directory file
                    File file = Utils.join(Repository.CWD, args[1]);
                    if(file.exists()) {
                        String content = Utils.readContentsAsString(file);
                        String blobId = Repository.checkBlobExist(file.getName(), content);
                        /* read blob data and check file content;
                         * if the content change, add it to the stage area */
                        if(blobId.equals("")) {
                            blobId = Repository.writeBlobIntoObjects(file.getName(), content);
                        }
                        Repository.addFileToStage(file.getName(), blobId);
                    } else Repository.exitRepository("File does not exist.");
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "commit":
                validateNumArgs(args, 2); // commit [message]
                if(Repository.checkRepositoryExist()) {
                    /* after we commit, the file data in stage area is gone!
                       HEAD and branch head will both move to the latest commit */
                    Repository.initBranch();
                    Date date = new Date();
                    String message = args[1];
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

                    /* If the file is neither staged nor tracked by the head commit. do not remove */

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
                    // TODO: not finish since merge
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
                    // TODO NO finished
                    Repository.showStatusInfo();
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "checkout":
                if(Repository.checkRepositoryExist()) {
                    Repository.initBranch();
                    if(args.length == 2) { // checkout [branch name]
                        /* Only checkout of a full branch modifies the staging area
                           otherwise files scheduled for addition or removal remain so. */
                        String branchName = args[1];

                    } else if(args.length == 3) { // checkout -- [file name]
                        /* Takes the version of the file as it exists in the head commit
                           and puts it in the working directory,
                           overwriting the version of the file that’s already there if there is one.
                           The new version of the file is not staged. */
                        String fileName = args[2];

                    } else if(args.length == 4) { // checkout [commit id] -- [file name]
                        /* Takes the version of the file as it exists in the commit with the given id,
                           and puts it in the working directory,
                           overwriting the version of the file that’s already there if there is one.
                           The new version of the file is not staged. */
                        String commitId = args[1];
                        String fileName = args[3];

                    } else Repository.exitRepository("Incorrect operands.");
                } else Repository.exitRepository("Not in an initialized Gitlet directory.");
                break;
            case "branch":
                validateNumArgs(args, 2);
                String newBranchName = args[1];
                Repository.initBranch();
                Repository.createNewBranch(newBranchName);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);

                break;
            case "reset":
                validateNumArgs(args, 2);

                break;
            case "merge": // in gitlet, we only support two branches merge
                validateNumArgs(args, 2);

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
