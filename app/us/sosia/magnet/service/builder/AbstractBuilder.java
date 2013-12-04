package us.sosia.magnet.service.builder;


import com.fasterxml.jackson.databind.JsonNode;
import com.ning.http.client.*;
import com.ning.http.multipart.FilePart;
import play.Logger;
import play.libs.F;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.WS.Response;
import play.libs.WS.WSRequest;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Results.AsyncResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */

/**
 * Custom request need extents this class to implement the request
 */
public abstract class AbstractBuilder implements Builder {
    protected static final int INVALIDATE_VALUE = -1;
    private static final PerRequestConfig config;
    static {
        config = new PerRequestConfig();
        config.setRequestTimeoutInMs(60000);
    }

    private final WSRequest builder;
    private AtomicBoolean validate = new AtomicBoolean(false);


    protected AbstractBuilder(final String method) {
        this.builder = new WSRequest(method);
    }

    public AbstractBuilder setURl(final String url) {
        builder.setUrl(url);
        return this;
    }

    public AbstractBuilder setHeader(final String key, final String value) {
        if (value == null){
            return this;
        }
        builder.setHeader(key, value);
        return this;
    }

    public AbstractBuilder addHeader(final String key, final String value) {
        if (value == null){
            return this;
        }
        builder.addHeader(key, value);
        return this;
    }

    public AbstractBuilder addParameter(final String key, final String value) {
        if (value == null){
            return this;
        }
        builder.addParameter(key, value);
        return this;
    }

    public AbstractBuilder addQueryParameter(final String key, final String value) {
        if (value == null){
            return this;
        }
        builder.addQueryParameter(key, value);
        return this;
    }

    public AbstractBuilder setBody(final String body) {
        builder.setBody(body);
        return this;
    }

    public AbstractBuilder setBody(final byte[] data){
        builder.setBody(data);
        return this;
    }

    public AbstractBuilder setBody(final File file){
        builder.setBody(file);
        return this;
    }

    public AbstractBuilder setBody(final BodyGenerator bodyGenerator){
        builder.setBody(bodyGenerator);
        return this;
    }

    public AbstractBuilder addBody(final String filename,final File file){
        final  Part bodyPart;
        try {
            bodyPart = new FilePart(filename,file);
            builder.addBodyPart(bodyPart);
            return this;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return this;
        }
    }

    public AbstractBuilder authorization(final String token) {
        builder.addHeader("Authorization", token);
        return this;
    }


    @Override
    public Builder build() {
        Logger.info("\n"+
                "+----------------------------------------------------------------------------------------------+\n"+
                "                                       Request Builder Data                                      \n"+
                "\n"+
                this+"\n"+
                "+----------------------------------------------------------------------------------------------+\n");

        if (!validated()) {
            throw new IllegalStateException("is not validated,need more stable state");
        }
        preBuild();
        updateValidateAtomically(true);
        return this;
    }

    abstract protected void preBuild();

    protected boolean validated() {
        return false;
    }

    private boolean updateValidateAtomically(boolean newValue) {
        for (; ; ) {
            boolean current = validate.get();
            if (validate.compareAndSet(current, newValue))
                return current;
        }
    }

    private static final F.Callback<Response> loggerCallback = new F.Callback<Response>() {
        @Override
        public void invoke(Response response) throws Throwable {
            Logger.info("\n"+
                        "+----------------------------------------------------------------------------------------------+\n"+
                        "                                         Server Response                                      \n"+
                        "\n"+
                        response.getBody()+"\n"+
                        "+----------------------------------------------------------------------------------------------+\n");
        }
    };

    @Override
    public Promise<Response> execute() {
        if (!validate.get()) {
            throw new IllegalStateException("must call build first");
        }
        builder.setPerRequestConfig(config);
        builder.setFollowRedirects(true);
//        here we need recover the result to an response,but for now,
//        I can't find an better way to make a response
        Promise<Response> result = builder.execute();
        Request request = builder.build();
        Logger.info("\n" +
                "+----------------------------------------------------------------------------------------------+\n" +
                "                                           Call   Server                                      \n" +
                "\n" +
                "Method :"+ request.getMethod() + "\n" +
                "URL    :"+request.getUrl() + "\n" +
                "Header :\n" + request.getHeaders() + "\n" +
                "Params :\n" + request.getParams() + "\n" +
                "+----------------------------------------------------------------------------------------------+\n");
        //result.onRedeem(loggerCallback);
        return result;
    }

    private static AsyncHttpClient client() {
        return play.api.libs.ws.WS.client();
    }

    public Promise<Response> execute(final ProgressHandler handler) {
        final scala.concurrent.Promise<Response> scalaPromise = scala.concurrent.Promise$.MODULE$.<Response>apply();
        try {
            client().executeRequest(builder.build(), new AsyncCompletionHandler<com.ning.http.client.Response>() {
                @Override
                public com.ning.http.client.Response onCompleted(com.ning.http.client.Response response) {
                    final com.ning.http.client.Response ahcResponse = response;
                    scalaPromise.success(new Response(ahcResponse));
                    if (handler != null){
                        handler.onCompleted();
                    }
                    return response;
                }
                @Override
                public void onThrowable(Throwable t) {
                    scalaPromise.failure(t);
                }

                @Override
                public STATE onHeaderWriteCompleted() {
                    if (handler != null){
                        handler.onHeaderWriteCompleted();
                    }
                    return super.onHeaderWriteCompleted();
                }

                @Override
                public STATE onContentWriteCompleted() {
                    if (handler != null){
                        handler.onContentWriteCompleted();
                    }
                    return super.onContentWriteCompleted();
                }

                @Override
                public STATE onContentWriteProgress(long amount, long current, long total) {
                    if (handler != null){
                        handler.onContentWriteProgress(amount,current,total);
                    }
                    return super.onContentWriteProgress(amount, current, total);
                }
            });
        } catch (IOException exception) {
            scalaPromise.failure(exception);
        }
        return new Promise<Response>(scalaPromise.future());
    }

    public static interface ProgressHandler{
        void onHeaderWriteCompleted();
        void onContentWriteCompleted();
        void onContentWriteProgress(long amount, long current, long total);
        void onCompleted();
    }

    @Override
    public Response executeAndGet() {
        return execute().get();
    }

    @Override
    public String executeAndGetBodyAsString() {
        Logger.info("\n"+
                "+-----------------------------------------------------------------------------------------------+\n"+
                "                                            Call   Server                                      \n"+
                "\n"+
                builder.getMethod()+"\n" + builder.getUrl() + "\n"+
                "+-----------------------------------------------------------------------------------------------+\n");
        final String responseBody = executeAndGet().getBody();
        return responseBody;
    }

    @Override
    public JsonNode executeAndGetBodyAsJsonNode() {
        return executeAndGet().asJson();
    }

    @Override
    public AsyncResult executeAndAsyncHandle(final Function<Response, Result> function) {
        return Results.async(execute().map(function));
    }


}
