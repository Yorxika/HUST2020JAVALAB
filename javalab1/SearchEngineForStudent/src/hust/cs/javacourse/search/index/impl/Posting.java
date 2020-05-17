package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.AbstractPosting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

public class Posting extends AbstractPosting {

    /**
     * 缺省构造函数
     */
    public Posting() {
    }

    /**
     * 构造函数
     *
     * @param docId     ：包含单词的文档id
     * @param freq      ：单词在文档里出现的次数
     * @param positions ：单词在文档里出现的位置
     */
    public Posting(int docId, int freq, List<Integer> positions) {
        super(docId, freq, positions);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        else if(obj instanceof Posting){
            Posting posting = (Posting)obj;
            if(((Posting) obj).positions != null && this.positions != null)
                return this.positions.size() == posting.positions.size() && this.positions.containsAll(((Posting) obj).positions)
                 && this.docId == posting.docId && this.freq == posting.freq;
            else if(posting.positions == null && this.positions == null)
                return this.freq == posting.freq && this.docId == posting.docId;
        }
        return false;
    }

    @Override
    public String toString() {
        return "docId:" + this.docId + ", freq:" + this.freq + ", positions:" + this.positions +"";
    }

    @Override
    public int getDocId() {
        return this.docId;
    }

    @Override
    public void setDocId(int docId) {
        this.docId = docId;
    }

    @Override
    public int getFreq() {
        return this.freq;
    }

    @Override
    public void setFreq(int freq) {
        this.freq = freq;
    }

    @Override
    public List<Integer> getPositions() {
        return this.positions;
    }

    @Override
    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    @Override
    public int compareTo(AbstractPosting o) {
        return this.docId - o.getDocId();
    }

    @Override
    public void sort() {
        Collections.sort(this.positions);
    }

    @Override
    public void writeObject(ObjectOutputStream out) {
        try{
            out.writeInt(this.docId);
            out.writeInt(this.freq);
            out.writeObject(this.positions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readObject(ObjectInputStream in) {
        try{
            this.docId = in.readInt();
            this.freq = in.readInt();
            this.positions = (List<Integer>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
