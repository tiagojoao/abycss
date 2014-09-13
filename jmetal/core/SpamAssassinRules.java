package jmetal.core;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import jmetal.core.Rule;

public class SpamAssassinRules extends ArrayList {
  
  private ArrayList<Rule> _rules;
  private String _line;
  private BufferedReader _in;
  private String _fileName;
  private double _oddCpuTime = 100;
  private double _evenCpuTime = 200;
  private double _oddIOTime = 50;
  private double _evenIOTime = 300;

  public SpamAssassinRules(String fileName) throws Exception
  {
    this._fileName = fileName;
    this._rules = new ArrayList<Rule>();
    this._open();
    this._line = "";
    this._parse();
    this._close();
  }
   
  private void _parse() throws Exception
  {
    int pair = 0;
    while ((this._line = this._in.readLine()) != null) {
      String parts[] = this._line.split(" ");
      Rule _rule = new Rule();
      _rule.setName(parts[1]);
      _rule.setScore(Double.parseDouble(parts[2]));
      if ( (_rules.size() & 1) == 0)
      { 
        _rule.setCPU(_evenCpuTime);
        _rule.setIO(_evenIOTime);
      } else {
        _rule.setCPU(_oddCpuTime);
        _rule.setIO(_oddIOTime);
      }
      this._rules.add(_rule);
    }
  }

  private void _open() throws Exception
  {
    this._in = new BufferedReader(new FileReader(this._fileName));
  }

  private void _close() throws Exception
  {
    this._in.close();
  }

  public void out()
  {  
    for ( Rule r : this._rules )
    {
      System.out.println("Name:" + r.getName() + " | Score " + r.getScore() + " | CPU:" + r.getCPU() + " | IO:" + r.getIO());
    }
  }

  public double[] getScore()
  {
    double[] _score = new double[this._rules.size()];

    int i = 0;
    for ( Rule r : this._rules )
    {
      _score[i] = r.getScore();
      i++;
    }
    
    return _score;
  }

  public int getSize()
  {
    return this._rules.size();
  }

  public Rule getRule(int index)
  {
    return this._rules.get(index);
  }
  
}
