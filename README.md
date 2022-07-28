## Folder Structure

The workspace contains two folders, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder.

## Dependency Management

    To execute the JSON utils, json-20210307.jar was added to the Reference Libraries under VS Code


## Steps to execute
    location_simulator\src\Location_Simulator_App.java
    Execute Location_Simulator_App

## Input Format
    Enter Source Latitude
    12.93175
    Enter Source Longitude
    77.62872
    Enter Destination Latitude
    12.93402
    Enter Destination Longitude
    77.61429

## Output Format
12.93166,77.62852,blue,marker
12.93203,77.62826,blue,marker
12.93239,77.62798,blue,marker
12.93276,77.62773,blue,marker
12.93311,77.62745,blue,marker
12.93344,77.62714,blue,marker

## Verificaton
 The output is formatted in a way that it can be directly pasted into https://mobisoftinfotech.com/tools/plot-multiple-points-on-map/
 and visualize the output


## Next Steps
    Right now in this implementation due to time contraint i decoded "overview_polyline" and build the logic around it.
    But it might be accurate in few places, to get more better accuracy i am planning to decode "polyline" under each "steps" and then
    plot the route.

## Test cases (Screenshots provided under the folder "tests screenshots")
1.  Provided Test Case
    12.93175
    77.62872
    12.92662
    77.63696

2. My Apartment to Locus office
   12.97059
   77.69202
   12.92574
   77.63673

3. Curvy Ghat Section to test curves - Charmadi Ghat to Charmadi Falls
   13.09211
   75.47874
   13.12542
   75.49536

4. Source same as provided to truffles as destination
    12.93175
    77.62872
    12.93402 
    77.61429

5. Clarence High School to golf course
    13.00272
    77.61580
    12.98683
    77.58871
