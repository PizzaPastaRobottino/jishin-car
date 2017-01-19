package macchina;

import joystick.shared.JoystickState;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;
import net.JoystickListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Macchina {
    protected enum Marcia {
        M_R, M_N, M_1, M_2, M_3, M_4; // reverse, neutral, 1, 2, 3, 4

        private static Marcia[] vals = values();

        public Marcia next() {
            if (this.ordinal() == vals.length - 1) return vals[this.ordinal()];
            return vals[(this.ordinal() + 1) % vals.length];
        }

        public Marcia previous() {
            if (this.ordinal() == 0) return vals[this.ordinal()];
            return vals[(this.ordinal() - 1 + vals.length) % vals.length];
        }
    }

    protected class ParametriMarcia {
        public final int statoLaterale;
        public final float posizione;

        public ParametriMarcia(int statoLaterale, float posizione) {
            this.statoLaterale = statoLaterale;
            this.posizione = posizione;
        }
    }

    private final Map<Marcia, ParametriMarcia> parametriMarce = new HashMap<>();

    private final static int STERZO_GRADI = 90;
    private final static int SHIFT_LATERALE_GRADI = 40;
    private final static int SHIFT_POSIZIONE_GRADI = 180;

    private final JoystickListener listener;
    private final BlockingQueue<Boolean> shiftQueue = new LinkedBlockingQueue<>(); // true => up, false => down

    private Marcia marciaAttuale = Marcia.M_N; // di default in folle
    private final AtomicBoolean canUsePower = new AtomicBoolean(true);

    private final EV3LargeRegulatedMotor motore = new EV3LargeRegulatedMotor(MotorPort.A);
    private final EV3MediumRegulatedMotor sterzo = new EV3MediumRegulatedMotor(MotorPort.B);
    private final EV3MediumRegulatedMotor cambioLaterale = new EV3MediumRegulatedMotor(MotorPort.C);
    private final EV3MediumRegulatedMotor cambioPosizione = new EV3MediumRegulatedMotor(MotorPort.D);

    public Macchina(JoystickListener listener) {
        this.listener = listener;

        parametriMarce.put(Marcia.M_R, new ParametriMarcia(-1, 0f));
        parametriMarce.put(Marcia.M_N, new ParametriMarcia(0, 0f));
        parametriMarce.put(Marcia.M_1, new ParametriMarcia(-1, 0f));
        parametriMarce.put(Marcia.M_2, new ParametriMarcia(1, 0f));
        parametriMarce.put(Marcia.M_3, new ParametriMarcia(-1, 1f));
        parametriMarce.put(Marcia.M_4, new ParametriMarcia(1, 1f));

        motore.setAcceleration(1500);
        sterzo.setAcceleration(3000);

        //cambioLaterale.setAcceleration(500);
        cambioPosizione.setAcceleration(5000);
    }

    private void startShiftThread() {
        Runnable shiftRunnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Avviato shift thread");

                try {
                    while (true) {
                        Marcia shiftTo = shiftQueue.take() ? marciaAttuale.next() : marciaAttuale.previous(); // aspetta l'elemento
                        if (shiftTo.equals(marciaAttuale)) continue;

                        //canUsePower.set(false);
                        ParametriMarcia nuovoStato = parametriMarce.get(shiftTo);

                        System.out.println("Shift to " + shiftTo.toString());

                        //int motorSpeed = motore.getSpeed();

                        cambioLaterale.rotateTo(0); // Neutral
                        Delay.msDelay(75);
                        cambioPosizione.rotateTo((int) (nuovoStato.posizione * SHIFT_POSIZIONE_GRADI));

                        //motore.setSpeed(0f);
                        Delay.msDelay(75);
                        cambioLaterale.rotateTo(nuovoStato.statoLaterale * SHIFT_LATERALE_GRADI); // marcia scelta
                        //motore.setSpeed(motorSpeed);
                        //canUsePower.set(true);

                        marciaAttuale = shiftTo;
                    }
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        };

        Thread shiftThread = new Thread(shiftRunnable);
        shiftThread.start();
    }

    public void start() {
        startShiftThread();

        try {
            while (true) {
                JoystickState state = this.listener.getLastState();
                if (state == null) {
                    Thread.sleep(25);
                    continue;
                }

                if (state.isShiftUp()) // mutualmente esclusivi
                    shiftQueue.put(Boolean.TRUE);
                else if (state.isShiftDown())
                    shiftQueue.put(Boolean.FALSE);

                setAcceleratore(state.getAcceleratore());
                setSterzo(state.getSterzo());
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    private void setAcceleratore(float value) {
        if (!canUsePower.get()) return;

        if (Math.abs(value) <= 0.05) {
            motore.stop();
            return;
        }

        motore.setSpeed(value * motore.getMaxSpeed());
        if (marciaAttuale.equals(Marcia.M_R))
            motore.backward();
        else
            motore.forward();
    }

    private void setSterzo(float value) {
        value *= -1; // Lo sterzo è sul retro

        if (value < -1f) value = -1f;
        else if (value > 1f) value = 1f;

        int valoreAttuale = sterzo.getTachoCount();

        if (Math.abs(valoreAttuale - Math.round(value * STERZO_GRADI)) < 2) return;

        sterzo.rotateTo(Math.round(value * STERZO_GRADI));
    }
}
