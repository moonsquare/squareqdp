package com.qdevelop.core.workflow;

import java.util.ArrayList;
import java.util.HashMap;

import com.qdevelop.core.CoreFactory;
import com.qdevelop.core.datasource.DataBaseFactory;


public class WorkFlowEngine{
	private static WorkFlowEngine _WorkFlowEngine = new WorkFlowEngine();
	public static WorkFlowEngine getInstance(){
		return _WorkFlowEngine;
	}

	@SuppressWarnings("unchecked")
	public void updateFlowStatus(String wfIndex,int curIdx,boolean isAccess,HashMap<String,String>data){
		WorkFlowBean wfb  =  WorkFlowLoader.getInstance().get(wfIndex);
		int nowIndex = wfb.getNext(curIdx, isAccess);
		if(data==null)data = new HashMap<String,String>();
		data.put("wfIdx", String.valueOf(nowIndex));
		
		String[] sqlIndexs = wfb.getExcuteSqlIndexs(curIdx, isAccess);
		CoreFactory cf = CoreFactory.getInstance();
		if(sqlIndexs!=null){
			if(sqlIndexs.length==1){
				data.put("index", sqlIndexs[0]);
				cf.getQueryUpdate(data);
			}else{
				HashMap<String,String> tmp;String[] sqls;ArrayList<String> executeSqls = new ArrayList<String>();
				for(String index:sqlIndexs){
					tmp = (HashMap<String,String>)data.clone();
					tmp.put("index", index);
					sqls = cf.getQueryBean(tmp).getQuery();
					for(String sql:sqls){
						executeSqls.add(sql);
					}
					tmp.clear();
				}
				DataBaseFactory.getInstance().update(executeSqls);
			}
		}

	}

//	public static void main(String[] args) {
//		WorkFlowBean wfb  =  WorkFlowLoader.getInstance().get("userPayFlow");
//		System.out.println(wfb.getNext(3, true));
//		System.out.println(wfb.getNext(2, false));
//		String[] sqls = wfb.getExcuteSqlIndexs(1, true);
//		HashMap data = new HashMap();
//		data.put("legalperson", "ssss");
//		if(sqls!=null){
//			HashMap tmp;
//			for(String index:sqls){
//				tmp = (HashMap) data.clone();
//				tmp.put("index", index);
//				System.out.println(CoreFactory.getInstance().getQueryBean(tmp).getSql());
//			}
//		}
//	}
}
