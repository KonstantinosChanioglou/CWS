1) from https://github.com/techbuzzblogs/camunda/tree/main/camunda-platform-8.0.0
   2) dowload the YAML file
   3) RUN docker-compose -f docker-compose-core.yaml up -d
   4) Download connectors https://hub.docker.com/r/camunda/connectors


Infastructure
1) Run CWS docker image
2) Run CWS Intelij main class
3) Design and deploy BPMN


So far what I have:
1) Service task can execute precompiles classes in the project in order to publish specific topics to the message broker
   2) Is there any way to directly publish topics?
   3) So this will be the precompiles functionality in the design BPMN time to publish topics
3) Then Message broker publish topics
4) Adaptors implement HL7 and trigger with the appropriate system to execute the UF