//  AbYCSSStudy.java
//
//  Authors:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.experiments.studies;

import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;
import jmetal.experiments.settings.*;
import jmetal.experiments.util.Friedman;
import jmetal.util.JMException;
import jmetal.problems.SSpamProblem2D;
import jmetal.metaheuristics.abycss.AbYCSS;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.core.SpamAssassinRules;
/**
 * Class implementing a typical experimental study. Five algorithms are 
 * compared when solving the ZDT, DTLZ, and WFG benchmarks, and the hypervolume,
 * spread and additive epsilon indicators are used for performance assessment.
 */
public class AbYCSSStudy extends Experiment {

  /**
   * Configures the algorithms in each independent run
   * @param problemName The problem to solve
   * @param problemIndex
   * @throws ClassNotFoundException 
   */
  public void algorithmSettings(String problemName, 
  		                          int problemIndex, 
  		                          Algorithm[] algorithm) throws ClassNotFoundException {
    try {
      int numberOfAlgorithms = algorithmNameList_.length;

      HashMap[] parameters = new HashMap[numberOfAlgorithms];

      for (int i = 0; i < numberOfAlgorithms; i++) {
        parameters[i] = new HashMap();
      } // for

      if (!paretoFrontFile_[problemIndex].equals("")) {
        for (int i = 0; i < numberOfAlgorithms; i++)
          parameters[i].put("paretoFrontFile_", paretoFrontFile_[problemIndex]);
        } // if

        //algorithm[0] = new NSGAII_Settings(problemName).configure(parameters[0]);
        algorithm[0] = new NSGAIIPermutation_Settings(problemName).configure(parameters[0]);
        //algorithm[0] = new AbYCSS_Settings(problemName).configure(parameters[0]);
        //algorithm[1] = new AbYSS_Settings(problemName).configure(parameters[1]);
        //algorithm[1] = new SPEA2_Settings(problemName).configure(parameters[1]);
        //algorithm[2] = new MOCell_Settings(problemName).configure(parameters[2]);
        //algorithm[3] = new SMPSO_Settings(problemName).configure(parameters[3]);
        //algorithm[4] = new GDE3_Settings(problemName).configure(parameters[4]);
      } catch (IllegalArgumentException ex) {
      Logger.getLogger(AbYCSSStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(AbYCSSStudy.class.getName()).log(Level.SEVERE, null, ex);
    } catch  (JMException ex) {
      Logger.getLogger(AbYCSSStudy.class.getName()).log(Level.SEVERE, null, ex);
    }
  } // algorithmSettings

  /**
   * Main method
   * @param args
   * @throws JMException
   * @throws IOException
   */
  public static void main(String[] args) throws JMException, IOException {
    AbYCSSStudy exp = new AbYCSSStudy();

    exp.experimentName_ = "AbYCSSStudy";
    exp.algorithmNameList_ = new String[]{
                                "AbYCSS"};//, "AbYSS"};
                                //, "SPEA2", "MOCell", "SMPSO", "GDE3"};
    exp.problemList_ = new String[]{"SSpamProblem2D"};
								//, "ZDT2","ZDT3", "ZDT4","ZDT6",
                                //    "WFG1","WFG2","WFG3","WFG4","WFG5","WFG6",
                                //    "WFG7","WFG8","WFG9",
                                //    "DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
                                //    "DTLZ6","DTLZ7"};
    exp.paretoFrontFile_ = new String[]{"SSpamProblem2D.pf"}; /*, "ZDT2.pf","ZDT3.pf",
                                    "ZDT4.pf","ZDT6.pf",
                                    "WFG1.2D.pf","WFG2.2D.pf","WFG3.2D.pf",
                                    "WFG4.2D.pf","WFG5.2D.pf","WFG6.2D.pf",
                                    "WFG7.2D.pf","WFG8.2D.pf","WFG9.2D.pf",
                                    "DTLZ1.2D.pf","DTLZ2.2D.pf","DTLZ3.2D.pf",
                                    "DTLZ4.2D.pf","DTLZ5.2D.pf","DTLZ6.2D.pf",
                                    "DTLZ7.2D.pf"};*/

    exp.indicatorList_ = new String[]{"HV", "SPREAD", "EPSILON"};

    int numberOfAlgorithms = exp.algorithmNameList_.length;

    exp.experimentBaseDirectory_ = //"/Users/tiagoinacio/ownCloud/java/jmetal/" +
									"./" +
                                   exp.experimentName_;
    exp.paretoFrontDirectory_ = //"//Users/tiagoinacio/ownCloud/java/jmetal/paretoFronts";
									"./";

    exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

    exp.independentRuns_ = 5;

    exp.initExperiment();

    // Run the experiments
    int numberOfThreads ;
    exp.runExperiment(numberOfThreads = 2) ;
    
    //tiago
    //exp.generateQualityIndicators() ;

    // Generate latex tables
    //tiago
    //exp.generateLatexTables() ;

    // Configure the R scripts to be generated
    int rows  ;
    int columns  ;
    String prefix ;
    String [] problems ;
    boolean notch ;

    // Configuring scripts for ZDT
    rows = 3 ;
    columns = 2 ;
    prefix = new String("ZDT");
    problems = new String[]{"ZDT1", "ZDT2","ZDT3", "ZDT4","ZDT6"} ;
    
    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = false, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;

    // Configure scripts for DTLZ
    rows = 3 ;
    columns = 3 ;
    prefix = new String("DTLZ");
    problems = new String[]{"DTLZ1","DTLZ2","DTLZ3","DTLZ4","DTLZ5",
                                    "DTLZ6","DTLZ7"} ;

    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch=false, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;

    // Configure scripts for WFG
    rows = 3 ;
    columns = 3 ;
    prefix = new String("WFG");
    problems = new String[]{"WFG1","WFG2","WFG3","WFG4","WFG5","WFG6",
                            "WFG7","WFG8","WFG9"} ;

    exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch=false, exp) ;
    exp.generateRWilcoxonScripts(problems, prefix, exp) ;

    // Applying Friedman test
    // tiago
    /*Friedman test = new Friedman(exp);
    test.executeTest("EPSILON");
    test.executeTest("HV");
    test.executeTest("SPREAD");
    */
  } // main
} // AbYCSSStudy



