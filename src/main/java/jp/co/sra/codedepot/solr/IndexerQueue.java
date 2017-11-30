/**
* Copyright (c) 2009 SRA (Software Research Associates, Inc.)
*
* This file is part of CodeDepot.
* CodeDepot is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3.0
* as published by the Free Software Foundation and appearing in
* the file GPL.txt included in the packaging of this file.
*
* CodeDepot is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
*
**/
package jp.co.sra.codedepot.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;

import jp.co.sra.codedepot.parser.Parser;

/**
 * IndexerQueue class
 *
 *  @author    nisinaka
 *  @created   2010/01/12 (by nisinaka)
 *  @updated   N/A
 *  @version
 *  @copyright
 *
 * $Id$
 */
public class IndexerQueue {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(IndexerQueue.class.getName());
    }

    protected Indexer indexer;
    protected int numberOfWorkers;
    protected LinkedList<Runnable> queue;
    protected ArrayList<Worker> workers;

    /**
     * Create a new instance of IndexerQueue and initialize it.
     *
     * @param numberOfWorkers int
     * @category Instance creation
     */
    public IndexerQueue(Indexer anIndexer, int numberOfWorkers) {
        assert numberOfWorkers > 1;

        this.indexer = anIndexer;
        this.numberOfWorkers = numberOfWorkers;
        this.queue = new LinkedList<Runnable>();
        this.workers = new ArrayList<Worker>();
    }

    /**
     * Parse the files.
     *
     * @param server org.apache.solr.client.solrj.SolrServer
     * @param parsers java.util.List<Parser>
     * @param files java.util.List<String>
     * @param doIndex boolean
     * @param doCopy boolean
     * @param doHtml boolean
     * @return java.util.List<String>
     * @throws java.lang.Exception
     * @category parsing
     */
    public List<String> parseFiles(final SolrServer server, final List<Parser> parsers, List<String> files, final boolean doIndex, final boolean doCopy, final boolean doHtml) throws Exception {
        final HashMap<String, Integer> pmap = new HashMap<String, Integer>();

        // Prepare the queue.
        for (final String location : files) {
            queue.add(new Runnable() {
                public void run() {
                    try {
                        int dcount = indexer.parseFile(location, server, parsers, doIndex, doCopy, doHtml);
                        if (dcount > 0) {
                            pmap.put(location, dcount);
                        }
                    } catch (Exception e) {
                        logger.warning(e.getMessage());
                    }
                }
            });
        }

        // Create and start the workers.
        synchronized (workers) {
            for (int i = 0; i < numberOfWorkers; i++) {
                workers.add(new Worker());
            }
            for (Worker worker : workers) {
                worker.start();
            }
        }

        // Wait for the workers to be finished.
        synchronized (workers) {
            while (workers.isEmpty() == false) {
                workers.wait();
            }
        }

        // Make a result.
        List<String> plist = new ArrayList<String>(pmap.keySet());
        int dcount = 0;
        for (String location : plist) {
            dcount += pmap.get(location);
        }

        logger.info("regist " + dcount + " fragments for " + plist.size() + " files.");
        return plist;
    }

    ////////////////////////////////////////////////////////////////////////////////

    private class Worker extends Thread {
        public void run() {
            Runnable r;
            while (true) {
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        break;
                    }

                    r = queue.removeFirst();
                }

                try {
                    r.run();
                } catch (RuntimeException e) {
                    logger.warning(e.getMessage());
                }
            }

            synchronized (workers) {
                workers.remove(this);
                workers.notify();
            }
        }
    }

}
