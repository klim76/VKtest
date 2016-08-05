package klim.mobile.android.testvk;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created by e.klim on 03.08.2016.
 */
public class NetUtil {
    public static JSONObject getJSON(String url) throws IOException, JSONException {
        return getJSON(url, null);
    }

    public static JSONObject getJSON(String url, String encoding) throws IOException, JSONException {
        final String string = getString(url, encoding);
        if (string != null) {
            return new JSONObject(string);
        } else {
            return null;
        }
    }

    public static String getString(String url, String encoding) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        // Prepare a request object
        HttpGet httpget = new HttpGet(url);
        // Execute the request
        HttpResponse response = httpclient.execute(httpget);
        // Return the result
        return readResponse(response, encoding);
    }

    private static String readResponse(HttpResponse response, String encoding) throws IOException {
        String result = null;
        // Get the response entity
        HttpEntity entity = response.getEntity();
        // If response entity is not null
        if (entity != null) {
            InputStream in = null;
            try {
                // get entity contents and convert it to string
                in = entity.getContent();
                if (encoding != null) {
                    result = readStream(in, encoding);
                } else {
                    result = readStream(in);
                }
                // Closing the input stream will trigger connection release
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return result;
    }

    public static String readStream(InputStream inputStream) throws IOException {
        return readToString(new InputStreamReader(inputStream, Charset.defaultCharset()));
    }

    public static String readStream(InputStream inputStream, String charsetName) throws IOException {
        return readToString(new InputStreamReader(inputStream, charsetName));
    }

    private static String readToString(Reader in) throws IOException {
        BufferedReader reader = new BufferedReader(in);

        StringBuilder sb = new StringBuilder();
        char[] buf = new char[10 * 1024];
        int readed;
        while ((readed = reader.read(buf)) != -1) {
            sb.append(buf, 0, readed);
        }

        return sb.toString();
    }
}
