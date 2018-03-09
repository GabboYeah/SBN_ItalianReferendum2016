/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.DAO;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.Entities.User;
import uniroma1.sbn.finalproject.avellinotrappolini.italianreferendum2016.builder.PoliticiansIndexBuilder;

/**
 *
 * @author Gabriele
 */
public class CSVReader {
    
    private BufferedReader br;
    private String delimiter;
    private String path;

    public CSVReader(String delimiter, String path) {
        this.delimiter = delimiter;
        this.path = path;
    }
    
    public ArrayList<String[]> readCSV() throws FileNotFoundException, IOException{
        br = new BufferedReader(new FileReader(path));

        String line;
        ArrayList<String[]> rows = new ArrayList<String[]>();
            
        br.readLine();
                    
        while ((line = br.readLine()) != null) {
            rows.add(line.split(delimiter));
        }
            
        return rows;
    }
}
