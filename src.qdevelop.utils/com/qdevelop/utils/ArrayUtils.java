package com.qdevelop.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({"unchecked","rawtypes"})
public class ArrayUtils {
	

	/**
	 * Conver List<Integer> to int[].
	 * @param ints
	 * @return
	 */
	public static int[] convert(Collection<Integer> ints) {
		int[] result = new int[ints.size()];
		int i = 0;
		Iterator<Integer> iter = ints.iterator();
		while (iter.hasNext()) {
			result[i] = iter.next();
		}
		return result;
	}

	/**
	 * merge inputed two int[] arrays.
	 * 
	 * @param array1
	 * @param array2
	 * @return
	 * @throws NullPointerException
	 *             if <code>array1</code> or <code>array2</code> is null.
	 */
	public  static int[] merge(int[] array1, int[] array2) {
		if (array1 == null || array2 == null) {
			throw new NullPointerException();
		}

		// check arguments
		if (array1.length == 0) {
			return array2;
		}
		if (array2.length == 0) {
			return array1;
		}

		// initialize arguments
		int[] array = new int[array1.length + array2.length];
		int pos1 = 0;
		int pos2 = 0;
		int pos = 0;

		// loop and merge
		while (pos1 < array1.length && pos2 < array2.length) {
			if (array1[pos1] < array2[pos2]) {
				array[pos++] = array1[pos1++];
				if (pos1 == array1.length) {
					System.arraycopy(array2, pos2, array, pos, array2.length
							- pos2);
					pos = pos + array2.length - pos2;
				}
			} else if (array1[pos1] == array2[pos2]) {
				array[pos++] = array1[pos1];
				pos1++;
				pos2++;
				if ((pos1 == array1.length) && (pos2 == array2.length)) {
					break;
				}
				if (pos1 == array1.length) {
					System.arraycopy(array2, pos2, array, pos, array2.length
							- pos2);
					pos = pos + array2.length - pos2;
				}
				if (pos2 == array2.length) {
					System.arraycopy(array1, pos1, array, pos, array1.length
							- pos1);
					pos = pos + array1.length - pos1;
				}
			} else {
				array[pos++] = array2[pos2++];
				if (pos2 == array2.length) {
					System.arraycopy(array1, pos1, array, pos, array1.length
							- pos1);
					pos = pos + array1.length - pos1;
				}
			}
		}

		// prepare return variable
		int[] result = new int[pos];
		System.arraycopy(array, 0, result, 0, pos);
		return result;
	}

	
	/**
	 * 找两组List的交集
	 * @param l1
	 * @param l2
	 * @return
	 */
     public static ArrayList<Object> intersect(List<Object> l1,List<Object> l2){
    	HashSet<Object> hs = new HashSet<Object>(l1);
    	HashSet<Object> hs1 = new HashSet<Object>(l2);
    	ArrayList<Object> al = new ArrayList<Object>();
    	HashSet<Object> hs2 = new HashSet<Object>();
    	for(int i = 0; i < l1.size(); i++){
    		Object o = l1.get(i);
    		if(!hs2.contains(o)&& (hs.contains(o) && hs1.contains(o))){
    			al.add(o);
    		}
    		hs2.add(o);
    	}
    	hs1.clear();
    	hs.clear();
    	hs2.clear();
    	hs = hs1 = hs2 = null;
    	return al;
    }
    
	/**
	 * intersect inputed two int[] arrays.
	 * 
	 * @param array1
	 * @param array2
	 * @return
	 * @throws NullPointerException
	 *             if <code>array1</code> or <code>array2</code> is null.
	 */
	public static  List<Integer> intersect(Integer[] array1, Integer[] array2) {
		if (array1 == null || array2 == null) {
			throw new NullPointerException();
		}

		// check arguments
		if (array1.length == 0 || array2.length == 0) {
			return new ArrayList<Integer>();
		}

		// initialize arguments
		List<Integer> array = new ArrayList<Integer>(Math.min(array1.length, array2.length));
		int pos1 = 0;
		int pos2 = 0;

		while (pos1 < array1.length && pos2 < array2.length) {
			if (array1[pos1].compareTo(array2[pos2]) < 0) {
				pos1++;
				if (pos1 == array1.length) {
					break;
				}
			} else if (array1[pos1].compareTo(array2[pos2]) == 0) {
				array.add(array1[pos1]);
				pos1++;
				pos2++;
				if ((pos1 == array1.length) || (pos2 == array2.length)) {
					break;
				}
			} else {
				pos2++;
				if (pos2 == array2.length) {
					break;
				}
			}
		}
		return array;
	}
	
	
	
