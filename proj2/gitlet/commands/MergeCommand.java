package gitlet.commands;

public class MergeCommand implements Command {
    private String branchName;

    public MergeCommand(String[] args) {
        validateNumArgs(args, 2);
        this.branchName = args[1];
    }

    @Override
    public void execute() {

    }
}
