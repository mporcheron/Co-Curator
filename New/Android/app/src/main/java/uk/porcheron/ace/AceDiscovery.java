package uk.porcheron.ace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for handling the discovery of nearby users.
 */
public final class AceDiscovery {

    /** Logging */
    public static final String TAG = AceFramework.TAG + ":Discovery";

    /** List of listeners that respond to new devices been discovered. */
    private static final List<DevicesDiscoveredListener> mDeviceDiscoveredListeners = new ArrayList<>(1);

    /**
     * Class that contains information relating to a device that has been discovered.
     */
    public final static class Device {

        /** All data for the device. */
        private final Map<DeviceInfo,String> mData = new HashMap<>();

        /** Private constructor. */
        private Device() {}

        /** Information relating to the device information type. */
        public enum DeviceInfo {

            IDENTIFIER, IP_ADDRESS, PORT

        }

        /**
         * Retrieve an item of information for the device.
         *
         * @param key Identifier of the information type.
         * @return {@code true} if the device has broadcast this piece of information.
         */
        public boolean containsKey(DeviceInfo key) {
            return mData.containsKey(key);
        }

        /**
         * Retrieve an item of information for the device.
         *
         * @param key Identifier of the information type.
         * @return The value for the piece of information, or {@code null} if it doesn't exist.
         */
        public String get(DeviceInfo key) {
            return mData.get(key);
        }

        /**
         * Set a piece of information for the device.
         *
         * @param key Identifier of the information type.
         * @param value Value of the information type.
         */
        private void put(DeviceInfo key, String value) {
            mData.put(key, value);
        }

    }

    /** Listener that is called when the list of discovered devices is updated. */
    public interface DevicesDiscoveredListener {

        /**
         * Updated list of discovered devices.
         *
         * @param devices List of all devices.
         */
        void onDiscoveryComplete(List<Device> devices);

    }

    /**
     * Add a new {@link AceDiscovery.DevicesDiscoveredListener}.
     *
     * @param listener Listener that will be called when the list of discovered devices is updated.
     */
    public static final void addDevicesDiscoveredListener(DevicesDiscoveredListener listener) {
        mDeviceDiscoveredListeners.add(listener);
    }

    /**
     * Remove an existing {@link AceDiscovery.DevicesDiscoveredListener}.
     *
     * @param listener Listener that will no longer be called when the list of discovered devices
     *                 is updated.
     */
    public static final void removeDevicesDiscoveredListener(DevicesDiscoveredListener listener) {
        mDeviceDiscoveredListeners.remove(listener);
    }

    

}
