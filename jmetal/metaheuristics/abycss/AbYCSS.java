//  AbYCSS.java
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

package jmetal.metaheuristics.abycss;

import jmetal.core.*;
import jmetal.operators.localSearch.LocalSearch;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.Spea2Fitness;
import jmetal.util.archive.CrowdingArchive;
import jmetal.util.comparators.CrowdingDistanceComparator;
import jmetal.util.comparators.DominanceComparator;
import jmetal.util.comparators.ObjectiveComparator;
import jmetal.util.comparators.EqualSolutions;
import jmetal.util.comparators.FitnessComparator;
import jmetal.util.wrapper.XReal;
import jmetal.core.ParseRules;
import jmetal.util.comparators.DistanceToPopulationComparator;
import java.util.Comparator;

/**
 * This class implements the AbYCSS algorithm. This algorithm is an adaptation
 * of the single-objective scatter search template defined by F. Glover in:
 * F. Glover. "A template for scatter search and path relinking", Lecture Notes 
 * in Computer Science, Springer Verlag, 1997. AbYCSS is described in: 
 *   A.J. Nebro, F. Luna, E. Alba, B. Dorronsoro, J.J. Durillo, A. Beham 
 *   "AbYCSS: Adapting Scatter Search to Multiobjective Optimization." 
 *   IEEE Transactions on Evolutionary Computation. Vol. 12, 
 *   No. 4 (August 2008), pp. 439-457
 */
public class AbYCSS extends Algorithm {

  /**
   * Stores the number of subranges in which each encodings.variable is divided. Used in
   * the diversification method. By default it takes the value 4 (see the method
   * <code>initParams</code>).
   */
  /**
   * These variables are used in the diversification method.
   */
  int []  sumOfFrequencyValues_        ; 
  int []  sumOfReverseFrequencyValues_ ;
  int [][] frequency_                  ; 
  int [][] reverseFrequency_           ; 

  /**
   * Stores the initial solution set
   */
  private SolutionSet solutionSet_;

  /**
   * Stores the external solution archive
   */
  private CrowdingArchive archive_ ;

  /**
   * Stores the reference set one
   */
  private SolutionSet refSet1_ ;

  /**
   * Stores the reference set two
   */
  private SolutionSet refSet2_ ;

  /**
   * Stores the solutions provided by the subset generation method of the
   * scatter search template
   */
  private SolutionSet subSet_ ;    

  /**
   * Maximum number of solution allowed for the initial solution set
   */
  private int solutionSetSize_;

  /**
   * Maximum size of the external archive
   */
  private int archiveSize_;

  /** 
   * Maximum size of the reference set one
   */
  private int refSet1Size_;

  /**
   * Maximum size of the reference set two
   */
  private int refSet2Size_;  

  /**
   * Maximum number of getEvaluations to carry out
   */
  private int maxEvaluations;  

  /**
   * Stores the current number of performed getEvaluations
   */
  private int evaluations_ ;
  /**
   * Stores the current number of performed getEvaluations
   */
  private int numberOfSubranges_ ;

  /**
   * Stores the comparators for dominance and equality, respectively
   */
  private Comparator dominance_ ;
  private Comparator equal_     ;
  private Comparator fitness_   ;
  private Comparator crowdingDistance_;
  private Comparator dominance1_;
  private Comparator dominance2_;

  /**
   * Stores the crossover operator
   */
  private Operator crossoverOperator_;

  /**
   * Stores the improvement operator
   */
  private LocalSearch improvementOperator_;

  /**
   * Stores a <code>Distance</code> object
   */
  private Distance distance_;

  /**
   * Constructor.
   * @param problem Problem to solve
   */
  public AbYCSS(Problem problem){
    super (problem) ;
    //Initialize the fields 

    solutionSet_ = null ;
    archive_     = null ;
    refSet1_     = null ;
    refSet2_     = null ;
    subSet_      = null ;    
  } // AbYCSS

