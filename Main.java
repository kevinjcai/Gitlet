package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Kevin Cai
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        if (!Repository.GITLET_DIR.exists() && !firstArg.equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        switch (firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                if (Repository.GITLET_DIR.exists()) {
                    System.out.print("A Gitlet version-control system already"
                            + " exists in the current directory.");
                } else {
                    Repository.setupPersistence();
                    Repository.createInit();
                }
                break;

            case "log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.log();
                break;

            case "global-log":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.globalLog();
                break;

            case "status":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.status();
                break;

            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.add(args[1]);
                break;


            case "commit":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.createCommit(args[1]);
                break;

            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.remove(args[1]);
                break;

            case "checkout":
                if (args.length == 2) {
                    Repository.branchCheckout(args[1]);
                } else if (args.length == 3 && args[1].equals("--")) {
                    Repository.fileCheckout(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.fileCheckout(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                }
                break;

            case "find":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.find(args[1]);
                break;

            case "branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.branch(args[1]);
                break;

            case "rm-branch":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.rmBranch(args[1]);
                break;

            case "reset":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.reset(args[1]);
                break;

            case "merge":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.merge(args[1]);
                break;

            case "add-remote":
                if (args.length != 3) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.addRemote(args[1], args[2]);
                break;

            case "rm-remote":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    break;
                }
                Repository.rmRemote(args[1]);
                break;

            default:
                System.out.println("No command with that name exists.");
        }
    }
}
