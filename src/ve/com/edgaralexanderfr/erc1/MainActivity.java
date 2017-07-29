package ve.com.edgaralexanderfr.erc1;

import ve.com.edgaralexanderfr.erc.BluetoothSerial;
import ve.com.edgaralexanderfr.erc.Log;
import ve.com.edgaralexanderfr.erc1.api.ERC1SerialProtocol;
import ve.com.edgaralexanderfr.erc1.api.ERC1SerialProtocolEvents;
import ve.com.edgaralexanderfr.erc1.api.ERC1Settings;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

public class MainActivity extends ERCActivity implements View.OnTouchListener, SeekBar.OnSeekBarChangeListener, SensorEventListener, Runnable {
	public static final short ANALOGIC_UPDATE_TIME      = 500;
	public static final byte GEAR_REVERSE               = 0;
	public static final byte GEAR_NEUTRAL               = 1;
	public static final byte GEAR_FORWARD               = 2;
	public static final byte GEAR_DIRECTION_UP_SHIFT    = 1;
	public static final byte GEAR_DIRECTION_DOWN_SHIFT  = -1;
	public static final byte SENSOR_MAX_ACCELERATION    = 10;
	
	private String STRING_DISABLED_BLUETOOTH_MESSAGE    = "";
	private String STRING_NO_PAIRED_DEVICES_MESSAGE     = "";
	private String STRING_SELECT_PAIRED_DEVICE_MESSAGE  = "";
	private String STRING_CONNECTION_SUCCESS_MESSAGE    = "";
	private String STRING_CONNECTION_FAILED_MESSAGE     = "";
	private String STRING_DISCONNECTED_MESSAGE          = "";
	private String STRING_NOT_CONNECTED_MESSAGE         = "";
	private String STRING_CONNECTION_SUCCESS_LOG        = "";
	private String STRING_CONNECTION_FAILED_LOG         = "";
	private String STRING_COLLISION_SECTORS_UPDATED_LOG = "";
	private String STRING_DISCONNECTED_LOG              = "";
	private String[] STRING_OUT_RESPONSE_LOGS           = new String[0];
	
	private ImageButton leftBlinkerButton               = null;
	private ImageButton headLightsButton                = null;
	private ImageButton hornButton                      = null;
	private ImageButton sirenButton                     = null;
	private ImageButton emergencyLightsButton           = null;
	private ImageButton rightBlinkerButton              = null;
	private SeekBar directionController                 = null;
	private ImageView northWestCollisionSector          = null;
	private ImageView northEastCollisionSector          = null;
	private ImageView southWestCollisionSector          = null;
	private ImageView southEastCollisionSector          = null;
	private SeekBar accelerationController              = null;
	private ImageButton gearButton                      = null;
	private ImageButton brakeButton                     = null;
	private ImageButton accelerateButton                = null;
	private SensorManager sensorManager                 = null;
	private Sensor sensor                               = null;
	private Handler analogicTimerHandler                = new Handler();
	private byte gear                                   = GEAR_FORWARD;
	private byte gearDirection                          = GEAR_DIRECTION_DOWN_SHIFT;
	
	/**
	 * Returns the current gear.
	 * 
	 * @return {byte}.
	 */
	public byte getGear () {
		return this.gear;
	}
	
	/**
	 * Returns the current gear direction (1 or -1).
	 * 
	 * @return {byte}.
	 */
	public byte getGearDirection () {
		return this.gearDirection;
	}
	
	/**
	 * Creates the activity itself.
	 * 
	 * @param {Bundle} savedInstanceState Saved instance state.
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		this.init();
	}
	
	/**
	 * Initializes all the necessary resources.
	 */
	private void init () {
		this.setStrings();
		this.findViews();
		this.setListeners();
		this.adaptAccelerometer();
	}
	
