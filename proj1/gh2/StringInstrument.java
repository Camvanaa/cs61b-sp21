package gh2;

import deque.Deque;
import deque.ArrayDeque;

public abstract class StringInstrument {

    protected Deque<Double> buffer;
    protected int capacity;

    public static final int SR = 44100;

    public StringInstrument(double frequency, double capacityMult) {
        this.capacity = (int) Math.round(SR / frequency * capacityMult);
        buffer = new ArrayDeque<>();
        for (int i = 0; i < capacity; i++) {
            buffer.addLast(0.0);
        }
    }

    public void pluck() {
        for (int i = 0; i < capacity; i++) {
            buffer.removeFirst();
            double r = Math.random() - 0.5;
            buffer.addLast(r);
        }
    }

    public double sample() {
        return buffer.get(0);
    }

    public abstract void tic();
}
