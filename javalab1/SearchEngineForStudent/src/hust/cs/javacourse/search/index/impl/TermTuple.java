package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.AbstractTerm;
import hust.cs.javacourse.search.index.AbstractTermTuple;

public class TermTuple extends AbstractTermTuple {


    public TermTuple() {
    }

    public TermTuple(AbstractTerm term,int curPos){
        this.term = term;
        this.curPos = curPos;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        else if(obj instanceof TermTuple){
            TermTuple termTuple = (TermTuple)obj;
            if(termTuple.term != null && this.term != null)
                return termTuple.term.equals(this.term) && termTuple.curPos == this.curPos;
            else if(termTuple.term == null && this.term == null)
                return termTuple.curPos == this.curPos;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.term.toString() + ", position: " + this.curPos + ", freq: " + this.freq;
    }
}
