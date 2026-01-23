package gitlet.commands;

import gitlet.Repository;

public class CommitCommand implements Command {
    private final String[] args;

    public CommitCommand(String[] args) {
        this.args = args;
    }

    @Override
    public void execute() {
        hasDir();
        if (args.length < 2 || args[1].isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        validateNumArgs(args, 2);
        Repository.commit(args[1], null);
    }
}
