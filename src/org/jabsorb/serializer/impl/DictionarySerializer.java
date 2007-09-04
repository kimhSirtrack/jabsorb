/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 Arthur Blake and William Becker
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jabsorb.serializer.impl;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.jabsorb.json.JSONObject;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;

/**
 * Serialises Hashtables
 * 
 * TODO: why not use a map serialiser?
 */
public class DictionarySerializer extends AbstractSerializer
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * Classes that this can serialise.
   */
  private static Class[] _serializableClasses = new Class[] { Hashtable.class };

  /**
   * Classes that this can serialise to.
   */
  private static Class[] _JSONClasses = new Class[] { JSONObject.class };

  public boolean canSerialize(Class clazz, Class jsonClazz)
  {
    return (super.canSerialize(clazz, jsonClazz) || ((jsonClazz == null || jsonClazz == JSONObject.class) && Dictionary.class
        .isAssignableFrom(clazz)));
  }

  public Class[] getJSONClasses()
  {
    return _JSONClasses;
  }

  public Class[] getSerializableClasses()
  {
    return _serializableClasses;
  }

  public Object marshall(SerializerState state, Object o)
      throws MarshallException
  {
    Dictionary ht = (Dictionary) o;
    JSONObject obj = new JSONObject();
    JSONObject mapdata = new JSONObject();
    if (ser.getMarshallClassHints())
    {
      obj.put("javaClass", o.getClass().getName());
    }
    obj.put("map", mapdata);
    Object key = null;
    Object val = null;
    try
    {
      Enumeration en = ht.keys();
      while (en.hasMoreElements())
      {
        key = en.nextElement();
        val = ht.get(key);
        // only support String keys
        mapdata.put(key.toString(), ser.marshall(state, val));
      }
    }
    catch (MarshallException e)
    {
      throw new MarshallException("map key " + key + " " + e.getMessage());
    }
    return obj;
  }

  public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class = jso.getString("javaClass");
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    if (!(java_class.equals("java.util.Dictionary") || java_class
        .equals("java.util.Hashtable")))
    {
      throw new UnmarshallException("not a Dictionary");
    }
    JSONObject jsonmap = jso.getJSONObject("map");
    if (jsonmap == null)
    {
      throw new UnmarshallException("map missing");
    }
    ObjectMatch m = new ObjectMatch(-1);
    Iterator i = jsonmap.keys();
    String key = null;
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        m = ser.tryUnmarshall(state, null, jsonmap.get(key)).max(m);
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage());
    }
    return m;
  }

  public Object unmarshall(SerializerState state, Class clazz, Object o)
      throws UnmarshallException
  {
    JSONObject jso = (JSONObject) o;
    String java_class = jso.getString("javaClass");
    if (java_class == null)
    {
      throw new UnmarshallException("no type hint");
    }
    Hashtable ht = null;
    if (java_class.equals("java.util.Dictionary")
        || java_class.equals("java.util.Hashtable"))
    {
      ht = new Hashtable();
    }
    else
    {
      throw new UnmarshallException("not a Dictionary");
    }
    JSONObject jsonmap = jso.getJSONObject("map");
    if (jsonmap == null)
    {
      throw new UnmarshallException("map missing");
    }
    Iterator i = jsonmap.keys();
    String key = null;
    try
    {
      while (i.hasNext())
      {
        key = (String) i.next();
        ht.put(key, ser.unmarshall(state, null, jsonmap.get(key)));
      }
    }
    catch (UnmarshallException e)
    {
      throw new UnmarshallException("key " + key + " " + e.getMessage());
    }
    return ht;
  }

}