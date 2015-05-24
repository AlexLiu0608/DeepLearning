Deeplearning4J: Neural Net Platform
=========================
 
[![Join the chat at https://gitter.im/deeplearning4j/deeplearning4j](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/deeplearning4j/deeplearning4j?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Deeplearning4J is an Apache 2.0-licensed, open-source, distributed neural net library written in Java and Scala.

Deeplearning4J integrates with Hadoop and Spark and runs on several backends that enable use of CPUs and GPus. The aim is to create a plug-and-play solution that is more convention than configuration, and which allows for fast prototyping. 

---
## Main Features
- Versatile n-dimensional array class
- GPU integration
- Scalable on Hadoop, Spark and Akka + AWS et al

---
## Modules
- cli = command line interface for deeplearning4j
- core = core neural net structures and supporting components such as datasets, iterators, clustoring algorithms, optimization methods, evaluation tools and plots.
- scaleout = integrations
    - aws = loading data to and from aws resources EC2 and S3
    - nlp = natural language processing components including vecotrizers, models, sample datasets and renders
    - akka = setup concurrent and distributed applications on the JVM
    - api = core components like workers and mult-threading
    - zookeeper = maintain configuration for distributed systems
    - hadoop-yarn = common map-reduce distributed system
    - spark = integration with spark
- ui = provides visual interfaces with models like nearest neighbors
- test-resources = datasets and supporting components for tests

---
## Documentation
Documentation is available at [deeplearning4j.org](http://deeplearning4j.org/) and [JavaDocs](http://deeplearning4j.org/doc/).

---
## Installation
To install Deeplearning4J, there are a couple approaches (briefly described below). More information can be found on the  [ND4J website](http://nd4j.org/getstarted.html).

#### Use Maven Central Repository

    Search for [deeplearning4j](https://search.maven.org/#search%7Cga%7C1%7Cdeeplearning4j) to get a list of jars you can use

    Add the dependency information into your pom.xml

#### Get the Code
Deeplearning4J is being actively developed and you can clone the repository, compile it and reference it in your project.

Clone the repository:

    $ git clone git://github.com/deeplearning4j/deeplearning4j.git

Compile the project:

    $ cd deeplearning4j && mvn clean install -DskipTests -Dmaven.javadoc.skip=true

Add the local compiled file dependencies to your pom.xml file. Here's an example of what they'll look like:

    <dependency>
        <groupId>org.deeplearning4j</groupId>
        <artifactId>deeplearning4j-cli</artifactId>
        <version>0.0.3.3.3.alpha1-SNAPSHOT</version>
    </dependency>

#### Load RPM (*coming soon*)
Download the Red Hat Package Management (RPM) file and run the following command:

    $ sudo rpm -iv [package.rpm]

---
## Contribute
1. Check for open issues or open a fresh one to start a discussion around a feature idea or a bug. 
2. If you feel uncomfortable or uncertain about an issue or your changes, don't hesitate to contact us on Gitter using the link above.
3. Fork [the repository](https://github.com/deeplearning4j/deeplearning4j.git) on GitHub to start making your changes to the **master** branch (or branch off of it).
4. Write a test which shows that the bug was fixed or that the feature works as expected.
5. Send a pull request and bug us on Gitter until it gets merged and published. :)
