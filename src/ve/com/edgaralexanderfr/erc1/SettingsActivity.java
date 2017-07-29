package ve.com.edgaralexanderfr.erc1;

import ve.com.edgaralexanderfr.erc.BluetoothSerial;
import ve.com.edgaralexanderfr.erc.Log;
import ve.com.edgaralexanderfr.erc1.api.ERC1SerialProtocol;
import ve.com.edgaralexanderfr.erc1.api.ERC1Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

public class SettingsActivity extends ERCActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, OnItemSelectedListener {
	private String STRING_RESET_DIRECTION_ON_DROP_ON_LOG     = "";
	private String STRING_RESET_DIRECTION_ON_DROP_OFF_LOG    = "";
	private String STRING_RESET_ACCELERATION_ON_DROP_ON_LOG  = "";
	private String STRING_RESET_ACCELERATION_ON_DROP_OFF_LOG = "";
	private String STRING_WHEEL_ON_LOG                       = "";
	private String STRING_WHEEL_OFF_LOG                      = "";
	private String STRING_THROTTLE_ON_LOG                    = "";
	private String STRING_THROTTLE_OFF_LOG                   = "";
	
	private Switch resetDirectionOnDrop                      = null;
	private Switch resetAccelerationOnDrop                   = null;
	private Switch wheel                                     = null;
	private Switch throttle                                  = null;
	private SeekBar motorsLevelFactor                        = null;
	private SeekBar directionForRotation                     = null;
	private Switch automaticHeadlights                       = null;
	private Switch collisionProtectionBrake                  = null;
	private Spinner plcRoutine                               = null;
	
