package com.qdevelop.cache.tactics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.qdevelop.cache.bean.IndexItem;
import com.qdevelop.cache.bean.TableCacheItem;
import com.qdevelop.cache.bean.TableCacheSort;
import com.qdevelop.cache.clear.TableIndexs;
import com.qdevelop.cache.interfaces.ITacties;
import com.qdevelop.core.connect.ConnectFactory;
import com.qdevelop.core.datasource.QueryFromDataBaseImp;
import com.qdevelop.lang.QDevelopException;
import com.qdevelop.utils.QDate;
import com.qdevelop.utils.QProperties;

public class FirstTactiesImpl extends HashMap<String,TableCacheItem> implements ITacties{
	/**
	 * TODO （描述变量的作用）
	 */
	private static final long serialVersionUID = -1538966925614753894L;

	@Override
	public void addKey(String key, String config, String query) {

	}

	@Override
	public boolean isDeleteImmediately(IndexItem ki) {

		return false;
	}

	@Override
	public boolean isUpdateAsynchronous(IndexItem ki) {

		return false;
	}

	@Override
	public boolean isRemoveAsynchronous(IndexItem ki) {

		return false;
	}

	@Override
	public void updateKey(String key, String config, String query) {

	}

	@Override
	public void removeKey(String key, String config) {

	}

	@Override
	public void addCacheRate(String key, String config, String query) {

	}

