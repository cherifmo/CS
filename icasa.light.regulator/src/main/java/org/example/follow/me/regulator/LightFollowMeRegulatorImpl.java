package org.example.follow.me.regulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.example.follow.me.api.FollowMeConfiguration;

import fr.liglab.adele.icasa.device.DeviceListener;
import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.device.light.BinaryLight;
import fr.liglab.adele.icasa.device.light.DimmerLight;
import fr.liglab.adele.icasa.device.presence.PresenceSensor;

/**

 */
@Component
@Instantiate(name = "light.follow.me.regulator")
@Provides(specifications = { FollowMeConfiguration.class })
@SuppressWarnings("rawtypes")
public class LightFollowMeRegulatorImpl implements DeviceListener, FollowMeConfiguration {

    @Requires(id = "presenceSensors", optional = true)
    /** Field for presenceSensors dependency */
    private PresenceSensor[] presenceSensor;
    @Requires(id = "binaryLights", optional = true)
    /** Field for binaryLights dependency */
    private BinaryLight[] binaryLight;
    @Requires(id = "dimmerLights", optional = true)
    /** Field for dimmerLights dependency */
    private DimmerLight[] dimmerLight;

    /**
     * The name of the LOCATION property
     */
    public static final String LOCATION_PROPERTY_NAME = "Location";

    /**
     * The name of the location for unknown value
     */
    public static final String LOCATION_UNKNOWN = "unknown";

    /*
     * ======================== Administration variables
     * ========================
     */

    /**
     * The maximum energy consumption allowed in a room in Watt:
     **/
    private double maximumEnergyConsumptionAllowedInARoom = 100.0d;
    // The maximum number of lights to turn on when a user enters the room :
    private int maxLightsToTurnOnPerRoom = 1;

