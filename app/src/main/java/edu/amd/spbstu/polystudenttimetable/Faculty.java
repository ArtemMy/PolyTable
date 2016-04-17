package edu.amd.spbstu.polystudenttimetable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Faculty implements Serializable
{
  @SerializedName("Faculty Name")
  public String   m_name;
  @SerializedName("Faculty Id")
  public int      m_id;
  @SerializedName("Faculty Abbreviature")
  public String   m_abbr;

  public Faculty()
  {
    m_name  = "";
    m_id    = -1;
    m_abbr  = "";
  }
}

