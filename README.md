# Masters Of Renaissance - Project by Dottor, Corti, Cutrupi
Masters of Renaissance is the final test of "Software Engineering", course of "Computer Science Engineering" held at Politecnico di Milano (2020/2021).

Teacher: *Alessandro Margara*

Final Score: **30L/30**

# Project specification
The project consists of a Java version of the board game Masters of Renaissance, made by Cranio.

The final version includes:

-initial UML diagram (@ingswAM2021-Dottor-Corti-Cutrupi/documents);

-sequence diagram for the main actions of the game (@ingswAM2021-Dottor-Corti-Cutrupi/documents)

-final UML diagram, generated from the code by automated tools;

-working game implementation, which has to be rules compliant;

-source code of the implementation;

-source code of unity tests.

# Implemented Functionalities

-Basic rules

-Complete rules

-CLI

-Socket

-GUI

-Multiple games

-Parameters editor

-Disconnection resilience

# Tests
The model part of the project has been tested using JUNIT and it reached a percentage of 100% class tested, 91% methods tested and 75% line tested (most of the methods and lines that are not tested regard json files so it was hard to test them with JUNIT). 
Everything else wasn't tested with JUNIT because it required connection between a server and a client, but it was properly tested playing multiple games.


# JAR
The directory for the executable application is ingswAM2021-Dottor-Corti-Cutrupi/ingswAM2021-Dottor-Corti-Cutrupi/shade/AM41.jar
To run it, you have to use cmd shell (from windows) and type: java -jar AM41.jar

At the moment parameter editor GUI has some problems working completely by jar since it tries to write on a json file internally, which is not consented. We are working on fixing the problem and hopefully it will be solved soon. Anyway the parameter editor application works completely if run with IDE.
