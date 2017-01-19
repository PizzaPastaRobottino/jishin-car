package net;

import joystick.shared.JoystickState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class JoystickListener extends Thread {
    private final int port;
    private JoystickState lastState = null;
    private AtomicBoolean readState = new AtomicBoolean(true);

    public JoystickListener(int port) {
        super("JoystickListener");
        this.port = port;
    }

    @Override
    public void run() {
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("Socket listening on port " + port);

        while (true) {
            ObjectInputStream inputStream;

            try {
                Socket connectionSocket = socket.accept();
                System.out.println("New connection from " + connectionSocket.getRemoteSocketAddress().toString());
                inputStream = new ObjectInputStream(connectionSocket.getInputStream());
            } catch (IOException e) {
                System.err.println(e.getMessage());
                continue;
            }


            while (true) {
                try {
                    lastState = (JoystickState) inputStream.readObject();
                    readState.set(false); // Non letto

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println(e.getMessage());
                    break;
                }
            }

            lastState = new JoystickState(); // Alla disconnessione resetto lo state
        }
    }

    public JoystickState getLastState() {
        boolean canRead = readState.compareAndSet(false, true);
        if (canRead)
            return lastState;

        return null;
    }
}
