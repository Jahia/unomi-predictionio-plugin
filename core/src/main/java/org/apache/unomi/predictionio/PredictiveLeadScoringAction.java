/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.unomi.predictionio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.unomi.api.CustomItem;
import org.apache.unomi.api.Event;
import org.apache.unomi.api.actions.Action;
import org.apache.unomi.api.actions.ActionExecutor;
import org.apache.unomi.api.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class PredictiveLeadScoringAction implements ActionExecutor {

    private static final String LEAD_SCORING_PROPERTY = "leadScoringProbability";

    private String predictiveLeadScoringEngineUrl = "http://localhost:8000/queries.json";

    private static Logger logger = LoggerFactory.getLogger(PredictiveLeadScoringAction.class);
    private CloseableHttpClient httpClient;

    @Override
    public int execute(Action action, Event event) {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }
        if (predictiveLeadScoringEngineUrl == null) {
            logger.warn("Configuration incomplete.");
            return EventService.NO_CHANGE;
        }

        return getLeadScoring(event);
    }

    private int getLeadScoring(Event event) {
        HttpPost httpPost = new HttpPost(predictiveLeadScoringEngineUrl);
        String landingPageId = "example.com/page8";
        String referrerId = "referrer9.com";
        if ("view".equals(event.getEventType())) {
            CustomItem pageItem = (CustomItem) event.getTarget();
            Map<String,Object> pageInfo = (Map<String,Object>) pageItem.getProperties().get("pageInfo");
            landingPageId = (String) pageInfo.get("destinationURL");
            referrerId = (String) pageInfo.get("referringURL");
        }
        // String browser = "Firefox";
        String browser = (String) event.getSession().getProperty("userAgentName");

        String JSON_STRING="{ \"landingPageId\": \"" + landingPageId + "\", \"referrerId\": \"" + referrerId + "\", \"browser\": \"" + browser + "\"}";
        HttpEntity stringEntity = new StringEntity(JSON_STRING, ContentType.APPLICATION_JSON);
        httpPost.setEntity(stringEntity);
        JsonNode jsonPredictedScore = null;
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity entity = response.getEntity();
                String responseString;
                if (entity != null) {
                    try {
                        responseString = EntityUtils.toString(entity);
                        ObjectMapper objectMapper = new ObjectMapper();
                        jsonPredictedScore = objectMapper.readTree(responseString);
                        double predictedScore = jsonPredictedScore.get("score").asDouble() * 100.0;
                        if (predictedScore >= 0) {
                            logger.info("Profile score is now: " + predictedScore);
                            int predictedScoreInt = (int) predictedScore;
                            event.getProfile().setProperty(LEAD_SCORING_PROPERTY, (Integer) predictedScoreInt);
                        }
                    } catch (IOException e) {
                        logger.error("Error with the API json response.", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error with the Http Request execution. Wrong parameters given", e);
        } finally {
            if (response != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
        return EventService.SESSION_UPDATED;
    }

    public void setPredictiveLeadScoringEngineUrl(String predictiveLeadScoringEngineUrl) {
        this.predictiveLeadScoringEngineUrl = predictiveLeadScoringEngineUrl;
    }
}
