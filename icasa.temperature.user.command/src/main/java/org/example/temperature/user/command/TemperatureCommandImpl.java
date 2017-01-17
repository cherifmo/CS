package org.example.temperature.user.command;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.temperature.api.TemperatureManager;

import fr.liglab.adele.icasa.command.handler.Command;
import fr.liglab.adele.icasa.command.handler.CommandProvider;

//Define this class as an implementation of a component :
@Component
// Create an instance of the component
@Instantiate(name = "temperature.administration.command")
// Use the handler command and declare the command as a command provider. The
// namespace is used to prevent name collision.
@CommandProvider(namespace = "temperature")
public class TemperatureCommandImpl {

    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private TemperatureManager temperatureManager;

    /**
     * Felix shell command implementation to sets the illuminance preference.
     *
     * @param goal
     *            the new illuminance preference ("SOFT", "MEDIUM", "FULL")
     */

    @Command
    public void tempTooHigh(String room) {
        temperatureManager.temperatureIsTooHigh(room);
    }

    @Command
    public void tempTooLow(String room) {
        temperatureManager.temperatureIsTooLow(room);
    }

}