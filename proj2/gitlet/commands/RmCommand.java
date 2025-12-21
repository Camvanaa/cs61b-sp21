package gitlet.commands;

public class RmCommand implements Command {
    private String fileName;

    public RmCommand(String[] args) {
        validateNumArgs(args, 2);
        this.fileName = args[1];
    }

    @Override
    public void execute() {

    }
}
