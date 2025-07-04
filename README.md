# Project structure
todo : update tree
![](.github/tree.png)
![](.github/tree2.png)

## Framework - Spring - GOAT
### Spring-boot Spring-data, Spring-security - GOAT
![SpringBOOT](.github/SpringBOOT.gif)

### config
Contains configuration files for different aspects of the project such as spring-security, Encryption, Environment variables
etc. They have bean definitions present for the beans used within the project.

### controller
Handles incoming HTTP requests and processes them. Ideally they do minimal HTTP related processing, getting inputs etc..
and delegate the business logic to the service layer. All controllers used here are REST controllers (@RestController, they serve json)
as opposed to the @Controller that can return a view (some html template).

### dto
Contains classes for Data Transfer Objects. These DTOs are used to send data to the client instead of model to ensure
we are not exposing the internal structures to the client (for safety).

### exceptions
Has custom exceptions and a global exception handler to send end appropriate responses to client on some exceptions.

### model
Contains classes used for mostly DB mappings but can serve other purposes also. DB mapped classes should not be exposed
to client (good practice + safety).

### repository
This layer is directly responsible for communication with the database. In this project we are using spring-data-mongodb. 
It holds interfaces that extend MongoRepository (as we are using mongodb, for sql those would extend JPARepository). In
those we declare functions to for getting data from DB which are implemented for us by spring automatically, making DB
access easy. This is not the only way for getting data as we can also use the MongoTemplate bean to create queries by
ourselves instead.

### security
Contains a util class for AES. It is used for encrypting pagination cursor before sending to the client. Probably
not that necessary to encrypt the cursor but still chose to include it.

### service
Contains main business logic for our application.

### util
Contains utility classes for different functionalities.

## Glorified HashMap.. Uh.. I mean DATABASE
Using Mongodb Atlas for the prod database saving us time setting up local mongodb and has pre-configured fault
tolerance related stuff (Setting up locally is also simple anyways [have local setup in my pc]).

### Local testing
Have a separate local DB for local testing, as we are using transactions for mongodb, it will not work with "out of the box"
local mongodb configuration - Hence added a replica set to local mongodb

#### procedure for adding replica-set locally
Adding replica-set locally allowed me to use mongodb transactions. It also significantly sped-up file upload and deletion 
in local-testing helping me test faster (apart from in code parallelization I did).

##### Better do with a clean new mongodb setup to save hassle (or) delete curr stuff and do setup from start. If local mongodb data is needed, backup data (lookup on internet) and do these steps.
Stop mongod (mongo daemon)
    
    sudo systemctl stop mongod

Edit config file - /etc/mongod.conf -> add the following and save it

    replication:
        replSetName: "rs0"

Start mongod and verify it is active 

    sudo systemctl start mongod
    sudo systemctl status mongod

Goto mongosh (mongoshell) and initialize replica-set for the DB

    mongosh # below commands are to be typed in the mongo shell

        use <DB-name> # mine was WhatsappParserLocal so - use WhatsappParserLocal
        rs.initiate()
        rs.status() # verify that it initiated properly

# Notes and stuff
## Made chat inserts into DB parallel
We were putting data into DB in bulk, using BulkOperations.BulkMode.ORDERED before but we don't need to actually put 
them in ordered was, as later we will get them back based on timestamp. So can use UNORDERED way of inserting. The advantage
of UNORDERED is that MongoDB can execute the operations in parallel. We are keeping track of the counts for number of chats
inserted using AtomicInteger variable. We can throw an exception if total inserts is not equal to the total
number of chats present. Then for the bulk operations too, we can put chunks instead of all at once. Those chunks can be inserted into DB
in an async way. For this I'm using CompletableFuture.runAsync to put them as chunks into DB. All this sped up the 
file upload considerably.

### metrics
- sequential inserts : each taking a good while, sometimes even hitting 13 seconds. Below figure shows sequential inserts and their time taken
![](.github/metrics_chatIns_seq.png)
- concurrent, with batch size as 1000 chats : straight up we see nearly 50% improvement on average. 
![](.github/metrics_chatInsParallel_1000.png)
- concurrent, with batch size as 100 chats : nearly similar to batch size as 1000 but this will be useful for more files as not every file will have 1000s of chats. Maybe this is not good as the app scales as the batch size is kinda small, I can try to look into that later if it causes issues and perhaps tune to a good middle-ground.
![](.github/metrics_chatInsParallel_100.png)

## Loose benchmarks 

    $ apt-get install apache2-utils
    $ ab -n 10000 -c 10 http://localhost:8080/actuator/metrics

# Statistics
![](.github/cloc.png)

# Thanks
- [Genuine coder](https://www.youtube.com/@GenuineCoder) - Great for learning spring, also features some other stuff.
- [cloc util](https://github.com/AlDanial/cloc) - Used in this file to Count Lines Of Code
- [Literally SpringBOOT](https://tenor.com/view/goofy-shoes-gif-26687557)