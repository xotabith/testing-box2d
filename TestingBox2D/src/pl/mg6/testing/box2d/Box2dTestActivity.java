package pl.mg6.testing.box2d;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;

public class Box2dTestActivity extends Activity {

	private TestView view;
	private int rotation;
	
	private static final String KEY_DEFAULT_LANDSCAPE = "pl.mg6.testing.box2d.default_landscape";
	private boolean defaultLandscape;
	
	private static final String PREF_ROTATION = "pref.key.rotation";
	private static final String PREF_GRAVITY = "pref.key.gravity";
	
	private static final int GRAVITY_NORMAL = 0;
	private static final int GRAVITY_MOON = 3;
	private static final int GRAVITY_ZERO = 1;
	private static final int GRAVITY_ACCELEROMETER = 2;
	
	private int gravity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = new TestView(this);
		setContentView(view);
		// new World(new Vec2(0.0f, -10.0f), true);
		gravity = getPreferences(Context.MODE_PRIVATE).getInt(PREF_GRAVITY, GRAVITY_ACCELEROMETER);
		w = (PhysicsWorld) getLastNonConfigurationInstance();
		if (w == null) {
			Vec2 grav = new Vec2(0.0f, 0.0f);
			if (gravity == GRAVITY_NORMAL) {
				grav.y = -10.0f;
			} else if (gravity == GRAVITY_MOON) {
				grav.y = -1.67f;
			}
			w = new PhysicsWorld();
			w.create(grav);
		}
		view.setModel(w);
		if (savedInstanceState != null) {
			defaultLandscape = savedInstanceState.getBoolean(KEY_DEFAULT_LANDSCAPE);
			
		} else {
			defaultLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
			
			rotation = getPreferences(Context.MODE_PRIVATE).getInt(PREF_ROTATION, Surface.ROTATION_0);
			int orientation;
			if (defaultLandscape && rotation == Surface.ROTATION_90
					|| !defaultLandscape && rotation == Surface.ROTATION_180) {
				orientation = 9;
			} else if (defaultLandscape && rotation == Surface.ROTATION_180
					|| !defaultLandscape && rotation == Surface.ROTATION_270) {
				orientation = 8;
			} else if (defaultLandscape && rotation == Surface.ROTATION_270
					|| !defaultLandscape && rotation == Surface.ROTATION_0) {
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			} else {
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			}
			setRequestedOrientation(orientation);
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_DEFAULT_LANDSCAPE, defaultLandscape);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return w;
	}

	private PhysicsWorld w;
	private Runnable r;

	@Override
	protected void onResume() {
		super.onResume();
		r = new Runnable() {
			public void run() {
				w.update();
				view.invalidate();
				getWindow().getDecorView().postDelayed(r, 10);
			}
		};
		getWindow().getDecorView().post(r);
		rotation = getWindowManager().getDefaultDisplay().getOrientation();
		Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.putInt(PREF_ROTATION, rotation);
		editor.commit();
		if (gravity == GRAVITY_ACCELEROMETER) {
			SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if (sensor != null) {
				sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		getWindow().getDecorView().removeCallbacks(r);
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.unregisterListener(listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_clear:
			removeObjects();
			return true;
		case R.id.item_rotate:
			rotateScreen();
			return true;
		case R.id.item_gravity_normal: {
			SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensorManager.unregisterListener(listener);
			w.getWorld().setGravity(new Vec2(0.0f, -10.0f));
			saveGravity(GRAVITY_NORMAL);
			return true;
		}
		case R.id.item_gravity_moon: {
			SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensorManager.unregisterListener(listener);
			w.getWorld().setGravity(new Vec2(0.0f, -1.67f));
			saveGravity(GRAVITY_MOON);
			return true;
		}
		case R.id.item_gravity_zero: {
			SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensorManager.unregisterListener(listener);
			w.getWorld().setGravity(new Vec2(0.0f, 0.0f));
			saveGravity(GRAVITY_ZERO);
			return true;
		}
		case R.id.item_gravity_accelerometer:
			SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if (sensor != null) {
				sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
			}
			saveGravity(GRAVITY_ACCELEROMETER);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void saveGravity(int newGravity) {
		gravity = newGravity;
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt(PREF_GRAVITY, gravity);
		editor.commit();
	}
	
	private void removeObjects() {
		Body body = w.getWorld().getBodyList();
		while (body != null) {
			Body current = body;
			body = body.m_next;
			if (current.m_userData != null) {
				w.getWorld().destroyBody(current);
			}
		}
	}
	
	private void rotateScreen() {
		int orientation;
		if (defaultLandscape) {
			if (rotation == Surface.ROTATION_0) {
				if (Build.VERSION.SDK_INT >= 9) {
					orientation = 9;
				} else {
					orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				}
			} else if (rotation == Surface.ROTATION_90) {
				orientation = 8;
			} else if (rotation == Surface.ROTATION_180) {
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			} else {
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			}
		} else {
			if (rotation == Surface.ROTATION_0) {
				orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			} else if (rotation == Surface.ROTATION_90) {
				if (Build.VERSION.SDK_INT >= 9) {
					orientation = 9;
				} else {
					orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				}
			} else if (rotation == Surface.ROTATION_180) {
				orientation = 8;
			} else {
				orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		setRequestedOrientation(orientation);
	}

	private final SensorEventListener listener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent event) {
			float x, y;
			if (rotation == Surface.ROTATION_0) {
				x = -event.values[SensorManager.DATA_X];
				y = -event.values[SensorManager.DATA_Y];
			} else if (rotation == Surface.ROTATION_90) {
				x = event.values[SensorManager.DATA_Y];
				y = -event.values[SensorManager.DATA_X];
			} else if (rotation == Surface.ROTATION_180) {
				x = event.values[SensorManager.DATA_X];
				y = event.values[SensorManager.DATA_Y];
			} else {
				x = -event.values[SensorManager.DATA_Y];
				y = event.values[SensorManager.DATA_X];
			}
			// Log.v("gravity", "x,y: " + x + " " + y);
			w.getWorld().setGravity(new Vec2(x, y));
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
}