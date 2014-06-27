package geohack.apps.awosm;

import java.util.Arrays;

import org.apache.http.Header;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapFragment extends Fragment {
	private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;
    
	public MapFragment() {
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
        
        mapView.getController().setZoom(16);
        mapView.getController().setCenter(startPoint);
        
        GpsMyLocationProvider imlp = new GpsMyLocationProvider(context);
        imlp.setLocationUpdateMinDistance(10);
        imlp.setLocationUpdateMinTime(5000);
        
        mLocationOverlay = new MyLocationNewOverlay(context, imlp, mapView);
    	mLocationOverlay.enableMyLocation();
    	mapView.getOverlays().add(mLocationOverlay);
    	
    	/*
    	Handles the search bar
    	*/
    	final SearchView osmSearchView = (SearchView) rootView.findViewById(R.id.OSMSearchView);
    	osmSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				// TODO Auto-generated method stub
				Log.d("Search", query);
				Projection bbox = mapView.getProjection();
		    	String strBbox = String.valueOf(bbox.getSouthWest().getLatitude()) + ','
		    			+ String.valueOf(bbox.getSouthWest().getLongitude()) + ','
		    			+ String.valueOf(bbox.getNorthEast().getLatitude()) + ','
		    			+ String.valueOf(bbox.getNorthEast().getLongitude());
		    	
				OverpassApiWrapper overpass = new OverpassApiWrapper();
				overpass.getResults(
					query,
					strBbox,
					new AsyncHttpResponseHandler() {
						
						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] response) {
							// TODO Auto-generated method stub
							String jsonResponse = new String(response);
							Log.d("API", jsonResponse);
						}
						
						@Override
						public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
							// TODO Auto-generated method stub
							
							String error = Arrays.toString(errorResponse);
							Log.d("API Error", String.valueOf(statusCode));
							Log.d("API Error", error);
						}
					}
				);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
		});
    	
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