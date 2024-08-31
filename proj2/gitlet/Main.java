package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author skye-ye
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        Repository repo = new Repository();
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkArgs(args, 1);
                repo.init();
                break;
            case "add":
                checkArgs(args, 2);
                repo.add(args[1]);
                break;
            case "commit":
                if (args.length < 2) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                checkArgs(args, 2);
                repo.commit(args[1]);
                break;
            case "rm":
                checkArgs(args, 2);
                repo.rm(args[1]);
                break;
            case "log":
                checkArgs(args, 1);
                repo.log();
                break;
            case "global-log":
                checkArgs(args, 1);
                repo.globalLog();
                break;
            case "find":
                checkArgs(args, 2);
                repo.find(args[1]);
                break;
            case "status":
                break;
            case "checkout":
                break;
            case "branch":
                break;
            case "rm-branch":
                break;
            case "reset":
                break;
            case "merge":
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static void checkArgs(String[] args, int numArgs) {
        if (args.length != numArgs) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
