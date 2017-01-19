package macchina;

import net.JoystickListener;

public class Main {
    public static void main(String[] args) {
        JoystickListener listener = new JoystickListener(25565);
        listener.start();

        Macchina macchina = new Macchina(listener);
        macchina.start();
    }
}
