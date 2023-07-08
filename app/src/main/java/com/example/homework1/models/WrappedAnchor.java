package com.example.homework1.models;

import com.google.ar.core.Anchor;
import com.google.ar.core.Trackable;

public class WrappedAnchor {
    private final Anchor anchor;
    private final Trackable trackable;

    public WrappedAnchor(Anchor anchor, Trackable trackable) {
        this.anchor = anchor;
        this.trackable = trackable;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public Trackable getTrackable() {
        return trackable;
    }
}
