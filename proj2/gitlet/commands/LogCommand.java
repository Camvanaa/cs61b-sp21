package gitlet.commands;

public class LogCommand implements Command {
    public LogCommand(String[] args) {
        validateNumArgs(args, 1);
    }

    @Override
    public void execute() {

    }
}
