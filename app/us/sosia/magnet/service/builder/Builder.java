package us.sosia.magnet.service.builder;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.WS.Response;
import play.mvc.Result;
import play.mvc.Results.AsyncResult;

/**
 * Author: kerr
 * Mail: pin.he@pekall.com
 */

public interface Builder {
    /**
     * build the request
     */
    Builder build();

    /**
     * execute the request and get an async response
     */
    Promise<Response> execute();

    Response executeAndGet();

    String executeAndGetBodyAsString();

    JsonNode executeAndGetBodyAsJsonNode();

    public AsyncResult executeAndAsyncHandle(final Function<Response, Result> function);
}