	public static  boolean equals(byte[] b1, byte[] b2) {
		if (b1 == b2) {
			return true;
		}
		if (b1 == null || b2 == null) {
			return false;
		}
		if (b1.length != b2.length) {
			return false;
		}
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static void toPrint(Object[] result){
		for(int i=0;i<result.length;i++){
			System.out.println(i+":\t"+result[i]);
		}
	}
	
	public static void toPrint(List result){
		for(int i=0;i<result.size();i++){
			System.out.println(i+":\t"+result.get(i));
		}
	}
	
	  /**
	    * 增长指定数组的大小，默认为1
	    * @param a 数组对象
	    * @return 增长后的数组对象
	    */
	   public static Object growArray(Object a) {
	      Class c1 = a.getClass();
	      if (!c1.isArray()) {

	         return null;
	      }
	      Class componentType = c1.getComponentType();
	      int length = Array.getLength(a);
	      int newLength = length + 1;
	      Object newArray = Array.newInstance(componentType, newLength);
	      System.arraycopy(a, 0, newArray, 0, length);
	      return newArray;
	   }

	   public static Object growArray(Object a, int size) {
	      Class c1 = a.getClass();
	      if (!c1.isArray()) {
	         return null;
	      }
	      Class componentType = c1.getComponentType();
	      int length = Array.getLength(a);
	      int newLength = size + 1;
	      Object newArray = Array.newInstance(componentType, newLength);
	      System.arraycopy(a, 0, newArray, 0, length);
	      return newArray;
	   }

	   public static int[] addElementToArray(int[] source, int obj) {
	      int[] ret = (int[]) growArray(source);
	      ret[source.length] = obj;
	      return ret;
	   }

	   public static long[] addElementToArray(long[] source, long obj) {
	      long[] ret = (long[]) growArray(source);
	      ret[source.length] = obj;
	      return ret;
	   }

	   public static double[] addElementToArray(double[] source, double obj) {
	      double[] ret = (double[]) growArray(source);
	      ret[source.length] = obj;
	      return ret;
	   }

	   public static String[] addElementToArray(String[] source, String obj) {
	      String[] ret = (String[]) growArray(source);
	      ret[source.length] = obj;
	      return ret;
	   }

	   public static Object[] addElementToArray(Object[] source, Object obj) {
	      Object[] ret = (Object[]) growArray(source);
	      ret[source.length] = obj;
	      return ret;
	   }

	   public static int[] insert(int[] source, int obj, int index) {
	      if ((index < 0) || (index > source.length)) {
	         throw new IllegalArgumentException("index out of range:  " + index);
	      }
	      int length = source.length;
	      int[] ret = new int[length + 1];
	      System.arraycopy(source, 0, ret, 0, index);
	      ret[index] = obj;
	      System.arraycopy(source, index, ret, index + 1, length - index);
	      return ret;
	   }

	   public static Object[] insert(Object[] source, Object obj, int index) {
	      if ((index < 0) || (index > source.length)) {
	         throw new IndexOutOfBoundsException("index out of range:  " + index);
	      }
	      Object[] ret = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length + 1);
	      System.arraycopy(source, 0, ret, 0, index);
	      ret[index] = obj;
	      System.arraycopy(source, index, ret, index + 1, source.length - index);
	      return ret;
	   }

	   public static int[] remove(int[] source, int index) {
	      if ((index < 0) || (index >= source.length)) {
	         throw new ArrayIndexOutOfBoundsException(index);
	      }
	      int length = source.length;
	      int[] ret = new int[length - 1];
	      System.arraycopy(source, 0, ret, 0, index);
	      System.arraycopy(source, index + 1, ret, index, ret.length - index);
	      return ret;
	   }

