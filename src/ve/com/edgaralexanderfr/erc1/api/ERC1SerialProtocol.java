package ve.com.edgaralexanderfr.erc1.api;

import ve.com.edgaralexanderfr.erc.BluetoothSerial;
import ve.com.edgaralexanderfr.erc.ERCSerialProtocol;

public class ERC1SerialProtocol extends ERCSerialProtocol {
	public static final byte OUT_TOGGLE_LEFT_BLINKER        = 15;
	public static final byte OUT_TOGGLE_HEADLIGHTS          = 16;
	public static final byte OUT_START_HORN                 = 17;
	public static final byte OUT_STOP_HORN                  = 18;
	public static final byte OUT_TOGGLE_SIREN               = 19;
	public static final byte OUT_TOGGLE_EMERGENCY_LIGHTS    = 20;
	public static final byte OUT_TOGGLE_RIGHT_BLINKER       = 21;
	public static final byte OUT_SET_AUTOMATIC_HEADLIGHTS   = 22;
	public static final byte OUT_UNSET_AUTOMATIC_HEADLIGHTS = 23;
	
	private static ERC1SerialProtocol erc1SerialProtocol    = null;
	
	/**
	 * Constructs a new ERC1SerialProtocol object.
	 * 
	 * @param {BluetoothSerial} bluetoothSerial BluetoothSerial object to use.
	 */
	public ERC1SerialProtocol (BluetoothSerial bluetoothSerial) {
		super(bluetoothSerial);
	}
	
	/**
	 * Returns a global instance from ERC1SerialProtocol.
	 *
	 * @param  {BluetoothSerial} bluetoothSerial BluetoothSerial object to use.
	 * @return {ERC1SerialProtocol}.
	 */
	public static ERC1SerialProtocol get (BluetoothSerial bluetoothSerial) {
		return (erc1SerialProtocol == null) ? (erc1SerialProtocol = new ERC1SerialProtocol(bluetoothSerial)) : erc1SerialProtocol ;
	}
	
	/**
	 * Invoked when a full packet content is dispatched.
	 * 
	 * @param {byte[]} content Content of the full packet in bytes.
	 */
	@Override
	public void onPacketReceived (byte[] content) {
		super.onPacketReceived(content);
	}
	
	/**
	 * Turns on/off the ERC's left blinker.
	 */
	public void toggleLeftBlinker () {
		this.send(OUT_TOGGLE_LEFT_BLINKER, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Turns on/off the ERC's headlights.
	 */
	public void toggleHeadlights () {
		this.send(OUT_TOGGLE_HEADLIGHTS, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Turns on ERC's horn sound.
	 */
	public void startHorn () {
		this.send(OUT_START_HORN, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Turns off ERC's horn sound.
	 */
	public void stopHorn () {
		this.send(OUT_STOP_HORN, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Toggles ERC's siren sound.
	 */
	public void toggleSiren () {
		this.send(OUT_TOGGLE_SIREN, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Turns on/off the emergency lights.
	 */
	public void toggleEmergencyLights () {
		this.send(OUT_TOGGLE_EMERGENCY_LIGHTS, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Turns on/off the ERC's right blinker.
	 */
	public void toggleRightBlinker () {
		this.send(OUT_TOGGLE_RIGHT_BLINKER, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Configures the ERC to considerate turning on/off the headlights automatically.
	 */
	public void setAutomaticHeadlights () {
		this.send(OUT_SET_AUTOMATIC_HEADLIGHTS, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
	
	/**
	 * Configures the ERC to ignore turning on/off the headlights automatically.
	 */
	public void unsetAutomaticHeadlights () {
		this.send(OUT_UNSET_AUTOMATIC_HEADLIGHTS, NO_VALUE, NO_VALUE, NO_VALUE, NO_VALUE);
	}
}
