package gitlet;

// TODO: any imports you need here
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Serializable;
import java.io.File;

import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author skye-ye
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private final String message;
    /** The time of this Commit. */
    private final Instant time;
    /** The parent of this Commit. */
    private final String parent;
    /** The second parent if this Commit is a merge. */
    private String secondParent;
    /** The blobs of this Commit. */
    private final HashMap<String, String> blobs;

    /* TODO: fill in the rest of this class. */
    public Commit(String message, String parent, HashMap<String, String> blobs) {
        this.message = message;
        this.parent = parent;
        this.secondParent = null;
        if (parent == null) {
            this.time = Instant.EPOCH;
        } else {
            this.time = Instant.now();
        }
        if (blobs == null) {
            this.blobs = new HashMap<>();
        } else {
            this.blobs = new HashMap<>(blobs);
        }
    }

    public static Commit fromFile(File fileName) {
        if (!fileName.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return readObject(fileName, Commit.class);
    }

    /** Check if a certain file is in the commit. */
    public boolean containsFile(String fileName) {
        return blobs.containsKey(fileName);
    }

    /** Check if a certain file is in the commit and has the same sha1. */
    public boolean containsSameFile(String fileName, String sha1) {
        return blobs.containsKey(fileName) && blobs.get(fileName).equals(sha1);
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public void changeBlobs(HashMap<String, String> blobs) {
        this.blobs.putAll(blobs);
    }

    public void removeBlobs(List<String> fileNames) {
        for (String fileName : fileNames) {
            blobs.remove(fileName);
        }
    }

    public String getParent() {
        return parent;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy Z")
                .withZone(ZoneId.systemDefault());
        return formatter.format(this.time);
    }

    public void setSecondParent(String secondParent) {
        this.secondParent = secondParent;
    }

    public String getSecondParent() {
        return secondParent;
    }
}
