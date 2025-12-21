package gitlet.commands;

public class BranchCommand implements Command {
    private String branchName;

    public BranchCommand(String[] args) {
        validateNumArgs(args, 2);
        this.branchName = args[1];
    }
    @Override
    public void execute() {

    }
}
