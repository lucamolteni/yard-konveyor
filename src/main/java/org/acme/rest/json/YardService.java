package org.acme.rest.json;

import jakarta.enterprise.context.ApplicationScoped;
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

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class YardService {

    private static final Logger LOG = LoggerFactory.getLogger(YardResource.class);
    private String dtXML;

    public Map<String, Object> callYardDT(Map<String, Object> readValue) {
        String yamlDecision = getScoreCardTable("/konveyor-analysis.yml");
        if (dtXML == null) {
            dtXML = readDecisionTableXML(yamlDecision);
        }

        DMNRuntime dmnRuntime = createDMNRuntime(dtXML);
        DMNResult dmnResult = evaluate(readValue, dmnRuntime);

        Map<String, Object> onlyOutputs = new LinkedHashMap<>();
        for (DMNDecisionResult r : dmnResult.getDecisionResults()) {
            onlyOutputs.put(r.getDecisionName(), r.getResult());
        }

        return (Map<String, Object>) MarshallingStubUtils.stubDMNResult(onlyOutputs, Object::toString);
    }

    public String getScoreCardTable(String filePath) {
        URI uri;
        try {
            uri = getClass().getResource(filePath).toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("File not found: %s", filePath), e);
        }
        Path yamlPath = Paths.get(uri);

        try {
            return Files.readString(yamlPath);
        } catch (IOException e) {
            throw new RuntimeException(String.format("File is not a valid YAML: %s", filePath), e);
        }
    }

    private static String readDecisionTableXML(String yamlDecision) {
        DMNMarshaller dmnMarshaller = DMNMarshallerFactory.newDefaultMarshaller();
        YaRDParser parser = new YaRDParser();

        Definitions definitions;
        try {
            definitions = parser.parse(yamlDecision);
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse YAML:\n" + yamlDecision, e);
        }

        String xml = dmnMarshaller.marshal(definitions);
        LOG.debug("{}", xml);
        return xml;
    }


    private static DMNRuntime createDMNRuntime(String xml) {
        return DMNRuntimeBuilder.fromDefaults()
                                .buildConfiguration()
                                .fromResources(
                                        List.of(new ReaderResource(new StringReader(xml))))
                                .getOrElseThrow(RuntimeException::new);
    }

    private static DMNResult evaluate(Map<String, Object> readValue, DMNRuntime dmnRuntime) {
        DMNContext dmnContext = new DynamicDMNContextBuilder(dmnRuntime.newContext(), dmnRuntime.getModels().get(0))
                .populateContextWith(readValue);

        return dmnRuntime.evaluateAll(dmnRuntime.getModels().get(0), dmnContext);
    }
}
