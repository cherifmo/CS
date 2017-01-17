package org.example.follow.me.manager.command;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.follow.me.api.FollowMeAdministration;
import org.example.follow.me.api.IlluminanceGoal;
import org.example.follow.me.api.IlluminancePower;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;

//Define this class as an implementation of a component :
@Component
// Create an instance of the component
@Instantiate(name = "follow.me.command")
// Use the handler command and declare the command as a command provider. The
// namespace is used to prevent name collision.
@CommandProvider(namespace = "followme")
public class FollowMeManagerCommandImpl {

    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private FollowMeAdministration m_administrationService;

    /**
     * Felix shell command implementation to sets the illuminance preference.
     *
     * @param goal
     *            the new illuminance preference ("SOFT", "MEDIUM", "FULL")
     */

    @Command
    public void setIlluminancePowerPreference(String goal) {
        // The targeted goal
        IlluminancePower illuminancePower;

        switch (goal) {
        case "LOW":
            illuminancePower = IlluminancePower.LOW;
            break;

        case "MEDIUM":
            illuminancePower = IlluminancePower.MEDIUM;
            break;

        case "HIGH":
            illuminancePower = IlluminancePower.HIGH;
            break;

        default:
            System.out.println("Please be reasonable...");
            illuminancePower = IlluminancePower.LOW;
            break;
        }

        // call the administration service to configure it :
        m_administrationService.setMaximumAllowedEnergyInRoom(illuminancePower);
    }

    @Command
    public void getIlluminancePowerPreference() {
        final String pref = m_administrationService.getMaximumAllowedEnergyInRoom().toString();
        System.out.println("The illuminance Power is " + pref);
    }

    // Each command should start with a @Command annotation
    @Command
    public void setIlluminancePreference(String goal) {
        // The targeted goal
        IlluminanceGoal illuminanceGoal;

        switch (goal) {
        case "SOFT":
            illuminanceGoal = IlluminanceGoal.SOFT;
            break;

        case "MEDIUM":
            illuminanceGoal = IlluminanceGoal.MEDIUM;
            break;

        case "FULL":
            illuminanceGoal = IlluminanceGoal.FULL;
            break;

        default:
            System.out.println("Please be reasonable...");
            illuminanceGoal = IlluminanceGoal.SOFT;
            break;
        }

        // call the administration service to configure it :
        m_administrationService.setIlluminancePreference(illuminanceGoal);
    }

    @Command
    public void getIlluminancePreference() {
        final String pref = m_administrationService.getIlluminancePreference().toString();
        System.out.println("The illuminance goal is " + pref);
    }

}