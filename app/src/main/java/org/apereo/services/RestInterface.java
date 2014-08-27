package org.apereo.services;


import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.androidannotations.api.rest.RestClientHeaders;
import org.androidannotations.api.rest.RestClientRootUrl;
import org.androidannotations.api.rest.RestClientSupport;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

@Rest(
    converters = { StringHttpMessageConverter.class, FormHttpMessageConverter.class, GsonHttpMessageConverter.class},
    interceptors = {UmobileHeaderInterceptor.class })
public interface RestInterface extends RestClientRootUrl, RestClientSupport, RestClientHeaders {

    @Get("/uPortal/layout.json")
    @Accept(MediaType.APPLICATION_JSON)
    String getMainFeed();

}
