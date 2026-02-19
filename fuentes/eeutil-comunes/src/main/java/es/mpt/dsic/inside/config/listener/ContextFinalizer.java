/*
 * Copyright (C) 2025, Gobierno de España This program is licensed and may be used, modified and
 * redistributed under the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European Commission. Unless
 * required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and more details. You
 * should have received a copy of the EUPL1.1 license along with this program; if not, you may find
 * it at http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 */

package es.mpt.dsic.inside.config.listener;



import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.LogFactory;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.util.IntrospectorCleanupListener;

public class ContextFinalizer extends IntrospectorCleanupListener {

  private ClassLoader loader = null;

  public void contextInitialized(ServletContextEvent sce) {
    System.out.println("Calling>>>>>>>>>>>>>>>>>>>>>>>.?");
    /* Introspector.flushCaches(); */
    ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
    CachedIntrospectionResults.clearClassLoader(cl1);
    LogFactory.releaseAll();


    /* Enhancer.registerCallbacks(enhanced, null); */
    // cleanUp();
  }

  @SuppressWarnings("deprecation")
  public void contextDestroyed(ServletContextEvent sce) {
    /*
     * Thread t = Thread.currentThread(); Runtime.getRuntime().addShutdownHook(t);
     */
    System.out.println("Good Bye>>>>>>>>>>>>>>>>>>>>>.?");
    cleanUp();
    java.beans.Introspector.flushCaches();
    java.security.Security.removeProvider(null);
    ClassLoader cl1 = Thread.currentThread().getContextClassLoader();
    CachedIntrospectionResults.clearClassLoader(cl1);
    LogFactory.releaseAll();
    org.apache.log4j.LogManager.shutdown();

    Enumeration<Driver> drivers = DriverManager.getDrivers();
    Driver d = null;

    ClassLoader cl = Thread.currentThread().getContextClassLoader();

    while (drivers.hasMoreElements()) {
      try {
        d = drivers.nextElement();
        DriverManager.deregisterDriver(d);

      } catch (Exception ex) {
        // LOGGER.warn(String.format("Error deregistering driver %s",
        // d), ex);
      }
    }

    /*
     * if (ConnectionImpl.class.getClassLoader() == getClass().getClassLoader()) { Field f = null;
     * try { f = ConnectionImpl.class.getDeclaredField("cancelTimer"); f.setAccessible(true); Timer
     * timer = (Timer) f.get(null); timer.cancel(); }catch(Exception e) {
     * 
     * }finally { f = null; } }
     */


    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
    Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
    for (Thread t : threadArray) {
      /*
       * if (t.isInterrupted()) { break; }
       */

      if (t.getName().contains("Abandoned connection cleanup thread")) {
        synchronized (t) {
          // don't complain, it works
          if (t.isAlive()) {
            System.out.println("Alive True");
            if (t.isDaemon()) {
              System.out.println("isDaemon True");
              t.stop();
            } else {
              System.out.println("isDaemon False");
              t.stop();
            }
          } else {
            System.out.println("Alive Flase");
            t.stop();
          }
          // new Timer(true);
        }
      } else if (t.getName().contains("http-nio-8081-exec-1")) {
        System.out.println("http-nio-8081-exec-1>>>>>>>>>>>");
      } else {
        System.out.println("Else If Block");
        synchronized (t) {
          t.setDaemon(true);
          t.suspend();
        }
      }
    }
    java.beans.Introspector.flushCaches();

  }

  public void onApplicationEvent(ContextRefreshedEvent arg0) {
    System.out.println("--------------- Context Refreshed -----------------");
    System.out.println("::::::::::::::::::::::::  Calling   :::::::::::::::::::::::::::::");

    ApplicationContext context = arg0.getApplicationContext();
    System.out.println(context.getDisplayName());
  }

  private void cleanUp() {
    Thread[] threads = getThreads();
    for (Thread thread : threads) {
      if (thread != null) {
        System.out.println("Inside IFF");
        cleanContextClassLoader(thread);
        cleanOrb(thread);
        cleanThreadLocal(thread);

      }

    }
  }

