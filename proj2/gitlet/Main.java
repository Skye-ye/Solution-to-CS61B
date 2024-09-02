package gitlet;

import java.io.IOException;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author skye-ye
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Please enter a command.");
                System.exit(0);
            }
            Repository repo = new Repository();
            String firstArg = args[0];
            switch (firstArg) {
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
                    checkArgs(args, 1);
                    repo.status();
                    break;
                case "checkout":
                    if (args.length == 2) {
                        repo.checkoutBranch(args[1]);
                    } else if (args.length == 3) {
                        if (!args[1].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        repo.checkoutFileFromCurrentCommit(args[2]);
                    } else if (args.length == 4) {
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        repo.checkoutFileFromCommit(args[1], args[3]);
                    } else {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    break;
                case "branch":
                    checkArgs(args, 2);
                    repo.branch(args[1]);
                    break;
                case "rm-branch":
                    checkArgs(args, 2);
                    repo.rmBranch(args[1]);
                    break;
                case "reset":
                    checkArgs(args, 2);
                    repo.reset(args[1]);
                    break;
                case "merge":
                    break;
                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkArgs(String[] args, int numArgs) {
        if (args.length != numArgs) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