  /**
   * Reads the parameter from the parameter list using the
   * <code>getInputParameter</code> method.
   */
  public void initParam(){
    //Read the parameters
    solutionSetSize_= ((Integer)getInputParameter("populationSize")).intValue();
    refSet1Size_    = ((Integer)getInputParameter("refSet1Size")).intValue();
    refSet2Size_    = ((Integer)getInputParameter("refSet2Size")).intValue();
    archiveSize_    = ((Integer)getInputParameter("archiveSize")).intValue();
    maxEvaluations  = ((Integer)getInputParameter("maxEvaluations")).intValue();

    //Initialize the variables
    solutionSet_ = new SolutionSet(solutionSetSize_);     
    archive_     = new CrowdingArchive(archiveSize_,problem_.getNumberOfObjectives());        
    refSet1_     = new SolutionSet(refSet1Size_);        
    refSet2_     = new SolutionSet(refSet2Size_);        
    subSet_      = new SolutionSet(solutionSetSize_*1000);
    evaluations_       = 0 ;

    numberOfSubranges_ = 5 ; 

    //dominance_ = new DominanceComparator();
    dominance1_ = new ObjectiveComparator(0);
    dominance2_ = new ObjectiveComparator(1);
    equal_     = new EqualSolutions();     
    fitness_   = new FitnessComparator();
    crowdingDistance_ = new CrowdingDistanceComparator();
    distance_  = new Distance();
    sumOfFrequencyValues_        = new int[problem_.getNumberOfVariables()] ;
    sumOfReverseFrequencyValues_ = new int[problem_.getNumberOfVariables()] ;
    frequency_        = new int[numberOfSubranges_][problem_.getNumberOfVariables()] ;
    reverseFrequency_ = new int[numberOfSubranges_][problem_.getNumberOfVariables()] ;    

    //Read the operators of crossover and improvement
    crossoverOperator_   =  operators_.get("crossover");
    improvementOperator_ = (LocalSearch) operators_.get("improvement");
    improvementOperator_.setParameter("archive",archive_);        
  } // initParam

  /**
   * Returns a <code>Solution</code> using the diversification generation method
   * described in the scatter search template.
   * @throws JMException 
   * @throws ClassNotFoundException 
   */
  public Solution diversificationGeneration() throws JMException, ClassNotFoundException{

    Solution solution = new Solution(problem_);
    SolutionSet solutionLocal_ = new SolutionSet(solutionSetSize_);
    
    for ( int i = 0; i < solutionSetSize_; i++ )
    {
      solution = new Solution(problem_);
      problem_.evaluate(solution);            
      problem_.evaluateConstraints(solution);
      evaluations_++;
      solution = (Solution)improvementOperator_.execute(solution);         
      evaluations_ += improvementOperator_.getEvaluations();
      solutionLocal_.add(solution);
    }
    
    ObjectiveComparator obj1_ = new ObjectiveComparator(0); 
    ObjectiveComparator obj2_ = new ObjectiveComparator(1); 
    
    solutionLocal_.sort(obj1_);
    
    for ( int e = 0; e < solutionSetSize_; e++ )
    {
    
    // quando metade da população for gerada, ordena-se para o segundo objectivo
    if ( e == solutionSetSize_ / 2 ) solutionLocal_.sort(obj2_);

    SolutionSet solution1_ = new SolutionSet(solutionSetSize_);
    SolutionSet solution2_ = new SolutionSet(solutionSetSize_);
    SolutionSet solution3_ = new SolutionSet(solutionSetSize_);
    SolutionSet solution4_ = new SolutionSet(solutionSetSize_);
    SolutionSet solution5_ = new SolutionSet(solutionSetSize_);

    // dividir os invividuos em 5 partes iguais
    for ( int i = 0; i < solutionSetSize_ / 5; i++ )
    {
      solution1_.add(solutionLocal_.get(i));
      solution2_.add(solutionLocal_.get(i + ( solutionSetSize_ / 5 ) * 1 ) );
      solution3_.add(solutionLocal_.get(i + ( solutionSetSize_ / 5 ) * 2 ) );
      solution4_.add(solutionLocal_.get(i + ( solutionSetSize_ / 5 ) * 3 ) );
      solution5_.add(solutionLocal_.get(i + ( solutionSetSize_ / 5 ) * 4 ) );
    }
    
    Solution individual1_;
    Solution individual2_;
    // selecionar o índividuo com maior valor de função objectiva
    // anteriormente ordenado / primeira extremidade
    individual1_ = solution1_.get(0);                
    individual1_.setDistanceToSolutionSet(distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(individual1_,solution2_, solutionLocal_) + 
                                         distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(individual1_,solution3_, solutionLocal_) +
                                         distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(individual1_,solution4_, solutionLocal_)
                                         );
    
    // selecionar individuo com pior valor função objectiva
    // última extremidade
    individual2_ = solution5_.get(solution5_.size() - 1);                
    individual2_.setDistanceToSolutionSet(distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(individual2_,solution2_, solutionLocal_) + 
                                         distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(individual2_,solution3_, solutionLocal_) +
                                         distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(individual2_,solution4_, solutionLocal_)
                                         ); 
    
    if ( individual1_.getDistanceToSolutionSet() > individual2_.getDistanceToSolutionSet())
    {
      solutionSet_.add(individual1_);
      for ( int i = 0; i < solution1_.size() / 5; i++ ) solution1_.remove(0);
    } else {
      solutionSet_.add(individual2_);
      for ( int i = solution1_.size() - 1; i <= solution1_.size() - solution1_.size() / 5; i++ ) solution1_.remove(i);
    }
  }

    return solution ;
  } // diversificationGeneration


