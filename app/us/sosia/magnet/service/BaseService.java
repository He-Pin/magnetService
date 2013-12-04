package us.sosia.magnet.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import play.Logger;
import play.cache.Cache;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.WS;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import us.sosia.magnet.service.libs.Jsons;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: kerr
 * Mail: hepin@sosia.us
 */
public class BaseService {
    /**
     * return an bad request with request corrupted code
     */

    public static Result requestCorrupted(){
        return Results.badRequest("bad request");
    }

    private static Result serverErrorResult(Throwable throwable){
        return Results.internalServerError(throwable==null?"unKnow error":throwable.toString());
    }

    public static Result OK(byte[] bytes){
        return Results.ok(bytes);
    }

    public static Result OK(InputStream inputStream){
        return Results.ok(inputStream);
    }

    public static Result async(final Promise<Result> resultPromise) {
        Promise<Result> recoveredPromise = resultPromise.recover(new F.Function<Throwable, Result>() {
            @Override
            public Result apply(Throwable throwable) throws Throwable {
                Logger.debug("Error On Async :", throwable);
                throwable.printStackTrace();
                return serverErrorResult(throwable);
            }
        });
        return Results.async(recoveredPromise);
    }

    public static Http.Context context() {
        return Http.Context.current();
    }

    /**
     * Returns the current HTTP session.
     */
    public static Http.Session session() {
        return context().session();
    }

    /**
     * Puts a new value into the current session.
     */
    public static void session(String key, String value) {
        session().put(key, value);
    }

    /**
     * Returns a value from the session.
     */
    public static String session(String key) {
        return session().get(key);
    }

    public static void clearSession() {
        session().clear();
    }

    public static Http.Flash flash() {
        return context().flash();
    }

    /**
     * Puts a new value into the flash scope.
     */
    public static void flash(String key, String value) {
        flash().put(key, value);
    }

    /**
     * Returns a value from the flash scope.
     */
    public static String flash(String key) {
        return flash().get(key);
    }

    public static Http.Request request() {
        return context().request();
    }

    public static Http.Response response() {
        return context().response();
    }

    public static Map<String, String[]> queryString() {
        return request().queryString();
    }


    public static String[] queryString(final String key) {
        return queryString().get(key);
    }

