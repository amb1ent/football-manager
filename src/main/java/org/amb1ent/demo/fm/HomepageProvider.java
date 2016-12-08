package org.amb1ent.demo.fm;

import io.corefabric.pi.AppWebServerVerticle;
import io.corefabric.pi.appweb.DocApiCall;
import io.corefabric.pi.appweb.IDocApiProvider;
import io.corefabric.pi.appweb.UIDocApiWorkerVerticle;
import io.corefabric.pi.appweb.providers.HomeProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import org.kritikal.fabric.CoreFabric;
import org.kritikal.fabric.core.VERTXDEFINES;
import org.kritikal.fabric.core.VertxHelpers;
import org.kritikal.fabric.core.exceptions.FabricError;
import org.kritikal.fabric.daemon.MqttBrokerVerticle;
import org.kritikal.fabric.net.http.CorsOptionsHandler;
import org.kritikal.fabric.net.mqtt.IMqttBroker;
import org.kritikal.fabric.net.mqtt.MqttBroker;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by ben on 08/12/2016.
 */
public class HomepageProvider implements IDocApiProvider {

    public static void addRoutes(final Vertx vertx, final Router router, final String prefix, final CorsOptionsHandler corsOptionsHandler) {
        router.options(prefix + PATH).handler(corsOptionsHandler);
        router.get(prefix + PATH).handler(rc -> {
            final HttpServerRequest req = rc.request();
            final String instancekey = "demo"; //FIXME
            final String corefabric = AppWebServerVerticle.cookieCutter(req);
            final JsonObject apicall = new JsonObject();
            apicall.put("path", PATH);
            apicall.put("method", "open_singleton");
            apicall.put("args", new JsonArray());

            JsonObject o = new JsonObject();
            o.put("instancekey", instancekey);
            o.put("payload", apicall);
            o.put("corefabric", corefabric);
            o.put("rest", true);

            vertx.eventBus().send("ui.docapi", o, VERTXDEFINES.DELIVERY_OPTIONS, new Handler<AsyncResult<Message<JsonObject>>>() {
                @Override
                public void handle(AsyncResult<Message<JsonObject>> asyncResult) {
                    if (asyncResult.succeeded()) {
                        final JsonObject event = asyncResult.result().body();
                        req.response().headers().add("Access-Control-Allow-Credentials", "true");
                        String optionsOrigin = req.headers().get("Origin");
                        if (optionsOrigin == null) optionsOrigin = "*";
                        req.response().headers().add("Access-Control-Allow-Origin", optionsOrigin);
                        req.response().headers().add("Content-Type", "application/json; charset=UTF-8");
                        req.response().end(event.encode());
                    } else {
                        req.response().setStatusCode(500).end();
                    }
                }
            });

        });
    }

    private final static Logger logger = LoggerFactory.getLogger(HomepageProvider.class);
    public final static String PATH = "/home";

    private DocApiCall __init(UIDocApiWorkerVerticle.Context context) {
        DocApiCall call = new DocApiCall(context, context.cfg.instancekey + PATH);
        __prepare(call);
        return call;
    }

    final static String dataTopic = "|model/teams";
    final static String dataTeamTopicPrefix = "|model/team/";
    final static IMqttBroker broker = MqttBrokerVerticle.mqttBroker();

    private void __prepare(DocApiCall call) {
        JsonObject mqttMessage = broker.apiPeek(dataTopic);
        String content = "[]";
        if (mqttMessage != null) {
            if (mqttMessage.containsKey("body")) {
                try {
                    content = new String(mqttMessage.getBinary("body"), "UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    logger.fatal("", uee);
                    content = "[]";
                }
            }
        }
        JsonArray teams = new JsonArray(content);
        if (teams.size() == 0) {
            JsonObject exampleTeam = new JsonObject();
            exampleTeam.put("name", "Dream Team");
            exampleTeam.put("city", "Atlantis");
            exampleTeam.put("owner", "Santa Claus");
            exampleTeam.put("stadium_capacity", 50000);
            exampleTeam.put("competition", "Fantasy Football League");
            exampleTeam.put("n_players", 25);
            exampleTeam.put("date_created", "2017/01/01");
            teams.add(exampleTeam);
        }
        for (int i = 0, l = teams.size(); i < l; ++i) {
            final JsonObject team = teams.getJsonObject(i);
            if (!team.containsKey("id")) {
                team.put("id", UUID.randomUUID().toString());
            }
            final String topic = dataTeamTopicPrefix + team.getString("id");
            try {
                broker.apiPublish(topic, team.encode().getBytes("UTF-8"), 0, true);
            } catch (UnsupportedEncodingException uee) {
                logger.fatal("", uee);
            }
        }
        try {
            broker.apiPublish(dataTopic, teams.encode().getBytes("UTF-8"), 0, true);
        } catch (UnsupportedEncodingException uee) {
            logger.fatal("", uee);
        }
        VertxHelpers.compute(call.o, teams, (t) -> {
            call.o.put("teams", t);
            return t;
        });
    }

    private DocApiCall __reinit(UIDocApiWorkerVerticle.Context context) {
        DocApiCall call = new DocApiCall(context, context.cfg.instancekey + PATH, context.apicall.getJsonObject("ad").getJsonObject("data"));
        __prepare(call);
        return call;
    }

    private void __complete(DocApiCall call) {

        JsonArray teams = call.o.getJsonArray("teams");
        for (int i = 0, l = teams.size(); i < l; ++i) {
            final JsonObject team = teams.getJsonObject(i);
            if (!team.containsKey("id")) {
                team.put("id", UUID.randomUUID().toString());
            }
            final String topic = dataTeamTopicPrefix + team.getString("id");
            try {
                broker.apiPublish(topic, team.encode().getBytes("UTF-8"), 0, true);
            } catch (UnsupportedEncodingException uee) {
                logger.fatal("", uee);
            }
        }
        try {
            broker.apiPublish(dataTopic, teams.encode().getBytes("UTF-8"), 0, true);
        } catch (UnsupportedEncodingException uee) {
            logger.fatal("", uee);
        }

        // if changed:
        if (call.hasExited()) return;
        call.publish();
    }

    @Override
    public DocApiCall open_singleton(UIDocApiWorkerVerticle.Context context) {
        DocApiCall call = __init(context);

        call.publish();
        call.reply();

        __complete(call);

        return call;
    }

    @Override
    public DocApiCall open(UIDocApiWorkerVerticle.Context context) {
        throw new FabricError();
    }

    @Override
    public DocApiCall list(UIDocApiWorkerVerticle.Context context) {
        throw new FabricError();
    }

    @Override
    public DocApiCall upsert(UIDocApiWorkerVerticle.Context context) { throw new FabricError(); }

    public DocApiCall add_team(UIDocApiWorkerVerticle.Context context) {
        DocApiCall call = __reinit(context);

        call.publish();
        call.reply();

        final JsonArray teams = call.o.getJsonArray("teams");
        final JsonArray args = context.apicall.getJsonArray("args");
        if (args != null && args.size() > 0) {
            teams.add(args.getJsonObject(0));
        }

        __complete(call);

        return call;
    }
}