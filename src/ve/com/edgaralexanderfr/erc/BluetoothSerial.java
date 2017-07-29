package ve.com.edgaralexanderfr.erc;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

public class BluetoothSerial implements Runnable {
	public static final String SPP_UUID              = "00001101-0000-1000-8000-00805f9b34fb";
	public static final char PACKET_DELIMITER        = '|';
	
	private static BluetoothSerial bluetoothSerial   = null;
	private static BluetoothAdapter bluetoothAdapter = null;
	
	private SerialProtocol protocol                  = null;
	private boolean runningConnectionThread          = false;
	private BluetoothDevice targetDevice             = null;
	private int bufferReadSize                       = 255;
	private boolean disconnecting                    = false;
	private Thread thread                            = null;
	private BluetoothSocket bluetoothSocket          = null;
	private InputStream inputStream                  = null;
	private OutputStream outputStream                = null;
	
	/**
	 * Tells whether the device's bluetooth is enabled or not.
	 * 
	 * @return {boolean}.
	 */
	public static boolean isBluetoothEnabled () {
		return (bluetoothAdapter != null) ? bluetoothAdapter.isEnabled() : false ;
	}
	
	/**
	 * Initializes the bluetooth adapter.
	 */
	static {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	/**
	 * Returns the global/static object creating it with the first call and keeping the 
	 * instance.
	 * 
	 * @return {BluetoothSerial}.
	 */
	public static BluetoothSerial get () {
		return (bluetoothSerial != null) ? bluetoothSerial : (bluetoothSerial = new BluetoothSerial()) ;
	}
	
	/**
	 * Returns a list of all paired devices.
	 * 
	 * @return {BluetoothDevice[]}.
	 */
	public static BluetoothDevice[] getPairedDevices () {
		if (bluetoothAdapter == null) {
			return new BluetoothDevice[0];
		}
		
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		
		return pairedDevices.toArray(new BluetoothDevice[ pairedDevices.size() ]);
	}
	
	/**
	 * Returns the names of the provided devices in a String array.
	 * 
	 * @param  {BluetoothDevice[]} devices List of devices.
	 * @return {String[]}.
	 */
	public static String[] getDevicesNames (BluetoothDevice[] devices) {
		final int total       = devices.length;
		String[] devicesNames = new String[ total ];
		int i;
		
		for (i = 0; i < total; i++) {
			devicesNames[ i ] = devices[ i ].getName();
		}
		
		return devicesNames;
	}
	
	/**
	 * Creates a new bluetooth socket.
	 * 
	 * @param  {String}          uuid   Device UUID.
	 * @param  {BluetoothDevice} device Bluetooth target device.
	 * @return {BluetoothSocket}.
	 */
	public static BluetoothSocket createSocket (final String uuid, BluetoothDevice device) {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				Method method = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
				
				return (BluetoothSocket) method.invoke(device, uuid);
			} catch (Exception ex1) {
				
			}
		}
		
