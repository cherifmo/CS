package org.example.temperature.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.example.temperature.api.TemperatureConfiguration;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.temperature.Cooler;
import fr.liglab.adele.icasa.device.temperature.Heater;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;

@Component
@Instantiate(name = "temperature.controller")
@Provides(specifications = { PeriodicRunnable.class, TemperatureConfiguration.class })
@SuppressWarnings("rawtypes")
public class TemperatureControllerImpl
        implements DeviceListener<GenericDevice>, PeriodicRunnable, TemperatureConfiguration {

    /** Field for cooler dependency */
    @Requires(id = "cooler", optional = true)
    private Cooler[] coolers;
    /** Field for heater dependency */
    @Requires(id = "heater", optional = true)
    private Heater[] heaters;
    /** Field for thermometer dependency */
    @Requires(id = "thermometer", optional = true)
    private Thermometer[] thermometers;

    /**
     * To associate rooms and temp
     */
    private Map<String, Double> roomsTemperatures;
    private Map<String, Double> targetRoomsTemperatures;

    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";

    @Override
    public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object arg3) {

        if (device instanceof Thermometer) {
            thermometerChanged((Thermometer) device, propertyName, oldValue);
        } else if (device instanceof Cooler) {
            coolerChanged((Cooler) device, oldValue.toString());
        } else if (device instanceof Heater) {
            heaterChanged((Heater) device, oldValue.toString());
        }

    }

    /**
     * Return all Thermo from the given location
     *
     * @param location
     *            : the given location
     * @return the list of matching thermo
     */
    private synchronized List<Cooler> getCoolersFromLocation(String location) {
        final List<Cooler> coolerFromLocation = new ArrayList<Cooler>();
        for (final Cooler cooler : coolers) {
            if (cooler.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                coolerFromLocation.add(cooler);
            }
        }
        return coolerFromLocation;
    }

    /**
     * Return all Thermo from the given location
     *
     * @param location
     *            : the given location
     * @return the list of matching thermo
     */
    private synchronized List<Heater> getHeatersFromLocation(String location) {
        final List<Heater> heaterFromLocation = new ArrayList<Heater>();
        for (final Heater heater : heaters) {
            if (heater.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                heaterFromLocation.add(heater);
            }
        }
        return heaterFromLocation;
    }

    // Method to update all rooms temperature
    private void updateRoomsTemperature() {
        for (final Thermometer thermo : thermometers) {
            if (!roomsTemperatures.containsKey(thermo.getPropertyValue(LOCATION_PROPERTY_NAME))) {
                roomsTemperatures.put((String) thermo.getPropertyValue(LOCATION_PROPERTY_NAME),
                        thermo.getTemperature());
            } else {
                roomsTemperatures.replace((String) thermo.getPropertyValue(LOCATION_PROPERTY_NAME),
                        thermo.getTemperature());
            }

        }

        String room = "";
        Double temp = (double) 0;
        double power = 0;
        List<Heater> heatersFromLocation;
        List<Cooler> coolersFromLocation;
        
        for (final Map.Entry<String, Double> roomRoom : roomsTemperatures.entrySet()) {
            System.out.println(roomRoom.getKey());
            // If temp < target, this < 0, power heaters
            // else, this > 0, power coolers
            // String room =
            // (String)thermo.getPropertyValue(LOCATION_PROPERTY_NAME);
            room = roomRoom.getKey();
            temp = roomRoom.getValue();
            power = temp - targetRoomsTemperatures.get(room);
            System.out.println("Différence de temp pour "+room+" est "+power);
            if (Math.abs(power) < (double) 1) {
                System.out.println("Inférieur à 1 !");
                heatersFromLocation = getHeatersFromLocation(room);
                // if(!heatersFromLocation.isEmpty())
                heatersFromLocation.get(0).setPowerLevel(0.001);
                coolersFromLocation = getCoolersFromLocation(room);
                // if(!coolersFromLocation.isEmpty())
                coolersFromLocation.get(0).setPowerLevel(0.001);
            } else if (power > 0) {
                System.out.println("Positif !");
                coolersFromLocation = getCoolersFromLocation(room);
                // System.out.println(coolersFromLocation);
                // if(!coolersFromLocation.isEmpty())
                coolersFromLocation.get(0).setPowerLevel(0.005);
                // Shutdown all heaters
                heatersFromLocation = getHeatersFromLocation(room);
                // if(!heatersFromLocation.isEmpty())
                heatersFromLocation.get(0).setPowerLevel(0.0);
            } else if (power < 0) {
                System.out.println("Négatif !");
                heatersFromLocation = getHeatersFromLocation(room);
                // System.out.println(heatersFromLocation);
                // if(!heatersFromLocation.isEmpty())
                heatersFromLocation.get(0).setPowerLevel(0.005);
                // shutdown all coolers
                coolersFromLocation = getCoolersFromLocation(room);
                // if(!coolersFromLocation.isEmpty())
                coolersFromLocation.get(0).setPowerLevel(0.0);
            }
        }

    }

    @Override
    public void run() {
        System.out.println("Tic !");
        updateRoomsTemperature();
        System.out.println(roomsTemperatures.toString());

    }

    @Override
    public long getPeriod() {
        // TODO Auto-generated method stub
        return 2000;
    }

    @Override
    public TimeUnit getUnit() {
        // TODO Auto-generated method stub
        return TimeUnit.SECONDS;
    }

    private void heaterChanged(Heater device, String string) {
        // get the location of the changing device
        /*
         * String deviceLocation = (String)
         * device.getPropertyValue(LOCATION_PROPERTY_NAME);
         * System.out.println("Heater id:" + device.getSerialNumber() +
         * " has changed room.\n\t -> Now in " + deviceLocation);
         */

    }

    private void coolerChanged(Cooler device, String string) {
        // get the location of the changing device
        /*
         * String deviceLocation = (String)
         * device.getPropertyValue(LOCATION_PROPERTY_NAME);
         * System.out.println("Cooler id:" + device.getSerialNumber() +
         * " has changed room.\n\t -> Now in " + deviceLocation);
         */

    }

    private void thermometerChanged(Thermometer device, String propertyName, Object oldValue) {
        // get the location of the changing device
        // String deviceLocation = (String)
        // device.getPropertyValue(LOCATION_PROPERTY_NAME);
        // System.out.println("Thermometer id:" + device.getSerialNumber() + "
        // has changed room.\n\t -> Now in " + deviceLocation);

    }

    /** Bind Method for heater dependency */
    @Bind(id = "heater")
    public void bindHeater(Heater heater, Map properties) {
        System.out.println("bind heater " + heater.getSerialNumber());
        heater.addListener(this);
    }

    /** Unbind Method for heater dependency */
    @Unbind(id = "heater")
    public void unbindHeater(Heater heater, Map properties) {
        System.out.println("unbind heater " + heater.getSerialNumber());
        heater.removeListener(this);
    }

    /** Bind Method for thermometer dependency */
    @Bind(id = "thermometer")
    public void bindThermometer(Thermometer thermometer, Map properties) {
        System.out.println("bind thermometer " + thermometer.getSerialNumber());
        thermometer.addListener(this);
    }

    /** Unbind Method for thermometer dependency */
    @Unbind(id = "thermometer")
    public void unbindThermometer(Thermometer thermometer, Map properties) {
        System.out.println("unbind thermometer " + thermometer.getSerialNumber());
        thermometer.removeListener(this);
    }

    /** Bind Method for cooler dependency */
    @Bind(id = "cooler")
    public void bindCooler(Cooler cooler, Map properties) {
        System.out.println("bind cooler " + cooler.getSerialNumber());
        cooler.addListener(this);
    }

    /** Unbind Method for cooler dependency */
    @Unbind(id = "cooler")
    public void unbindCooler(Cooler cooler, Map properties) {
        System.out.println("unbind cooler " + cooler.getSerialNumber());
        cooler.removeListener(this);
    }

    @Override
    public void deviceAdded(GenericDevice arg0) {
      // TODO Auto-generated method stub

    }

    @Override
    public void deviceEvent(GenericDevice arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void devicePropertyAdded(GenericDevice arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void devicePropertyRemoved(GenericDevice arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deviceRemoved(GenericDevice arg0) {
        // TODO Auto-generated method stub

    }

    /** Component Lifecycle Method */
    @Invalidate
    public void stop() {
        System.out.println("Component is stopping...");
    }

    /** Component Lifecycle Method */
    @Validate
    public void start() {
        System.out.println("Component is starting...");
        this.roomsTemperatures = new HashMap<>();
        this.targetRoomsTemperatures = new HashMap<>();
        targetRoomsTemperatures.put("livingroom", 291.15);
        targetRoomsTemperatures.put("kitchen", 288.15);
        targetRoomsTemperatures.put("bathroom", 296.15);
        targetRoomsTemperatures.put("bedroom", 293.15);

    }

    @Override
    public void setTargetedTemperature(String targetedRoom, double temperature) {
        targetRoomsTemperatures.replace(targetedRoom, temperature);
    }

    @Override
    public double getTargetedTemperature(String room) {
        return targetRoomsTemperatures.get(room);
    }

}
