package org.mockserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Multimap;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.SET_COOKIE;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpStatusCode.NOT_FOUND_404;
import static org.mockserver.model.HttpStatusCode.OK_200;

/**
 * @author jamesdbloom
 */
@SuppressWarnings("rawtypes")
public class HttpResponse extends Action<HttpResponse> implements HttpMessage<HttpResponse, BodyWithContentType> {
    private int hashCode;
    private Integer statusCode;
    private String reasonPhrase;
    private BodyWithContentType body;
    private Headers headers;
    private Cookies cookies;
    private ConnectionOptions connectionOptions;

    /**
     * Static builder to create a response.
     */
    public static HttpResponse response() {
        return new HttpResponse();
    }

    /**
     * Static builder to create a response with a 200 status code and the string response body.
     *
     * @param body a string
     */
    public static HttpResponse response(String body) {
        return new HttpResponse().withStatusCode(OK_200.code()).withReasonPhrase(OK_200.reasonPhrase()).withBody(body);
    }

    /**
     * Static builder to create a not found response.
     */
    public static HttpResponse notFoundResponse() {
        return new HttpResponse().withStatusCode(NOT_FOUND_404.code()).withReasonPhrase(NOT_FOUND_404.reasonPhrase());
    }

    /**
     * The status code to return, such as 200, 404, the status code specified
     * here will result in the default status message for this status code for
     * example for 200 the status message "OK" is used
     *
     * @param statusCode an integer such as 200 or 404
     */
    public HttpResponse withStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        this.hashCode = 0;
        return this;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * The reason phrase to return, if no reason code is returned this will
     * be defaulted to the standard reason phrase for the statusCode,
     * i.e. for a statusCode of 200 the standard reason phrase is "OK"
     *
     * @param reasonPhrase an string such as "Not Found" or "OK"
     */
    public HttpResponse withReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
        this.hashCode = 0;
        return this;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Set response body to return as a string response body. The character set will be determined by the Content-Type header
     * on the response. To force the character set, use {@link #withBody(String, Charset)}.
     *
     * @param body a string
     */
    public HttpResponse withBody(String body) {
        if (body != null) {
            this.body = new StringBody(body);
            this.hashCode = 0;
        }
        return this;
    }

    /**
     * Set response body to return a string response body with the specified encoding. <b>Note:</b> The character set of the
     * response will be forced to the specified charset, even if the Content-Type header specifies otherwise.
     *
     * @param body    a string
     * @param charset character set the string will be encoded in
     */
    public HttpResponse withBody(String body, Charset charset) {
        if (body != null) {
            this.body = new StringBody(body, charset);
            this.hashCode = 0;
        }
        return this;
    }

    /**
     * Set response body to return a string response body with the specified encoding. <b>Note:</b> The character set of the
     * response will be forced to the specified charset, even if the Content-Type header specifies otherwise.
     *
     * @param body        a string
     * @param contentType media type, if charset is included this will be used for encoding string
     */
    public HttpResponse withBody(String body, MediaType contentType) {
        if (body != null) {
            this.body = new StringBody(body, contentType);
            this.hashCode = 0;
        }
        return this;
    }

    /**
     * Set response body to return as binary such as a pdf or image
     *
     * @param body a byte array
     */
    public HttpResponse withBody(byte[] body) {
        this.body = new BinaryBody(body);
        this.hashCode = 0;
        return this;
    }

    /**
     * Set the body to return for example:
     * <p/>
     * string body:
     * - exact("<html><head/><body><div>a simple string body</div></body></html>");
     * <p/>
     * or
     * <p/>
     * - new StringBody("<html><head/><body><div>a simple string body</div></body></html>")
     * <p/>
     * binary body:
     * - binary(IOUtils.readFully(getClass().getClassLoader().getResourceAsStream("example.pdf"), 1024));
     * <p/>
     * or
     * <p/>
     * - new BinaryBody(IOUtils.readFully(getClass().getClassLoader().getResourceAsStream("example.pdf"), 1024));
     *
     * @param body an instance of one of the Body subclasses including StringBody or BinaryBody
     */
    public HttpResponse withBody(BodyWithContentType body) {
        this.body = body;
        this.hashCode = 0;
        return this;
    }

    public BodyWithContentType getBody() {
        return body;
    }

    @JsonIgnore
    public byte[] getBodyAsRawBytes() {
        return this.body != null ? this.body.getRawBytes() : new byte[0];
    }

    @JsonIgnore
    public String getBodyAsString() {
        if (body != null) {
            return body.toString();
        } else {
            return null;
        }
    }

    public Headers getHeaders() {
        return this.headers;
    }

    private Headers getOrCreateHeaders() {
        if (this.headers == null) {
            this.headers = new Headers();
            this.hashCode = 0;
        }
        return this.headers;
    }

    public HttpResponse withHeaders(Headers headers) {
        if (headers == null || headers.isEmpty()) {
            this.headers = null;
        } else {
            this.headers = headers;
        }
        this.hashCode = 0;
        return this;
    }

    /**
     * The headers to return as a list of Header objects
     *
     * @param headers a list of Header objects
     */
    public HttpResponse withHeaders(List<Header> headers) {
        getOrCreateHeaders().withEntries(headers);
        this.hashCode = 0;
        return this;
    }

    /**
     * The headers to return as a varargs of Header objects
     *
     * @param headers varargs of Header objects
     */
    public HttpResponse withHeaders(Header... headers) {
        getOrCreateHeaders().withEntries(headers);
        this.hashCode = 0;
        return this;
    }

    /**
     * Add a header to return as a Header object, if a header with
     * the same name already exists this will NOT be modified but
     * two headers will exist
     *
     * @param header a Header object
     */
    public HttpResponse withHeader(Header header) {
        getOrCreateHeaders().withEntry(header);
        this.hashCode = 0;
        return this;
    }

    /**
     * Add a header to return as a Header object, if a header with
     * the same name already exists this will NOT be modified but
     * two headers will exist
     *
     * @param name   the header name
     * @param values the header values
     */
    public HttpResponse withHeader(String name, String... values) {
        getOrCreateHeaders().withEntry(name, values);
        this.hashCode = 0;
        return this;
    }

    /**
     * Add a header to return as a Header object, if a header with
     * the same name already exists this will NOT be modified but
     * two headers will exist
     *
     * @param name   the header name as a NottableString
     * @param values the header values which can be a varags of NottableStrings
     */
    public HttpResponse withHeader(NottableString name, NottableString... values) {
        getOrCreateHeaders().withEntry(header(name, values));
        this.hashCode = 0;
        return this;
    }

    public HttpResponse withContentType(MediaType mediaType) {
        getOrCreateHeaders().withEntry(header(CONTENT_TYPE.toString(), mediaType.toString()));
        this.hashCode = 0;
        return this;
    }

    /**
     * Update header to return as a Header object, if a header with
     * the same name already exists it will be modified
     *
     * @param header a Header object
     */
    public HttpResponse replaceHeader(Header header) {
        getOrCreateHeaders().replaceEntry(header);
        this.hashCode = 0;
        return this;
    }

    /**
     * Update header to return as a Header object, if a header with
     * the same name already exists it will be modified
     *
     * @param name   the header name
     * @param values the header values
     */
    public HttpResponse replaceHeader(String name, String... values) {
        getOrCreateHeaders().replaceEntry(name, values);
        this.hashCode = 0;
        return this;
    }

    public List<Header> getHeaderList() {
        if (this.headers != null) {
            return this.headers.getEntries();
        } else {
            return Collections.emptyList();
        }
    }

    public Multimap<NottableString, NottableString> getHeaderMultimap() {
        if (this.headers != null) {
            return this.headers.getMultimap();
        } else {
            return null;
        }
    }

    public List<String> getHeader(String name) {
        if (this.headers != null) {
            return this.headers.getValues(name);
        } else {
            return Collections.emptyList();
        }
    }

    public String getFirstHeader(String name) {
        if (this.headers != null) {
            return this.headers.getFirstValue(name);
        } else {
            return "";
        }
    }

    /**
     * Returns true if a header with the specified name has been added
     *
     * @param name the header name
     * @return true if a header has been added with that name otherwise false
     */
    public boolean containsHeader(String name) {
        if (this.headers != null) {
            return this.headers.containsEntry(name);
        } else {
            return false;
        }
    }

    public HttpResponse removeHeader(String name) {
        if (this.headers != null) {
            headers.remove(name);
            this.hashCode = 0;
        }
        return this;
    }

    public HttpResponse removeHeader(NottableString name) {
        if (this.headers != null) {
            headers.remove(name);
            this.hashCode = 0;
        }
        return this;
    }

    /**
     * Returns true if a header with the specified name has been added
     *
     * @param name  the header name
     * @param value the header value
     * @return true if a header has been added with that name otherwise false
     */
    public boolean containsHeader(String name, String value) {
        if (this.headers != null) {
            return this.headers.containsEntry(name, value);
        } else {
            return false;
        }
    }


    public Cookies getCookies() {
        return this.cookies;
    }

    private Cookies getOrCreateCookies() {
        if (this.cookies == null) {
            this.cookies = new Cookies();
            this.hashCode = 0;
        }
        return this.cookies;
    }

    public HttpResponse withCookies(Cookies cookies) {
        if (cookies == null || cookies.isEmpty()) {
            this.cookies = null;
        } else {
            this.cookies = cookies;
        }
        this.hashCode = 0;
        return this;
    }

    /**
     * The cookies to return as Set-Cookie headers as a list of Cookie objects
     *
     * @param cookies a list of Cookie objects
     */
    public HttpResponse withCookies(List<Cookie> cookies) {
        getOrCreateCookies().withEntries(cookies);
        this.hashCode = 0;
        return this;
    }

    /**
     * The cookies to return as Set-Cookie headers as a varargs of Cookie objects
     *
     * @param cookies a varargs of Cookie objects
     */
    public HttpResponse withCookies(Cookie... cookies) {
        getOrCreateCookies().withEntries(cookies);
        this.hashCode = 0;
        return this;
    }

    /**
     * Add cookie to return as Set-Cookie header
     *
     * @param cookie a Cookie object
     */
    public HttpResponse withCookie(Cookie cookie) {
        getOrCreateCookies().withEntry(cookie);
        this.hashCode = 0;
        return this;
    }

    /**
     * Add cookie to return as Set-Cookie header
     *
     * @param name  the cookies name
     * @param value the cookies value
     */
    public HttpResponse withCookie(String name, String value) {
        getOrCreateCookies().withEntry(name, value);
        this.hashCode = 0;
        return this;
    }

    /**
     * Adds one cookie to match on or to not match on using the NottableString, each NottableString can either be a positive matching value,
     * such as string("match"), or a value to not match on, such as not("do not match"), the string values passed to the NottableString
     * can be a plain string or a regex (for more details of the supported regex syntax see
     * http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html)
     *
     * @param name  the cookies name
     * @param value the cookies value
     */
    public HttpResponse withCookie(NottableString name, NottableString value) {
        getOrCreateCookies().withEntry(name, value);
        this.hashCode = 0;
        return this;
    }

    public List<Cookie> getCookieList() {
        if (this.cookies != null) {
            return this.cookies.getEntries();
        } else {
            return Collections.emptyList();
        }
    }

    public Map<NottableString, NottableString> getCookieMap() {
        if (this.cookies != null) {
            return this.cookies.getMap();
        } else {
            return null;
        }
    }

    public boolean cookieHeadeDoesNotAlreadyExists(Cookie cookieValue) {
        List<String> setCookieHeaders = getHeader(SET_COOKIE.toString());
        for (String setCookieHeader : setCookieHeaders) {
            String existingCookieName = ClientCookieDecoder.LAX.decode(setCookieHeader).name();
            String existingCookieValue = ClientCookieDecoder.LAX.decode(setCookieHeader).value();
            if (existingCookieName.equalsIgnoreCase(cookieValue.getName().getValue()) && existingCookieValue.equalsIgnoreCase(cookieValue.getValue().getValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean cookieHeadeDoesNotAlreadyExists(String name, String value) {
        List<String> setCookieHeaders = getHeader(SET_COOKIE.toString());
        for (String setCookieHeader : setCookieHeaders) {
            try {
                String existingCookieName = ClientCookieDecoder.LAX.decode(setCookieHeader).name();
                String existingCookieValue = ClientCookieDecoder.LAX.decode(setCookieHeader).value();
                if (existingCookieName.equalsIgnoreCase(name) && existingCookieValue.equalsIgnoreCase(value)) {
                    return false;
                }
            } catch (NullPointerException e) {
                System.out.println("NullPointerException cookieHeadeDoesNotAlreadyExists");
            }
        }
        return true;
    }

    /**
     * The connection options for override the default connection behaviour, this allows full control of headers such
     * as "Connection" or "Content-Length" or controlling whether the socket is closed after the response has been sent
     *
     * @param connectionOptions the connection options for override the default connection behaviour
     */
    public HttpResponse withConnectionOptions(ConnectionOptions connectionOptions) {
        this.connectionOptions = connectionOptions;
        this.hashCode = 0;
        return this;
    }

    public ConnectionOptions getConnectionOptions() {
        return connectionOptions;
    }

    @Override
    @JsonIgnore
    public Type getType() {
        return Type.RESPONSE;
    }

    public HttpResponse shallowClone() {
        return response()
            .withStatusCode(statusCode)
            .withReasonPhrase(reasonPhrase)
            .withBody(body)
            .withHeaders(headers)
            .withCookies(cookies)
            .withDelay(getDelay())
            .withConnectionOptions(connectionOptions);
    }


    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public HttpResponse clone() {
        return response()
            .withStatusCode(statusCode)
            .withReasonPhrase(reasonPhrase)
            .withBody(body)
            .withHeaders(headers != null ? headers.clone() : null)
            .withCookies(cookies != null ? cookies.clone() : null)
            .withDelay(getDelay())
            .withConnectionOptions(connectionOptions);
    }

    public HttpResponse update(HttpResponse replaceResponse) {
        if (replaceResponse.getStatusCode() != null) {
            withStatusCode(replaceResponse.getStatusCode());
        }
        if (replaceResponse.getReasonPhrase() != null) {
            withReasonPhrase(replaceResponse.getReasonPhrase());
        }
        for (Header header : replaceResponse.getHeaderList()) {
            getOrCreateHeaders().replaceEntry(header);
        }
        for (Cookie cookie : replaceResponse.getCookieList()) {
            withCookie(cookie);
        }
        if (replaceResponse.getBody() != null) {
            withBody(replaceResponse.getBody());
        }
        if (replaceResponse.getConnectionOptions() != null) {
            withConnectionOptions(replaceResponse.getConnectionOptions());
        }
        this.hashCode = 0;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (hashCode() != o.hashCode()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        HttpResponse that = (HttpResponse) o;
        return Objects.equals(statusCode, that.statusCode) &&
            Objects.equals(reasonPhrase, that.reasonPhrase) &&
            Objects.equals(body, that.body) &&
            Objects.equals(headers, that.headers) &&
            Objects.equals(cookies, that.cookies) &&
            Objects.equals(connectionOptions, that.connectionOptions);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hash(super.hashCode(), statusCode, reasonPhrase, body, headers, cookies, connectionOptions);
        }
        return hashCode;
    }
}
