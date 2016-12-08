package io.corefabric.pi.appweb;

import io.corefabric.pi.appweb.IDocApiHost;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.kritikal.fabric.core.VERTXDEFINES;
import org.kritikal.fabric.core.VertxHelpers;
import org.kritikal.fabric.daemon.MqttBrokerVerticle;

import java.io.UnsupportedEncodingException;

/**
 * Created by ben on 08/12/2016.
 */
public class OneShotResultsHost implements IDocApiHost {
    final static Logger logger = LoggerFactory.getLogger(OneShotResultsHost.class);

    @Override
    public void publish(DocApiCall call) {
        try {
            MqttBrokerVerticle.mqttBroker().apiPublish(call.topic, VertxHelpers.toString(call.o, call.o).getBytes("UTF-8"), 2, true);
        } catch (UnsupportedEncodingException ue) {
            logger.warn(ue);
        }
    }

    @Override
    public void reply(DocApiCall call) {
        // does nothing
    }

    @Override
    public void finish(DocApiCall call) {
        JsonObject reply = new JsonObject();
        reply.put("success", true);
        reply.put("topic", call.topic);
        reply.put("statusTopic", call.statusTopic);
        reply.put("o", call.o);
        call.context.message.reply(reply, VERTXDEFINES.DELIVERY_OPTIONS);
    }
}
