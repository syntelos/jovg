package com.jogamp.jovg;

/**
 * Java bindings to OpenVG, accelerated vector graphics
 * @author John Pritchard
 */
public class VGException
    extends java.lang.RuntimeException
{

    public VGException(){
	super();
    }
    public VGException(String m){
	super(m);
    }
    public VGException(String fmt, Object... args){
	super(String.format(fmt,args));
    }
}
