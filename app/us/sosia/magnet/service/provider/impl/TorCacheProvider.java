package us.sosia.magnet.service.provider.impl;

import us.sosia.magnet.service.provider.ConverterWebConstants;
import us.sosia.magnet.service.provider.Priority;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public class TorCacheProvider extends AbstractProvider {
    @Override
    public String downloadUrl(String magnet) {
        if (magnet == null){
            return null;
        }
        String hash = hash(magnet);
        return ConverterWebConstants.TORCACHE + hash + ".torrent";
    }

    @Override
    protected String hash(String magnet) {
        try{
            int hashStart = magnet.indexOf("btih:")+5;
            return magnet.substring(hashStart);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Priority priority() {
        return Priority.NORMAL;
    }
}
