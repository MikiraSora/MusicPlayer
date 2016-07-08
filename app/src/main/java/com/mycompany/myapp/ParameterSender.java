package com.mycompany.myapp;

import android.app.Application;

import java.util.HashMap;

/**
 * Created by mikir on 2016/7/8.
 */
public class ParameterSender extends Application {

    HashMap<Object,Object> map=new HashMap<>();

    public Object getObject(Object key){
        return map.containsKey(key)?map.get(key):null;
    }

    public void putObject(Object key,Object val){
        map.put(key,val);
    }
}
