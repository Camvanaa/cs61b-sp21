package gitlet.commands;

public class FindCommand implements Command {
    private String commitMessage;

    public FindCommand(String[] args) {
        validateNumArgs(args, 2);
        this.commitMessage = args[1];
    }

    @Override
    public void execute() {

    }
}
