package geohack.apps.awosm;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

public class MapActivity extends ActionBarActivity {
	private static final int DIALOG_ABOUT_ID = 1;
	private static final String MAP_FRAGMENT_TAG = "geohack.apps.awosm.MAP_FRAGMENT_TAG";
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private MyLocationNewOverlay mLocationOverlay;
	    private CompassOverlay mCompassOverlay;
	    
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_map, container,
					false);
			
			final Context context = rootView.getContext();
			GeoPoint startPoint = new GeoPoint(22.4109, 88.6216);
			
			final MapView mapView = (MapView) rootView.findViewById(R.id.mapview);
	        mapView.setBuiltInZoomControls(true);
	        mapView.setMultiTouchControls(true);
	        
	        mapView.getController().setZoom(12);
	        mapView.getController().setCenter(startPoint);
	        
	        GpsMyLocationProvider imlp = new GpsMyLocationProvider(context);
	        imlp.setLocationUpdateMinDistance(10);
	        imlp.setLocationUpdateMinTime(5000);
	        
	        mLocationOverlay = new MyLocationNewOverlay(context, imlp, mapView);
	    	mLocationOverlay.enableMyLocation();
	    	mapView.getOverlays().add(mLocationOverlay);
	    	
			return rootView;
		}
		
		@Override
		public void onResume() {
			super.onResume();
		    mLocationOverlay.enableMyLocation();
		    mLocationOverlay.enableFollowLocation();
		}
		

		@Override
		public void onPause() {
			super.onPause();
		    mLocationOverlay.disableMyLocation();
		    mLocationOverlay.disableFollowLocation();
		}
		
	}

}