  private Thread[] getThreads() {
    ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
    ThreadGroup parentGroup;
    if (rootGroup.getParent() != null) {
      parentGroup = rootGroup.getParent();
      if (parentGroup != null) {
        rootGroup = parentGroup;
      }
    }
    Thread[] threads = new Thread[rootGroup.activeCount()];
    while (rootGroup.enumerate(threads, true) == threads.length) {
      threads = new Thread[threads.length * 2];
    }
    return threads;
  }

  private boolean loaderRemovable(ClassLoader cl) {
    if (cl == null) {
      return false;
    }
    Object isDoneCalled = getObject(cl, "doneCalled");
    String clName = cl.getClass().getName();
    loader = Thread.currentThread().getContextClassLoader();
    String ldr = null;
    loader = loader.getParent();
    if (loader != null) {
      // loader.getParent();
      ldr = loader.getClass().getName();
    }

    if (clName != null && ldr != null && isDoneCalled != null) {
      if (clName.equalsIgnoreCase(ldr) && isDoneCalled instanceof Boolean
          && (Boolean) isDoneCalled) {
        return true;
      }
    }

    return loader == cl;
  }

  private Field getField(Class clazz, String fieldName) {
    Field f = null;
    try {
      f = clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException ex) {

    } catch (SecurityException ex) {
    }

    if (f == null) {
      Class parent = clazz.getSuperclass();
      if (parent != null) {
        f = getField(parent, fieldName);
      }
    }
    if (f != null) {
      f.setAccessible(true);
    }
    return f;
  }

  private Object getObject(Object instance, String fieldName) {
    Class clazz = instance.getClass();
    Field f = getField(clazz, fieldName);
    if (f != null) {
      try {
        return f.get(instance);
      } catch (IllegalArgumentException | IllegalAccessException ex) {
      }
    }
    return null;
  }

  private void cleanContextClassLoader(Thread thread) {
    if (loaderRemovable(thread.getContextClassLoader())) {
      thread.setContextClassLoader(null);
    }
  }

  private void cleanOrb(Thread thread) {
    Object currentWork = getObject(thread, "currentWork");
    if (currentWork != null) {
      Object orb = getObject(currentWork, "orb");
      if (orb != null) {
        Object transportManager = getObject(orb, "transportManager");
        if (transportManager != null) {
          Thread selector = (Thread) getObject(transportManager, "selector");
          if (selector != null && loaderRemovable(selector.getContextClassLoader())) {
            selector.setContextClassLoader(null);
          }
        }
      }
    }
  }

  private void removeThreadLocal(Object entry, Object threadLocals, Thread thread) {
    ThreadLocal threadLocal = (ThreadLocal) getObject(entry, "referent");
    if (threadLocal != null) {
      Class clazz = null;
      try {
        clazz = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
      } catch (ClassNotFoundException ex) {
      }
      if (clazz != null) {
        Method removeMethod = null;
        Method[] methods = clazz.getDeclaredMethods();
        if (methods != null) {
          for (Method method : methods) {
            if (method.getName().equals("remove")) {
              removeMethod = method;
              removeMethod.setAccessible(true);
              break;
            }
          }
        }
        if (removeMethod != null) {
          try {
            removeMethod.invoke(threadLocals, threadLocal);
          } catch (IllegalAccessException | IllegalArgumentException
              | InvocationTargetException ex) {
          }
        }

      }

    }
  }

  private void cleanThreadLocal(Thread thread) {
    Object threadLocals = getObject(thread, "threadLocals");
    if (threadLocals != null) {
      Object table = getObject(threadLocals, "table");
      if (table != null) {
        int size = Array.getLength(table);
        for (int i = 0; i < size; i++) {
          Object entry = Array.get(table, i);
          if (entry != null) {
            Field valueField = getField(entry.getClass(), "value");
            if (valueField != null) {
              try {
                Object value = valueField.get(entry);
                if (value != null && value instanceof ClassLoader
                    && loaderRemovable((ClassLoader) value)) {
                  removeThreadLocal(entry, threadLocals, thread);
                }
              } catch (IllegalArgumentException | IllegalAccessException ex) {

              }

            }
          }

        }
      }
    }
  }

}
