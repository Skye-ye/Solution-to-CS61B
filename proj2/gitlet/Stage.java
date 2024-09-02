package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.*;

public class Stage implements Serializable {
    private final HashMap<String, String> stagedFiles;
    private final HashSet<String> removedFiles;

    public Stage() {
        stagedFiles = new HashMap<>();
        removedFiles = new HashSet<>();
    }

    public void add(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        stagedFiles.put(fileName, sha1(readContents(file)));
        removedFiles.remove(fileName);
    }

    public void remove(String fileName) {
        if (!stagedFiles.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        stagedFiles.remove(fileName);
        removedFiles.remove(fileName);
    }

    public void clear() {
        stagedFiles.clear();
        removedFiles.clear();
    }

    public boolean containsStagedFile(String fileName) {
        return stagedFiles.containsKey(fileName);
    }

    public boolean containsSameStagedFile(String fileName, String sha1) {
        return stagedFiles.containsKey(fileName) && stagedFiles.get(fileName).equals(sha1);
    }

    public boolean containsRemovedFile(String fileName) {
        return removedFiles.contains(fileName);
    }

    public boolean containsFile(String fileName) {
        return stagedFiles.containsKey(fileName) || removedFiles.contains(fileName);
    }

    public static Stage fromFile(File fileName) {
        if (!fileName.exists()) {
            System.out.println("No stage file found.");
            System.exit(0);
        }
        return readObject(fileName, Stage.class);
    }

    public String getHash(String fileName) {
        return stagedFiles.get(fileName);
    }

    public HashMap<String, String> getStagedFiles() {
        return stagedFiles;
    }

    public void addRemovedFile(String fileName) {
        removedFiles.add(fileName);
    }

    public HashSet<String> getRemovedFiles() {
        return removedFiles;
    }
}
