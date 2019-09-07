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

Apache Unomi - Apache PredictionIO Integration example
======================================================

Requirements
------------
- Apache PredictionIO 0.13.0+
- PredictionIO Lead Scoring Template
- Apache Unomi 1.3.0+ 

Installation
------------ 

Install & Start PredictionIO (With Docker):

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

Install & Start PredictionIO (From Source):

    PredictionIO-0.14.0/bin/pio eventserver &
    PredictionIO-0.14.0/bin/pio status

Build, Train & deploy Lead Scoring PredictionIO template (with Docker):

    git clone https://github.com/sergehuber/template-scala-parallel-leadscoring.git MyLeadScoring
    cd MyLeadScoring
    git checkout unomi-integration
    export PATH=**REPLACE_WITH_PATH_TO_PREDICTIONIO_SRC**/docker/bin/:$PATH
    pio-docker app new MyLeadScoring    
    pio-docker build --verbose
    pio-docker app list
    ACCESS_KEY=cdelLgZqZxj7CI_2hDM_vy-Q3fhLDxlTQKao_UHe9DgcFLkSVm9Yfq_3ve8BTgzl
    pip install predictionio
    python data/import_eventserver.py --access_key $ACCESS_KEY
    pio-docker train
    pio-docker deploy

Build, Train & deploy Lead Scoring PredictionIO template (with Docker):

    git clone https://github.com/sergehuber/template-scala-parallel-leadscoring.git MyLeadScoring
    cd MyLeadScoring
    git checkout unomi-integration
    export PATH=**REPLACE_WITH_PATH_TO_PREDICTIONIO_SRC**/bin/:$PATH
    pio app new MyLeadScoring    
    pio build --verbose
    pio app list
    ACCESS_KEY=cdelLgZqZxj7CI_2hDM_vy-Q3fhLDxlTQKao_UHe9DgcFLkSVm9Yfq_3ve8BTgzl
    pip install predictionio
    python data/import_eventserver.py --access_key $ACCESS_KEY
    pio train
    pio deploy

Download, configure and start ElasticSearch 5.6.3
    
    curl -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.6.3.tar.gz
    tar zxvf elasticsearch-5.6.3.tar.gz
    cd elasticsearch-5.6.3
    change cluster.name to cluster.name: contextElasticSearch-COMPUTERNAME in config/elasticsearch.yml file
    bin/elasticsearch
    
Compile & start Apache Unomi:
    
    git clone https://github.com/apache/unomi.git
    cd unomi
    ./buildAndRunNoTests.sh
    unomi:start (in Karaf shell) 
    
Deploy plugin to Apache Unomi (in Karaf shell):

    feature:repo-add mvn:org.apache.unomi/unomi-predictionio-kar/${project.version}/xml/features
    feature:install unomi-predictionio-kar

Testing
-------

You can then open a browser at the following URL to test the integration:

    http://localhost:8181/predictionio
    
First click on 'Page 1' on the top right back, then click Page 2 and go back to Page 1, 
nothing changes, click Page 3 and go back to Page 1 and you should see the banner change.

Also check the Unomi logs in the Karaf shell using : log:tail to see the probability value.
