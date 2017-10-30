package com.festivefacefilters;

import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by pritesh on 30/10/17.
 */

public class ObjectMover {
    private static ObjectMover objectMover;
    private Hashtable<String,Object> hashtable;
    private ObjectMover()
    {
        hashtable=new Hashtable<>();
    }

    private static ObjectMover getInstance()
    {
        if(objectMover==null)
            objectMover=new ObjectMover();
        return objectMover;
    }

    public static void put(String key,Object ob)
    {
           getInstance().hashtable.put(key,ob);
    }
    public static Object get(String key)
    {
        ObjectMover objectMover=getInstance();
        Object ob=objectMover.hashtable.get(key);
        objectMover.hashtable.remove(key);
        objectMover=null;
        return ob;

    }
}
