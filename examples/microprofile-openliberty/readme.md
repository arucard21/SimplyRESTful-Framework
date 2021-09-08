# MicroProfile generated Application

## Introduction

MicroProfile Starter has generated this MicroProfile application for you.

The generation of the executable jar file can be performed by issuing the following command



    ./gradlew libertyPackage


This will create an executable jar file **microprofile-openliberty.jar** within the _build/libs_ gradle folder. This can be started by executing the following command


    ./gradlew libertyRun --no-daemon


### Liberty Dev Mode

During development, you can use Liberty's development mode (dev mode) to code while observing and testing your changes on the fly.
With the dev mode, you can code along and watch the change reflected in the running server right away; 
unit and integration tests are run on pressing Enter in the command terminal; you can attach a debugger to the running server at any time to step through your code.




    ./gradlew libertyDev

To stop Liberty running in the background, use:

    ./gradlew libertyStop



To launch the test page, open your browser at the following URL

    http://localhost:9080/index.html  



## Specification examples

By default, there is always the creation of a JAX-RS application class to define the path on which the JAX-RS endpoints are available.

Also, a simple Hello world endpoint is created, have a look at the class **HelloController**.

More information on MicroProfile can be found [here](https://microprofile.io/)


