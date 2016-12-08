package org.amb1ent.demo.fm;

import io.corefabric.pi.appweb.DocApiCall;
import io.corefabric.pi.appweb.IDocApiProvider;
import io.corefabric.pi.appweb.UIDocApiWorkerVerticle;
import io.corefabric.pi.appweb.providers.HomeProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.kritikal.fabric.CoreFabric;
import org.kritikal.fabric.core.VertxHelpers;
import org.kritikal.fabric.core.exceptions.FabricError;
import org.kritikal.fabric.daemon.MqttBrokerVerticle;
import org.kritikal.fabric.net.mqtt.IMqttBroker;
import org.kritikal.fabric.net.mqtt.MqttBroker;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by ben on 08/12/2016.
 */
public class HomepageProvider implements IDocApiProvider {
    private final static Logger logger = LoggerFactory.getLogger(HomepageProvider.class);
    public final static String PATH = "/home";

    private DocApiCall __init(UIDocApiWorkerVerticle.Context context) {
        DocApiCall call = new DocApiCall(context, context.cfg.instancekey + PATH);
        final String dataTopic = "|model/teams";
        final String dataTeamTopicPrefix = "|model/team/";
        final IMqttBroker broker = MqttBrokerVerticle.mqttBroker();
        JsonObject mqttMessage = broker.apiPeek(dataTopic);
        String content = null;
        if (mqttMessage != null && mqttMessage.containsKey("body")) {
            try {
                content = new String(mqttMessage.getBinary("body"), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                logger.fatal("", uee);
                content = "[]";
            }
        }
        else {
            content = "[]";
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
                broker.apiPublish(topic, team.encode().getBytes("UTF-8"), 0, false);
            } catch (UnsupportedEncodingException uee) {
                logger.fatal("", uee);
            }
        }
        try {
            broker.apiPublish(dataTopic, teams.encode().getBytes("UTF-8"), 0, false);
        } catch (UnsupportedEncodingException uee) {
            logger.fatal("", uee);
        }
        VertxHelpers.compute(call.o, teams, (t) -> {
            call.o.put("teams", t);
            return t;
        });
        return call;
    }

    private DocApiCall __reinit(UIDocApiWorkerVerticle.Context context) {
        DocApiCall call = new DocApiCall(context, context.cfg.instancekey + PATH, context.apicall.getJsonObject("ad").getJsonObject("data"));
        return call;
    }

    private void __complete(DocApiCall call) {
        // TODO: flesh out data structure
        // if changed:
        //if (!currentCall.getOrDefault(call.context.cfg.instancekey, call.uuid).equals(call.uuid)) return;
        //call.publish();
    }

    @Override
    public void open_singleton(UIDocApiWorkerVerticle.Context context) {
        DocApiCall call = __init(context);

        call.publish();
        call.reply();

        __complete(call);
    }

    @Override
    public void open(UIDocApiWorkerVerticle.Context context) {
        throw new FabricError();
    }

    @Override
    public void list(UIDocApiWorkerVerticle.Context context) {
        throw new FabricError();
    }

    @Override
    public void upsert(UIDocApiWorkerVerticle.Context context) { throw new FabricError(); }
}