    /**
     * This method is part of the DeviceListener interface and is called when a
     * subscribed device property is modified.
     * 
     * @param device
     *            is the device whose property has been modified.
     * @param propertyName
     *            is the name of the modified property.
     * @param oldValue
     *            is the old value of the property
     */
    @Override
    public void devicePropertyModified(GenericDevice device, String propertyName, Object oldValue, Object arg3) {

        System.out.println("Device changed : " + device.toString());
        // If the device is a presence sensor
        if (device instanceof PresenceSensor) {
            presenceSensorChanged((PresenceSensor) device, propertyName, oldValue);
        } else if (device instanceof BinaryLight) {
            binaryLightChanged((BinaryLight) device, oldValue.toString());
        } else if (device instanceof DimmerLight) {
            dimmerLightChanged((DimmerLight) device, oldValue.toString());
        }

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

    @Override
    public void deviceAdded(GenericDevice arg0) {
        // TODO Auto-generated method stub
    }

    /*
     * ==================== Handlers for devices ====================
     */

    /*
     * Function called if presence sensor has changed
     */
    public void presenceSensorChanged(PresenceSensor changingSensor, String propertyName, Object oldValue) {
        // Get location of sensor
        final String detectorLocation = (String) changingSensor.getPropertyValue("Location");
        // Check if sensor changed room
        if (propertyName.equals("presenceSensor.sensedPresence")) {
            System.out.println("Presence sensor id:" + changingSensor.getSerialNumber() + " has changed state.");
            if (((Boolean) oldValue).booleanValue()) {
                System.out.println("\t -> [ ] No more presence");
            } else {
                System.out.println("\t -> [X] Sensed presence");
            }
            // Modify state of all devices in room
            if (!detectorLocation.equals("unknown")) {
                updateAllDevicesOfRoom(detectorLocation);
            }
        } else {
            System.out.println("Presence sensor id:" + changingSensor.getSerialNumber()
                    + " has changed room.\n\t -> Now in " + (String) oldValue);
            // Search if sensor was last one of its old room
            final List<PresenceSensor> sameLocationSensors = this.getPresenceSensorFromLocation((String) oldValue);
            if (sameLocationSensors.isEmpty()) { // If so, shut down all lights
                // from this old room
                updateAllDevicesOfRoom((String) oldValue);
            }
        }
    }

    /**
     * Return all BinaryLight from the given location
     * 
     * @param location
     *            : the given location
     * @return the list of matching BinaryLights
     */
    private synchronized List<PresenceSensor> getPresenceSensorFromLocation(String location) {
        final List<PresenceSensor> presenceSensorLocation = new ArrayList<PresenceSensor>();
        for (final PresenceSensor presensor : presenceSensor) {
            if (presensor.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                presenceSensorLocation.add(presensor);
            }
        }
        return presenceSensorLocation;
    }

    /*
     * Function called if binary light has changed
     */
    public void binaryLightChanged(BinaryLight changingLight, String oldValue) {
        // get the location of the changing sensor
        final String lightLocation = (String) changingLight.getPropertyValue(LOCATION_PROPERTY_NAME);
        System.out.println(
                "Light id:" + changingLight.getSerialNumber() + " has changed room.\n\t -> Now in " + lightLocation);
        // Update lights of old location
        updateAllDevicesOfRoom(oldValue);
        updateAllDevicesOfRoom(lightLocation);
    }

    /**
     * Return all BinaryLight from the given location
     * 
     * @param location//TODO
     *            : implement the command that print the current value of the
     *            goal : the given location
     * @return the list of matching BinaryLights
     */
    private synchronized List<BinaryLight> getBinaryLightFromLocation(String location) {
        final List<BinaryLight> binaryLightsLocation = new ArrayList<BinaryLight>();
        for (final BinaryLight binLight : binaryLight) {
            if (binLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                binaryLightsLocation.add(binLight);
            }
        }
        return binaryLightsLocation;
    }

    private void dimmerLightChanged(DimmerLight changingLight, String oldValue) {
        // get the location of the changing sensor
        final String lightLocation = (String) changingLight.getPropertyValue(LOCATION_PROPERTY_NAME);
        System.out.println("Dimmerlight id:" + changingLight.getSerialNumber() + " has changed room.\n\t -> Now in "
                + lightLocation);
        // Update lights of old location
        updateAllDevicesOfRoom(oldValue);
        updateAllDevicesOfRoom(lightLocation);

    }

    /**
     * Return all DimmerLight from the given location
     * 
     * @param location
     *            : the given location
     * @return the list of matching DimmerLight
     */
    private synchronized List<DimmerLight> getDimmerLightFromLocation(String location) {
        final List<DimmerLight> dimmerLightsLocation = new ArrayList<DimmerLight>();
        for (final DimmerLight dimLight : dimmerLight) {
            if (dimLight.getPropertyValue(LOCATION_PROPERTY_NAME).equals(location)) {
                dimmerLightsLocation.add(dimLight);
            }
        }
        return dimmerLightsLocation;
    }

    /*
     * ============================================= Functions to turn on and
     * off all room devices =============================================
     */

    public void updateAllDevicesOfRoom(String room) {
        Boolean state;
        // Get list of sensors from same room
        final List<PresenceSensor> sameLocationSensors = getPresenceSensorFromLocation(room);
        if (sameLocationSensors.isEmpty()) {
            // If list is empty, shut down lightjava.lang.Boolean cannot be cast
            // to java.lang.String
            state = false;
        } else if (sameLocationSensors.get(0).getSensedPresence()) {
            state = true;
        } else {
            state = false;
        }

        // If lights needs to be on
        if (state) {
            /*
             * ======= OLD ========== // Check if limit number of powered lights
             * is reached List<BinaryLight> sameLocationLights =
             * this.getBinaryLightFromLocation(room); int i = 0; //For each
             * light of room for (BinaryLight binaryLight : sameLocationLights)
             * { //If light is powered, count if (binaryLight.getPowerStatus())
             * i++; //If max light powered if (i >= maxLightsToTurnOnPerRoom)
             * break; // Quick exit } // If limit not reached, power up another
             * one if (i < maxLightsToTurnOnPerRoom) { i = 0; //For each light
             * of room for (BinaryLight binaryLight : sameLocationLights) { //
             * If light is off if (!binaryLight.getPowerStatus()) { //Power it
             * on binaryLight.setPowerStatus(true); i++; //Count } //If limit is
             * reached, quick exit if (state && i >= maxLightsToTurnOnPerRoom)
             * break; } }
             * 
             * Double powerLevel = 0.0; if (i == 0) powerLevel = 1.0; else
             * powerLevel = 0.5; //Power on all dimmer lights List<DimmerLight>
             * sameLocationDimmerLight = this.getDimmerLightFromLocation(room);
             * for (DimmerLight dimmerLight : sameLocationDimmerLight) {
             * dimmerLight.setPowerLevel(powerLevel); }
             */

            // Check if limit number of powered lights is reached
            final List<BinaryLight> sameLocationLights = this.getBinaryLightFromLocation(room);
            int i = (int) maximumEnergyConsumptionAllowedInARoom;
            // If limit not reached, power up another one
            if (i >= 100) {
                // For each light of room
                for (final BinaryLight binaryLight : sameLocationLights) {
                    // Power light (on or off)
                    binaryLight.setPowerStatus(state);
                    i -= 100; // Count
                    // If limit is reached, quick exit
                    if (i < 100 || (maximumEnergyConsumptionAllowedInARoom - i) / 100 >= maxLightsToTurnOnPerRoom) {
                        state = false;
                    }
                }
                // If need to power more dimmer lights
                if (state && i != 0) {
                    final List<DimmerLight> sameLocationDimmerLight = this.getDimmerLightFromLocation(room);

                    for (final DimmerLight dimmerLight : sameLocationDimmerLight) {
                        if (i >= 100) {
                            dimmerLight.setPowerLevel(1.0);
                            i -= 100;
                        } else {
                            dimmerLight.setPowerLevel(i / 100);
                            i = 0;
                        }
                        if ((maximumEnergyConsumptionAllowedInARoom - i) / 100 >= maxLightsToTurnOnPerRoom) {
                            i = 0;
                        }
                    }
                }

            }

            /*
             * Double powerLevel = 0.0; if (i == 0) powerLevel = 1.0; else
             * powerLevel = 0.5; //Power on all dimmer lights List<DimmerLight>
             * sameLocationDimmerLight = this.getDimmerLightFromLocation(room);
             * for (DimmerLight dimmerLight : sameLocationDimmerLight) {
             * dimmerLight.setPowerLevel(powerLevel); }
             */

        } else {
            // If lights needs to be off
            final List<BinaryLight> sameLocationLights = this.getBinaryLightFromLocation(room);
            for (final BinaryLight binaryLight : sameLocationLights) {
                binaryLight.setPowerStatus(false);
            }
            final List<DimmerLight> sameLocationDimmerLight = this.getDimmerLightFromLocation(room);
            for (final DimmerLight dimmerLight : sameLocationDimmerLight) {
                dimmerLight.setPowerLevel(0.0);
            }
        }
    }

    /*
     * ========================================= Basic functions for binding and
     * unbinding =========================================
     */

    /** Bind Method for binaryLight dependency */
    @Bind(id = "dimmerLights")
    public void bindDimmerLight(DimmerLight dimmerLight, Map properties) {
        System.out.println("bind dimmer light " + dimmerLight.getSerialNumber());
        dimmerLight.addListener(this);
    }

    /** Unbind Method for binaryLight dependency */
    @Unbind(id = "dimmerLights")
    public void unbindDimmerLight(DimmerLight dimmerLight, Map properties) {
        System.out.println("unbind dimmer light " + dimmerLight.getSerialNumber());
        dimmerLight.removeListener(this);
    }

    /** Bind Method for binaryLight dependency */
    @Bind(id = "binaryLights")
    public void bindBinaryLight(BinaryLight binaryLight, Map properties) {
        System.out.println("bind binary light " + binaryLight.getSerialNumber());
        binaryLight.addListener(this);
    }

    /** Unbind Method for binaryLight dependency */
    @Unbind(id = "binaryLights")
    public void unbindBinaryLight(BinaryLight binaryLight, Map properties) {
        System.out.println("unbind binary light " + binaryLight.getSerialNumber());
        binaryLight.removeListener(this);
    }

    /** Bind Method for presenceSensor dependency */
    @Bind(id = "presenceSensors")
    public synchronized void bindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
        System.out.println("bind presence sensor" + presenceSensor.getSerialNumber());
        presenceSensor.addListener(this);
    }

    /** Unbind Method for presenceSensor dependency */
    @Unbind(id = "presenceSensors")
    public synchronized void unbindPresenceSensor(PresenceSensor presenceSensor, Map properties) {
        System.out.println("unbind presence sensor" + presenceSensor.getSerialNumber());
        presenceSensor.removeListener(this);
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
    }

    @Override
    public int getMaximumNumberOfLightsToTurnOn() {
        return maxLightsToTurnOnPerRoom;
    }

    @Override
    public void setMaximumNumberOfLightsToTurnOn(int maxLightsToTurnOnPerRoom) {
        this.maxLightsToTurnOnPerRoom = maxLightsToTurnOnPerRoom;
    }

    @Override
    public double getMaximumAllowedEnergyInRoom() {
        return maximumEnergyConsumptionAllowedInARoom;

    }

    @Override
    public void setMaximumAllowedEnergyInRoom(double maximumEnergy) {
        this.maximumEnergyConsumptionAllowedInARoom = maximumEnergy;

    }

}
