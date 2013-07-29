package com.qdevelop.core.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.qdevelop.bean.ResultBean;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TreeBean extends HashMap<String,Map> implements ResultBean,Serializable,Cloneable{
	public TreeBean clone(){
		return (TreeBean) super.clone();
	}
	private static final long serialVersionUID = 6947512596624161425L;
	private ArrayList<String> tmpIndex = new ArrayList<String>();
	
	String targetKey,targetParentKey,rootParentValue,textKey;
	ArrayList<String> rootKeyValue = new ArrayList();
	boolean hasRoot=true,isRichData=true;
	private HashMap<String,List<String>> parent2child = new HashMap();

	public TreeBean(String valueKey,String parentValueKey,String rootParentValue,String textKey){
		targetKey = valueKey;
		targetParentKey = parentValueKey;
		this.rootParentValue = rootParentValue;
		this.textKey = textKey;
	}

	/**
	 * treeKey				树ID对应字段名<br>
	 * treeParentKey		树父ID对应字段名称<br>
	 * treeRootParentValue	树根目录父亲对应的值<br>
	 * treeTextKey			树显示名称对应的字段名<br>
	 * treeRootShow			是否显示树的根 Defaulte true<br>
	 * treeRichData			是否显示数的附加信息 Default true
	 * @param query
	 */
	public TreeBean(Map query){
		targetKey = query.get("treeKey")==null?"ID":String.valueOf(query.get("treeKey"));
		query.remove("treeKey");
		targetParentKey = query.get("treeParentKey")==null?"PID":String.valueOf(query.get("treeParentKey"));
		query.remove("treeParentKey");
		this.rootParentValue = query.get("treeRootParentValue")==null?"0":String.valueOf(query.get("treeRootParentValue"));
		query.remove("treeRootParentValue");
		this.textKey = query.get("treeTextKey")==null?"NAME":String.valueOf(query.get("treeTextKey"));
		query.remove("treeTextKey");
		this.hasRoot = parseBoolean(String.valueOf(query.get("treeRootShow")));
		this.isRichData = parseBoolean(String.valueOf(query.get("treeRichData")));
		query.remove("treeRootShow");
		query.remove("treeRichData");
	}

	public TreeBean(){
		targetKey = "ID";
		targetParentKey = "PID";
		this.rootParentValue = "0";
		this.textKey = "NAME";
	}

	public void addResult(String id,String name,String pid){
		Map data = new HashMap();
		data.put("ID", id);
		data.put("NAME", name);
		data.put("PID", pid);
		addResult(data);
	}

	public boolean isLeaf(String target){
		return parent2child.get(target)==null;
	}

	public Map<String,Object> getParentById(String id){
		Map data =  this.get(id);
		if(data==null)return null;
		return this.get(String.valueOf(data.get(targetParentKey)));
	}
	
	public String getParentTextById(String id){
		Map data = getParentById(id);
		if(data==null)return null;
		return String.valueOf(data.get(textKey));
	}

	public void addResult(Map<String,Object> data){
		List<String> child = parent2child.get(String.valueOf(data.get(targetParentKey)));
		if(child == null){
			child = new ArrayList();
		}
		child.add(String.valueOf(data.get(targetKey)));
		parent2child.put(String.valueOf(data.get(targetParentKey)), child);
		this.put(String.valueOf(data.get(targetKey)), data);
		tmpIndex.add(String.valueOf(data.get(targetKey)));
		if(String.valueOf(data.get(targetParentKey)).equals(String.valueOf(rootParentValue)))
			rootKeyValue.add(String.valueOf(data.get(targetKey)));
	}
	
	/**
	 * 
	 * @param key 指定查询的columnName
	 * @param value 指定column完全匹配的值
	 * @return
	 */
	public List<Map<String,Object>> getRouteByLeafValue(String key,String value){
		if(key==null||value==null)return null;
		String _key = key.toUpperCase();
		java.util.Iterator<Map> itor = this.values().iterator();
		while(itor.hasNext()){
			Map data = itor.next();
			if(String.valueOf(data.get(_key)).equals(value)){
				return getRouteByLeafId(String.valueOf(data.get(targetKey)));
			}
		}
		return null;
	}
	/**
	 * 
	 * TODO 根据叶子ID值获取整个路由数据 
	 * 
	 * @param leadId
	 * @return
	 */
	public List<Map<String,Object>> getRouteByLeafId(String leadId){
		ArrayList<Map<String,Object>> tmp =new ArrayList<Map<String,Object>>();
		Map<String,Object> data = this.get(leadId);
		if(data==null)return null;
		tmp.add(data);
		while(true){
			data = this.get(String.valueOf(data.get(targetParentKey)));
			if(data==null)break;
			tmp.add(data);
		}
		ArrayList<Map<String,Object>> route = new ArrayList<Map<String,Object>>(tmp.size());
		int start = tmp.size()-1;
		for(int i=start;i>-1;i--){
			route.add(tmp.get(i));
		}
		return route;
	}

	public String getTextById(String ... ids){
		if(ids==null)return null;
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<ids.length;i++){
			if(this.get(ids[i])!=null){
				if(i>0)sb.append("|");
				sb.append(this.get(ids[i]).get(textKey));
			}
		}
		return sb.toString();
	}

	public List getChilds(String key){
		List<String> index = parent2child.get(key);
		if(index==null)return null;
		List childs = new ArrayList();
		for(String k:index){
			childs.add(this.get(k));
		}
		return childs;
	}
	
	/**
	 * 
	 * TODO 获取某个节点下面所有子节点ID的值；以"^"分开 
	 * 
	 * @param parentId
	 * @return
	 */
	public String getAllChildIDs(String parentId){
		StringBuffer sb = new StringBuffer();
		collectChildids(parentId,sb);
		return sb.length() < 1 ? "":sb.substring(1).toString();
	}
	
	private void collectChildids(String pid,StringBuffer sb){
		List<String> index = parent2child.get(pid);
		if(index!=null){
			for(String k:index){
				String currentKey = String.valueOf(this.get(k).get(targetKey));
				sb.append("^").append(currentKey);
				if(parent2child.get(currentKey)!=null){
					collectChildids(currentKey,sb);
				}
			}
		}
	}
	/**
	 * 
	 * TODO 获取父节点下所有的child数据 
	 * 
	 * @param parentId
	 * @return
	 */
	public List<Map> getAllChilds(String parentId){
		List<Map> childs = new ArrayList<Map>();
		collectChild(parentId,childs);
		return childs;
	}
	
	private void collectChild(String pid,List<Map> collections){
		List<String> index = parent2child.get(pid);
		if(index!=null){
			for(String k:index){
				collections.add(this.get(k));
				String currentKey = String.valueOf(this.get(k).get(targetKey));
				if(parent2child.get(currentKey)!=null){
					collectChild(currentKey,collections);
				}
			}
		}
	}

	public List getChildsKeys(String target){
		return parent2child.get(target);
	}



	public List toEasyTreeGrid(){
		if(rootKeyValue.size()==0)return null;
		List tree = new ArrayList();
		if(rootKeyValue.size()==1){
			if(hasRoot){
				List children = new ArrayList();
				Map node = new HashMap(this.get(rootKeyValue.get(0)));
				node.put("children", children);
				tree.add(node);
				findGridNodes(parent2child.get(rootKeyValue.get(0)),children);
			}else
				findGridNodes(parent2child.get(rootKeyValue.get(0)),tree);
		}else{
			findGridNodes(rootKeyValue,tree);
		}
		return tree;
	}

	public String getParent(String target){
		return String.valueOf(this.get(target).get(targetParentKey));
	}

	//	public String[] getChilds(String target){
	//		String[] childs = new String[2];
	//		return childs;
	//	}

	public List toEasyTree(){
		return toEasyTree(null,null,false);
	}

	public List toEasyTree(Object checkedValue){
		return toEasyTree(checkedValue,null,false);
	}

	/**
	 * 获取Easy Tree 格式的数据结果
	 * @param checkedValue 对ID或TEXT均有效
	 * @param filterParentKey 指定开头ID值
	 * @return
	 */
	public List toEasyTree(Object checkedValue,String filterParentKey,boolean filterLeaf){
		if(rootKeyValue.size()==0)return null;
		TreeCheckBean tcb = null;
		if(checkedValue!=null){
			tcb = new TreeCheckBean(checkedValue);
			String[] tmpIds = tcb.getCheckValues();
			for(String id:tmpIds){
				tcb.init(this.get(id),targetKey,targetParentKey);
			}
		}
		List tree = new ArrayList();
		if(rootKeyValue.size() >1){
			findTreeNodes(rootKeyValue,tree,tcb,filterLeaf);
			return tree;
		}
		if(hasRoot){
			List children = new ArrayList();
			Map node = new HashMap();
			Map data = this.get(getRootValue(filterParentKey,rootKeyValue.get(0)));
			if(data==null)return tree;
			node.put("id", data.get(targetKey));
			node.put("text", data.get(textKey));
			if(isRichData)
				node.put("attributes", data);
			node.put("children", children);
			tree.add(node);
			findTreeNodes(parent2child.get(getRootValue(filterParentKey,rootKeyValue.get(0))),children,tcb,filterLeaf);
		}else{
			findTreeNodes(parent2child.get(getRootValue(filterParentKey,rootKeyValue.get(0))),tree,tcb,filterLeaf);
		}
		return tree;
	}

	private String getRootValue(String filterParentKey,String defaultValue){
		return filterParentKey==null?defaultValue:filterParentKey;
	}

	private boolean parseBoolean(String val){
		if(val==null||val.equals("null"))return true;
		return Boolean.parseBoolean(val);
	}

	/**
	 * 	
	 * 		
	 * 
	 * @param childs
	 * @param nodesCollect 
	 * 	[{
	 * 		id:... ,
	 * 		text:... ,
	 * 		attributes:...,
	 * 		state:closed,
	 * 		children:[nodes]
	 * 	},...]
	 */
	private void findTreeNodes(List<String> childs,List nodesCollect,TreeCheckBean checkValues,boolean isFilterLeaf){
		if(childs==null)return;
		for(String id:childs){
			Map node = new HashMap();
			Map data = this.get(id);
			if(!isFilterLeaf || !isLeaf(String.valueOf(data.get(targetKey)))){
				node.put("id", data.get(targetKey));
				node.put("text", data.get(textKey));
				if(checkValues!=null && checkValues.isChecked(String.valueOf(data.get(targetKey)))){ 
					//					(ArrayUtils.contains(checkValues, String.valueOf(data.get(targetKey)))||ArrayUtils.contains(checkValues, String.valueOf(data.get(textKey))))){
					node.put("checked",new Boolean(true));
				}
				if(isRichData)
					node.put("attributes", data);
				nodesCollect.add(node);
			}
			if(parent2child.get(id)!=null){
				List children = new ArrayList();
				node.put("children", children);
				node.put("state", checkValues==null?"closed":checkValues.getStated(parent2child.get(id),String.valueOf(data.get(targetKey))));
				findTreeNodes(parent2child.get(id),children,checkValues,isFilterLeaf);
			}
		}
	}

	//	private String getStateByChildren(List<String> childs,String[] checkValues){
	//		if(checkValues == null)return "closed";
	//		for(String id:childs){
	////			Map data = this.get(id);
	//			
	//		}
	//		return "closed";
	//	}

	private void findGridNodes(List<String> childs,List nodesCollect){
		if(childs==null)return;
		for(String id:childs){
			Map node = new HashMap(this.get(id));
			nodesCollect.add(node);
			if(parent2child.get(id)!=null){
				List children = new ArrayList();
				node.put("children", children);
				node.put("state", "closed");
				findGridNodes(parent2child.get(id),children);
			}
		}
	}

	@Override
	public void flush() {

	}

	@Override
	public List getResultList() {
		List tmp = new ArrayList(this.size());
		Iterator itor = this.values().iterator();
		while(itor.hasNext()){
			tmp.add(itor.next());
		}
		return tmp;
	}

	@Override
	public Map getResultMap(Object i) {
		return this.get(tmpIndex.get(Integer.parseInt(String.valueOf(i))));
	}

	public void clear(){
		parent2child.clear();
		parent2child = null;
		targetKey=targetParentKey=rootParentValue=textKey=null;
		rootKeyValue.clear();
		super.clear();
	}


	public String[] getIdByText(String ... texts){
		if(texts == null)return null;
		Iterator<Map> itor = this.values().iterator();
		String[] result = new String[texts.length];
		String tmp;Map data;
		while(itor.hasNext()){
			data = itor.next();
			tmp = (String)data.get(textKey);
			if(tmp!=null){
				for(int i=0;i<texts.length;i++){
					if(checkIsThisByText(texts[i],tmp)){
						if(result[i] == null)
							result[i] = String.valueOf(data.get(targetKey));
						else 
							result[i] = new StringBuffer().append(result[i]).append("|").append(data.get(targetKey)).toString();
					}
				}	
			}
		}
		return result;
	}

	private boolean checkIsThisByText(String text,String source){
		if(text==null||text.length()==0)return false;
		if(text.indexOf("*")==-1){
			return source.equals(text);
		}else{
			return source.indexOf(text.replace("*", ""))>-1;
		}
	}



	/**
	 * 设置树根值
	 * @param value
	 */
	public void setTreeRootValue(String value){
		hasRoot = false;
		rootKeyValue.add(value);
	}

	public void setRichData(boolean isRichData){
		this.isRichData = isRichData;
	}

	public void setTreeKey(String treeKey){
		this.targetKey = treeKey;
	}

	public void setTreeParentKey(String treeParentKey){
		this.targetParentKey = treeParentKey;
	}

	public void setTreeTextKey(String treeTextKey){
		this.textKey = treeTextKey;
	}

	private void str(Map<String,Object> node ,StringBuffer sb,int level){
		for(int i=0;i<level;i++)
			sb.append("\t");
		sb.append("[").append(node.get("id")).append("]").append(node.get("text")).append("\r\n");
		
		if(node.get("children")!=null){
			List<Map> children = (List)node.get("children");
			for(Map cnode : children){
				str(cnode,sb,level+1);
			}
		}
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		List<Map> tree = this.toEasyTree();
		for(Map node : tree){
			str(node,sb,0);
		}
		//tree.clear();
		return sb.toString();
	}

	@Override
	public void setResultList(List<Map<String, Object>> result) {
		
	}

}
