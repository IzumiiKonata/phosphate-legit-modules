package tech.konata.phosphate.legit.utils;

/**
 * @author IzumiiKonata
 * Date: 2025/1/28 21:23
 */
public class Pair<A, B> {

    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }

}
