package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;


import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  @author Skye-ye
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The blobs' directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "objects", "blobs");
    /** The commits' directory. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "objects",
            "commits");
    /** The branches' directory. */
    public static final File BRANCHES_DIR = join(GITLET_DIR, "heads");
    /** The current branch. */
    public static final File CURRENT_BRANCH = join(GITLET_DIR, "current");
    /** The staging file. */
    public static final File STAGING_FILE = join(GITLET_DIR, "staging");
    /** The head file. */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    /** The remote directory. */
    // public static final File REMOTE_DIR = join(GITLET_DIR, "remotes");
    /** The remote information file. */
    public static final File REMOTE_INFO_FILE = join(GITLET_DIR, "remote");
    /** Default branch. */
    public static final String DEFAULT_BRANCH = "master";


    public void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            return;
        }
        /* Create the .gitlet directory and its subdirectories */
        GITLET_DIR.mkdir();
        BLOBS_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BRANCHES_DIR.mkdirs();
        CURRENT_BRANCH.createNewFile();
        STAGING_FILE.createNewFile();
        HEAD_FILE.createNewFile();
        REMOTE_INFO_FILE.createNewFile();

        /* Create the initial commit */
        Commit initialCommit = new Commit("initial commit", null, null);
        submitCommit(initialCommit, DEFAULT_BRANCH);

        /* Create a staging file */
        Stage stage = new Stage();
        writeObject(STAGING_FILE, stage);

        /* Create a remote object */
        HashMap<String, String> remotes = new HashMap<>();
        writeObject(REMOTE_INFO_FILE, remotes);
    }

    public void add(String fileName) {
        /* Error checking */
        checkGitletDir();

        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        /* Read the staging area */
        Stage stage = Stage.fromFile(STAGING_FILE);
        String hash = sha1(readContents(file));

        /* Search files in current commit */
        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR,
                readContentsAsString(HEAD_FILE)));
        if (currentCommit.containsSameFile(fileName, hash)) {
            if (stage.containsStagedFile(fileName)) {
                String oldHash = stage.getHash(fileName);
                stage.removeStagedFile(fileName);

                /* Remove the previous version of the file from the blobs directory */
                File blobFile = join(BLOBS_DIR, oldHash);
                if (!blobFile.exists()) {
                    System.out.println("File in previous version does not "
                            + "exist.");
                    System.exit(0);
                }
                blobFile.delete();
            } else if (stage.containsRemovedFile(fileName)) {
                stage.resetRemovedFile(fileName);
            }
        } else {
            stage.add(fileName);

            /* Add the file to the blobs directory */
            File blobFile = join(BLOBS_DIR, hash);
            writeContents(blobFile, readContents(file));
        }
        writeObject(STAGING_FILE, stage);
    }

    public void commit(String message) throws IOException {
        /* Error checking */
        checkGitletDir();

        Stage stage = Stage.fromFile(STAGING_FILE);
        if (stage.getStagedFiles().isEmpty()
                && stage.getRemovedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        /* Create the commit */
        String currentHash = readContentsAsString(HEAD_FILE);
        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR, currentHash));
        Commit newCommit = new Commit(message, currentHash,
                currentCommit.getBlobs());
        newCommit.changeBlobs(stage.getStagedFiles());
        newCommit.removeBlobs(stage.getRemovedFiles());

        /* Submit the commit */
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        submitCommit(newCommit, currentBranch);

        /* Clear the staging area */
        stage.clear();
        writeObject(STAGING_FILE, stage);
    }

    public void rm(String fileName) {
        /* Error checking */
        checkGitletDir();

        Stage stage = Stage.fromFile(STAGING_FILE);
        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR,
                readContentsAsString(HEAD_FILE)));
        if (!stage.containsStagedFile(fileName)
                && !currentCommit.containsFile(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (stage.containsStagedFile(fileName)) {
            stage.removeStagedFile(fileName);
        }

        if (currentCommit.containsFile(fileName)) {
            stage.addRemovedFile(fileName);
            File file = join(CWD, fileName);
            file.delete();
        }

        writeObject(STAGING_FILE, stage);
    }

    public void log() {
        /* Error checking */
        checkGitletDir();

        /* Print the log */
        String hash = readContentsAsString(HEAD_FILE);
        while (hash != null) {
            Commit commit = Commit.fromFile(join(COMMITS_DIR, hash));
            System.out.println("===");
            System.out.println("commit " + hash);
            if (commit.getSecondParent() != null) {
                System.out.println("Merge: " + commit.getFirstParent().substring(0,
                        7) + " " + commit.getSecondParent().substring(0, 7));
            }
            System.out.println("Date: " + commit.getTime());
            System.out.println(commit.getMessage());
            System.out.println();
            hash = commit.getFirstParent();
        }
    }

    public void globalLog() {
        /* Error checking */
        checkGitletDir();

        /* Print the global log */
        List<String> files = plainFilenamesIn(COMMITS_DIR);
        if (files == null) {
            return;
        }
        for (String file : files) {
            File commitFile = join(COMMITS_DIR, file);
            Commit commit = Commit.fromFile(commitFile);
            System.out.println("===");
            System.out.println("commit " + commitFile.getName());
            if (commit.getSecondParent() != null) {
                System.out.println("Merge: " + commit.getFirstParent().substring(0,
                        7) + " " + commit.getSecondParent().substring(0, 7));
            }
            System.out.println("Date: " + commit.getTime());
            System.out.println(commit.getMessage());
            System.out.println();
        }
    }

    public void find(String message) {
        /* Error checking */
        checkGitletDir();

        /* Search for the commit */
        List<String> files = plainFilenamesIn(COMMITS_DIR);
        if (files == null) {
            return;
        }
        boolean found = false;
        for (String file : files) {
            File commitFile = join(COMMITS_DIR, file);
            Commit commit = Commit.fromFile(commitFile);
            if (Objects.equals(commit.getMessage(), message)) {
                System.out.println(commitFile.getName());
                found = true;
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        checkGitletDir();

        printBranches();

        Stage stage = Stage.fromFile(STAGING_FILE);
        Commit commit = Commit.fromFile(join(COMMITS_DIR,
                readContentsAsString(HEAD_FILE)));
        List<String> workingDirectoryFiles = plainFilenamesIn(CWD);
        if (workingDirectoryFiles == null) {
            System.exit(0);
        }

        TreeSet<String> stagedFiles = new TreeSet<>();
        TreeSet<String>  removedFiles = new TreeSet<>();
        TreeMap<String, Integer> modifiedFiles = new TreeMap<>();

        HashMap<String, String> addedStagedFiles = stage.getStagedFiles();
        HashSet<String> removedStagedFiles = stage.getRemovedFiles();
        HashMap<String, String> commitFiles = commit.getBlobs();

        for (Map.Entry<String, String> entry : addedStagedFiles.entrySet()) {
            String fileName = entry.getKey();
            File file = join(CWD, fileName);
            if (!file.exists()) {
                modifiedFiles.put(fileName, 0);
            } else {
                String localHash = sha1(readContents(file));
                if (entry.getValue().equals(localHash)) {
                    stagedFiles.add(fileName);
                } else {
                    modifiedFiles.put(fileName, 1);
                }
            }
        }

        for (Map.Entry<String, String> entry : commitFiles.entrySet()) {
            String fileName = entry.getKey();
            File file = join(CWD, fileName);
            if (!file.exists()) {
                if (!removedStagedFiles.contains(fileName)) {
                    modifiedFiles.put(fileName, 0);
                }
            } else {
                String localHash = sha1(readContents(file));
                if (!entry.getValue().equals(localHash)
                        && !addedStagedFiles.containsKey(fileName)
                        && !removedStagedFiles.contains(fileName)) {
                    modifiedFiles.put(fileName, 1);
                }
            }
        }

        for (String fileName : removedStagedFiles) {
            File file = join(CWD, fileName);
            if (!file.exists()) {
                removedFiles.add(fileName);
            }
        }

        printStatus(stagedFiles, removedFiles, modifiedFiles);

        System.out.println("=== Untracked Files ===");
        for (String fileName : workingDirectoryFiles) {
            if ((!commitFiles.containsKey(fileName)
                    && !addedStagedFiles.containsKey(fileName))
                    || removedStagedFiles.contains(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println();
    }

    public void checkoutFileFromCommit(String hash, String fileName) {
        /* Error checking */
        checkGitletDir();

        /* Get full hash */
        String fullHash = findFullCommitHash(hash);

        File commitFile = join(COMMITS_DIR, fullHash);
        Commit commit = Commit.fromFile(commitFile);

        /* Search for the file */
        if (!commit.containsFile(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        /* Checkout the file */
        String blobHash = commit.getBlobHash(fileName);
        File blobFile = join(BLOBS_DIR, blobHash);
        File file = join(CWD, fileName);
        writeContents(file, readContents(blobFile));
    }

    public void checkoutFileFromCurrentCommit(String fileName) {
        checkoutFileFromCommit(readContentsAsString(HEAD_FILE), fileName);
    }

    public void checkoutBranch(String branch) throws IOException {
        /* Error checking */
        checkGitletDir();

        /* Search for the branch */
        File branchFile = join(BRANCHES_DIR, branch);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        /* Check if the branch is the current branch */
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        if (branch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        /* Checkout target commit */
        String targetHash = readContentsAsString(branchFile);
        checkoutCommit(targetHash);

        /* Update the current branch */
        writeContents(CURRENT_BRANCH, branch);
        writeContents(HEAD_FILE, targetHash);
    }

    public void branch(String branchName) {
        /* Error checking */
        checkGitletDir();

        /* Search for the branch */
        File branchFile = join(BRANCHES_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        /* Create the branch */
        writeContents(branchFile, readContentsAsString(HEAD_FILE));
    }

    public void rmBranch(String branchName) {
        /* Error checking */
        checkGitletDir();

        /* Search for the branch */
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        /* Check if the branch is the current branch */
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        /* Remove the branch */
        branchFile.delete();
    }

    public void reset(String hash) {
        /* Error checking */
        checkGitletDir();

        /* Get full hash */
        String fullHash = findFullCommitHash(hash);

        /* Checkout the files */
        checkoutCommit(fullHash);

        /* Update the current branch */
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        writeContents(join(BRANCHES_DIR, currentBranch), fullHash);

        /* Update the head file */
        writeContents(HEAD_FILE, fullHash);
    }

    public void merge(String branchName) throws IOException {
        /* Error checking */
        checkGitletDir();

        /* Check staging area */
        checkBlankStagingArea();

        /* Search for the branch */
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        /* Check if the branch is the current branch */
        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        if (branchName.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        /* Get the split point */
        String currentCommitHash = readContentsAsString(HEAD_FILE);
        String givenCommitHash = readContentsAsString(branchFile);
        String splitCommitHash = findSplitPoint(currentCommitHash,
                givenCommitHash);

        /* Check if the split point is the same as the given branch */
        if (splitCommitHash.equals(givenCommitHash)) {
            System.out.println("Given branch is an ancestor of "
                    + "the current branch.");
            System.exit(0);
        }

        /* Check if the split point is the same as the current branch */
        if (splitCommitHash.equals(currentCommitHash)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR, currentCommitHash));
        Commit givenCommit = Commit.fromFile(join(COMMITS_DIR, givenCommitHash));
        Commit splitCommit = Commit.fromFile(join(COMMITS_DIR, splitCommitHash));

        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        HashMap<String, String> givenBlobs = givenCommit.getBlobs();
        HashMap<String, String> splitBlobs = splitCommit.getBlobs();

        List<String> workingDirectoryFiles = plainFilenamesIn(CWD);
        if (workingDirectoryFiles == null) {
            System.exit(0);
        }

        HashSet<String> lockedFiles = new HashSet<>();
        HashMap<String, String> newBlobs = new HashMap<>(currentBlobs);

        boolean conflict = false;

        conflict = checkGivenBranch(givenBlobs,
                lockedFiles, splitBlobs, currentBlobs,
                givenCommitHash, newBlobs, conflict, workingDirectoryFiles);

        conflict = checkCurrentBranch(currentBlobs, lockedFiles, splitBlobs,
                givenBlobs, newBlobs, conflict);

        /* Create the merge commit */
        Commit commit = new Commit("Merged " + branchName + " into "
                + currentBranch + ".", currentCommitHash, newBlobs);
        commit.setSecondParent(givenCommitHash);
        submitCommit(commit, currentBranch);

        /* Print whether there is a conflict */
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    public void addRemote(String remoteName, String remoteDir) {
        /* Error checking */
        checkGitletDir();

        /* Check if already exists */
        @SuppressWarnings("unchecked")
        HashMap<String, String> remotes =
                readObject(REMOTE_INFO_FILE, HashMap.class);
        if (remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }

        /* Convert the path */
        remoteDir = convertPath(remoteDir);

        /* Read remote information */
        remotes.put(remoteName, remoteDir);

        /* Write remote information */
        writeObject(REMOTE_INFO_FILE, remotes);
    }

    public void rmRemote(String remoteName) {
        /* Error checking */
        checkGitletDir();

        /* Check if the remote exists */
        @SuppressWarnings("unchecked")
        HashMap<String, String> remotes =
                readObject(REMOTE_INFO_FILE, HashMap.class);
        if (!remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        /* Remove the remote branches */
        File remoteBranchFile = join(BRANCHES_DIR, remoteName);
        if (remoteBranchFile.exists()) {
            List<String> branches = plainFilenamesIn(remoteBranchFile);
            if (branches != null) {
                for (String branch : branches) {
                    File branchFile = join(remoteBranchFile, branch);
                    branchFile.delete();
                }
            }
            remoteBranchFile.delete();
        }

        /* Remove the remote information */
        remotes.remove(remoteName);
        writeObject(REMOTE_INFO_FILE, remotes);
    }

    public void push(String remoteName, String remoteBranch) throws IOException {
        /* Error checking */
        checkGitletDir();

        /* Check if the remote exists */
        @SuppressWarnings("unchecked")
        HashMap<String, String> remotes =
                readObject(REMOTE_INFO_FILE, HashMap.class);
        if (!remotes.containsKey(remoteName)) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        /* Get the remote directory */
        File remoteDir = new File(remotes.get(remoteName));
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        /* Get the remote branch */
        File remoteBranchFile = join(remoteDir, "heads", remoteBranch);
        if (!remoteBranchFile.exists()) {
            remoteBranchFile.createNewFile();
            String remoteInitHash = findRemoteInitHash(remoteDir);
            writeContents(remoteBranchFile, remoteInitHash);
        }

        /*
         * Check if the remote branch’s head is in the
         * history of the current local head
         */
        String localBranch = readContentsAsString(CURRENT_BRANCH);
        File localBranchFile = join(BRANCHES_DIR, localBranch);
        String localCommitHash = readContentsAsString(localBranchFile);
        String remoteCommitHash = readContentsAsString(remoteBranchFile);
        if (!isAncestor(localCommitHash, remoteCommitHash)) {
            System.out.println("Please pull down remote changes before pushing.");
            System.exit(0);
        }

        /* Append the future commits to the remote branch */
        File remoteCommitDir = join(remoteDir, "objects", "commits");
        File remoteBlobDir = join(remoteDir, "objects", "blobs");
        while (!localCommitHash.equals(remoteCommitHash)) {
            Commit localCommit = Commit.fromFile(join(COMMITS_DIR, localCommitHash));
            File remoteCommitFile = join(remoteCommitDir, localCommitHash);
            remoteCommitFile.createNewFile();
            writeObject(remoteCommitFile, localCommit);

            for (Map.Entry<String, String> entry
                    : localCommit.getBlobs().entrySet()) {
                String blobHash = entry.getValue();
                File localBlobFile = join(BLOBS_DIR, blobHash);
                File remoteBlobFile = join(remoteBlobDir, blobHash);
                if (!remoteBlobFile.exists()) {
                    remoteBlobFile.createNewFile();
                    writeContents(remoteBlobFile, readContents(localBlobFile));
                }
            }

            localCommitHash = localCommit.getFirstParent();
        }

        /* Reset remote to the front of the append commits */
        writeContents(remoteBranchFile, readContentsAsString(localBranchFile));
        writeContents(join(remoteDir, "HEAD"),
                readContentsAsString(remoteBranchFile));
    }

    public void fetch(String remoteName, String remoteBranch) throws IOException {
        /* Error checking */
        checkGitletDir();

        /* Check if the remote exists */
        @SuppressWarnings("unchecked")
        HashMap<String, String> remotes =
                readObject(REMOTE_INFO_FILE, HashMap.class);
        if (!remotes.containsKey(remoteName)) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        /* Get the remote directory */
        File remoteDir = new File(remotes.get(remoteName));
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        /* Get the remote branch */
        File remoteBranchFile = join(remoteDir, "heads", remoteBranch);
        if (!remoteBranchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }

        /* Get the remote commit dir and blob dir */
        File remoteCommitDir = join(remoteDir, "objects", "commits");
        File remoteBlobDir = join(remoteDir, "objects", "blobs");

        /* Create local branch */
        File localDir = join(BRANCHES_DIR, remoteName);
        if (!localDir.exists()) {
            localDir.mkdirs();
        }
        File localBranchFile = join(localDir, remoteBranch);
        if (!localBranchFile.exists()) {
            localBranchFile.createNewFile();
        }
        writeContents(localBranchFile, readContents(remoteBranchFile));

        /* Copy commits and blobs to local repository */
        String remoteCommitHash = readContentsAsString(remoteBranchFile);
        while (remoteCommitHash != null) {
            File remoteCommitFile = join(remoteCommitDir, remoteCommitHash);
            Commit remoteCommit = Commit.fromFile(remoteCommitFile);
            File localCommitFile = join(COMMITS_DIR, remoteCommitHash);
            if (!localCommitFile.exists()) {
                localCommitFile.createNewFile();
                writeObject(localCommitFile, remoteCommit);

                for (Map.Entry<String, String> entry
                        : remoteCommit.getBlobs().entrySet()) {
                    String blobHash = entry.getValue();
                    File remoteBlobFile = join(remoteBlobDir, blobHash);
                    File localBlobFile = join(BLOBS_DIR, blobHash);
                    if (!localBlobFile.exists()) {
                        localBlobFile.createNewFile();
                        writeContents(localBlobFile, readContents(remoteBlobFile));
                    }
                }
            }

            remoteCommitHash = remoteCommit.getFirstParent();
        }
    }

    public void pull(String remoteName, String remoteBranch) throws IOException {
        /* Error checking */
        checkGitletDir();

        /* Fetch the remote branch */
        fetch(remoteName, remoteBranch);

        /* Merge the remote branch */
        merge(remoteName + "/" + remoteBranch);
    }

    private static void checkGitletDir() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static void submitCommit(Commit commit, String branch)
            throws IOException {
        String hash = sha1(serialize(commit));
        File commitFile = join(COMMITS_DIR, hash);
        commitFile.createNewFile();
        writeObject(commitFile, commit);

        writeContents(HEAD_FILE, hash);

        File branchFile = join(BRANCHES_DIR, branch);
        writeContents(branchFile, hash);

        writeContents(CURRENT_BRANCH, branch);
    }

    private static void printBranches() {
        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        if (branches == null) {
            System.exit(0);
        }

        String currentBranch = readContentsAsString(CURRENT_BRANCH);
        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
    }

    private static void printStatus(TreeSet<String> stagedFiles,
                                    TreeSet<String> removedFiles,
                                    TreeMap<String, Integer> modifiedFiles) {
        System.out.println("=== Staged Files ===");
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (Map.Entry<String, Integer> entry : modifiedFiles.entrySet()) {
            String fileName = entry.getKey();
            int status = entry.getValue();
            if (status == 1) {
                System.out.println(fileName + " (modified)");
            } else {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();
    }

    private static String findFullCommitHash(String hash) {
        List<String> files = plainFilenamesIn(COMMITS_DIR);
        List<String> matchedFiles = new ArrayList<>();

        if (files == null) {
            return null;
        }

        for (String file : files) {
            if (file.startsWith(hash)) {
                matchedFiles.add(file);
            }
        }

        if (matchedFiles.isEmpty()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (matchedFiles.size() > 1) {
            System.out.println("Commit id is ambiguous.");
            System.exit(0);
        }

        return matchedFiles.get(0);
    }

    private static void checkoutCommit(String targetHash) {
        /* Read the current commit */
        String currentHash = readContentsAsString(HEAD_FILE);
        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR, currentHash));

        /* Read the target commit */
        Commit targetCommit = Commit.fromFile(join(COMMITS_DIR, targetHash));

        /* Read Staging Area */
        Stage stage = Stage.fromFile(STAGING_FILE);

        /* Check if there is an untracked file */
        List<String> workingDirectoryFiles = plainFilenamesIn(CWD);
        if (workingDirectoryFiles == null) {
            return;
        }
        for (String fileName : workingDirectoryFiles) {
            if ((!currentCommit.containsFile(fileName)
                    && !stage.containsStagedFile(fileName))
                    || stage.containsRemovedFile(fileName)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        /* Delete files under CWD */
        for (String fileName : workingDirectoryFiles) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                file.delete();
            }
        }

        /* Checkout files from the target commit */
        for (Map.Entry<String, String> entry
                : targetCommit.getBlobs().entrySet()) {
            String fileName = entry.getKey();
            String blobHash = entry.getValue();
            File blobFile = join(BLOBS_DIR, blobHash);
            File file = join(CWD, fileName);
            writeContents(file, readContents(blobFile));
        }

        /* Clear the staging area */
        stage.clear();
        writeObject(STAGING_FILE, stage);
    }

    private static void checkBlankStagingArea() {
        Stage stage = Stage.fromFile(STAGING_FILE);
        if (!stage.getStagedFiles().isEmpty()
                || !stage.getRemovedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    private static String findSplitPoint(String hash1, String hash2) {
        HashSet<String> ancestors1 = new HashSet<>();
        HashSet<String> ancestors2 = new HashSet<>();
        Queue<String> queue1 = new LinkedList<>();
        Queue<String> queue2 = new LinkedList<>();

        queue1.offer(hash1);
        queue2.offer(hash2);

        /* Use BFS to search for latest common ancestor */
        while (!queue1.isEmpty() || !queue2.isEmpty()) {
            if (!queue1.isEmpty()) {
                String current = queue1.poll();
                if (!ancestors1.add(current)) {
                    continue;
                }
                if (ancestors2.contains(current)) {
                    return current;
                }
                Commit commit = Commit.fromFile(join(COMMITS_DIR, current));
                String parent = commit.getFirstParent();
                String secondParent = commit.getSecondParent();
                if (parent != null) {
                    queue1.offer(parent);
                }
                if (secondParent != null) {
                    queue1.offer(secondParent);
                }
            }

            if (!queue2.isEmpty()) {
                String current = queue2.poll();
                if (!ancestors2.add(current)) {
                    continue;
                }
                if (ancestors1.contains(current)) {
                    return current;
                }
                Commit commit = Commit.fromFile(join(COMMITS_DIR, current));
                String parent = commit.getFirstParent();
                String secondParent = commit.getSecondParent();
                if (parent != null) {
                    queue2.offer(parent);
                }
                if (secondParent != null) {
                    queue2.offer(secondParent);
                }
            }
        }

        return null;
    }

    private static String dealConflict(String fileName, String currentFileHash,
                                     String givenFileHash) {
        File file = join(CWD, fileName);
        String currentContent, givenContent;

        if (currentFileHash != null) {
            currentContent = readContentsAsString(join(BLOBS_DIR,
                    currentFileHash));
        } else {
            currentContent = "";
        }
        if (givenFileHash != null) {
            givenContent = readContentsAsString(join(BLOBS_DIR,
                    givenFileHash));
        } else {
            givenContent = "";
        }

        String conflictContent = "<<<<<<< HEAD\n" + currentContent
                + "=======\n" + givenContent + ">>>>>>>\n";

        writeContents(file, conflictContent);

        String hash = sha1(conflictContent);
        File blobFile = join(BLOBS_DIR, hash);
        writeContents(blobFile, conflictContent);
        return hash;
    }

    private static boolean checkGivenBranch(
            HashMap<String, String> givenBlobs,
            HashSet<String> lockedFiles,
            HashMap<String, String> splitBlobs,
            HashMap<String, String> currentBlobs,
            String givenCommitHash,
            HashMap<String, String> newBlobs,
            boolean conflict,
            List<String> workingDirectoryFiles) {

        /* Check files in given branch */
        for (Map.Entry<String, String> entry : givenBlobs.entrySet()) {
            String fileName = entry.getKey();
            String givenFileHash = entry.getValue();
            if (lockedFiles.contains(fileName)) {
                continue;
            }

            if (splitBlobs.containsKey(fileName)
                    && currentBlobs.containsKey(fileName)) {
                String splitFileHash = splitBlobs.get(fileName);
                String currentFileHash = currentBlobs.get(fileName);

                if (!Objects.equals(splitFileHash, givenCommitHash)
                        && Objects.equals(splitFileHash, currentFileHash)) {
                    writeContents(join(CWD, fileName),
                            readContents(join(BLOBS_DIR, givenFileHash)));
                    newBlobs.put(fileName, givenFileHash);
                } else if (!Objects.equals(splitFileHash, givenFileHash)
                        && !Objects.equals(splitFileHash, currentFileHash)
                        && !Objects.equals(currentFileHash, givenFileHash)) {
                    String hash = dealConflict(fileName, currentFileHash,
                            givenFileHash);
                    newBlobs.put(fileName, hash);
                    conflict = true;
                }
            } else if (!splitBlobs.containsKey(fileName)
                    && currentBlobs.containsKey(fileName)) {
                String currentFileHash = currentBlobs.get(fileName);

                if (!Objects.equals(currentFileHash, givenFileHash)) {
                    String hash = dealConflict(fileName, currentFileHash,
                            givenFileHash);
                    newBlobs.put(fileName, hash);
                    conflict = true;
                }
            } else if (!splitBlobs.containsKey(fileName)
                    && !currentBlobs.containsKey(fileName)) {

                if (workingDirectoryFiles.contains(fileName)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }

                writeContents(join(CWD, fileName),
                        readContents(join(BLOBS_DIR, givenFileHash)));
                newBlobs.put(fileName, givenFileHash);
            } else {
                String splitFileHash = splitBlobs.get(fileName);

                if (!Objects.equals(splitFileHash, givenFileHash)) {
                    String hash = dealConflict(fileName, null, givenFileHash);
                    newBlobs.put(fileName, hash);
                    conflict = true;
                }
            }
            lockedFiles.add(fileName);
        }
        return conflict;
    }

    private static boolean checkCurrentBranch(
            HashMap<String, String> currentBlobs,
            HashSet<String> lockedFiles,
            HashMap<String, String> splitBlobs,
            HashMap<String, String> givenBlobs,
            HashMap<String, String> newBlobs, boolean conflict) {

        /* Check files in current branch */
        for (Map.Entry<String, String> entry : currentBlobs.entrySet()) {
            String fileName = entry.getKey();
            String currentFileHash = entry.getValue();
            if (lockedFiles.contains(fileName)) {
                continue;
            }

            if (splitBlobs.containsKey(fileName) && !givenBlobs.containsKey(fileName)) {
                String splitFileHash = splitBlobs.get(fileName);
                if (Objects.equals(splitFileHash, currentFileHash)) {
                    File file = join(CWD, fileName);
                    file.delete();
                    newBlobs.remove(fileName);
                } else {
                    String hash = dealConflict(fileName, currentFileHash, null);
                    newBlobs.put(fileName, hash);
                    conflict = true;
                }
            }
            lockedFiles.add(fileName);
        }
        return conflict;
    }

    private static String findRemoteInitHash(File remoteDir) {
        String headHash = readContentsAsString(join(remoteDir, "HEAD"));
        String initHash = headHash;
        while (headHash != null) {
            initHash = headHash;
            headHash = Commit.fromFile(join(remoteDir, "objects", "commits",
                    headHash)).getFirstParent();
        }

        return initHash;
    }

    private static boolean isAncestor(String localHash, String remoteHash) {
        while (localHash != null) {
            if (localHash.equals(remoteHash)) {
                return true;
            }
            localHash = Commit.fromFile(join(COMMITS_DIR,
                    localHash)).getFirstParent();
        }
        return false;
    }

    private static String convertPath(String path) {
        return path.replace("/", File.separator);
    }
}
