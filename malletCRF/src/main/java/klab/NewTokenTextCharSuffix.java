package klab;

/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/**
 Catch a specified prefix and set the value to a double. Read the properties of the feature and add as doubles.

 @author (original) Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 @author Nathan Bartley <a href="mailto:bartleyn@uchicago.edu">bartleyn@uchicago.edu</a>
 */

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.Token;
import cc.mallet.types.TokenSequence;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.io.*;

import cc.mallet.pipe.*;
import cc.mallet.types.*;
import cc.mallet.util.PropertyList;

import static java.lang.System.exit;

public class NewTokenTextCharSuffix extends Pipe implements Serializable
{
    String prefix;
    int suffixLength;

    public NewTokenTextCharSuffix (String prefix, int suffixLength)
    {
        this.prefix=prefix;
        this.suffixLength = suffixLength;
    }

    public NewTokenTextCharSuffix ()
    {
        this ("SUFFIX=", 2);
    }

    public Instance pipe (Instance carrier)
    {
        TokenSequence ts = (TokenSequence) carrier.getData();
        for (int i = 0; i < ts.size(); i++) {
            Token t = ts.get(i);
            String s = t.getText();

            int slen = s.length();
            if (s.startsWith(prefix)) {

                int min_len = (prefix.length() + suffixLength >= slen - prefix.length()) ? slen : prefix.length() + suffixLength;
                t.setFeatureValue((prefix + "!"), Double.valueOf(s.substring(prefix.length(), min_len)));
                

            }
            PropertyList properties = t.getProperties();
            if (properties != null) {
                PropertyList.Iterator iter = properties.iterator();
                while (iter.hasNext()) {
                    s = iter.getKey();
                    slen = s.length();
                    //System.out.println(s + " -- " + prefix);

                    if (s.startsWith(prefix)) {
                        int min_len = (prefix.length() + suffixLength >= slen - prefix.length()) ? slen : prefix.length() + suffixLength;
                        t.setFeatureValue((prefix + "!"), Double.valueOf(s.substring(prefix.length(), min_len)));
                    }
                    iter.next();
                }
            }


        }
        //System.out.println(ts.toStringShort());
        //exit(-1);
        return carrier;
    }

    // Serialization

    private static final long serialVersionUID = 1;
    private static final int CURRENT_SERIAL_VERSION = 0;

    private void writeObject (ObjectOutputStream out) throws IOException {
        out.writeInt (CURRENT_SERIAL_VERSION);
        out.writeObject (prefix);
        out.writeInt (suffixLength);
    }

    private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
        int version = in.readInt ();
        prefix = (String) in.readObject();
        suffixLength = in.readInt ();
    }


}
