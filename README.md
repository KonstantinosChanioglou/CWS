CWS Proof of Concept Implementaiton

The goal of the provided implementation is to demonstrate the capabilities of the proposed evolvable CWS design.

In Adaptors folder can be found the three adaptors used for the scenarios described in the report. The idea is that each adaptor can be linked with a specific external system. Adoptor Type 1 is linked with the external system PM type 1 and adaptor Type 2 is linked to external system PM Type 2.
Both provide functionality to invoke exposed services of the external systems, and expose service to trigger sepsis workflow in CWS. They simulate the needed translation the requests from-to CWS is needed. Also a third adaptor handles communication with the an external system simulating the EMR.
In full scale implementation other adaptors can be added in this folder as well.

In UFs folder all the UFs needed for executing the sepsis workflow are implemented. In full scale implementation other adaptors can be added in this folder as well.

In External Systems folder the two PMs types as well as a DB simulating an EMR can be found. The two different PMs simulating different types of PMs by just exposing differnt tyes of endpoints but the point is that in the actual implementation these types will implement different interoperability protocols and not just different endpoints.

To run the system:
1) Download and run the containers provided by Camunda Platform 8 (docker-compose-core.yaml): from https://github.com/techbuzzblogs/camunda/tree/main/camunda-platform-8.0.
   1) This include Zeebe Cluster, Camunda Operate and Tasklist as well as Elasticsearch
   2) This is provided already in this project
   3) In the CWS folder run: docker-compose -f docker-compose-core.yaml up -d

2) Download and run te RabbitMQ container
   1) docker run -d --hostname rabbit-host --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

3) To run all the UFs and Adators:
   1) docker-compose -f docker-compose-cws.yaml up --build

3) To run the external systems
   1) docker-compose -f docker-compose-extsys.yaml up --build

After running all the containers, you can start using the CWS for executing sepsis workflow.

1) Deploy the Sepsis Workflow.bpmn to the execution eninge using the Camunda editor (Cluster Endpoint is the zeebe endpoint http://localhost:26559 or 26500)

2) You can either trigger the workflow weither by using the editor or sending a request to PM Adaptors from inside the container of a PM-Type1 using the command:
   1) curl -X POST http://PatientMonitorAdaptorType1:8181/trigger-sepsis -H "Content-Type: application/json" -d '{"patientId": 1}'

3) The instance of the workflow will appear in the camunda operate which you can open by clicking the port on its container in Docker.

4) The user tasks can be executed by opening in the same way the tasklist and selecting the wanted task.


[//]: # (Documentation:)

[//]: # ()
[//]: # (1&#41; Camunda8Application.java: The main Spring Boot application class responsible for bootstrapping the application and initializing all configured components and services. It includes the @EnableZeebeClient annotation to connect the application to the Zeebe broker.)

[//]: # ()
[//]: # (2&#41; TemperatureTake.java: Defines the logic for handling specific process tasks related to temperature monitoring. Interacts with RabbitMQ to publish messages to adaptors for requesting temperature from external systems)

[//]: # ()
[//]: # (3&#41; PMAdaptor.java &#40;Formerly TemperatureAdaptor.java&#41;: Acts as adaptow that listens to interested topics and sends Requests to PM. Same way once it has a response publish to specific topics the response data)

[//]: # ()
[//]: # (Dynamic BPMN Reconfiguration, Deployment and Execution)

[//]: # ()
[//]: # (1&#41; DynamicQueueManager.java: )

[//]: # (   1&#41; Provides functionlity for dynamic creation and deletion of message queues within RabbitMQ.)

[//]: # (   2&#41; Then the consumers producers use it to declare the routing keys and queues.)

[//]: # (   3&#41; It also creates the exchange )

[//]: # ()

[//]: # (application.properties: Configuration file containing settings related to the Spring Boot application, including Zeebe broker connection details, RabbitMQ settings, and other application-specific properties.)

[//]: # ()
[//]: # ()
[//]: # (service-task-session.bpmn: BPMN file defining a different workflow, potentially with service tasks requiring integration with external systems or automated processing.)

[//]: # ()
[//]: # (docker-compose.yaml: Configuration file defining services, networks, and dependencies for running the application with Docker, including Zeebe broker, RabbitMQ, and other components as containers.)


[//]: # (Current Idea:)

[//]: # ()
[//]: # (1&#41; Edit)

[//]: # (2&#41; Deploy)

[//]: # (   3&#41; Invoke-WebRequest -Uri "http://localhost:8080/process/deploy?filePath=src/main/resources/service-task-session.bpmn" -Method POST)

[//]: # (4&#41; Execute )

[//]: # (   5&#41; Manualy )

[//]: # (   6&#41; Automatically from PM &#40;not yet&#41; but simple executing: Invoke-WebRequest -Uri "http://localhost:8181/trigger-sepsis" -Method POST -Body &#40;@{ patientId = 1 } | ConvertTo-Json&#41; -ContentType "application/json" or from inside the PM adaptor container    curl -X POST http://localhost:8181/trigger-sepsis -H "Content-Type: application/json" -d '{"patientId": 1}')

[//]: # ()
[//]: # (      7&#41; Now the PM adaptor received request )

[//]: # (      8&#41; Publish sepsis.execution)

[//]: # (      9&#41; ProcessExecutor Listens and executes the process with the correc ID)

[//]: # (      10&#41; Service task is executed and calls TemperatureTaking)

[//]: # (      11&#41; Publish topic to the adaptor and recieving the temperature from a server like being the PM.)

[//]: # (      12&#41; Value is published to temperature.response to the TemperatureTakings)

[//]: # (      13&#41; Value goas back to the BPMN)