	/**
	 * Creates a reference for all the the necessary string resources.
	 */
	private void setStrings () {
		this.STRING_DISABLED_BLUETOOTH_MESSAGE    = this.getResources().getString(R.string.main_activity_disabled_bluetooth_message);
		this.STRING_NO_PAIRED_DEVICES_MESSAGE     = this.getResources().getString(R.string.main_activity_no_paired_devices_message);
		this.STRING_SELECT_PAIRED_DEVICE_MESSAGE  = this.getResources().getString(R.string.main_activity_select_paired_device_message);
		this.STRING_CONNECTION_SUCCESS_MESSAGE    = this.getResources().getString(R.string.main_activity_connection_success_message);
		this.STRING_CONNECTION_FAILED_MESSAGE     = this.getResources().getString(R.string.main_activity_connection_failed_message);
		this.STRING_DISCONNECTED_MESSAGE          = this.getResources().getString(R.string.main_activity_disconnected_message);
		this.STRING_NOT_CONNECTED_MESSAGE         = this.getResources().getString(R.string.main_activity_not_connected_message);
		this.STRING_CONNECTION_SUCCESS_LOG        = this.getResources().getString(R.string.main_activity_connection_success_log);
		this.STRING_CONNECTION_FAILED_LOG         = this.getResources().getString(R.string.main_activity_connection_failed_log);
		this.STRING_COLLISION_SECTORS_UPDATED_LOG = this.getResources().getString(R.string.main_activity_collision_sectors_updated_log);
		this.STRING_DISCONNECTED_LOG              = this.getResources().getString(R.string.main_activity_disconnected_log);
		this.STRING_OUT_RESPONSE_LOGS             = this.getResources().getStringArray(R.array.main_activity_out_response_logs);
	}
	
	/**
	 * Finds the references of all the activity's views.
	 */
	private void findViews () {
		this.leftBlinkerButton        = (ImageButton) this.findViewById(R.id.main_activity_left_blinker_button);
		this.headLightsButton         = (ImageButton) this.findViewById(R.id.main_activity_headlights_button);
		this.hornButton               = (ImageButton) this.findViewById(R.id.main_activity_horn_button);
		this.sirenButton              = (ImageButton) this.findViewById(R.id.main_activity_siren_button);
		this.emergencyLightsButton    = (ImageButton) this.findViewById(R.id.main_activity_emergency_lights_button);
		this.rightBlinkerButton       = (ImageButton) this.findViewById(R.id.main_activity_right_blinker_button);
		this.directionController      = (SeekBar)     this.findViewById(R.id.main_activity_direction_controller);
		this.northWestCollisionSector = (ImageView)   this.findViewById(R.id.main_activity_northwest_collision_sector);
		this.northEastCollisionSector = (ImageView)   this.findViewById(R.id.main_activity_northeast_collision_sector);
		this.southWestCollisionSector = (ImageView)   this.findViewById(R.id.main_activity_southwest_collision_sector);
		this.southEastCollisionSector = (ImageView)   this.findViewById(R.id.main_activity_southeast_collision_sector);
		this.accelerationController   = (SeekBar)     this.findViewById(R.id.main_activity_acceleration_controller);
		this.gearButton               = (ImageButton) this.findViewById(R.id.main_activity_gear_button);
		this.brakeButton              = (ImageButton) this.findViewById(R.id.main_activity_brake_button);
		this.accelerateButton         = (ImageButton) this.findViewById(R.id.main_activity_accelerate_button);
	}
	
