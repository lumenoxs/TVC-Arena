package net.tvc.managers;

public class Manager {
    protected boolean initialized = false;

    public Manager(boolean ...registerAtStartUp) {
        if (registerAtStartUp.length > 0 && !registerAtStartUp[0])
            return;

        this.initialized = true;
    }

    public void register() {
        System.out.println("[Manager] Registered: " + name());
    }

    public final boolean isInitialized() {
        return this.initialized;
    }

    public final String name() {
        return getClass().getSimpleName();
    }

    public final String toString() {
        return name();
    }

    public final boolean equals(Object obj) {
        return obj instanceof Manager && ((Manager) obj).name().equals(name());
    }

    public final int hashCode() {
        return name().hashCode();
    }
}
