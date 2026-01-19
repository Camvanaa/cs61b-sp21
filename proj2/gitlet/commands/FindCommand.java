package gitlet.commands;

import gitlet.Repository;

public class FindCommand implements Command {
    private final String[] args;
    public FindCommand(String[] args) {
        this.args = args;
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 2);
        Repository.find(args[1]);
    }
}
