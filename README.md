# SOEN342 — Train Planner System section II

## Overview

This project simulates a simplified **Train Route Planner** system that allows travelers to search for direct and indirect train connections between cities using data from a CSV schedule file.  
It supports searching by filters such as departure time, arrival city, and times.

All **UML diagrams** for this project are available in the **Wiki** section under **"Diagrams"**.

---

## How to Run

1. **Open the project** in your IDE or terminal.
2. Navigate to the `app` package and run the file:
3. Make sure to always click option "1" to load the file

## Team members

- Rita Kardachian 40283698 (team leader)
- Nayla Nocera 40283927
- Ava Samimi 40048117

## System booking policies

1.  Reasonable layover times:

Itenraries with layovers longer than 2h after 22:00 till 6:00 are not suggested by the system. Meanwhile, itenraries with layovers no longer than 30 mins during daytime are allowed.

2. Too slow and unreasonable comparet to direct:

If there is a direct train between A and B, then an indirect itinerary is only allowed/suggested if its total travel time is at most 3 hours longer than the best direct option.

## Instructions 

To run the app, use AppCLI.java which is the updated version of Main.java which was hardcoded for small testing. 

Run the following commands:

1.

```java
   javac -cp "lib/*:src" -d out $(find src -name "*.java")
```
2.  

```java
   java -cp "out:lib/*" app.SeedDbMain
```
3. 
```java
   java -cp "out:lib/*" app.AppCLI
```
