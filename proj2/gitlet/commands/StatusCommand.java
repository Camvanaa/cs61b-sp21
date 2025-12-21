package gitlet.commands;

public class StatusCommand implements Command {
    public StatusCommand(String[] args) {
        validateNumArgs(args, 1);
    }

    @Override
    public void execute() {

    }
}
