package geohack.apps.awosm;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.Projection;
import org.xmlpull.v1.XmlSerializer;

import android.text.TextUtils;
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
    public String outputFormat = "xml";
    public Integer timeout = 25;

    public String getXmlFromQuery(String query, Projection bboxProjection) {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        String regexQuery = "("
                + query + "|"
                + query.toLowerCase(Locale.getDefault()) + "|"
                + query.toUpperCase(Locale.getDefault()) + "|"
                + this.toTitleCase(query) + ")";
        String amenityQuery = query.toLowerCase(Locale.getDefault()).replace(" ", "_");

        try {
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag("", "osm-script");
            xmlSerializer.attribute("", "output", this.outputFormat);
            xmlSerializer.attribute("", "timeout", this.timeout.toString());

            xmlSerializer.startTag("", "union");

            this.addNodeToQuery(xmlSerializer, true, "name", regexQuery, bboxProjection);
            this.addNodeToQuery(xmlSerializer, true, "amenity", amenityQuery, bboxProjection);

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

    public void getResults(String query, Projection bboxProjection,
            AsyncHttpResponseHandler responseHandler) {
        String xml = this.getXmlFromQuery(query, bboxProjection);
        Log.d("OverPass XML", xml);

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("data", xml);
        client.post(this.baseUrl, params, responseHandler);
    }

    private void addNodeToQuery(XmlSerializer xmlSerializer, Boolean regex, String key,
            String value, Projection bboxProjection) {
        try {
            String[] types = { "node", "way", "relation" };
            for (String type : types) {
                xmlSerializer.startTag("", "query");
                xmlSerializer.attribute("", "type", type);
                xmlSerializer.startTag("", "has-kv");
                xmlSerializer.attribute("", "k", key);

                if (regex) {
                    xmlSerializer.attribute("", "regv", value);
                } else {
                    xmlSerializer.attribute("", "v", value);
                }

                xmlSerializer.endTag("", "has-kv");
                xmlSerializer.startTag("", "bbox-query");
                xmlSerializer.attribute("", "s",
                        String.valueOf(bboxProjection.getSouthWest().getLatitude()));
                xmlSerializer.attribute("", "w",
                        String.valueOf(bboxProjection.getSouthWest().getLongitude()));
                xmlSerializer.attribute("", "n",
                        String.valueOf(bboxProjection.getNorthEast().getLatitude()));
                xmlSerializer.attribute("", "e",
                        String.valueOf(bboxProjection.getNorthEast().getLongitude()));
                xmlSerializer.endTag("", "bbox-query");
                xmlSerializer.endTag("", "query");
            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String toTitleCase(String str) {
        final String[] arr = TextUtils.split(str, " ");
        final int len = arr.length;

        for (int i = 0; i < len; i++) {
            final String s = arr[i];
            final String s0 = "" + s.toUpperCase().charAt(0);
            final String s1 = s.toLowerCase().substring(1, s.length());
            arr[i] = s0 + s1;
        }

        str = TextUtils.join(" ", arr);
        return str;
    }
}
