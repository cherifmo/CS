package org.example.follow.me.manager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.FollowMeConfiguration;
import org.example.follow.me.api.IlluminanceGoal;
import org.example.follow.me.api.IlluminancePower;

//Define this class as an implementation of a component :
@Component
// Create an instance of the component
@Instantiate(name = "follow.me.manager")
@Provides(specifications = { FollowMeAdministration.class })
public class LightFollowMeManagerImpl implements FollowMeAdministration {

    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private FollowMeConfiguration followMeConfiguration;

    // IlluminanceGoal illuminanceGoal;

    /*
     * public LightFollowMeManagerImpl(IlluminanceGoal illuminanceGoal) {
     * this.illuminanceGoal = illuminanceGoal; }
     */

    // @Override
    @Override
    public void setIlluminancePreference(IlluminanceGoal illuminanceGoal) {
        // this.illuminanceGoal = illuminanceGoal;

        int illuminance = 1;

        switch (illuminanceGoal) {
        case SOFT:
            illuminance = 1;
            break;

        case MEDIUM:
            illuminance = 2;
            break;

        case FULL:
            illuminance = 3;
            break;

        default:
            illuminance = 1;
            break;
        }

        followMeConfiguration.setMaximumNumberOfLightsToTurnOn(illuminance);

    }

    // @Override
    @Override
    public IlluminanceGoal getIlluminancePreference() {

        final int illuminance = followMeConfiguration.getMaximumNumberOfLightsToTurnOn();

        switch (illuminance) {
        case 1:
            return IlluminanceGoal.SOFT;
        case 2:
            return IlluminanceGoal.MEDIUM;
        case 3:
            return IlluminanceGoal.FULL;
        default:
            return IlluminanceGoal.SOFT;
        }

    }

    @Override
    public IlluminancePower getMaximumAllowedEnergyInRoom() {
        final double power = followMeConfiguration.getMaximumAllowedEnergyInRoom();

        switch ((int) power) {
        case 100:
            return IlluminancePower.LOW;
        case 200:
            return IlluminancePower.MEDIUM;
        case 1000:
            return IlluminancePower.HIGH;
        default:
            return IlluminancePower.LOW;
        }

    }

    @Override
    public void setMaximumAllowedEnergyInRoom(IlluminancePower maximumEnergy) {

        double power = 100;

        switch (maximumEnergy) {
        case LOW:
            power = 100;
            break;

        case MEDIUM:
            power = 200;
            break;

        case HIGH:
            power = 1000;
            break;

        default:
            power = 100;
            break;
        }

        followMeConfiguration.setMaximumAllowedEnergyInRoom(power);

    }

    /**
     * Bind Method for SmartHouseConfiguration dependency
     * 
     * @Bind public void bindFollowMeConfiguration(FollowMeConfiguration
     *       followMeConfiguration, Map properties) { // TODO: Add your
     *       implementation code here }
     * 
     *       /** Unbind Method for SmartHouseConfiguration dependency
     * @Unbind public void unbindFollowMeConfiguration(FollowMeConfiguration
     *         followMeConfiguration, Map properties) { // TODO: Add your
     *         implementation code here }
     */

}
