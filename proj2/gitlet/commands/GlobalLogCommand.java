package gitlet.commands;

public class GlobalLogCommand implements Command {
    public GlobalLogCommand(String[] args) {
        validateNumArgs(args, 1);
    }

    @Override
    public void execute() {

    }
}
