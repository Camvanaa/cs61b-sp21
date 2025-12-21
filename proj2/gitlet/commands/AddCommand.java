package gitlet.commands;

public class AddCommand implements Command {
    private String fileName;

    public AddCommand(String[] args) {
        validateNumArgs(args, 2);
        this.fileName = args[1];
    }

    @Override
    public void execute() {

    }
}
