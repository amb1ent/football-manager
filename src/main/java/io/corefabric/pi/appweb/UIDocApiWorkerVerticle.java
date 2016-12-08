package io.corefabric.pi.appweb;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.amb1ent.demo.fm.HomepageProvider;
import org.kritikal.fabric.core.VERTXDEFINES;
import org.kritikal.fabric.core.Configuration;
import org.kritikal.fabric.core.ConfigurationManager;
import io.corefabric.pi.appweb.providers.DtnConfigProvider;
import org.kritikal.fabric.db.pgsql.ConnectionInformation;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by ben on 03/11/2016.
 */
public class UIDocApiWorkerVerticle extends org.kritikal.fabric.db.pgsql.DWWorkerVerticle {

    public static class Context {
        public Context(Message<JsonObject> message, Configuration cfg, ConnectionInformation ci, JsonObject apicall, String corefabric, IDocApiHost host)
        {
            this.message = message;
            this.cfg = cfg;
            this.ci = ci;
            this.apicall = apicall;
            this.corefabric = corefabric;
            this.host = host;
        }

        public final Message<JsonObject> message;
        public final Configuration cfg;
        public final ConnectionInformation ci;
        public final JsonObject apicall;
        public final String corefabric;
        public final IDocApiHost host;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        final String instancekey = message.body().getString("instancekey");
        final JsonObject apicall = message.body().getJsonObject("payload");
        final String corefabric = message.body().getString("corefabric");
        final boolean rest = message.body().containsKey("rest");
        ConfigurationManager.getConfigurationAsync(vertx, instancekey, cfg -> {
            try {
                final ConnectionInformation ci = connect(cfg);
                try {
                    final IDocApiHost host = rest ? new OneShotResultsHost() : new StreamingResultsHost();
                    handle(host, message, cfg, ci, apicall, corefabric);
                }
                finally {
                    release(ci);
                }
            }
            catch (Throwable t)
            {
                logger.warn(cfg.instancekey + "\tui-docapi", t);
                JsonObject response = new JsonObject();
                response.put("success", false);
                response.put("error", "FABRIC-UI-00001");
                response.put("exception_type", t.getClass().getName());
                response.put("exception", t.getMessage());
                message.reply(response, VERTXDEFINES.DELIVERY_OPTIONS);
            }
        });
    }

    void handle(final IDocApiHost host, Message<JsonObject> message, Configuration cfg, ConnectionInformation ci, JsonObject apicall, String corefabric) throws Throwable {
        final Context context = new Context(message, cfg, ci, apicall, corefabric, host);
        IDocApiProvider provider = null;
        switch (apicall.getString("path")) {
            // /home (or /)
            //case HomeProvider.PATH:
            //    provider = new HomeProvider();
            //    break;
            case HomepageProvider.PATH:
                provider = new HomepageProvider();
                break;
            case DtnConfigProvider.PATH:
                provider = new DtnConfigProvider();
                break;
        }
        if (provider == null) {
            {
                JsonObject response = new JsonObject();
                response.put("success", false);
                response.put("error", "FABRIC-UI-00002");
                message.reply(response, VERTXDEFINES.DELIVERY_OPTIONS);
            }
            return;
        }
        try {
            DocApiCall call = (DocApiCall) provider.getClass().getDeclaredMethod(apicall.getString("method"), Context.class).invoke(provider, context);
            call.finish();
        }
        catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

}
