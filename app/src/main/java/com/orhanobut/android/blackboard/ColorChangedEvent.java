package com.orhanobut.android.blackboard;

/**
 * @author Orhan Obut
 */
final class ColorChangedEvent {

    private final int color;

    public ColorChangedEvent(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
