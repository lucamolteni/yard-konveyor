package org.acme.rest.json;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

class YardServiceTest {

    @Test
    public void perfectScoreForZeroIssues() {
        YardService yardService = new YardService();

        Map<String, Object> mandatoryIssues = Map.of("mandatoryIssues", 0);
        Map<String, Object> score = yardService.callYardDT(mandatoryIssues);
        assertThat(score).containsEntry("konveyorScoreCard", valueOf(100));
    }

    @Test
    public void silverFor3Issues() {
        YardService yardService = new YardService();

        Map<String, Object> mandatoryIssues = Map.of("mandatoryIssues", 3);
        Map<String, Object> score = yardService.callYardDT(mandatoryIssues);
        assertThat(score).containsEntry("konveyorScoreCard", valueOf(50));
    }

    @Test
    public void bronzeScoreFor3Issues() {
        YardService yardService = new YardService();

        Map<String, Object> mandatoryIssues = Map.of("mandatoryIssues", 13);
        Map<String, Object> score = yardService.callYardDT(mandatoryIssues);
        assertThat(score).containsEntry("konveyorScoreCard", valueOf(0));
    }

}