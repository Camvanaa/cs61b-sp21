package gitlet.commands;

import gitlet.Repository;

public class ResetCommand implements Command {
    private final String commitId;
    private final String[] args;


    public ResetCommand(String[] args) {
        this.args = args;
        this.commitId = args[1];
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 2);
        Repository.reset(commitId);
    }
}
