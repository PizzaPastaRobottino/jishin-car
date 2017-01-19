package joystick.shared;

import java.io.Serializable;

public class JoystickState implements Serializable {
    private static final long serialVersionUID = 4242424242L;

    private boolean shiftUp;
    private boolean shiftDown;

    private float sterzo; // -1f => 1f
    private float acceleratore; // 0f => 1f

    public JoystickState(boolean shiftUp, boolean shiftDown, float sterzo, float acceleratore) {
        this.shiftUp = shiftUp;
        this.shiftDown = shiftDown;
        this.sterzo = sterzo;
        this.acceleratore = acceleratore;
    }

    public JoystickState() {
        this.shiftUp = this.shiftDown = false;
        this.sterzo = this.acceleratore = 0f;
    }

    public boolean isShiftUp() {
        return shiftUp;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public float getSterzo() {
        return sterzo;
    }

    public float getAcceleratore() {
        return acceleratore;
    }

    public JoystickState difference(JoystickState a) {
        boolean diffShiftUp = false;
        boolean diffShiftDown = false;

        if (shiftUp != a.shiftUp)
            diffShiftUp = a.shiftUp;

        if (shiftDown != a.shiftDown)
            diffShiftDown = a.shiftDown;

        return new JoystickState(
                diffShiftUp,
                diffShiftDown,
                a.sterzo,
                a.acceleratore
        );
    }

    public void setShiftUp(boolean shiftUp) {
        this.shiftUp = shiftUp;
    }

    public void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    public void setSterzo(float sterzo) {
        this.sterzo = sterzo;
    }

    public void setAcceleratore(float acceleratore) {
        this.acceleratore = acceleratore;
    }

    @Override
    public String toString() {
        return "JoystickState{" +
                "shiftUp=" + shiftUp +
                ", shiftDown=" + shiftDown +
                ", sterzo=" + sterzo +
                ", acceleratore=" + acceleratore +
                '}';
    }
}
