package org.amb1ent.demo.fm.api;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kritikal.fabric.core.exceptions.FabricError;
import org.kritikal.platform.CoreFabricUnitTest;
import org.kritikal.platform.MyVertxUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ben on 08/12/2016.
 */
@RunWith(MyVertxUnitRunner.class)
public class API000_Test extends CoreFabricUnitTest {

    final static Logger logger = LoggerFactory.getLogger(API000_Test.class);

    private static String get(String u)
    {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(u);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setReadTimeout(5 * 1000);

                if (conn.getResponseCode() != 200) {
                    throw new IOException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));

                String output;
                while ((output = br.readLine()) != null) {
                    content.append(output);
                }

            } finally {
                conn.disconnect();
            }
        } catch (MalformedURLException e) {
            logger.error("", e);
            return null;
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }

        return content.toString();
    }

    @Test
    public void api000_CheckPing(TestContext context) {
        Vertx vertx = rule.vertx();

        final String data = get("http://localhost:1080/api/json/status");
        Assert.assertNotNull(data);
        JsonObject jsonObject = new JsonObject(data);
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.containsKey("up"));
        Assert.assertTrue(jsonObject.getBoolean("up"));
    }

    @Test
    public void api001_CheckHomepageGet(TestContext context) {
        Vertx vertx = rule.vertx();

        final String data = get("http://localhost:1080/api/rest/home");
        Assert.assertNotNull(data);
        JsonObject jsonObject = new JsonObject(data);
        Assert.assertNotNull(jsonObject);
        Assert.assertTrue(jsonObject.containsKey("o"));
        JsonObject o = jsonObject.getJsonObject("o");
        Assert.assertTrue(o.containsKey("teams"));
        JsonArray teams = o.getJsonArray("teams");
        Assert.assertTrue(teams.size() > 0);
    }
}
