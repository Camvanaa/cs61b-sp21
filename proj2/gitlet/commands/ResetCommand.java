package gitlet.commands;

public class ResetCommand implements Command {
    private String commitID;

    public ResetCommand(String[] args) {
        validateNumArgs(args, 2);
        this.commitID = args[1];
    }

    @Override
    public void execute() {

    }
}
