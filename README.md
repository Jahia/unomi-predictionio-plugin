<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

Requirements
------------
- Apache PredictionIO 0.13.0+
- PredictionIO Lead Scoring Template
- Apache Unomi 1.3.0+ 

Installation
------------ 

Install & Start PredictionIO:

    git clone https://github.com/apache/predictionio.git
    cd predictionio
    cd docker
    docker build -t predictionio/pio pio
    docker-compose -f docker-compose.yml \
         -f pgsql/docker-compose.base.yml \
         -f pgsql/docker-compose.meta.yml \
         -f pgsql/docker-compose.event.yml \
         -f pgsql/docker-compose.model.yml \
         up

Build, Train & deploy Lead Scoring PredictionIO template:

    git clone https://github.com/sergehuber/template-scala-parallel-leadscoring.git MyLeadScoring
    cd MyLeadScoring
    pio-docker build --verbose
    pio-docker app list
    ACCESS_KEY=cdelLgZqZxj7CI_2hDM_vy-Q3fhLDxlTQKao_UHe9DgcFLkSVm9Yfq_3ve8BTgzl
    python data/import_eventserver.py --access_key $ACCESS_KEY
    pio-docker train
    pio-docker deploy
    
Compile & start Apache Unomi:
    
    
Deploy plugin to Apache Unomi:

    feature:repo-add mvn:org.apache.unomi/unomi-predictionio-kar/${project.version}/xml/features
    feature:install unomi-predictionio-kar

Test request
------------

    curl -X POST http://localhost:8181/context.json?sessionId=1234 \
    -H "Content-Type: application/json" \
    -d @- <<'EOF'
    {
        "source": {
            "itemId":"homepage",
            "itemType":"page",
            "scope":"example"
        },
        "requiredProfileProperties":["*"],
        "requiredSessionProperties":["*"],
        "requireSegments":true
    }
    EOF

should return something like:

    {
      "profileId": "ce1123d3-93c5-4d82-a1da-f51296fcee27",
      "sessionId": "1234",
      "profileProperties": {
        "lastVisit": "2019-09-04T08:45:53Z",
        "nbOfVisits": 1,
        "firstVisit": "2019-09-04T08:45:53Z"
      },
      "sessionProperties": {
        "sessionCity": "Geneva",
        "operatingSystemFamily": "Unknown",
        "userAgentNameAndVersion": "curl@@7.54.0",
        "countryAndCity": "Switzerland@@Geneva@@2660645@@6458783",
        "userAgent": "curl/7.54.0",
        "userAgentName": "curl",
        "leadScoringPrediction": 0.775,
        "sessionCountryCode": "CH",
        "sessionCountryName": "Switzerland",
        "deviceCategory": "Unknown",
        "userAgentVersion": "7.54.0",
        "sessionAdminSubDiv2": "6458783",
        "sessionAdminSubDiv1": "2660645",
        "location": {
          "lon": 6.1282508,
          "lat": 46.1884341
        },
        "sessionIsp": "Cablecom",
        "operatingSystemName": "Unknown"
      },
      "profileSegments": [],
      "filteringResults": null,
      "personalizations": null,
      "trackedConditions": [
        {
          "parameterValues": {
            "formId": "testFormTracking",
            "pagePath": "/tracker/"
          },
          "type": "formEventCondition"
        },
        {
          "parameterValues": {
            "formId": "searchForm"
          },
          "type": "formEventCondition"
        },
        {
          "parameterValues": {
            "formId": "advancedSearchForm"
          },
          "type": "formEventCondition"
        }
      ],
      "anonymousBrowsing": false,
      "consents": {}
    }
    
The `leadScoringProbability` property should be there and have a value that represents the probability that the visitor
will convert.    