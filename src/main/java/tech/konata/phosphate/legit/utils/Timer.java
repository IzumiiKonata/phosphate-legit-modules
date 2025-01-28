package tech.konata.phosphate.legit.utils;

import java.time.Duration;

/**
 * @author IzumiiKonata
 * @since 2023/12/10
 */
public class Timer {

    public long lastNs = System.nanoTime();

    public boolean isDelayed(long ms) {
        return this.isDelayed(ms * 1_000_000.0);
    }

    public boolean isDelayed(double nanoSeconds) {
        return (System.nanoTime() - this.lastNs)/* * 0.000001*/ >= nanoSeconds;
    }

    public boolean isDelayed(Duration duration) {
        return System.nanoTime() - lastNs >= duration.toNanos();
    }

    public boolean isDelayed(long nanoSeconds, boolean reset) {
        boolean delayed = this.isDelayed(nanoSeconds);

        if (delayed && reset) {
            this.reset();
            return true;
        }

        return delayed;
    }

    public void reset() {
        this.lastNs = System.nanoTime();
    }

    public Duration delayed() {
        return Duration.ofNanos(System.nanoTime() - lastNs);
    }

}
