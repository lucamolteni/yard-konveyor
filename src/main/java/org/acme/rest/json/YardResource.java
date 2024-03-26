package org.acme.rest.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.drools.io.ReaderResource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.marshalling.DMNMarshaller;
import org.kie.dmn.backend.marshalling.v1x.DMNMarshallerFactory;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.core.internal.utils.DynamicDMNContextBuilder;
import org.kie.dmn.core.internal.utils.MarshallingStubUtils;
import org.kie.dmn.model.api.Definitions;
import org.kie.yard.impl1.YaRDParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ObjectName;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/yard")
public class YardResource {

    private static final Logger LOG = LoggerFactory.getLogger(YardResource.class);

    @Inject
    ObjectMapper jsonMapper;

    @GET
    public Response decisionTable() throws IOException, URISyntaxException {
        String yamlDecision = getScoreCardTable();
        return Response.ok(yamlDecision).build();
    }

    private String getScoreCardTable() throws URISyntaxException, IOException {
        // Get the path of the YAML file in the resources folder
        java.nio.file.Path yamlPath = Paths.get(getClass().getResource("/sonarcloud.yml").toURI());

        // Create an InputStream from the path
        String yamlDecision = Files.readString(yamlPath);
        return yamlDecision;
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response scorecard(Map<String, Object> readValue) throws Exception {

        String yamlDecision = getScoreCardTable();

        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        YaRDParser parser = new YaRDParser();

        Definitions definitions = parser.parse(yamlDecision);
        String xml = dmnMarshaller.marshal(definitions);
        LOG.info("{}", xml);

        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults()
                                                 .buildConfiguration()
                                                 .fromResources(
                                                         Arrays.asList(new ReaderResource(new StringReader(xml))))
                                                 .getOrElseThrow(RuntimeException::new);

        DMNContext dmnContext = new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnRuntime.getModels().get(0))
                .populateContextWith(readValue);
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnRuntime.getModels().get(0), dmnContext);
        Map<String, Object> onlyOutputs = new LinkedHashMap<>();
        for (DMNDecisionResult r : dmnResult.getDecisionResults()) {
            onlyOutputs.put(r.getDecisionName(), r.getResult());
        }


        Object serialized = MarshallingStubUtils.stubDMNResult(onlyOutputs, Object::toString);
        final String OUTPUT_JSON = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(serialized);

        return Response.ok(OUTPUT_JSON).build();
    }
}
