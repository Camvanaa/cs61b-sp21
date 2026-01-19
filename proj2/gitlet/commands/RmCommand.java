package gitlet.commands;

import gitlet.Repository;

public class RmCommand implements Command {
    private final String fileName;
    private final String[] args;

    public RmCommand(String[] args) {
        this.args = args;
        this.fileName = args[1];
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 2);
        Repository.rm(fileName);
    }
}
