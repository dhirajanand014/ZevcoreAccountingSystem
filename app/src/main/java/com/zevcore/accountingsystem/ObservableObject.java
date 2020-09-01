package com.zevcore.accountingsystem;

import java.util.Observable;

/**
 * Observer class to process network changes to Splash screen or main activity
 */
public class ObservableObject extends Observable {
    private static ObservableObject instance;

    private ObservableObject() {
    }

    /**
     * Singleton Instance
     *
     * @return
     */
    public static ObservableObject getInstance() {
        if (null == instance) {
            instance = new ObservableObject();
        }
        return instance;
    }

    /**
     * Create new ObservableObject
     */
    public void reset() {
        instance = new ObservableObject();
    }

    /**
     * Call update method of respective implementation.
     *
     * @param data
     */
    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }
}
