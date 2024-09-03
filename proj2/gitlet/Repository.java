package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;


import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
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
    public static final File COMMITS_DIR = join(GITLET_DIR, "objects", "commits");
    /** The branches' directory. */
    public static final File BRANCHES_DIR = join(GITLET_DIR, "heads");
    /** The staging file. */
    public static final File STAGING_FILE = join(GITLET_DIR, "staging");
    /** The head file. */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    /** Default branch. */
    public static final String DEFAULT_BRANCH = "master";
    /** The current branch. */
    private String currentBranch = DEFAULT_BRANCH;


    public void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }
        /* Create the .gitlet directory and its subdirectories */
        GITLET_DIR.mkdir();
        BLOBS_DIR.mkdirs();
        COMMITS_DIR.mkdirs();
        BRANCHES_DIR.mkdirs();
        STAGING_FILE.createNewFile();
        HEAD_FILE.createNewFile();

        /* Create the initial commit */
        Commit initialCommit = new Commit("initial commit", null, null);
        submitCommit(initialCommit, DEFAULT_BRANCH);

        /* Create a staging file */
        Stage stage = new Stage();
        writeObject(STAGING_FILE, stage);
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
        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR, readContentsAsString(HEAD_FILE)));
        if (currentCommit.containsSameFile(fileName, hash)) {
            if (stage.containsStagedFile(fileName)) {
                String oldHash = stage.getHash(fileName);
                stage.removeStagedFile(fileName);

                /* Remove the previous version of the file from the blobs directory */
                File blobFile = join(BLOBS_DIR, oldHash);
                if (!blobFile.exists()) {
                    System.out.println("File in previous version does not exist.");
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
        if (stage.getStagedFiles().isEmpty() && stage.getRemovedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        /* Create the commit */
        String currentHash = readContentsAsString(HEAD_FILE);
        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR, currentHash));
        Commit newCommit = new Commit(message, currentHash, currentCommit.getBlobs());
        newCommit.changeBlobs(stage.getStagedFiles());
        newCommit.removeBlobs(stage.getRemovedFiles());

        /* Submit the commit */
        submitCommit(newCommit, currentBranch);

        /* Clear the staging area */
        stage.clear();
        writeObject(STAGING_FILE, stage);
    }

    public void rm(String fileName) {
        /* Error checking */
        checkGitletDir();

        Stage stage = Stage.fromFile(STAGING_FILE);
        Commit currentCommit = Commit.fromFile(join(COMMITS_DIR, readContentsAsString(HEAD_FILE)));
        if (!stage.containsStagedFile(fileName) && !currentCommit.containsFile(fileName)) {
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
                System.out.println("Merge: " + commit.getParent().substring(0, 7) + " " + commit.getSecondParent().substring(0, 7));
            }
            System.out.println("Date: " + commit.getTime());
            System.out.println(commit.getMessage());
            System.out.println();
            hash = commit.getParent();
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
                System.out.println("Merge: " + commit.getParent().substring(0, 7) + " " + commit.getSecondParent().substring(0, 7));
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
        /* Error checking */
        checkGitletDir();

        /* Print branches */
        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        if (branches == null) {
            return;
        }
        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();

        Stage stage = Stage.fromFile(STAGING_FILE);
        Commit commit = Commit.fromFile(join(COMMITS_DIR, readContentsAsString(HEAD_FILE)));
        List<String> workingDirectoryFiles = plainFilenamesIn(CWD);
        if (workingDirectoryFiles == null) {
            return;
        }

        /* Check files */
        TreeSet<String> stagedFiles = new TreeSet<>();
        TreeSet<String>  removedFiles = new TreeSet<>();
        TreeMap<String, Integer> modifiedFiles = new TreeMap<>();
        TreeSet<String>  trackedFiles = new TreeSet<>();
        TreeSet<String>  untrackedFiles = new TreeSet<>();

        HashMap<String, String> addedStagedFiles = stage.getStagedFiles();
        HashSet<String> removedStagedFiles = stage.getRemovedFiles();
        HashMap<String, String> commitFiles = commit.getBlobs();

        /* Check files staged for addition */
        for (Map.Entry<String, String> entry : addedStagedFiles.entrySet()) {
            String fileName = entry.getKey();
            File file = join(CWD, fileName);
            if (!file.exists()) {
                /* Staged for addition, but deleted in the working directory */
                modifiedFiles.put(fileName, 0);
            } else {
                String localHash = sha1(readContents(file));
                if (entry.getValue().equals(localHash)) {
                    /* Staged for addition, but with different contents than in the working directory */
                    stagedFiles.add(fileName);
                } else {
                    /* Staged for addition, modified in the working directory */
                    modifiedFiles.put(fileName, 1);
                }
                trackedFiles.add(fileName);
            }
        }

        for (Map.Entry<String, String> entry : commitFiles.entrySet()) {
            String fileName = entry.getKey();
            File file = join(CWD, fileName);
            if (!file.exists()) {
                if (!removedStagedFiles.contains(fileName)) {
                    /*
                     * Not staged for removal, but tracked in the current
                     * commit and deleted from the working directory
                     */
                    modifiedFiles.put(fileName, 0);
                }
            } else {
                String localHash = sha1(readContents(file));
                if (!entry.getValue().equals(localHash) && !addedStagedFiles.containsKey(fileName) && !removedStagedFiles.contains(fileName)) {
                    /* Tracked in the current commit, changed in the working directory, but not staged */
                    modifiedFiles.put(fileName, 1);
                }
                trackedFiles.add(fileName);
            }
        }

        for (String fileName : removedStagedFiles) {
            File file = join(CWD, fileName);
            if (file.exists()) {
                /* have been staged for removal, but then re-created without Gitlet’s knowledge */
                untrackedFiles.add(fileName);
            } else {
                removedFiles.add(fileName);
            }
        }

        for (String fileName : workingDirectoryFiles) {
            if (!trackedFiles.contains(fileName)) {
                untrackedFiles.add(fileName);
            }
        }

        /* Print files */
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

        System.out.println("=== Untracked Files ===");
        for (String fileName : untrackedFiles) {
            System.out.println(fileName);
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
        if (branch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        /* Checkout target commit */
        String targetHash = readContentsAsString(branchFile);
        checkoutCommit(targetHash);

        /* Update the current branch */
        currentBranch = branch;
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
        writeContents(join(BRANCHES_DIR, currentBranch), fullHash);

        /* Update the head file */
        writeContents(HEAD_FILE, fullHash);
    }

    private void checkGitletDir() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private void submitCommit(Commit commit, String branch) throws IOException {
        String hash = sha1(serialize(commit));
        File commitFile = join(COMMITS_DIR, hash);
        commitFile.createNewFile();
        writeObject(commitFile, commit);

        writeContents(HEAD_FILE, hash);

        File branchFile = join(BRANCHES_DIR, branch);
        writeContents(branchFile, hash);
    }

    private String findFullCommitHash(String hash) {
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

    private void checkoutCommit(String targetHash) {
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
            if ((!currentCommit.containsFile(fileName) && !stage.containsStagedFile(fileName)) || stage.containsRemovedFile(fileName)) {
                System.out.println("There is an untracked file in the way; delete it or add it first.");
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
        for (Map.Entry<String, String> entry : targetCommit.getBlobs().entrySet()) {
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
}
