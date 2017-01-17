package org.icasa.temperature.manager;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.example.temperature.api.TemperatureConfiguration;
import org.example.temperature.api.TemperatureManager;

//Define this class as an implementation of a component :
@Component
// Create an instance of the component
@Instantiate(name = "temperature.manager")
@Provides(specifications = { TemperatureManager.class })
public class TemperatureManagerImpl implements TemperatureManager {

    // Declare a dependency to a FollowMeAdministration service
    @Requires
    private TemperatureConfiguration temperatureConfiguration;

    @Override
    public void temperatureIsTooHigh(String roomName) {
        temperatureConfiguration.setTargetedTemperature(roomName,
                temperatureConfiguration.getTargetedTemperature(roomName) - 1);

    }

    @Override
    public void temperatureIsTooLow(String roomName) {
        temperatureConfiguration.setTargetedTemperature(roomName,
                temperatureConfiguration.getTargetedTemperature(roomName) + 1);

    }

}