    public static String queryStringFlated(final String key) {
        String[] strings = queryString(key);
        if (strings == null || strings.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string);
        }
        return sb.toString();
    }

    public static void printQueryString() {
        Map<String, String[]> queryMap = queryString();
        for (Map.Entry<String, String[]> entry : queryMap.entrySet()) {
            for (String value : entry.getValue()) {
                System.out.println("key :" + entry.getKey() + "  value :" + value);
            }
        }
    }

    public static Map<String, String[]> headers() {
        return request().headers();
    }

    public static String headerFlated(final String key) {
        String[] strings = header(key);
        if (strings == null || strings.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            sb.append(string);
        }
        return sb.toString();
    }

    public static String[] header(final String key) {
        return headers().get(key);
    }

    public static void printHeaders() {
        Map<String, String[]> headers = headers();
        for (Map.Entry<String, String[]> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                System.out.println("key :" + entry.getKey() + "  value :" + value);
            }
        }
    }

    public static Http.RequestBody requestBody() {
        return request().body();
    }

    //POST GET

    public static <T> Result handlePOST(final POSTHandler<T> handler) {
        Logger.info("\n" +
                "+----------------------------------------------------------------------------------------------+\n" +
                "                                       Dump Request From WebUI                                  \n" +
                "\n" +
                dumpRequest() + "\n" +
                "+----------------------------------------------------------------------------------------------+\n");
        final Form<T> postForm =
                Form.form(handler.clazz()).bindFromRequest();
        if (postForm.hasErrors()) {
            return handler.onRequestError();
        } else {
            final T bean = postForm.get();
            Logger.info("bean : " + bean);
            return handler.OnRequestHandle(bean,postForm.data());
        }
    }

    public static Result handlePOSTJson(final POSTJsonHandler handler) {
        final JsonNode jsonNode = requestBody().asJson();
        if (jsonNode == null) {
            return handler.onRequestError();
        } else {
            Logger.info("post json body :" + jsonNode);
            return handler.OnRequestHandle(jsonNode,null);
        }
    }

    protected static class DefaultPOSTJsonHandler extends DefaultPOSTHandler<JsonNode> implements POSTJsonHandler {
        public DefaultPOSTJsonHandler() {
            super(JsonNode.class);
        }

        public DefaultPOSTJsonHandler bodyHandler(POSTJsonBodyHandler handler) {
            super.bodyHandler(handler);
            return this;
        }

        @Override
        public DefaultPOSTHandler bodyHandler(POSTBodyHandler<JsonNode> handler) {
            super.bodyHandler(handler);
            return this;
        }

        public static interface POSTJsonBodyHandler extends POSTBodyHandler<JsonNode> {
        };
    }

    protected static class DefaultPOSTHandler<T> implements POSTHandler<T> {
        protected AtomicReference<POSTBodyHandler<T>> bodyHandler = new AtomicReference<POSTBodyHandler<T>>(null);
        protected final Class<T> entityClass;
        private boolean canCache = false;
        private String cacheKey = null;
        private int cacheTimeOut = ConverterConstants.CacheTimeOut;

        public DefaultPOSTHandler(Class<T> entityClass) {
            this.entityClass = entityClass;
        }

        public DefaultPOSTHandler bodyHandler(final POSTBodyHandler<T> handler) {
            updateAtomically(handler);
            return this;
        }

        private void updateAtomically(final POSTBodyHandler<T> handler) {
            for (; ; ) {
                if (this.bodyHandler.compareAndSet(this.bodyHandler.get(), handler)) {
                    break;
                }
            }
        }

        public DefaultPOSTHandler canCache(boolean canCache){
            this.canCache = canCache;
            return this;
        }

        public DefaultPOSTHandler cacheKey(final String cacheKey){
            this.cacheKey = cacheKey;
            return this;
        }

        public DefaultPOSTHandler cacheTimeOut(final int cacheTimeOut){
            this.cacheTimeOut = cacheTimeOut;
            return this;
        }

        @Override
        public Class<T> clazz() {
            return entityClass;
        }

        @Override
        public Result onRequestError() {
            return requestCorrupted();
        }

        @Override
        public Result OnRequestHandle(final T bean,final Map<String,String> rawData) {
            if (bodyHandler.get() != null) {
                if (ConverterConstants.CacheEnable && canCache && cacheKey != null){
                    try {
                        return Cache.getOrElse(cacheKeyValue(cacheKey,bean),
                                new Callable<Result>() {
                                    @Override
                                    public Result call() throws Exception {
                                        return bodyHandler.get().handle(bean,rawData);
                                    }
                                },cacheTimeOut);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return serverErrorResult(e);
                    }
                }else {
                    return bodyHandler.get().handle(bean,rawData);
                }
            } else {
                return Results.TODO;
            }
        }

        @Override
        public boolean canCache() {
            return canCache;
        }

        @Override
        public String cacheKey() {
            return cacheKey;
        }

        @Override
        public int cacheTimeOut() {
            return cacheTimeOut;
        }

        public static interface POSTBodyHandler<T> {
            Result handle(final T bean,final Map<String,String> rawData);
        }

    }


    private static interface POSTHandler<T> {
        Class<T> clazz();

        Result onRequestError();

        Result OnRequestHandle(final T bean,final Map<String,String> rawData);

        boolean canCache();

        String cacheKey();

        int cacheTimeOut();
    }

    // companion type
    private static interface POSTJsonHandler extends POSTHandler<JsonNode> {
    }

    ;

    public static Result handleGET(final GETHandler handler) {
        Logger.info("\n" +
                "+----------------------------------------------------------------------------------------------+\n" +
                "                                       Dump Request From WebUI                                  \n" +
                "\n" +
                dumpRequest() + "\n" +
                "+----------------------------------------------------------------------------------------------+\n");
        final DynamicForm form = Form.form().bindFromRequest();
        if (form.hasErrors()) {
            return handler.onRequestError();
        } else {
            final String queryValue = form.get(handler.queryKey());
            Logger.info("queryValue : " + queryValue);
            return handler.onRequestHandle(queryValue,form.data());
        }
    }

    private static String cacheKeyValue(Object ... objects){
        StringBuilder keyBuilder = new StringBuilder();
        for (Object obj : objects){
            if (obj != null){
                keyBuilder.append(obj);
            }
        }
        return keyBuilder.toString();
    }

    protected static class DefaultGETHandler implements GETHandler {
        private final String queryKey;
        private AtomicReference<QueryHandler> queryHandler = new AtomicReference<QueryHandler>(null);
        private boolean canCache = false;
        private String cacheKey = null;
        private int cacheTimeOut = ConverterConstants.CacheTimeOut;

        public DefaultGETHandler(String queryKey) {
            this.queryKey = queryKey;
        }

        @Override
        public Result onRequestError() {
            return requestCorrupted();
        }

        @Override
        public String queryKey() {
            return queryKey;
        }


        public DefaultGETHandler queryHandler(final QueryHandler handler) {
            updateAtomically(handler);
            return this;
        }

        private void updateAtomically(final QueryHandler handler) {
            for (; ; ) {
                if (this.queryHandler.compareAndSet(this.queryHandler.get(), handler)) {
                    break;
                }
            }
        }

        public DefaultGETHandler canCache(boolean canCache){
            this.canCache = canCache;
            return this;
        }

        public DefaultGETHandler cacheKey(final String cacheKey){
            this.cacheKey = cacheKey;
            return this;
        }

        public DefaultGETHandler cacheTimeOut(final int cacheTimeOut){
            this.cacheTimeOut = cacheTimeOut;
            return this;
        }

        /**
         * need override
         */
        @Override
        public Result onRequestHandle(final String queryValue,final Map<String, String> rawData) {
            if (queryHandler.get() != null) {
                if (ConverterConstants.CacheEnable && canCache && cacheKey != null){
                    try {
                        return Cache.getOrElse(cacheKeyValue(cacheKey,queryValue),
                                new Callable<Result>() {
                                    @Override
                                    public Result call() throws Exception {
                                        return queryHandler.get().handle(queryValue,rawData);
                                    }
                                }, cacheTimeOut);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return serverErrorResult(e);
                    }
                }else {
                    return queryHandler.get().handle(queryValue,rawData);
                }
            } else {
                return Results.TODO;
            }
        }

        @Override
        public boolean canCache() {
            return canCache;
        }

        @Override
        public String cacheKey() {
            return cacheKey;
        }

        @Override
        public int cacheTimeOut() {
            return cacheTimeOut;
        }

        public interface QueryHandler {
            Result handle(final String queryValue,final Map<String,String> rawData);
        }
    }


    private static interface GETHandler {
        Result onRequestError();

        String queryKey();

        Result onRequestHandle(final String queryValue,Map<String, String> rawData);

        boolean canCache();

        String cacheKey();

        int cacheTimeOut();
    }

    private static final String LINE = "-------------------------------------------------\n";

    public static String dumpRequest() {
        Http.Request request = request();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("URI :").append(request.uri()).append("\n");
        stringBuilder.append("Method :").append(request.method()).append("\n");
        stringBuilder.append("Version :").append(request.version()).append("\n");
        stringBuilder.append("Host :").append(request.host()).append("\n");
        stringBuilder.append("Client Address :").append(request.remoteAddress()).append("\n");
        stringBuilder.append("Path :").append(request.path()).append("\n");
        stringBuilder.append("Accept Lang :").append(acceptLanguages(request)).append("\n");
        stringBuilder.append("Accept Type :").append(accept(request)).append("\n");
        stringBuilder.append("Headers :").append(headers(request)).append("\n");
        stringBuilder.append("Query String :").append(queryString(request)).append("\n");
        stringBuilder.append("Body :").append(body(request)).append("\n");
        stringBuilder.append("FormUrlEncoded :").append(form(request)).append("\n");
        return stringBuilder.toString();
    }

    private static String acceptLanguages(final Http.Request request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(LINE);
        for (play.i18n.Lang lang : request.acceptLanguages()) {
            stringBuilder.append(lang.language()).append("\n");
        }
        stringBuilder.append(LINE);
        return stringBuilder.toString();
    }

    private static String accept(final Http.Request request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(LINE);
        for (play.api.http.MediaRange mediaRange : request.acceptedTypes()) {
            stringBuilder.append(mediaRange.mediaType()).append(" / ")
                    .append(mediaRange.mediaSubType()).append("\n");
        }
        stringBuilder.append(LINE);
        return stringBuilder.toString();
    }

    public static String headers(final Http.Request request) {
        return mapIt(request.headers());
    }

    public static String queryString(final Http.Request request) {
        return mapIt(request.queryString());
    }

    public static String form(final Http.Request request) {
        return mapIt(request.body().asFormUrlEncoded());
    }

    public static String mapIt(Map<String, String[]> stringMap) {
        if (stringMap == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(LINE);
        for (Map.Entry<String, String[]> entry : stringMap.entrySet()) {
            for (String value : entry.getValue()) {
                stringBuilder.
                        append("key :").append(entry.getKey())
                        .append(" value :").append(value).append("\n");
            }
        }
        stringBuilder.append(LINE);
        return stringBuilder.toString();
    }

    public static String body(final Http.Request request) {
        Http.RequestBody body = request.body();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n").append(LINE);
        stringBuilder.append("\tAs String :").append("\n\t");
        stringBuilder.append(stringDump(body)).append("\n");
        stringBuilder.append("\tAs Bytes Hex Dump:").append("\n\t");
        stringBuilder.append(hexDump(request.body())).append("\n");
        stringBuilder.append("\tAs Text :").append("\n\t");
        stringBuilder.append(body.asText()).append("\n");
        stringBuilder.append("\tAs Json :").append("\n\t");
        stringBuilder.append(body.asJson()).append("\n");
        stringBuilder.append("\tAs Xml :").append("\n\t");
        stringBuilder.append(body.asXml()).append("\n");
        stringBuilder.append(LINE);
        return stringBuilder.toString();
    }

    private static String stringDump(final Http.RequestBody body) {
        if (body.asRaw() == null || body.asRaw().size() == 0) {
            return null;
        }
        return new String(body.asRaw().asBytes());
    }

    private static String hexDump(final Http.RequestBody body) {
        if (body.asRaw() == null || body.asRaw().size() == 0) {
            return null;
        }
        ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(body.asRaw().asBytes());
        return ChannelBuffers.hexDump(channelBuffer);
    }

    protected static List<File> formFile(){
        final Http.RequestBody requestBody = requestBody();
        if (requestBody == null){
            return null;
        }
        final Http.MultipartFormData formData = requestBody.asMultipartFormData();
        if (formData == null){
            return null;
        }
        final List<Http.MultipartFormData.FilePart> fileParts = formData.getFiles();
        if (fileParts == null){
            return null;
        }
        List<File> files = new ArrayList<File>(fileParts.size());
        for (Http.MultipartFormData.FilePart filePart : fileParts){
            if (filePart != null){
                File file = filePart.getFile();
                if (file != null){
                    files.add(file);
                }
            }
        }
        return files;
    }

    protected static List<Http.MultipartFormData.FilePart> formFilePart(){
        final Http.RequestBody requestBody = requestBody();
        if (requestBody == null){
            return null;
        }
        final Http.MultipartFormData formData = requestBody.asMultipartFormData();
        if (formData == null){
            return null;
        }
        final List<Http.MultipartFormData.FilePart> fileParts = formData.getFiles();
        if (fileParts == null){
            return null;
        }
        return fileParts;
    }

    protected static Http.MultipartFormData.FilePart formFilePart(String fileName){
        final Http.RequestBody requestBody = requestBody();
        if (requestBody == null){
            return null;
        }
        final Http.MultipartFormData formData = requestBody.asMultipartFormData();
        if (formData == null){
            return null;
        }
        final Http.MultipartFormData.FilePart fileParts = formData.getFile(fileName);
        if (fileParts == null){
            return null;
        }
        return fileParts;
    }

    protected static File rawFile(){
        final Http.RequestBody requestBody = requestBody();
        if (requestBody == null){
            return null;
        }
        final Http.RawBuffer buffer = requestBody.asRaw();
        if (buffer == null){
            return null;
        }
        return buffer.asFile();
    }

    protected static byte[] rawBytes(){
        final Http.RequestBody requestBody = requestBody();
        if (requestBody == null){
            return null;
        }
        final Http.RawBuffer buffer = requestBody.asRaw();
        if (buffer == null){
            System.out.println("buffer null");
            return null;
        }else {
            System.out.println(buffer.size());
        }
        return buffer.asBytes();
    }

    public static <T> T toBean(final WS.Response response,final Class<T> clazz){
        return Jsons.toBean(response,clazz);
    }

    public static <T> T toBean(final String jsonStr,final Class<T> clazz){
        return Jsons.toBean(jsonStr,clazz);
    }

    public static <T> T toBean(final JsonNode jsonNode,final Class<T> clazz){
        return Jsons.toBean(jsonNode,clazz);
    }

    public static JsonNode toJsonNode(final WS.Response response){
        return Jsons.toJsonNode(response);
    }

    public static JsonNode toJsonNode(final String jsonStr){
        return Jsons.toJsonNode(jsonStr);
    }

    public static JsonNode toJsonNode(final Object object){
        return Jsons.toJsonNode(object);
    }

    public static String toJsonString(final Object object){
        return Jsons.toJsonString(object);
    }

}
