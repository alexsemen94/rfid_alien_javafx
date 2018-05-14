public class Pinguin extends Bird {
    @Override
    public void fly() {
        throw new RuntimeException("Cannot fly");
    }
}
