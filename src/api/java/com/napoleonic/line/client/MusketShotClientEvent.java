package com.napoleonic.line.client;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired on the client when a line shot should show smoke.
 * The effects addon cancels this and draws its own smoke and tracers.
 */
@Cancelable
public class MusketShotClientEvent extends Event {
    private final Vec3 muzzleOrigin;
    private final Vec3 direction;
    private final Vec3 panOrigin;

    public MusketShotClientEvent(Vec3 muzzleOrigin, Vec3 direction, Vec3 panOrigin) {
        this.muzzleOrigin = muzzleOrigin;
        this.direction = direction;
        this.panOrigin = panOrigin;
    }

    public Vec3 getMuzzleOrigin() {
        return this.muzzleOrigin;
    }

    public Vec3 getDirection() {
        return this.direction;
    }

    public Vec3 getPanOrigin() {
        return this.panOrigin;
    }
}
