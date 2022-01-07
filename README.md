# spring-microservices-practice
## 1. Create Customer Service project
### 1.1 Created application using start.spring.io’s REST API and HTTPie.

    http https://start.spring.io/starter.zip bootVersion==2.6.2 \
       groupId==com.github.zhangwei1979 \
       packageName==com.github.zhangwei1979.customerservice \
       artifactId==customer-service name==customer-service baseDir==customer-service \
       dependencies==web,cloud-eureka | tar -xzvf -

### 1.2 Create a Customer class to use as our domain object.

    public class Customer {
        private final int id;
        private final String name;
        //getter and setter ...
    }
### 1.3 Create CustomerController
    @RestController
    public class CustomerController {
    private List<Customer> customers = Arrays.asList(
    new Customer(1, "Joe Bloggs"),
    new Customer(2, "Jane Doe"));
    
        @GetMapping
        public List<Customer> getAllCustomers() {
            return customers;
        }
        
        @GetMapping("/{id}")
        public Customer getCustomerById(@PathVariable int id) {
            return customers.stream()
                            .filter(customer -> customer.getId() == id)
                            .findFirst()
                            .orElseThrow(IllegalArgumentException::new);
        }
    }
### 1.4 Add the following to the src/main/resources/application.properties file
    spring.application.name=customer-service
    server.port=3001
### 1.5 Run and verify the project
    mvn spring-boot:run
Visiting http://localhost:3001 should display a list of customers represented in JSON. Open http://localhost:3001/1 to see Joe Bloggs or http://localhost:3001/2 to see Jane Doe as individual data items.

## 2. Create Order Service project
### 2.1 Create application
    http https://start.spring.io/starter.zip bootVersion==2.6.2 \
    groupId==com.github.zhangwei1979 \
    packageName==com.github.zhangwei1979.orderservice \
    artifactId==order-service name==order-service baseDir==order-service \
    dependencies==web,cloud-eureka | tar -xzvf -
### 2.2 Create Order domain
    public class Order {
        private final int id;
        private final int customerId;
        private final String name;
    
        public Order(final int id, final int customerId, final String name) {
            this.id = id;
            this.customerId = customerId;
            this.name = name;
        }
        
        //getter and setter ...
    }
### 2.3 Create an OrderController
    @RestController
    public class OrderController {
        private final List<Order> orders = Arrays.asList(
        new Order(1, 1, "Product A"),
        new Order(2, 1, "Product B"),
        new Order(3, 2, "Product C"),
        new Order(4, 1, "Product D"),
        new Order(5, 2, "Product E"));
    
        @GetMapping
        public List<Order> getAllOrders() {
            return orders;
        }
    
        @GetMapping("/{id}")
        public Order getOrderById(@PathVariable int id) {
            return orders.stream()
                         .filter(order -> order.getId() == id)
                         .findFirst()
                         .orElseThrow(IllegalArgumentException::new);
        }
    }
### 2.4 Update the application.properties
    spring.application.name=order-service
    server.port=3002
### 2.5 Run and verify the application
Visiting http://localhost:3002
## 3. Create Service Discovery project
### 3.1 Create application
    http https://start.spring.io/starter.zip bootVersion==2.6.2 \
    groupId==com.github.zhangwei1979 \
    packageName==com.github.zhangwei1979.discoveryservice \
    artifactId==discovery-service name==discovery-service baseDir==discovery-service \
    dependencies==cloud-eureka-server | tar -xzvf -
### 3.2 Add the @EnableEurekaServer annotation
Open up the DiscoveryServiceApplication class and add the @EnableEurekaServer annotation, to stand up a Eureka service registry:
    
    @SpringBootApplication
    @EnableEurekaServer
    public class DiscoveryServiceApplication {
        public static void main(String[] args) {
            SpringApplication.run(DiscoveryServiceApplication.class, args);
        }
    }

In application.properties add the following:

    spring.application.name=discovery-service
    server.port=3000
    eureka.client.registerWithEureka=false
    eureka.client.fetchRegistry=false
    eureka.instance.hostname=localhost
    eureka.client.serviceUrl.defaultZone=http://${eureka.instance.hostname}:${server.port}/eureka/
### 3.3 Run and verify
Visiting http://localhost:3000. . You should see a Eureka dashboard which displays information about the running instance
### 3.4 Registering with the Discovery Service
We’ll need to add the Eureka Discovery Client dependency to Order Service project and Customer Service Project. 

Annotate our CustomerServiceApplication and OrderServiceApplication classes with the @EnableEurekaClient annotation:
    
    @SpringBootApplication
    @EnableEurekaClient
    public class CustomerServiceApplication {
        public static void main(String[] args) {
            SpringApplication.run(CustomerServiceApplication.class, args);
        }
    }

Finally, let’s tell the Eureka Client where to find our Discovery Service. In each service’s application.properties, add the following line:
    
    eureka.client.serviceUrl.defaultZone=http://localhost:3000/eureka/

## 4. Routing and Server-Side Load Balancing with Spring Cloud Gateway
### 4.1 Create application
    http https://start.spring.io/starter.zip bootVersion==2.6.2 \
    groupId==com.github.zhangwei1979 \
    packageName==com.github.zhangwei1979.gatewayservice \
    artifactId==gateway-service name==gateway-service baseDir==gateway-service \
    dependencies==cloud-gateway,cloud-eureka | tar -xzvf -
