## Requirements
* Java 11
* Maven 3

## Usage
Tested on Ubuntu 20.04.

### Compile

    mvn compiler:compile

### Run

    mvn exec:java

Note: Press <Enter> to close the WebSocket listener and end the program.

### Run the tests

    mvn test

## Random topics
* Is multithreading a good idea, given the strict event ordering required?
* Gson is the fastest JSON parser, according to [this study](https://www.overops.com/blog/the-ultimate-json-library-json-simple-vs-gson-vs-jackson-vs-json/)
* ZonedDateTime, instead of local?
* The testing falls into the definition of "integration testing", yet it has proved the most efficient when parsing protocols.