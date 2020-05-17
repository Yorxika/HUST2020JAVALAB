package hust.cs.javacourse.search.index.impl;

import hust.cs.javacourse.search.index.AbstractDocument;
import hust.cs.javacourse.search.index.AbstractDocumentBuilder;
import hust.cs.javacourse.search.index.AbstractIndex;
import hust.cs.javacourse.search.index.AbstractIndexBuilder;
import hust.cs.javacourse.search.util.FileUtil;

import java.io.*;

public class IndexBuilder extends AbstractIndexBuilder {

    private int docNum = 0;

    public IndexBuilder(AbstractDocumentBuilder docBuilder) {
        super(docBuilder);
    }

    @Override
    public AbstractIndex buildIndex(String rootDirectory) {

        //DocumentBuilder documentBuilder = new DocumentBuilder();
        AbstractIndex index = new Index();
        for(String path : FileUtil.list(rootDirectory)){
            AbstractDocument document = docBuilder.build(docNum++,path,new File(path));
            index.addDocument(document);
        }
        return index;
    }

}
