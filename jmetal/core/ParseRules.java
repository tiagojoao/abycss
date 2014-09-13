package jmetal.core;
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
import java.text.*;
import java.util.Properties;
import jmetal.util.JMException;
import jmetal.core.*;

public class ParseRules {

  int numberOfRules = 900;
  int iteration = 1;
  private SpamAssassinRules rules;
  
  public ParseRules() {
    try{
      this.rules = new SpamAssassinRules("files/50_scores.cf");
    } catch(Exception e) {
      
    } 
  }

  public SpamAssassinRules getRules()
  {
    return this.rules;
  }



}