  /** 
   * Implements the referenceSetUpdate method.
   * @param build if true, indicates that the reference has to be build for the
   *        first time; if false, indicates that the reference set has to be
   *        updated with new solutions
   * @throws JMException 
   */
  public void referenceSetUpdate(boolean build) throws JMException{
    if (build) { 
      // Build a new reference set
      // STEP 1. Select the p best individuals of P, where p is refSet1Size_. 
      //         Selection Criterium: ObjectiveComparator
      Solution individual;
      ObjectiveComparator obj1_ = new ObjectiveComparator(0);
      ObjectiveComparator obj2_ = new ObjectiveComparator(1);

      SolutionSet solutionSet1_ = new SolutionSet(solutionSet_.size() / 2);
      SolutionSet solutionSet2_ = new SolutionSet(solutionSet_.size() / 2);

      //
      for ( int i = 0; i < solutionSet_.size() / 2; i++ )
      {
        solutionSet1_.add(solutionSet_.get(i));
        solutionSet2_.add(solutionSet_.get(i + solutionSet_.size() / 2 ));
      }
      
      // sort by two objectives
      solutionSet1_.sort(obj1_);
      solutionSet2_.sort(obj2_);
      int set1_ = 0;
      int set2_ = 0;

      /*
       * replace order by the sorted solutions above
       * */
      for ( int i = 0; i < solutionSet_.size(); i++ )
      {
        if ( i % 2 == 0 )
        {
          solutionSet_.replace(i, solutionSet1_.get(set1_));
          set1_++;
        } else {
          solutionSet_.replace(i, solutionSet2_.get(set2_));
          set2_++;
        } // if
      } // for

      // STEP 2. Build the RefSet1 with these p individuals            
      for (int i = 0; i  < refSet1Size_; i++) {
        individual = solutionSet_.get(0);
        solutionSet_.remove(0);
        individual.unMarked();
        refSet1_.add(individual);                 
      }
      // STEP 3. Calcular distância com base nos dois objectivos 
      for (int i = 0; i < solutionSet_.size(); i++) {
        solutionSet_.get(i).setDistanceToSolutionSet(distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(solutionSet_.get(i),refSet1_, solutionSet_));                       
        //System.out.println("distance1 " + (float)distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(solutionSet_.get(i),refSet1_, solutionSet_));
       } 
      // Step 4. Ordenar por distância maior
      DistanceToPopulationComparator distanceToPopulationComparator_ = new DistanceToPopulationComparator();
      solutionSet_.sort(distanceToPopulationComparator_);      
      /*
       * for (int i = 0; i < solutionSet_.size(); i++){
        System.out.println("distance2 " + solutionSet_.get(i).getDistanceToSolutionSet());
      }*/
      int size = refSet2Size_;
      if (solutionSet_.size() < refSet2Size_) {
        size = solutionSet_.size();
      }
      

      // STEP 4. Construir o RefSet2
      for (int i = size - 1; i >= 0; i--){
        // Find the maximumMinimunDistanceToPopulation
        individual = solutionSet_.get(solutionSet_.size() - 1);
        solutionSet_.remove(solutionSet_.size() - 1);
        refSet2_.add(individual);
      } // for                       

    } else { // Update the reference set from the subset generation result
      Solution individual;
      for (int i = 0; i < subSet_.size();i++){
        individual = (Solution)improvementOperator_.execute(subSet_.get(i));
        evaluations_ += improvementOperator_.getEvaluations();


        if (refSet1Test(individual)){ //Update distance of RefSet2
          for (int indSet2 = 0; indSet2 < refSet2_.size(); indSet2++) {
            double aux = distance_.distanceBetweenSolutionsAbYCSS(individual,
                refSet2_.get(indSet2), refSet1_, refSet2_);
            if (aux < refSet2_.get(indSet2).getDistanceToSolutionSet()) {
              refSet2_.get(indSet2).setDistanceToSolutionSet(aux);
            } // if */ 
          } // for                 
        }  else {
          refSet2Test(individual);
        } // if 
      }
      subSet_.clear();
    }
  } // referenceSetUpdate

