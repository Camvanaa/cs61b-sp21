package gitlet.commands;

import gitlet.Repository;

public class InitCommand implements Command {
    private final String[] args;
    public InitCommand(String[] args) {
        this.args = args;
    }

    @Override
    public void execute() {
        validateNumArgs(args, 1);
        if (Repository.GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        Repository.init();
    }
}
