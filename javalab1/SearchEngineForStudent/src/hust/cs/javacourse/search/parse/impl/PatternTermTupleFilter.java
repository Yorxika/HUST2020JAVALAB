package hust.cs.javacourse.search.parse.impl;

import hust.cs.javacourse.search.index.AbstractTermTuple;
import hust.cs.javacourse.search.parse.AbstractTermTupleFilter;
import hust.cs.javacourse.search.parse.AbstractTermTupleStream;
import hust.cs.javacourse.search.util.Config;

import java.util.regex.Pattern;

public class PatternTermTupleFilter extends AbstractTermTupleFilter {

    private Pattern pattern = Pattern.compile(Config.TERM_FILTER_PATTERN);

    /**
     * 构造函数
     *
     * @param input ：Filter的输入，类型为AbstractTermTupleStream
     */
    public PatternTermTupleFilter(AbstractTermTupleStream input) {
        super(input);
    }


    /**
     * 构造函数
     * @param input ：Filter的输入，类型为AbstractTermTupleStream
     * @param regex ：设置为正则表达式过滤类型
     */
    public PatternTermTupleFilter(AbstractTermTupleStream input,String regex){
        super(input);
        pattern = Pattern.compile(regex);
    }

    /**
     * 获得下一个三元组
     *
     * @return: 下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        AbstractTermTuple termTuple = input.next();
        if(termTuple == null)
            return null;
        /*Matcher matcher = pattern.matcher(termTuple.term.getContent());
        StringBuilder sb = new StringBuilder();
        while (matcher.find())
            sb.append(matcher.group());
        termTuple.term.setContent(sb.toString());
        if(termTuple.term.getContent().equals(""))
            return null;*/
        while(!termTuple.term.getContent().matches(Config.TERM_FILTER_PATTERN)){
            termTuple = input.next();
            if(termTuple == null)
                return null;
        }
        return termTuple;
    }

}
