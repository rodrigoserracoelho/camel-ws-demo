package io.surisoft.demo;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestOperationParamDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.apache.camel.spi.RestConfiguration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CamelComponent extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        String webSocketName = "/chat";
        String authorizedHost = "http://localhost";
        String endpointProtocol = "http://";
        String endpointHost = "localhost:9010";
        String endpointContext = "/chat";

        Map<String, String> corsHeaders = new HashMap<>();
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
        corsHeaders.put("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
        corsHeaders.put("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");
        corsHeaders.put("Access-Control-Allow-Origin", authorizedHost);

        RestConfiguration restConfiguration = new RestConfiguration();
        restConfiguration.setEnableCORS(true);
        restConfiguration.setCorsHeaders(corsHeaders);

        /*from("ahc-ws://localhost:8080/gw/chat").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                log.info("Incoming websocket connection.....");
            }
        }).to("ahc-ws://localhost:9010/chat");*/

        rest(webSocketName + "/info").get().route().process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                log.info("CHAT_INFO");
            }
        }).to(endpointProtocol + endpointHost + endpointContext + "/info?bridgeEndpoint=true");

        rest(webSocketName).get().route().process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                log.info("CHAT");
            }
        }).to(endpointProtocol + endpointHost + endpointContext + "?bridgeEndpoint=true");


        RouteDefinition routeDefinition = rest(webSocketName + "/{param1}/{param2}/{param3}").get().route().process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                Map<String, Object> headers = exchange.getIn().getHeaders();
                for(String header : headers.keySet()) {
                    log.info(header);
                    log.info(exchange.getIn().getHeader(header) != null ? exchange.getIn().getHeader(header).getClass().getCanonicalName() : "------");
                    if(header.equalsIgnoreCase("upgrade")) {
                        log.info((String)exchange.getIn().getHeader(header));
                    }
                    log.info("----");
                }
                log.info("3 PARAM THING");
            }
        });
        RestOperationParamDefinition restParamDefinition = new RestOperationParamDefinition();
        List<String> paramList = evaluatePath(webSocketName + "/{param1}/{param2}/{param3}");
        for(String param : paramList) {
            log.info(param);
            restParamDefinition.name(param)
                    .type(RestParamType.path)
                    .dataType("String");
        }
        routeDefinition
                .to(endpointProtocol + endpointHost + "?bridgeEndpoint=true");

        RouteDefinition routePostDefinition = rest(webSocketName + "/{param1}/{param2}/{param3}").post().route().process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                //exchange.getIn().setHeader("Upgrade", "websocket");
                Map<String, Object> headers = exchange.getIn().getHeaders();
                for(String header : headers.keySet()) {
                    log.info(header);
                    log.info(exchange.getIn().getHeader(header) != null ? exchange.getIn().getHeader(header).getClass().getCanonicalName() : "------");
                    if(header.equalsIgnoreCase("upgrade")) {
                        log.info((String)exchange.getIn().getHeader(header));
                    }
                    log.info("----");
                }
                log.info("3 PARAM THING");
            }
        });
        RestOperationParamDefinition restPostParamDefinition = new RestOperationParamDefinition();
        List<String> paramPostList = evaluatePath(webSocketName + "/{param1}/{param2}/{param3}");
        for(String param : paramPostList) {
            log.info(param);
            restPostParamDefinition.name(param)
                    .type(RestParamType.path)
                    .dataType("String");
        }
        routePostDefinition
                .to(endpointProtocol + endpointHost + "?bridgeEndpoint=true");


    }

    public List<String> evaluatePath(String fullPath) {
        List<String> paramList = new ArrayList<>();
        if(fullPath.contains("{")) {
            String[] splittedPath = fullPath.split("/");
            for(String path : splittedPath) {
                if(path.contains("{")) {
                    String name = path.substring(1, path.length()-1);
                    paramList.add(name);
                }
            }
        }
        return paramList;
    }
}
