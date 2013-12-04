package us.sosia.magnet.service.provider.impl;

import play.libs.F;
import us.sosia.magnet.service.provider.ConverterProvider;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public abstract class AbstractProvider implements ConverterProvider {
    @Override
    public F.Promise<byte[]> convert(String magnet) {
        return Downloader.url(downloadUrl(magnet)).download();
    }

    protected abstract String hash(final String magnet);
}
