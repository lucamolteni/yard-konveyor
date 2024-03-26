package org.acme.rest.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/hub/applications")
@RegisterRestClient(configKey="konveyor-api")
public interface KonveyorService {

    @GET
    @Path("{id}/analysis")
    ObjectNode analysis(@PathParam("id") int id);
}