### 4.2 Modify the GatewayApplication to add the EnableEurekaClient annotation
    @SpringBootApplication
    @EnableEurekaClient
    public class GatewayServiceApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(GatewayServiceApplication.class, args);
        }
    
    }

### 4.3 Define the application.yml as follows-

    server:
      port: 80
     
    eureka:
      client:
        serviceUrl:
          defaultZone: http://localhost:3000/eureka/

    spring:
      application:
        name: gateway-service
      cloud:
        gateway:
          routes:
          - id: customer-service
            uri: lb://CUSTOMER-SERVICE
            predicates:
            - Path=/customers/**
            filters:
             - RewritePath=/customers(?<segment>/?.*), /$\{segment}
          - id: order-service
            uri: lb://ORDER-SERVICE
            predicates:
            - Path=/orders/**
            filters:
             - RewritePath=/orders(?<segment>/?.*), /$\{segment}
### 4.4 Run and verify

Visiting 
http://localhost/customers
http://localhost/orders

## 5. Inter-Service Communication
### 5.1 Modifying the getAllOrders method of the OrderController
add the ability to filter orders by an optional customer ID in the query string:

    @GetMapping
    public List<Order> getAllOrders(@RequestParam(required = false) Integer customerId) {
        if (customerId != null) {
            return orders.stream()
                .filter(order -> customerId.equals(order.getCustomerId()))
                .collect(Collectors.toList());
        }
    
        return orders;
    }
Visit http://localhost:3002/?customerId=2 to see all of Jane Doe’s orders.
### 5.2 Add the Spring Cloud OpenFeign dependency to the Customer Service
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
### 5.3 Add the @EnableFeignClients annotation to the CustomerServiceApplication class
    @SpringBootApplication
    @EnableEurekaClient
    @EnableFeignClients
    public class CustomerServiceApplication {
        public static void main(String[] args) {
            SpringApplication.run(CustomerServiceApplication.class, args);
        }
    }
### 5.4 Create a Feign client to communicate with the Order Service
declaring a method called getOrdersForCustomer

    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RequestParam;
    
    import java.util.List;
    
    @FeignClient(name = "order-service")
    public interface OrderClient {
        @GetMapping("/")
        Object getOrdersForCustomer(@RequestParam int customerId);
    }
### 5.5 Inject OrderClient into the CustomerController:
    @RestController
    public class CustomerController {
        private List<Customer> customers = Arrays.asList(
        new Customer(1, "Joe Bloggs"),
        new Customer(2, "Jane Doe"));
    
        private OrderClient orderClient;
    
        public CustomerController(OrderClient orderClient) {
            this.orderClient = orderClient;
        }
### 5.6 And create a new method to handle GET /{id}/orders
    @GetMapping("/{id}/orders")
    public Object getOrdersForCustomer(@PathVariable int id) {
        return orderClient.getOrdersForCustomer(id);
    }
### 5.7 Verify
Visit http://localhost/customers/2/orders, You should see the same list of orders belonging to Jane Doe

## 6. Client-Side Load Balancing
### 6.1 Modify the Order Service 
add the port number on which the service is currently running to the body of the response to prove that load-balancing is enabled.
Create a ResponseWrapper class:

    public class ResponseWrapper<T> {
    private final Integer port;
    private final T data;
    
        public ResponseWrapper(final Environment environment, final T data) {
            String serverPort = environment.getProperty("server.port");
            this.port = serverPort != null ? Integer.parseInt(serverPort) : null;
            this.data = data;
        }
    
        public Integer getPort() {
            return port;
        }
    
        public T getData() {
            return data;
        }
    }

Then inject the Spring Environment into the OrderController:

    @RestController
    public class OrderController {
        private final List<Order> orders = Arrays.asList(
        new Order(1, 1, "Product A"),
        new Order(2, 1, "Product B"),
        new Order(3, 2, "Product C"),
        new Order(4, 1, "Product D"),
        new Order(5, 2, "Product E"));
    
        private final Environment environment;
    
        @Autowired
        public OrderController(final Environment environment) {
            this.environment = environment;
        }
And change the getAllOrders method to return a List<Order> wrapped in a ResponseWrapper:

    @GetMapping
    public ResponseWrapper<List<Order>> getAllOrders(@RequestParam(required = false) Integer customerId) {
        if (customerId != null) {
            return new ResponseWrapper<>(
                environment,
                orders.stream()
                    .filter(order -> customerId.equals(order.getCustomerId()))
                    .collect(Collectors.toList()));
        }
    
        return new ResponseWrapper<>(environment, orders);
    }
###6.2 Launch multiple instances of the Order Service on different ports
    java -jar target/order-service-0.0.1-SNAPSHOT.jar --server.port=3003
    java -jar target/order-service-0.0.1-SNAPSHOT.jar --server.port=3004
You should see multiple instances of the Order Service
###6.3 Make a few GET requests to http://localhost/customers/2/orders 
and take note of the port in the response. It should change each time.
##7. Resiliency
Todo: should use Resilience4J but haven't found a good tutorial