	/**
	 * Establishes all the necessary listeners.
	 */
	private void setListeners () {
		final MainActivity $this = this;
		
		ERC1SerialProtocol.get(BluetoothSerial.get()).setEvents(new ERC1SerialProtocolEvents () {
			/**
			 * Invoked when ERC has been connected.
			 */
			@Override
			public void onConnectionSuccess () {
				$this.runOnUiThread(new Runnable () {
					/**
					 * Starts executing the active part of the class' code.
					 */
					@Override
					public void run () {
						Log.write($this.STRING_CONNECTION_SUCCESS_LOG);
						$this.alert($this.STRING_CONNECTION_SUCCESS_MESSAGE);
					}
				});
			}
			
			/**
			 * Invoked when there's a failure during the connection attempt with the ERC.
			 */
			@Override
			public void onConnectionFailed () {
				$this.runOnUiThread(new Runnable () {
					/**
					 * Starts executing the active part of the class' code.
					 */
					@Override
					public void run () {
						Log.write($this.STRING_CONNECTION_FAILED_LOG);
						$this.alert($this.STRING_CONNECTION_FAILED_MESSAGE);
					}
				});
			}
			
			/**
			 * Invoked when ERC notifies a log.
			 * 
			 * @param {byte} logCode ERC's log code.
			 */
			@Override
			public void onLogWrite (final byte logCode) {
				if (logCode >= 0 && logCode < STRING_OUT_RESPONSE_LOGS.length) {
					Log.write(STRING_OUT_RESPONSE_LOGS[ logCode ]);
				}
			}
			
			/**
			 * Invoked when ERC reports its used memory.
			 * 
			 * @param {int} freeMemory ERC's free memory.
			 */
			@Override
			public void onLogWriteFreeMemory (final int freeMemory) {
				Log.write(freeMemory + "");
			}
			
			/**
			 * Invoked when ERC updates the collision sectors state.
			 * 
			 * @param {byte} northWestCollisionSectorDistance Scaled distance with closest north-west object.
			 * @param {byte} northEastCollisionSectorDistance Scaled distance with closest north-east object.
			 * @param {byte} southWestCollisionSectorDistance Scaled distance with closest south-west object.
			 * @param {byte} southEastCollisionSectorDistance Scaled distance with closest south-east object.
			 */
			@Override
			public void onCollisionSectorsUpdate (final byte northWestCollisionSectorDistance, final byte northEastCollisionSectorDistance, final byte southWestCollisionSectorDistance, final byte southEastCollisionSectorDistance) {
				$this.runOnUiThread(new Runnable () {
					/**
					 * Starts executing the active part of the class' code.
					 */
					@Override
					public void run () {
						//Log.write(northWestCollisionSectorDistance + ", " + northEastCollisionSectorDistance + ", " + southWestCollisionSectorDistance + ", " + southEastCollisionSectorDistance);
						Log.write($this.STRING_COLLISION_SECTORS_UPDATED_LOG);
						$this.northWestCollisionSector.setAlpha((float) (1.0f - (northWestCollisionSectorDistance / 127.0f)));
						$this.northEastCollisionSector.setAlpha((float) (1.0f - (northEastCollisionSectorDistance / 127.0f)));
						$this.southWestCollisionSector.setAlpha((float) (1.0f - (southWestCollisionSectorDistance / 127.0f)));
						$this.southEastCollisionSector.setAlpha((float) (1.0f - (southEastCollisionSectorDistance / 127.0f)));
					}
				});
			}
			
			/**
			 * Invoked when ERC has been disconnected.
			 */
			@Override
			public void onDisconnection () {
				$this.runOnUiThread(new Runnable () {
					/**
					 * Starts executing the active part of the class' code.
					 */
					@Override
					public void run () {
						Log.write($this.STRING_DISCONNECTED_LOG);
						$this.alert($this.STRING_DISCONNECTED_MESSAGE);
					}
				});
			}
		});
		
		BluetoothSerial.get().setProtocol(ERC1SerialProtocol.get(BluetoothSerial.get()));
		
		this.leftBlinkerButton.setOnTouchListener(this);
		this.headLightsButton.setOnTouchListener(this);
		this.hornButton.setOnTouchListener(this);
		this.sirenButton.setOnTouchListener(this);
		this.emergencyLightsButton.setOnTouchListener(this);
		this.rightBlinkerButton.setOnTouchListener(this);
		this.directionController.setOnSeekBarChangeListener(this);
		this.gearButton.setOnTouchListener(this);
		this.accelerationController.setOnSeekBarChangeListener(this);
		this.brakeButton.setOnTouchListener(this);
		this.accelerateButton.setOnTouchListener(this);
	}
	
