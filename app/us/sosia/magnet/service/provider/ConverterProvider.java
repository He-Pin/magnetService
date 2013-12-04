package us.sosia.magnet.service.provider;

import play.libs.F;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public interface ConverterProvider {
    //not implement ok
    F.Promise<byte[]> convert(final String magnet);

    String downloadUrl(final String magnet);

    Priority priority();
}
