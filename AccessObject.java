

abstract class AccessObject{

  abstract public String getName();

  abstract public boolean equals(Object obj);

  public Class<?> getType(){
    return this.getClass();
  }

}
