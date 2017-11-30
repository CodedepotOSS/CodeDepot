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
package jp.co.sra.codedepot.parser.pygments;

import jp.co.sra.codedepot.parser.Parser;
import jp.co.sra.codedepot.parser.IndexedCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;

import java.util.Iterator;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import jp.co.sra.codedepot.util.LineRead;
import jp.co.sra.codedepot.util.UniversalReader;
import org.apache.commons.io.FilenameUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class PygmentsParser extends Parser {

    public static final String CLONE_TOKEN_SP = "_";
    public static final String CLONE_ID_TOKEN = "$";
    public static final String CLONE_OP_TOKEN = "+";

    private static final String UTF8_BOM = "\uFEFF";
    private static final Logger logger = LoggerFactory.getLogger(PygmentsParser.class);

    private Object _lockObj = new Object();
    private ProcessBuilder _python = null;

    private String language = null;
    private String[] pattern = {};

    /*
     * Constructor
     */

    public PygmentsParser(String language, String[] pattern) {

        /* set language and pattern */

        this.language = language;
        this.setPattern(pattern);
    }

    public String[] getPattern() {
        return this.pattern;
    }

    public void setPattern(String[] pattern) {
        if (pattern != null && pattern.length > 0) {
            this.pattern = pattern;
        }
    }

    @Override
    public String getLanguageName() {
        return this.language;
    }

    @Override
    public boolean accept(java.io.File file) {
        String path = file.getName().toLowerCase();
        for (String s : this.pattern) {
            if (FilenameUtils.wildcardMatch(path, s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toHtml(IndexedCode ic) {
        IndexedCodeFile icf;
        try {
            icf = (IndexedCodeFile)ic;
            return icf.toHTML();
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public IndexedCodeFile parse(String fid, String uuid, File f) throws Exception {
        IndexedCodeFile indexedCodeFile = null;

        try {
            String contents = UniversalReader.getContents(f);
            if (contents.startsWith(UTF8_BOM)) {
                contents = contents.substring(UTF8_BOM.length());
            }
            char[] text = contents.toCharArray();

            indexedCodeFile = new IndexedCodeFile();
            indexedCodeFile.setId(fid);
            indexedCodeFile.setUuid(uuid);
            indexedCodeFile.setText(text);
            indexedCodeFile.setLang(this.language);

            LineRead rct = new LineRead(text);
            indexedCodeFile.setLineRead(rct);

            List<Token> token = parseToken(contents);
            indexedCodeFile.setToken(correctToken(token));

            List<String> ctoken = getCloneToken(token);
            indexedCodeFile.setClone(ctoken);

        } catch (IOException e) {
            logger.warn("Error in reading file " + f.getAbsolutePath(), e);
        }
        return indexedCodeFile;
    }

    @Override
    public void close() {
    }

    public List<Token> parseToken(String code) {

        List<Token> token = new ArrayList<Token>();

        if (code.isEmpty()) {
            return token;
        }


        String jsonStr;

        try {
            jsonStr = parseCodeWithPython(code);
        }
        catch (java.io.IOException e) {
            logger.warn("IOError", e);
            return token;
        }

        if (jsonStr == null) {
            return token;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonStr);

            int offset = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray elemArray = jsonArray.getJSONArray(i);
                String ttype = elemArray.getString(0);
                String value = elemArray.getString(1);
                if (value.length() > 0) {
                    Token newToken = new Token(ttype, value, offset);
                    offset += value.length();
                    token.add(newToken);
                }
            }

        } catch (JSONException e) {
            logger.warn("JSONException", e);
            return token;
        } catch (OutOfMemoryError oe) {
            logger.warn("OutofMemoryError", oe);
            return token;
        }

        return token;
    }

    public List<Token> correctToken(List<Token> token) {
        Iterator<Token> it = token.iterator();
        Token pt = null;

        while (it.hasNext()) {
            Token t = it.next();
            if (t.getValue().equals("(") && pt != null && pt.getKind().equals("Name")) {
                pt.setKind(pt.getKind() + ".Call");
                pt = null;
            } else {
                if (t.getKind().startsWith("Error") || t.getKind().startsWith("Text")) {
                    continue;
                }
                pt = t;
            }
        }
        return token;
    }

    public ProcessBuilder getPython() {
        if (_python == null) {

            String pythonPath = null;

            String resource = "application.properties";
            try {
                Properties props = new Properties();
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                InputStream stream = loader.getResourceAsStream(resource);
                props.load(stream);
                stream.close();

                pythonPath = props.getProperty("PYTHON_PATH");
            } catch (Exception e) {
                logger.warn("Cannot load resouce: " + resource, e);
            }

            if (pythonPath == null || pythonPath.isEmpty()) {
                pythonPath = "python";
            }

            List<String> command = new ArrayList<String>();
            command.add(pythonPath);
            command.add("codedepot.py");
            command.add(this.language);

            String classPath;
            URL url = Thread.currentThread().getContextClassLoader().getResource("");
            try {
                classPath = new URI(url.toString()).getPath();
            }
            catch (URISyntaxException e) {
                classPath = url.getPath();
            }

            if (!classPath.endsWith("/")) {
                classPath = classPath + "/";
            }
            _python = new ProcessBuilder(command);
            _python.redirectErrorStream(true);
            _python.directory(new File(classPath + "python"));
        }
        return _python;
   }

   public String parseCodeWithPython(String code) throws java.io.IOException {
        synchronized (_lockObj) {
            ProcessBuilder pb = getPython();
            Process python;

            try {
                python = pb.start();
            } catch (IOException e) {
                logger.error("Process start failed", e);
                return null;
            }

            OutputStream ost = python.getOutputStream();
            InputStream ist = python.getInputStream();

            final String inputString = code;
            final StringBuffer sb = new StringBuffer();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(ist, "UTF-8"));
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ost, "UTF-8"));

	    Runnable readerRunner = new Runnable() {
                public void run() {
                     char[] cbuf = new char[1024];
                     while (true) {
                         try {
                             int n = reader.read(cbuf);
                             if (n <= 0) {
                                 break;
                             }
                             sb.append(cbuf, 0, n);
                         } catch (IOException e) {
                             logger.warn("Process IOError", e);
                             break;
                         }
                     }
                     try {
                         reader.close();
                     } catch (IOException e) {
                         logger.warn("Process IOError", e);
                     }
                }
            };

	    Runnable writerRunner = new Runnable(){
                public void run() {
                     try {
                         writer.write(inputString);
                         writer.flush();
                     } catch (IOException e) {
                         logger.warn("Process IOError", e);
                     }
                     try {
                         writer.close();
                     } catch (IOException e) {
                         logger.warn("Process IOError", e);
                     }
                }
	    };

            Thread writerThread = new Thread(writerRunner);
            Thread readerThread = new Thread(readerRunner);

            readerThread.start();
            writerThread.start();

            try {
                writerThread.join();
                readerThread.join();

                int status = python.waitFor();

                return sb.toString();
            } catch (InterruptedException e) {
                logger.warn("Process Interrupted", e);
                return null;
            }
        }
    }

    public List<String> getCloneToken(List<Token> token) {
        List<String>ctoken = new ArrayList<String>();

        if (token.isEmpty()) {
            return ctoken;
        }

        List<Token> stmt = new ArrayList<Token>();
        Iterator<Token> it = token.iterator();

        while (it.hasNext()) {
            Token t = it.next();
            if (stmt.isEmpty()) {
                stmt.add(t);
                continue;
            }
            stmt.add(t);
            if (this.isCloneEndToken(t, stmt)) {
                String term = getCloneTerm(stmt);
                if (term != null) {
                    ctoken.add(term);
                }
                stmt.clear();
                continue;
            }
        }
        if (stmt.size() > 0) {
            String term = getCloneTerm(stmt);
            if (term != null) {
                ctoken.add(term);
            }
            stmt.clear();
        }
        return ctoken;
    }

    public boolean isCloneEndToken(Token t, List<Token>token) {
        return t.getValue().startsWith("\n");
    }

    public boolean isCloneDropToken(Token t) {
        String kind = t.getKind();
        if (kind.endsWith(".Doc")) {
            return true;
        }
        if (kind.startsWith("Comment")) {
            return true;
        }
        if (kind.startsWith("Error")) {
            return true;
        }
        if (kind.startsWith("Text")) {
            return true;
        }
        return false;
    }

    public boolean isCloneOperatorToken(Token t) {
        if (t.getKind().startsWith("Operator") || t.getKind().startsWith("Punctuation")) {
            String value = t.getValue();
            String operators = "<>&|^+-*/%";
            if (operators.indexOf(value.charAt(0)) >= 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isCloneTextToken(Token t) {
        if (t.getKind().startsWith("Text")) {
            return true;
        }
        return false;
    }

    public boolean isCloneRetainToken(Token t) {
        String kind = t.getKind();
        if (kind.startsWith("Keyword")) {
            return true;
        }
        if (kind.startsWith("Operator")) {
            return true;
        }
        if (kind.startsWith("Punctuation")) {
            return true;
        }
        return false;
    }


    public String getCloneTerm(List<Token> tokens) {
        Iterator<Token> it = tokens.iterator();
        StringBuffer sb = new StringBuffer();

        Token st = null;
        Token et = null;

        while (it.hasNext()) {
            Token t = it.next();
            if (t.getValue().isEmpty()) {
                continue;
            }

            String kind = t.getKind();
            if (!this.isCloneDropToken(t)) {
                if (st == null) {
                    st = t;
                }
                if (this.isCloneOperatorToken(t)) {
                    if (sb.length() > 0) {
                        sb.append(CLONE_TOKEN_SP);
                    }
                    sb.append(CLONE_OP_TOKEN);
                } else if (!this.isCloneRetainToken(t)) {
                    if (sb.length() > 0) {
                        sb.append(CLONE_TOKEN_SP);
                    }
                    sb.append(CLONE_ID_TOKEN);
                } else if (t.getValue().length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(CLONE_TOKEN_SP);
                    }
                    sb.append(t.getValue());
                }
                et = t;
            }
        }

        String value = sb.toString();
        if (value.isEmpty()) {
            return null;
        }

        String sp = String.valueOf(st.getOffset());
        String ep = String.valueOf(et.getOffset() + et.getLength());

        StringBuffer term = new StringBuffer();
        term.append(sp);
        term.append(".");
        term.append(ep);
        term.append("@");
        term.append(value);
        return term.toString();
    }
}
