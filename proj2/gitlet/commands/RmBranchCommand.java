package gitlet.commands;

public class RmBranchCommand implements Command {
    private String branchName;

    public RmBranchCommand(String[] args) {
        validateNumArgs(args, 2);
        this.branchName = args[1];
    }
    @Override
    public void execute() {

    }
}
