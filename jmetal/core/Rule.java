package jmetal.core;

public class Rule {

  private String _name;
  private double _score;
  private double _cpu;
  private double _io;
  private double _real;
  
  public Rule()
  {
    this._name  = "not set";
    this._score = 0;
    this._cpu   = 0;
    this._io    = 0;
    this._real  = 0;
  }

  public void setName(String name)
  {
    this._name = name;
  }

  public void setScore(double score)
  {
    this._score = score;
  }

  public String getName()
  {
    return this._name; 
  }

  public double getScore()
  {
    return this._score;
  }

  public void setCPU(double cpu)
  {
    this._cpu = cpu;
  }

  public void setIO(double io)
  {
    this._io = io;
  }

  public double getCPU()
  {
    return this._cpu;
  }

  public double getIO()
  {
    return this._io;
  }
}
