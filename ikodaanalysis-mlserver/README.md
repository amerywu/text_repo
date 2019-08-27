# mlServer

This is a simple Spring Boot server started as part of a Spark Submit app. It facilitates communication with remote applications.

### mlServer Functions:

1. Communicates with remote streaming applications. It confirms the existence of the Spark master. It also passes metadata about incoming streams to the Spark Submit app streaming functions.
2. Acts as an interface for ML predictions. It passes data to the predeiction engine and returns the prediction to the remote query source.

Note: There is no guide or API documentation for this tool as it is of little generic use.
