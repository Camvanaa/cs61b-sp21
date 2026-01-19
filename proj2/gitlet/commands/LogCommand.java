package gitlet.commands;

import gitlet.Repository;

public class LogCommand implements Command {
    private final String[] args;

    public LogCommand(String[] args) {
        this.args = args;
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 1);
        Repository.log();
    }
}
