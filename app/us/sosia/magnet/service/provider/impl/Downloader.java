package us.sosia.magnet.service.provider.impl;

import play.libs.F;
import play.libs.WS;
import us.sosia.magnet.service.builder.AbstractBuilder;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public class Downloader extends AbstractBuilder {
    private String url;

    private Downloader(String url) {
        super("GET");
        this.url = url;
    }

    public static final Downloader url(String url){
        return new Downloader(url);
    }

    public final F.Promise<byte[]> download(){
        return this.build().execute().map(new F.Function<WS.Response, byte[]>() {
            @Override
            public byte[] apply(WS.Response response) throws Throwable {
                return response.asByteArray();
            }
        });
    }

    @Override
    protected void preBuild() {
        this.setURl(url);
    }

    @Override
    protected boolean validated() {
        return url != null;
    }
}