	/**
	 * Adapts the accelerometer sensor to simulate the phone wheel/throttle.
	 */
	private void adaptAccelerometer () {
		this.sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		this.sensor        = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	/**
	 * Called when the activity will start interacting with the user. At this point your 
	 * activity is at the top of the activity stack, with user input going to it.
	 * 
	 * Always followed by onPause()
	 */
	@Override
	public void onResume () {
		super.onResume();
		this.sensorManager.registerListener(this, this.sensor, SensorManager.SENSOR_DELAY_NORMAL);
		this.analogicTimerHandler.postDelayed(this, ANALOGIC_UPDATE_TIME);
	}
	
	/**
	 * Called when the activity is no longer visible to the user, because another activity 
	 * has been resumed and is covering this one. This may happen either because a new 
	 * activity is being started, an existing one is being brought in front of this one, 
	 * or this one is being destroyed.
	 * 
	 * Followed by either onRestart() if this activity is coming back to interact with the 
	 * user, or onDestroy() if this activity is going away.
	 */
	@Override
	public void onStop () {
		super.onStop();
		this.sensorManager.unregisterListener(this);
		this.analogicTimerHandler.removeCallbacks(this);
	}
	
	/**
	 * Executed when creating the action bar and overflow menu.
	 * 
	 * @param  {Menu} menu Menu where to append the resource.
	 * @return {boolean}.
	 */
	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		this.getMenuInflater().inflate(R.menu.overflow_menu, menu);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Event triggered when user selects an option from overflow menu.
	 * 
	 * @param  {MenuItem} item Selected item reference.
	 * @return {boolean}.
	 */
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		final int itemId = item.getItemId();
		
		switch (itemId) {
			case R.id.overflow_menu_connect  : {
				this.connect();
			}; break;
			case R.id.overflow_menu_logs     : {
				this.openLog();
			}; break;
			case R.id.overflow_menu_settings : {
				this.openSettings();
			}; break;
			default : return super.onOptionsItemSelected(item);
		}
		
		return true;
	}
	
	/**
	 * Called when a touch event is dispatched to a view. This allows listeners to get a 
	 * chance to respond before the target view.
	 * 
	 * @param  {View}        view  The view the touch event has been dispatched to.
	 * @param  {MotionEvent} event The MotionEvent object containing full information about the event.
	 * @return {boolean}.
	 */
	@Override
	public boolean onTouch (View view, MotionEvent event) {
		final int action = event.getAction();
		
		if (action == MotionEvent.ACTION_DOWN) {
			this.onTouchDown(view);
		} else 
		if (action == MotionEvent.ACTION_UP) {
			this.onTouchUp(view);
			view.performClick();
		}
		
		return true;
	}
	
