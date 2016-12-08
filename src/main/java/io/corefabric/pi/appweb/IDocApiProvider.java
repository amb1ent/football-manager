package io.corefabric.pi.appweb;

/**
 * Created by ben on 03/11/2016.
 */
/**
 * Created by ben on 7/6/16.
 */
public interface IDocApiProvider {

    DocApiCall open_singleton(UIDocApiWorkerVerticle.Context context);

    DocApiCall open(UIDocApiWorkerVerticle.Context context);

    DocApiCall list(UIDocApiWorkerVerticle.Context context);

    DocApiCall upsert(UIDocApiWorkerVerticle.Context context);

}
