public class Creature {
    Creature reproduce() {
        Class c = this.getClass();
        try {
            return (Creature) c.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
