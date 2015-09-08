package klab;

/* Copyright (C) 2002 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */




/**
 Add the token text as a feature with value 1.0.

 @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
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
            if (slen > suffixLength)
                //t.setFeatureValue ((prefix + s.substring (slen - suffixLength, slen)), 1.0);
                t.setFeatureValue((prefix), Double.valueOf(s.substring(prefix.length(), prefix.length() + suffixLength)));
        }
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
