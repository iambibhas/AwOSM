package geohack.apps.awosm;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;
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
    private NodeItemizedOverlay mItemizedOverlay;
    private MapView mapView;

    public MapFragment() {
    }

    private void cacheLocation(Context context, GeoPoint center) {
        String FILENAME = "awosm.lastpos";

        FileOutputStream fos;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(center.toDoubleString().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String readCachedLocationString(Context context) {
        String FILENAME = "awosm.lastpos";
        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = context.openFileInput(FILENAME);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }

            isr.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return datax.toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        final Context context = rootView.getContext();
        GeoPoint startPoint = new GeoPoint(22.4109, 88.6216);

        mapView = (MapView) rootView.findViewById(R.id.mapview);
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
         * Handles the search bar
         */
        final SearchView osmSearchView = (SearchView) rootView.findViewById(R.id.OSMSearchView);
        osmSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Executed when you press Search after entering text
                Log.d("Search", query);
                Projection bboxProjection = mapView.getProjection();

                OverpassApiWrapper overpass = new OverpassApiWrapper();
                overpass.getResults(query.trim(), bboxProjection, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        // API Call was successful. Parse JSON and do stuff
                        String jsonResponse = new String(response);
                        try {
                            JSONObject jObject = new JSONObject(jsonResponse);
                            JSONArray jArray = jObject.getJSONArray("elements");
                            if (jArray.length() > 0) {
                                drawResults(jArray);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Log.d("API", jsonResponse);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
                            Throwable e) {
                        // API call failed
                        
                        String error = Arrays.toString(errorResponse);
                        
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        Log.d("API Error", String.valueOf(statusCode));
                        Log.d("API Error", error);
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Fire every time the text field's value changes
                return false;
            }
        });

        return rootView;
    }
    
    public void drawResults(JSONArray elements) {
        // We've got a list of nodes, draw some markers
        final Context context = getActivity().getApplicationContext();
        ResourceProxy mResourceProxy = new DefaultResourceProxyImpl(context);
        mapView = (MapView) getActivity().findViewById(R.id.mapview);
        Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
        mItemizedOverlay = new NodeItemizedOverlay(drawable, mResourceProxy);
        mapView.getOverlays().add(mItemizedOverlay);
        
        ArrayList<OverlayItem> mItems = new ArrayList<OverlayItem>();
        
        for (int i = 0; i < elements.length(); i++) {
            try {
                JSONObject oneObject = elements.getJSONObject(i);
                
                if (oneObject.getString("type").equalsIgnoreCase("node") && oneObject.has("tags")) {
                    JSONObject tags = oneObject.getJSONObject("tags");
                    
                    mItemizedOverlay.addItem(
                        new GeoPoint(oneObject.getDouble("lat"), oneObject.getDouble("lon")),
                        tags.getString("name"),
                        ""
                    );
                    Log.d("API", "Adding Overlay " + tags.getString("name"));
                } else {
                    Log.d("API", "I'm in Else block");
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
        
        
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
    
    public static GeoPoint coordinatesToGeoPoint(double[] coords) {
        if (coords.length > 2) {
            return null;
        }
        if (coords[0] == Double.NaN || coords[1] == Double.NaN) {
            return null;
        }
        final int latitude = (int) (coords[0] * 1E6);
        final int longitude = (int) (coords[1] * 1E6);
        return new GeoPoint(latitude, longitude);
    }

}