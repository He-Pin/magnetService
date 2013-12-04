package us.sosia.magnet.service;

import play.mvc.Result;
import play.mvc.Results;
import us.sosia.magnet.service.provider.ConverterManager;

import java.util.Map;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public class ConverterService extends BaseService {

    public static Result convertGET(){
        return handleGET(new DefaultGETHandler("q")
                .queryHandler(new DefaultGETHandler.QueryHandler() {
                    @Override
                    public Result handle(final String queryValue,final Map<String, String> rawData) {
                        final String downloadUrl =
                                ConverterManager.manager().provider().downloadUrl(queryValue);
                        return Results.redirect(downloadUrl);
                    }
                }));
    }

    public static Result convertPOST(){
        return Results.TODO;
    }

}
