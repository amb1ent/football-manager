package io.corefabric.pi.appweb;

import io.corefabric.pi.appweb.DocApiCall;

/**
 * Created by ben on 08/12/2016.
 */
public interface IDocApiHost {
    void publish(DocApiCall call);
    void reply(DocApiCall call);
    void finish(DocApiCall call);
}