	/**
	 * Creates the activity itself.
	 * 
	 * @param {Bundle} savedInstanceState Saved instance state.
	 */
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_settings);
		this.init();
	}
	
	/**
	 * Initializes all the necessary resources.
	 */
	private void init () {
		this.setStrings();
		this.findViews();
		this.supplyPlcRoutineSpinnerOptions();
		this.setListeners();
		this.initializeSettings();
	}
	
	/**
	 * Creates a reference for all the the necessary string resources.
	 */
	private void setStrings () {
		this.STRING_RESET_DIRECTION_ON_DROP_ON_LOG     = this.getResources().getString(R.string.settings_activity_reset_direction_on_drop_on_log);
		this.STRING_RESET_DIRECTION_ON_DROP_OFF_LOG    = this.getResources().getString(R.string.settings_activity_reset_direction_on_drop_off_log);
		this.STRING_RESET_ACCELERATION_ON_DROP_ON_LOG  = this.getResources().getString(R.string.settings_activity_reset_acceleration_on_drop_on_log);
		this.STRING_RESET_ACCELERATION_ON_DROP_OFF_LOG = this.getResources().getString(R.string.settings_activity_reset_acceleration_on_drop_off_log);
		this.STRING_WHEEL_ON_LOG                       = this.getResources().getString(R.string.settings_activity_wheel_on_log);
		this.STRING_WHEEL_OFF_LOG                      = this.getResources().getString(R.string.settings_activity_wheel_off_log);
		this.STRING_THROTTLE_ON_LOG                    = this.getResources().getString(R.string.settings_activity_throttle_on_log);
		this.STRING_THROTTLE_OFF_LOG                   = this.getResources().getString(R.string.settings_activity_throttle_off_log);
	}
	
	/**
	 * Finds the references of all the activity's views.
	 */
	private void findViews () {
		this.resetDirectionOnDrop     = (Switch)  this.findViewById(R.id.settings_activity_reset_direction_on_drop);
		this.resetAccelerationOnDrop  = (Switch)  this.findViewById(R.id.settings_activity_reset_acceleration_on_drop);
		this.wheel                    = (Switch)  this.findViewById(R.id.settings_activity_wheel);
		this.throttle                 = (Switch)  this.findViewById(R.id.settings_activity_throttle);
		this.motorsLevelFactor        = (SeekBar) this.findViewById(R.id.settings_activity_motors_level_factor);
		this.directionForRotation     = (SeekBar) this.findViewById(R.id.settings_activity_direction_for_rotation);
		this.automaticHeadlights      = (Switch)  this.findViewById(R.id.settings_activity_automatic_headlights);
		this.collisionProtectionBrake = (Switch)  this.findViewById(R.id.settings_activity_collision_protection_brake);
		this.plcRoutine               = (Spinner) this.findViewById(R.id.settings_activity_plc_routine);
	}
	
	/**
	 * Supplies the options for the PLC routine spinner within the activity.
	 */
	private void supplyPlcRoutineSpinnerOptions () {
		ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.settings_activity_plc_routine_options, android.R.layout.simple_spinner_item);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.plcRoutine.setAdapter(arrayAdapter);
	}
	
	/**
	 * Establishes all the necessary listeners.
	 */
	private void setListeners () {
		this.resetDirectionOnDrop.setOnClickListener(this);
		this.resetAccelerationOnDrop.setOnClickListener(this);
		this.wheel.setOnClickListener(this);
		this.throttle.setOnClickListener(this);
		this.motorsLevelFactor.setOnSeekBarChangeListener(this);
		this.directionForRotation.setOnSeekBarChangeListener(this);
		this.automaticHeadlights.setOnClickListener(this);
		this.collisionProtectionBrake.setOnClickListener(this);
		this.plcRoutine.setOnItemSelectedListener(this);
	}
	
	/**
	 * Establishes the settings state within the current activity.
	 */
	private void initializeSettings () {
		this.resetDirectionOnDrop.setChecked(ERC1Settings.get().resetDirectionOnDropEnabled());
		this.resetAccelerationOnDrop.setChecked(ERC1Settings.get().resetAccelerationOnDropEnabled());
		this.wheel.setChecked(ERC1Settings.get().wheelEnabled());
		this.throttle.setChecked(ERC1Settings.get().throttleEnabled());
		this.motorsLevelFactor.setProgress(ERC1Settings.get().getMotorsLevelFactor());
		this.directionForRotation.setProgress(ERC1Settings.get().getDirectionForRotation());
		this.automaticHeadlights.setChecked(ERC1Settings.get().automaticHeadlightsEnabled());
		this.collisionProtectionBrake.setChecked(ERC1Settings.get().collisionProtectionBrakeEnabled());
		this.plcRoutine.setSelection(ERC1Settings.get().getPlcRoutine());
		ERC1Settings.get().setERC1SerialProtocol(ERC1SerialProtocol.get(BluetoothSerial.get()));
	}
	
	/**
	 * Called when a view has been clicked.
	 * 
	 * @param {View} view The view that was clicked.
	 */
	@Override
	public void onClick (View view) {
		final int viewId = view.getId();
		
		switch (viewId) {
			case R.id.settings_activity_reset_direction_on_drop     : {
				final boolean checked = ((Switch) view).isChecked();
				ERC1Settings.get().setResetDirectionOnDrop(checked);
				Log.write((checked) ? this.STRING_RESET_DIRECTION_ON_DROP_ON_LOG : this.STRING_RESET_DIRECTION_ON_DROP_OFF_LOG );
			}; break;
			case R.id.settings_activity_reset_acceleration_on_drop  : {
				final boolean checked = ((Switch) view).isChecked();
				ERC1Settings.get().setResetAccelerationOnDrop(checked);
				Log.write((checked) ? this.STRING_RESET_ACCELERATION_ON_DROP_ON_LOG : this.STRING_RESET_ACCELERATION_ON_DROP_OFF_LOG );
			}; break;
			case R.id.settings_activity_wheel                       : {
				final boolean checked = ((Switch) view).isChecked();
				ERC1Settings.get().setWheel(checked);
				Log.write((checked) ? this.STRING_WHEEL_ON_LOG : this.STRING_WHEEL_OFF_LOG );
			}; break;
			case R.id.settings_activity_throttle                    : {
				final boolean checked = ((Switch) view).isChecked();
				ERC1Settings.get().setThrottle(checked);
				Log.write((checked) ? this.STRING_THROTTLE_ON_LOG : this.STRING_THROTTLE_OFF_LOG );
			}; break;
			case R.id.settings_activity_automatic_headlights        : {
				ERC1Settings.get().setAutomaticHeadlights(((Switch) view).isChecked());
			}; break;
			case R.id.settings_activity_collision_protection_brake  : {
				ERC1Settings.get().setCollisionProtectionBrake(((Switch) view).isChecked());
			}; break;
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
			case R.id.settings_activity_motors_level_factor    : {
				ERC1Settings.get().setMotorsLevelFactor(seekBar.getProgress());
			}; break;
			case R.id.settings_activity_direction_for_rotation : {
				ERC1Settings.get().setDirectionForRotation(seekBar.getProgress());
			}; break;
		}
	}
	
	/**
	 * Callback method to be invoked when an item in this view has been selected.
	 * 
	 * @param {AdapterView<?>} parent   The AdapterView where the selection happened.
	 * @param {View}           view     The view within the AdapterView that was clicked.
	 * @param {int}            position The position of the view in the adapter.
	 * @param {long}           id       The row id of the item that is selected.
	 */
	@Override
	public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
		ERC1Settings.get().setPlcRoutine((byte) id);
	}
	
	/**
	 * Callback method to be invoked when the selection disappears from this view.
	 * 
	 * @param {AdapterView<?>} parent The AdapterView that now contains no selected item.
	 */
	@Override
	public void onNothingSelected (AdapterView<?> parent) {
		
	}
}
