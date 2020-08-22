# Fast-RPC Framework

A high-performance Java RPC Framework based on Netty.



## Features

- Simple coding and easy to use
- Support both synchronous calls and asynchronous calls
- Support idle connections detection with heartbeat and auto closure
- Support RPC-level hooks
- Support transmission of primitive data types as well as custom data types



## How to Use

### 1. Create a Service Interface

```java
public interface SimpleService {
    String hello(Person person);
}
```

### 2. Implement the Service Interface

```java
public class SimpleServiceImpl implements SimpleService {
    @Override
    public String hello(Person person) {
        return "hello, name=" + person.getName() + ", age=" + person.getAge();
    }
}
```

### 3. Register the Service and Run the Server

```java
// register the service on 127.0.0.1:8080
RpcServer rpcServer = new RpcServer("127.0.0.1", 8080).registerService(SimpleServiceImpl.class);

// run the server
rpcServer.run();
```

### 4. RPC by the Client

**Synchronous calls**

```java
// create a client
RpcClient rpcClient = new RpcClient("127.0.0.1", 8080);

// specify the idle time of a connection
// RpcClient rpcClient = new RpcClient(HOST, PORT, 2000L);

// discover the service
SimpleServiceImpl simpleService = rpcClient.syncService(SimpleServiceImpl.class);

// synchronous RPC
String helloMsg = simpleService.hello(new Person());

// synchronous RPC with timeout
String helloMsg = simpleService.hello(new Person());

// shutdown the client
rpcClient.shutDown();
```

**Asynchronous calls and synchronous calls**

```java
// discover the service
RpcService rpcService = rpcClient.asyncService(SimpleServiceImpl.class);

// get a future instance as the result of the asynchronous call
RpcFuture rpcFuture = rpcService.asyncCall("hello", new Person());

// get the RPC result synchronously
rpcFuture.get();

// get the RPC result immediately
rpcFuture.getNow();

// add a Rpc hook
rpcFuture.addRpcHook(new RpcHook() {
    @Override
    public void beforeRpc(RpcRequest rpcRequest) {
        /* ... */
    }

    @Override
    public void afterRpc(RpcResponse rpcResponse) {
        /* ... */
    }
});
```



## Next Steps

- Enable annotation-driven development
- Enable other serialization/deserialization techniques
- Enable auto-reconnection
- Enable service-registry and service-discovery with Zookeeper
- Enable integration into Spring framework

