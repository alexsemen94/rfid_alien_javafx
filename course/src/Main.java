public class Main {
    public static void main(String[] args) {
        Creature b = new Mushroom();
        System.out.println(b);
        Creature child = b.reproduce();
        ((Bird)child).fly();
        System.out.println(child);
    }
}