	   public static Object[] remove(Object[] source, int index) {
	      if ((index < 0) || (index >= source.length)) {
	         throw new ArrayIndexOutOfBoundsException(index);
	      }
	      Object[] ret = (Object[]) Array.newInstance(source.getClass().getComponentType(), source.length - 1);
	      System.arraycopy(source, 0, ret, 0, index);
	      System.arraycopy(source, index + 1, ret, index, ret.length - index);
	      return ret;
	   }

	   public static Object[] remove(Object[] source, Object obj) {
	      int index = -1;
	      for (int i = 0; i < source.length; i++) {
	         if (source[i] == obj) {
	            index = i;
	            break;
	         }
	      }
	      if (index > -1) {
	         return remove(source, index);
	      }
	      return source;
	   }

	   public static Object[] removeDuplicates(Object[] array) {
	      Hashtable hashtable = new Hashtable();
	      for (int i = 0; i < array.length; i++) {
	         hashtable.put(array[i], array[i]);

	      }
	      Object[] ret = (Object[]) Array.newInstance(array.getClass().getComponentType(), hashtable.size());
	      int index = 0;
	      Enumeration enumeration = hashtable.elements();
	      while (enumeration.hasMoreElements()) {
	         ret[index++] = enumeration.nextElement();
	      }
	      return ret;
	   }

	   public static Object[] subArray(Object[] source, int startIndex, int length) {
	      if ((startIndex == 0) && (length == source.length)) {
	         return source;
	      }
	      Object[] ret = (Object[]) Array.newInstance(source.getClass().getComponentType(), length);
	      System.arraycopy(source, startIndex, ret, 0, length);
	      return ret;
	   }

	   public static Object[] subArray(Object[] source, int startIndex) {
	      return subArray(source, startIndex, source.length - startIndex);
	   }

	   /**
	    * /** 转换存放整形的集合到标准数组
	     *
	     * @param coll
	     *            存放整形的集合
	     * @return 标准整形数组
	     */
	    public static int[] toIntArray(Collection coll) {
	       Iterator it = coll.iterator();
	       int[] arr = new int[coll.size()];
	       int i = 0;
	       while (it.hasNext()) {
	          arr[i++] = ((Integer) it.next()).intValue();
	       }
	       return arr;
	    }

	    public static String[] toStringArray(Object[] objects) {
	      int length = objects.length;
	      String[] result = new String[length];
	      for (int i = 0; i < length; i++) {
	         result[i] = objects[i].toString();
	      }
	      return result;
	   }

	   /**
	    * 匹配对象数组到指定的类型。比如匹配到字符串数组：
	    *
	    * <pre>
	    *  Object[] obj = new Object[] {&quot;test1&quot;, &quot;test2&quot;, &quot;test3&quot;};
	    *  String[] s = String[] typecast(obj, new String[0]);
	    * </pre>
	    *
	    * @param array
	    *            要匹配的数组类型
	    * @param to
	    *            匹配后的数组类型
	    * @return 正确类型的一份克隆
	    */
	   public static Object[] typecast(Object[] array, Object[] to) {
	      return Arrays.asList(array).toArray(to);
	   }

	   public static String[] toStringArray(Collection coll) {
	      return toStringArray(coll.toArray());
	   }

	   public static boolean byteEquals(final byte a[], final byte b[]) {
	      if (a == b) {
	         return true;
	      }
	      if (a == null || b == null) {
	         return false;
	      }
	      if (a.length != b.length) {
	         return false;
	      }

	      try {
	         for (int i = 0; i < a.length; i++) {
	            if (a[i] != b[i]) {
	               return false;
	            }
	         }
	      }
	      catch (ArrayIndexOutOfBoundsException e) {
	         return false;
	      }
	      return true;
	   }
	   
	   public static List<Object> toArrayList(Object[] array){
		   List<Object> tmp = new ArrayList<Object>();
		   for(Object obj:array){
			   tmp.add(obj);
		   }
		   return tmp;
	   }
	
	public static void main(String[] args){
		Integer[] a = new Integer[]{13,2,23,4,24,16,23};
		Integer[] b = new Integer[]{4,12,23,16,13};
//		int [] tmp = ArrayUtils.merge(a, b);
		List aa = ArrayUtils.intersect(ArrayUtils.toArrayList(a), ArrayUtils.toArrayList(b));
		ArrayUtils.toPrint(aa);
//		ArrayUtils.intersectArrayWithSeq(l1, l2)
	}

}
