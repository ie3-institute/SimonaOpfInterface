# SimonaOpfInterface

This repository is to be used in combination with SIMONA and the SIMONA API. The relevant branches can be found in the development repository [simonaAPI_dev](https://github.com/ie3-institute/simonaAPI_dev). It simulates a cost-based DC-optimization in the distribution grid (e.g. redispatch optimization) and checks if the calculated real power setpoints are physically feasible by using these setpoints for the SIMONA power flow calculation.

## Basic Functionality
The simonaOpfInterface establishes a connection to Matpower using a Matlab Engine JAR. The custom Matpower function “dcopf” is called with the parameter “gridname”. This function returns the setpoints for all generators in the grid. The SimonaAPI handles the connection between SIMONA and the simonaOpfInterface so that the setpoints are used in the correct way. The setpoints are then handed over to the generators’ agents in SIMONA as primary data for the power flow calculation. 

## Preliminary setup
Mainly three steps need to be set up, in order to  use the DC-OPF Interface with the exemplary grid:
1. downloading and installing the Matlab Engine 
2. creating shadow jar for external repository 
3. save grid information for Matlab

### Matlab Engine

The Matlab Engine JAR is used to run Matlab code from a Java application. It is available in the Matlab shop and has to be placed in the simonaOpfInterface repositoryYou can find information on how to install and use the engine [here](https://www.mathworks.com/help/matlab/matlab_external/get-started-with-matlab-engine-api-for-java.html)

### Shadow Jar

The simulation itself is processed in the SIMONA repository. Both, the SIMONA API and the simonaOpfInterface need to be added as shadow jar to the SIMONA dependencie(See also other doc) In order for SIMONA to recognize the use of an external simulation, the shadow jar of the simonaOpfInterface needs to be placed inside the input folder (\input\ext_sim). 

### Example grid
Besides the grid input data, SIMONA uses for the simulation, the grid needs to be available for Matlab to load. Since the whole simulation is run in the SIMONA repository and not the simonaOpfInterface repository, the grid needs to be placed in this directory. The exemplary grid is called “semiurb4.mat” and is called inside the simonaOpfInterface. The grid’s name is not yet implemented as a variable and needs to changed inside the code, if another grid is to be used. 

When everything is set up, the SIMONA simulation is startet just like a run without an external simulation. See whole documentation in the [SIMONA Docs](https://simona.readthedocs.io/en/latest/)