	/**
	 * Performed when a view's ACTION_DOWN is registered within the onTouch event.
	 * 
	 * @param {View} view Touched view.
	 */
	private void onTouchDown (View view) {
		if (!BluetoothSerial.get().isOutputAvailable()) {
			this.shortToast(this.STRING_NOT_CONNECTED_MESSAGE);
			
			return;
		}
		
		final int viewId = view.getId();
		
		switch (viewId) {
			case R.id.main_activity_horn_button       : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).startHorn();
			}; break;
			case R.id.main_activity_brake_button      : {
				this.directionController.setProgress(this.directionController.getMax() / 2);
				this.accelerationController.setProgress(0);
				ERC1SerialProtocol.get(BluetoothSerial.get()).startBraking();
			}; break;
			case R.id.main_activity_accelerate_button : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).accelerate();
			};
		}
	}
	
	/**
	 * Performed when a view's ACTION_UP is registered within the onTouch event.
	 * 
	 * @param {View} view Touched view.
	 */
	private void onTouchUp (View view) {
		if (!BluetoothSerial.get().isOutputAvailable()) {
			this.shortToast(this.STRING_NOT_CONNECTED_MESSAGE);
			
			return;
		}
		
		final int viewId = view.getId();
		
		switch (viewId) {
			case R.id.main_activity_left_blinker_button     : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).toggleLeftBlinker();
			}; break;
			case R.id.main_activity_headlights_button       : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).toggleHeadlights();
			}; break;
			case R.id.main_activity_horn_button             : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).stopHorn();
			}; break;
			case R.id.main_activity_siren_button            : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).toggleSiren();
			}; break;
			case R.id.main_activity_emergency_lights_button : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).toggleEmergencyLights();
			}; break;
			case R.id.main_activity_right_blinker_button    : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).toggleRightBlinker();
			}; break;
			case R.id.main_activity_gear_button             : {
				this.shift();
			}; break;
			case R.id.main_activity_brake_button            : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).stopBraking();
			}; break;
			case R.id.main_activity_accelerate_button       : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).stopAccelerating();
			};
		}
	}
	
	/**
	 * Notification that the progress level has changed. Clients can use the fromUser 
	 * parameter to distinguish user-initiated changes from those that occurred 
	 * programmatically.
	 * 
	 * @param {SeekBar} seekBar  The SeekBar whose progress has changed.
	 * @param {int}     progress The current progress level. This will be in the range 
	 * 							 0..max where max was set by setMax(int). (The default 
	 * 							 value for max is 100.)
	 * @param {boolean} fromUser True if the progress change was initiated by the user.
	 */
	@Override
	public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
		
	}
	
	/**
	 * Notification that the user has started a touch gesture. Clients may want to use 
	 * this to disable advancing the seekbar.
	 * 
	 * @param {SeekBar} seekBar The SeekBar in which the touch gesture began.
	 */
	@Override
	public void onStartTrackingTouch (SeekBar seekBar) {
		
	}
	
	/**
	 * Notification that the user has finished a touch gesture. Clients may want to use 
	 * this to re-enable advancing the seekbar.
	 * 
	 * @param {SeekBar} seekBar The SeekBar in which the touch gesture began.
	 */
	@Override
	public void onStopTrackingTouch (SeekBar seekBar) {
		final int seekBarId = seekBar.getId();
		
		switch (seekBarId) {
			case R.id.main_activity_direction_controller    : {
				if (ERC1Settings.get().resetDirectionOnDropEnabled()) {
					this.directionController.setProgress(this.directionController.getMax() / 2);
				}
			}; break;
			case R.id.main_activity_acceleration_controller : {
				if (ERC1Settings.get().resetAccelerationOnDropEnabled()) {
					this.accelerationController.setProgress(0);
				}
			};
		}
	}
	
	/**
	 * Called when the accuracy of the registered sensor has changed.
	 * 
	 * @param {Sensor} sensor   Sensor instance.
	 * @param {int}    accuracy The new accuracy of this sensor, one of SensorManager.SENSOR_STATUS_* 
	 */
	@Override
	public void onAccuracyChanged (Sensor sensor, int accuracy) {
		
	}
	
	/**
	 * Called when sensor values have changed.
	 * 
	 * @param {SensorEvent} event The SensorEvent.
	 */
	@Override
	public void onSensorChanged (SensorEvent event) {
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			return;
		}
		
		this.setDirectionController(event.values[1]);
		this.setAccelerationController(event.values[2]);
	}
	
	/**
	 * Timer callback that updates the ERC's direction.
	 */
	@Override
	public void run () {
		this.updateDirectionAndAcceleration();
		this.analogicTimerHandler.postDelayed(this, ANALOGIC_UPDATE_TIME);
	}
	
	/**
	 * If bluetooth is enabled, it prompts a list of paired devices to select for 
	 * establish a single connection. 
	 */
	public void connect () {
		if (!BluetoothSerial.isBluetoothEnabled()) {
			this.alert(this.STRING_DISABLED_BLUETOOTH_MESSAGE);
			
			return;
		}
		
		final BluetoothDevice[] pairedDevices = BluetoothSerial.getPairedDevices();
		
		if (pairedDevices.length == 0) {
			this.alert(this.STRING_NO_PAIRED_DEVICES_MESSAGE);
			
			return;
		}
		
		String[] pairedDevicesNames = BluetoothSerial.getDevicesNames(pairedDevices);
		this.selectDialog(this.STRING_SELECT_PAIRED_DEVICE_MESSAGE, pairedDevicesNames, new SelectDialog () {
			/**
			 * Method triggered when an option from a select dialog is selected.
			 * 
			 * @param {int} selectedIndex Selected option index.
			 */
			@Override
			public void onSelection (int selectedIndex) {
				BluetoothDevice targetDevice = pairedDevices[ selectedIndex ];
				BluetoothSerial.get().connectTo(targetDevice);
			}
		});
	}
	
	/**
	 * Starts the log activity.
	 */
	public void openLog () {
		Intent intent = new Intent(this, LogActivity.class);
		this.startActivity(intent);
	}
	
	/**
	 * Starts the settings activity.
	 */
	public void openSettings () {
		Intent intent = new Intent(this, SettingsActivity.class);
		this.startActivity(intent);
	}
	
	/**
	 * Updates the current direction and acceleration in ERC.
	 */
	public void updateDirectionAndAcceleration () {
		final byte direction    = (byte) this.directionController.getProgress();
		final byte acceleration = (byte) this.accelerationController.getProgress();
		ERC1SerialProtocol.get(BluetoothSerial.get()).setDirectionAndAcceleration(direction, acceleration);
	}
	
	/**
	 * Alternates the current shift going forward/backward.
	 */
	public void shift () {
		switch (this.gear) {
			case GEAR_REVERSE : this.gearDirection = GEAR_DIRECTION_UP_SHIFT;   break;
			case GEAR_FORWARD : this.gearDirection = GEAR_DIRECTION_DOWN_SHIFT; break;
		}
		
		this.gear += this.gearDirection;
		
		switch (this.gear) {
			case GEAR_REVERSE : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).setReverse();
				this.gearButton.setImageResource(R.drawable.reverse_button);
			}; break;
			case GEAR_NEUTRAL : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).setNeutral();
				this.gearButton.setImageResource(R.drawable.neutral_button);
			}; break;
			case GEAR_FORWARD : {
				ERC1SerialProtocol.get(BluetoothSerial.get()).setForward();
				this.gearButton.setImageResource(R.drawable.first_gear_button);
			};
		}
	}
	
	/**
	 * Set the direction controller based on the accelerometer's Y acceleration, checking 
	 * that Y parameter is within the minus SENSOR_MAX_ACCELERATION and 
	 * SENSOR_MAX_ACCELERATION range.
	 * 
	 * @param {float} y Accelerometer Y acceleration.
	 */
	public void setDirectionController (final float y) {
		if (!ERC1Settings.get().wheelEnabled() || y < -SENSOR_MAX_ACCELERATION || y > SENSOR_MAX_ACCELERATION) {
			return;
		}
		
		final int directionControllerMiddle = this.directionController.getMax() / 2;
		final int difference                = Math.round((Math.abs(y) * directionControllerMiddle) / SENSOR_MAX_ACCELERATION);
		final int progress                  = directionControllerMiddle + ((y > 0) ? difference : -difference );
		this.directionController.setProgress(progress);
	}
	
	/**
	 * Set the acceleration controller based on the accelerometer's Y acceleration, 
	 * checking that Z parameter is within the 0 and SENSOR_MAX_ACCELERATION range.
	 * 
	 * @param {float} z Accelerometer Z acceleration.
	 */
	public void setAccelerationController (final float z) {
		if (!ERC1Settings.get().throttleEnabled() || z < 0 || z > SENSOR_MAX_ACCELERATION) {
			return;
		}
		
		final int progress = Math.round((z * this.accelerationController.getMax()) / SENSOR_MAX_ACCELERATION);
		this.accelerationController.setProgress(progress);
	}
}
