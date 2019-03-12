package com.threathunter.basictools.correlation.getter;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.IOException;

/**
 * created by www.threathunter.cn
 */
public class NeoFilterMain {

    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts( "depth" ).withRequiredArg().ofType(Integer.class).defaultsTo(3);
        parser.accepts( "output" ).withRequiredArg().ofType(String.class).defaultsTo("");
        parser.accepts( "input" ).withRequiredArg().ofType(String.class).required();
        parser.accepts( "db" ).withRequiredArg().ofType(String.class).required();

        NeoFilterRelationGetter getter = null;
        try {
            OptionSet options = parser.parse(args);
            Integer depth = (Integer)options.valueOf("depth");
            String db = (String)options.valueOf("db");
            String output = (String)options.valueOf("output");
            String input = (String)options.valueOf("input");

            getter = new NeoFilterRelationGetter(db, depth);
            getter.addRelationToNeo(input);
            getter.getNodesNeighbors(output);
        } catch (Exception ex) {
            System.err.println("Invalid command");
            try {
                parser.printHelpOn(System.err);
            } catch (IOException ignore) {
            }
            System.exit(1);
        }
    }
}