		try {
			return device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
		} catch (Exception ex2) {
			return null;
		}
	}
	
	/**
	 * Splits a byte[] in two in case there's a present delimiter that is not preceded by 
	 * a backslash code. In case there's no coincidence, the first index will be an empty 
	 * byte array and the second one the original byte array.
	 * 
	 * @param  {byte[]} array     Byte array to inspect and split.
	 * @param  {byte}   delimiter Byte that shouldn't be escaped within the byte array 
	 *                            and divides both parts.
	 * @return {byte[][]}.
	 */
	public static byte[][] splitUnescapedDelimiter (final byte[] array, final byte delimiter) {
		final int length = array.length;
		byte[][] parts   = new byte[][] {new byte[0], array};
		int i;
		
		for (i = 0; i < length; i++) {
			if (array[ i ] == delimiter && (i == 0 || array[ i - 1 ] != 92)) {
				parts[0] = cutByteArray(array, 0, i);
				parts[1] = cutByteArray(array, i + 1, length);
				
				break;
			}
		}
		
		return parts;
	}
	
	/**
	 * Extracts a specific part from the byte array (like String's substring).
	 * 
	 * @param  {byte[]} array Byte array where to extract the result array.
	 * @param  {int}    start Start index.
	 * @param  {int}    end   End index.
	 * @return {byte[]}.
	 */
	public static byte[] cutByteArray (final byte[] array, final int start, final int end) {
		final int arrayLength = array.length;
		
		if (start < 0 || start >= arrayLength || end <= start || end > arrayLength) {
			return new byte[0];
		}
		
		final int length = end - start;
		byte[] byteArray = new byte[ length ];
		int index        = 0;
		int i;
		
		for (i = start; i < end; i++) {
			byteArray[ index ] = array[ i ];
			index++;
		}
		
		return byteArray;
	}
	
	/**
	 * Escapes the specified byte within the provided byte array preceding it with a  
	 * backslash ASCII code.
	 * 
	 * @param  {byte[]} bytes      Bytes array where to search and escape.
	 * @param  {byte}   escapeByte Byte to escape.
	 * @return {byte[]}.
	 */
	public static byte[] escapeByte (final byte[] bytes, final byte escapeByte) {
		int length           = bytes.length;
		String escapedString = "";
		int i;
		
		for (i = 0; i < length; i++) {
			if (bytes[ i ] == escapeByte) {
				escapedString += "92,";
			}
			
			escapedString += bytes[ i ] + ",";
		}
		
		String[] bytesStrings = escapedString.split(",", -1);
		length                = bytesStrings.length - 1;
		byte[] escapedBytes   = new byte[ length ];
		
		for (i = 0; i < length; i++) {
			escapedBytes[ i ] = Byte.parseByte(bytesStrings[ i ]);
		}
		
		return escapedBytes;
	}
	
	/**
	 * Replaces the backslashes ASCII codes from escaped bytes within the provided byte  
	 * array.
	 * 
	 * @param  {byte[]} bytes       Byte array where to search and unescape.
	 * @param  {byte}   escapedByte Escaped byte to unescape.
	 * @return {byte[]}.
	 */
	public static byte[] unescapeByte (final byte[] bytes, final byte escapedByte) {
		int length       = bytes.length;
		final int last   = length - 1;
		String unescapedString = "";
		int i;
		
		for (i = 0; i < length; i++) {
			if (bytes[ i ] == 92 && i < last && bytes[ i + 1 ] == escapedByte) {
				continue;
			}
			
			unescapedString += bytes[ i ] + ",";
		}
		
		String[] bytesStrings = unescapedString.split(",", -1);
		length                = bytesStrings.length - 1;
		byte[] unescapedBytes = new byte[ length ];
		
		for (i = 0; i < length; i++) {
			unescapedBytes[ i ] = Byte.parseByte(bytesStrings[ i ]);
		}
		
		return unescapedBytes;
	}
	
	/**
	 * Merges two arrays of bytes into a single one.
	 * 
	 * @param  {byte[]} array1 Array one to join.
	 * @param  {byte[]} array2 Array two to join.
	 * @return {byte[]}.
	 */
	public static byte[] mergeBytesArrays (final byte[] array1, final byte[] array2) {
		final int length1 = array1.length;
		final int length2 = array2.length;
		final int length  = length1 + length2;
		byte[] array      = new byte[ length ];
		int index = 0;
		int i;
		
		for (i = 0; i < length1; i++) {
			array[ index ] = array1[ i ];
			index++;
		}
		
		for (i = 0; i < length2; i++) {
			array[ index ] = array2[ i ];
			index++;
		}
		
		return array;
	}
	
	/**
	 * When a byte array ends in a specific index, the rest would be null or empty, 
	 * however, this method allows to create a new array with the full-true length so we 
	 * can know the exact size.
	 * 
	 * @param  {byte[]} array Array to normalize.
	 * @param  {int}    size  Full-true size of the array.
	 * @return {byte[]}.
	 */
	public static byte[] byteArrayRealSize (final byte[] array, final int size) {
		byte[] result = new byte[ size ];
		int i;
		
		for (i = 0; i < size; i++) {
			result[ i ] = array[ i ];
		}
		
		return result;
	}
	
	/**
	 * Tells whether the connection thread is running or not.
	 * 
	 * @return {boolean}.
	 */
	public boolean isRunningConnectionThread () {
		return this.runningConnectionThread;
	}
	
	/**
	 * Returns the target device to connect.
	 * 
	 * @return {BluetoothDevice}.
	 */
	public BluetoothDevice getTargetDevice () {
		return this.targetDevice;
	}
	
	/**
	 * Returns the buffer read size for chunks that are read from the input buffer.
	 * 
	 * @return {int}.
	 */
	public int getBufferReadSize () {
		return this.bufferReadSize;
	}
	
	/**
	 * Tells whether there's a disconnection is progress or not.
	 * 
	 * @return {boolean}.
	 */
	public boolean isDisconnecting () {
		return this.disconnecting;
	}
	
	/**
	 * Tells whether there's a connection output available or not.
	 * 
	 * @return {boolean}.
	 */
	public boolean isOutputAvailable () {
		return this.outputStream != null;
	}
	
	/**
	 * Provides a protocol to handle the connection with the target device.
	 * 
	 * @param {SerialProtocol} protocol Serial protocol to use with the connection.
	 */
	public void setProtocol (SerialProtocol protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * Establishes the buffer read size for chunks that are read from the input buffer.
	 * 
	 * @param {int} bufferReadSize Buffer read size to set.
	 */
	public void setBufferReadSize (int bufferReadSize) {
		this.bufferReadSize = bufferReadSize;
	}
	
	/**
	 * This thread tries to establish the connection with the device and manages all the 
	 * incoming (input) stream.
	 */
	@Override
	public void run () {
		this.establishConnection();
		this.manageIncomingInput();
	}
	
	/**
	 * Tries to a establish a connection with the specified bluetooth device in a new 
	 * thread.
	 * 
	 * @param {BluetoothDevice} targetDevice Target bluetooth device.
	 */
	public void connectTo (BluetoothDevice targetDevice) {
		if (targetDevice == null) {
			return;
		}
		
		this.closeCurrentConnection();
		this.runningConnectionThread = true;
		this.targetDevice            = targetDevice;
		this.thread                  = new Thread(this);
		this.thread.start();
	}
	
	/**
	 * Tries to reestablish the connection with the current target device. 
	 */
	public void resumeConnection () {
		this.connectTo(this.targetDevice);
	}
	
	/**
	 * This method should be invoked when there's a connection failure. It ensures to check
	 * if there's no active disconnection so it can nullify the thread to prevent waiting 
	 * issues and calls the closeCurrentConnection by itself, It also calls and notifies 
	 * the onConnectionFailed event if the protocol object has been set.
	 */
	private void handleConnectionFailure () {
		if (!this.disconnecting) {
			this.thread = null;
			this.closeCurrentConnection();
		}
		
		if (this.protocol != null) {
			this.protocol.onConnectionFailed();
		}
	}
	
	/**
	 * This method should be invoked when there's a disconnection with a target device. 
	 * It ensures to check if there's no active disconnection so it can nullify the 
	 * thread to prevent waiting issues and calls the closeCurrentConnection by itself, 
	 * it also calls and notifies the onDisconnection event if the protocol object has 
	 * been set.
	 */
	private void handleDisconnection () {
		if (!this.disconnecting) {
			this.thread = null;
			this.closeCurrentConnection();
		}
		
		if (this.protocol != null) {
			this.protocol.onDisconnection();
		}
	}
	
	/**
	 * Note: this method runs in the connection thread.
	 * 
	 * It tries to establish the communication with the target device as soon as the 
	 * connection thread is started. It also handles the connection failure/success 
	 * events and aborts the current connection in case of any error.
	 */
	private void establishConnection () {
		this.bluetoothSocket = createSocket(SPP_UUID, this.targetDevice);
		bluetoothAdapter.cancelDiscovery();
		
		try {
			this.bluetoothSocket.connect();
		} catch (Exception ex1) {
			this.handleConnectionFailure();
			
			return;
		}
		
		try {
			this.inputStream  = this.bluetoothSocket.getInputStream();
			this.outputStream = this.bluetoothSocket.getOutputStream();
		} catch (Exception ex2) {
			this.handleConnectionFailure();
			
			return;
		}
		
		if (this.protocol != null) {
			this.protocol.onConnectionSuccess();
		}
	}
	
	/**
	 * Note: this method runs in the connection thread.
	 * 
	 * It controls all the buffer input and dumps the data into a buffer String. It 
	 * ensures to validate and dispatch the incoming packets when the information is 
	 * complete and ready to go and also checks if the connection is still active and 
	 * ensures to disconnect the device correctly when is lost or something goes wrong.
	 * 
	 * This control will be alive as long as runningConnectionThread is set to true.
	 */
	private void manageIncomingInput () {
		int chunkBufferCount = 0;
		byte[] chunkBuffer   = new byte[ this.bufferReadSize ];
		byte[] buffer        = new byte[0];
		byte[][] nextPacket;
		byte[] packetContent;
		
		while (this.runningConnectionThread) {
			try {
				chunkBufferCount = this.inputStream.read(chunkBuffer);
				
				if (chunkBufferCount != -1) {
					buffer = mergeBytesArrays(buffer, byteArrayRealSize(chunkBuffer, chunkBufferCount));
				}
			} catch (Exception ex1) {
				this.handleDisconnection();
				
				break;
			}
			
			if (buffer.length > 0) {
				nextPacket    = splitUnescapedDelimiter(buffer, (byte) PACKET_DELIMITER);
				packetContent = nextPacket[0];
				buffer        = nextPacket[1];
				
				if (packetContent.length > 0 && this.protocol != null) {
					this.protocol.onPacketReceived(unescapeByte(packetContent, (byte) PACKET_DELIMITER));
				}
			}
		}
	}
	
	/**
	 * Send a new full packet to the target device.
	 * 
	 * @param {byte[]} content Content of the full packet in bytes.
	 */
	public void sendPacket (byte[] bytes) {
		if (this.isOutputAvailable()) {
			try {
				this.outputStream.write(mergeBytesArrays(escapeByte(bytes, (byte) PACKET_DELIMITER), new byte[] { PACKET_DELIMITER }));
			} catch (Exception ex1) {
				
			}
		}
	}
	
	/**
	 * Send a new full packet to the target device.
	 * 
	 * @param {String} content Content of the full packet.
	 */
	public void sendPacket (final String content) {
		this.sendPacket(content.getBytes());
	}
	
	/**
	 * Stops the execution of the connection thread if it's running and ensures to wait 
	 * until it stops the current task that is performing so it can proceed with the rest.
	 * When connection thread is running on the establishConnection phase, it will stop 
	 * as soon as an error has occurred, otherwise it will jump to the manageIncomingInput 
	 * phase and will check the runningConnectionThread boolean (previously set to false) 
	 * so it can terminate the full execution of the thread safely.
	 * 
	 * It also closes all the opened input/output streams and nullifies all the used 
	 * objects for future re-usage.
	 */
	public void closeCurrentConnection () {
		if (this.disconnecting) {
			return;
		}
		
		this.disconnecting = true;
		
		if (this.inputStream != null) {
			try {
				this.inputStream.close();
			} catch (Exception ex3) {
				
			}
		}
		
		if (this.outputStream != null) {
			try {
				this.outputStream.close();
			} catch (Exception ex4) {
				
			}
		}
		
		if (this.bluetoothSocket != null) {
			try {
				this.bluetoothSocket.close();
			} catch (Exception ex2) {
				
			}
		}
		
		this.runningConnectionThread = false;
		
		if (this.thread != null) {
			try {
				this.thread.join();
			} catch (Exception ex1) {
				
			}
		}
		
		this.thread          = null;
		this.bluetoothSocket = null;
		this.outputStream    = null;
		this.inputStream     = null;
		this.disconnecting   = false;
	}
}
