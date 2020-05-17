package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.AbstractTerm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Term extends AbstractTerm {


    public Term() {
    }


    public Term(String content) {
        super(content);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        else if(obj instanceof Term){
            Term term = (Term)obj;
            if(term.content != null && this.content != null)
                return term.content.equals(this.content);
            else return term.content == null && this.content == null;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.content;
    }

    @Override
    public String getContent() {
        return this.content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int compareTo(AbstractTerm o) {
        /*if(o instanceof Term){
            Term term = (Term)o;
            char[] content1 = this.content.toCharArray();
            char[] content2 = term.content.toCharArray();
            int i;
            for(i = 0 ; i < content1.length && i < content2.length ; ++i){
                if(content1[i] - content2[i] > 0)
                    return 1;
                else if(content2[i] - content1[i] > 0)
                    return -1;
            }
            return Integer.compare(content1.length, content2.length);
        }
        return 0;*/
        return content.compareTo(o.getContent());
    }

    @Override
    public void writeObject(ObjectOutputStream out) {
        try{
            out.writeObject(this.content);
           // out.writeUTF(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readObject(ObjectInputStream in) {
        try{
            //this.content = in.readUTF();
            this.content = (String)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
