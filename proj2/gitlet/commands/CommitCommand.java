package gitlet.commands;

public class CommitCommand implements Command {
    private String message;

    public CommitCommand(String[] args) {
        validateNumArgs(args, 2);
        this.message = args[1];
    }

    @Override
    public void execute() {

    }
}
