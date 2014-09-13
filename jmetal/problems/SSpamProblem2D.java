//  Spam.java
//
//  Author:
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

package jmetal.problems;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.ArrayRealSolutionType;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.solutionType.ArrayRealAndBinarySolutionType;
import jmetal.encodings.variable.ArrayInt;
import jmetal.encodings.variable.ArrayReal;
import jmetal.encodings.variable.Binary;
import jmetal.encodings.variable.Int;
import jmetal.encodings.solutionType.PermutationSolutionType;
import jmetal.encodings.variable.Permutation;

import jmetal.util.Spea2Fitness;
import jmetal.util.JMException;
import java.sql.*;
import java.text.*;
import java.util.Properties;
import jmetal.core.ParseRules;
import jmetal.core.SpamAssassinRules;

/** 
 * Class representing problem SpamProblem
 */
public class SSpamProblem2D extends Problem {

  int numberOfRules = 900;
  int iteration = 1;
  SpamAssassinRules rules;
  public SSpamProblem2D(String solutionType) throws ClassNotFoundException, IOException {

    ParseRules parse = new ParseRules();
    this.rules = parse.getRules();
    
    numberOfVariables_ = 1; // vbasto
    numberOfObjectives_ = 2;
    numberOfConstraints_ = 0;
    problemName_ = "SSpamProblem2D";

    length_       = new int[numberOfVariables_];
    length_      [0] = this.numberOfRules;

    if (solutionType.compareTo("Permutation") == 0)
      solutionType_ = new PermutationSolutionType(this) ;
    else {
      System.out.println("Error: solution type " + solutionType + " invalid") ;
      System.exit(-1) ;
    }

  } //Spam


  /** 
   * Evaluates a solution 
   * @param solution The solution to evaluate
   * @throws JMException 
   */        
  public void evaluate(Solution solution) throws JMException {

    double scoreRequired = 50D;
    double totalScore = 0D;
    double totalTime = 0D;
    int executedRules = 0;
    int x ;
    try {
      
      PrintWriter writer = new PrintWriter(new FileOutputStream( new File("results/nsgaii/" + iteration + ".txt"), true));
      
      for (int i = 0; i < numberOfRules; i++) 
      {
        x = ((Permutation)solution.getDecisionVariables()[0]).vector_[i] ;
        if ( this.rules.getRule(x).getScore() > 99 || this.rules.getRule(x).getScore() < -30 ) continue;
        totalTime += this.rules.getRule(x).getCPU() + this.rules.getRule(x).getIO();
        writer.println(i + 1 + " [" + x + "] " + this.rules.getRule(x).getName() + " has a score of " + this.rules.getRule(x).getScore());

        totalScore += this.rules.getRule(x).getScore();
        if (totalScore > scoreRequired)
        {
          executedRules = i + 1; 
          break;
        }
      } // for

      solution.setObjective(0, totalTime);      
      solution.setObjective(1, executedRules);
        writer.println("totalTime: " + totalTime + " | executedRules: " + executedRules + " | totalScore: " + totalScore + "\n");
        writer.close();
        iteration++;
    } catch (Exception e) {
      System.out.println("File not found." + e.toString());  
    }

  } // evaluate
} // Spam


