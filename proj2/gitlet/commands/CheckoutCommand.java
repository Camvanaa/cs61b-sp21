package gitlet.commands;

public class CheckoutCommand implements Command {
    public CheckoutCommand(String[] args) {
        if (args.length < 2 || args.length > 4) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    @Override
    public void execute() {

    }

}