  /** 
   * Tries to update the reference set 2 with a <code>Solution</code>
   * @param solution The <code>Solution</code>
   * @return true if the <code>Solution</code> has been inserted, false 
   * otherwise.
   * @throws JMException 
   */
  public boolean refSet2Test(Solution solution) throws JMException{        

    if (refSet2_.size() < refSet2Size_){
      solution.setDistanceToSolutionSet(distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(solution,refSet1_, solutionSet_));
      double aux = distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(solution,refSet2_, solutionSet_);
      if (aux < solution.getDistanceToSolutionSet()) {
        solution.setDistanceToSolutionSet(aux);
      }
      refSet2_.add(solution);
      return true;
    }

    solution.setDistanceToSolutionSet(distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(solution,refSet1_, solutionSet_));
    double aux = distance_.distanceToSolutionSetInSolutionSpaceAbYCSS(solution,refSet2_, solutionSet_);
    if (aux < solution.getDistanceToSolutionSet()) {
      solution.setDistanceToSolutionSet(aux);
    }

    double peor = 0.0;     
    int index = 0;
    for (int i = 0; i < refSet2_.size();i++){
      aux = refSet2_.get(i).getDistanceToSolutionSet();
      if (aux > peor){
        peor = aux;
        index = i;
      }
    }

    if (solution.getDistanceToSolutionSet() < peor){            
      refSet2_.remove(index);
      //Update distances in REFSET2
      for (int j = 0; j < refSet2_.size();j++){
        aux = distance_.distanceBetweenSolutionsAbYCSS(refSet2_.get(j),solution, refSet1_, refSet2_);
        if (aux < refSet2_.get(j).getDistanceToSolutionSet()){
          refSet2_.get(j).setDistanceToSolutionSet(aux);
        }
      }
      solution.unMarked();
      refSet2_.add(solution);
      return true;
    }           
    return false;
  } // refSet2Test

  /** 
   * Tries to update the reference set one with a <code>Solution</code>.
   * @param solution The <code>Solution</code>
   * @return true if the <code>Solution</code> has been inserted, false
   * otherwise.
   */
  public boolean refSet1Test(Solution solution){
    boolean dominated = false;
    int flag;
    int flag2;
    int i = 0;
    while (i < refSet1_.size()){
      flag  = dominance1_.compare(solution,refSet1_.get(i));
      flag2 = dominance2_.compare(solution,refSet1_.get(i));
      if (flag == -1 && flag2 == -1) { //This is: solution dominates 
        refSet1_.remove(i);
      } else if (flag == 1 || flag2 == 1) {
        dominated = true;
        i++;
      } else {
        flag = equal_.compare(solution,refSet1_.get(i));
        if (flag == 0) {
          return true;
        } // if
        i++;
      } // if 
    } // while

    if (!dominated){
      solution.unMarked();
      if (refSet1_.size() < refSet1Size_) { //refSet1 isn't full
        refSet1_.add(solution);
      } else {
        archive_.add(solution);                
      } // if
    } else {
      return false;
    } // if
    return true;        
  } // refSet1Test

