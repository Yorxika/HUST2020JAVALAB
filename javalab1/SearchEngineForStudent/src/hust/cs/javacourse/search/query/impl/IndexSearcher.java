package hust.cs.javacourse.search.query.impl;

import hust.cs.javacourse.search.index.AbstractPosting;
import hust.cs.javacourse.search.index.AbstractPostingList;
import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.impl.Posting;
import hust.cs.javacourse.search.index.impl.Term;
import hust.cs.javacourse.search.query.AbstractHit;
import hust.cs.javacourse.search.query.AbstractIndexSearcher;
import hust.cs.javacourse.search.query.Sort;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexSearcher extends AbstractIndexSearcher {

    /**
     * 从指定索引文件打开索引，加载到index对象里. 一定要先打开索引，才能执行search方法
     *
     * @param indexFile ：指定索引文件
     */
    @Override
    public void open(String indexFile) {
        index.load(new File(indexFile));
        index.optimize();
    }

    /**
     * 根据单个检索词进行搜索
     *
     * @param queryTerm ：检索词
     * @param sorter    ：排序器
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm, Sort sorter) {
        AbstractPostingList postingList = index.search(queryTerm);  //取得Term对应的PostingList
        if(postingList == null)
            return null;
        List<AbstractHit> list = new ArrayList<>();
        for(int i = 0 ; i < postingList.size() ; ++i){
            AbstractPosting posting = postingList.get(i);
            String path = index.getDocName(posting.getDocId());
            HashMap<AbstractTerm,AbstractPosting> map = new HashMap<>();
            map.put(queryTerm,posting);
            AbstractHit hit = new Hit(posting.getDocId(),path,map);
            hit.setScore(sorter.score(hit));
            list.add(hit);
        }
        sorter.sort(list);
        //return (AbstractHit[]) list.toArray();  //不能将Object[] 转化为AbstractHit[]，转化的话只能是取出每一个元素再转化。
        // java中的强制类型转换只是针对单个对象的，想要偷懒将整个数组转换成另外一种类型的数组是不行的，这和数组初始化时需要一个个来也是类似的
        return list.toArray(new AbstractHit[0]);
    }


    /**
     * 根据二个检索词进行搜索
     *
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter     ：    排序器
     * @param combine    ：   多个检索词的逻辑组合方式
     * @return ：命中结果数组
     */
    @Override
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter, LogicalCombination combine) {
        AbstractPostingList postingList1 = index.search(queryTerm1);
        AbstractPostingList postingList2 = index.search(queryTerm2);
        if(postingList1 == null && postingList2 == null)
            return null;
        List<AbstractHit> list = new ArrayList<>();
        int len1 = postingList1 == null ? 0 : postingList1.size();
        int len2 = postingList2 == null ? 0 : postingList2.size();
        int i = 0 , j = 0;
        Posting temp = new Posting(-1,-1,null);
        while (i < len1 || j < len2){
            assert postingList1 != null;
            AbstractPosting posting1 = i < len1 ? postingList1.get(i) : temp;
            assert postingList2 != null;
            AbstractPosting posting2 = j < len2 ? postingList2.get(j) : temp;
            int docId1 = posting1.getDocId(), docId2 = posting2.getDocId();
            if(docId1 == docId2 && docId1 != -1){  //且
                String path = index.getDocName(docId1);
                HashMap<AbstractTerm,AbstractPosting> map = new HashMap<>();
                map.put(queryTerm1,posting1);
                map.put(queryTerm2,posting2);
                AbstractHit hit = new Hit(docId1,path,map);
                hit.setScore(sorter.score(hit));
                list.add(hit);
                i++;
                j++;
            }else if(docId1 < docId2 && docId1 != -1 ){  //或
                if(combine == LogicalCombination.OR) {
                    String path = index.getDocName(docId1);
                    HashMap<AbstractTerm, AbstractPosting> map = new HashMap<>();
                    map.put(queryTerm1, posting1);
                    AbstractHit hit = new Hit(docId1, path, map);
                    hit.setScore(sorter.score(hit));
                    list.add(hit);
                }
                    i++;
                }else {
                    if(combine == LogicalCombination.OR && docId2 != -1){
                        String path = index.getDocName(docId2);
                        HashMap<AbstractTerm,AbstractPosting> map = new HashMap<>();
                        map.put(queryTerm2,posting2);
                        AbstractHit hit = new Hit(docId2,path,map);
                        hit.setScore(sorter.score(hit));
                        list.add(hit);
                    }
                    j++;
                }
            }
        sorter.sort(list);
        //TODO:待完成两个搜索词
        return list.toArray(new AbstractHit[0]);
    }

    /**
     * 根据二个相邻的检索词进行搜索
     *
     * @param queryTerm1 ：第1个检索词
     * @param queryTerm2 ：第2个检索词
     * @param sorter     ：    排序器
     * @return ：命中结果数组
     */
    public AbstractHit[] search(AbstractTerm queryTerm1, AbstractTerm queryTerm2, Sort sorter) {
        AbstractPostingList postingList1 = index.search(queryTerm1);
        AbstractPostingList postingList2 = index.search(queryTerm2);
        if(postingList1 == null || postingList2 == null)
            return null;
        List<AbstractHit> list = new ArrayList<>();
        int len1 = postingList1.size();
        int len2 = postingList2.size();
        int i = 0 , j = 0;
        while (i < len1 && j < len2){
            AbstractPosting posting1 = postingList1.get(i);
            AbstractPosting posting2 = postingList2.get(j);
            int docId1 = posting1.getDocId(), docId2 = posting2.getDocId();
            if(docId1 == docId2 && docId1 != -1){  //且
                List<Integer> position1 = posting1.getPositions();
                List<Integer> position2 = posting2.getPositions();
                List<Integer> curPosition = new ArrayList<>();
                int pos1 = 0 , pos2 = 0;
                while (pos1 < position1.size() && pos2 < position2.size()){
                    int curPos1 = position1.get(pos1) , curPos2 = position2.get(pos2);
                    if(Math.abs(curPos1 - curPos2) == 1){
                        curPosition.add(Math.min(curPos1,curPos2));
                        pos1++;
                        pos2++;
                    }else if(curPos1 < curPos2 - 2)
                        pos1++;
                    else if(curPos2 < curPos1 - 2)
                        pos2++;
                }
                if(!curPosition.isEmpty()){
                    String path = index.getDocName(docId1);
                    HashMap<AbstractTerm,AbstractPosting> map = new HashMap<>();
                    map.put(new Term(queryTerm1.getContent() + " " + queryTerm2.getContent()),new Posting(docId1,curPosition.size(),curPosition));
                    AbstractHit hit = new Hit(docId1,path,map);
                    hit.setScore(sorter.score(hit));
                    list.add(hit);
                }
                i++;
                j++;
            }else if(docId1 < docId2 ) //或
                i++;
            else
                j++;
        }
        sorter.sort(list);
        //TODO:待完成两个搜索词
        return list.toArray(new AbstractHit[0]);
    }
}
