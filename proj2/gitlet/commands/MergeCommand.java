package gitlet.commands;

import gitlet.Repository;

public class MergeCommand implements Command {
    private final String branchName;
    private final String[] args;


    public MergeCommand(String[] args) {
        this.args = args;
        this.branchName = args[1];
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 2);
        Repository.merge(branchName);
    }
}
