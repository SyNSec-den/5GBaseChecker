package org.example.uelearner.devices;

import org.example.uelearner.ue.UESUL;

public abstract class DeviceSUL {
    UESUL uesul;

    public DeviceSUL(UESUL uesul) {
        this.uesul = uesul;
    }

    public abstract void pre();

    public abstract void post();

    public abstract String step(String symbol);
}
