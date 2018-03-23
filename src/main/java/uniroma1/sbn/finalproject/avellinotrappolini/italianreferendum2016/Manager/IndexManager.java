/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

/**
 *
 * @author Gabriele
 */
public abstract class IndexManager {

    /**
     *
     */
    public String indexPath;

    /**
     *
     */
    public IndexReader ir;

    /**
     *
     */
    public IndexSearcher searcher;

    /**
     *
     * @param indexPath
     */
    public IndexManager(String indexPath) {
        this.indexPath = indexPath;
    }

    /**
     *
     * @param sourcePath
     */
    public abstract void create(String sourcePath);

    /**
     *
     * @param sourcePath
     * @param fieldName
     * @param fieldValues
     */
    public abstract void create(String sourcePath, String fieldName, ArrayList<String> fieldValues);

    /**
     *
     * @param fieldName
     * @param fieldValue
     * @param range
     * @return
     */
    public ArrayList<Document> searchForField(String fieldName, String fieldValue, int range) {
        try {
            this.setReader(this.indexPath);

            Query q;
            if (fieldName.equals("date")) {
                BytesRef ref = new BytesRef();
                NumericUtils.longToPrefixCoded(Long.parseLong(fieldValue), 0, ref);
                q = new TermQuery(new Term(fieldName, ref));
            } else {
                q = new TermQuery(new Term(fieldName, fieldValue));
            }

            TopDocs top = searcher.search(q, range);
            ScoreDoc[] hits = top.scoreDocs;

            ArrayList<Document> results = new ArrayList<>();

            Document doc = null;

            for (ScoreDoc entry : hits) {
                doc = searcher.doc(entry.doc);
                results.add(doc);
            }

            return results;

        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();

            return null;
        }
    }

    /**
     *
     * @param fieldName
     * @param fieldValues
     * @param range
     * @return
     */
    public ArrayList<Document> searchForField(String fieldName, ArrayList<String> fieldValues, int range) {

        ArrayList<Document> results = new ArrayList<>();

        for (String fieldValue : fieldValues) {
            results.addAll(searchForField(fieldName, fieldValue, range));
        }

        return results;
    }

    /**
     *
     * @param docs
     * @param fieldName
     * @return
     */
    public ArrayList<String> getFieldValuesList(ArrayList<Document> docs, String fieldName) {
        ArrayList<String> results = new ArrayList<String>();

        for (Document doc : docs) {
            results.add(doc.get(fieldName));
        }

        return results;
    }

    /**
     *
     * @param filterFieldName
     * @param filterFieldValues
     * @param fieldOfInterest
     * @param range
     * @return
     */
    public ArrayList<String> searchFilteredValueField(String filterFieldName,
            ArrayList<String> filterFieldValues, String fieldOfInterest, int range) {
        return getFieldValuesList(searchForField(filterFieldName, filterFieldValues, range), fieldOfInterest);
    }

    /**
     *
     * @param filterFieldName
     * @param filterFieldValue
     * @param fieldOfInterest
     * @param range
     * @return
     */
    public ArrayList<String> searchFilteredValueField(String filterFieldName,
            String filterFieldValue, String fieldOfInterest, int range) {
        return getFieldValuesList(searchForField(filterFieldName, filterFieldValue, range), fieldOfInterest);
    }

    /**
     *
     * @return
     */
    public int getIndexSizes() {
        try {
            Directory dir = new SimpleFSDirectory(new File(indexPath));
            IndexReader ir = DirectoryReader.open(dir);
            return ir.numDocs();

        } catch (IOException ex) {
            System.out.println("---> Problems with source files: IOException <---");
            ex.printStackTrace();
            return -1;
        }
    }

    /**
     *
     * @param indexPath
     * @throws IOException
     */
    public void setReader(String indexPath) throws IOException {
        Directory dir = new SimpleFSDirectory(new File(indexPath));

        ir = DirectoryReader.open(dir);
        searcher = new IndexSearcher(ir);
    }

    /**
     *
     * @param term
     * @param field
     * @return
     */
    public ScoreDoc[] searchTermInAField(String term, String field) {
        try {
            setReader(this.indexPath);
            TermQuery t = new TermQuery(new Term(field, term));
            BooleanQuery query = new BooleanQuery();
            query.add(t, BooleanClause.Occur.MUST);

            TopDocs hits = searcher.search(query, 1000000);
            ScoreDoc[] scoreDocs = hits.scoreDocs;

            return scoreDocs;

        } catch (IOException ex) {
            Logger.getLogger(IndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @param term1
     * @param field1
     * @param term2
     * @param field2
     * @return
     */
    public ScoreDoc[] searchTwoTermsInAField(String term1, String field1, String term2, String field2) {
        TermQuery t1 = new TermQuery(new Term(field1, term1));
        TermQuery t2 = new TermQuery(new Term(field2, term2));
        BooleanQuery query = new BooleanQuery();
        query.add(t1, BooleanClause.Occur.MUST);
        query.add(t2, BooleanClause.Occur.MUST);

        try {
            TopDocs hits = searcher.search(query, 1000000);
            ScoreDoc[] scoreDocs = hits.scoreDocs;

            return scoreDocs;

        } catch (IOException ex) {
            Logger.getLogger(IndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public ScoreDoc[] searchTermsInAField(ArrayList<String> terms, String field) {

        BooleanQuery query = new BooleanQuery();
        for (String term : terms) {
            query.add(new TermQuery(new Term(field, term)), BooleanClause.Occur.SHOULD);
        }

        try {
            TopDocs hits = searcher.search(query, 10000000);
            ScoreDoc[] scoreDocs = hits.scoreDocs;

            return scoreDocs;

        } catch (IOException ex) {
            Logger.getLogger(IndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public ArrayList<Document> getAllDocs() {
        try {
            setReader(this.indexPath);
            ArrayList<Document> results = new ArrayList<Document>();

            for (int i = 0; i < ir.numDocs(); i++) {
                results.add(ir.document(i));
            }

            return results;

        } catch (IOException ex) {
            Logger.getLogger(IndexManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
