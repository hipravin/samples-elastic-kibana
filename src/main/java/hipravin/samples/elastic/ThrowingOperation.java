package hipravin.samples.elastic;

@FunctionalInterface
public interface ThrowingOperation<E extends Exception> {
    void perform() throws E;
}
