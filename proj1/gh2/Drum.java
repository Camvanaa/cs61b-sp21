package gh2;

public class Drum extends StringInstrument {
    private static final double DECAY = 1.0;

    public Drum(double frequency) {
        super(frequency, 1.0);
    }

    @Override
    public void tic() {
        double front = buffer.removeFirst();
        double next = buffer.get(0);

        double avg = (front + next) * 0.5;
        if (Math.random() < 0.5) {
            avg = -avg;
        }

        double newDouble = avg * DECAY;
        buffer.addLast(newDouble);
    }
}
