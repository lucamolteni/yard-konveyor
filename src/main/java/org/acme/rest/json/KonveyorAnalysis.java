package org.acme.rest.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Path("/konveyor")
public class KonveyorAnalysis {

    private static final Logger LOG = LoggerFactory.getLogger(YardResource.class);

    @Inject
    Scope scope;

    @Inject
    @RestClient
    KonveyorService konveyorService;

    @POST
    @Path("/score")
    @Consumes(MediaType.APPLICATION_JSON)
    public int score(ObjectNode konveyorAnalysis) throws JsonQueryException {

        final JsonQuery query = JsonQuery.compile(".issues[] | select(.category == \"mandatory\")", Versions.JQ_1_6);
        LOG.info(query.toString());

        List<JsonNode> out = new ArrayList<>();
        query.apply(scope, konveyorAnalysis, out::add);

        return out.size();
    }

    @GET
    @Path("/{id}/analysis")
    @Consumes(MediaType.APPLICATION_JSON)
    public int score(@PathParam("id") int id) throws JsonQueryException {
        ObjectNode analysis = konveyorService.analysis(id);

        int score = score(analysis);

        return score;
    }

}
