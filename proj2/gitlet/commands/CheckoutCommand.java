package gitlet.commands;

import gitlet.Repository;

public class CheckoutCommand implements Command {
    private final String[] args;

    public CheckoutCommand(String[] args) {
        this.args = args;
    }

    @Override
    public void execute() {
        hasDir();
        if (args.length == 3 && args[1].equals("--")) {
            Repository.checkout1(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            Repository.checkout2(args[1], args[3]);
        } else if (args.length == 2 && !args[1].equals("--")) {
            Repository.checkout3(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
