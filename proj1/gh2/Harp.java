package gh2;

public class Harp extends StringInstrument {
    private static final double DECAY = 0.996;

    public Harp(double frequency) {
        super(frequency, 0.5);
    }

    @Override
    public void tic() {
        double front = buffer.removeFirst();
        double next = buffer.get(0);
        double newDouble = -1 * (front + next) * 0.5 * DECAY;
        buffer.addLast(newDouble);
    }
}
