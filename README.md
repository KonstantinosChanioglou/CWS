# CWS Proof of Concept Implementation

The goal of the provided implementation is to demonstrate the capabilities of the proposed evolvable CWS design.

---

## Adaptors

The `Adaptors` folder contains the three adaptors used in the scenarios described in the report. Each adaptor is linked to a specific external system:

- Adaptor Type 1 is connected to PM Type 1
- Adaptor Type 2 is connected to PM Type 2

Both provide functionality to:
- Invoke services exposed by their respective external systems
- Expose a service that triggers the sepsis workflow in the CWS

They simulate the necessary translation of requests between the CWS and the external systems.  
A third adaptor handles communication with an external system that simulates an EMR.

In a full-scale implementation, additional adaptors can be added to this folder as needed.

---

## Unit Functions (UFs)

The `UFs` folder contains all Unit Functions (UFs) required to execute the sepsis workflow.

In a full-scale implementation, additional UFs may also be added here.

---

## External Systems

The `External Systems` folder contains:
- Two types of simulated PMs
- A database representing an EMR

The PMs simulate different types of patient monitors by exposing different types of endpoints.  
In an actual implementation, these different types of external systems would implement different interoperability protocols, not just different endpoints.

---

## How to Run the System

1. Execute `docker-compose-core.yaml` to download and run the (Zeebe Engine, Elastic Search, Operate, Tasklist) containers provided by Camunda Platform 8:  

   ```bash
   docker-compose -f docker-compose-core.yaml up -d
   ```
2. Download and install Modeler from [Camunda](https://docs.camunda.io/docs/components/modeler/desktop-modeler/install-the-modeler/)

3. Execute the command beloow to download and run the RabbitMQ container:

   ```bash
   docker run -d --hostname rabbit-host --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```

4. To run all the UFs and Adaptors:

   ```bash
   docker-compose -f docker-compose-cws.yaml up --build
   ```

5. To run the external systems:

   ```bash
   docker-compose -f docker-compose-extsys.yaml up --build
   ```

---

## How to Use the System

1. Deploy the `Sepsis Workflow.bpmn` and its subworkflows `Standard Treatment.bpmn` and `SubWorkflow.bpmn` to the execution engine using the Camunda modeler.  
   Cluster Endpoint: `http://localhost:26559` or `http://localhost:26500`

2. Trigger the workflow:
   - Either via the Camunda modeler, or
   - By sending a request to the PM Adaptor from inside the container of `PM-Type1`:

     ```bash
     curl -X POST http://PatientMonitorAdaptorType1:8181/trigger-sepsis -H "Content-Type: application/json" -d '{"patientId": 1}'
     ```

3. The instance of the workflow will appear in Camunda Operate, which can be accessed by clicking the port on its container in Docker.

4. User tasks can be executed by opening the Tasklist in the same way and selecting the desired task.

5. Reconfiguration of the workflow can be done using the Camunda modeler.  
   After updating the workflow, redeploy it to the execution engine before use.

6. Adaptors can be enabled or disabled via the Docker interface, simulating the dynamic adaptation of the system to the availability of external systems.


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