	@SuppressWarnings("unchecked")
	public HashMap<String,Object> getVal(String key,String config) throws QDevelopException{
		String[] item = TableIndexs.getInstance().getPrimaryItem(config);
		if(item == null)return null;
		String query = new StringBuffer().append("select * from ").append(item[0]).append(" where ").append(item[2]).append("='").append(key).append("'").toString();
		Connection conn = null; 
		try {
			conn = ConnectFactory.getInstance((item[1].endsWith("_R")?item[1]:item[1]+"_R")).getConnection();
			if(QProperties.isDebug)System.out.println(new StringBuffer().append("First Cache load Data from Table:[").append(config).append("] id:[").append(key).append("] cacheConnect:").append(conn!=null));
			return (HashMap<String,Object>)QueryFromDataBaseImp.getInstance().selectSingle(query, conn, null);
		} catch (QDevelopException e) {
			throw e;
		}finally{
			try {
				if(conn!=null){
					conn.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public HashMap<String,HashMap<String,Object>> getVals(String tableIndex,List<String> keys) throws QDevelopException{
		String[] item = TableIndexs.getInstance().getPrimaryItem(tableIndex);
		if(item == null)return null;
		HashMap<String,HashMap<String,Object>> result = new HashMap<String,HashMap<String,Object>>();
		StringBuffer sql = new StringBuffer().append("select * from ").append(item[0]).append(" where ").append(item[2]).append(" in ("); 
		for(int i=0;i<keys.size();i++){
			if(i>0)sql.append(",");
			sql.append("'").append(keys.get(i)).append("'");
		}
		sql.append(")");
		Connection conn = null; 
		try {
			if(QProperties.isDebug)System.out.println(new StringBuffer().append("First Cache load Data from Table:[").append(tableIndex).append("] id:[").append(keys.toString()).append("] cacheConnect:").append(conn!=null));
			String uniKey = item[2].toUpperCase();

			ResultSet rs = null ;
			PreparedStatement ps = null;
			try {
				conn = ConnectFactory.getInstance(item[1]+"_R").getConnection();
				ps = conn.prepareStatement(sql.toString(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
				rs = ps.executeQuery();
				while(rs.next()){
					ResultSetMetaData rsmd = rs.getMetaData();
					int recordSize = rsmd.getColumnCount();
					HashMap<String,Object> data = new HashMap<String,Object>(recordSize);
					for(int i=1;i<=recordSize;i++){
						data.put(rsmd.getColumnName(i).toUpperCase(), rs.getObject(i));
					}
					result.put(String.valueOf(data.get(uniKey)), data);
				}
			} catch (Exception e) {
				System.out.println(QDate.getNow("yyyy-MM-dd HH:mm:ss")+"\tERROR ==> "+sql);
				throw new QDevelopException(e);
			} finally {
				try {
					if(rs!=null)rs.close();
					if (ps != null)	ps.close();
					if(conn!=null)conn.close();
				} catch (Exception e) {
				}
			}
		} catch (QDevelopException e) {
			throw e;
		}
		return result;
	}

//	private static TableCacheItem noCareUpdate = new TableCacheItem(null,false);
	
	public TableCacheItem put(String key,TableCacheItem value){
		if(1000 - this.size() < 10){
			TableCacheItem[] tableCacheItems = this.values().toArray(new TableCacheItem[]{});
			Arrays.sort(tableCacheItems,new TableCacheSort());
			for(int i=0;i<20;i++){
				this.remove(tableCacheItems[i].getKey());
			}
		}
		return super.put(key,value);
	}

	public TableCacheItem get(String key){
		TableCacheItem tci = super.get(key);
		if(tci!=null){
			tci.addRate();
		}
		return tci;
	}
//	private static Pattern cleanSql = Pattern.compile("^ +|\n|\t");
//	private static Pattern toKeyPattern = Pattern.compile("SET.+?WHERE|=.+$| ");
//	private static Pattern uniKeyPattern = Pattern.compile("^.+WHERE|=.+$| ");
//	private static Pattern tablePattern = Pattern.compile("^UPDATE| WHERE.+?$| SET.+?$|`| ");
//	private static Pattern valuePattern = Pattern.compile("'| ");
	
//	/**
//	 * 
//	 * TODO 判定SQL只有符合一定规范书写时，才能被正确执行缓存清理工作的判定<br>
//	 * 例：update ${table} set xxx where ${主键}=${value} 
//	 * 
//	 * @param sql
//	 * @return
//	 */
//	public TableCacheItem getTableCacheItem(String sql){
//		String _sql = cleanSql.matcher(sql.toUpperCase()).replaceAll("");
//		if(!_sql.substring(0,6).equals("UPDATE"))return noCareUpdate;
//		String key = toKeyPattern.matcher(_sql).replaceAll("");
//		TableCacheItem tci = this.get(key);
//		if(tci == null){
//			String tableName = tablePattern.matcher(_sql).replaceAll("");
//			String uniKey = uniKeyPattern.matcher(_sql).replaceAll("");
//			tci = SQLModelLoader.getInstance().getTableCacheItem(tableName, uniKey);
//			if(tci == null)tci = new TableCacheItem(key,false);
//			this.put(key, tci);
//		}
//		if(tci.isTableCache()){
//			tci.setUniValue(valuePattern.matcher(sql.substring(sql.lastIndexOf("=")+1)).replaceAll(""));
//		}
//		return tci;
//	}

	@Override
	public String toKey(String key, String config) {
		return new StringBuffer(config).append(key).toString();
	}

	@Override
	public void printQueue() {

	}

//	@Override
//	public void mergeAsynAddCache(CasIndexArray casIndexArray) {
//
//	}

	@Override
	public void asynRun() {
//		ConnectFactory.asynClose();
	}

	@Override
	public void delCacheRate(String key, String config, String query) {
		// TODO Auto-generated method stub
		
	}
//	public static void main(String[] args) {
//		String sql = "		update products set product_name = 'CAVALLI TENUTA DEGLI DEI葡萄酒2008HGGHGJHGHGHGGGHJGHJHJGHJHJGJHJH',group_code = 0 ,price = 0,sale_price = 0,max_point = 0,grade = 0,refund = '0' ,attr_select = '{}',extra = '{year1:\"\"}',bid = 149 ,isLarge = 0 ,eb_id = 0,english_name = 'CAVALLI TENUTA DEGLI DEI2008',brand = '性价之王',detail = '<p>\n 		        <strong>荣誉/评分：</strong><br />\n 		        <br />\n 		        <strong>等级：</strong>IGT 典型产区酒<br />\n 		        <br />\n 		        <strong>特点介绍：</strong>Cavalli将托斯卡纳的葡萄酒传统和家族对酒的热爱充分的结合在了一起。每年，CAVALLI TENUTA DEGLI DEI的酒标上都会展示一个流行工作室设计的著名印花。酒庄精心酿制每一瓶酒，使每一瓶<br />\n 		        <br />\n 		        &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;酒都成为您的独一无二。<br />\n 		        <br />\n 		        <strong>葡萄品种：</strong>赤霞珠(Cabernet Sauvignon)，品丽珠(Cabernet Franc)，小味儿多(Petit Verdot)，阿利坎特(Alicante)<br />\n 		        <br />\n 		        <strong>土壤气候：</strong>泥灰岩质土壤，排水性极好，葡萄园朝南方向能很好的吸收阳光的雨水。<br />\n 		        <br />\n 		        <strong>风格口味：</strong>充满了红色水果的香气，紧接着是淡淡的烟草味的咖啡香，口感活泼丰富。<br />\n 		        <br />\n 		        <strong>配餐建议：</strong>牛排、披萨等<br />\n 		        <br />\n 		        <strong>陈酿方式：</strong>将葡萄放入桶内发酵为8-10天。之后在橡木桶中陈酿12-18个月。</p>\n 		',sell_point_title = '酒庄精心酿制，每一瓶酒都成为您的独一无二' where  pid = 'HJtNVAQGf'";
//		TableCacheItem tci = new FirstTactiesImpl().getTableCacheItem(sql);
//		System.out.println(tci.isTableCache());
//		System.out.println(tci.getTableName());
//		System.out.println(tci.getUniKey());
//		System.out.println(tci.getUniValue());
//	}

}
