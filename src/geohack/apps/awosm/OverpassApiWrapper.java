package geohack.apps.awosm;

import java.io.IOException;
import java.io.StringWriter;

import org.osmdroid.views.Projection;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import com.loopj.android.http.*;


//<osm-script output="json" timeout="25">
//  <!-- gather results -->
//  <union>
//    <!-- query part for: “atm” -->
//    <query type="node">
//      <has-kv k="name" v="KFC"/>
//      <bbox-query {{bbox}}/>
//    </query>
//    <query type="node">
//      <has-kv k="amenity" v="atm"/>
//      <bbox-query {{bbox}}/>
//    </query>
//  </union>
//  <!-- print results -->
//  <print mode="body"/>
//  <recurse type="down"/>
//  <print mode="skeleton" order="quadtile"/>
//</osm-script>


public class OverpassApiWrapper {
	public String baseUrl = "http://overpass-api.de/api/interpreter";
	public String outputFormat = "json";
	public Integer timeout = 25;
	
	public String getXmlFromQuery(String query, String bbox) {
		XmlSerializer xmlSerializer = Xml.newSerializer();
	    StringWriter writer = new StringWriter();
	    
	    try {
	    	xmlSerializer.setOutput(writer);
			xmlSerializer.startDocument("UTF-8", true);
			
			xmlSerializer.startTag("", "osm-script");
			xmlSerializer.attribute("", "output", this.outputFormat);
			xmlSerializer.attribute("", "timeout", this.timeout.toString());
			
			xmlSerializer.startTag("", "union");
			
			xmlSerializer.startTag("", "query");
			xmlSerializer.attribute("", "type", "node");
			xmlSerializer.startTag("", "has-kv");
			xmlSerializer.attribute("", "k", "name");
			xmlSerializer.attribute("", "v", query);
			xmlSerializer.endTag("", "has-kv");
			xmlSerializer.startTag("", "bbox-query");
			xmlSerializer.attribute("", "s", bbox.split(",")[0]);
			xmlSerializer.attribute("", "w", bbox.split(",")[1]);
			xmlSerializer.attribute("", "n", bbox.split(",")[2]);
			xmlSerializer.attribute("", "e", bbox.split(",")[3]);
			xmlSerializer.endTag("", "bbox-query");
			xmlSerializer.endTag("", "query");
			
			xmlSerializer.startTag("", "query");
			xmlSerializer.attribute("", "type", "node");
			xmlSerializer.startTag("", "has-kv");
			xmlSerializer.attribute("", "k", "amenity");
			xmlSerializer.attribute("", "v", query);
			xmlSerializer.endTag("", "has-kv");
			xmlSerializer.startTag("", "bbox-query");
			xmlSerializer.attribute("", "s", bbox.split(",")[0]);
			xmlSerializer.attribute("", "w", bbox.split(",")[1]);
			xmlSerializer.attribute("", "n", bbox.split(",")[2]);
			xmlSerializer.attribute("", "e", bbox.split(",")[3]);
			xmlSerializer.endTag("", "bbox-query");
			xmlSerializer.endTag("", "query");
			
			xmlSerializer.endTag("", "union");

			xmlSerializer.startTag("", "print");
			xmlSerializer.attribute("", "mode", "body");
			xmlSerializer.endTag("", "print");

			xmlSerializer.startTag("", "recurse");
			xmlSerializer.attribute("", "type", "down");
			xmlSerializer.endTag("", "recurse");

			xmlSerializer.startTag("", "print");
			xmlSerializer.attribute("", "mode", "skeleton");
			xmlSerializer.attribute("", "order", "quadtile");
			xmlSerializer.endTag("", "print");
			
			xmlSerializer.endTag("", "osm-script");
			
			xmlSerializer.endDocument();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return writer.toString();
	}
	
	public void getResults(String query, Projection bboxProjection, AsyncHttpResponseHandler responseHandler) {
		String bbox = this.getBboxFromProjection(bboxProjection);
		String xml = this.getXmlFromQuery(query, bbox);
		Log.d("OverPass XML", xml);
		
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("data", xml);
		client.post(this.baseUrl, params, responseHandler);
	}
	
	public String getBboxFromProjection(Projection bboxProjection) {
		return String.valueOf(bboxProjection.getSouthWest().getLatitude()) + ','
		+ String.valueOf(bboxProjection.getSouthWest().getLongitude()) + ','
		+ String.valueOf(bboxProjection.getNorthEast().getLatitude()) + ','
		+ String.valueOf(bboxProjection.getNorthEast().getLongitude());
	}
}
