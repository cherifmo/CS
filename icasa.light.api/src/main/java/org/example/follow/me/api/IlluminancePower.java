package org.example.follow.me.api;

/**
 * Created by aygalinc on 09/11/16.
 */
public enum IlluminancePower {

    /** The goal associated with soft illuminance. */
    LOW(100),
    /** The goal associated with medium illuminance. */
    MEDIUM(250),
    /** The goal associated with fsull illuminance. */
    HIGH(1000);

    /** The number of lights to turn on. */
    private double power;

    /**
     * Gets the number of lights to turn On.
     *
     * @return the number of lights to turn On.
     */
    public double getMaximumAllowedEnergyInRoom() {
        return power;
    }

    /**
     * Instantiates a new power goal.
     *
     * @param maximumEnergy
     * 
     */
    private IlluminancePower(double maximumEnergy) {
        this.power = maximumEnergy;
    }

}
