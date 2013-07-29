package com.qdevelop.cache.utils;

public class CLang {
	/**索引缓存开头**/
	public static String INDEX_PRERFIX = "i_";
	
	/**以此值开头的策略均对缓存及时性要求不高，可以常驻缓存的异步更新的；适合前台**/
	public static String LONG_CACHE_PREFIX = "l_";
	
	/**以此值开头的策略对缓存的及时性要求较高，需要强制清理缓存；适合后台使用**/
	public static String SHORT_CACHE_PREFIX = "s_";
	
	/**以此开头的策略会全部清理该索引内的cache**/
	public static String MINI_CACHE_PREFIX = "m_";
	
}
