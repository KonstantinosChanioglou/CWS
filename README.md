1) from https://github.com/techbuzzblogs/camunda/tree/main/camunda-platform-8.0.0
   2) dowload the YAML file
   3) RUN docker-compose -f docker-compose-core.yaml up -d
   4) Download connectors https://hub.docker.com/r/camunda/connectors


Infastructure
1) Run CWS docker image
2) run Rabitmq
   3) docker run -d --hostname rabbitmq --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   4) # Connect to RabbitMQ Docker container
   docker exec -it rabbitmq rabbitmqctl list_exchanges
   
   # Create an exchange
   docker exec -it rabbitmq rabbitmqadmin declare exchange name=temperature.exchange type=direct
   
   # Create queues
   docker exec -it rabbitmq rabbitmqadmin declare queue name=temperature.request.queue durable=true
   docker exec -it rabbitmq rabbitmqadmin declare queue name=temperature.response.queue durable=true
   
   # Bind queues to exchange
   docker exec -it rabbitmq rabbitmqadmin declare binding source=temperature.exchange destination=temperature.request.queue routing_key=temperature.request
   docker exec -it rabbitmq rabbitmqadmin declare binding source=temperature.exchange destination=temperature.response.queue routing_key=temperature.response

2) Run CWS Intelij main class
3) Design and deploy BPMN
4) Execute the BPMN
5) Open Camunda processes and tasklist from docker to see the execution


So far what I have:
1) Service task can execute precompiles classes in the project in order to publish specific topics to the message broker
   2) Is there any way to directly publish topics?
   3) So this will be the precompiles functionality in the design BPMN time to publish topics
3) Then Message broker publish topics
4) Adaptors implement HL7 and trigger with the appropriate system to execute the UF

Next Steps:
1) Validate Functionality of the implemented components
   1) Precompiled Functionality
   2) Adaptor
   3) External System
2) Validate Broker Facrionality
   1) What if there are two consumers or 2 external systems publish the smae information?

Question to Aly:
1) Is what I am doing the wanted?
2) Map the current architecture with mine
3) Evaluate the 3 scenarios for deployment scenarios
