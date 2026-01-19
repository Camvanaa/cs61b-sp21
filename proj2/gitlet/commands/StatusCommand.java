package gitlet.commands;

import gitlet.Repository;

public class StatusCommand implements Command {
    private final String[] args;

    public StatusCommand(String[] args) {
        this.args = args;
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 1);
        Repository.status();
    }
}
