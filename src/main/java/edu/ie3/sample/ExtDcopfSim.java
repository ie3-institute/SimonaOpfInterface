package edu.ie3.sample;

import ch.qos.logback.classic.Logger;
import com.mathworks.engine.EngineException;
import edu.ie3.datamodel.models.value.PValue;
import edu.ie3.simona.api.data.dcopf.ExtOpfData;
import edu.ie3.simona.api.data.dcopf.ExtOpfSimulation;
import edu.ie3.simona.api.data.dcopf.ontology.SetpointsMessage;
import edu.ie3.simona.api.data.dcopf.ontology.builder.SetpointsMessageBuilder;
import edu.ie3.simona.api.simulation.ExtSimulation;
import com.mathworks.engine.MatlabEngine;
import edu.ie3.util.quantities.PowerSystemUnits;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutionException;

import tech.units.indriya.quantity.Quantities;


public class ExtDcopfSim extends ExtSimulation implements ExtOpfSimulation {
    private final Logger log = (Logger) LoggerFactory.getLogger("ExternalSampleSim");

    private static MatlabEngine eng;
    private ExtOpfData opfData;

    private HashMap<Integer, UUID> generatorsMp2Simona;
    private String path = "input/samples/vn_simona/fullGrid/fixed_feed_in_input.csv";
    private String gridname = "semiurb4.mat";


    @Override
    public void setExtOpfData(ExtOpfData opfData) {this.opfData = opfData;}

    @Override
    protected List<Long> initialize() {
        log.info("Main args handed over to external simulation: {}", Arrays.toString(getMainArgs()));

        //TODO: read in path to csv files
        this.generatorsMp2Simona = mapMp2Simona(this.path);
        // TODO: read SimBench grid name

        ArrayList<Long> newTicks = new ArrayList<>();
        newTicks.add(0L);
        return newTicks;
    }

    @Override
    protected List<Long> doActivity(long tick) {
        log.info("External DCOPF Simulation: Tick {} has been triggered.", tick);

        // start Matlab and call Matpower function
        double[][]results = callMatpower(this.gridname);

        // transform Double into Comparable Quantity PValue
        ArrayList<PValue> activePower = new ArrayList<PValue>();
        for (int i=0;i<results.length;i++){
            activePower.add(toPValue(results[i][1]));
        }

        // Map setpoints to generators' UUID
        HashMap<Integer, UUID> generatorsMp2Simona = this.generatorsMp2Simona;
        HashMap<UUID, PValue> setpoints = new HashMap<UUID, PValue>();

        for(int i=0;i<activePower.size();i++){
            setpoints.put(generatorsMp2Simona.get(i), activePower.get(i));
        }

        SetpointsMessage setpointsMessage = new SetpointsMessage(setpoints);

        log.debug("Sending active power setpoints to SIMONA: {}", setpoints);
        opfData.sendSetpoints(setpointsMessage);

        // return triggers for next activity
        ArrayList<Long> newTicks = new ArrayList<>();
        newTicks.add(tick + 900);
        log.info("Sending next ticks to SIMONA: {}", newTicks);
        return newTicks;
    }

    public double[][] callMatpower(String mpc) throws ExecutionException, InterruptedException {
        //start Matlab
        eng = MatlabEngine.startMatlab();

        //call custom Matpower function "dcopf"
        double[][]results = eng.feval("dcopf", mpc);

        return results;
    }

    public HashMap<Integer, UUID> mapMp2Simona(String path){
        // read information from fixedfeedin file
        int index_uuid = 0;
        int index_bus = 2;

        ArrayList<UUID> uuid_simona = csvreader(path, index_uuid);
        ArrayList<String> bus_matpower = csvreader(path, index_bus);

        // remove first line, because this is the not a generator but the superior grid
        uuid_simona.remove(0);
        bus_matpower.remove(0);

        // remove irrelevant information from bus_matpower and convert into integer ArrayList
        ArrayList<Integer> bus_mp = new ArrayList<Integer>();

        for(int i=0;i<bus_matpower.size();i++){
            String str = bus_matpower.get(i).substring(13,14);            // bis zum Ende: bus_matpower.get(i).length()
            bus_mp.add(Integer.parseInt(str));
        }

        // Create Hashmap
        HashMap<Integer, UUID> mp2simona = new HashMap<Integer, UUID>();

        for(int i=0;i<bus_mp.size();i++){
            mp2simona.put(bus_mp.get(i), uuid_simona.get(i));
        }

        return mp2simona;
    }

    public PValue toPValue (Double setpoint){
        PValue power = new PValue(Quantities.getQuantity(setpoint, PowerSystemUnits.KILOWATT));
        return power;
    }

    public static ArrayList<String> csvreader(String path, int index, Type T){

        String line = "";
        ArrayList<> output = new ArrayList<T>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            while((line = br.readLine()) != null) {
                Type[] values = line.split(";");
                output.add(values[index]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return output;
    }




}
