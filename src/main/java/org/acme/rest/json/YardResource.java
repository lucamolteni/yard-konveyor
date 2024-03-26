package org.acme.rest.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

@Path("/yard")
public class YardResource {

    private static final Logger LOG = LoggerFactory.getLogger(YardResource.class);

    @Inject
    ObjectMapper jsonMapper;

    @Inject
    YardService yardService;

    @GET
    public Response decisionTable() throws IOException, URISyntaxException {
        String yamlDecision = yardService.getScoreCardTable("/konveyor-analysis.yml");
        return Response.ok(yamlDecision).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response scorecard(Map<String, Object> readValue) throws Exception {

        Object serialized = yardService.callYardDT(readValue);
        final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serialized);

        return Response.ok(OUTPUT_JSON).build();
    }

}
