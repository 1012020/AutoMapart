package me.bebeli555.automapart.events.render;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends Cancellable {
    public ParticleEffect effect;

    public ParticleEvent(ParticleEffect effect) {
        this.effect = effect;
    }
}
