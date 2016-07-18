# jtcpserver

jtcpserver is an open-source tcp communication engine written in Java. It is easy to use, allowing to achieve from simple point-to-point connections to full client-server systems. Some of its features are:

* Simple point to point connections: the most basic layer of jtcpserver (communication layer) allows connecting to other tcp points and reading/writing byte-array based data or serialized Java objects.
* Channel layer: on top of the communication layer, the channel layer provides a 256 channel-multiplexed point to point communication architecture. Reception of data relies on an event-based design, simplifying development. In addition, the channel layer supports the use of communication FSMs, that greatly simplify the creation of point-to-point protocols.
* Client-server layer: the client-server layer sits on top of the former layers, providing a full client-server communication architecture. You can define and start a tcp server which will wait for client connections. Server and clients use the former 256-channel multiplexed communication architecture, with all its features.


Documentation under construction...