  /** 
   * Implements the subset generation method described in the scatter search
   * template
   * @return  Number of solutions created by the method
   * @throws JMException 
   */
  public int subSetGeneration() throws JMException{            
    Solution [] parents = new Solution[2];
    Solution [] offSpring;

    subSet_.clear();                                                                                        

    //All pairs from refSet1
    for (int i = 0; i < refSet1_.size();i++){
      parents[0] = refSet1_.get(i);
      for (int j = i+1; j < refSet1_.size();j++){                
        parents[1] = refSet1_.get(j);
        if (!parents[0].isMarked() || !parents[1].isMarked()){
          //offSpring = parent1.crossover(1.0,parent2);
          offSpring = (Solution [])crossoverOperator_.execute(parents);
          problem_.evaluate(offSpring[0]);
          problem_.evaluate(offSpring[1]);    
          problem_.evaluateConstraints(offSpring[0]);
          problem_.evaluateConstraints(offSpring[1]);                    
          evaluations_ += 2;                                        
          if (evaluations_ < maxEvaluations){
            subSet_.add(offSpring[0]);
            subSet_.add(offSpring[1]);    
          }
          parents[0].marked();
          parents[1].marked();
        }                
      }
    }

    // All pairs from refSet2
    for (int i = 0; i < refSet2_.size();i++){
      parents[0] = refSet2_.get(i);
      for (int j = i+1; j < refSet2_.size();j++){                
        parents[1] = refSet2_.get(j);
        if (!parents[0].isMarked() || !parents[1].isMarked()){
          //offSpring = parents[0].crossover(1.0,parent2);                    
          offSpring = (Solution []) crossoverOperator_.execute(parents);
          problem_.evaluateConstraints(offSpring[0]);
          problem_.evaluateConstraints(offSpring[1]);                    
          problem_.evaluate(offSpring[0]);
          problem_.evaluate(offSpring[1]);
          evaluations_+=2;                                        
          if (evaluations_ < maxEvaluations){
            subSet_.add(offSpring[0]);
            subSet_.add(offSpring[1]);
          }
          parents[0].marked();
          parents[1].marked();
        }                
      }
    }

    return subSet_.size();
  } // subSetGeneration

  /**   
   * Runs of the AbYCSS algorithm.
   * @return a <code>SolutionSet</code> that is a set of non dominated solutions
   * as a result of the algorithm execution  
   * @throws JMException 
   */  
  public SolutionSet execute() throws JMException, ClassNotFoundException {
    // STEP 1. Initialize parameters
    initParam();
    // STEP 2. Build the initial solutionSet
    Solution solution; 
    diversificationGeneration();

    // STEP 3. Main loop
    int newSolutions = 0;
    while (evaluations_ < maxEvaluations) {                       
      referenceSetUpdate(true);
      newSolutions = subSetGeneration();        
      while (newSolutions > 0) { // New solutions are created           
        referenceSetUpdate(false);
        if (evaluations_ >= maxEvaluations)                                        
          return archive_;                
        newSolutions = subSetGeneration();                
      } // while

      // RE-START
      if (evaluations_ < maxEvaluations){
        solutionSet_.clear();
        // Add refSet1 to SolutionSet
        for (int i = 0; i < refSet1_.size();i++){
          solution = refSet1_.get(i);
          solution.unMarked();
          solution = (Solution)improvementOperator_.execute(solution);
          evaluations_ += improvementOperator_.getEvaluations();
          solutionSet_.add(solution);
        }
        // Remove refSet1 and refSet2
        refSet1_.clear();        
        refSet2_.clear();

        // Sort the archive and insert the best solutions
        distance_.crowdingDistanceAssignment(archive_,
            problem_.getNumberOfObjectives());                                
        archive_.sort(crowdingDistance_);                

        int insert = solutionSetSize_  / 2;
        if (insert > archive_.size())
          insert = archive_.size();

        if (insert > (solutionSetSize_ - solutionSet_.size())) 
          insert = solutionSetSize_ - solutionSet_.size();         

        // Insert solutions
        for (int i = 0; i < insert; i++){                
          solution = new Solution(archive_.get(i));                                        
          //solution = improvement(solution);
          solution.unMarked();
          solutionSet_.add(solution);
        }

        // Create the rest of solutions randomly
        while (solutionSet_.size() < solutionSetSize_){
          // tiago
          
          //diversificationGeneration();                    
         
         // solution = diversificationGeneration();                    
          solution = new Solution(problem_);                    
          problem_.evaluateConstraints(solution);                                         
          problem_.evaluate(solution);
          evaluations_++;
          solution = (Solution)improvementOperator_.execute(solution);
          evaluations_ += improvementOperator_.getEvaluations();
          solution.unMarked();
          solutionSet_.add(solution);
        } // while
      } // if   
    } // while       

    archive_.printFeasibleFUN("FUN_AbYCSS") ;

    // STEP 4. Return the archive
    return archive_;                
  } // execute
} // AbYCSS
