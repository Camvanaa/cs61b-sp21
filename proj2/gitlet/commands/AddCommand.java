package gitlet.commands;

import gitlet.Repository;

import static gitlet.Utils.join;

public class AddCommand implements Command {
    private final String fileName;
    private final String[] args;


    public AddCommand(String[] args) {
        this.args = args;
        this.fileName = args[1];
    }

    @Override
    public void execute() {
        hasDir();
        validateNumArgs(args, 2);
        if (!join(Repository.CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Repository.add(fileName);
    }
}
