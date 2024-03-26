package org.acme.rest.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public int countByCategory(ObjectNode konveyorAnalysis, @QueryParam("category") List<String> categories)
            throws JsonQueryException {

        if (categories == null || categories.isEmpty()) {
            categories = List.of("mandatory");
        }

        String categoriesQueryString = categories.stream()
                                                 .map(c -> String.format(".category == \"%s\"", c))
                                                 .collect(Collectors.joining(" and "));

        String jqQuery = String.format(".issues[] | select(%s)", categoriesQueryString);

        final JsonQuery query = JsonQuery.compile(jqQuery, Versions.JQ_1_6);
        LOG.info(query.toString());

        List<JsonNode> out = new ArrayList<>();
        query.apply(scope, konveyorAnalysis, out::add);

        return out.size();
    }

    @GET
    @Path("/{id}/analysis")
    @Consumes(MediaType.APPLICATION_JSON)
    public int countByCategory(@PathParam("id") int id, @QueryParam("category") List<String> categories)
            throws JsonQueryException {
        ObjectNode analysis = konveyorService.analysis(id);

        int score = countByCategory(analysis, categories);

        return score;
    }

}
