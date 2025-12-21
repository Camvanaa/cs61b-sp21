package gitlet;

import gitlet.commands.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author camvan
 */
public class Main {
    private static final Map<String, Function<String[], Command>> COMMAND_MAP = new HashMap<>();

    static {
        COMMAND_MAP.put("add", AddCommand::new);
        COMMAND_MAP.put("branch", BranchCommand::new);
        COMMAND_MAP.put("checkout", CheckoutCommand::new);
        COMMAND_MAP.put("commit", CommitCommand::new);
        COMMAND_MAP.put("find", FindCommand::new);
        COMMAND_MAP.put("global-log", GlobalLogCommand::new);
        COMMAND_MAP.put("init", InitCommand::new);
        COMMAND_MAP.put("log", LogCommand::new);
        COMMAND_MAP.put("merge", MergeCommand::new);
        COMMAND_MAP.put("reset", ResetCommand::new);
        COMMAND_MAP.put("rm-branch", RmBranchCommand::new);
        COMMAND_MAP.put("rm", RmCommand::new);
        COMMAND_MAP.put("status", StatusCommand::new);
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please enter a command.");
            System.exit(1);
        }

        String commandName = args[0];

        Function<String[], Command> creator = COMMAND_MAP.get(commandName);

        if (creator == null) {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }

        Command cmd = creator.apply(args);
        cmd.execute();
    }
}
