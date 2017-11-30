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
package jp.co.sra.codedepot.parser.c;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.cdt.core.*;
import org.eclipse.cdt.core.dom.*;
import org.eclipse.cdt.core.dom.ast.*;

import jp.co.sra.codedepot.parser.c.*;
import jp.co.sra.codedepot.util.c.*;

public class AllIdentifierVisitor extends ASTVisitor {
    /*
     * Privates.
     */
    private class IASTNodeCompare implements Comparator<IASTName> {

        public int compare(IASTName n0, IASTName n1) {
            IASTFileLocation fl0 = n0.getFileLocation();
            IASTFileLocation fl1 = n1.getFileLocation();

            if (fl0 == null || fl1 == null) {
                System.err.printf("fl0 or fl1 null.\n");
                return 0;
            }

            int n0Off = fl0.getNodeOffset();
            int n1Off = fl1.getNodeOffset();

            if (n0Off < n1Off) {
                return -1;
            } else if (n0Off == n1Off) {
                return 0;
            } else {
                return 1;
            }
        }

        public boolean equals(IASTName n0, IASTName n1) {
            IASTFileLocation fl0 = n0.getFileLocation();
            IASTFileLocation fl1 = n1.getFileLocation();
            int n0Off = fl0.getNodeOffset();
            int n1Off = fl1.getNodeOffset();

            return (n0Off == n1Off) ? true : false;
        }

    }


    private String mFilename = null;

    private String getFilename(IASTNode node) {
        String ret = CDTUtils.getFilename(node);
        if (ret == null) {
            ret = mFilename;
        }
        return ret;
    }
    private void setFilename(String filename) {
        mFilename = filename;
    }


    private HashMap<String, ArrayList<IASTName>> mIdentList =
        new HashMap<String, ArrayList<IASTName>>();


    private void addIdent(IASTName node) {
        String key = getFilename(node);
        ArrayList<IASTName> al = mIdentList.get(key);
        if (al == null) {
            /*
             * very first time.
             */
            al = new ArrayList<IASTName>();
            mIdentList.put(key, al);
        }
        al.add(node);
    }


    /*
     * Publics.
     */
    public HashMap<String, IASTName[]> getIdentifiers() {
        Set<String> keySet = mIdentList.keySet();
        String[] keys = (String[])keySet.toArray(new String[keySet.size()]);
        IASTNodeCompare c = new IASTNodeCompare();

        HashMap<String, IASTName[]> ret =
            new HashMap<String, IASTName[]>();


        for (int i = 0; i < keys.length; i++) {
            ArrayList<IASTName> al = mIdentList.get(keys[i]);
            if (al != null) {
                IASTName[] nodes =
                    (IASTName[])al.toArray(new IASTName[al.size()]);
                Arrays.sort(nodes, c);
                ret.put(keys[i], nodes);
            }
        }

        return ret;
    }


    /*
     * The visitor.
     */
    public int visit(IASTName node) {
        String name = node.toString();
        if (name != null && name.length() > 0) {
            addIdent(node);
        }

        return PROCESS_CONTINUE;
    }


    public int visit(IASTProblem node) {
        /*
         * Just in case.
         */
        System.err.printf("%s: line %5d: '%s'\n",
                          getFilename(node),
                          CDTUtils.getStartLine(node),
                          node.getMessage());
        return PROCESS_CONTINUE;
    }


    /*
     * Constructor.
     */
    public AllIdentifierVisitor(String filename) {
        setFilename(filename);

        /*
         * FIXME:
         *	Following initializations are kinda munbo-jumbo. Need
         *	to specify which ones are really needed.
         */

        shouldVisitNames = true;
        shouldVisitDeclarations = true;

        shouldVisitInitializers = true;
        shouldVisitParameterDeclarations = true;

        shouldVisitDeclarators = true;
        shouldVisitDeclSpecifiers = true;

        shouldVisitExpressions = true;

        shouldVisitStatements = true;

        shouldVisitTypeIds = true;
        shouldVisitEnumerators = true;

        shouldVisitTranslationUnit = true;
        shouldVisitProblems = true;
        shouldVisitDesignators = true;

        shouldVisitBaseSpecifiers = true;

        shouldVisitNamespaces = true;
        shouldVisitTemplateParameters = true;
    }
}
