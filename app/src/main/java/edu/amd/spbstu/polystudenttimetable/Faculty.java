package edu.amd.spbstu.polystudenttimetable;

import java.io.Serializable;

public class Faculty implements Serializable
{
  public String   m_name;
  public int      m_id;
  public String   m_abbr;

  public Faculty()
  {
    m_name  = "";
    m_id    = -1;
    m_abbr  = "";
  }
}

