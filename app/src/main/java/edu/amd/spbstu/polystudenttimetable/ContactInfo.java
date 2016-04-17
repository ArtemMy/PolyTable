package edu.amd.spbstu.polystudenttimetable;

import android.provider.ContactsContract;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ContactInfo implements Serializable
{
    @SerializedName("Phone")
    public String   m_phone;
    @SerializedName("Email")
    public String m_email;
    @SerializedName("Note")
    public String m_note;
    @SerializedName("Website")
    public String m_site;

    public ContactInfo()
    {
        m_phone          = "";
        m_email          = "";
        m_note          = "";
        m_site          = "";
    }
}
