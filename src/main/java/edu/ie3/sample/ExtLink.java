/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */

package edu.ie3.sample;

import edu.ie3.simona.api.ExtLinkInterface;
import edu.ie3.simona.api.data.ExtDataSimulation;
import edu.ie3.simona.api.simulation.ExtSimulation;
import java.util.ArrayList;
import java.util.List;

public class ExtLink implements ExtLinkInterface {
  //private final ExternalSampleSim sampleSim = new ExternalSampleSim();
  private final ExtDcopfSim dcopfSim = new ExtDcopfSim();

  @Override
  public ExtSimulation getExtSimulation() {
    return dcopfSim;
  }

  @Override
  public List<ExtDataSimulation> getExtDataSimulations() {
    ArrayList<ExtDataSimulation> list = new ArrayList<>();
    list.add(dcopfSim);
    return list;
  }
}
