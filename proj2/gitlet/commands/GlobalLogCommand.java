package gitlet.commands;

import gitlet.Repository;

public class GlobalLogCommand implements Command {
    private final String[] args;
    public GlobalLogCommand(String[] args) {
        this.args = args;
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 1);
        Repository.globalLog();
    }
}
